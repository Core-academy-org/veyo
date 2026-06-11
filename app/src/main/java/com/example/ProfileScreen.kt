package com.example

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: VeyoViewModel,
    state: VeyoUiState
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Local states for editing profile fields
    var localBio by remember(state.profileBio) { mutableStateOf(state.profileBio) }
    var selectedAvatarType by remember(state.avatarType) { mutableStateOf(state.avatarType) } // "emoji", "image_tech", "image_explorer", "image_safety"
    var selectedEmoji by remember(state.avatarEmoji) { mutableStateOf(state.avatarEmoji) }
    var selectedBgColorHex by remember(state.avatarBgColorHex) { mutableStateOf(state.avatarBgColorHex) }
    var selectedFrameStyle by remember(state.avatarFrameStyle) { mutableStateOf(state.avatarFrameStyle) } // "simple", "neon_glow", "double_ring"

    // Edit modes
    var isEditingBio by remember { mutableStateOf(false) }
    var showAvatarCustomizer by remember { mutableStateOf(false) }

    val presetColors = listOf(
        "#0DF9A3" to "Neon Green",
        "#006EFF" to "Electric Blue",
        "#FF2E63" to "Hot Red",
        "#9D4EDD" to "Purple Nest",
        "#FF9F1C" to "Warm Apricot",
        "#3A0CA3" to "Deep Abyss"
    )

    val presetEmojis = listOf(
        "⚡", "👦", "👩", "🚴", "🦊", "🤖", "🛡️", "👾", "🕵️", "🚀", "🪐", "💎", "🔥", "🌈"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("profile_screen")
    ) {
        // --- HEADER ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "HISOB VA MAXFIYLIK",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = ElectricBlue,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Shaxsiy ma'lumotlar va geolokatsiya xavfsizligi.",
                    fontSize = 12.sp,
                    color = SlateTextSecondary
                )
            }
            Box(
                modifier = Modifier
                    .background(ElectricBlue.copy(alpha = 0.08f), RoundedCornerShape(12.dp))
                    .border(1.dp, ElectricBlue.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "ID: 409-${state.username.take(3).uppercase()}",
                    color = ElectricBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- MAIN PROFILE CARD ---
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.02f)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Customized avatar container with chosen frame style
                val frameModifier = when (selectedFrameStyle) {
                    "neon_glow" -> Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .border(3.dp, Color(android.graphics.Color.parseColor(selectedBgColorHex)), CircleShape)
                        .background(Color.White.copy(alpha = 0.04f))
                    "double_ring" -> Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .border(2.dp, ElectricBlue, CircleShape)
                        .padding(4.dp)
                        .border(2.dp, NeonGreen, CircleShape)
                        .background(Color.White.copy(alpha = 0.04f))
                    else -> Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        .background(Color.White.copy(alpha = 0.04f))
                }

                Box(
                    modifier = frameModifier,
                    contentAlignment = Alignment.Center
                ) {
                    when (selectedAvatarType) {
                        "image_tech" -> {
                            Image(
                                painter = painterResource(id = R.drawable.img_avatar_tech),
                                contentDescription = "Tech Avatar",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        "image_explorer" -> {
                            Image(
                                painter = painterResource(id = R.drawable.img_avatar_explorer),
                                contentDescription = "Explorer Avatar",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        "image_safety" -> {
                            Image(
                                painter = painterResource(id = R.drawable.img_avatar_safety),
                                contentDescription = "Safety Avatar",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                        else -> {
                            // Emoji style
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(android.graphics.Color.parseColor(selectedBgColorHex))),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = selectedEmoji,
                                    fontSize = 42.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showAvatarCustomizer = !showAvatarCustomizer },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(32.dp).border(1.dp, ElectricBlue.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Icon(Icons.Default.Palette, contentDescription = "Edit", tint = ElectricBlue, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Avatarni sozlash", color = ElectricBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = state.username,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = SlateTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .background(NeonGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Muhrlangan",
                            tint = DarkBg,
                            modifier = Modifier.size(10.dp)
                        )
                    }
                }

                Text(
                    text = state.userEmail,
                    fontSize = 12.sp,
                    color = SlateTextSecondary,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Divider(color = Color.White.copy(alpha = 0.05f))

                Spacer(modifier = Modifier.height(10.dp))

                // Bio Description Section
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MEN HAQIMDA (BIO)",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = SlateTextSecondary,
                            letterSpacing = 0.5.sp
                        )
                        IconButton(
                            onClick = { isEditingBio = !isEditingBio },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = if (isEditingBio) Icons.Default.Done else Icons.Default.Edit,
                                contentDescription = "Edit Bio",
                                tint = ElectricBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }

                    if (isEditingBio) {
                        OutlinedTextField(
                            value = localBio,
                            onValueChange = { localBio = it },
                            textStyle = LocalTextStyle.current.copy(color = SlateTextPrimary, fontSize = 13.sp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.01f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.01f),
                                focusedBorderColor = ElectricBlue,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.1f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp)
                                .testTag("profile_bio_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                isEditingBio = false
                                viewModel.updateProfile(
                                    bio = localBio,
                                    avatarType = selectedAvatarType,
                                    emoji = selectedEmoji,
                                    colorHex = selectedBgColorHex,
                                    frameStyle = selectedFrameStyle
                                )
                                Toast.makeText(context, "Profil ma'lumotlari saqlandi!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.align(Alignment.End).height(32.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                        ) {
                            Text("Bio-ni Saqlash", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Text(
                            text = localBio,
                            fontSize = 13.sp,
                            color = SlateTextPrimary,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // --- AVATAR EDITOR DRAWER/PANEL ---
        AnimatedVisibility(
            visible = showAvatarCustomizer,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AVATAR VA STILNI SOZLASH",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Type Choice Choice tab row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White.copy(alpha = 0.03f), RoundedCornerShape(10.dp))
                            .padding(2.dp)
                    ) {
                        listOf(
                            "preset" to "Illyustratsiyalar",
                            "emoji" to "Emoji & Rang"
                        ).forEach { (typeKey, label) ->
                            val isSelected = if (typeKey == "preset") {
                                selectedAvatarType.startsWith("image_")
                            } else {
                                selectedAvatarType == "emoji"
                            }
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) ElectricBlue else Color.Transparent)
                                    .clickable {
                                        selectedAvatarType = if (typeKey == "preset") "image_tech" else "emoji"
                                    }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else SlateTextSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (selectedAvatarType.startsWith("image_")) {
                        // PRESET IMAGE CHOICES
                        Text("Dizayn rasm paketini tanlang:", fontSize = 11.sp, color = SlateTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            listOf(
                                "image_tech" to R.drawable.img_avatar_tech to "Tech",
                                "image_explorer" to R.drawable.img_avatar_explorer to "Sayohat",
                                "image_safety" to R.drawable.img_avatar_safety to "Himoya"
                            ).forEach { (triple, label) ->
                                val (type, resId) = triple
                                val isChosen = selectedAvatarType == type
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isChosen) ElectricBlue.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.01f))
                                        .border(
                                            1.5.dp,
                                            if (isChosen) ElectricBlue else Color.White.copy(alpha = 0.05f),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedAvatarType = type }
                                        .padding(8.dp)
                                ) {
                                    Image(
                                        painter = painterResource(id = resId),
                                        contentDescription = label,
                                        modifier = Modifier.size(54.dp).clip(CircleShape),
                                        contentScale = ContentScale.Crop
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(label, color = SlateTextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    } else {
                        // EMOJI & BACKGROUND COLOR CHOICES
                        Text("Fon rangini tanlang:", fontSize = 11.sp, color = SlateTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            presetColors.forEach { (hex, name) ->
                                val isSelected = selectedBgColorHex.equals(hex, ignoreCase = true)
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(Color(android.graphics.Color.parseColor(hex)))
                                        .border(
                                            2.dp,
                                            if (isSelected) Color.White else Color.Transparent,
                                            CircleShape
                                        )
                                        .clickable { selectedBgColorHex = hex }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Emoji ikonasini tanlang:", fontSize = 11.sp, color = SlateTextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState(), enabled = false),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            presetEmojis.take(7).forEach { emoji ->
                                val isSelected = selectedEmoji == emoji
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent)
                                        .border(
                                            1.dp,
                                            if (isSelected) ElectricBlue else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedEmoji = emoji },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(emoji, fontSize = 18.sp)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            presetEmojis.drop(7).forEach { emoji ->
                                val isSelected = selectedEmoji == emoji
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color.White.copy(alpha = 0.15f) else Color.Transparent)
                                        .border(
                                            1.dp,
                                            if (isSelected) ElectricBlue else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedEmoji = emoji },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(emoji, fontSize = 18.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // BORDER FRAME STYLES Choice
                    Text("Ramka/Border Dizayni:", fontSize = 11.sp, color = SlateTextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "simple" to "Oddiy Ramka",
                            "neon_glow" to "Fluo Neon",
                            "double_ring" to "Ergo Double"
                        ).forEach { (style, label) ->
                            val isSelected = selectedFrameStyle == style
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) ElectricBlue.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.02f))
                                    .border(
                                        1.dp,
                                        if (isSelected) ElectricBlue else Color.White.copy(alpha = 0.06f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .clickable { selectedFrameStyle = style }
                                    .padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(label, color = if (isSelected) Color.White else SlateTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.updateProfile(
                                bio = localBio,
                                avatarType = selectedAvatarType,
                                emoji = selectedEmoji,
                                colorHex = selectedBgColorHex,
                                frameStyle = selectedFrameStyle
                            )
                            showAvatarCustomizer = false
                            Toast.makeText(context, "Avatar mukammal o'zgartirildi!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(42.dp)
                    ) {
                        Text("YIG'ISH VA SAQLASH", color = DarkBg, fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- PRIVACY PREFERENCES PANEL ---
        Text(
            text = "SHAXSIYLIK VA MAXFIYLIK SOZLAMALARI",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = SlateTextSecondary,
            letterSpacing = 0.5.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.012f)),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color.White.copy(alpha = 0.04f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // 1. Location Visibility
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Loc", tint = ElectricBlue, modifier = Modifier.size(16.dp))
                    Text("Mening joylashuvimni kimlar ko'ra oladi?", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateTextPrimary)
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Location toggle rows
                val locOptions = listOf(
                    "EVERYONE" to "Barchaga",
                    "FRIENDS" to "Sinflar & Do'stlar",
                    "FAMILY" to "Faqat Oila",
                    "NOBODY" to "Hech kimga (Yopiq)"
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    locOptions.forEach { (key, title) ->
                        val isSelected = state.privacyLocationVisibility == key
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) ElectricBlue.copy(alpha = 0.08f) else Color.Transparent)
                                .border(
                                    0.5.dp,
                                    if (isSelected) ElectricBlue.copy(alpha = 0.3f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    viewModel.updatePrivacySettings(key, state.privacyActivityVisibility)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(title, color = if (isSelected) Color.White else SlateTextSecondary, fontSize = 12.sp)
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = ElectricBlue, modifier = Modifier.size(16.dp))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(16.dp))

                // 2. Activity Visibility
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Security, contentDescription = "Act", tint = NeonGreen, modifier = Modifier.size(16.dp))
                    Text("Holat va faoliyatlarim ko'rinishi", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SlateTextPrimary)
                }

                Spacer(modifier = Modifier.height(10.dp))

                val actOptions = listOf(
                    "EVERYONE" to "Ochiq holat",
                    "FAMILY" to "Faqat Vasyat (Oila)",
                    "NOBODY" to "Maxfiy rejim"
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    actOptions.forEach { (key, title) ->
                        val isSelected = state.privacyActivityVisibility == key
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) NeonGreen.copy(alpha = 0.08f) else Color.Transparent)
                                .border(
                                    0.5.dp,
                                    if (isSelected) NeonGreen.copy(alpha = 0.3f) else Color.Transparent,
                                    RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    viewModel.updatePrivacySettings(state.privacyLocationVisibility, key)
                                }
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(title, color = if (isSelected) Color.White else SlateTextSecondary, fontSize = 12.sp)
                            if (isSelected) {
                                Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = NeonGreen, modifier = Modifier.size(16.dp))
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- ACCOUNT CONTROL / LOGOUT ---
        Button(
            onClick = {
                viewModel.logout()
            },
            colors = ButtonDefaults.buttonColors(containerColor = HotRed.copy(alpha = 0.12f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .border(1.dp, HotRed.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Logout,
                contentDescription = "Chiqish",
                tint = HotRed,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Hisobdan Chiqish (Log out)",
                fontSize = 13.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = "VEYO v1.2.6 ● Daxlsiz Geokuzatuv Tarmog'i",
            color = SlateTextSecondary.copy(alpha = 0.6f),
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(bottom = 30.dp)
        )
    }
}
