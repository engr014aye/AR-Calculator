package com.munawarini.arcalculator.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.munawarini.arcalculator.ui.theme.AccentCyan
import com.munawarini.arcalculator.ui.theme.AccentViolet
import com.munawarini.arcalculator.ui.theme.GradientEnd
import com.munawarini.arcalculator.ui.theme.GradientStart
import com.munawarini.arcalculator.ui.theme.TextWhite
import com.munawarini.arcalculator.ui.theme.arColors

// ── Button type enum ──────────────────────────────────────────────────────────

enum class ButtonType { NUMBER, OPERATOR, EQUALS, FUNCTION, CLEAR, SPECIAL }

// ── Shadow draw helper ────────────────────────────────────────────────────────

fun Modifier.coloredShadow(
    color: Color,
    borderRadius: Dp = 20.dp,
    blurRadius: Dp = 16.dp,
    offsetY: Dp = 4.dp,
    offsetX: Dp = 0.dp,
    spread: Float = 0f
): Modifier = this.drawBehind {
    this.drawIntoCanvas {
        val paint = Paint().apply {
            asFrameworkPaint().apply {
                isAntiAlias = true
                this.color = android.graphics.Color.TRANSPARENT
                setShadowLayer(
                    blurRadius.toPx(),
                    offsetX.toPx(),
                    offsetY.toPx(),
                    color.copy(alpha = 0.45f).toArgb()
                )
            }
        }
        it.drawRoundRect(
            left   = spread,
            top    = spread,
            right  = size.width - spread,
            bottom = size.height - spread,
            radiusX = borderRadius.toPx(),
            radiusY = borderRadius.toPx(),
            paint  = paint
        )
    }
}

// ── CalcButton composable ─────────────────────────────────────────────────────

/**
 * Premium glassmorphic calculator button with:
 * - Spring-physics press animation
 * - Haptic feedback on press
 * - Per-type background (glass, gradient, operator, etc.)
 * - Soft shadow for depth
 */
@Composable
fun CalcButton(
    symbol: String,
    type: ButtonType = ButtonType.NUMBER,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    fontSize: androidx.compose.ui.unit.TextUnit = 22.sp,
    onClick: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val arColors = MaterialTheme.arColors
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness    = Spring.StiffnessHigh
        ),
        label = "btnScale"
    )

    val cornerRadius = 20.dp
    val shape = RoundedCornerShape(cornerRadius)

    // Resolve background and text color per type
    val (bgColor, textColor) = when (type) {
        ButtonType.NUMBER   -> arColors.numberBtn   to arColors.displayText
        ButtonType.OPERATOR -> arColors.operatorBtn to arColors.accent
        ButtonType.FUNCTION -> arColors.functionBtn to arColors.accentSecondary
        ButtonType.CLEAR    -> arColors.clearBtn    to Color(0xFFFF6B6B)
        ButtonType.SPECIAL  -> arColors.surfaceHigh to arColors.resultText
        ButtonType.EQUALS   -> Color.Transparent    to TextWhite
    }

    val shadowColor = when (type) {
        ButtonType.EQUALS   -> AccentViolet
        ButtonType.OPERATOR -> arColors.accent.copy(alpha = 0.4f)
        else                -> Color.Black
    }

    Box(
        modifier = modifier
            .scale(scale)
            .coloredShadow(
                color        = shadowColor,
                borderRadius = cornerRadius,
                blurRadius   = if (type == ButtonType.EQUALS) 20.dp else 10.dp,
                offsetY      = if (type == ButtonType.EQUALS) 6.dp else 3.dp
            )
            .clip(shape)
            .then(
                // Equals: gradient fill; others: solid color with glass sheen
                if (type == ButtonType.EQUALS) {
                    Modifier.background(
                        brush = Brush.linearGradient(
                            colors = listOf(GradientStart, GradientEnd),
                            start  = Offset(0f, 0f),
                            end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                        )
                    )
                } else {
                    Modifier.background(bgColor)
                }
            )
            .border(
                width = 0.6.dp,
                color = arColors.glassBorder,
                shape = shape
            )
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        tryAwaitRelease()
                        isPressed = false
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text      = symbol,
            color     = textColor,
            fontSize  = fontSize,
            fontWeight = when (type) {
                ButtonType.NUMBER -> androidx.compose.ui.text.font.FontWeight.Light
                ButtonType.EQUALS -> androidx.compose.ui.text.font.FontWeight.Bold
                else -> androidx.compose.ui.text.font.FontWeight.SemiBold
            },
            textAlign = TextAlign.Center,
            maxLines  = 1
        )
    }
}
