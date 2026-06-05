package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ManhwaEntity
import kotlinx.coroutines.launch

@Composable
fun DashboardScreen(
    manhwas: List<ManhwaEntity>,
    isPipelineAnalyzing: Boolean,
    pipelineProgress: Float,
    pipelineProgressText: String,
    onReadManhwa: (Long) -> Unit,
    onUploadManhwa: (String, String, String) -> Unit,
    onDeleteManhwa: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf("LIBRARY") } // LIBRARY, PRD
    var showUploadModal by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF09090D))
            .testTag("dashboard_screen_root")
    ) {
        // App Main Interactive Header Logo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // Draw clean top diagonal ambient gradient rays
                    drawCircle(Color(0xFF6A11CB).copy(alpha = 0.08f), radius = 320f, center = Offset(0f, 0f))
                    drawCircle(Color(0xFF2575FC).copy(alpha = 0.08f), radius = 400f, center = Offset(size.width, 100f))
                }
                .padding(top = 24.dp, bottom = 12.dp, start = 16.dp, end = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "ManhwaAI Logo",
                        tint = Color(0xFF2575FC),
                        modifier = Modifier
                            .size(34.dp)
                            .padding(end = 6.dp)
                    )
                    Column {
                        Text(
                            text = "MANHWAAI",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = "MULTIMEDIA CINEMATIC SCROLLER",
                            color = Color.LightGray.copy(alpha = 0.6f),
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }

                // Header Info Chip
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0x332575FC)),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "v1.0 ACTIVE",
                        color = Color(0xFF2575FC),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Modern Tab Selectors: LIBRARY, PRD & ARCHITECTURE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .background(Color(0xFF13131A), RoundedCornerShape(10.dp))
                .padding(4.dp)
        ) {
            TabButton(
                title = "Library Shelf",
                icon = Icons.Default.Star,
                isActive = activeTab == "LIBRARY",
                modifier = Modifier.weight(1f),
                onClick = { activeTab = "LIBRARY" }
            )
            TabButton(
                title = "PRD & Research Spec",
                icon = Icons.Default.Settings,
                isActive = activeTab == "PRD",
                modifier = Modifier.weight(1f),
                onClick = { activeTab = "PRD" }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Content Display with Transition
        Crossfade(
            targetState = activeTab,
            animationSpec = tween(300),
            label = "TabCrossfade",
            modifier = Modifier.weight(1f)
        ) { tab ->
            when (tab) {
                "PRD" -> PrdViewerScreen(modifier = Modifier.fillMaxSize())
                else -> {
                    // Library Shelf Dashboard
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (manhwas.isEmpty() && !isPipelineAnalyzing) {
                            EmptyLibraryView(onSeedDefault = { showUploadModal = true })
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Shelf Analytics Summary Bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    QuickStatChip(
                                        title = "Manhwa Universes",
                                        value = manhwas.size.toString(),
                                        modifier = Modifier.weight(1.3f)
                                    )
                                    QuickStatChip(
                                        title = "Live SFX Pipelines",
                                        value = manhwas.filter { it.isProcessed }.size.toString() + " ACTIVE",
                                        modifier = Modifier.weight(1.7f)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Grid view of manhwas
                                LazyVerticalGrid(
                                    columns = GridCells.Fixed(1),
                                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    items(manhwas) { item ->
                                        ManhwaCard(
                                            manhwa = item,
                                            onRead = { onReadManhwa(item.id) },
                                            onDelete = { onDeleteManhwa(item.id) }
                                        )
                                    }
                                }
                            }
                        }

                        // Floating action button (FAB) to upload/design comic universes
                        FloatingActionButton(
                            onClick = { showUploadModal = true },
                            containerColor = Color(0xFF2575FC),
                            contentColor = Color.White,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .testTag("upload_fab")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Upload Manhwa", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Real-Time Pipeline Progress Overlay Panel (Simulates or Runs actual Gemini analysis)
        AnimatedVisibility(
            visible = isPipelineAnalyzing,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF13131A)),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color(0xFF2575FC).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth().testTag("pipeline_overlay_container")
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = Color(0xFF2575FC),
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "AI Manhwa Enrichment Pipeline",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Decomposing panel drawings with Gemini-3.5-Flash",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Progress Indicators
                        LinearProgressIndicator(
                            progress = pipelineProgress,
                            color = Color(0xFF2575FC),
                            trackColor = Color.White.copy(alpha = 0.1f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "${(pipelineProgress * 100).toInt()}% • $pipelineProgressText",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontFamily = FontFamily.Monospace
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Interactive steps tracker log
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black, RoundedCornerShape(8.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "LOGS:\n" +
                                        "» POST https://generativelanguage.googleapis.com\n" +
                                        "» Payload contains layout constraints\n" +
                                        "» Structuring onomatopoeia markers [BOOM, CLANG...]\n" +
                                        "» Syncing PCM synthesizer oscillators...",
                                color = Color.Green,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 14.sp
                            )
                        }
                    }
                }
            }
        }

        // Custom High-Fidelity Manhwa Upload Dialog Sheet
        if (showUploadModal) {
            var inputTitle by remember { mutableStateOf("") }
            var inputNarrativePrompt by remember { mutableStateOf("") }
            var selectedGenre by remember { mutableStateOf("ACTION") } // ACTION, HORROR, ROMANCE, MYSTERY

            AlertDialog(
                onDismissRequest = { showUploadModal = false },
                containerColor = Color(0xFF13131A),
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFF2575FC))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create AI Comic Universe", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Enter raw comic outlines or descriptions. The Gemini AI Pipeline will automatically generate custom 6-panel scene orchestrations, onomatopoeias, dynamic overlays, and procedural PCM soundtracks.",
                            color = Color.LightGray,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )

                        OutlinedTextField(
                            value = inputTitle,
                            onValueChange = { inputTitle = it },
                            label = { Text("Manhwa Title") },
                            placeholder = { Text("e.g. Solo Levitation") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2575FC),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedLabelColor = Color(0xFF2575FC),
                                unfocusedLabelColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("upload_title_input")
                        )

                        OutlinedTextField(
                            value = inputNarrativePrompt,
                            onValueChange = { inputNarrativePrompt = it },
                            label = { Text("Storyline Description or Prompt") },
                            placeholder = { Text("Describe the fight or romance detail happening...") },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2575FC),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.15f),
                                focusedLabelColor = Color(0xFF2575FC),
                                unfocusedLabelColor = Color.Gray,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            maxLines = 4,
                            modifier = Modifier.fillMaxWidth().height(90.dp).testTag("upload_desc_input")
                        )

                        // Genre Tag Chips Selector
                        Text("Universe Core Atmospheric Signature", color = Color.Gray, fontSize = 11.sp)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GenreChip(name = "Action", isSelected = selectedGenre == "ACTION", onClick = { selectedGenre = "ACTION" })
                            GenreChip(name = "Horror", isSelected = selectedGenre == "HORROR", onClick = { selectedGenre = "HORROR" })
                            GenreChip(name = "Romance", isSelected = selectedGenre == "ROMANCE", onClick = { selectedGenre = "ROMANCE" })
                            GenreChip(name = "Mystery", isSelected = selectedGenre == "MYSTERY", onClick = { selectedGenre = "MYSTERY" })
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showUploadModal = false
                            onUploadManhwa(
                                inputTitle.ifEmpty { "AI Generated Saga" },
                                inputNarrativePrompt.ifEmpty { "An epic clash of elemental stars." },
                                selectedGenre
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2575FC)),
                        modifier = Modifier.testTag("submit_upload_button")
                    ) {
                        Text("Launch AI Pipeline", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUploadModal = false }) {
                        Text("Discard", color = Color.Gray)
                    }
                }
            )
        }
    }
}

