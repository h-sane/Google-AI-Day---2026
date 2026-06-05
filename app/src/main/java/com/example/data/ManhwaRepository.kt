package com.example.data

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class ReadingManhwa(
    val manhwa: ManhwaEntity,
    val panels: List<ReadingPanel>
)

data class ReadingPanel(
    val panel: PanelEntity,
    val onomatopoeias: List<OnomatopoeiaEntity>,
    val visualFxs: List<VisualFxEntity>
)

class ManhwaRepository(private val database: AppDatabase) {
    private val TAG = "ManhwaRepository"
    
    val allManhwas: Flow<List<ManhwaEntity>> = database.manhwaDao().getAllManhwas()

    /**
     * Preloads high-fidelity default samples into the database if empty.
     */
    suspend fun checkAndSeedSamples() = withContext(Dispatchers.IO) {
        val existing = database.manhwaDao().getAllManhwas().firstOrNull()
        if (!existing.isNullOrEmpty()) {
            return@withContext
        }

        Log.d(TAG, "Database is empty. Seeding gorgeous preloaded manhwa universes.")

        // 1. Epic Action: "Ragnarok Overdrive: The Zenith Strike"
        val actionManhwa = ManhwaEntity(
            title = "Ragnarok Overdrive",
            author = "AI Director Jin",
            genre = "ACTION",
            description = "The last bastion of humankind stands against the Frost Titan. Sparks fly, blades clash, and the arena shakes under devastating kinetic tremors.",
            coverUrl = "",
            isProcessed = true,
            imageSeed = 422026L
        )
        seedManhwaWithSample(actionManhwa, GeminiClient.generateProceduralFallback(
            actionManhwa.title, actionManhwa.description, actionManhwa.genre
        ))

        // 2. Chilling Horror: "Sanity Corridor: 3 AM"
        val horrorManhwa = ManhwaEntity(
            title = "Sanity Corridor: 3 AM",
            author = "Shin Eun-Ji",
            genre = "HORROR",
            description = "A paramedic gets trapped inside a shuttered hospital wing where shadows stretch, fluorescent tubes crack, and things crawl on the high ceilings.",
            coverUrl = "",
            isProcessed = true,
            imageSeed = 666300L
        )
        seedManhwaWithSample(horrorManhwa, GeminiClient.generateProceduralFallback(
            horrorManhwa.title, horrorManhwa.description, horrorManhwa.genre
        ))

        // 3. Cherry Blossom Romance: "Courtyard Warmth"
        val romanceManhwa = ManhwaEntity(
            title = "Courtyard Warmth",
            author = "Lee Seo-Ah",
            genre = "ROMANCE",
            description = "A heart-bounding encounter in the school courtyard under falling cherry blossom petals. A story of a long-lost promise finally fulfilled.",
            coverUrl = "",
            isProcessed = true,
            imageSeed = 777100L
        )
        seedManhwaWithSample(romanceManhwa, GeminiClient.generateProceduralFallback(
            romanceManhwa.title, romanceManhwa.description, romanceManhwa.genre
        ))
    }

    private suspend fun seedManhwaWithSample(manhwa: ManhwaEntity, data: ProcessedManhwa) {
        val manhwaId = database.manhwaDao().insertManhwa(manhwa)
        for (p in data.panels) {
            val hSpan = 0.20f // Each panel active across 20% scroll depth spans
            val top = p.panelIndex * hSpan
            val bottom = top + hSpan

            val panelDb = PanelEntity(
                manhwaId = manhwaId,
                panelIndex = p.panelIndex,
                description = p.description,
                mood = p.mood,
                topOffsetPercent = top,
                bottomOffsetPercent = bottom,
                animationType = p.animationType
            )
            val panelId = database.panelDao().insertPanel(panelDb)

            // Insert Onomatopoeias
            val onomatos = p.onomatopoeias.map {
                OnomatopoeiaEntity(
                    panelId = panelId,
                    text = it.text,
                    xPercent = it.xPercent,
                    yPercent = it.yPercent,
                    sfxType = it.sfxType
                )
            }
            database.onomatopoeiaDao().insertOnomatopoeias(onomatos)

            // Insert Visual Fxs
            val fxs = p.visualFxs.map {
                VisualFxEntity(
                    panelId = panelId,
                    fxType = it.fxType,
                    intensity = it.intensity
                )
            }
            database.visualFxDao().insertVisualFxs(fxs)
        }
    }

    /**
     * Executes the actual Gemini AI pipeline to process and ingest an uploaded Manhwa record.
     */
    suspend fun processUploadedManhwa(title: String, prompt: String, suggestedGenre: String) = withContext(Dispatchers.IO) {
        // Core step: Analyze narrative with Gemini API/Backup Fallback
        val result = GeminiClient.analyzeManhwa(title, prompt, suggestedGenre)
        
        // Write the resulting structure safely into Room Db
        val seed = (System.currentTimeMillis() % 1000000)
        val manhwa = ManhwaEntity(
            title = result.title,
            author = "AI Creator Pipeline",
            genre = result.genre,
            description = result.description,
            coverUrl = "",
            isProcessed = true,
            imageSeed = seed
        )
        seedManhwaWithSample(manhwa, result)
    }

    /**
     * Retrieves a full stitched structure enclosing panels and multi-media overlays for the reader viewport.
     */
    suspend fun getReadingManhwa(manhwaId: Long): ReadingManhwa? = withContext(Dispatchers.IO) {
        val manhwa = database.manhwaDao().getManhwaById(manhwaId) ?: return@withContext null
        val panels = database.panelDao().getPanelsForManhwa(manhwaId)
        
        if (panels.isEmpty()) return@withContext ReadingManhwa(manhwa, emptyList())

        val panelIds = panels.map { it.id }
        
        val onomatopoeias = database.onomatopoeiaDao().getOnomatopoeiasForPanels(panelIds)
        val visualFxs = database.visualFxDao().getVisualFxForPanels(panelIds)

        val readingPanels = panels.map { panel ->
            ReadingPanel(
                panel = panel,
                onomatopoeias = onomatopoeias.filter { it.panelId == panel.id },
                visualFxs = visualFxs.filter { it.panelId == panel.id }
            )
        }

        ReadingManhwa(manhwa, readingPanels)
    }

    suspend fun deleteManhwa(manhwaId: Long) = withContext(Dispatchers.IO) {
        database.manhwaDao().deleteManhwaById(manhwaId)
    }
}
