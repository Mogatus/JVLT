package com.kardatech.jvlt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kardatech.jvlt.ui.AppScreen
import com.kardatech.jvlt.ui.VocabularyManagementScreen
import com.kardatech.jvlt.ui.VocabularyScreen
import com.kardatech.jvlt.ui.VocabularyViewModel
import com.kardatech.jvlt.ui.theme.VocJetTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VocJetTheme(dynamicColor = false) {
                val viewModel: VocabularyViewModel = viewModel(factory = VocabularyViewModel.Factory)
                
                LaunchedEffect(Unit) {
                    viewModel.seedFromAssets(this@MainActivity)
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (viewModel.currentScreen) {
                            AppScreen.LEARNING -> {
                                VocabularyScreen(
                                    viewModel = viewModel,
                                ) { viewModel.currentScreen = AppScreen.MANAGEMENT }
                            }

                            AppScreen.MANAGEMENT -> {
                                VocabularyManagementScreen(
                                    viewModel = viewModel,
                                ) { viewModel.currentScreen = AppScreen.LEARNING }
                            }
                        }
                    }
                }
            }
        }
    }
}
