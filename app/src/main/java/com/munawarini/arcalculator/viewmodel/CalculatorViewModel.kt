package com.munawarini.arcalculator.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.munawarini.arcalculator.data.db.CalculationHistory
import com.munawarini.arcalculator.data.repository.HistoryRepository
import com.munawarini.arcalculator.utils.MathParser
import com.munawarini.arcalculator.utils.UnitConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ── Enums & State Models ──────────────────────────────────────────────────────

enum class CalcMode { STANDARD, SCIENTIFIC, CONVERTER }

data class ConverterState(
    val category: UnitConverter.Category = UnitConverter.Category.LENGTH,
    val fromUnit: String = "m",
    val toUnit: String = "km",
    val inputValue: String = "",
    val result: String = ""
)

data class CalculatorUiState(
    val expression: String = "",
    val liveResult: String = "",
    val currentMode: CalcMode = CalcMode.STANDARD,
    val isScientificExpanded: Boolean = false,
    val errorMessage: String? = null,
    val converterState: ConverterState = ConverterState(),
    /** True immediately after '=' is pressed — triggers display animation */
    val justEvaluated: Boolean = false
)

// ── ViewModel ─────────────────────────────────────────────────────────────────

class CalculatorViewModel(
    private val repository: HistoryRepository
) : ViewModel() {

    // ── UI State ─────────────────────────────────────────────────────────────

    private val _uiState = MutableStateFlow(CalculatorUiState())
    val uiState: StateFlow<CalculatorUiState> = _uiState.asStateFlow()

    /** Room history observed as a hot StateFlow. */
    val history = repository.allHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    // ── Calculator Key Actions ────────────────────────────────────────────────

    /**
     * Called for every button press on the keypad.
     * Handles digits, operators, functions, and special tokens.
     */
    fun onKey(key: String) {
        _uiState.update { state ->
            val current = if (state.justEvaluated && key.isDigit()) "" else state.expression
            val resetIfEvaluated = state.justEvaluated && key.isOperator()

            val newExpr = when {
                // After evaluation: operator continues from result, digit starts fresh
                resetIfEvaluated -> {
                    val lastResult = state.liveResult.ifEmpty { state.expression }
                    lastResult + key
                }
                key == "(" || key == ")" -> current + key
                key == "π" || key == "e" -> current + key
                // Function tokens: append with opening paren
                key in listOf("sin", "cos", "tan", "log", "ln", "√", "asin", "acos", "atan", "fact", "abs") -> {
                    val token = if (key == "√") "√(" else "$key("
                    current + token
                }
                else -> current + key
            }

            val preview = MathParser.liveEvaluate(newExpr)
            state.copy(
                expression = newExpr,
                liveResult = preview ?: state.liveResult,
                errorMessage = null,
                justEvaluated = false
            )
        }
    }

    /** Backspace — remove last character or function token. */
    fun onBackspace() {
        _uiState.update { state ->
            if (state.justEvaluated) return@update state.copy(
                expression = "",
                liveResult = "",
                justEvaluated = false
            )

            val expr = state.expression
            if (expr.isEmpty()) return@update state

            // Remove multi-char function tokens cleanly
            val functionTokens = listOf("sin(", "cos(", "tan(", "log(", "ln(", "asin(", "acos(", "atan(", "fact(", "abs(", "√(")
            val newExpr = functionTokens.firstOrNull { expr.endsWith(it) }
                ?.let { expr.dropLast(it.length) }
                ?: expr.dropLast(1)

            val preview = MathParser.liveEvaluate(newExpr)
            state.copy(
                expression = newExpr,
                liveResult = preview ?: "",
                errorMessage = null
            )
        }
    }

    /** Clear (C) — wipe display, keep history. */
    fun onClear() {
        _uiState.update { it.copy(expression = "", liveResult = "", errorMessage = null, justEvaluated = false) }
    }

    /** Evaluate the current expression. Saves result to Room on success. */
    fun onEquals() {
        val currentExpr = _uiState.value.expression
        if (currentExpr.isBlank()) return

        when (val result = MathParser.evaluate(currentExpr)) {
            is MathParser.ParseResult.Success -> {
                val formatted = MathParser.formatResult(result.value)
                _uiState.update {
                    it.copy(
                        expression = currentExpr,
                        liveResult = formatted,
                        errorMessage = null,
                        justEvaluated = true
                    )
                }
                saveToHistory(equation = currentExpr, resultStr = formatted)
            }
            is MathParser.ParseResult.Error -> {
                _uiState.update {
                    it.copy(errorMessage = result.message.ifEmpty { "Error" })
                }
            }
        }
    }

    /** Toggle +/- sign on the current last number. */
    fun onToggleSign() {
        _uiState.update { state ->
            val expr = state.expression
            if (expr.isEmpty()) return@update state
            val newExpr = if (expr.startsWith("-")) expr.drop(1) else "-$expr"
            state.copy(expression = newExpr, liveResult = MathParser.liveEvaluate(newExpr) ?: "")
        }
    }

    /** Insert a percentage — divides last number by 100. */
    fun onPercent() {
        val expr = _uiState.value.expression
        if (expr.isEmpty()) return
        when (val r = MathParser.evaluate("($expr)/100")) {
            is MathParser.ParseResult.Success -> {
                val formatted = MathParser.formatResult(r.value)
                _uiState.update { it.copy(expression = formatted, liveResult = "") }
            }
            is MathParser.ParseResult.Error -> Unit
        }
    }

    // ── Mode ─────────────────────────────────────────────────────────────────

    fun setMode(mode: CalcMode) {
        _uiState.update { it.copy(currentMode = mode) }
    }

    fun toggleScientific() {
        _uiState.update { it.copy(isScientificExpanded = !it.isScientificExpanded) }
    }

    // ── History Actions ───────────────────────────────────────────────────────

    fun loadFromHistory(item: CalculationHistory) {
        _uiState.update {
            it.copy(
                expression = item.result,
                liveResult = "",
                justEvaluated = false,
                errorMessage = null,
                currentMode = CalcMode.STANDARD
            )
        }
    }

    fun deleteHistoryItem(id: Long) {
        viewModelScope.launch { repository.deleteById(id) }
    }

    fun clearAllHistory() {
        viewModelScope.launch { repository.deleteAll() }
    }

    fun updateHistoryLabel(id: Long, label: String?) {
        viewModelScope.launch { repository.updateLabel(id, label) }
    }

    // ── Unit Converter ────────────────────────────────────────────────────────

    fun onConverterInputChange(value: String) {
        _uiState.update { state ->
            val result = computeConversion(
                input = value,
                from = state.converterState.fromUnit,
                to = state.converterState.toUnit,
                category = state.converterState.category
            )
            state.copy(converterState = state.converterState.copy(inputValue = value, result = result))
        }
    }

    fun onConverterCategoryChange(category: UnitConverter.Category) {
        val units = UnitConverter.unitsFor(category)
        val from = units[0].symbol
        val to = units[1].symbol
        _uiState.update { state ->
            state.copy(
                converterState = ConverterState(
                    category = category,
                    fromUnit = from,
                    toUnit = to,
                    inputValue = "",
                    result = ""
                )
            )
        }
    }

    fun onConverterFromUnitChange(unit: String) {
        _uiState.update { state ->
            val result = computeConversion(
                input = state.converterState.inputValue,
                from = unit,
                to = state.converterState.toUnit,
                category = state.converterState.category
            )
            state.copy(converterState = state.converterState.copy(fromUnit = unit, result = result))
        }
    }

    fun onConverterToUnitChange(unit: String) {
        _uiState.update { state ->
            val result = computeConversion(
                input = state.converterState.inputValue,
                from = state.converterState.fromUnit,
                to = unit,
                category = state.converterState.category
            )
            state.copy(converterState = state.converterState.copy(toUnit = unit, result = result))
        }
    }

    fun swapConverterUnits() {
        _uiState.update { state ->
            val cs = state.converterState
            val result = computeConversion(
                input = cs.inputValue,
                from = cs.toUnit,
                to = cs.fromUnit,
                category = cs.category
            )
            state.copy(
                converterState = cs.copy(fromUnit = cs.toUnit, toUnit = cs.fromUnit, result = result)
            )
        }
    }

    // ── Private Helpers ───────────────────────────────────────────────────────

    private fun saveToHistory(equation: String, resultStr: String) {
        viewModelScope.launch {
            repository.insert(
                CalculationHistory(
                    equation = equation,
                    result = resultStr
                )
            )
        }
    }

    private fun computeConversion(
        input: String,
        from: String,
        to: String,
        category: UnitConverter.Category
    ): String {
        val value = input.toDoubleOrNull() ?: return ""
        val converted = UnitConverter.convert(value, from, to, category)
        return UnitConverter.formatResult(converted)
    }

    // ── Extension Helpers ─────────────────────────────────────────────────────

    private fun String.isDigit() = this.length == 1 && this[0].isDigit()
    private fun String.isOperator() = this in listOf("+", "-", "*", "/", "^")
}
