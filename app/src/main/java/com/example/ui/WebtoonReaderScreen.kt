package com.example.ui

import android.util.Log
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.ProceduralAudioEngine
import com.example.data.ReadingManhwa
import com.example.data.ReadingPanel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

/**
 * High-fidelity Scroll-Sensitive Kinetic Webtoon Reader Screen.
 * Renders procedural, highly responsive comic panels dynamically via Canvas.
 * Hooks scroll offsets to drive low-level Audio Engine synth progressions,
 * screen shakes, live visual particle filters, and physical onomatopoeia audio/visual sparks.
 */
@OptIn(ExperimentalTextApi::class, ExperimentalMaterial3Api::class)
@Composable
fun WebtoonReaderScreen(
    readingManhwa: ReadingManhwa,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Audio Engine State Management
    var playAudio by remember { mutableStateOf(true) }
    var activePanelIndex by remember { mutableStateOf(0) }
    var activePanelMood by remember { mutableStateOf(readingManhwa.panels.firstOrNull()?.panel?.mood ?: "MYSTERY") }

    // Scroll metrics tracking to calculate speed/velocity
    var lastScrollOffset by remember { mutableStateOf(0) }
    var lastScrollTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var calculatedVelocity by remember { mutableStateOf(0f) }

    // Screenshake Animation Trigger States
    val shakeAnim = remember { Animatable(0f) }
    var lastTriggeredPanelIdx by remember { mutableStateOf(-1) }

    // Onomatopoeia display trigger
    var activeOnomatopoeiaText by remember { mutableStateOf("") }
    var activeOnomatopoeiaX by remember { mutableStateOf(0.5f) }
    var activeOnomatopoeiaY by remember { mutableStateOf(0.5f) }
    var showOnomatopoeia by remember { mutableStateOf(false) }

    // Manage procedural particle animations running globally
    val transition = rememberInfiniteTransition(label = "ReaderGlobe")
    val waveOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 2 * Math.PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "WaveOffset"
    )

    val scaleOffset by transition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    // Side effects synchronizations
    DisposableEffect(playAudio) {
        if (playAudio) {
            ProceduralAudioEngine.start()
        } else {
            ProceduralAudioEngine.stop()
        }
        onDispose {
            ProceduralAudioEngine.stop()
        }
    }

    // Capture vertical scroll velocities and focus panel index in real-time
    LaunchedEffect(lazyListState.firstVisibleItemScrollOffset) {
        val currOffset = lazyListState.firstVisibleItemScrollOffset + 
                         (lazyListState.firstVisibleItemIndex * 400) // crude virtual height offset
        val currTime = System.currentTimeMillis()
        val timeDelta = currTime - lastScrollTime
        if (timeDelta > 30) {
            val dist = Math.abs(currOffset - lastScrollOffset)
            val vel = (dist.toFloat() / timeDelta) * 10f // speed multiplier
            calculatedVelocity = vel.coerceIn(0f, 12f)
            ProceduralAudioEngine.scrollVelocity = calculatedVelocity
            
            lastScrollOffset = currOffset
            lastScrollTime = currTime
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = readingManhwa.manhwa.title,
                            color = Color.Black,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Cursive,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Interactive Comic Sensory Scroller • ${readingManhwa.manhwa.genre}",
                            color = Color.DarkGray,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("reader_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Library",
                            tint = Color.Black
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { playAudio = !playAudio }) {
                        Icon(
                            imageVector = if (playAudio) Icons.Default.Settings else Icons.Default.Refresh,
                            contentDescription = "Toggle SFX Soundtrack",
                            tint = if (playAudio) Color(0xFFFF3366) else Color.Gray,
                            modifier = Modifier
                                .border(1.5.dp, Color.Black, RoundedCornerShape(8.dp))
                                .background(if (playAudio) Color(0xFFFFEA79) else Color.White, RoundedCornerShape(8.dp))
                                .padding(4.dp)
                        )
                    }
                },
                modifier = Modifier.drawBehind {
                    drawLine(Color.Black, Offset(0f, size.height), Offset(size.width, size.height), strokeWidth = 3.dp.toPx())
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFFEA79),
                    titleContentColor = Color.Black
                )
            )
        },
        modifier = modifier.testTag("reader_screen_layout")
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFFFFDF0))
                .drawBehind {
                    val dotColor = Color(0x0C000000)
                    val spacing = 28.dp.toPx()
                    val radius = 1.5f.dp.toPx()
                    var x = 0f
                    while (x < size.width) {
                        var y = 0f
                        while (y < size.height) {
                            drawCircle(dotColor, radius, Offset(x, y))
                            y += spacing
                        }
                        x += spacing
                    }
                }
        ) {
            // LazyColumn to present beautiful panel cells
            LazyColumn(
                state = lazyListState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                // Intro banner styled as a retro poster
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .drawBehind {
                                drawCircle(Color(0xFFFFEA79).copy(alpha = 0.4f), radius = 180f, center = Offset(size.width * 0.85f, size.height * 0.5f))
                            },
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFFE57F), RoundedCornerShape(12.dp))
                                .border(2.dp, Color.Black, RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                "⚡ SCROLL FOR PROCEDURAL MULTIMEDIA ⚡",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = readingManhwa.manhwa.title,
                            color = Color.Black,
                            fontSize = 28.sp,
                            fontFamily = FontFamily.Cursive,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Universe Crafted by ${readingManhwa.manhwa.author}",
                            color = Color(0xFF333333),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = readingManhwa.manhwa.description,
                            color = Color.DarkGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(2.dp).background(Color.Black.copy(alpha = 0.15f)))
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }

                // Render dynamic processed panels
                itemsIndexed(readingManhwa.panels) { index, readingPanel ->
                    var isFocused by remember { mutableStateOf(false) }

                    // Custom Panel focus monitor hooked inside the lazy column layout bounds
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp)
                            .onGloballyPositioned { layoutCoordinates ->
                                val positionY = layoutCoordinates.positionInParent().y
                                val screenCenter = with(density) { 350.dp.toPx() } // dynamic window focus threshold
                                val layoutHeight = layoutCoordinates.size.height
                                
                                val isInFocus = (positionY < screenCenter && (positionY + layoutHeight) > screenCenter)
                                if (isInFocus && !isFocused) {
                                    isFocused = true
                                    activePanelIndex = index
                                    activePanelMood = readingPanel.panel.mood
                                    
                                    // Synchronize music moods dynamically
                                    ProceduralAudioEngine.activeMood = readingPanel.panel.mood

                                    // Prevent duplicate sparks inside same scroll state
                                    if (lastTriggeredPanelIdx != index) {
                                        lastTriggeredPanelIdx = index
                                        
                                        // 1. Audio Effect trigger
                                        val sfxItem = readingPanel.onomatopoeias.firstOrNull()
                                        if (sfxItem != null && playAudio) {
                                            ProceduralAudioEngine.triggerSfx(sfxItem.sfxType)
                                            
                                            // 2. Visual popup placement state update
                                            activeOnomatopoeiaText = sfxItem.text
                                            activeOnomatopoeiaX = sfxItem.xPercent
                                            activeOnomatopoeiaY = sfxItem.yPercent
                                            showOnomatopoeia = true
                                            
                                            coroutineScope.launch {
                                                delay(1200)
                                                // auto fade-out
                                                showOnomatopoeia = false
                                            }
                                        }

                                        // 3. Shake screens if tagged
                                        if (readingPanel.panel.animationType == "SHAKE") {
                                            coroutineScope.launch {
                                                for (i in 0..2) {
                                                    shakeAnim.animateTo(12f, spring(stiffness = Spring.StiffnessHigh))
                                                    shakeAnim.animateTo(-12f, spring(stiffness = Spring.StiffnessHigh))
                                                }
                                                shakeAnim.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                                            }
                                        }
                                    }
                                } else if (!isInFocus && isFocused) {
                                    isFocused = false
                                }
                            }
                    ) {
                        // Dynamic kinematic animations applied on the frame box itself
                        val animatedScale by animateFloatAsState(
                            targetValue = if (isFocused) {
                                if (readingPanel.panel.animationType == "PULSE" || readingPanel.panel.animationType == "ZOOM_IN") scaleOffset else 1.02f
                            } else 0.98f,
                            animationSpec = tween(500, easing = EaseOutCubic),
                            label = "PanelBoxScale"
                        )

                        val animatedBorderAlpha by animateFloatAsState(
                            targetValue = if (isFocused) 0.65f else 0.08f,
                            label = "PanelBorderAlpha"
                        )

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp)
                                .offset(
                                    x = if (isFocused && readingPanel.panel.animationType == "SHAKE") shakeAnim.value.dp else 0.dp,
                                    y = 0.dp
                                )
                                .graphicsLayer {
                                    scaleX = animatedScale
                                    scaleY = animatedScale
                                }
                                .border(
                                    width = 3.dp, // Thick solid comic outliner border list!
                                    color = Color.Black,
                                    shape = RoundedCornerShape(16.dp)
                                ),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clipToBounds()
                            ) {
                                // Real-Time Procedural Illustrator drawn directly on Canvas!
                                ProceduralPanelIllustration(
                                    panel = readingPanel,
                                    isFocused = isFocused,
                                    waveOffset = waveOffset,
                                    velocity = calculatedVelocity
                                )

                                // Overlay metadata markers (dialog text bubble footer)
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .fillMaxWidth()
                                        .background(Color(0xFFFFFDE7)) // Beautiful dairy cream dialogue label
                                        .border(width = 2.5.dp, color = Color.Black, shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                                        .padding(12.dp)
                                ) {
                                    Column {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .background(
                                                        color = if (isFocused) Color(0xFFFF4081) else Color.Gray,
                                                        shape = CircleShape
                                                    )
                                                    .border(1.dp, Color.Black, CircleShape)
                                            )
                                            Text(
                                                text = "PANEL ${index + 1} ★ ${readingPanel.panel.mood}",
                                                color = Color.Black,
                                                fontWeight = FontWeight.Black,
                                                fontSize = 11.sp,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = readingPanel.panel.description,
                                            color = Color.Black,
                                            fontSize = 12.sp,
                                            fontFamily = FontFamily.Cursive,
                                            fontWeight = FontWeight.Black,
                                            lineHeight = 16.sp
                                        )
                                    }
                                }

                                // Interactive Onomatopoeia Text Popups dynamically overlayed on custom percentages!
                                if (isFocused && showOnomatopoeia && activeOnomatopoeiaText.isNotEmpty()) {
                                    val popScale by animateFloatAsState(
                                        targetValue = 1.3f,
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioHighBouncy,
                                            stiffness = Spring.StiffnessHigh
                                        ),
                                        label = "PopScale"
                                    )

                                    val popDegrees by remember { mutableStateOf((-15..15).random().toFloat()) }

                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(24.dp)
                                    ) {
                                        Text(
                                            text = activeOnomatopoeiaText,
                                            color = when (readingPanel.panel.mood.uppercase()) {
                                                "ACTION" -> Color(0xFFFF2E93) // Barbie Pop Neon Pink
                                                "HORROR" -> Color(0xFF00FF7F) // Toxic Neon Green
                                                "ROMANCE" -> Color(0xFFFF9100) // Electric Tangerine
                                                else -> Color(0xFF00E5FF) // Cyber Cyan
                                            },
                                            fontSize = 34.sp,
                                            fontWeight = FontWeight.Black,
                                            fontFamily = FontFamily.Cursive,
                                            modifier = Modifier
                                                .align(
                                                    Alignment.TopStart
                                                )
                                                .offset(
                                                    x = (activeOnomatopoeiaX * 220).dp,
                                                    y = (activeOnomatopoeiaY * 180).dp
                                                )
                                                .graphicsLayer {
                                                    scaleX = popScale
                                                    scaleY = popScale
                                                    rotationZ = popDegrees
                                                },
                                            style = TextStyle(
                                                shadow = Shadow(
                                                    color = Color.Black,
                                                    offset = Offset(4f, 4f),
                                                    blurRadius = 0f // signature sharp comic block shadow!
                                                ),
                                                textAlign = TextAlign.Center
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Conclusion screen segment
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFFFEA79), CircleShape)
                                .border(2.5.dp, Color.Black, CircleShape)
                                .padding(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "To Be Continued...",
                            color = Color.Black,
                            fontSize = 22.sp,
                            fontFamily = FontFamily.Cursive,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "You've scrolled back to reality. The AI pipeline stands ready to enrich even more uploads.",
                            color = Color.DarkGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onBack,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(2.dp, Color.Black),
                            modifier = Modifier.testTag("reader_finish_button")
                        ) {
                            Text("Back to Dashboard", color = Color.Black, fontWeight = FontWeight.Bold, fontFamily = FontFamily.Cursive)
                        }
                    }
                }
            }

            // Real-Time Soundtrack Controller Dock (Floating HUD)
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
                    .widthIn(max = 400.dp)
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars) // avoid system gesture cutout!
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xEC09090D)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(Color(0xFF6A11CB), Color(0xFF2575FC))
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Procedural Audio Synthesizer",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Mood: $activePanelMood | Tempo: ${if (calculatedVelocity > 1f) "Acceleration" else "Steady"}",
                                color = Color.LightGray,
                                fontSize = 9.5.sp,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }

                    Switch(
                        checked = playAudio,
                        onCheckedChange = { playAudio = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF2575FC),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color(0xFF1C1B1F)
                        )
                    )
                }
            }
        }
    }
}

