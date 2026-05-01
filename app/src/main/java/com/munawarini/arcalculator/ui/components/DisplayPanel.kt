package com.munawarini.arcalculator.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.munawarini.arcalculator.ui.theme.AccentCyan
import com.munawarini.arcalculator.ui.theme.AccentViolet
import com.munawarini.arcalculator.ui.theme.ErrorRed
import com.munawarini.arcalculator.ui.theme.GradientEnd
import com.munawarini.arcalculator.ui.theme.GradientStart
import com.munawarini.arcalculator.ui.theme.arColors

/**
 * The large equation display panel at the top of the calculator.
 *
 * - Shows the current [expression] in large ExtraLight text, right-aligned.
 * - Shows the [liveResult] preview below in medium dimmed text.
 * - Animates vertically when [expression] changes (slide up).
 * - Shows [errorMessage] in red with a fade animation when set.
 */
@Composable
fun DisplayPanel(
    expression: String,
    liveResult: String,
    errorMessage: String?,
    modifier: Modifier = Modifier
) {
    val arColors = MaterialTheme.arColors

    // Gradient overlay at the top edge for depth
    Box(modifier = modifier.fillMaxWidth()) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .animateContentSize(),
            horizontalAlignment = Alignment.End
        ) {

            // ── Error message (animated fade) ─────────────────────────────
            AnimatedContent(
                targetState = errorMessage,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "errorAnim"
            ) { err ->
                if (err != null) {
                    Text(
                        text       = err,
                        color      = ErrorRed,
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign  = TextAlign.End,
                        modifier   = Modifier.fillMaxWidth()
                    )
                } else {
                    // Empty placeholder to maintain layout stability
                    Spacer(modifier = Modifier.height(0.dp))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── Live result preview ───────────────────────────────────────
            AnimatedContent(
                targetState = liveResult,
                transitionSpec = {
                    (slideInVertically { it / 2 } + fadeIn()) togetherWith
                            (slideOutVertically { -it / 2 } + fadeOut())
                },
                label = "previewAnim"
            ) { preview ->
                Text(
                    text       = if (preview.isNotEmpty() && expression.isNotEmpty()) "= $preview" else "",
                    color      = arColors.accent.copy(alpha = 0.7f),
                    fontSize   = 26.sp,
                    fontWeight = FontWeight.Light,
                    textAlign  = TextAlign.End,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                    modifier   = Modifier
                        .fillMaxWidth()
                        .alpha(if (preview.isNotEmpty()) 1f else 0f)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // ── Main expression text (animated slide) ─────────────────────
            AnimatedContent(
                targetState = expression,
                transitionSpec = {
                    (slideInVertically { it / 3 } + fadeIn()) togetherWith
                            (slideOutVertically { -it / 3 } + fadeOut())
                },
                label = "exprAnim"
            ) { expr ->
                // Auto-scale: shrink font as expression grows
                val fontSize = when {
                    expr.length > 20 -> 28.sp
                    expr.length > 14 -> 36.sp
                    expr.length > 9  -> 44.sp
                    else             -> 52.sp
                }

                Text(
                    text       = expr.ifEmpty { "0" },
                    color      = if (errorMessage != null) ErrorRed.copy(alpha = 0.5f) else arColors.displayText,
                    fontSize   = fontSize,
                    fontWeight = FontWeight.ExtraLight,
                    textAlign  = TextAlign.End,
                    maxLines   = 2,
                    overflow   = TextOverflow.Ellipsis,
                    modifier   = Modifier.fillMaxWidth()
                )
            }
        }

        // Subtle gradient fade at the top of the display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp)
                .align(Alignment.TopCenter)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            arColors.background,
                            Color.Transparent
                        )
                    )
                )
        )
    }
}
