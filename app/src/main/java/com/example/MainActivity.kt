package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                VeyoAppContainer()
            }
        }
    }
}

@Composable
fun VeyoAppContainer() {
    val viewModel: VeyoViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()

    var showSplashScreen by remember { mutableStateOf(true) }
    var currentTab by remember { mutableStateOf("chat") } // "globe", "safehub", "chat", "profile"
    var userLayoutPreference by remember { mutableStateOf("auto") } // "auto", "phone", "desktop"

    // Animation scale for the glowing brand logo
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_logo")
    val logoScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    if (showSplashScreen) {
        // --- Glowing Interactive Splash / Onboarding intro ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(DarkBg, DarkSurface)
                    )
                )
                .testTag("brand_splash_screen"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(24.dp)
            ) {
                // Pulse brand emblem
                Box(
                    modifier = Modifier
                        .scale(logoScale)
                        .size(130.dp)
                        .background(ElectricBlue.copy(alpha = 0.12f), CircleShape)
                        .border(2.dp, ElectricBlue, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .scale(logoScale * 0.9f)
                            .size(76.dp)
                            .background(NeonGreen.copy(alpha = 0.15f), CircleShape)
                            .border(1.5.dp, NeonGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "🛰️",
                            fontSize = 32.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "VEYO",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Black,
                    color = SlateTextPrimary,
                    letterSpacing = 2.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Yaqinlaringiz va jamoangiz bilan doim bir to'lqinda, har qadamda daxlsiz xavfsiz.",
                    fontSize = 14.sp,
                    color = SlateTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Badge illustrating Triple-Shield Tech
                Box(
                    modifier = Modifier
                        .background(DarkSurfaceElevated, RoundedCornerShape(20.dp))
                        .border(1.dp, ElectricBlue.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(NeonGreen, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "TRIPLE-SHIELD FAILSAPE ONLINE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                    }
                }

                Spacer(modifier = Modifier.height(64.dp))

                // Interactive tap to enter button
                Button(
                    onClick = { showSplashScreen = false },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(56.dp)
                        .testTag("onboarding_start_button")
                ) {
                    Text(
                        "Dasturga Kirish (Enter VEYO)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = "Demo Version • Built for Pitch",
                    fontSize = 10.sp,
                    color = SlateTextSecondary
                )
            }
        }
    } else if (!state.isLoggedIn) {
        GoogleSignInScreen(viewModel = viewModel)
    } else {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWideScreen = maxWidth > 720.dp
            val useDesktopLayout = isWideScreen

            // --- Core VEYO Scaffold Dashboard ---
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = DarkBg,
                topBar = {
                    // Immersive App Header matching the design HTML specs
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Avatar profile frame with premium dual-color gradient ring
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        brush = Brush.sweepGradient(
                                            colors = listOf(ElectricBlue, NeonGreen, ElectricBlue)
                                        )
                                    )
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape)
                                        .background(DarkBg),
                                    contentAlignment = Alignment.Center
                                ) {
                                    val initials = if (state.username.isNotBlank()) {
                                        state.username.split(" ").filter { it.isNotBlank() }.map { it.take(1) }.take(2).joinToString("").uppercase()
                                    } else "BJ"
                                    Text(
                                        text = initials,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // User names & status metadata
                            Column {
                                Text(
                                    text = "VEYO SECURE",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = SlateTextSecondary,
                                    letterSpacing = 1.5.sp
                                )
                                Text(
                                    text = state.username,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateTextPrimary
                                )
                            }
                        }

                        // Interactive utility notification controls & Layout Mode Switcher
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .background(NeonGreen.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                                    .border(1.dp, NeonGreen.copy(alpha = 0.25f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "● Online",
                                    color = NeonGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.06f))
                                    .clickable {
                                        // Trigger quick search toggle
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search profiles",
                                    tint = SlateTextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    // Touch target accessibility 48dp
                                    .minimumInteractiveComponentSize()
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.06f))
                                    .clickable {
                                        // Trigger notification flyout drawer
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications alerts",
                                    tint = SlateTextPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                // Smart live green alert badge dot matching design spec
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = (-10).dp, y = (10).dp)
                                        .size(7.dp)
                                        .background(NeonGreen, CircleShape)
                                        .border(1.dp, DarkBg, CircleShape)
                                )
                            }
                        }
                    }
                },
                bottomBar = {
                    if (!useDesktopLayout) {
                        // Highly customized bottom nav dock representing index buttons CSS details
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(78.dp)
                                .background(DarkBg)
                                .border(width = 1.dp, color = Color.White.copy(alpha = 0.05f), shape = RoundedCornerShape(0.dp))
                                .navigationBarsPadding()
                                .testTag("veyo_bottom_nav_bar"),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val tabs = listOf(
                                Triple("chat", "V-Chat", Icons.Default.ChatBubble),
                                Triple("globe", "Xarita", Icons.Default.Map),
                                Triple("safehub", "Safe-Hub", Icons.Default.Security),
                                Triple("profile", "Profil", Icons.Default.Person)
                            )

                            tabs.forEach { (tabId, label, icon) ->
                                val isSelected = currentTab == tabId
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            currentTab = tabId
                                            if (tabId != "chat") {
                                                viewModel.selectChat(null)
                                            }
                                        }
                                        .testTag("nav_tab_${tabId}"),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(width = 56.dp, height = 32.dp)
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isSelected) ElectricBlue.copy(alpha = 0.12f) else Color.Transparent),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = label,
                                            tint = if (isSelected) ElectricBlue else SlateTextSecondary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        
                                        // Red unread bubble marker on chat icon matching Design spec "3"
                                        if (tabId == "chat" && !isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.TopEnd)
                                                    .offset(x = 2.dp, y = (-2).dp)
                                                    .size(15.dp)
                                                    .background(HotRed, CircleShape),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    "3",
                                                    color = Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(5.dp))
                                    Text(
                                        text = label,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) ElectricBlue else SlateTextSecondary,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(DarkBg)
                ) {
                    // Main Content Immersive view frame mimicking: mx-4 mb-4 rounded-[32px] overflow-hidden bg-[#111111]
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 12.dp)
                            .padding(horizontal = 14.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .border(1.5.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(30.dp))
                            .background(DarkSurface)
                    ) {
                        if (useDesktopLayout) {
                            // Render desktop dual-pane row with navigation rail
                            Row(modifier = Modifier.fillMaxSize()) {
                                // 1. Vertical Navigation Rail (Sidebar Sidebar)
                                Column(
                                    modifier = Modifier
                                        .width(76.dp)
                                        .fillMaxHeight()
                                        .background(Color.White.copy(alpha = 0.015f))
                                        .padding(vertical = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Box(
                                            modifier = Modifier
                                                .size(44.dp)
                                                .background(ElectricBlue.copy(alpha = 0.12f), CircleShape)
                                                .border(1.dp, ElectricBlue.copy(alpha = 0.4f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text("🛰️", fontSize = 18.sp)
                                        }
                                        
                                        Spacer(modifier = Modifier.height(32.dp))
                                        
                                        // Vertical Navigation Tab Icons
                                        val railTabs = listOf(
                                            Triple("chat", "V-Chat", Icons.Default.ChatBubble),
                                            Triple("globe", "Xarita", Icons.Default.Map),
                                            Triple("safehub", "Safe-Hub", Icons.Default.Security),
                                            Triple("profile", "Profil", Icons.Default.Person)
                                        )
                                        
                                        railTabs.forEach { (tabId, label, icon) ->
                                            val isSelected = currentTab == tabId
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                modifier = Modifier
                                                    .padding(vertical = 10.dp)
                                                    .clickable {
                                                        currentTab = tabId
                                                        if (tabId != "chat") {
                                                            viewModel.selectChat(null)
                                                        }
                                                    }
                                                    .testTag("desktop_rail_${tabId}")
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(46.dp)
                                                        .clip(RoundedCornerShape(12.dp))
                                                        .background(if (isSelected) ElectricBlue.copy(alpha = 0.15f) else Color.Transparent)
                                                        .border(
                                                            width = 1.dp,
                                                            color = if (isSelected) ElectricBlue.copy(alpha = 0.3f) else Color.Transparent,
                                                            shape = RoundedCornerShape(12.dp)
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = icon,
                                                        contentDescription = label,
                                                        tint = if (isSelected) ElectricBlue else SlateTextSecondary,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = label,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isSelected) ElectricBlue else SlateTextSecondary
                                                )
                                            }
                                        }
                                    }
                                    
                                    // Bottom status key
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(NeonGreen.copy(alpha = 0.1f), CircleShape)
                                            .border(1.dp, NeonGreen.copy(alpha = 0.4f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🟢", fontSize = 11.sp)
                                    }
                                }
                                
                                // Vertical Inner Divider
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(1.dp)
                                        .background(Color.White.copy(alpha = 0.06f))
                                )
                                
                                // 2. Central Split Content Pane
                                Box(modifier = Modifier.weight(1f)) {
                                    when (currentTab) {
                                        "globe" -> {
                                            Row(modifier = Modifier.fillMaxSize()) {
                                                Box(modifier = Modifier.weight(1f)) {
                                                    TheGlobeScreen(
                                                        viewModel = viewModel,
                                                        state = state,
                                                        onNavigateToChat = { chatId ->
                                                            currentTab = "chat"
                                                            viewModel.selectChat(chatId)
                                                        }
                                                    )
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .width(1.dp)
                                                        .background(Color.White.copy(alpha = 0.05f))
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .width(310.dp)
                                                        .fillMaxHeight()
                                                        .background(Color.White.copy(alpha = 0.015f))
                                                ) {
                                                    GlobeDesktopSidebar(viewModel = viewModel, state = state, onNavigateToChat = { tabName ->
                                                        currentTab = tabName
                                                    })
                                                }
                                            }
                                        }
                                        "safehub" -> {
                                            Row(modifier = Modifier.fillMaxSize()) {
                                                Box(modifier = Modifier.weight(1f)) {
                                                    SafeHubScreen(
                                                        viewModel = viewModel,
                                                        state = state
                                                    )
                                                }
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .width(1.dp)
                                                        .background(Color.White.copy(alpha = 0.05f))
                                                )
                                                Box(
                                                    modifier = Modifier
                                                        .width(310.dp)
                                                        .fillMaxHeight()
                                                        .background(Color.White.copy(alpha = 0.015f))
                                                ) {
                                                    SafeHubDesktopSidebar(viewModel = viewModel, state = state)
                                                }
                                            }
                                        }
                                        "chat" -> {
                                            Row(modifier = Modifier.fillMaxSize()) {
                                                // Left master threads index panel
                                                Box(
                                                    modifier = Modifier
                                                        .width(300.dp)
                                                        .fillMaxHeight()
                                                        .background(Color.White.copy(alpha = 0.005f))
                                                ) {
                                                    ChatSearchAndListPanel(viewModel = viewModel, state = state)
                                                }
                                                
                                                // Inner thread list divider
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxHeight()
                                                        .width(1.dp)
                                                        .background(Color.White.copy(alpha = 0.05f))
                                                )
                                                
                                                // Right detail conversation workspace (Telegram style!)
                                                Box(modifier = Modifier.weight(1f)) {
                                                    if (state.activeChatId != null) {
                                                        VChatScreen(
                                                            viewModel = viewModel,
                                                            state = state,
                                                            selectedThreadId = state.activeChatId,
                                                            onSelectThread = { chatId ->
                                                                viewModel.selectChat(chatId)
                                                            }
                                                        )
                                                    } else {
                                                        DesktopChatPlaceholder()
                                                    }
                                                }
                                            }
                                        }
                                        "profile" -> {
                                            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                                ProfileScreen(viewModel = viewModel, state = state)
                                            }
                                        }

                                    }
                                }
                            }
                        } else {
                            // Classical smartphone stacking logic
                            when (currentTab) {
                                "globe" -> TheGlobeScreen(
                                    viewModel = viewModel,
                                    state = state,
                                    onNavigateToChat = { chatId ->
                                        currentTab = "chat"
                                        viewModel.selectChat(chatId)
                                    }
                                )
                                "safehub" -> SafeHubScreen(
                                    viewModel = viewModel,
                                    state = state
                                )
                                "chat" -> VChatScreen(
                                    viewModel = viewModel,
                                    state = state,
                                    selectedThreadId = state.activeChatId,
                                    onSelectThread = { chatId ->
                                        viewModel.selectChat(chatId)
                                    }
                                )
                                "profile" -> ProfileScreen(viewModel = viewModel, state = state)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GlobeDesktopSidebar(
    viewModel: VeyoViewModel,
    state: VeyoUiState,
    onNavigateToChat: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(NeonGreen, CircleShape)
            )
            Text(
                text = "NAZORAT KONSOLI",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SlateTextPrimary,
                letterSpacing = 1.sp
            )
        }
        Text(
            text = "Real-vaqtda barcha terminallar geotelemertiyasi",
            fontSize = 10.sp,
            color = SlateTextSecondary,
            modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
        )

        Divider(color = Color.White.copy(alpha = 0.05f))

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "KUZATILAYOTGAN FOYDALANUVCHILAR (${state.users.size})",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SlateTextSecondary,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(state.users) { user ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.selectedUserForDetails?.id == user.id) 
                            ElectricBlue.copy(alpha = 0.1f) else Color.White.copy(alpha = 0.015f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectUserForDetails(user) }
                        .border(
                            1.dp, 
                            if (state.selectedUserForDetails?.id == user.id) ElectricBlue.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(user.avatarEmoji, fontSize = 20.sp)
                                Column {
                                    Text(
                                        text = user.name,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (user.isOnline) SlateTextPrimary else SlateTextSecondary
                                    )
                                    Text(
                                        text = user.type,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = ElectricBlue
                                    )
                                }
                            }
                            // Battery
                            val batteryColor = when {
                                user.batteryLevel > 50 -> NeonGreen
                                user.batteryLevel > 20 -> Color(0xFFFBC02D)
                                else -> HotRed
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text("🔋", fontSize = 10.sp)
                                Text(
                                    text = "${user.batteryLevel}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = batteryColor
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "📍 ${user.locationName}",
                            fontSize = 10.sp,
                            color = SlateTextPrimary,
                            maxLines = 1
                        )
                        Text(
                            text = "⏱️ ${user.durationText}",
                            fontSize = 9.sp,
                            color = SlateTextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Coordinate feedback streamer
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(6.dp)
                        ) {
                            Text(
                                text = "LAT: ${"%.6f".format(user.currentLat)}  LON: ${"%.6f".format(user.currentLng)}",
                                fontSize = 8.sp,
                                color = NeonGreen.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Chat navigate hot button
                        Button(
                            onClick = {
                                val targetChatId = if (user.id == "farzand") "group_family" else "group_company"
                                viewModel.selectChat(targetChatId)
                                onNavigateToChat("chat")
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(28.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue.copy(alpha = 0.15f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubble,
                                    contentDescription = "Suhbat",
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(10.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Suhbatlashish",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SafeHubDesktopSidebar(
    viewModel: VeyoViewModel,
    state: VeyoUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(HotRed, CircleShape)
            )
            Text(
                text = "TRIPLE SHIELD TELEMETRY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = SlateTextPrimary,
                letterSpacing = 0.5.sp
            )
        }
        Text(
            text = "Mesh va Failsafe zaxira monitoringi",
            fontSize = 10.sp,
            color = SlateTextSecondary,
            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
        )

        Divider(color = Color.White.copy(alpha = 0.05f))

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "XAVFSIZLIK MEXANIZMI",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SlateTextSecondary,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Safety selectors triggering
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val safetyModes = listOf(
                Triple(SafetyMode.NORMAL, "NORMAL (GPS/LTE)", ElectricBlue),
                Triple(SafetyMode.OFFLINE, "OFFLINE (Mesh Net)", Color(0xFFFBC02D)),
                Triple(SafetyMode.POWER_OFF, "POWER LOST (SOS)", HotRed)
            )

            safetyModes.forEach { (mode, label, color) ->
                val isSelected = state.safetyMode == mode
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) color.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.02f))
                        .border(
                            1.dp, 
                            if (isSelected) color else Color.White.copy(alpha = 0.05f),
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { viewModel.setSafetyMode(mode) }
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else SlateTextSecondary
                        )
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(color, CircleShape)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "LIVE SHIELD LOGS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SlateTextSecondary,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Monospace terminal logger
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
                .padding(10.dp)
        ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(state.simulationLogs) { log ->
                    val color = when (log.level) {
                        "CRITICAL" -> HotRed
                        "WARN" -> Color(0xFFFBC02D)
                        else -> ElectricBlue
                    }
                    Column {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "[${log.timestamp}]",
                                fontSize = 8.sp,
                                color = SlateTextSecondary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = log.service,
                                fontSize = 8.sp,
                                color = color,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = log.message,
                            fontSize = 8.sp,
                            color = SlateTextPrimary,
                            modifier = Modifier.padding(top = 1.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Divider(color = Color.White.copy(alpha = 0.04f))
                    }
                }
            }
        }
    }
}



@Composable
fun ChatSearchAndListPanel(
    viewModel: VeyoViewModel,
    state: VeyoUiState
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("Barchasi") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        // High-tech search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { 
                searchQuery = it 
            },
            placeholder = { Text("Muloqotlarni qidirish...", fontSize = 11.sp, color = SlateTextSecondary) },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White.copy(alpha = 0.02f),
                unfocusedContainerColor = Color.White.copy(alpha = 0.02f),
                focusedBorderColor = ElectricBlue,
                unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                focusedTextColor = SlateTextPrimary,
                unfocusedTextColor = SlateTextPrimary
            ),
            shape = RoundedCornerShape(10.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Categories chips matching Telegram Desktop Design and look
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Barchasi", "Guruhlar", "Kanallar").forEach { cat ->
                val isSel = selectedTab == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSel) ElectricBlue.copy(alpha = 0.15f) else Color.Transparent)
                        .border(1.dp, if (isSel) ElectricBlue else Color.White.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                        .clickable { selectedTab = cat }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        cat, 
                        fontSize = 10.sp, 
                        fontWeight = FontWeight.Bold, 
                        color = if (isSel) Color.White else SlateTextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Thread entries columns
        val filteredThreads = state.chatThreads.filter {
            val matchesSearch = it.title.contains(searchQuery, ignoreCase = true) || 
                                it.lastMessage.contains(searchQuery, ignoreCase = true)
            val matchesCategory = when (selectedTab) {
                "Guruhlar" -> !it.isChannel
                "Kanallar" -> it.isChannel
                else -> true
            }
            matchesSearch && matchesCategory
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(filteredThreads) { thread ->
                val isActive = state.activeChatId == thread.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isActive) ElectricBlue.copy(alpha = 0.1f) else Color.Transparent)
                        .border(
                            width = 1.dp, 
                            color = if (isActive) ElectricBlue.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.03f), 
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { viewModel.selectChat(thread.id) }
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(Color.White.copy(alpha = 0.05f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(thread.iconEmoji, fontSize = 18.sp)
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = thread.title,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateTextPrimary,
                                maxLines = 1
                            )
                            if (thread.isChannel) {
                                Box(
                                    modifier = Modifier
                                        .background(HotRed.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                ) {
                                    Text("KANAL", fontSize = 7.sp, color = HotRed, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                        Text(
                            text = thread.lastMessage,
                            fontSize = 9.sp,
                            color = SlateTextSecondary,
                            maxLines = 1,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DesktopChatPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .background(Color.White.copy(alpha = 0.02f), CircleShape)
                    .border(2.dp, ElectricBlue.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ChatBubble,
                    contentDescription = "No messaging selected",
                    tint = ElectricBlue.copy(alpha = 0.6f),
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "VEYO SECURE CLIENT • WEB APP",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = SlateTextPrimary,
                letterSpacing = 1.sp
            )
            Text(
                text = "Muloqot qilish, ruxsat so'rab trackingni cheklash va live radar darslarini kuzatish uchun chap paneldagi suhbatlardan birini tanlang.",
                fontSize = 10.sp,
                color = SlateTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .padding(top = 6.dp, bottom = 18.dp)
                    .widthIn(max = 280.dp)
            )

            // Current secure ticks dashboard
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.3f)),
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TOSHKENT, O'ZBEKISTON",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = NeonGreen
                    )
                    Text(
                        text = "LIVE SECURE TELEMETRY ACTIVE",
                        fontSize = 8.sp,
                        color = SlateTextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun GoogleSignInScreen(viewModel: VeyoViewModel) {
    var isCreateMode by remember { mutableStateOf(true) }
    var selectedName by remember { mutableStateOf("Bahodir Jo'rayev") }
    var inputEmail by remember { mutableStateOf("jbahodir770@gmail.com") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBg)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(28.dp))
                .padding(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Branded Google Multi-color G-icon
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .background(Color.White.copy(alpha = 0.03f), CircleShape)
                        .border(1.5.dp, Color.White.copy(alpha = 0.08f), CircleShape)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = this.size.width
                            val h = this.size.height
                            // Draw Google multi-color arcs around the edge
                            drawArc(
                                color = Color(0xFFEA4335), // Red
                                startAngle = 180f,
                                sweepAngle = 90f,
                                useCenter = true
                            )
                            drawArc(
                                color = Color(0xFF4285F4), // Blue
                                startAngle = 270f,
                                sweepAngle = 90f,
                                useCenter = true
                            )
                            drawArc(
                                color = Color(0xFF34A853), // Green
                                startAngle = 0f,
                                sweepAngle = 90f,
                                useCenter = true
                            )
                            drawArc(
                                color = Color(0xFFFBBC05), // Yellow
                                startAngle = 90f,
                                sweepAngle = 90f,
                                useCenter = true
                            )
                            // Draw center mask to make a ring
                            drawCircle(
                                color = Color.White,
                                radius = this.size.width * 0.38f,
                                center = Offset(w/2f, h/2f)
                            )
                        }
                        Text(
                            text = "G",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF4285F4) // Google Blue
                        )
                    }
                }

                Text(
                    text = if (isCreateMode) "Yangi Akkount Yaratish" else "Akkountni Qaytarish",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = SlateTextPrimary,
                    letterSpacing = 0.5.sp
                )

                Text(
                    text = if (isCreateMode) 
                        "Google Account orqali tezda yangi maxfiy profil yarating."
                    else 
                        "Mavjud Google profilingizni kiritib tarmoqni qayta tiklang.",
                    fontSize = 11.sp,
                    color = SlateTextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )

                // Tab Selector for Create vs Restore
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(12.dp))
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isCreateMode) ElectricBlue.copy(alpha = 0.12f) else Color.Transparent)
                            .border(1.dp, if (isCreateMode) ElectricBlue.copy(alpha = 0.4f) else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { isCreateMode = true }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Akkayunt Yaratish",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isCreateMode) Color.White else SlateTextSecondary
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isCreateMode) ElectricBlue.copy(alpha = 0.12f) else Color.Transparent)
                            .border(1.dp, if (!isCreateMode) ElectricBlue.copy(alpha = 0.4f) else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { isCreateMode = false }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Akkountni Tiklash",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!isCreateMode) Color.White else SlateTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (isCreateMode) {
                    OutlinedTextField(
                        value = selectedName,
                        onValueChange = { selectedName = it },
                        label = { Text("To'liq ism-sharf", color = SlateTextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = SlateTextPrimary,
                            unfocusedTextColor = SlateTextPrimary,
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                            focusedContainerColor = Color.White.copy(alpha = 0.02f),
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("google_login_name"),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                OutlinedTextField(
                    value = inputEmail,
                    onValueChange = { inputEmail = it },
                    label = { Text("Google Gmail manzili", color = SlateTextSecondary) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = SlateTextPrimary,
                        unfocusedTextColor = SlateTextPrimary,
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                        focusedContainerColor = Color.White.copy(alpha = 0.02f),
                        unfocusedContainerColor = Color.Transparent
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("google_login_email"),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        val finalName = if (isCreateMode) selectedName else {
                            if (inputEmail.contains("@")) inputEmail.substringBefore("@").replaceFirstChar { it.uppercase() } else "Foydalanuvchi"
                        }
                        viewModel.performGoogleSignIn(finalName, inputEmail)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("google_signin_submit_button")
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .background(Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("G", color = Color(0xFF4285F4), fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                        Text(
                            text = if (isCreateMode) "Google bilan oson yaratish" else "Google bilan qaytarish",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
