package com.munawarini.arcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.munawarini.arcalculator.data.db.AppDatabase
import com.munawarini.arcalculator.data.repository.HistoryRepository
import com.munawarini.arcalculator.ui.screens.MainScreen
import com.munawarini.arcalculator.ui.theme.ARCalculatorTheme
import com.munawarini.arcalculator.viewmodel.CalculatorViewModel

/**
 * AR-Calculator entry point.
 *
 * Wires up:
 *  - SplashScreen API (instant dismiss, no animation delay)
 *  - Edge-to-edge window insets
 *  - Manual DI: AppDatabase → HistoryRepository → CalculatorViewModel
 *  - ARCalculatorTheme wrapping MainScreen
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen before super.onCreate
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Enable edge-to-edge (transparent status + nav bars)
        // Belt-and-suspenders: ensure decorFitsSystemWindows is false on all API levels
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()

        setContent {
            val context = LocalContext.current

            // Manual dependency injection — no Hilt needed for this scale
            val database   = remember { AppDatabase.getInstance(context) }
            val repository = remember { HistoryRepository(database.calculationHistoryDao()) }
            val viewModel  = remember { CalculatorViewModel(repository) }

            ARCalculatorTheme(darkTheme = isSystemInDarkTheme()) {
                MainScreen(viewModel = viewModel)
            }
        }
    }
}
