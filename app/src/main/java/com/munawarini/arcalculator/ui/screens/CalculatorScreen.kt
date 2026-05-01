package com.munawarini.arcalculator.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.munawarini.arcalculator.ui.components.ButtonType
import com.munawarini.arcalculator.ui.components.CalcButton
import com.munawarini.arcalculator.ui.components.DisplayPanel
import com.munawarini.arcalculator.ui.theme.arColors
import com.munawarini.arcalculator.viewmodel.CalculatorUiState

// ── Button data model ─────────────────────────────────────────────────────────

private data class KeyDef(
    val symbol: String,
    val displaySymbol: String = symbol,
    val type: ButtonType = ButtonType.NUMBER,
    val widthWeight: Float = 1f
)

// ── Standard keypad rows ──────────────────────────────────────────────────────

private val standardRows = listOf(
    listOf(
        KeyDef("C",   type = ButtonType.CLEAR),
        KeyDef("+/-", type = ButtonType.SPECIAL),
        KeyDef("%",   type = ButtonType.SPECIAL),
        KeyDef("÷",   type = ButtonType.OPERATOR)
    ),
    listOf(
        KeyDef("7"), KeyDef("8"), KeyDef("9"),
        KeyDef("×", type = ButtonType.OPERATOR)
    ),
    listOf(
        KeyDef("4"), KeyDef("5"), KeyDef("6"),
        KeyDef("-", type = ButtonType.OPERATOR)
    ),
    listOf(
        KeyDef("1"), KeyDef("2"), KeyDef("3"),
        KeyDef("+", type = ButtonType.OPERATOR)
    ),
    listOf(
        KeyDef("(", type = ButtonType.FUNCTION),
        KeyDef("0"),
        KeyDef(".", type = ButtonType.SPECIAL),
        KeyDef("=", type = ButtonType.EQUALS)
    )
)

// ── Scientific extra rows ─────────────────────────────────────────────────────

private val scientificRows = listOf(
    listOf(
        KeyDef("sin",  type = ButtonType.FUNCTION),
        KeyDef("cos",  type = ButtonType.FUNCTION),
        KeyDef("tan",  type = ButtonType.FUNCTION),
        KeyDef("(",    type = ButtonType.FUNCTION)
    ),
    listOf(
        KeyDef("ln",   type = ButtonType.FUNCTION),
        KeyDef("log",  type = ButtonType.FUNCTION),
        KeyDef("√",    type = ButtonType.FUNCTION),
        KeyDef(")",    type = ButtonType.FUNCTION)
    ),
    listOf(
        KeyDef("π",    type = ButtonType.FUNCTION),
        KeyDef("e",    type = ButtonType.FUNCTION),
        KeyDef("^",    "xʸ", type = ButtonType.OPERATOR),
        KeyDef("fact", "n!", type = ButtonType.FUNCTION)
    )
)

// ── CalculatorScreen ──────────────────────────────────────────────────────────

/**
 * The main calculator screen. Handles both Standard and Scientific modes.
 * Scientific rows animate in/out with expandVertically + spring physics.
 */
@Composable
fun CalculatorScreen(
    uiState: CalculatorUiState,
    onKey: (String) -> Unit,
    onBackspace: () -> Unit,
    onClear: () -> Unit,
    onEquals: () -> Unit,
    onToggleSign: () -> Unit,
    onPercent: () -> Unit,
    modifier: Modifier = Modifier
) {
    val arColors = MaterialTheme.arColors

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(arColors.background)
        // NOTE: statusBarsPadding + navigationBarsPadding are handled by
        // the parent MainScreen Column — do NOT apply again here.
    ) {
        // ── Display panel ─────────────────────────────────────────────────
        DisplayPanel(
            expression   = uiState.expression,
            liveResult   = uiState.liveResult,
            errorMessage = uiState.errorMessage,
            modifier     = Modifier
                .fillMaxWidth()
                .weight(1f)
        )

        // ── Scientific rows (animated expand/collapse) ────────────────────
        AnimatedVisibility(
            visible = uiState.isScientificExpanded,
            enter = expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness    = Spring.StiffnessMediumLow
                )
            ) + fadeIn(),
            exit = shrinkVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness    = Spring.StiffnessMedium
                )
            ) + fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .padding(top = 4.dp, bottom = 6.dp),
                // 8dp between sci rows — compact but not touching
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                scientificRows.forEach { row ->
                    KeyRow(
                        keys         = row,
                        onKey        = onKey,
                        onClear      = onClear,
                        onEquals     = onEquals,
                        onToggleSign = onToggleSign,
                        onPercent    = onPercent,
                        isScientific = true   // short pill-shape buttons
                    )
                }
            }
        }

        // ── Standard keypad ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 12.dp)
                .padding(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)  // was 10.dp
        ) {
            standardRows.forEach { row ->
                KeyRow(
                    keys         = row,
                    onKey        = onKey,
                    onClear      = onClear,
                    onEquals     = onEquals,
                    onToggleSign = onToggleSign,
                    onPercent    = onPercent
                )
            }
        }
    }
}

// ── Helper: single keypad row ─────────────────────────────────────────────────

@Composable
private fun KeyRow(
    keys: List<KeyDef>,
    onKey: (String) -> Unit,
    onClear: () -> Unit,
    onEquals: () -> Unit,
    onToggleSign: () -> Unit,
    onPercent: () -> Unit,
    // Scientific buttons use a wider aspect ratio so they stay short
    // (1.6 = width/height → button is wider than tall, preventing vertical crunch)
    // Standard buttons use a 1:1 square ratio.
    isScientific: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        keys.forEach { key ->
            val aspectRatio = if (isScientific) 1.6f * key.widthWeight else key.widthWeight
            CalcButton(
                symbol = key.displaySymbol,
                type   = key.type,
                fontSize = when {
                    isScientific            -> 14.sp   // slightly smaller label for sci pills
                    key.type == ButtonType.EQUALS   -> 26.sp
                    else                            -> 22.sp
                },
                modifier = Modifier
                    .weight(key.widthWeight)
                    .aspectRatio(aspectRatio),
                onClick = {
                    when (key.symbol) {
                        "C"   -> onClear()
                        "="   -> onEquals()
                        "+/-" -> onToggleSign()
                        "%"   -> onPercent()
                        "÷"   -> onKey("/")
                        "×"   -> onKey("*")
                        ")"   -> onKey(")")
                        else  -> onKey(key.symbol)
                    }
                }
            )
        }
    }
}
