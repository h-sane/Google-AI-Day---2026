package com.example.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "manhwas")
data class ManhwaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val author: String,
    val genre: String, // ACTION, HORROR, ROMANCE, MYSTERY, FANTASY
    val description: String,
    val coverUrl: String,
    val isProcessed: Boolean = false,
    val imageSeed: Long = 12345L // Drives procedural panel generation
)

@Entity(
    tableName = "panels",
    foreignKeys = [
        ForeignKey(
            entity = ManhwaEntity::class,
            parentColumns = ["id"],
            childColumns = ["manhwaId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("manhwaId")]
)
data class PanelEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val manhwaId: Long,
    val panelIndex: Int,
    val description: String,
    val mood: String, // ACTION, HORROR, ROMANCE, MYSTERY
    val topOffsetPercent: Float,     // Segment trigger boundaries from 0.0 to n.0
    val bottomOffsetPercent: Float,
    val animationType: String = "NONE" // SHAKE, PULSE, ZOOM_IN, SLIDE_IN_LEFT, NONE
)

@Entity(
    tableName = "onomatopoeias",
    foreignKeys = [
        ForeignKey(
            entity = PanelEntity::class,
            parentColumns = ["id"],
            childColumns = ["panelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("panelId")]
)
data class OnomatopoeiaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val panelId: Long,
    val text: String,
    val xPercent: Float,
    val yPercent: Float,
    val sfxType: String, // BOOM, WHOOSH, CLANG, BADUM
    val scale: Float = 1.0f
)

@Entity(
    tableName = "visual_fx",
    foreignKeys = [
        ForeignKey(
            entity = PanelEntity::class,
            parentColumns = ["id"],
            childColumns = ["panelId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("panelId")]
)
data class VisualFxEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val panelId: Long,
    val fxType: String, // SPEED_LINES, SPARKS, ANGER_VEINS, SWEAT_DROPS, BOKEH_HEARTS
    val intensity: Float = 1.0f
)
