package com.munawarini.arcalculator.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.munawarini.arcalculator.ui.components.ModeTabBar
import com.munawarini.arcalculator.ui.theme.arColors
import com.munawarini.arcalculator.viewmodel.CalcMode
import com.munawarini.arcalculator.viewmodel.CalculatorViewModel

/**
 * Root navigation host screen.
 *
 * Layout:
 * ┌────────────────────────────────┐
 * │  Top action bar (icons)        │
 * │  ModeTabBar                    │
 * ├────────────────────────────────┤
 * │                                │
 * │  AnimatedContent page area     │
 * │  (Calculator / Converter)      │
 * │                                │
 * └────────────────────────────────┘
 *
 * History appears as a ModalBottomSheet overlay (not a separate nav destination).
 */
@Composable
fun MainScreen(viewModel: CalculatorViewModel) {
    val uiState   by viewModel.uiState.collectAsState()
    val history   by viewModel.history.collectAsState()
    val arColors = MaterialTheme.arColors

    var showHistory by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(arColors.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // ── Top action bar ─────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // Scientific toggle (only visible in STANDARD mode)
                if (uiState.currentMode == CalcMode.STANDARD || uiState.currentMode == CalcMode.SCIENTIFIC) {
                    IconButton(
                        onClick = { viewModel.toggleScientific() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Functions,
                            contentDescription = "Toggle Scientific",
                            tint = if (uiState.isScientificExpanded)
                                arColors.accent
                            else
                                arColors.mutedText,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                // History button
                IconButton(
                    onClick = { showHistory = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = if (history.isNotEmpty()) arColors.accent else arColors.mutedText,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // ── Mode tab bar ───────────────────────────────────────────────
            ModeTabBar(
                currentMode  = uiState.currentMode,
                onModeChange = { mode ->
                    viewModel.setMode(mode)
                    // Auto-expand scientific when switching to Sci tab
                    if (mode == CalcMode.SCIENTIFIC && !uiState.isScientificExpanded) {
                        viewModel.toggleScientific()
                    } else if (mode == CalcMode.STANDARD && uiState.isScientificExpanded) {
                        // Keep scientific rows open when on standard tab — user controls via Σ button
                    }
                },
                modifier = Modifier.padding(vertical = 8.dp)
            )

            // ── Animated page content ──────────────────────────────────────
            // Direction-aware horizontal slide: tapping a tab to the RIGHT slides
            // the new screen in from the right; tapping LEFT slides from the left.
            // This mirrors the physical position of tabs for a natural iOS-style feel.
            AnimatedContent(
                targetState = uiState.currentMode,
                transitionSpec = {
                    val goingRight = targetState.ordinal > initialState.ordinal
                    val slideIn = slideInHorizontally(
                        animationSpec = spring(
                            stiffness    = Spring.StiffnessMediumLow,
                            dampingRatio = Spring.DampingRatioNoBouncy
                        )
                    ) { if (goingRight) it else -it }
                    val slideOut = slideOutHorizontally(
                        animationSpec = spring(
                            stiffness    = Spring.StiffnessMediumLow,
                            dampingRatio = Spring.DampingRatioNoBouncy
                        )
                    ) { if (goingRight) -it else it }
                    // Fade blends the slide for a premium crossfade feel
                    (slideIn + fadeIn(tween(180))) togetherWith
                            (slideOut + fadeOut(tween(120)))
                },
                label = "pageTransition",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { mode ->
                when (mode) {
                    CalcMode.STANDARD, CalcMode.SCIENTIFIC -> {
                        CalculatorScreen(
                            uiState      = uiState,
                            onKey        = viewModel::onKey,
                            onBackspace  = viewModel::onBackspace,
                            onClear      = viewModel::onClear,
                            onEquals     = viewModel::onEquals,
                            onToggleSign = viewModel::onToggleSign,
                            onPercent    = viewModel::onPercent,
                            modifier     = Modifier.fillMaxSize()
                        )
                    }
                    CalcMode.CONVERTER -> {
                        UnitConverterScreen(
                            converterState    = uiState.converterState,
                            onCategoryChange  = viewModel::onConverterCategoryChange,
                            onFromUnitChange  = viewModel::onConverterFromUnitChange,
                            onToUnitChange    = viewModel::onConverterToUnitChange,
                            onInputChange     = viewModel::onConverterInputChange,
                            onSwapUnits       = viewModel::swapConverterUnits,
                            modifier          = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }

        // ── History bottom sheet overlay ───────────────────────────────────
        if (showHistory) {
            HistoryBottomSheet(
                history       = history,
                onItemTap     = { item ->
                    viewModel.loadFromHistory(item)
                    showHistory = false
                },
                onDeleteItem  = viewModel::deleteHistoryItem,
                onClearAll    = viewModel::clearAllHistory,
                onLabelChange = viewModel::updateHistoryLabel,
                onDismiss     = { showHistory = false }
            )
        }
    }
}