/**
 * Procedural illustration canvas coordinator.
 * Renders beautiful fully dynamic vector drawings depending on scene genre and features.
 */
@Composable
fun ProceduralPanelIllustration(
    panel: ReadingPanel,
    isFocused: Boolean,
    waveOffset: Float,
    velocity: Float
) {
    if (panel.panel.imagePath != null) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(290.dp),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = panel.panel.imagePath,
                contentDescription = panel.panel.description,
                modifier = Modifier.fillMaxSize().testTag("panel_image_${panel.panel.panelIndex}"),
                contentScale = ContentScale.Crop
            )
        }
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "IllustratorTransitions")
    val breatheValue by infiniteTransition.animateFloat(
        initialValue = -10f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BreatheAnim"
    )

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(290.dp)
    ) {
        val width = size.width
        val height = size.height

        val primaryColor = when (panel.panel.mood.uppercase()) {
            "ACTION" -> Color(0xFFFF5722)
            "HORROR" -> Color(0xFF2E7D32)
            "ROMANCE" -> Color(0xFFE91E63)
            else -> Color(0xFF1565C0) // MYSTERY
        }

        // Draw generic scenic ambient backdrop gradients
        val backgroundBrush = Brush.verticalGradient(
            colors = when (panel.panel.mood.uppercase()) {
                "ACTION" -> listOf(Color(0xFF1E1515), Color(0xFF0F0000))
                "HORROR" -> listOf(Color(0xFF0F1510), Color(0xFF030A04))
                "ROMANCE" -> listOf(Color(0xFF331F2E), Color(0xFF1A0F1A))
                else -> listOf(Color(0xFF141F33), Color(0xFF09111D))
            }
        )
        drawRect(brush = backgroundBrush, size = size)

        // Draw dynamic vector representations for each dramatic layout
        when (panel.panel.mood.uppercase()) {
            "ACTION" -> {
                // Drawing 1: Epic Action Battle Arena Scene
                // Draw ground fractures
                val groundPath = Path().apply {
                    moveTo(0f, height * 0.85f)
                    lineTo(width * 0.3f, height * 0.78f)
                    lineTo(width * 0.5f, height * 0.88f)
                    lineTo(width * 0.7f, height * 0.76f)
                    lineTo(width, height * 0.85f)
                }
                drawPath(groundPath, color = Color(0xFF3E2723), style = Stroke(width = 6f))

                // Draw central sword slash arc
                withTransform({
                    translate(left = breatheValue * 0.5f, top = 0f)
                }) {
                    drawArc(
                        color = Color(0xFFFFEB3B).copy(alpha = 0.85f),
                        startAngle = 180f,
                        sweepAngle = 110f,
                        useCenter = false,
                        topLeft = Offset(width * 0.15f, height * 0.25f),
                        size = Size(width * 0.7f, height * 0.45f),
                        style = Stroke(width = 18f, cap = StrokeCap.Round)
                    )
                    // Draw dual sword vector lines
                    drawLine(
                        color = Color.White,
                        start = Offset(width * 0.25f, height * 0.65f),
                        end = Offset(width * 0.75f, height * 0.18f),
                        strokeWidth = 10f,
                        cap = StrokeCap.Round
                    )
                }

                // Draw speed particles explosion center
                drawCircle(
                    color = Color(0xFFFF5722).copy(alpha = 0.35f),
                    radius = 55f + breatheValue,
                    center = Offset(width * 0.55f, height * 0.4f)
                )

                // Render Active SPEED_LINES visual overlay if active
                val overlayActive = panel.visualFxs.any { it.fxType == "SPEED_LINES" }
                if (isFocused || overlayActive) {
                    val lineCount = 18
                    for (i in 0 until lineCount) {
                        val angle = (i * 360f / lineCount) + (velocity * 2f)
                        val rad = Math.toRadians(angle.toDouble())
                        val startLen = 140f
                        val endLen = 280f + (velocity * 12f)
                        val startX = (width * 0.5f + startLen * cos(rad)).toFloat()
                        val startY = (height * 0.45f + startLen * sin(rad)).toFloat()
                        val endX = (width * 0.5f + endLen * cos(rad)).toFloat()
                        val endY = (height * 0.45f + endLen * sin(rad)).toFloat()
                        
                        drawLine(
                            color = Color.White.copy(alpha = 0.18f),
                            start = Offset(startX, startY),
                            end = Offset(endX, endY),
                            strokeWidth = 3f
                        )
                    }
                }
            }

            "HORROR" -> {
                // Drawing 2: Eerie Suspend Horror Corridor Scene
                // Draw converging linear ceiling walls (perspective)
                drawLine(Color.DarkGray, Offset(0f, 0f), Offset(width * 0.4f, height * 0.4f), 3f)
                drawLine(Color.DarkGray, Offset(width, 0f), Offset(width * 0.6f, height * 0.4f), 3f)
                drawLine(Color.DarkGray, Offset(0f, height), Offset(width * 0.4f, height * 0.6f), 3f)
                drawLine(Color.DarkGray, Offset(width, height), Offset(width * 0.6f, height * 0.6f), 3f)

                // Draw glowing eerie window mirror
                drawRect(
                    color = Color(0xFF1B5E20),
                    topLeft = Offset(width * 0.4f, height * 0.35f),
                    size = Size(width * 0.2f, height * 0.25f),
                    style = Stroke(width = 3f)
                )

                // Flickering dynamic light pulse
                val flickerAlpha = if (waveOffset.toInt() % 2 == 0) 0.6f else 0.15f
                drawCircle(
                    color = Color(0xFFC8E6C9).copy(alpha = flickerAlpha),
                    radius = 90f,
                    center = Offset(width * 0.5f, height * 0.15f)
                )

                // Creepy elongated structural arm shadow
                withTransform({
                    translate(breatheValue * 1.2f, breatheValue * 0.4f)
                }) {
                    val shadowPath = Path().apply {
                        moveTo(width * 0.2f, height * 0.45f)
                        quadraticTo(width * 0.35f, height * 0.52f, width * 0.48f, height * 0.48f)
                        lineTo(width * 0.45f, height * 0.55f)
                        quadraticTo(width * 0.3f, height * 0.58f, width * 0.18f, height * 0.55f)
                        close()
                    }
                    drawPath(shadowPath, color = Color.Black.copy(alpha = 0.75f))
                }

                // Render sweat drops animations
                val overlayActive = panel.visualFxs.any { it.fxType == "SWEAT_DROPS" }
                if (isFocused || overlayActive) {
                    for (i in 0..5) {
                        val dropX = width * (0.2f + (i * 0.15f))
                        val dropY = (height * 0.15f + ((waveOffset * 40f + (i * 50f)) % height))
                        
                        // Draw falling teardrop vectors
                        val sweatPath = Path().apply {
                            moveTo(dropX, dropY)
                            lineTo(dropX - 4f, dropY + 8f)
                            lineTo(dropX + 4f, dropY + 8f)
                            close()
                        }
                        drawPath(sweatPath, color = Color(0xFF29B6F6).copy(alpha = 0.6f))
                    }
                }
            }

            "ROMANCE" -> {
                // Drawing 3: Golden Courtyard Cherry Blossom Scene
                // Draw warm setting radial sun
                drawCircle(
                    color = Color(0xFFFFB74D),
                    radius = 120f,
                    center = Offset(width * 0.5f, height * 0.45f)
                )

                // Glowing warm center
                drawCircle(
                    color = Color.White.copy(alpha = 0.5f),
                    radius = 80f,
                    center = Offset(width * 0.5f, height * 0.45f)
                )

                // Intersecting silhouette faces close to each other
                withTransform({
                    val scaleOffsetLocal = 1.0f + (breatheValue / 200f)
                    scale(scaleX = scaleOffsetLocal, scaleY = scaleOffsetLocal, pivot = Offset(width * 0.5f, height * 0.45f))
                }) {
                    // Left profile face silhouette
                    val leftFace = Path().apply {
                        moveTo(width * 0.15f, height)
                        quadraticTo(width * 0.28f, height * 0.8f, width * 0.35f, height * 0.55f)
                        lineTo(width * 0.42f, height * 0.55f)
                        lineTo(width * 0.38f, height * 0.65f)
                        quadraticTo(width * 0.42f, height * 0.82f, width * 0.3f, height)
                        close()
                    }
                    drawPath(leftFace, color = Color(0x73880E4F))

                    // Right profile face silhouette
                    val rightFace = Path().apply {
                        moveTo(width * 0.85f, height)
                        quadraticTo(width * 0.72f, height * 0.8f, width * 0.65f, height * 0.55f)
                        lineTo(width * 0.58f, height * 0.55f)
                        lineTo(width * 0.62f, height * 0.65f)
                        quadraticTo(width * 0.58f, height * 0.82f, width * 0.7f, height)
                        close()
                    }
                    drawPath(rightFace, color = Color(0x73880E4F))
                }

                // Render falling warm cherry blossom bokeh hearts and vectors
                val overlayActive = panel.visualFxs.any { it.fxType == "BOKEH_HEARTS" }
                if (isFocused || overlayActive) {
                    for (i in 0..7) {
                        val theta = (i * 1.5 + waveOffset)
                        val petalX = (width * 0.1f + (i * width * 0.12f) + sin(theta) * 20f).toFloat()
                        // Falling multiplier
                        val petalY = ((height * 0.05f + (i * 35f) + waveOffset * 15f) % (height * 0.85f)).toFloat()

                        drawCircle(
                            color = Color(0xFFFF80AB).copy(alpha = 0.55f),
                            radius = 6f + (i % 3) * 3f,
                            center = Offset(petalX, petalY)
                        )
                    }
                }
            }

            else -> {
                // Drawing 4: Ancient Glowing Rune Mystery Crypt Scene
                // Draw majestic crypt columns
                drawRect(Color(0xFF263238), Offset(width * 0.1f, 0f), Size(width * 0.15f, height * 0.8f))
                drawRect(Color(0xFF263238), Offset(width * 0.75f, 0f), Size(width * 0.15f, height * 0.8f))

                // Centred runic concentric circular symbols (glowing glass)
                val rotateDegrees = waveOffset * 25f
                withTransform({
                    rotate(rotateDegrees, pivot = Offset(width * 0.5f, height * 0.42f))
                }) {
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = 0.4f),
                        radius = 75f,
                        center = Offset(width * 0.5f, height * 0.42f),
                        style = Stroke(width = 4f)
                    )
                    drawCircle(
                        color = Color(0xFF00E5FF).copy(alpha = 0.25f),
                        radius = 50f,
                        center = Offset(width * 0.5f, height * 0.42f),
                        style = Stroke(width = 2f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f)))
                    )
                }

                // Floating sacred crystal shard
                withTransform({
                    translate(0f, breatheValue * 0.6f)
                }) {
                    val crystalPath = Path().apply {
                        moveTo(width * 0.5f, height * 0.22f)
                        lineTo(width * 0.56f, height * 0.42f)
                        lineTo(width * 0.5f, height * 0.62f)
                        lineTo(width * 0.44f, height * 0.42f)
                        close()
                    }
                    drawPath(
                        path = crystalPath,
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF84FFFF), Color(0xFF00B0FF))
                        )
                    )
                }

                // Spontaneous dynamic tiny floating magic sparks
                val overlayActive = panel.visualFxs.any { it.fxType == "SPARKS" }
                if (isFocused || overlayActive) {
                    for (i in 0..12) {
                        val sparkX = (width * 0.3f + (sin((i + waveOffset).toDouble()) * 110f)).toFloat()
                        val sparkY = ((height * 0.7f - (i * 18f) - (waveOffset * 8f)) % height)
                        drawCircle(
                            color = Color(0xFF80DEEA).copy(alpha = 0.75f),
                            radius = 3f + (i % 2) * 2f,
                            center = Offset(sparkX, sparkY)
                        )
                    }
                }
            }
        }
    }
}
