package com.munawarini.arcalculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ClearAll
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.munawarini.arcalculator.data.db.CalculationHistory
import com.munawarini.arcalculator.ui.components.HistoryItem
import com.munawarini.arcalculator.ui.theme.ErrorRed
import com.munawarini.arcalculator.ui.theme.GradientEnd
import com.munawarini.arcalculator.ui.theme.GradientStart
import com.munawarini.arcalculator.ui.theme.arColors
import kotlinx.coroutines.launch

/**
 * Modal bottom sheet showing the full calculation history ledger.
 *
 * Features:
 * - Ordered newest-first (from Room Flow)
 * - Tap item → [onItemTap] reloads it into the calculator
 * - Per-item swipe-able delete via [HistoryItem] action button
 * - "Clear All" with confirmation dialog
 * - Empty state illustration
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBottomSheet(
    history: List<CalculationHistory>,
    onItemTap: (CalculationHistory) -> Unit,
    onDeleteItem: (Long) -> Unit,
    onClearAll: () -> Unit,
    onLabelChange: (Long, String?) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()
    val arColors = MaterialTheme.arColors
    var showClearConfirm by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = arColors.surface,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 12.dp, bottom = 8.dp)
                    .size(width = 40.dp, height = 4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(arColors.glassBorder)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // ── Header ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = arColors.accent,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "History",
                        style = MaterialTheme.typography.headlineMedium,
                        color = arColors.displayText
                    )
                    Text(
                        text = "  ${history.size} entries",
                        color = arColors.mutedText,
                        fontSize = 13.sp
                    )
                }

                if (history.isNotEmpty()) {
                    IconButton(onClick = { showClearConfirm = true }) {
                        Icon(
                            imageVector = Icons.Default.ClearAll,
                            contentDescription = "Clear all",
                            tint = ErrorRed.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(arColors.glassBorder)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // ── Content ───────────────────────────────────────────────────
            if (history.isEmpty()) {
                // Empty state
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "∑",
                        color = arColors.accent.copy(alpha = 0.2f),
                        fontSize = 72.sp,
                        fontWeight = FontWeight.Thin
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No calculations yet.\nStart calculating!",
                        color = arColors.mutedText,
                        textAlign = TextAlign.Center,
                        fontSize = 15.sp
                    )
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(
                        items = history,
                        key = { it.id }
                    ) { item ->
                        HistoryItem(
                            item = item,
                            onTap = {
                                onItemTap(item)
                                scope.launch { sheetState.hide() }
                                onDismiss()
                            },
                            onDelete = { onDeleteItem(item.id) },
                            onLabelChange = { label -> onLabelChange(item.id, label) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }

    // ── Confirm clear all dialog ──────────────────────────────────────────────
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text("Clear History", color = arColors.displayText) },
            text = {
                Text(
                    "Delete all calculation history? This cannot be undone.",
                    color = arColors.resultText
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onClearAll()
                    showClearConfirm = false
                }) {
                    Text("Clear All", color = ErrorRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = arColors.accent)
                }
            },
            containerColor = arColors.surfaceVariant,
            titleContentColor = arColors.displayText,
            textContentColor = arColors.resultText
        )
    }
}
