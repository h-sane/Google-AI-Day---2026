package com.example.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (username: String, role: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedRole by remember { mutableStateOf("READER") } // "CREATOR" or "READER"
    var username by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Adaptive default names based on role selection
    LaunchedEffect(selectedRole) {
        if (username.isEmpty() || username == "Top Creator Jin" || username == "Avid Reader Aria") {
            username = if (selectedRole == "CREATOR") "Top Creator Jin" else "Avid Reader Aria"
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFFFFDF0)) // Cream custom comic backing page
            .drawBehind {
                val dotColor = Color(0x0C000000)
                val spacing = 24.dp.toPx()
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
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Large stylized star avatar/logo
        Box(
            modifier = Modifier
                .size(90.dp)
                .drawBehind {
                    drawRoundRect(
                        color = Color.Black,
                        topLeft = Offset(8f, 8f),
                        size = size,
                        cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
                    )
                }
                .background(Color(0xFFFFEA79), RoundedCornerShape(24.dp))
                .border(3.dp, Color.Black, RoundedCornerShape(24.dp))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = Color.Black,
                modifier = Modifier.size(48.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Main App Banner Title
        Text(
            text = "Manhwa AI ⚡",
            color = Color.Black,
            fontSize = 40.sp,
            fontFamily = FontFamily.Cursive,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Adaptive Sensory Universe Builder",
            color = Color.DarkGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.SansSerif,
            letterSpacing = 0.5.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Comic Speech Bubble greeting
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
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(3.dp, Color.Black, RoundedCornerShape(16.dp))
                .padding(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Welcome, adventurer! Are you here to upload and orchestrate legendary webtoons, or stream cinematic multi-sensory comics?",
                    color = Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.SansSerif,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "👇 CHOOSE YOUR ROLE BELOW 👇",
                    color = Color(0xFFFF3366),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Custom Comic Role Toggles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Reader Role Selected Box
            val readerSelected = selectedRole == "READER"
            val readerElevation by animateFloatAsState(targetValue = if (readerSelected) 4f else 8f)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .drawBehind {
                        drawRoundRect(
                            color = Color.Black,
                            topLeft = Offset(readerElevation, readerElevation),
                            size = size,
                            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                        )
                    }
                    .background(
                        if (readerSelected) Color(0xFFD1F2FF) else Color.White,
                        RoundedCornerShape(16.dp)
                    )
                    .border(3.dp, Color.Black, RoundedCornerShape(16.dp))
                    .clickable { selectedRole = "READER" }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .background(if (readerSelected) Color(0xFF40C4FF) else Color(0xFFF0F0F0), CircleShape)
                            .border(2.dp, Color.Black, CircleShape)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Face,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "READER 📖",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Cursive,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Read & Stream\nwith Live SFX",
                        color = Color.DarkGray,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 12.sp
                    )
                }
            }

            // Creator Role Selected Box
            val creatorSelected = selectedRole == "CREATOR"
            val creatorElevation by animateFloatAsState(targetValue = if (creatorSelected) 4f else 8f)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .drawBehind {
                        drawRoundRect(
                            color = Color.Black,
                            topLeft = Offset(creatorElevation, creatorElevation),
                            size = size,
                            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                        )
                    }
                    .background(
                        if (creatorSelected) Color(0xFFFFDFEA) else Color.White,
                        RoundedCornerShape(16.dp)
                    )
                    .border(3.dp, Color.Black, RoundedCornerShape(16.dp))
                    .clickable { selectedRole = "CREATOR" }
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .background(if (creatorSelected) Color(0xFFFF4081) else Color(0xFFF0F0F0), CircleShape)
                            .border(2.dp, Color.Black, CircleShape)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "CREATOR 🎨",
                        color = Color.Black,
                        fontSize = 15.sp,
                        fontFamily = FontFamily.Cursive,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = "Upload Stories &\nBuild soundscapes",
                        color = Color.DarkGray,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Username Input field in distinctive bubble borders
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawRoundRect(
                        color = Color.Black,
                        topLeft = Offset(6f, 6f),
                        size = size,
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                    )
                }
                .background(Color.White, RoundedCornerShape(16.dp))
                .border(2.5.dp, Color.Black, RoundedCornerShape(16.dp))
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) {
            TextField(
                value = username,
                onValueChange = { username = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("login_username_input"),
                placeholder = {
                    Text(
                        text = "Enter your handle...",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Black
                )
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Action launch button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .drawBehind {
                    drawRoundRect(
                        color = Color.Black,
                        topLeft = Offset(8f, 8f),
                        size = size,
                        cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx())
                    )
                }
                .background(Color(0xFFFF3366), RoundedCornerShape(16.dp))
                .border(3.dp, Color.Black, RoundedCornerShape(16.dp))
                .clickable {
                    val finalName = username.trim().ifEmpty {
                        if (selectedRole == "CREATOR") "Unamed Creator" else "Avid Reader"
                    }
                    onLoginSuccess(finalName, selectedRole)
                }
                .testTag("login_submit_button"),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "ENTER UNIVERSE ★ GO!",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Cursive,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(40.dp))
    }
}
