package com.munawarini.arcalculator.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.munawarini.arcalculator.ui.theme.GradientEnd
import com.munawarini.arcalculator.ui.theme.GradientStart
import com.munawarini.arcalculator.ui.theme.arColors
import com.munawarini.arcalculator.utils.UnitConverter
import com.munawarini.arcalculator.viewmodel.ConverterState

/**
 * Unit Converter screen with three categories: Length, Weight, Currency.
 * Features:
 * - Category filter chips
 * - Two unit dropdowns (From / To) with swap button
 * - Large result display card with gradient border
 * - Animated result slide-in
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(
    converterState: ConverterState,
    onCategoryChange: (UnitConverter.Category) -> Unit,
    onFromUnitChange: (String) -> Unit,
    onToUnitChange: (String) -> Unit,
    onInputChange: (String) -> Unit,
    onSwapUnits: () -> Unit,
    modifier: Modifier = Modifier
) {
    val arColors = MaterialTheme.arColors
    val units = UnitConverter.unitsFor(converterState.category)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(arColors.background)
            // NOTE: insets are already applied by parent MainScreen Column.
            .padding(horizontal = 20.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // ── Header ────────────────────────────────────────────────────────
        Text(
            text       = "Unit Converter",
            style      = MaterialTheme.typography.headlineLarge,
            color      = arColors.displayText
        )
        Text(
            text  = "Offline · No internet required",
            color = arColors.mutedText,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // ── Category chips ────────────────────────────────────────────────
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            UnitConverter.Category.entries.forEach { cat ->
                val isSelected = cat == converterState.category
                FilterChip(
                    selected = isSelected,
                    onClick  = { onCategoryChange(cat) },
                    label    = {
                        Text(
                            text = cat.name.lowercase().replaceFirstChar { it.uppercase() },
                            fontSize = 13.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = arColors.accent,
                        selectedLabelColor     = androidx.compose.ui.graphics.Color.White,
                        containerColor         = arColors.surfaceVariant,
                        labelColor             = arColors.mutedText
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ── Input field ───────────────────────────────────────────────────
        OutlinedTextField(
            value = converterState.inputValue,
            onValueChange = onInputChange,
            label = { Text("Enter value", color = arColors.mutedText) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            textStyle = MaterialTheme.typography.headlineMedium.copy(
                color = arColors.displayText,
                fontSize = 28.sp
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor   = arColors.accent,
                unfocusedBorderColor = arColors.glassBorder,
                focusedLabelColor    = arColors.accent,
                cursorColor          = arColors.accent,
                focusedTextColor     = arColors.displayText,
                unfocusedTextColor   = arColors.displayText
            ),
            shape  = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // ── Unit pickers row ──────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            UnitDropdown(
                label      = "From",
                selected   = converterState.fromUnit,
                units      = units,
                onSelect   = onFromUnitChange,
                modifier   = Modifier.weight(1f)
            )

            IconButton(
                onClick = onSwapUnits,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(arColors.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "Swap units",
                    tint = arColors.accent
                )
            }

            UnitDropdown(
                label    = "To",
                selected = converterState.toUnit,
                units    = units,
                onSelect = onToUnitChange,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // ── Result card ───────────────────────────────────────────────────
        AnimatedVisibility(
            visible = converterState.result.isNotEmpty(),
            enter = slideInVertically { it / 2 } + fadeIn(),
            exit  = slideOutVertically { it / 2 } + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(arColors.surface)
                    .border(
                        width = 1.5.dp,
                        brush = Brush.linearGradient(listOf(GradientStart, GradientEnd)),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(vertical = 28.dp, horizontal = 24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text  = converterState.inputValue.ifEmpty { "0" } + " ${converterState.fromUnit}  =",
                        color = arColors.mutedText,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    AnimatedContent(
                        targetState = converterState.result,
                        transitionSpec = {
                            (slideInVertically { -it / 3 } + fadeIn()) togetherWith
                                    (slideOutVertically { it / 3 } + fadeOut())
                        },
                        label = "resultAnim"
                    ) { res ->
                        Text(
                            text       = "$res ${converterState.toUnit}",
                            color      = arColors.displayText,
                            fontSize   = 36.sp,
                            fontWeight = FontWeight.Light,
                            textAlign  = TextAlign.Center,
                            modifier   = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    val fromName = units.find { it.symbol == converterState.fromUnit }?.displayName ?: ""
                    val toName   = units.find { it.symbol == converterState.toUnit }?.displayName ?: ""
                    Text(
                        text  = "$fromName → $toName",
                        color = arColors.mutedText,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

// ── Unit dropdown component ───────────────────────────────────────────────────

@Composable
private fun UnitDropdown(
    label: String,
    selected: String,
    units: List<UnitConverter.UnitInfo>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val arColors = MaterialTheme.arColors
    var expanded by remember { mutableStateOf(false) }
    val selectedUnit = units.find { it.symbol == selected }

    Column(modifier = modifier) {
        Text(label, color = arColors.mutedText, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(arColors.surfaceVariant)
                .border(0.5.dp, arColors.glassBorder, RoundedCornerShape(12.dp))
                .clickable { expanded = true }
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Column {
                Text(
                    text       = selectedUnit?.symbol ?: selected,
                    color      = arColors.displayText,
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text  = selectedUnit?.displayName ?: "",
                    color = arColors.mutedText,
                    fontSize = 11.sp
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(arColors.surface)
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(unit.symbol, color = arColors.displayText, fontWeight = FontWeight.SemiBold)
                            Text(unit.displayName, color = arColors.mutedText, fontSize = 12.sp)
                        }
                    },
                    onClick = {
                        onSelect(unit.symbol)
                        expanded = false
                    }
                )
            }
        }
    }
}
