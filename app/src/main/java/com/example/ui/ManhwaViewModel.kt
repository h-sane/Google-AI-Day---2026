package com.example.ui

import android.app.Application
import android.content.Context
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

data class UserSession(val username: String, val role: String) // role is "CREATOR" or "READER"

class ManhwaViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "ManhwaViewModel"
    private val database = AppDatabase.getDatabase(application)
    private val repository = ManhwaRepository(database)
    private val sharedPrefs = application.getSharedPreferences("manhwa_ai_prefs", Context.MODE_PRIVATE)

    // User session states
    private val _userSession = MutableStateFlow<UserSession?>(null)
    val userSession: StateFlow<UserSession?> = _userSession.asStateFlow()

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
        // Load saved session if exists
        val savedName = sharedPrefs.getString("saved_username", null)
        val savedRole = sharedPrefs.getString("saved_role", null)
        if (savedName != null && savedRole != null) {
            _userSession.value = UserSession(savedName, savedRole)
        }

        // Enforce automatic seed preloading to ensure instant user satisfaction
        viewModelScope.launch {
            try {
                repository.checkAndSeedSamples()
            } catch (e: Exception) {
                Log.e(TAG, "Failed seeding database samples", e)
            }
        }
    }

    fun login(username: String, role: String) {
        sharedPrefs.edit()
            .putString("saved_username", username)
            .putString("saved_role", role)
            .apply()
        _userSession.value = UserSession(username, role)
    }

    fun logout() {
        sharedPrefs.edit()
            .remove("saved_username")
            .remove("saved_role")
            .apply()
        _userSession.value = null
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
    fun uploadAndProcessManhwa(
        title: String, 
        prompt: String, 
        suggestedGenre: String, 
        zipUri: android.net.Uri? = null,
        imageUris: List<android.net.Uri>? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _pipelineProgress.value = 0.05f
            _pipelineStateText.value = "Establishing contact with Gemini API Ingestion..."
            
            try {
                // Step 1: Initial parsing (simulated connection latency)
                kotlinx.coroutines.delay(600)
                
                val localPaths = mutableListOf<String>()
                val context = getApplication<Application>()
                
                if (zipUri != null) {
                    _pipelineProgress.value = 0.15f
                    _pipelineStateText.value = "Unpacking and sorting ZIP manhwa pages..."
                    val destFolder = "manhwa_zip_${System.currentTimeMillis()}"
                    val extracted = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        com.example.util.FileHelper.unpackZipManhwa(context, zipUri, destFolder)
                    }
                    localPaths.addAll(extracted)
                } else if (!imageUris.isNullOrEmpty()) {
                    _pipelineProgress.value = 0.15f
                    _pipelineStateText.value = "Caching and ordering selected page images..."
                    val destFolder = "manhwa_imgs_${System.currentTimeMillis()}"
                    val copied = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                        com.example.util.FileHelper.copyImagesToStorage(context, imageUris, destFolder)
                    }
                    localPaths.addAll(copied)
                }
                
                _pipelineProgress.value = 0.30f
                _pipelineStateText.value = "Analyzing scenes with Gemini model..."

                // Step 2: Triggering Gemini analysis
                val progressJob = viewModelScope.launch {
                    // Update progress during slow analytical queries
                    while (_isAnalyzing.value && _pipelineProgress.value < 0.85f) {
                        kotlinx.coroutines.delay(400)
                        _pipelineProgress.value += 0.03f
                    }
                }

                val creatorAuthor = _userSession.value?.username ?: "AI Creator"
                val pathsArg = if (localPaths.isNotEmpty()) localPaths else null
                repository.processUploadedManhwa(title, prompt, suggestedGenre, creatorAuthor, pathsArg)
                progressJob.cancel()
                
                _pipelineProgress.value = 0.90f
                _pipelineStateText.value = "Injecting dynamic multimedia overlays & mood soundtracks..."
                kotlinx.coroutines.delay(650)
                
                _pipelineProgress.value = 1.0f
                _pipelineStateText.value = "Saga processing complete! Caches ingested into Room database."
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
