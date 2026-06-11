package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafeHubScreen(
    viewModel: VeyoViewModel,
    state: VeyoUiState
) {
    var customDisconnectReason by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(horizontal = 16.dp)
            .testTag("safe_hub_list"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 48.dp)
    ) {
        // --- Premium Header ---
        item {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                Text(
                    text = "SAFE-HUB",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = ElectricBlue,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Xavfsizlikni boshqarish va anti-tamper ruxsatnomalar markazi.",
                    fontSize = 13.sp,
                    color = SlateTextSecondary
                )
            }
        }

        // --- Active Tracker Statistics ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .background(ElectricBlue.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(28.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Meni necha kishi kuzatyapti?",
                            fontSize = 12.sp,
                            color = SlateTextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "${state.trackingCount} ta faol kuzatuvchi",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonGreen
                        )
                    }
                }
            }
        }

        // --- Triple-Shield Real-Time Simulated Environment ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectricBlue.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "🛡️ TRIPLE-SHIELD PROTOKOL SIMULATOR",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Telefon o'chganda yoki internet uzilganda ham doimiy kuzatuv texnologiyasini sinab ko'ring:",
                        fontSize = 11.sp,
                        color = SlateTextSecondary
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3 mode toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val modes = listOf(
                            Triple(SafetyMode.NORMAL, "NORMAL", Icons.Default.CheckCircle),
                            Triple(SafetyMode.OFFLINE, "OFFLINE MESH", Icons.Default.WifiOff),
                            Triple(SafetyMode.POWER_OFF, "BATAREYA 0%", Icons.Default.BatteryAlert)
                        )

                        modes.forEach { (mode, title, icon) ->
                            val isSelected = state.safetyMode == mode
                            val btnBg by animateColorAsState(if (isSelected) ElectricBlue else DarkSurfaceElevated)
                            val btnContentColor by animateColorAsState(if (isSelected) Color.White else SlateTextPrimary)

                            Button(
                                onClick = { viewModel.setSafetyMode(mode) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("shield_mode_${mode.name}"),
                                colors = ButtonDefaults.buttonColors(containerColor = btnBg, contentColor = btnContentColor),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                Icon(icon, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(title, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Scrolling Terminal Diagnostic Console
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black)
                            .border(1.dp, Color(0xFF1E2F1E), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(state.simulationLogs) { log ->
                                val logColor = when (log.level) {
                                    "CRITICAL" -> HotRed
                                    "WARN" -> ElectricYellow
                                    else -> NeonGreen
                                }
                                Text(
                                    text = "[${log.timestamp}] [${log.service}] ${log.message}",
                                    color = logColor,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- Disconnect Requests Panel (The critical business rule) ---
        item {
            Text(
                text = "Disconnect Sarlavhalari (Ruxsatnomalar)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SlateTextPrimary,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (state.disconnectRequests.none { it.status == "PENDING" }) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Hozircha hech qanday disconnect qilish ruxsatlari yo'q.",
                            color = SlateTextSecondary,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        } else {
            items(state.disconnectRequests.filter { it.status == "PENDING" }) { req ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("disconnect_request_${req.id}"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(ElectricYellow.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Schedule, contentDescription = null, tint = ElectricYellow, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = req.requesterName,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateTextPrimary
                                )
                                Text(
                                    text = "Roli: ${req.requesterType} • ${req.time}",
                                    fontSize = 11.sp,
                                    color = SlateTextSecondary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = "Sabab: \"${req.reason}\"",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = SlateTextPrimary
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.approveDisconnect(req.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_approve_${req.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Ruxsat berish", fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                            }

                            Button(
                                onClick = { viewModel.denyDisconnect(req.id) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("btn_deny_${req.id}"),
                                colors = ButtonDefaults.buttonColors(containerColor = HotRed),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Rad etish", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // --- Outward Tracking Requests & Network Control ---
        item {
            Text(
                text = "Kuzatish So'rovlari (Request Network)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = SlateTextPrimary,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        if (state.trackingRequests.isEmpty()) {
            item {
                Text(
                    "Hech qanday qo'shimcha so'rovlar mavjud emas.",
                    color = SlateTextSecondary,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }
        } else {
            items(state.trackingRequests) { req ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tracking_request_${req.id}"),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (req.direction == "INCOMING") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                            contentDescription = null,
                            tint = if (req.direction == "INCOMING") NeonGreen else ElectricBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(req.personName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = SlateTextPrimary)
                            Text(
                                text = if (req.direction == "INCOMING") "Sizni kuzatishni so'rayapti" else "Kuzatish so'rovi yuborilgan",
                                fontSize = 11.sp,
                                color = SlateTextSecondary
                            )
                        }

                        if (req.direction == "INCOMING" && req.status == "PENDING") {
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { viewModel.acceptIncomingRequest(req.id) }) {
                                    Icon(Icons.Default.Check, contentDescription = "Accept", tint = NeonGreen)
                                }
                                IconButton(onClick = { viewModel.declineRequest(req.id) }) {
                                    Icon(Icons.Default.Close, contentDescription = "Decline", tint = HotRed)
                                }
                            }
                        } else {
                            Text(
                                text = req.status,
                                fontSize = 11.sp,
                                color = if (req.status == "ACCEPTED") NeonGreen else ElectricYellow,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // --- Demo Simulation Trigger: Add a dynamic Child Disconnect request ---
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Simulyatsiya: Farzand/Xodim tomondan disconnect so'rash",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customDisconnectReason,
                        onValueChange = { customDisconnectReason = it },
                        placeholder = { Text("Disconnect sababini kiriting...", fontSize = 12.sp, color = SlateTextSecondary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("custom_reason_input"),
                        textStyle = TextStyle(color = SlateTextPrimary, fontSize = 13.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = SlateTextSecondary
                        )
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val reason = if (customDisconnectReason.isEmpty()) "Do'stlar bilan do'konda, dars tugadi" else customDisconnectReason
                            viewModel.childRequestDisconnect(reason)
                            customDisconnectReason = ""
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("simulate_disconnect_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Boladan Disconnect request jo'natish", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
