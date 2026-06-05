package com.example.audio

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.util.Log
import kotlinx.coroutines.*
import kotlin.math.sin

/**
 * ProceduralAudioEngine is an extremely lightweight, real-time audio synthesizer for Android.
 * It uses AudioTrack in Streaming mode to generate pure synthesized music themes and SFX.
 * By generating PCM waveforms dynamically, it avoids heavy MP3 dependencies and runs offline.
 */
object ProceduralAudioEngine {
    private const val TAG = "ProceduralAudio"
    private const val SAMPLE_RATE = 22050
    private const val BUFFER_SIZE = SAMPLE_RATE / 10 // 0.1s buffer

    private var audioTrack: AudioTrack? = null
    private var isPlaying = false
    private var job: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    // Synthesizer State Parameters (Thread-Safe via Volatiles/State updates)
    @Volatile var activeMood: String = "MYSTERY" // MYSTERY, ROMANCE, HORROR, ACTION
    @Volatile var scrollVelocity: Float = 0f    // Drives dynamic tempo and filter sweep overrides
    @Volatile var synthMasterVolume: Float = 0.5f

    // Trigger states for Sound Effects (SFX) that are injected into the wave generator
    @Volatile private var sfxTriggerType: String? = null
    @Volatile private var sfxProgress: Int = 0
    @Volatile private var sfxDurationSamples: Int = 0

