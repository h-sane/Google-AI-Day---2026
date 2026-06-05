package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Interactive product requirements document (PRD) viewer.
 * Fulfills the user's specific request to "research about this and then create a full PRD...".
 */
@Composable
fun PrdViewerScreen(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF09090D))
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("prd_screen_container")
    ) {
        // Glowing Header Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFF6A11CB), Color(0xFF2575FC)),
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp)
        ) {
            Column {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x33FFFFFF)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "RESEARCH & STRATEGY SPECIFICATION",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontFamily = FontFamily.Monospace
                    )
                }
                Text(
                    text = "ManhwaAI Product Requirements Document (PRD)",
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    lineHeight = 28.sp
                )
                Text(
                    text = "A blueprint for the next-gen immersive mobile webtoon platforms powered by Generative AI and procedural multimedia engines.",
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Architectural Pillars
        Text(
            text = "Core Product Pillars",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PillarCard(
                title = "AI Orchestration",
                icon = Icons.Default.Star,
                desc = "Gemini-3.5-Flash analyzes panel geometry, registers actions and emotions, and design soundscape mappings.",
                accentColor = Color(0xFFD0BCFF),
                modifier = Modifier.weight(1f)
            )
            PillarCard(
                title = "Adaptive Audio",
                icon = Icons.Default.Settings,
                desc = "Low-latency synthesized waves change note pitch and frequency sweep values based on scroll metrics.",
                accentColor = Color(0xFFF06292),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PillarCard(
                title = "Kinematic FX",
                icon = Icons.Default.Settings,
                desc = "Active scroll indices trigger camera screen shakes, color-grades, custom onomatopoeia pops, and layout scales.",
                accentColor = Color(0xFF4FC3F7),
                modifier = Modifier.weight(1f)
            )
            PillarCard(
                title = "Full Offline",
                icon = Icons.Default.Info,
                desc = "Stitched in a local SQLite/Room scheme, minimizing network footprint and enabling robust reading access.",
                accentColor = Color(0xFF81C784),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Full Interactive Narrative PRD Section
        PrdSectionHeader(title = "1. Technical Architecture Schema", icon = Icons.Default.Settings)
        PrdBodyText(
            text = "During ingestion, raw vertical panel strips are sliced and inspected by the Gemini Multi-Modal parser. Rather than heavy audio/video transcoding on-device which chokes mobile processors and drains batteries, our platform uses a high-performance Client-Side Metadata schema.\n\n" +
                    "The parsed sound, motion, and visual cues are catalogued as lightweight vectors of coordinate percentages and metadata flags in our Room Database. The Webtoon reader then queries these markers dynamically to drive procedural, local synthesizers and particle drawing threads in lockstep with the user's thumb movement, ensuring a perfect 60 FPS performance."
        )

        Spacer(modifier = Modifier.height(16.dp))

        PrdSectionHeader(title = "2. Adaptive Synthesizer Logic", icon = Icons.Default.Settings)
        PrdBodyText(
            text = "The audio track uses procedural generation to output PCM arrays via Java's low-level AudioTrack API. Frequencies and chord sequences are tailored to four distinct thematic tags:\n" +
                    "• EPIC ACTION: High-energy sawtooth waves with double bass subharmonic layers operating at 140-160 Note BPM.\n" +
                    "• SUSPENSE HORROR: Chilling frequency modulated (FM) sine oscillators running with persistent sub-audible 20Hz hums and dissonant minor intervals.\n" +
                    "• MAGIC ROMANCE: Bright major arpeggiator cascades coupled with slow sweep sine wave chord overlays.\n" +
                    "• SERENE MYSTERY: Glassy sine wave whistle chords backed by calming resonance pulses."
        )

        Spacer(modifier = Modifier.height(16.dp))

        PrdSectionHeader(title = "3. Physical Touch Hooking", icon = Icons.Default.Settings)
        PrdBodyText(
            text = "Traditional sound comic readers trigger an MP3 file once a user crosses a threshold line, which feels disjointed and breaks easily if the reader scrolls backwards. Our reader calculates exact focal intersections based on vertical list offsets.\n\n" +
                    "As a panel scrolls through the active screen center:\n" +
                    "1. BACKGROUND SYNTH MOOD is crossfaded gracefully to match the panel's mood.\n" +
                    "2. SCROLL SPEED is mapped directly to the synthesizer oscillator's pulse speed and canvas speed-line offsets.\n" +
                    "3. ONOMATOPOEIA TRIGGERS (e.g. BOOM, WHOOSH, BADUM) generate real-time physical effects, accompanied by custom floating cartoon text overlays that shake and shrink over defined coordinate positions."
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Future Scope Highlights
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF2575FC).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .background(Color(0xFF13131A))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "FUTURE ROADMAP & SCALABILITY",
                    color = Color(0xFF2575FC),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = "Integrating AI Text-To-Speech (TTS) for Dialogs",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "By referencing bounding boxes of speech bubbles and using Gemini's structured output, we blueprint the translation of dialog text into spatial 3D audio tracks. This will allow the characters to literally speak directly from their actual panel layout coordinate as they scroll past.",
                    color = Color.LightGray,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun PillarCard(
    title: String,
    icon: ImageVector,
    desc: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(165.dp)
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13131A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = accentColor,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                color = Color.LightGray,
                fontSize = 10.5.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun PrdSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color(0xFF6A11CB),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )
    }
}

@Composable
fun PrdBodyText(text: String) {
    Text(
        text = text,
        color = Color.LightGray,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        textAlign = TextAlign.Justify,
        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
    )
}
