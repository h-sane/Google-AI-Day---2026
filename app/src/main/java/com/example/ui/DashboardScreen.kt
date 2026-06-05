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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
    onUploadManhwa: (String, String, String, Uri?, List<Uri>?) -> Unit,
    onDeleteManhwa: (Long) -> Unit,
    modifier: Modifier = Modifier,
    userSession: UserSession? = null,
    onLogout: () -> Unit = {}
) {
    var activeTab by remember { mutableStateOf("LIBRARY") } // LIBRARY, PRD
    var showUploadModal by remember { mutableStateOf(false) }
    var favoritedIds by remember { mutableStateOf(setOf<Long>()) }
    var showOnlyFavorites by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .testTag("dashboard_screen_root")
    ) {
        // App Main Interactive Bubbly Header Logo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    // Cute pastel background bubble accents
                    drawCircle(Color(0xFFFFEA79).copy(alpha = 0.3f), radius = 220f, center = Offset(50f, 50f))
                    drawCircle(Color(0xFFFF8DA1).copy(alpha = 0.2f), radius = 350f, center = Offset(size.width - 50f, size.height - 20f))
                }
                .padding(top = 20.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
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
                        tint = Color(0xFFFFCC00),
                        modifier = Modifier
                            .size(38.dp)
                            .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                            .background(Color.Black, RoundedCornerShape(12.dp))
                            .padding(4.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Manhwa AI ⚡",
                            color = Color.Black,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Cursive,
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Bouncy Soundscape Comic System",
                            color = Color.DarkGray,
                            fontSize = 9.sp,
                            fontFamily = FontFamily.SansSerif,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                    }
                }

                // Header Info Chip - Styled as Speech Bubble with Logout
                Column(horizontalAlignment = Alignment.End) {
                    Box(
                        modifier = Modifier
                            .background(
                                if (userSession?.role == "CREATOR") Color(0xFFFFB300) else Color(0xFF00E5FF),
                                RoundedCornerShape(12.dp)
                            )
                            .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                            .clickable { onLogout() }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                            .testTag("logout_button")
                    ) {
                        Text(
                            text = "${userSession?.username ?: "Guest"} ★ LOGOUT",
                            color = Color.Black,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Text(
                        text = "Role: ${userSession?.role ?: "READER"}",
                        color = Color.Black,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(top = 2.dp, end = 4.dp)
                    )
                }
            }
        }

        // Playful Tab Selectors: LIBRARY, PRD & ARCHITECTURE
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .drawBehind {
                    drawRoundRect(
                        color = Color.Black,
                        topLeft = Offset(6f, 6f),
                        size = size,
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                    )
                }
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(3.dp, Color.Black, RoundedCornerShape(16.dp))
                .padding(4.dp)
        ) {
            TabButton(
                title = "My Comic Library",
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

        Spacer(modifier = Modifier.height(10.dp))

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
                            EmptyLibraryView(onSeedDefault = { 
                                if (userSession?.role == "CREATOR") {
                                    showUploadModal = true 
                                } else {
                                    // Readers can't upload, let's just trigger a seed through a dummy upload or prompt
                                    onUploadManhwa("Default Action Saga", "Seeded adventure story with epic chords", "ACTION", null, null)
                                }
                            })
                        } else {
                            Column(modifier = Modifier.fillMaxSize()) {
                                // Warm Comic Greeting Bubble
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                        .drawBehind {
                                            drawRoundRect(
                                                color = Color.Black,
                                                topLeft = Offset(4f, 4f),
                                                size = size,
                                                cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                                            )
                                        }
                                        .background(Color(0xFFFFFDE7), RoundedCornerShape(12.dp))
                                        .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = if (userSession?.role == "CREATOR") {
                                            "🎨 CREATOR STUDIO: Welcome, Master ${userSession.username}! Draw and upload your manhwa saga with dynamic visual triggers below."
                                        } else {
                                            "📖 READER CORNER: Greeting, Enthusiast ${userSession?.username}! Enjoy immersive audio tracks synced in real-time as you scroll."
                                        },
                                        color = Color.Black,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace,
                                        lineHeight = 15.sp
                                    )
                                }

                                // Shelf Analytics Summary Bar
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    QuickStatChip(
                                        title = "Manhwa Universes",
                                        value = "${manhwas.size} Stories",
                                        modifier = Modifier.weight(1f)
                                    )
                                    QuickStatChip(
                                        title = "Live SFX Pipelines",
                                        value = manhwas.filter { it.isProcessed }.size.toString() + " ACTIVE",
                                        modifier = Modifier.weight(1f)
                                    )
                                    
                                    // Search/Favorites Filter chip for Readers
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .drawBehind {
                                                drawRoundRect(
                                                    color = Color.Black,
                                                    topLeft = Offset(3f, 3f),
                                                    size = size,
                                                    cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                                                )
                                            }
                                            .background(
                                                if (showOnlyFavorites) Color(0xFFFF8DA1) else Color.White,
                                                RoundedCornerShape(10.dp)
                                            )
                                            .border(1.5.dp, Color.Black, RoundedCornerShape(10.dp))
                                            .clickable { showOnlyFavorites = !showOnlyFavorites }
                                            .padding(horizontal = 8.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (showOnlyFavorites) "★ FAVS ONLY" else "★ SHOW ALL",
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Grid view of manhwas
                                val displayedManhwas = if (showOnlyFavorites) {
                                    manhwas.filter { favoritedIds.contains(it.id) }
                                } else {
                                    manhwas
                                }

                                if (displayedManhwas.isEmpty() && showOnlyFavorites) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "No favorited manhwas yet! Go tap some heart buttons on the comics below! ❤",
                                            color = Color.DarkGray,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                } else {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(1),
                                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 90.dp),
                                        verticalArrangement = Arrangement.spacedBy(16.dp),
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        items(displayedManhwas) { item ->
                                            ManhwaCard(
                                                manhwa = item,
                                                onRead = { onReadManhwa(item.id) },
                                                onDelete = { onDeleteManhwa(item.id) },
                                                isCreator = userSession?.role == "CREATOR",
                                                isFavorited = favoritedIds.contains(item.id),
                                                onToggleFavorite = {
                                                    favoritedIds = if (favoritedIds.contains(item.id)) {
                                                        favoritedIds - item.id
                                                    } else {
                                                        favoritedIds + item.id
                                                    }
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Floating action button (FAB) to upload/design comic universes (Only visible to Creators!)
                        if (userSession?.role == "CREATOR") {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(16.dp)
                                    .drawBehind {
                                        drawRoundRect(
                                            color = Color.Black,
                                            topLeft = Offset(8f, 8f),
                                            size = size,
                                            cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                                        )
                                    }
                                    .background(Color(0xFFFF3366), RoundedCornerShape(24.dp))
                                    .border(3.dp, Color.Black, RoundedCornerShape(24.dp))
                                    .clickable { showUploadModal = true }
                                    .padding(horizontal = 20.dp, vertical = 12.dp)
                                    .testTag("upload_fab")
                            ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Draw Comic Saga!",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Black,
                                    fontFamily = FontFamily.Cursive
                                )
                            }
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
            var selectedZipUri by remember { mutableStateOf<Uri?>(null) }
            var selectedImageUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
            val context = LocalContext.current

            val zipPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                if (uri != null) {
                    selectedZipUri = uri
                    selectedImageUris = emptyList()
                }
            }

            val imagePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetMultipleContents()
            ) { uris: List<Uri> ->
                if (uris.isNotEmpty()) {
                    selectedImageUris = uris
                    selectedZipUri = null
                }
            }

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
                            text = "Enter raw comic outlines or descriptions. The Gemini AI Pipeline will automatically generate custom scene orchestrations, page-mapped onomatopoeias, dynamic overlays, and procedural soundtracks.",
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

                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.15f),
                            thickness = 1.dp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Text("Optional: Upload Actual Manhwa Pages", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Include a sequential ZIP archive or multi-select individual page images to render real manhwa screens.", color = Color.Gray, fontSize = 10.sp, lineHeight = 13.sp)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { zipPickerLauncher.launch("application/zip") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedZipUri != null) Color(0xFF2E7D32) else Color(0xFF2575FC).copy(alpha = 0.15f)
                                ),
                                border = BorderStroke(1.dp, if (selectedZipUri != null) Color(0xFF81C784) else Color(0xFF2575FC)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("zip_upload_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (selectedZipUri != null) Icons.Default.Star else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (selectedZipUri != null) "ZIP Attached!" else "Upload ZIP",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Button(
                                onClick = { imagePickerLauncher.launch("image/*") },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selectedImageUris.isNotEmpty()) Color(0xFF2E7D32) else Color(0xFF2575FC).copy(alpha = 0.15f)
                                ),
                                border = BorderStroke(1.dp, if (selectedImageUris.isNotEmpty()) Color(0xFF81C784) else Color(0xFF2575FC)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f).testTag("images_upload_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (selectedImageUris.isNotEmpty()) Icons.Default.Star else Icons.Default.Add,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = if (selectedImageUris.isNotEmpty()) "${selectedImageUris.size} Pages!" else "Select Pages",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (selectedZipUri != null) {
                            Text(
                                text = "✓ Selected ZIP Archive: ${selectedZipUri?.lastPathSegment ?: "manhwa.zip"}",
                                color = Color(0xFF81C784),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        } else if (selectedImageUris.isNotEmpty()) {
                            Text(
                                text = "✓ Selected ${selectedImageUris.size} sorted sequential comic files.",
                                color = Color(0xFF81C784),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        } else {
                            Text(
                                text = "ℹ Using fully procedural dynamic vector graphics layout system.",
                                color = Color.White.copy(alpha = 0.5f),
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace
                            )
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
                                selectedGenre,
                                selectedZipUri,
                                selectedImageUris.ifEmpty { null }
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
            .clip(RoundedCornerShape(12.dp))
            .background(if (isActive) Color(0xFFFFE57F) else Color.Transparent) // Bright bubbly yellow highlight!
            .border(
                width = if (isActive) 2.dp else 0.dp,
                color = if (isActive) Color.Black else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
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
                tint = Color.Black,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = title,
                color = Color.Black,
                fontSize = 13.sp,
                fontFamily = FontFamily.Cursive,
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
    val randomBg = if (title.contains("Universe", ignoreCase = true)) Color(0xFFD1F2FF) else Color(0xFFFFDFEA)
    Box(
        modifier = modifier
            .drawBehind {
                drawRoundRect(
                    color = Color.Black,
                    topLeft = Offset(6f, 6f),
                    size = size,
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                )
            }
            .background(randomBg, RoundedCornerShape(12.dp))
            .border(2.5.dp, Color.Black, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Column {
            Text(
                text = title.uppercase(),
                fontSize = 8.5.sp,
                color = Color.Black.copy(alpha = 0.8f),
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                color = Color.Black,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Cursive
            )
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
        Box(
            modifier = Modifier
                .background(Color(0xFFFFEA79), CircleShape)
                .border(2.5.dp, Color.Black, CircleShape)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(54.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Your Bouncy Comic Shelf is Empty!",
            color = Color.Black,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            fontFamily = FontFamily.Cursive
        )
        Text(
            text = "Preloading takes a moment, or click below to launch your distinct fantasy outlining canvas! Spark dialogs, synthesize tunes!",
            color = Color.DarkGray,
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 24.dp)
        )
        Button(
            onClick = onSeedDefault,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF3366)),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(2.5.dp, Color.Black),
            modifier = Modifier
                .drawBehind {
                    drawRoundRect(
                        color = Color.Black,
                        topLeft = Offset(4f, 4f),
                        size = size,
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                    )
                }
        ) {
            Text("Create First Epic Comic! ✎", color = Color.White, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Cursive, fontSize = 16.sp)
        }
    }
}

@Composable
fun ManhwaCard(
    manhwa: ManhwaEntity,
    onRead: () -> Unit,
    onDelete: () -> Unit,
    isCreator: Boolean = true,
    isFavorited: Boolean = false,
    onToggleFavorite: () -> Unit = {}
) {
    val accentColor = when (manhwa.genre.uppercase()) {
        "ACTION" -> Color(0xFFFF5252) // Neon Red
        "HORROR" -> Color(0xFF69F0AE) // Neon Green
        "ROMANCE" -> Color(0xFFFF4081) // Neon Pink
        else -> Color(0xFF40C4FF) // Neon Cyan
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = Color.Black,
                    topLeft = Offset(10f, 10f),
                    size = size,
                    cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                )
            }
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(3.dp, Color.Black, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // High fidelity cover thumbnail with thick black outline
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(accentColor, RoundedCornerShape(12.dp))
                    .border(2.5.dp, Color.Black, RoundedCornerShape(12.dp)),
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
                    tint = Color.Black,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .background(accentColor, RoundedCornerShape(6.dp))
                            .border(1.5.dp, Color.Black, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = manhwa.genre.uppercase(),
                            color = Color.Black,
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    if (manhwa.isProcessed) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFCCFF90), RoundedCornerShape(6.dp))
                                .border(1.5.dp, Color.Black, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color.Black,
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = "AI ACTIVE",
                                    color = Color.Black,
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = manhwa.title,
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Cursive,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Seed: #${manhwa.imageSeed.toString().takeLast(5)} • By ${manhwa.author}",
                    color = Color.DarkGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onRead,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)), // Yellow action button
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(2.dp, Color.Black),
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .testTag("read_button_${manhwa.title}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Text("STREAM COMIC ⚡", color = Color.Black, fontSize = 12.sp, fontWeight = FontWeight.Black, fontFamily = FontFamily.Cursive)
                        }
                    }

                    if (isCreator) {
                        // Delete button to purge database records
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier
                                .size(38.dp)
                                .background(Color(0xFFFF8A80), RoundedCornerShape(12.dp))
                                .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete universe",
                                tint = Color.Black,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else {
                        // Favorite button for normal readers
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier
                                .size(38.dp)
                                .background(
                                    if (isFavorited) Color(0xFFFF8DA1) else Color.White,
                                    RoundedCornerShape(12.dp)
                                )
                                .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Favorite,
                                contentDescription = "Toggle favorite",
                                tint = if (isFavorited) Color.Red else Color.LightGray,
                                modifier = Modifier.size(18.dp)
                            )
                        }
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
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) Color(0xFFFF8DA1) else Color.White)
            .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            color = Color.Black,
            fontSize = 12.sp,
            fontFamily = FontFamily.Cursive,
            fontWeight = FontWeight.Black
        )
    }
}