    init {
        try {
            audioTrack = AudioTrack(
                AudioManager.STREAM_MUSIC,
                SAMPLE_RATE,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
                BUFFER_SIZE,
                AudioTrack.MODE_STREAM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize AudioTrack", e)
        }
    }

    @Synchronized
    fun start() {
        if (isPlaying) return
        isPlaying = true
        try {
            audioTrack?.play()
        } catch (e: Exception) {
            Log.e(TAG, "Error playing AudioTrack", e)
        }

        job = scope.launch {
            synthesizeLoop()
        }
    }

    @Synchronized
    fun stop() {
        isPlaying = false
        job?.cancel()
        try {
            audioTrack?.apply {
                stop()
                flush()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping AudioTrack", e)
        }
    }

    /**
     * Triggers a specific procedural sound effect
     */
    fun triggerSfx(type: String) {
        val durationSec = when (type.uppercase()) {
            "BOOM" -> 0.8
            "WHOOSH" -> 0.4
            "CLANG" -> 0.7
            "BADUM" -> 0.4
            else -> 0.5
        }
        sfxDurationSamples = (SAMPLE_RATE * durationSec).toInt()
        sfxProgress = 0
        sfxTriggerType = type.uppercase()
        Log.d(TAG, "SFX triggered: $type for $durationSec seconds")
    }

    /**
     * Main real-time synthesizer buffer filling loop
     */
    private suspend fun synthesizeLoop() {
        val buffer = ShortArray(BUFFER_SIZE)
        var phaseMain = 0.0
        var phaseMod = 0.0
        var phaseHarmonic = 0.0
        var noteCounter = 0L

        // Musical scales according to mood
        // Horror: Diminished/Dissonant triad intervals (Root, Flat 5th, Octave)
        val horrorScale = doubleArrayOf(110.0, 155.6, 220.0, 311.1)
        // Action: Epic heavy minor scale
        val actionScale = doubleArrayOf(87.3, 116.5, 130.8, 174.6, 233.1)
        // Romance: Dreamy major 7th and 9th chord triads
        val romanceScale = doubleArrayOf(261.6, 329.6, 392.0, 493.9, 587.3)
        // Mystery: Serene pentatonic scales
        val mysteryScale = doubleArrayOf(196.0, 220.0, 293.7, 329.6, 440.0)

        while (isPlaying) {
            val scale = when (activeMood.uppercase()) {
                "HORROR" -> horrorScale
                "ACTION" -> actionScale
                "ROMANCE" -> romanceScale
                else -> mysteryScale
            }

            // Calculate current base tempo based on scroll speed
            val baseTempoInSamples = when (activeMood.uppercase()) {
                "ACTION" -> 4000  // Fast notes (approx 160 BPM)
                "HORROR" -> 16000 // Very slow atmospheric chords
                "ROMANCE" -> 9000 // Comfort beat
                else -> 7000      // Ambient pace
            }

            // Scroll velocity increases note speed
            val tempoSamples = (baseTempoInSamples / (1f + (scrollVelocity.coerceIn(0f, 10f) * 0.15f))).toInt()

            for (i in buffer.indices) {
                // Background procedural sequencer
                val noteIdx = ((noteCounter / tempoSamples) % scale.size).toInt()
                val targetFreq = scale[noteIdx]

                // Smooth frequency interpolation
                phaseMain += (2.0 * Math.PI * targetFreq) / SAMPLE_RATE
                if (phaseMain > 2.0 * Math.PI) phaseMain -= 2.0 * Math.PI

                var backgroundSample = 0.0

                when (activeMood.uppercase()) {
                    "ACTION" -> {
                        // Action requires heavier saw-tooth oscillator + harmonic support
                        val saw = ((phaseMain % (2.0 * Math.PI)) / (2.0 * Math.PI)) * 2.0 - 1.0
                        backgroundSample = saw * 0.25
                        
                        // Add an sub-bass sub-harmonic
                        phaseHarmonic += (2.0 * Math.PI * (targetFreq * 0.5)) / SAMPLE_RATE
                        if (phaseHarmonic > 2.0 * Math.PI) phaseHarmonic -= 2.0 * Math.PI
                        backgroundSample += sin(phaseHarmonic) * 0.2
                    }
                    "HORROR" -> {
                        // Chilling eerie vibrato tone using low-freq modulation
                        phaseMod += (2.0 * Math.PI * 6.0) / SAMPLE_RATE // 6Hz vibrato LFO
                        if (phaseMod > 2.0 * Math.PI) phaseMod -= 2.0 * Math.PI
                        val vibrato = sin(phaseMod) * 8.0
                        val modifiedFreq = targetFreq + vibrato
                        phaseMain += (2.0 * Math.PI * modifiedFreq) / SAMPLE_RATE
                        backgroundSample = sin(phaseMain) * 0.35
                    }
                    "ROMANCE" -> {
                        // Dreamy smooth sine waves
                        backgroundSample = sin(phaseMain) * 0.45
                        
                        // Harmonized major third arpeggio overlay
                        phaseHarmonic += (2.0 * Math.PI * (targetFreq * 1.25)) / SAMPLE_RATE
                        if (phaseHarmonic > 2.0 * Math.PI) phaseHarmonic -= 2.0 * Math.PI
                        backgroundSample += sin(phaseHarmonic) * 0.15
                    }
                    else -> {
                        // Mystery: Pure glassy whistle sine waves + mild ambient sweep
                        backgroundSample = sin(phaseMain) * 0.35
                    }
                }

                // Ingest procedural dynamic SFX if active
                var sfxSample = 0.0
                val activeSfx = sfxTriggerType
                if (activeSfx != null && sfxProgress < sfxDurationSamples) {
                    val sfxRatio = sfxProgress.toDouble() / sfxDurationSamples
                    val decay = 1.0 - sfxRatio

                    when (activeSfx) {
                        "BOOM" -> {
                            // Downward pitch decay (Impact-like explosion)
                            val startFreq = 160.0
                            val endFreq = 20.0
                            val currentFreq = startFreq + (endFreq - startFreq) * sfxRatio
                            val sfxPhase = 2.0 * Math.PI * currentFreq * (sfxProgress.toDouble() / SAMPLE_RATE)
                            sfxSample = sin(sfxPhase) * decay * 0.75
                        }
                        "WHOOSH" -> {
                            // High bandwidth frequency sweep (Wind/Attack stroke)
                            val noise = (Math.random() * 2.0 - 1.0)
                            // Simulate dynamic resonant filters sweeping
                            val resonantFreq = 800.0 + sin(sfxRatio * Math.PI) * 1200.0
                            val sfxPhase = 2.0 * Math.PI * resonantFreq * (sfxProgress.toDouble() / SAMPLE_RATE)
                            sfxSample = (noise * 0.2 + sin(sfxPhase) * 0.4) * decay * 0.5
                        }
                        "CLANG" -> {
                            // Metallic disharmonic ring-modulated ringing waves
                            val freq1 = 280.0
                            val freq2 = 410.0
                            val freq3 = 635.0
                            val sfxPhase1 = 2.0 * Math.PI * freq1 * (sfxProgress.toDouble() / SAMPLE_RATE)
                            val sfxPhase2 = 2.0 * Math.PI * freq2 * (sfxProgress.toDouble() / SAMPLE_RATE)
                            val sfxPhase3 = 2.0 * Math.PI * freq3 * (sfxProgress.toDouble() / SAMPLE_RATE)
                            sfxSample = (sin(sfxPhase1) + sin(sfxPhase2) + sin(sfxPhase3)) / 3.0 * decay * 0.6
                        }
                        "BADUM" -> {
                            // Heart-bounding rapid ticks
                            val bpmFactor = if (sfxRatio < 0.4) {
                                // First heartbeat pulse
                                val pulseRatio = sfxRatio / 0.4
                                sin(2.0 * Math.PI * 55.0 * (sfxProgress.toDouble() / SAMPLE_RATE)) * (1.0 - pulseRatio)
                            } else if (sfxRatio in 0.4..0.8) {
                                // Second heartbeat pulse
                                val pulseRatio = (sfxRatio - 0.4) / 0.4
                                sin(2.0 * Math.PI * 65.0 * (sfxProgress.toDouble() / SAMPLE_RATE)) * (1.0 - pulseRatio)
                            } else {
                                0.0
                            }
                            sfxSample = bpmFactor * 0.8
                        }
                    }

                    sfxProgress++
                    if (sfxProgress >= sfxDurationSamples) {
                        sfxTriggerType = null // Exhausted SFX
                    }
                }

                // Sum, scale, convert to PCM 16-bit
                val mixedSignal = (backgroundSample * 0.4 + sfxSample * 0.6) * synthMasterVolume
                val clampedSignal = mixedSignal.coerceIn(-1.0, 1.0)
                buffer[i] = (clampedSignal * 32767.0).toInt().toShort()

                noteCounter++
            }

            // Push buffers synchronously to AudioTrack
            audioTrack?.write(buffer, 0, buffer.size)
            yield() // Yield to allow other coroutines/threads to execute
        }
    }
}
