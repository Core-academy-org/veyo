package com.example

import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VChatScreen(
    viewModel: VeyoViewModel,
    state: VeyoUiState,
    selectedThreadId: String?,
    onSelectThread: (String?) -> Unit
) {
    val context = LocalContext.current
    if (selectedThreadId == null) {
        // --- CHAT THREADS LIST SCREEN ---
        var showCreateChatDialog by remember { mutableStateOf(false) }
        var showAddFriendDialog by remember { mutableStateOf(false) }
        var localSearchQuery by remember { mutableStateOf("") }
        var localFolderTab by remember { mutableStateOf("Barchasi") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .testTag("chat_list")
        ) {
            // Header Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "VEYO CHAT",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = ElectricBlue,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = "Doimiy xavfsiz va faol aloqa markazi",
                        fontSize = 11.sp,
                        color = SlateTextSecondary
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Search/Add friend to track button
                    IconButton(
                        onClick = { showAddFriendDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(NeonGreen.copy(alpha = 0.12f))
                            .border(1.dp, NeonGreen.copy(alpha = 0.25f), CircleShape)
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = "Do'st qo'shish",
                            tint = NeonGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Standard channel/chat button
                    IconButton(
                        onClick = { showCreateChatDialog = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(ElectricBlue.copy(alpha = 0.12f))
                            .border(1.dp, ElectricBlue.copy(alpha = 0.25f), CircleShape)
                            .size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Yangi suhbat",
                            tint = ElectricBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Telegram-style Search Bar
            OutlinedTextField(
                value = localSearchQuery,
                onValueChange = { localSearchQuery = it },
                placeholder = { Text("Muloqotlar va kanallarni qidirish...", fontSize = 13.sp, color = SlateTextSecondary) },
                singleLine = true,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = SlateTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (localSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { localSearchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = SlateTextSecondary, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .height(48.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = DarkSurface.copy(alpha = 0.5f),
                    unfocusedContainerColor = DarkSurface.copy(alpha = 0.3f),
                    focusedBorderColor = ElectricBlue.copy(alpha = 0.6f),
                    unfocusedBorderColor = Color.White.copy(alpha = 0.08f),
                    focusedTextColor = SlateTextPrimary,
                    unfocusedTextColor = SlateTextPrimary
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Telegram Folder-style Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Barchasi", "Guruhlar", "Kanallar").forEach { cat ->
                    val isSel = localFolderTab == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSel) ElectricBlue.copy(alpha = 0.15f) else Color.Transparent)
                            .border(
                                1.dp,
                                if (isSel) ElectricBlue else Color.White.copy(alpha = 0.08f),
                                RoundedCornerShape(20.dp)
                            )
                            .clickable { localFolderTab = cat }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = cat,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSel) Color.White else SlateTextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Filtering logic
            val filteredThreads = state.chatThreads.filter { thread ->
                val matchesSearch = thread.title.contains(localSearchQuery, ignoreCase = true) || 
                                    thread.lastMessage.contains(localSearchQuery, ignoreCase = true)
                val matchesFolder = when (localFolderTab) {
                    "Guruhlar" -> !thread.isChannel
                    "Kanallar" -> thread.isChannel
                    else -> true
                }
                matchesSearch && matchesFolder
            }

            if (filteredThreads.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .background(Color.White.copy(alpha = 0.02f), CircleShape)
                                .border(1.dp, ElectricBlue.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = "Bo'sh chat",
                                tint = ElectricBlue.copy(alpha = 0.5f),
                                modifier = Modifier.size(26.dp)
                              )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Muloqotlar topilmadi",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextPrimary
                        )
                        Text(
                            text = if (localSearchQuery.isNotEmpty()) "Qidiruv bo'yicha hech narsa topilmadi." else "Ushbu ruknda faol suhbatlar mavjud emas.",
                            fontSize = 11.sp,
                            color = SlateTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 48.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredThreads) { thread ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onSelectThread(thread.id) }
                                .testTag("chat_thread_${thread.id}")
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Dynamic circle avatar with active status badge
                                Box(
                                    modifier = Modifier.size(46.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .background(
                                                if (thread.activeLiveStream) HotRed.copy(alpha = 0.15f) else ElectricBlue.copy(alpha = 0.12f),
                                                CircleShape
                                            )
                                            .border(
                                                width = if (thread.activeLiveStream) 1.5.dp else 1.dp,
                                                color = if (thread.activeLiveStream) HotRed else ElectricBlue.copy(alpha = 0.25f),
                                                shape = CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(thread.iconEmoji, fontSize = 22.sp)
                                    }

                                    // Online indicator or Live broadcast badge
                                    if (thread.activeLiveStream) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(HotRed, CircleShape)
                                                .border(1.5.dp, DarkBg, CircleShape)
                                                .align(Alignment.BottomEnd)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .background(NeonGreen, CircleShape)
                                                .border(1.5.dp, DarkBg, CircleShape)
                                                .align(Alignment.BottomEnd)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = thread.title,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = SlateTextPrimary,
                                            maxLines = 1
                                        )

                                        // Time check indicator
                                        Text(
                                            text = if (thread.messages.isNotEmpty()) thread.messages.last().time else "Hozir",
                                            fontSize = 10.sp,
                                            color = SlateTextSecondary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(3.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = thread.lastMessage,
                                            fontSize = 12.sp,
                                            color = SlateTextSecondary,
                                            maxLines = 1,
                                            modifier = Modifier.weight(1f)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        // Beautiful Badge Pill
                                        if (thread.activeLiveStream) {
                                            Box(
                                                modifier = Modifier
                                                    .background(HotRed, RoundedCornerShape(8.dp))
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                            ) {
                                                Text(
                                                    text = "LIVE",
                                                    color = Color.White,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Black
                                                )
                                            }
                                        } else {
                                            // Soft badge indicating type
                                            Box(
                                                modifier = Modifier
                                                    .background(ElectricBlue.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                                                    .padding(horizontal = 6.dp, vertical = 1.5.dp)
                                            ) {
                                                Text(
                                                    text = if (thread.isChannel) "Kanal" else "Guruh",
                                                    color = ElectricBlue,
                                                    fontSize = 8.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            // Underline divider exactly like Telegram
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(Color.White.copy(alpha = 0.04f))
                            )
                        }
                    }
                }
            }
        }

        if (showAddFriendDialog) {
            var friendName by remember { mutableStateOf("") }
            var friendEmail by remember { mutableStateOf("") }
            var selectedType by remember { mutableStateOf("Do'st") }

            AlertDialog(
                onDismissRequest = { showAddFriendDialog = false },
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("👦", fontSize = 22.sp)
                        Text("Mening Do'stlarim & Kuzatish", fontWeight = FontWeight.Bold, color = SlateTextPrimary)
                    }
                },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Do'stingiz ushbu daxlsiz Veyo ilovasiga kirganida uni xaritadan qidirib topish uchun uning Ma'lumotlarini to'ldiring. U birdaniga xaritamizda faol geolokatsiya va siz bilan maxfiy chat orqali bog'lanadi!",
                            fontSize = 11.sp,
                            color = SlateTextSecondary,
                            lineHeight = 14.sp
                        )

                        OutlinedTextField(
                            value = friendName,
                            onValueChange = { friendName = it },
                            label = { Text("Foydalanuvchi ismi (Ism-sharf)", color = SlateTextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SlateTextPrimary,
                                unfocusedTextColor = SlateTextPrimary,
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("add_friend_name_input")
                        )

                        OutlinedTextField(
                            value = friendEmail,
                            onValueChange = { friendEmail = it },
                            label = { Text("Google Gmail (Akkayunt)", color = SlateTextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SlateTextPrimary,
                                unfocusedTextColor = SlateTextPrimary,
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("add_friend_email_input")
                        )

                        Text("Kuzatish guruhi / Roli:", fontSize = 11.sp, color = SlateTextSecondary, fontWeight = FontWeight.Bold)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Do'st", "Farzand", "Xodim").forEach { role ->
                                val isSelected = selectedType == role
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) ElectricBlue.copy(alpha = 0.15f) else Color.Transparent)
                                        .border(1.dp, if (isSelected) ElectricBlue else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .clickable { selectedType = role }
                                        .padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(role, fontSize = 11.sp, color = if (isSelected) Color.White else SlateTextSecondary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (friendName.isNotEmpty()) {
                                viewModel.addNewTrackedUser(friendName, friendEmail, selectedType)
                                showAddFriendDialog = false
                                Toast.makeText(context, "$friendName daxlsiz tarmoqqa muvaffaqiyatli ulashildi!", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Iltimos, ismini yozing!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Text("Tarmoqqa Uash", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddFriendDialog = false }) {
                        Text("Bekor qilish", color = SlateTextSecondary)
                    }
                },
                containerColor = DarkSurface,
                shape = RoundedCornerShape(24.dp)
            )
        }

        if (showCreateChatDialog) {
            var chatTitle by remember { mutableStateOf("") }
            var isChannel by remember { mutableStateOf(false) }
            var chatEmoji by remember { mutableStateOf("👥") }

            // Group configuration states
            val invitedMembers = remember { mutableStateListOf<String>() }
            var isLocationLinked by remember { mutableStateOf(false) }
            var eventTitle by remember { mutableStateOf("") }
            var locationName by remember { mutableStateOf("") }
            var latInput by remember { mutableStateOf("41.3110") }
            var lngInput by remember { mutableStateOf("69.2405") }
            var eventDesc by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showCreateChatDialog = false },
                title = { Text("Yangi suhbat/kanal yaratish", fontWeight = FontWeight.Bold, color = SlateTextPrimary) },
                text = {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 450.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        OutlinedTextField(
                            value = chatTitle,
                            onValueChange = { chatTitle = it },
                            label = { Text("Muloqot/Guruh Nomi", color = SlateTextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SlateTextPrimary,
                                unfocusedTextColor = SlateTextPrimary,
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("create_chat_input_title")
                        )

                        Text("Muloqot turi (Type):", fontSize = 11.sp, color = SlateTextSecondary, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Guruh (Group)", "Kanal (Channel)").forEach { type ->
                                val channelChoice = (type == "Kanal (Channel)")
                                val selected = channelChoice == isChannel
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) ElectricBlue.copy(alpha = 0.2f) else Color.Transparent)
                                        .border(1.dp, if (selected) ElectricBlue else Color.White.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                                        .clickable {
                                            isChannel = channelChoice
                                            chatEmoji = if (isChannel) "📢" else "👥"
                                        }
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(type, fontSize = 11.sp, color = if (selected) Color.White else SlateTextSecondary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        OutlinedTextField(
                            value = chatEmoji,
                            onValueChange = { chatEmoji = it },
                            label = { Text("Avatar Emoji (Ikonka)", color = SlateTextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = SlateTextPrimary,
                                unfocusedTextColor = SlateTextPrimary,
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.12f)
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (!isChannel) {
                            // GROUP TYPE CONFIGURATIONS
                            Divider(color = Color.White.copy(alpha = 0.08f))

                            Text("Guruhga a'zolar taklif qilish:", fontSize = 11.sp, color = SlateTextSecondary, fontWeight = FontWeight.Bold)
                            
                            // Checkable rows for members (filter out the self user so we invite others)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                state.users.filter { it.id != "me" }.forEach { user ->
                                    val isInvited = invitedMembers.contains(user.name)
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isInvited) ElectricBlue.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.01f))
                                            .border(0.5.dp, if (isInvited) ElectricBlue.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.05f), RoundedCornerShape(8.dp))
                                            .clickable {
                                                if (isInvited) invitedMembers.remove(user.name) else invitedMembers.add(user.name)
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(user.avatarEmoji, fontSize = 14.sp)
                                            Text(user.name.split(" ")[0], color = SlateTextPrimary, fontSize = 12.sp)
                                        }
                                        Checkbox(
                                            checked = isInvited,
                                            onCheckedChange = {
                                                if (isInvited) invitedMembers.remove(user.name) else invitedMembers.add(user.name)
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = ElectricBlue)
                                        )
                                    }
                                }
                            }

                            Divider(color = Color.White.copy(alpha = 0.08f))

                            // GEOLOCATION ACTION LINK
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Guruhni tadbirga biriktirish", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateTextPrimary)
                                    Text("Uchrashuv koordinatasi va tafsiloti", fontSize = 10.sp, color = SlateTextSecondary)
                                }
                                Switch(
                                    checked = isLocationLinked,
                                    onCheckedChange = { isLocationLinked = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = ElectricBlue, checkedTrackColor = ElectricBlue.copy(alpha = 0.3f))
                                )
                            }

                            if (isLocationLinked) {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = eventTitle,
                                        onValueChange = { eventTitle = it },
                                        label = { Text("Tadbir/Voqea Nomi (Masalan: Dars vaqti)", color = SlateTextSecondary) },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    OutlinedTextField(
                                        value = locationName,
                                        onValueChange = { locationName = it },
                                        label = { Text("Uchrashuv Binosi (Masalan: 9-maktab)", color = SlateTextSecondary) },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedTextField(
                                            value = latInput,
                                            onValueChange = { latInput = it },
                                            label = { Text("Lat (Kenglik)", color = SlateTextSecondary) },
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                        OutlinedTextField(
                                            value = lngInput,
                                            onValueChange = { lngInput = it },
                                            label = { Text("Lng (Uzoqlik)", color = SlateTextSecondary) },
                                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue),
                                            singleLine = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }

                                    OutlinedTextField(
                                        value = eventDesc,
                                        onValueChange = { eventDesc = it },
                                        label = { Text("Tadbir batafsil tavsifi", color = SlateTextSecondary) },
                                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ElectricBlue),
                                        singleLine = false,
                                        maxLines = 3,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (chatTitle.isNotBlank()) {
                                if (isChannel) {
                                    viewModel.createChatThread(chatTitle, isChannel, chatEmoji)
                                } else {
                                    val finalMembers = invitedMembers.toList() + "Siz"
                                    viewModel.createGroupChat(
                                        title = chatTitle,
                                        iconEmoji = chatEmoji,
                                        members = finalMembers,
                                        eventName = if (isLocationLinked) eventTitle else null,
                                        locationName = if (isLocationLinked) locationName else null,
                                        lat = if (isLocationLinked) latInput.toDoubleOrNull() ?: 41.3110 else null,
                                        lng = if (isLocationLinked) lngInput.toDoubleOrNull() ?: 69.2405 else null,
                                        eventDesc = if (isLocationLinked) eventDesc else null
                                    )
                                }
                                showCreateChatDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                    ) {
                        Text("Yaratish", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCreateChatDialog = false }) {
                        Text("Bekor qilish", color = SlateTextSecondary)
                    }
                },
                containerColor = DarkSurface
            )
        }
    } else {
        // --- SPECIFIC CHAT CONVERSATION VIEW ---
        val thread = state.chatThreads.find { it.id == selectedThreadId } ?: return
        var chatInputText by remember { mutableStateOf("") }
        var showLiveMemberCoordinates by remember { mutableStateOf(false) }

        val members = if (thread.isGroup == true) {
            state.users.filter { user ->
                thread.members.any { memberName ->
                    user.name.contains(memberName, ignoreCase = true) || 
                    memberName.contains(user.name, ignoreCase = true) || 
                    (user.id == "me" && (memberName.contains("Siz", ignoreCase = true) || memberName.contains("me", ignoreCase = true)))
                }
            }
        } else if (thread.id == "group_family") {
            state.users.filter { it.type == "Farzand" || it.type == "Siz" || it.type == "Do'st" }
        } else if (thread.id == "group_company") {
            state.users.filter { it.type == "Xodim" || it.type == "Boshliq" || it.type == "Siz" }
        } else {
            state.users
        }

        var sinOffset by remember { mutableFloatStateOf(0f) }

        // Live stream wave animation trigger
        if (thread.activeLiveStream) {
            LaunchedEffect(Unit) {
                while (true) {
                    delay(50)
                    sinOffset = (sinOffset + 0.15f) % (2 * Math.PI.toFloat())
                }
            }
        }

        val listState = rememberLazyListState()

        // Auto-scroll to latest messages
        LaunchedEffect(thread.messages.size) {
            if (thread.messages.isNotEmpty()) {
                listState.animateScrollToItem(thread.messages.size - 1)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .testTag("chat_conversation_${selectedThreadId}")
        ) {
            // --- COLLAPSIBLE SIDEBAR: Availability and Location Context ---
            Column(
                modifier = Modifier
                    .width(132.dp)
                    .fillMaxHeight()
                    .background(Color.White.copy(alpha = 0.02f))
                    .padding(vertical = 12.dp, horizontal = 6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(NeonGreen, CircleShape)
                    )
                    Text(
                        text = "A'ZOLAR JONLI",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Black,
                        color = SlateTextSecondary,
                        letterSpacing = 1.sp
                    )
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(members) { user ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (user.id == "me") Color.White.copy(alpha = 0.04f) else Color.Transparent)
                                .padding(6.dp)
                        ) {
                            // Member info with pulsing beacon indicator
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(Color.White.copy(alpha = 0.08f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(user.avatarEmoji, fontSize = 12.sp)
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (user.id == "me") "Siz" else user.name.split(" ")[0],
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SlateTextPrimary,
                                        maxLines = 1
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(5.dp)
                                                .background(if (user.isOnline) NeonGreen else SlateTextSecondary, CircleShape)
                                        )
                                        Text(
                                            text = if (user.isOnline) "faol" else "oflayn",
                                            fontSize = 7.sp,
                                            color = if (user.isOnline) NeonGreen else SlateTextSecondary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            // Live location context
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(3.dp),
                                modifier = Modifier.padding(start = 2.dp)
                            ) {
                                Text(
                                    text = "📍",
                                    fontSize = 8.sp,
                                    modifier = Modifier.padding(top = 1.dp)
                                )
                                Text(
                                    text = user.locationName,
                                    fontSize = 9.sp,
                                    color = SlateTextSecondary,
                                    maxLines = 2,
                                    lineHeight = 11.sp
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(3.dp))
                            
                            // Battery & tracking status
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                modifier = Modifier.padding(start = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = "Battery power",
                                    tint = if (user.batteryLevel < 35) HotRed else NeonGreen,
                                    modifier = Modifier.size(9.dp)
                                )
                                Text(
                                    text = "${user.batteryLevel}%",
                                    fontSize = 8.sp,
                                    color = SlateTextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // VERTICAL DIVIDER LINE between Sidebar and Main Content
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(1.dp)
                    .background(Color.White.copy(alpha = 0.06f))
            )

            // --- DISCUSSION & MESSAGE SCROLLER PANEL ---
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                // Header (interactive location controls & return vectors) wrapped in column for bottom divider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.02f))
                            .padding(horizontal = 8.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { onSelectThread(null) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Orqaga", tint = SlateTextPrimary)
                        }

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(ElectricBlue.copy(alpha = 0.15f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(thread.iconEmoji, fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = thread.title,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = SlateTextPrimary
                            )
                            Text(
                                text = if (thread.isChannel) "Jonli dars kanali" else "Veyo xavfsiz monitoring guruhi",
                                fontSize = 11.sp,
                                color = SlateTextSecondary
                            )
                        }

                        if (!thread.isChannel) {
                            IconButton(onClick = { showLiveMemberCoordinates = !showLiveMemberCoordinates }) {
                                Icon(
                                    imageVector = if (showLiveMemberCoordinates) Icons.Default.LocationOff else Icons.Default.LocationOn,
                                    contentDescription = "Toggle telemetry settings",
                                    tint = if (showLiveMemberCoordinates) NeonGreen else SlateTextPrimary
                                )
                            }
                        }
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.05f)))
                }

                val context = LocalContext.current

                // --- LINKED LOCATION / EVENT BANNER ---
                if (thread.linkedEventName != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .border(1.dp, ElectricBlue.copy(alpha = 0.25f), RoundedCornerShape(12.dp)),
                        colors = CardDefaults.cardColors(containerColor = ElectricBlue.copy(alpha = 0.05f))
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(ElectricBlue.copy(alpha = 0.15f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Place, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(16.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "BOG'LANGAN VOQEA: ${thread.linkedEventName}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricBlue
                                )
                                Text(
                                    text = "${thread.linkedLocationName ?: "Noma'lum joy"} (${thread.linkedLat ?: 0.0}, ${thread.linkedLng ?: 0.0})",
                                    fontSize = 10.sp,
                                    color = SlateTextPrimary
                                )
                                if (thread.linkedEventDesc != null) {
                                    Text(
                                        text = thread.linkedEventDesc,
                                        fontSize = 9.sp,
                                        color = SlateTextSecondary,
                                        lineHeight = 11.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                            
                            // Center the map on this event!
                            Button(
                                onClick = {
                                    val dummyUser = MapUser(
                                        id = "linked_event_${thread.id}",
                                        name = thread.linkedEventName ?: "Tadbir",
                                        type = "Voqea",
                                        originalLat = thread.linkedLat ?: 41.3110,
                                        originalLng = thread.linkedLng ?: 69.2405,
                                        currentLat = thread.linkedLat ?: 41.3110,
                                        currentLng = thread.linkedLng ?: 69.2405,
                                        locationName = thread.linkedLocationName ?: "Bog'langan joy",
                                        durationText = "Guruh tadbiri",
                                        batteryLevel = 100,
                                        avatarEmoji = "📍"
                                    )
                                    viewModel.selectUserForDetails(dummyUser)
                                    Toast.makeText(context, "Xaritada voqea joyiga yo'naltirildi!", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(26.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text("Xarita", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Collateral Dropdown details
                AnimatedVisibility(visible = showLiveMemberCoordinates && !thread.isChannel) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.03f)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "A'zolar geolokatsiyasi va stay-time (nazorat):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonGreen
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            members.forEach { u ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(u.avatarEmoji, fontSize = 12.sp)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(u.name.split(" ")[0], fontSize = 11.sp, color = SlateTextPrimary, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = "${u.locationName} (${u.durationText})",
                                        fontSize = 10.sp,
                                        color = SlateTextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                // Live stream wave animation trigger (if active channel is broadcasted)
                if (thread.activeLiveStream) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color.Black.copy(alpha = 0.85f))
                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(16.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                color = HotRed.copy(alpha = 0.04f),
                                radius = size.width / 4.5f,
                                center = center,
                                style = Stroke(width = 2f)
                            )

                            // Simulated high-fidelity focus camera lines
                            val strokeW = 2f
                            val bracketLen = 16f
                            drawLine(Color.White.copy(alpha = 0.3f), Offset(10f, 10f), Offset(10f + bracketLen, 10f), strokeWidth = strokeW)
                            drawLine(Color.White.copy(alpha = 0.3f), Offset(10f, 10f), Offset(10f, 10f + bracketLen), strokeWidth = strokeW)
                            drawLine(Color.White.copy(alpha = 0.3f), Offset(size.width - 10f, 10f), Offset(size.width - 10f - bracketLen, 10f), strokeWidth = strokeW)
                            drawLine(Color.White.copy(alpha = 0.3f), Offset(size.width - 10f, 10f), Offset(size.width - 10f, 10f + bracketLen), strokeWidth = strokeW)

                            // Interactive flowing dynamic audio waves
                            val numPoints = 60
                            val stepX = size.width / numPoints
                            for (i in 0 until numPoints) {
                                val x = i * stepX
                                val freq = 0.18f
                                val amplitude = (size.height / 4f) * sin(i * freq + sinOffset)
                                val y = center.y + amplitude
                                drawCircle(
                                    color = HotRed.copy(alpha = 0.8f - (i.toFloat() / numPoints) * 0.4f),
                                    radius = 2.2f,
                                    center = Offset(x, y)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(modifier = Modifier.size(6.dp).background(HotRed, CircleShape))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("VEYO JONLI BROADCAST", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }

                        Row(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(10.dp)
                                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("340", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Messages list matching Immersive translucent glass-morphism aesthetic
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(ElectricBlue.copy(alpha = 0.05f), Color.Transparent),
                                center = Offset(150f, 300f),
                                radius = 500f
                            )
                        )
                ) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(top = 10.dp, bottom = 24.dp)
                    ) {
                        items(thread.messages) { msg ->
                            if (msg.isSystem) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(10.dp))
                                            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                            .padding(horizontal = 12.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = msg.text,
                                            fontSize = 11.sp,
                                            color = ElectricYellow,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            } else {
                                val isSelf = msg.sender.startsWith("Siz")
                                val alignment = if (isSelf) Alignment.End else Alignment.Start
                                val bubbleBg = if (isSelf) Color(0xFF2B5278) else Color(0xFF1C2534)
                                val bubbleContentColor = SlateTextPrimary

                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalAlignment = alignment
                                ) {
                                    if (!isSelf) {
                                        Text(
                                            text = msg.sender,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ElectricBlue,
                                            modifier = Modifier.padding(start = 12.dp, bottom = 2.dp)
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .widthIn(max = 260.dp)
                                            .clip(
                                                RoundedCornerShape(
                                                    topStart = 16.dp,
                                                    topEnd = 16.dp,
                                                    bottomStart = if (isSelf) 16.dp else 4.dp,
                                                    bottomEnd = if (isSelf) 4.dp else 16.dp
                                                )
                                            )
                                            .background(bubbleBg)
                                            .padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Column {
                                            Text(
                                                text = msg.text,
                                                color = bubbleContentColor,
                                                fontSize = 14.sp,
                                                lineHeight = 18.sp
                                            )
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Row(
                                                modifier = Modifier.align(Alignment.End),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = msg.time,
                                                    color = bubbleContentColor.copy(alpha = 0.45f),
                                                    fontSize = 9.sp
                                                )
                                                if (isSelf) {
                                                    Text(
                                                        text = "✓✓",
                                                        color = Color(0xFF5AB6FF), // soft sky blue ticks
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Glassy text messaging command dock wrapped with a top line divider
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.05f)))
                    
                    // --- TELEGRAM STYLE QUICK ACTIONS ACCESSORY ROW ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurface.copy(alpha = 0.4f))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Quick Emojis
                        listOf("👍", "🔥", "❤️", "🙌", "😂", "🤔").forEach { emoji ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.04f))
                                    .clickable {
                                        viewModel.sendMessage(thread.id, emoji)
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(emoji, fontSize = 12.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        // Smart Quick Attach Buttons like Telegram
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(ElectricBlue.copy(alpha = 0.12f))
                                .clickable {
                                    viewModel.sendMessage(thread.id, "📍 Jonli koordinatalar ulashildi: Toshkent (${state.users.find { it.id == "me" }?.currentLat ?: 41.3110}, ${state.users.find { it.id == "me" }?.currentLng ?: 69.2405})")
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("📍", fontSize = 10.sp)
                                Text("Joylashuv", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = ElectricBlue)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(HotRed.copy(alpha = 0.12f))
                                .clickable {
                                    viewModel.sendMessage(thread.id, "🚨 SHOSHILINCH (SOS): Mening geolokatsiyamni kuzating!")
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text("🚨", fontSize = 10.sp)
                                Text("SOS", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = HotRed)
                            }
                        }
                    }

                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.05f)))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding(),
                        color = Color.White.copy(alpha = 0.01f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextField(
                                value = chatInputText,
                                onValueChange = { chatInputText = it },
                                placeholder = { Text("Xabar yozish...", color = SlateTextSecondary, fontSize = 13.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("chat_input_textfield"),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White.copy(alpha = 0.04f),
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.04f),
                                    focusedTextColor = SlateTextPrimary,
                                    unfocusedTextColor = SlateTextPrimary,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(20.dp)
                            )

                            Spacer(modifier = Modifier.width(8.dp))

                            IconButton(
                                onClick = {
                                    viewModel.sendMessage(thread.id, chatInputText)
                                    chatInputText = ""
                                },
                                modifier = Modifier
                                    .background(ElectricBlue, CircleShape)
                                    .size(40.dp)
                                    .testTag("chat_send_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send message",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
