package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PrdViewerScreen(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("prd_screen_container")
    ) {
        // Glowing Bubbly Banner Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRoundRect(
                        color = Color.Black,
                        topLeft = Offset(8f, 8f),
                        size = size,
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                    )
                }
                .background(Color(0xFFFFEA79), RoundedCornerShape(16.dp))
                .border(3.dp, Color.Black, RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column {
                Box(
                    modifier = Modifier
                        .background(Color.Black, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "RESEARCH & STRATEGY SPECIFICATION",
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "ManhwaAI Product Requirements Document (PRD)",
                    color = Color.Black,
                    fontSize = 22.sp,
                    fontFamily = FontFamily.Cursive,
                    fontWeight = FontWeight.Black,
                    lineHeight = 28.sp
                )
                Text(
                    text = "A blueprint for next-gen immersive mobile webtoon platforms powered by Generative AI and procedural multimedia scroll engines.",
                    color = Color(0xFF222222),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Architectural Pillars
        Text(
            text = "Core Product Pillars ⚡",
            color = Color.Black,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Cursive,
            fontSize = 20.sp,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PillarCard(
                title = "AI Orchestration",
                icon = Icons.Default.Star,
                desc = "Gemini multi-modal pipeline analyzes panel regions, registers actions/expressions, and tags sound markers.",
                accentColor = Color(0xFFFF5252),
                modifier = Modifier.weight(1f)
            )
            PillarCard(
                title = "Adaptive Audio",
                icon = Icons.Default.Settings,
                desc = "Low-latency synthesized waves dynamically shift pitch and frequency based on scrolling metrics.",
                accentColor = Color(0xFFFF4081),
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
                desc = "Scroll indices trigger camera frame shakes, dynamic speedlines, minor/major chords, and speech pops.",
                accentColor = Color(0xFF40C4FF),
                modifier = Modifier.weight(1f)
            )
            PillarCard(
                title = "Local Offlining",
                icon = Icons.Default.Info,
                desc = "Synchronized via a local Room DB, minimizing network footprint while keeping readings smooth.",
                accentColor = Color(0xFF69F0AE),
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
                .drawBehind {
                    drawRoundRect(
                        color = Color.Black,
                        topLeft = Offset(6f, 6f),
                        size = size,
                        cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                    )
                }
                .background(Color(0xFFD1F2FF), RoundedCornerShape(12.dp))
                .border(2.5.dp, Color.Black, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = "FUTURE ROADMAP & SCALABILITY ★",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
                Text(
                    text = "Integrating AI Text-To-Speech (TTS) for Dialogs",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Cursive,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "By referencing bounding boxes of speech bubbles and using Gemini's structured output, we blueprint the translation of dialog text into spatial 3D audio tracks. This will allow the characters to literally speak directly from their actual panel layout coordinate as they scroll past.",
                    color = Color(0xFF222222),
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
    Box(
        modifier = modifier
            .height(180.dp)
            .drawBehind {
                drawRoundRect(
                    color = Color.Black,
                    topLeft = Offset(6f, 6f),
                    size = size,
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                )
            }
            .background(Color.White, RoundedCornerShape(12.dp))
            .border(2.5.dp, Color.Black, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .background(accentColor, RoundedCornerShape(6.dp))
                    .border(1.5.dp, Color.Black, RoundedCornerShape(6.dp))
                    .padding(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Cursive,
                fontSize = 15.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = desc,
                color = Color(0xFF444444),
                fontSize = 11.sp,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
fun PrdSectionHeader(title: String, icon: ImageVector) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .background(Color(0xFFFFCC00), RoundedCornerShape(6.dp))
                .border(1.5.dp, Color.Black, RoundedCornerShape(6.dp))
                .padding(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            color = Color.Black,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Cursive,
            fontSize = 18.sp
        )
    }
}

@Composable
fun PrdBodyText(text: String) {
    Text(
        text = text,
        color = Color(0xFF222222),
        fontSize = 13.sp,
        lineHeight = 19.sp,
        textAlign = TextAlign.Justify,
        modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
    )
}
