package com.example.data

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

// --- Moshi Data Classes for Gemini Request/Response ---

@JsonClass(generateAdapter = true)
data class GeminiPart(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GeminiContent(
    @Json(name = "parts") val parts: List<GeminiPart>
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    @Json(name = "responseMimeType") val responseMimeType: String? = null,
    @Json(name = "temperature") val temperature: Float? = null
)

@JsonClass(generateAdapter = true)
data class GeminiRequest(
    @Json(name = "contents") val contents: List<GeminiContent>,
    @Json(name = "generationConfig") val generationConfig: GenerationConfig? = null,
    @Json(name = "systemInstruction") val systemInstruction: GeminiContent? = null
)

@JsonClass(generateAdapter = true)
data class GeminiCandidate(
    @Json(name = "content") val content: GeminiContent
)

@JsonClass(generateAdapter = true)
data class GeminiResponse(
    @Json(name = "candidates") val candidates: List<GeminiCandidate>? = null
)

// --- Manhwa Processed Result parsed from Gemini JSON Schema ---

data class ProcessedManhwa(
    val title: String,
    val genre: String,
    val description: String,
    val panels: List<ProcessedPanel>
)

data class ProcessedPanel(
    val panelIndex: Int,
    val description: String,
    val mood: String, // ACTION, HORROR, ROMANCE, MYSTERY
    val animationType: String, // SHAKE, PULSE, ZOOM_IN, SLIDE_IN_LEFT, NONE
    val onomatopoeias: List<ProcessedOnomatopoeia>,
    val visualFxs: List<ProcessedVisualFx>
)

data class ProcessedOnomatopoeia(
    val text: String,
    val xPercent: Float,
    val yPercent: Float,
    val sfxType: String // BOOM, WHOOSH, CLANG, BADUM
)

data class ProcessedVisualFx(
    val fxType: String, // SPEED_LINES, SPARKS, ANGER_VEINS, SWEAT_DROPS, BOKEH_HEARTS
    val intensity: Float
)

/**
 * Client to communicate directly with the Gemini API REST endpoint.
 * Implements a smart fallback if the API Key is empty/missing, maintaining robust visual testing.
 */
object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val MODEL = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()

    /**
     * Sends the details or script prompt of a manhwa to Gemini to design the panel scroll directions, SFX triggers, and mood-music.
     */
    suspend fun analyzeManhwa(title: String, prompt: String, suggestedGenre: String, numPanels: Int = 6): ProcessedManhwa {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY" || apiKey.contains("placeholder", ignoreCase = true)) {
            Log.w(TAG, "Gemini API key is empty or placeholder! Running high-fidelity local procedural parser fallback.")
            return generateProceduralFallback(title, prompt, suggestedGenre, numPanels)
        }

        val requestPrompt = """
            You are an advanced AI comic sound-designer and kinematic director for webtoons.
            Analyze the following manhwa creation inputs to produce a structured JSON design:
            Title: "$title"
            Suggested Genre: "$suggestedGenre"
            User Narrative Prompt/Description:
            "$prompt"

            Construct a cinematic webtoon layout divided into exactly $numPanels consecutive, epic comic panels that tell a cohesive sequence or fight or dramatic event.
            The response MUST be a valid JSON object matching this schema strictly:
            {
              "title": "String (Same as input)",
              "genre": "ACTION | HORROR | ROMANCE | MYSTERY | FANTASY",
              "description": "Summarized narrative description",
              "panels": [
                {
                  "panelIndex": 0 to ${numPanels - 1},
                  "description": "Vivid graphic description of characters/scenery in the panel",
                  "mood": "ACTION | HORROR | ROMANCE | MYSTERY",
                  "animationType": "SHAKE | PULSE | ZOOM_IN | SLIDE_IN_LEFT | NONE",
                  "onomatopoeias": [
                    {
                      "text": "Comic action sound like 'WHOOSH!', 'BADUM...', 'CLANG!', 'BOOM!', 'SLASH!'",
                      "xPercent": 0.1 to 0.9 (horizontal coordinate overlay inside the panel space),
                      "yPercent": 0.1 to 0.9 (vertical coordinate overlay inside the panel space),
                      "sfxType": "BOOM | WHOOSH | CLANG | BADUM"
                    }
                  ],
                  "visualFxs": [
                    {
                      "fxType": "SPEED_LINES | SPARKS | ANGER_VEINS | SWEAT_DROPS | BOKEH_HEARTS",
                      "intensity": 0.1 to 1.0 (float)
                    }
                  ]
                }
              ]
            }
            Important: Ensure ONLY valid JSON is returned in raw layout. DO NOT wrap inside ```json marks. Take into consideration how a reader scrolls vertically, making each panel build on top of the physical suspense of the previous panel.
        """.trimIndent()

        val reqObj = GeminiRequest(
            contents = listOf(
                GeminiContent(parts = listOf(GeminiPart(text = requestPrompt)))
            ),
            generationConfig = GenerationConfig(
                responseMimeType = "application/json",
                temperature = 0.8f
            ),
            systemInstruction = GeminiContent(parts = listOf(GeminiPart(text = "You are a professional full JSON visual-audio director. Only output valid JSON.")))
        )

        try {
            val endpoint = "$BASE_URL?key=$apiKey"
            val adapter = moshi.adapter(GeminiRequest::class.java)
            val jsonReq = adapter.toJson(reqObj)

            val request = Request.Builder()
                .url(endpoint)
                .post(jsonReq.toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                throw Exception("HTTP Error: ${response.code} ${response.message}")
            }

            val bodyStr = response.body?.string() ?: throw Exception("Empty response body")
            val respObj = moshi.adapter(GeminiResponse::class.java).fromJson(bodyStr)
            val jsonText = respObj?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("Could not find generated text in response")

            return parseJsonToProcessedManhwa(jsonText)
        } catch (e: Exception) {
            Log.e(TAG, "Gemini API failed: ${e.message}. Falling back to procedural parser.", e)
            return generateProceduralFallback(title, prompt, suggestedGenre)
        }
    }

    private fun parseJsonToProcessedManhwa(rawJson: String): ProcessedManhwa {
        val root = JSONObject(rawJson)
        val title = root.getString("title")
        val genre = root.getString("genre")
        val description = root.optString("description", "An AI-enhanced interactive manhwa experience.")
        
        val panelsArray = root.getJSONArray("panels")
        val panelsList = mutableListOf<ProcessedPanel>()
        
        for (i in 0 until panelsArray.length()) {
            val panelObj = panelsArray.getJSONObject(i)
            val index = panelObj.getInt("panelIndex")
            val desc = panelObj.getString("description")
            val mood = panelObj.getString("mood")
            val animType = panelObj.optString("animationType", "NONE")
            
            // Parse list of Onomatopoeias
            val sfxArray = panelObj.optJSONArray("onomatopoeias")
            val onomatoList = mutableListOf<ProcessedOnomatopoeia>()
            if (sfxArray != null) {
                for (j in 0 until sfxArray.length()) {
                    val sfxObj = sfxArray.getJSONObject(j)
                    onomatoList.add(
                        ProcessedOnomatopoeia(
                            text = sfxObj.getString("text"),
                            xPercent = sfxObj.getDouble("xPercent").toFloat(),
                            yPercent = sfxObj.getDouble("yPercent").toFloat(),
                            sfxType = sfxObj.getString("sfxType")
                        )
                    )
                }
            }

            // Parse list of Visual Fx
            val fxArray = panelObj.optJSONArray("visualFxs")
            val fxList = mutableListOf<ProcessedVisualFx>()
            if (fxArray != null) {
                for (k in 0 until fxArray.length()) {
                    val fxObj = fxArray.getJSONObject(k)
                    fxList.add(
                        ProcessedVisualFx(
                            fxType = fxObj.getString("fxType"),
                            intensity = fxObj.optDouble("intensity", 1.0).toFloat()
                        )
                    )
                }
            }

            panelsList.add(
                ProcessedPanel(
                    panelIndex = index,
                    description = desc,
                    mood = mood,
                    animationType = animType,
                    onomatopoeias = onomatoList,
                    visualFxs = fxList
                )
            )
        }

        return ProcessedManhwa(title, genre, description, panelsList)
    }

    /**
     * Highly rich physical fallback library. Generates genre-tailored scenarios
     * when there is no API key available.
     */
    fun generateProceduralFallback(title: String, prompt: String, suggestedGenre: String, numPanels: Int = 6): ProcessedManhwa {
        val activeGenre = if (suggestedGenre.isEmpty()) "ACTION" else suggestedGenre.uppercase()
        val defaultTitle = if (title.isEmpty()) "Chronicles of Destiny" else title

        val basePanels = when (activeGenre) {
            "HORROR" -> listOf(
                ProcessedPanel(
                    panelIndex = 0,
                    description = "The abandoned sanity corridor is silent. A flickering fluorescent light creates jittery silhouettes of dripping dust strings.",
                    mood = "HORROR",
                    animationType = "NONE",
                    onomatopoeias = listOf(ProcessedOnomatopoeia("DRIP... DRIP...", 0.2f, 0.4f, "CLANG")),
                    visualFxs = listOf(ProcessedVisualFx("SWEAT_DROPS", 0.6f))
                ),
                ProcessedPanel(
                    panelIndex = 1,
                    description = "A chilling low noise echoes. The reader's breath tightens as a shadow materializes on the adjacent safety mirror glass.",
                    mood = "HORROR",
                    animationType = "PULSE",
                    onomatopoeias = listOf(ProcessedOnomatopoeia("CREEEAK...", 0.7f, 0.3f, "WHOOSH")),
                    visualFxs = listOf(ProcessedVisualFx("SWEAT_DROPS", 0.9f))
                ),
                ProcessedPanel(
                    panelIndex = 2,
                    description = "The shadow stretches elongated arms. Eyes reflect cold green malice.",
                    mood = "HORROR",
                    animationType = "ZOOM_IN",
                    onomatopoeias = listOf(ProcessedOnomatopoeia("BADUM! BADUM!", 0.5f, 0.8f, "BADUM")),
                    visualFxs = listOf(ProcessedVisualFx("ANGER_VEINS", 0.5f))
                ),
                ProcessedPanel(
                    panelIndex = 3,
                    description = "Suddenly, the light fails completely. Static fuzz crackles.",
                    mood = "HORROR",
                    animationType = "SHAKE",
                    onomatopoeias = listOf(ProcessedOnomatopoeia("ZAP!", 0.5f, 0.2f, "CLANG")),
                    visualFxs = listOf(ProcessedVisualFx("SPARKS", 0.8f))
                ),
                ProcessedPanel(
                    panelIndex = 4,
                    description = "Clawing scratching sounds scrape the ceiling tiles. They are directly above!",
                    mood = "HORROR",
                    animationType = "NONE",
                    onomatopoeias = listOf(ProcessedOnomatopoeia("SCRATCH!", 0.3f, 0.1f, "WHOOSH")),
                    visualFxs = listOf(ProcessedVisualFx("SWEAT_DROPS", 1.0f))
                ),
                ProcessedPanel(
                    panelIndex = 5,
                    description = "A massive entity of teeth drops down directly in front. The screen flashes blood red!",
                    mood = "HORROR",
                    animationType = "SHAKE",
                    onomatopoeias = listOf(ProcessedOnomatopoeia("SCREEECH!", 0.5f, 0.5f, "BOOM")),
                    visualFxs = listOf(ProcessedVisualFx("ANGER_VEINS", 1.0f))
                )
            )
            "ROMANCE" -> listOf(
                ProcessedPanel(
                    panelIndex = 0,
                    description = "Sunset at the cherry blossom courtyard. Pink petals fall quietly under the golden gradient sky.",
                    mood = "ROMANCE",
                    animationType = "NONE",
                    onomatopoeias = listOf(),
                    visualFxs = listOf(ProcessedVisualFx("BOKEH_HEARTS", 0.5f))
                ),
                ProcessedPanel(
                    panelIndex = 1,
                    description = "They lock eyes. A flustered red warm flush colors her cheeks. Beautiful bokeh overlays glitter.",
                    mood = "ROMANCE",
                    animationType = "PULSE",
                    onomatopoeias = listOf(ProcessedOnomatopoeia("BADUM...", 0.5f, 0.7f, "BADUM")),
                    visualFxs = listOf(ProcessedVisualFx("BOKEH_HEARTS", 0.8f))
                ),
                ProcessedPanel(
                    panelIndex = 2,
                    description = "He takes a step forward, his fingers brushing a stray cherry blossom leaf from her hair.",
                    mood = "ROMANCE",
                    animationType = "ZOOM_IN",
                    onomatopoeias = listOf(ProcessedOnomatopoeia("SWISH...", 0.4f, 0.3f, "WHOOSH")),
                    visualFxs = listOf(ProcessedVisualFx("BOKEH_HEARTS", 0.9f))
                ),
                ProcessedPanel(
                    panelIndex = 3,
                    description = "She gasps, holding her handbook close, golden particles wrapping them in safety.",
                    mood = "ROMANCE",
                    animationType = "NONE",
                    onomatopoeias = listOf(ProcessedOnomatopoeia("BADUM. BADUM.", 0.6f, 0.8f, "BADUM")),
                    visualFxs = listOf(ProcessedVisualFx("SWEAT_DROPS", 0.4f))
                ),
                ProcessedPanel(
                    panelIndex = 4,
                    description = "He whispers, soft wind sweeping the courtyard: 'I've always looked for you.'",
                    mood = "ROMANCE",
                    animationType = "PULSE",
                    onomatopoeias = listOf(ProcessedOnomatopoeia("SIGH...", 0.3f, 0.5f, "WHOOSH")),
                    visualFxs = listOf(ProcessedVisualFx("BOKEH_HEARTS", 1.0f))
                ),
                ProcessedPanel(
                    panelIndex = 5,
                    description = "They brace each other with a warm hug, locking their fates forever as night falls.",
                    mood = "ROMANCE",
                    animationType = "ZOOM_IN",
                    onomatopoeias = listOf(),
                    visualFxs = listOf(ProcessedVisualFx("BOKEH_HEARTS", 1.0f))
                )
            )
            "MYSTERY" -> listOf(
                ProcessedPanel(
                    panelIndex = 0,
                    description = "Dense mist hangs over an ancient library. Floor-to-ceiling rows of books contain dark historical manuscripts.",
                    mood = "MYSTERY",
                    animationType = "NONE",
                    onomatopoeias = listOf(ProcessedOnomatopoeia("SHHH...", 0.8f, 0.1f, "WHOOSH")),
                    visualFxs = listOf()
                ),
                ProcessedPanel(
                    panelIndex = 1,
                    description = "A clock ticks. A hidden mechanical clockwork lever clicks from behind the shelf.",
                    mood = "MYSTERY",
                    animationType = "PULSE",
                    onomatopoeias = listOf(ProcessedOnomatopoeia("CLICK!", 0.2f, 0.6f, "CLANG")),
                    visualFxs = listOf(ProcessedVisualFx("SPARKS", 0.4f))
                ),
                ProcessedPanel(
                    panelIndex = 2,
                    description = "The heavy bookshelf pivots. It reveals an illuminated underground staircase leading into a deep blue crypt.",
                    mood = "MYSTERY",
                    animationType = "SLIDE_IN_LEFT",
                    onomatopoeias = listOf(ProcessedOnomatopoeia("GROANNN...", 0.5f, 0.5f, "WHOOSH")),
                    visualFxs = listOf(ProcessedVisualFx("SWEAT_DROPS", 0.5f))
                ),
                ProcessedPanel(
                    panelIndex = 3,
                    description = "He walks down, holding a small matchstick. The flame lights a mysterious ancient circular geometric symbol.",
                    mood = "MYSTERY",
                    animationType = "ZOOM_IN",
                    onomatopoeias = listOf(ProcessedOnomatopoeia("PSSST...", 0.4f, 0.4f, "WHOOSH")),
                    visualFxs = listOf(ProcessedVisualFx("SPARKS", 0.7f))
                ),
                ProcessedPanel(
                    panelIndex = 4,
                    description = "A glowing sapphire stone floats in the center, breathing magical light structures.",
                    mood = "MYSTERY",
                    animationType = "PULSE",
                    onomatopoeias = listOf(ProcessedOnomatopoeia("HUMMM...", 0.5f, 0.5f, "BADUM")),
                    visualFxs = listOf(ProcessedVisualFx("SPARKS", 0.9f))
                ),
                ProcessedPanel(
                    panelIndex = 5,
                    description = "As his hand touches the crystal, memories of a thousand years flood his mind. Shards fly!",
                    mood = "MYSTERY",
                    animationType = "SHAKE",
                    onomatopoeias = listOf(ProcessedOnomatopoeia("SHATTER!", 0.5f, 0.3f, "CLANG")),
                    visualFxs = listOf(ProcessedVisualFx("SPARKS", 1.0f))
                )
            )
            else -> listOf( // ACTION
                ProcessedPanel(
                    panelIndex = 0,
                    description = "The arena floor. Dust particles kick up. The swordsman takes a low, offensive stance. Tension builds.",
                    mood = "ACTION",
                    animationType = "NONE",
                    onomatopoeias = listOf(ProcessedOnomatopoeia("WHOOSH...", 0.5f, 0.8f, "WHOOSH")),
                    visualFxs = listOf(ProcessedVisualFx("SPEED_LINES", 0.5f))
                ),
                ProcessedPanel(
                    panelIndex = 1,
                    description = "A massive beast charges forward! Fist crushing the stone floor with shockwaves.",
                    mood = "ACTION",
                    animationType = "SHAKE",
                    onomatopoeias = listOf(ProcessedOnomatopoeia("BOOM!", 0.5f, 0.4f, "BOOM")),
                    visualFxs = listOf(ProcessedVisualFx("SPEED_LINES", 0.8f))
                ),
                ProcessedPanel(
                    panelIndex = 2,
                    description = "The swordsman leaps into the air, sparks trailing his steel sword edge.",
                    mood = "ACTION",
                    animationType = "SLIDE_IN_LEFT",
                    onomatopoeias = listOf(ProcessedOnomatopoeia("SWISHH!", 0.3f, 0.5f, "WHOOSH")),
                    visualFxs = listOf(ProcessedVisualFx("SPARKS", 0.9f))
                ),
                ProcessedPanel(
                    panelIndex = 3,
                    description = "A clash of blades! Red sparks fly where metal crashes. The swordsman’s veins pop in intense exertion.",
                    mood = "ACTION",
                    animationType = "SHAKE",
                    onomatopoeias = listOf(ProcessedOnomatopoeia("CLANG!", 0.5f, 0.3f, "CLANG")),
                    visualFxs = listOf(ProcessedVisualFx("ANGER_VEINS", 0.8f))
                ),
                ProcessedPanel(
                    panelIndex = 4,
                    description = "The beast roars in anger. Fire-like violent shadows wrap around the frame.",
                    mood = "ACTION",
                    animationType = "PULSE",
                    onomatopoeias = listOf(ProcessedOnomatopoeia("GRAWRR!", 0.6f, 0.3f, "BOOM")),
                    visualFxs = listOf(ProcessedVisualFx("ANGER_VEINS", 1.0f))
                ),
                ProcessedPanel(
                    panelIndex = 5,
                    description = "In one instant, he delivers the final slash, slicing the sky in two. The backdrop bursts in explosive kinetic shockwaves!",
                    mood = "ACTION",
                    animationType = "SHAKE",
                    onomatopoeias = listOf(ProcessedOnomatopoeia("KA-CHING!", 0.5f, 0.5f, "BOOM")),
                    visualFxs = listOf(ProcessedVisualFx("SPEED_LINES", 1.0f), ProcessedVisualFx("SPARKS", 1.0f))
                )
            )
        }

        val panels = mutableListOf<ProcessedPanel>()
        for (i in 0 until numPanels) {
            val base = basePanels[i % basePanels.size]
            panels.add(
                ProcessedPanel(
                    panelIndex = i,
                    description = base.description,
                    mood = base.mood,
                    animationType = base.animationType,
                    onomatopoeias = base.onomatopoeias,
                    visualFxs = base.visualFxs
                )
            )
        }

        return ProcessedManhwa(defaultTitle, activeGenre, prompt.ifEmpty { "An AI-sculpted visual sequence of $activeGenre." }, panels)
    }
}