@Composable
fun TabButton(
    title: String,
    icon: Any, // Decouple to allow icons inside
    isActive: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isActive) Color(0xFF2575FC) else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon as ImageVector,
                contentDescription = null,
                tint = if (isActive) Color.White else Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                color = if (isActive) Color.White else Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun QuickStatChip(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13131A)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
            Text(text = title.uppercase(), fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Monospace)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, fontSize = 14.sp, color = Color.White, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
fun EmptyLibraryView(onSeedDefault: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.4f),
            modifier = Modifier.size(72.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your AI Shelf is Empty",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Preloading triggers takes a moment, or click below to launch your distinct fantasy outlining canvas.",
            color = Color.Gray,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 24.dp)
        )
        Button(
            onClick = onSeedDefault,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2575FC)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("Create First Manhwa Story", color = Color.White)
        }
    }
}

@Composable
fun ManhwaCard(
    manhwa: ManhwaEntity,
    onRead: () -> Unit,
    onDelete: () -> Unit
) {
    val accentColor = when (manhwa.genre.uppercase()) {
        "ACTION" -> Color(0xFFFF5722)
        "HORROR" -> Color(0xFF4CAF50)
        "ROMANCE" -> Color(0xFFE91E63)
        else -> Color(0xFF2196F3)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF13131A)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // High fidelity procedurally generated cover thumbnail
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(accentColor.copy(alpha = 0.8f), accentColor.copy(alpha = 0.2f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (manhwa.genre.uppercase()) {
                        "ACTION" -> Icons.Default.Star
                        "HORROR" -> Icons.Default.Warning
                        "ROMANCE" -> Icons.Default.Favorite
                        else -> Icons.Default.Info
                    },
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = accentColor.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = manhwa.genre.uppercase(),
                            color = accentColor,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (manhwa.isProcessed) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0x332E7D32)),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                    .fillMaxHeight(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFF4CAF50),
                                    modifier = Modifier.size(8.dp)
                                )
                                Text(
                                    text = "AI SYNCED",
                                    color = Color(0xFF4CAF50),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = manhwa.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Universe Seed: #${manhwa.imageSeed.toString().takeLast(5)} • ${manhwa.author}",
                    color = Color.Gray,
                    fontSize = 10.5.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onRead,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2575FC)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(34.dp)
                            .testTag("read_button_${manhwa.title}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(12.dp))
                            Text("STREAM LIVE", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }

                    // Delete button to purge database records
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(34.dp)
                            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete universe",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GenreChip(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) Color(0xFF2575FC) else Color.White.copy(alpha = 0.05f))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            color = if (isSelected) Color.White else Color.Gray,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
