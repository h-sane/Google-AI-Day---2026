package com.example.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class ManhwaViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "ManhwaViewModel"
    private val database = AppDatabase.getDatabase(application)
    private val repository = ManhwaRepository(database)

    // UI state streams
    val allManhwas: StateFlow<List<ManhwaEntity>> = repository.allManhwas
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _readingManhwaState = MutableStateFlow<UiState<ReadingManhwa>>(UiState.Idle)
    val readingManhwaState: StateFlow<UiState<ReadingManhwa>> = _readingManhwaState.asStateFlow()

    // Pipeline processing states
    private val _pipelineProgress = MutableStateFlow(0f)
    val pipelineProgress: StateFlow<Float> = _pipelineProgress.asStateFlow()

    private val _pipelineStateText = MutableStateFlow("")
    val pipelineStateText: StateFlow<String> = _pipelineStateText.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    init {
        // Enforce automatic seed preloading to ensure instant user satisfaction
        viewModelScope.launch {
            try {
                repository.checkAndSeedSamples()
            } catch (e: Exception) {
                Log.e(TAG, "Failed seeding database samples", e)
            }
        }
    }

    /**
     * Loads a complete stitched manhwa structure with panels and active tags for reading
     */
    fun loadReadingManhwa(manhwaId: Long) {
        viewModelScope.launch {
            _readingManhwaState.value = UiState.Loading
            try {
                val data = repository.getReadingManhwa(manhwaId)
                if (data != null) {
                    _readingManhwaState.value = UiState.Success(data)
                } else {
                    _readingManhwaState.value = UiState.Error("Manhwa universe details not found")
                }
            } catch (e: Exception) {
                _readingManhwaState.value = UiState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }

    /**
     * Closes the active reading session and resets states
     */
    fun closeReadingSession() {
        _readingManhwaState.value = UiState.Idle
    }

    /**
     * Sends the details of an upload record into the Gemini AI sound & kinetic designer pipeline.
     */
    fun uploadAndProcessManhwa(title: String, prompt: String, suggestedGenre: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _pipelineProgress.value = 0.05f
            _pipelineStateText.value = "Establishing contact with Gemini API Ingestion..."
            
            try {
                // Step 1: Initial parsing (simulated connection latency)
                kotlinx.coroutines.delay(800)
                _pipelineProgress.value = 0.20f
                _pipelineStateText.value = "Transmitting story segments to Gemini model..."

                // Step 2: Triggering Gemini analysis
                val requestTime = System.currentTimeMillis()
                viewModelScope.launch {
                    // Update progress during slow analytical queries
                    while (_isAnalyzing.value && _pipelineProgress.value < 0.85f) {
                        kotlinx.coroutines.delay(400)
                        _pipelineProgress.value += 0.03f
                    }
                }

                repository.processUploadedManhwa(title, prompt, suggestedGenre)
                
                _pipelineProgress.value = 0.90f
                _pipelineStateText.value = "Injecting coordinates, onomatopoeias, and synth soundscapes..."
                kotlinx.coroutines.delay(650)
                
                _pipelineProgress.value = 1.0f
                _pipelineStateText.value = "Pipeline processing complete! Ingested into Room Database."
                kotlinx.coroutines.delay(400)
                
                _isAnalyzing.value = false
                onSuccess()
            } catch (e: Exception) {
                Log.e(TAG, "Failed processing upload record", e)
                _pipelineStateText.value = "Pipeline error: ${e.message}"
                _isAnalyzing.value = false
            }
        }
    }

    /**
     * Deletes an individual manhwa universe
     */
    fun deleteManhwa(manhwaId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteManhwa(manhwaId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to delete manhwa", e)
            }
        }
    }
}
