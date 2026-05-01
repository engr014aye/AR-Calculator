package com.munawarini.arcalculator.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.munawarini.arcalculator.ui.theme.GradientEnd
import com.munawarini.arcalculator.ui.theme.GradientStart
import com.munawarini.arcalculator.ui.theme.arColors
import com.munawarini.arcalculator.viewmodel.CalcMode

/**
 * A pill-shaped animated tab bar for switching between calculator modes.
 *
 * The active indicator uses BoxWithConstraints to derive its width at runtime,
 * so the pill always covers exactly 1/3 of the bar on any screen size.
 * The indicator slides between tabs with tuned spring physics.
 * Each tab label cross-fades its color via animateColorAsState.
 */
@Composable
fun ModeTabBar(
    currentMode: CalcMode,
    onModeChange: (CalcMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic   = LocalHapticFeedback.current
    val arColors = MaterialTheme.arColors

    val tabs = listOf(
        CalcMode.STANDARD   to "Calc",
        CalcMode.SCIENTIFIC to "Sci",
        CalcMode.CONVERTER  to "Convert"
    )

    val selectedIndex = tabs.indexOfFirst { it.first == currentMode }.coerceAtLeast(0)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(44.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(arColors.surfaceVariant)
            .border(
                width = 0.5.dp,
                color = arColors.glassBorder,
                shape = RoundedCornerShape(22.dp)
            )
    ) {
        // ── Sliding indicator ─────────────────────────────────────────────────
        // BoxWithConstraints gives us the real pixel width of the pill bar
        // at composition time, so the indicator always covers exactly 1/3.
        BoxWithConstraints(modifier = Modifier.matchParentSize()) {
            val tabWidth  = maxWidth / tabs.size          // exact 1/3 width
            val pillWidth = tabWidth                      // indicator fills its tab slot

            val indicatorOffset by animateDpAsState(
                targetValue = tabWidth * selectedIndex,
                animationSpec = spring(
                    stiffness    = Spring.StiffnessMediumLow,
                    dampingRatio = Spring.DampingRatioLowBouncy
                ),
                label = "tabIndicator"
            )

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = indicatorOffset)
                    .width(pillWidth)
                    .height(36.dp)
                    .padding(horizontal = 4.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                GradientStart.copy(alpha = 0.92f),
                                GradientEnd.copy(alpha = 0.92f)
                            )
                        )
                    )
            )
        }

        // ── Tab labels ────────────────────────────────────────────────────────
        Row(modifier = Modifier.fillMaxWidth()) {
            tabs.forEachIndexed { index, (mode, label) ->
                val isSelected = index == selectedIndex

                // Smooth colour cross-fade on selection change
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) androidx.compose.ui.graphics.Color.White
                                  else arColors.mutedText,
                    animationSpec = spring(stiffness = Spring.StiffnessMedium),
                    label        = "tabColor_$index"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onModeChange(mode)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = label,
                        color      = textColor,
                        fontSize   = 13.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        textAlign  = TextAlign.Center
                    )
                }
            }
        }
    }
}
