package com.munawarini.arcalculator.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Label
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.munawarini.arcalculator.data.db.CalculationHistory
import com.munawarini.arcalculator.ui.theme.AccentViolet
import com.munawarini.arcalculator.ui.theme.ErrorRed
import com.munawarini.arcalculator.ui.theme.GradientEnd
import com.munawarini.arcalculator.ui.theme.GradientStart
import com.munawarini.arcalculator.ui.theme.arColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A single history entry card in the history bottom sheet.
 * Shows equation, result, formatted timestamp, and optional user label.
 * Inline label editing with a [BasicTextField].
 */
@Composable
fun HistoryItem(
    item: CalculationHistory,
    onTap: () -> Unit,
    onDelete: () -> Unit,
    onLabelChange: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val arColors = MaterialTheme.arColors
    var isEditingLabel by remember { mutableStateOf(false) }
    var labelText by remember(item.id) { mutableStateOf(item.label ?: "") }

    val timeStr = remember(item.timestamp) {
        SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()).format(Date(item.timestamp))
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(arColors.surface)
            .border(0.5.dp, arColors.glassBorder, RoundedCornerShape(16.dp))
            .animateContentSize()
    ) {
        // Left accent bar — gradient stripe
        Box(
            modifier = Modifier
                .width(3.dp)
                .height(72.dp)
                .align(Alignment.CenterStart)
                .background(
                    brush = Brush.verticalGradient(listOf(GradientStart, GradientEnd))
                )
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            // Top row: equation + delete button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Optional label
                    if (item.label != null && !isEditingLabel) {
                        Text(
                            text       = item.label,
                            color      = arColors.accent.copy(alpha = 0.8f),
                            fontSize   = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                    // Equation
                    Text(
                        text       = item.equation,
                        color      = arColors.resultText,
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.Normal,
                        maxLines   = 2
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Result
                    Text(
                        text       = "= ${item.result}",
                        color      = arColors.displayText,
                        fontSize   = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Timestamp
                    Text(
                        text  = timeStr,
                        color = arColors.mutedText,
                        fontSize = 11.sp
                    )
                }

                // Action buttons
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = ErrorRed.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            isEditingLabel = !isEditingLabel
                            if (!isEditingLabel) onLabelChange(labelText.ifEmpty { null })
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Label,
                            contentDescription = "Label",
                            tint = arColors.accent.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Inline label editor
            if (isEditingLabel) {
                Spacer(modifier = Modifier.height(8.dp))
                BasicTextField(
                    value = labelText,
                    onValueChange = { labelText = it },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall.copy(
                        color = arColors.displayText,
                        fontSize = 13.sp
                    ),
                    cursorBrush = SolidColor(arColors.accent),
                    decorationBox = { inner ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(arColors.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            if (labelText.isEmpty()) {
                                Text("Add a label…", color = arColors.mutedText, fontSize = 13.sp)
                            }
                            inner()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Tap overlay to reload into calc
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(16.dp))
                .then(
                    Modifier
                        .padding(start = 3.dp) // don't cover accent stripe
                )
        )
    }
}
