package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.ui.DashboardScreen
import com.example.ui.ManhwaViewModel
import com.example.ui.UiState
import com.example.ui.WebtoonReaderScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModelByLazy by viewModels<ManhwaViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF09090D))
                ) { innerPadding ->
                    val manhwas by viewModelByLazy.allManhwas.collectAsState()
                    val readingState by viewModelByLazy.readingManhwaState.collectAsState()
                    val progress by viewModelByLazy.pipelineProgress.collectAsState()
                    val progressText by viewModelByLazy.pipelineStateText.collectAsState()
                    val isAnalyzing by viewModelByLazy.isAnalyzing.collectAsState()

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (val state = readingState) {
                            is UiState.Success -> {
                                WebtoonReaderScreen(
                                    readingManhwa = state.data,
                                    onBack = { viewModelByLazy.closeReadingSession() }
                                )
                            }
                            is UiState.Loading -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFF0D0D11)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = Color(0xFF2575FC))
                                }
                            }
                            is UiState.Error -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFF0D0D11)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "Error: ${state.message}", color = Color.Red, modifier = Modifier.padding(16.dp))
                                }
                            }
                            else -> {
                                DashboardScreen(
                                    manhwas = manhwas,
                                    isPipelineAnalyzing = isAnalyzing,
                                    pipelineProgress = progress,
                                    pipelineProgressText = progressText,
                                    onReadManhwa = { id -> viewModelByLazy.loadReadingManhwa(id) },
                                    onUploadManhwa = { title, desc, genre ->
                                        viewModelByLazy.uploadAndProcessManhwa(title, desc, genre) {}
                                    },
                                    onDeleteManhwa = { id -> viewModelByLazy.deleteManhwa(id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
