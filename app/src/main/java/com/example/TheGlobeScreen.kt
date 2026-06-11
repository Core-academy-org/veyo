package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.draw.clip
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TheGlobeScreen(
    viewModel: VeyoViewModel,
    state: VeyoUiState,
    onNavigateToChat: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var radarAngle by remember { mutableFloatStateOf(0f) }
    var mapMode by remember { mutableStateOf("leaflet") } // "leaflet", "radar"

    // Simulating continuous rotating radar beam
    LaunchedEffect(Unit) {
        while (true) {
            delay(30)
            radarAngle = (radarAngle + 2) % 360f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        if (mapMode == "leaflet") {
            val eventUsers = remember(state.chatThreads) {
                state.chatThreads.filter { it.linkedEventName != null && it.linkedLat != null && it.linkedLng != null }.map { thread ->
                    MapUser(
                        id = "linked_event_${thread.id}",
                        name = "${thread.linkedEventName}",
                        type = "Voqea",
                        originalLat = thread.linkedLat ?: 41.3110,
                        originalLng = thread.linkedLng ?: 69.2405,
                        currentLat = thread.linkedLat ?: 41.3110,
                        currentLng = thread.linkedLng ?: 69.2405,
                        locationName = thread.linkedLocationName ?: "Tadbir uchrashuvi",
                        durationText = "Guruh tadbiri",
                        batteryLevel = 100,
                        isOnline = true,
                        avatarEmoji = "📍"
                    )
                }
            }
            val combinedUsers = state.users + eventUsers

            LeafletMap(
                users = combinedUsers,
                safetyMode = state.safetyMode,
                selectedUser = state.selectedUserForDetails,
                onUserClick = { userId ->
                    if (userId.startsWith("linked_event_")) {
                        val threadId = userId.substringAfter("linked_event_")
                        onNavigateToChat(threadId)
                    } else {
                        val matchedUser = state.users.find { it.id == userId }
                        if (matchedUser != null) {
                            viewModel.selectUserForDetails(matchedUser)
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // --- Custom Canvas Interactive Map ---
            Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.5f, 4.0f)
                        panOffset += pan
                    }
                }
                .testTag("interactive_map_canvas")
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseScale = 400f * zoomScale

            // Draw radial tech coordinates
            drawCircle(
                color = ElectricBlue.copy(alpha = 0.04f),
                radius = baseScale,
                center = Offset(center.x + panOffset.x, center.y + panOffset.y),
                style = Stroke(width = 1f)
            )
            drawCircle(
                color = ElectricBlue.copy(alpha = 0.02f),
                radius = baseScale * 1.5f,
                center = Offset(center.x + panOffset.x, center.y + panOffset.y),
                style = Stroke(width = 1f)
            )
            drawCircle(
                color = ElectricBlue.copy(alpha = 0.01f),
                radius = baseScale * 2f,
                center = Offset(center.x + panOffset.x, center.y + panOffset.y),
                style = Stroke(width = 1f)
            )

            // Draw technical crosshairs
            drawLine(
                color = ElectricBlue.copy(alpha = 0.05f),
                start = Offset(0f, center.y + panOffset.y),
                end = Offset(size.width, center.y + panOffset.y),
                strokeWidth = 1f
            )
            drawLine(
                color = ElectricBlue.copy(alpha = 0.05f),
                start = Offset(center.x + panOffset.x, 0f),
                end = Offset(center.x + panOffset.x, size.height),
                strokeWidth = 1f
            )

            // Draw glowing digital grid lines
            val gridSize = 100f * zoomScale
            val startX = (panOffset.x % gridSize) - gridSize
            val startY = (panOffset.y % gridSize) - gridSize

            var x = startX
            while (x < size.width + gridSize) {
                drawLine(
                    color = ElectricBlue.copy(alpha = 0.03f),
                    start = Offset(x, 0f),
                    end = Offset(x, size.height),
                    strokeWidth = 1.2f
                )
                x += gridSize
            }

            var y = startY
            while (y < size.height + gridSize) {
                drawLine(
                    color = ElectricBlue.copy(alpha = 0.03f),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.2f
                )
                y += gridSize
            }

            // Draw Simulated City Roads & Landmarks (Highly stylized vector art)
            val roads = listOf(
                // Ring road
                Offset(-1.5f, -1.5f) to Offset(1.5f, 1.5f),
                Offset(-1.5f, 1.5f) to Offset(1.5f, -1.5f),
                // Grid blocks
                Offset(-2f, -0.6f) to Offset(2f, -0.6f),
                Offset(-2f, 0.6f) to Offset(2f, 0.6f),
                Offset(-0.8f, -2f) to Offset(-0.8f, 2f),
                Offset(0.8f, -2f) to Offset(0.8f, 2f)
            )

            roads.forEach { (startFactor, endFactor) ->
                drawLine(
                    color = ElectricBlue.copy(alpha = 0.07f),
                    start = Offset(
                        center.x + panOffset.x + startFactor.x * baseScale,
                        center.y + panOffset.y + startFactor.y * baseScale
                    ),
                    end = Offset(
                        center.x + panOffset.x + endFactor.x * baseScale,
                        center.y + panOffset.y + endFactor.y * baseScale
                    ),
                    strokeWidth = 2f
                )
            }

            // Draw the global scanning radar arc centered on SIZ
            val myOffset = state.users.find { it.id == "me" }?.let {
                val dx = ((it.currentLng - 69.240562) * 5000f * zoomScale).toFloat()
                val dy = (-(it.currentLat - 41.311081) * 5000f * zoomScale).toFloat()
                Offset(center.x + panOffset.x + dx, center.y + panOffset.y + dy)
            } ?: Offset(center.x + panOffset.x, center.y + panOffset.y)

            val radarLen = baseScale * 0.9f
            val radAngle = Math.toRadians(radarAngle.toDouble())
            val radarEnd = Offset(
                myOffset.x + (radarLen * cos(radAngle)).toFloat(),
                myOffset.y + (radarLen * sin(radAngle)).toFloat()
            )

            // Draw radar sweeper beam line
            drawLine(
                color = NeonGreen.copy(alpha = 0.2f),
                start = myOffset,
                end = radarEnd,
                strokeWidth = 3f
            )

            // Draw radar glowing aura
            drawCircle(
                color = NeonGreen.copy(alpha = 0.015f),
                radius = radarLen,
                center = myOffset
            )
        }

        // --- Render actual marker overlay pins using layout box to make them fully clickable ---
        state.users.forEach { user ->
            // Let's project Lat/Lng relative to center point of Tashkent (41.311081, 69.240562)
            val centerWidth = 1000f // Scaling factor
            val dx = ((user.currentLng - 69.240562) * 20000f * zoomScale).toFloat()
            val dy = (-(user.currentLat - 41.311081) * 20000f * zoomScale).toFloat() // inverted lat coordinate for screen Y

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(
                        x = (panOffset.x + dx).dp / 2f,
                        y = (panOffset.y + dy).dp / 2f
                    )
                    .size(56.dp)
                    .clickable {
                        viewModel.selectUserForDetails(user)
                    }
                    .testTag("map_marker_${user.id}"),
                contentAlignment = Alignment.Center
            ) {
                // Pulsating beacon aura
                val markerColor = when (user.type) {
                    "Siz" -> ElectricBlue
                    "Farzand" -> if (state.safetyMode == SafetyMode.POWER_OFF) HotRed else NeonGreen
                    "Boshliq" -> Color.White
                    "Xodim" -> if (user.batteryLevel < 15) ElectricYellow else ElectricBlue
                    else -> NeonGreen
                }

                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(markerColor.copy(alpha = 0.2f), CircleShape)
                        .border(1.dp, markerColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.avatarEmoji,
                        fontSize = 18.sp
                    )
                }

                // Tiny Battery Indicator Dot
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(16.dp)
                        .background(DarkSurface, CircleShape)
                        .border(1.dp, if (user.batteryLevel > 20) NeonGreen else HotRed, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${user.batteryLevel}",
                        fontSize = 8.sp,
                        color = SlateTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Tiny Floating Name
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 10.dp)
                        .background(DarkSurface.copy(alpha = 0.85f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 1.dp)
                ) {
                    Text(
                        text = user.name.split(" ")[0],
                        fontSize = 9.sp,
                        color = SlateTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        }

        // --- App Header / Search overlay ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface.copy(alpha = 0.9f), RoundedCornerShape(24.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search icon",
                    tint = SlateTextSecondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = {
                        searchQuery = it
                        viewModel.handleSearch(it)
                    },
                    placeholder = { Text("Profile qidirish...", color = SlateTextSecondary, fontSize = 14.sp) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = SlateTextPrimary,
                        unfocusedTextColor = SlateTextPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_profiles_input")
                )
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        viewModel.handleSearch("")
                    }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear", tint = SlateTextSecondary)
                    }
                }

                // Map Mode Switcher
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                        .padding(2.dp)
                ) {
                    listOf("leaflet" to "🗺️", "radar" to "📡").forEach { (mode, emoji) ->
                        val isSelected = mapMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) ElectricBlue else Color.Transparent)
                                .clickable { mapMode = mode }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                emoji,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))

                // Tracker Count Status Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkBg),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(NeonGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Kuzatuvchilar: ${state.trackingCount}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                    }
                }
            }

            // Search results drop-down overlay
            if (state.searchResults.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated.copy(alpha = 0.95f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        state.searchResults.forEach { person ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.sendTrackingRequest(person)
                                        searchQuery = ""
                                    }
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, tint = ElectricBlue)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(person, color = SlateTextPrimary, fontSize = 14.sp)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("Request qo'shish", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        var showAddUserDialog by remember { mutableStateOf(false) }

        // --- Side Map Utilities (Zoom / Location lock buttons) ---
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
                .offset(y = (-40).dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            FloatingActionButton(
                onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(4f) },
                containerColor = DarkSurfaceElevated,
                contentColor = SlateTextPrimary,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(20.dp))
            }

            FloatingActionButton(
                onClick = { zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.5f) },
                containerColor = DarkSurfaceElevated,
                contentColor = SlateTextPrimary,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(20.dp))
            }

            FloatingActionButton(
                onClick = {
                    panOffset = Offset.Zero
                    zoomScale = 1.0f
                },
                containerColor = ElectricBlue,
                contentColor = Color.White,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Center on me", modifier = Modifier.size(20.dp))
            }

            FloatingActionButton(
                onClick = { showAddUserDialog = true },
                containerColor = NeonGreen,
                contentColor = Color.White,
                modifier = Modifier.size(44.dp)
                    .testTag("add_tracked_member_button")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = "A'zo qo'shish", modifier = Modifier.size(20.dp))
            }
        }

        if (showAddUserDialog) {
            var newUserName by remember { mutableStateOf("") }
            var newUserType by remember { mutableStateOf("Farzand") }
            var newUserEmoji by remember { mutableStateOf("👦") }

            AlertDialog(
                onDismissRequest = { showAddUserDialog = false },
                title = { Text("Kuzatuv ostidagi yangi a'zo qo'shish", fontWeight = FontWeight.Bold, color = SlateTextPrimary) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = newUserName,
                            onValueChange = { newUserName = it },
                            label = { Text("Ismi", color = SlateTextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SlateTextPrimary,
                                unfocusedTextColor = SlateTextPrimary,
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("add_user_input_name")
                        )

                        Text("Tizimdagi Maqomi (Category):", fontSize = 11.sp, color = SlateTextSecondary)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Farzand", "Do'st", "Xodim", "Boshliq").forEach { type ->
                                val isSel = newUserType == type
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) ElectricBlue.copy(alpha = 0.2f) else Color.Transparent)
                                        .border(1.dp, if (isSel) ElectricBlue else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            newUserType = type
                                            newUserEmoji = when (type) {
                                                "Farzand" -> "👦"
                                                "Do'st" -> "👩"
                                                "Xodim" -> "🚴"
                                                else -> "💼"
                                            }
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(type, fontSize = 10.sp, color = if (isSel) Color.White else SlateTextSecondary)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = newUserEmoji,
                            onValueChange = { newUserEmoji = it },
                            label = { Text("Avatar Emoji (Masalan: 👦)", color = SlateTextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SlateTextPrimary,
                                unfocusedTextColor = SlateTextPrimary,
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newUserName.isNotBlank()) {
                                val baseLat = 41.311081
                                val baseLng = 69.240562
                                val offsetLat = baseLat + (java.util.Random().nextDouble() - 0.5) * 0.012
                                val offsetLng = baseLng + (java.util.Random().nextDouble() - 0.5) * 0.012
                                viewModel.addTrackableUser(
                                    name = newUserName,
                                    type = newUserType,
                                    emoji = newUserEmoji,
                                    lat = offsetLat,
                                    lng = offsetLng
                                )
                                showAddUserDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Text("Qo'shish", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddUserDialog = false }) {
                        Text("Bekor qilish", color = SlateTextSecondary)
                    }
                },
                containerColor = DarkSurface
            )
        }

        // --- Bottom User Details Card (Animates on marker click) ---
        AnimatedVisibility(
            visible = state.selectedUserForDetails != null,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            state.selectedUserForDetails?.let { user ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(18.dp)
                    ) {
                        // Card Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(ElectricBlue.copy(alpha = 0.2f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(user.avatarEmoji, fontSize = 24.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = user.name,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateTextPrimary
                                )
                                Text(
                                    text = "Roli: ${user.type}",
                                    fontSize = 12.sp,
                                    color = SlateTextSecondary
                                )
                            }
                            IconButton(onClick = { viewModel.selectUserForDetails(null) }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = SlateTextSecondary)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Grid (Loc, battery, stay-time)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Manzil", fontSize = 11.sp, color = SlateTextSecondary)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = user.locationName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SlateTextPrimary,
                                        maxLines = 1
                                    )
                                }
                            }

                            Card(
                                modifier = Modifier.weight(1f),
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AccessTime, contentDescription = null, tint = ElectricYellow, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Stay-time", fontSize = 11.sp, color = SlateTextSecondary)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = user.durationText,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SlateTextPrimary
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Batareya quvvati: ${user.batteryLevel}%",
                                fontSize = 12.sp,
                                color = if (user.batteryLevel > 20) NeonGreen else HotRed,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Kolloratsiya: Real-Time",
                                fontSize = 11.sp,
                                color = SlateTextSecondary
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Actions Bar inside details card
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onNavigateToChat(
                                        if (user.type == "Farzand" || user.type == "Siz") "group_family" else "group_company"
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Chat, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Chatlashish")
                            }

                            if (user.type == "Farzand" || user.type == "Xodim") {
                                OutlinedButton(
                                    onClick = {
                                        // Trigger a mock request for tracking disconnect
                                        viewModel.childRequestDisconnect("Maktabdan keyin do'stlarim bilan kofe ichyapmiz, 1 soat ruxsat bering")
                                        viewModel.selectUserForDetails(null)
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = SlateTextPrimary),
                                    shape = RoundedCornerShape(12.dp),
                                    border = borderStroke(SlateTextSecondary)
                                ) {
                                    Icon(Icons.Default.PowerSettingsNew, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Disconnect So'rash")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Simple legacy border helper inside Compose
@Composable
fun borderStroke(color: Color) = androidx.compose.foundation.BorderStroke(1.dp, color)

@Composable
fun LeafletMap(
    users: List<MapUser>,
    safetyMode: SafetyMode,
    selectedUser: MapUser?,
    onUserClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val htmlContent = remember {
        """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
            <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" />
            <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
            <style>
                html, body, #map {
                    height: 100%;
                    width: 100%;
                    margin: 0;
                    padding: 0;
                    background-color: #0b0c10;
                }
                .custom-marker-wrapper {
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    justify-content: center;
                }
                .pulse-ring {
                    border-radius: 50%;
                    width: 32px;
                    height: 32px;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    animation: pulse-glow 2s infinite;
                }
                .marker-emoji {
                    font-size: 16px;
                }
                .marker-label {
                    background: rgba(17, 17, 17, 0.9);
                    color: #FFFFFF;
                    font-family: sans-serif;
                    font-size: 9px;
                    font-weight: bold;
                    padding: 1px 4px;
                    border-radius: 4px;
                    margin-top: 2px;
                    border: 0.5px solid rgba(255, 255, 255, 0.15);
                    white-space: nowrap;
                    box-shadow: 0 1px 3px rgba(0,0,0,0.5);
                }
                @keyframes pulse-glow {
                    0% {
                        box-shadow: 0 0 0 0px var(--glow-color, rgba(0, 110, 255, 0.7));
                    }
                    70% {
                        box-shadow: 0 0 0 8px var(--glow-color, rgba(0, 110, 255, 0));
                    }
                    100% {
                        box-shadow: 0 0 0 0px var(--glow-color, rgba(0, 110, 255, 0));
                    }
                }
            </style>
        </head>
        <body>
            <div id="map"></div>
            <script>
                var map = L.map('map', {
                    zoomControl: false,
                    attributionControl: false
                }).setView([41.311081, 69.240562], 13);

                L.tileLayer('https://{s}.basemaps.cartocdn.com/dark_all/{z}/{x}/{y}{r}.png', {
                    maxZoom: 20
                }).addTo(map);

                var markers = {};

                function getMarkerColor(type, battery, safetyMode) {
                    if (type === 'Siz') return '#006EFF';
                    if (type === 'Farzand') return safetyMode === 'POWER_OFF' ? '#FF2E63' : '#08F7A4';
                    if (type === 'Boshliq') return '#E0E0E0';
                    if (type === 'Xodim') return battery < 15 ? '#FBC02D' : '#006EFF';
                    if (type === 'Voqea') return '#FF9F1C'; // Vibrant safety orange
                    return '#08F7A4';
                }

                function getMarkerGlowColor(type, battery, safetyMode) {
                    if (type === 'Siz') return 'rgba(0, 110, 255, 0.6)';
                    if (type === 'Farzand') return safetyMode === 'POWER_OFF' ? 'rgba(255, 46, 99, 0.6)' : 'rgba(8, 247, 164, 0.6)';
                    if (type === 'Boshliq') return 'rgba(255, 255, 255, 0.4)';
                    if (type === 'Xodim') return battery < 15 ? 'rgba(251, 192, 45, 0.6)' : 'rgba(0, 110, 255, 0.6)';
                    if (type === 'Voqea') return 'rgba(255, 159, 28, 0.6)'; // Vibrant safety orange glow
                    return 'rgba(8, 247, 164, 0.6)';
                }

                function createCustomIcon(user, safetyMode) {
                    var color = getMarkerColor(user.type, user.battery, safetyMode);
                    var glowColor = getMarkerGlowColor(user.type, user.battery, safetyMode);
                    var shortName = user.name.split(' ')[0];
                    
                    var html = '<div class="custom-marker-wrapper">' +
                               '  <div class="pulse-ring" style="border: 2px solid ' + color + '; --glow-color: ' + glowColor + '; background: ' + color + '1F;">' +
                               '    <span class="marker-emoji">' + user.avatarEmoji + '</span>' +
                               '  </div>' +
                               '  <div class="marker-label">' + shortName + '</div>' +
                               '</div>';
                               
                    return L.divIcon({
                        html: html,
                        className: 'custom-leaflet-div-icon',
                        iconSize: [40, 50],
                        iconAnchor: [20, 25]
                    });
                }

                function updateMarkers(usersJson, safetyMode) {
                    var users = JSON.parse(usersJson);
                    var activeIds = {};

                    users.forEach(function(user) {
                        var lat = user.lat;
                        var lng = user.lng;
                        var id = user.id;
                        activeIds[id] = true;

                        var customIcon = createCustomIcon(user, safetyMode);

                        if (markers[id]) {
                            markers[id].setLatLng([lat, lng]);
                            markers[id].setIcon(customIcon);
                        } else {
                            var marker = L.marker([lat, lng], { icon: customIcon }).addTo(map);
                            marker.on('click', function() {
                                if (window.VeyoAndroid) {
                                    window.VeyoAndroid.onUserClicked(id);
                                }
                            });
                            markers[id] = marker;
                        }
                    });

                    for (var key in markers) {
                        if (!activeIds[key]) {
                            map.removeLayer(markers[key]);
                            delete markers[key];
                        }
                    }
                }

                function centerMapOn(lat, lng, zoom) {
                    map.setView([lat, lng], zoom || 14);
                }
            </script>
        </body>
        </html>
        """.trimIndent()
    }

    fun serializeUsers(usersList: List<MapUser>): String {
        return buildString {
            append("[")
            usersList.forEachIndexed { index, user ->
                if (index > 0) append(",")
                append("{")
                append("\"id\":\"${user.id}\",")
                append("\"name\":\"${user.name}\",")
                append("\"type\":\"${user.type}\",")
                append("\"lat\":${user.currentLat},")
                append("\"lng\":${user.currentLng},")
                append("\"battery\":${user.batteryLevel},")
                append("\"avatarEmoji\":\"${user.avatarEmoji}\"")
                append("}")
            }
            append("]")
        }
    }

    LaunchedEffect(users, safetyMode, webViewRef) {
        val webView = webViewRef ?: return@LaunchedEffect
        val serialized = serializeUsers(users)
        val safetyModeStr = safetyMode.name
        webView.evaluateJavascript("javascript:updateMarkers('$serialized', '$safetyModeStr')", null)
    }

    LaunchedEffect(selectedUser, webViewRef) {
        val webView = webViewRef ?: return@LaunchedEffect
        selectedUser?.let {
            webView.evaluateJavascript("javascript:centerMapOn(${it.currentLat}, ${it.currentLng}, 15)", null)
        }
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = WebViewClient()
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    useWideViewPort = true
                    loadWithOverviewMode = true
                }
                addJavascriptInterface(object {
                    @JavascriptInterface
                    fun onUserClicked(userId: String) {
                        post {
                            onUserClick(userId)
                        }
                    }
                }, "VeyoAndroid")

                loadDataWithBaseURL("https://example.com", htmlContent, "text/html", "UTF-8", null)
                webViewRef = this
            }
        },
        modifier = modifier.fillMaxSize(),
        update = {
            webViewRef = it
        }
    )
}
