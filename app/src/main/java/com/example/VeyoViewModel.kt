package com.example

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

// Represents a tracked user on the map
data class MapUser(
    val id: String,
    val name: String,
    val type: String, // "Siz", "Farzand", "Do'st", "Boshliq", "Xodim"
    val originalLat: Double,
    val originalLng: Double,
    var currentLat: Double,
    var currentLng: Double,
    val locationName: String,
    val durationText: String,
    val batteryLevel: Int,
    val isOnline: Boolean = true,
    val avatarEmoji: String
)

// Represents a chatting channel or group
data class ChatThread(
    val id: String,
    val title: String,
    val isChannel: Boolean,
    val lastMessage: String,
    val messages: List<ChatMessage>,
    val activeLiveStream: Boolean = false,
    val iconEmoji: String,
    val isGroup: Boolean = false,
    val members: List<String> = emptyList(),
    val linkedEventName: String? = null,
    val linkedLocationName: String? = null,
    val linkedLat: Double? = null,
    val linkedLng: Double? = null,
    val linkedEventDesc: String? = null
)

data class ChatMessage(
    val sender: String,
    val text: String,
    val time: String,
    val isSystem: Boolean = false
)

// Disconnect Request model (Anti-tamper protocol)
data class DisconnectRequest(
    val id: String,
    val requesterName: String,
    val requesterType: String,
    val reason: String,
    val time: String,
    val status: String = "PENDING" // "PENDING", "APPROVED", "DENIED"
)

// Active tracking request sent to others
data class TrackingRequest(
    val id: String,
    val personName: String,
    val direction: String, // "INCOMING" (they want to track me), "OUTGOING" (I want to track them)
    val status: String = "PENDING" // "PENDING", "ACCEPTED"
)

// Triple Shield Safety States
enum class SafetyMode {
    NORMAL,
    OFFLINE, // No internet/Wi-Fi
    POWER_OFF // No power / phone shutdown
}

data class TripleShieldLog(
    val timestamp: String,
    val service: String, // "Mesh Network", "SMS Fallback", "Blackbox Recorder"
    val message: String,
    val level: String // "INFO", "WARN", "CRITICAL"
)

data class VeyoUiState(
    val isLoggedIn: Boolean = false,
    val username: String = "Bahodir Jo'rayev",
    val userEmail: String = "jbahodir770@gmail.com",
    val users: List<MapUser> = emptyList(),
    val chatThreads: List<ChatThread> = emptyList(),
    val disconnectRequests: List<DisconnectRequest> = emptyList(),
    val trackingRequests: List<TrackingRequest> = emptyList(),
    val safetyMode: SafetyMode = SafetyMode.NORMAL,
    val simulationLogs: List<TripleShieldLog> = emptyList(),
    val isRecordingBlackbox: Boolean = false,
    val activeChatId: String? = null,
    val searchResults: List<String> = emptyList(),
    val trackingCount: Int = 0, // start with 0
    val selectedUserForDetails: MapUser? = null,
    val profileBio: String = "Veyo xavfsizlik va daxlsizlik tizimi a'zosi. Real-time geolokatsiya va daxlsiz aloqa tarmog'i.",
    val avatarType: String = "image_tech", // "emoji", "image_tech", "image_explorer", "image_safety"
    val avatarEmoji: String = "⚡",
    val avatarBgColorHex: String = "#0DF9A3", // Neon neon green/blue
    val avatarFrameStyle: String = "neon_glow", // "simple", "neon_glow", "double_ring"
    val privacyLocationVisibility: String = "FRIENDS", // "EVERYONE", "FRIENDS", "FAMILY", "NOBODY"
    val privacyActivityVisibility: String = "FAMILY", // "EVERYONE", "FAMILY", "NOBODY"
    val firestoreStatus: String = "Ulanmagan (Daxlsiz Rejim)", // "Ulanmagan (Daxlsiz Rejim)", "Ulanmoqda...", "Faol (Onlayn)", "Xato"
    val isFirestoreSyncEnabled: Boolean = true,
    val firestoreProjectId: String = "",
    val firestoreApiKey: String = ""
)

class VeyoViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(VeyoUiState())
    val uiState: StateFlow<VeyoUiState> = _uiState.asStateFlow()

    init {
        loadMockData()
        startRealTimeSimulation()
    }

    private fun loadMockData() {
        val initialUsers = listOf(
            MapUser(
                id = "me",
                name = "Bahodir Jo'rayev (Siz)",
                type = "Siz",
                originalLat = 41.311081,
                originalLng = 69.240562,
                currentLat = 41.311081,
                currentLng = 69.240562,
                locationName = "Toshkent, O'zbekiston",
                durationText = "Hozirda faol",
                batteryLevel = 100,
                isOnline = true,
                avatarEmoji = "⚡"
            ),
            MapUser(
                id = "farzand",
                name = "Sardorbek Jo'rayev (Farzand)",
                type = "Farzand",
                originalLat = 41.3210,
                originalLng = 69.2550,
                currentLat = 41.3210,
                currentLng = 69.2550,
                locationName = "9-maktab binosi",
                durationText = "Faol kuzatishda (3m oldin)",
                batteryLevel = 84,
                isOnline = true,
                avatarEmoji = "👦"
            ),
            MapUser(
                id = "dost",
                name = "Malika Toirova (Do'st)",
                type = "Do'st",
                originalLat = 41.3050,
                originalLng = 69.2300,
                currentLat = 41.3050,
                currentLng = 69.2300,
                locationName = "Toshkent City Park",
                durationText = "Geolokatsiya ulashilgan",
                batteryLevel = 92,
                isOnline = true,
                avatarEmoji = "👩"
            ),
            MapUser(
                id = "xodim",
                name = "Otabek Solihov (Xodim)",
                type = "Xodim",
                originalLat = 41.3150,
                originalLng = 69.2200,
                currentLat = 41.3150,
                currentLng = 69.2200,
                locationName = "Chilonzor Kvartal",
                durationText = "Ish rejimida",
                batteryLevel = 45,
                isOnline = true,
                avatarEmoji = "🚴"
            )
        )

        val defaultChats = listOf(
            ChatThread(
                id = "v_announcements",
                title = "E'lonlar & Yangiliklar",
                isChannel = true,
                lastMessage = "VEYO global mesh daxlsiz tarmoq sinovi muvaffaqiyatli yakunlandi.",
                iconEmoji = "📢",
                messages = listOf(
                    ChatMessage("Tizim", "Guruh yaratildi", "12:15", isSystem = true),
                    ChatMessage("Admin", "Assalomu alaykum, VEYO daxlsizlik tarmoq e'lonlar markaziga xush kelibsiz!", "12:18"),
                    ChatMessage("Admin", "VEYO global mesh daxlsiz tarmoq sinovi muvaffaqiyatli yakunlandi.", "12:20")
                )
            ),
            ChatThread(
                id = "group_family",
                title = "Oilaviy Xavfsizlik",
                isChannel = false,
                lastMessage = "Sardorbek Jo'rayev: Darslar tugadi, uyga qaytyapman",
                iconEmoji = "🏡",
                isGroup = true,
                members = listOf("Bahodir Jo'rayev (Siz)", "Sardorbek Jo'rayev (Farzand)"),
                linkedEventName = "Maktabdan qaytish",
                linkedLocationName = "9-maktab binosi",
                linkedLat = 41.3210,
                linkedLng = 69.2550,
                linkedEventDesc = "Sardorbekning darsdan keyin xavfsiz marshrut bo'yicha uyga qaytishini koordinatsiya qilish.",
                messages = listOf(
                    ChatMessage("Tizim", "Oila guruhi tuzildi. Voqea joylashuvi: 9-maktab", "12:15", isSystem = true),
                    ChatMessage("Bahodir Jo'rayev (Siz)", "Sardor, darslar tugadimi? Batareyani tekshir.", "12:22"),
                    ChatMessage("Sardorbek Jo'rayev (Farzand)", "Ha darslar tugadi, uyga qaytyapman", "12:24")
                )
            ),
            ChatThread(
                id = "group_cyber",
                title = "Daxlsiz Veyo Team",
                isChannel = false,
                lastMessage = "Malika Toirova: Sintezator audio datchigi faol holatda.",
                iconEmoji = "🛡️",
                isGroup = true,
                members = listOf("Bahodir Jo'rayev (Siz)", "Malika Toirova (Do'st)", "Otabek Solihov (Xodim)"),
                linkedEventName = "Tech Meetup",
                linkedLocationName = "Toshkent City Cafe",
                linkedLat = 41.3050,
                linkedLng = 69.2300,
                linkedEventDesc = "Oflayn xavfsizlik protokollarini o'rganish va sinab ko'rish uchun texnik uchrashuv.",
                messages = listOf(
                    ChatMessage("Tizim", "Yangi peer guruh yaratildi. Voqea joylashuvi: Toshkent City Cafe", "12:16", isSystem = true),
                    ChatMessage("Otabek Solihov (Xodim)", "Oflayn peer mesh ulagichlari sozlandi.", "12:20"),
                    ChatMessage("Malika Toirova (Do'st)", "Sintezator audio datchigi faol holatda.", "12:22")
                )
            )
        )

        _uiState.update {
            it.copy(
                users = initialUsers,
                chatThreads = defaultChats,
                disconnectRequests = emptyList(),
                trackingRequests = emptyList(),
                simulationLogs = listOf(
                    TripleShieldLog("12:20:01", "Mesh Network", "Mesh daemon faollashtirildi. Atrofdagi tugunlar qidirilmoqda...", "INFO"),
                    TripleShieldLog("12:20:15", "SMS Gateway", "Failsafe SMS transport mexanizmi tayyor holatda.", "INFO")
                )
            )
        }
    }

    fun performGoogleSignIn(name: String, email: String) {
        _uiState.update { state ->
            state.copy(
                isLoggedIn = true,
                username = name,
                userEmail = email,
                users = state.users.map { u ->
                    if (u.id == "me") u.copy(name = "$name (Siz)") else u
                }
            )
        }
    }

    fun logout() {
        _uiState.update { state ->
            state.copy(
                isLoggedIn = false
            )
        }
    }

    fun addNewTrackedUser(name: String, email: String, typeString: String = "Do'st") {
        val randomOffsetLat = (Random.nextDouble() - 0.5) * 0.04
        val randomOffsetLng = (Random.nextDouble() - 0.5) * 0.04
        val newUser = MapUser(
            id = "user_${System.currentTimeMillis()}",
            name = "$name ($typeString)",
            type = typeString,
            originalLat = 41.311081 + randomOffsetLat,
            originalLng = 69.240562 + randomOffsetLng,
            currentLat = 41.311081 + randomOffsetLat,
            currentLng = 69.240562 + randomOffsetLng,
            locationName = "Toshkent City yaqinida",
            durationText = "Onlayn kuzatuvda (Hozir)",
            batteryLevel = Random.nextInt(75, 100),
            isOnline = true,
            avatarEmoji = listOf("👦", "👩", "👨", "🌟").random()
        )
        // Auto add a private chat thread with this added friend so they can chat together as well
        val newChat = ChatThread(
            id = newUser.id,
            title = newUser.name,
            isChannel = false,
            lastMessage = "Muloqot boshlandi. Xavfsiz geolokatsiya ulashildi.",
            iconEmoji = newUser.avatarEmoji,
            messages = listOf(
                ChatMessage("Tizim", "Daxlsiz aloqa va geokokatsiya faollashtirildi.", "12:30", isSystem = true),
                ChatMessage(newUser.name, "Salom! Men ham Veyo tarmog'iga ulandim. Meni xaritadan bemalol kuzatishing mumkin!", "12:32")
            )
        )
        _uiState.update { state ->
            state.copy(
                users = state.users + newUser,
                chatThreads = state.chatThreads + newChat,
                trackingCount = state.trackingCount + 1
            )
        }
    }

    fun updateMyLocation(lat: Double, lng: Double, locationName: String) {
        _uiState.update { state ->
            state.copy(
                users = state.users.map { u ->
                    if (u.id == "me") {
                        u.copy(
                            currentLat = lat,
                            currentLng = lng,
                            locationName = locationName
                        )
                    } else {
                        u
                    }
                }
            )
        }
    }

    fun createChatThread(title: String, isChannel: Boolean, iconEmoji: String) {
        val newId = "chat_${System.currentTimeMillis()}"
        val newThread = ChatThread(
            id = newId,
            title = title,
            isChannel = isChannel,
            lastMessage = if (isChannel) "Kanal yaratildi" else "Guruh yaratildi",
            iconEmoji = iconEmoji,
            messages = emptyList(),
            activeLiveStream = false
        )
        _uiState.update { state ->
            state.copy(chatThreads = state.chatThreads + newThread, activeChatId = newId)
        }
    }

    fun addTrackableUser(name: String, type: String, emoji: String, lat: Double, lng: Double) {
        val newId = "user_${System.currentTimeMillis()}"
        val newUser = MapUser(
            id = newId,
            name = name,
            type = type,
            originalLat = lat,
            originalLng = lng,
            currentLat = lat,
            currentLng = lng,
            locationName = "Toshkent",
            durationText = "Hozirgina qo'shildi",
            batteryLevel = 100,
            avatarEmoji = emoji,
            isOnline = true
        )
        _uiState.update { state ->
            state.copy(
                users = state.users + newUser,
                trackingCount = state.trackingCount + 1
            )
        }
    }

    private fun startRealTimeSimulation() {
        viewModelScope.launch {
            while (true) {
                delay(2000) // Har 2 soniyada ozgina joylashuvlarni siljitamiz va chat yozishlarini simulyatsiya qilamiz
                
                // 1. Koordinatalarni minimal darajada o'zgartiramiz (Simulated motion)
                _uiState.update { currentState ->
                    val updatedUsers = currentState.users.map { user ->
                        if (user.id == "me") {
                            user // O'zimiz o'zgarmas qolamiz
                        } else {
                            val latDelta = (Random.nextDouble() - 0.5) * 0.0003
                            val lngDelta = (Random.nextDouble() - 0.5) * 0.0003
                            
                            // Agar power o'chirilgan bo'lsa, xodimlarning aloqasi real vaqtda mesh orqali keladi
                            val newBattery = if (currentState.safetyMode == SafetyMode.POWER_OFF && user.id == "farzand") {
                                0 // Batareya o'chdi!
                            } else {
                                (user.batteryLevel + if (Random.nextBoolean()) 1 else -1).coerceIn(1, 100)
                            }
                            
                            user.copy(
                                currentLat = user.currentLat + latDelta,
                                currentLng = user.currentLng + lngDelta,
                                batteryLevel = newBattery
                            )
                        }
                    }
                    
                    // Agar live efir ochiq bo'lsa, unga yangi xabarlar qo'shib boramiz
                    val updatedChats = currentState.chatThreads.map { thread ->
                        if (thread.activeLiveStream && Random.nextInt(10) > 6) {
                            val commentators = listOf("Sardorbek", "Jamshid", "Malika", "Ziyoda", "Umid", "Abror", "Dilnoza")
                            val uzbekFeedback = listOf(
                                "Wow! VEYO haqiqatdan ham foydali dastur ekan!",
                                "Zor, men ham ota-onamga o'rnatib beraman.",
                                "Ertaga jonli efir bo'ladimi yana?",
                                "Biznes plan daxshat, investorlar buni olishadi aniq!",
                                "Oflayn kuzatish qanday ishlashiga gap yo'q",
                                "Mesh tarmoq qoyilmaqom g'oya!",
                                "O'zbekistonda bunaqasi hali bo'lmagan daxshat!",
                                "Disconnect so'rash madaniyati judayam to'g'ri qo'shilgan!"
                            )
                            val newMsg = ChatMessage(
                                sender = commentators.random(),
                                text = uzbekFeedback.random(),
                                time = "12:24"
                            )
                            thread.copy(
                                messages = thread.messages + newMsg,
                                lastMessage = "${newMsg.sender}: ${newMsg.text}"
                            )
                        } else {
                            thread
                        }
                    }

                    currentState.copy(
                        users = updatedUsers,
                        chatThreads = updatedChats
                    )
                }
            }
        }
    }

    // Interactive custom user state modifiers:
    fun selectUserForDetails(user: MapUser?) {
        _uiState.update { it.copy(selectedUserForDetails = user) }
    }

    fun handleSearch(query: String) {
        val sampleDatabase = listOf("Sardorbek Nazarov", "Malika Toirova", "Akmal Rustamov", "Ali Ergashev", "Jasurbek Solihov", "Gulnoza Rahimova", "Dilshod Karimov")
        val results = if (query.isEmpty()) {
            emptyList()
        } else {
            sampleDatabase.filter { it.contains(query, ignoreCase = true) }
        }
        _uiState.update { it.copy(searchResults = results) }
    }

    fun sendTrackingRequest(name: String) {
        val newReq = TrackingRequest("t_${System.currentTimeMillis()}", name, "OUTGOING")
        _uiState.update {
            it.copy(
                trackingRequests = it.trackingRequests + newReq,
                searchResults = emptyList()
            )
        }
    }

    fun acceptIncomingRequest(reqId: String) {
        _uiState.update { state ->
            val updated = state.trackingRequests.map {
                if (it.id == reqId) it.copy(status = "ACCEPTED") else it
            }
            state.copy(
                trackingRequests = updated,
                trackingCount = state.trackingCount + 1
            )
        }
    }

    fun declineRequest(reqId: String) {
        _uiState.update { state ->
            state.copy(
                trackingRequests = state.trackingRequests.filterNot { it.id == reqId }
            )
        }
    }

    // Disconnect requests management (Approve/Deny)
    fun approveDisconnect(reqId: String) {
        _uiState.update { state ->
            val updated = state.disconnectRequests.map {
                if (it.id == reqId) {
                    // Update user online status
                    val reqName = it.requesterName.split(" ")[0].lowercase()
                    _uiState.update { s ->
                        s.copy(users = s.users.map { u ->
                            if (u.id.contains(reqName)) u.copy(isOnline = false, locationName = "Offline (Ruxsat etilgan)") else u
                        })
                    }
                    it.copy(status = "APPROVED")
                } else it
            }
            state.copy(disconnectRequests = updated)
        }
    }

    fun denyDisconnect(reqId: String) {
        _uiState.update { state ->
            val updated = state.disconnectRequests.map {
                if (it.id == reqId) it.copy(status = "DENIED") else it
            }
            state.copy(disconnectRequests = updated)
        }
    }

    // Trigger user-originated disconnect request simulation
    fun childRequestDisconnect(reason: String) {
        val newReq = DisconnectRequest(
            id = "req_${System.currentTimeMillis()}",
            requesterName = "Sardorbek (Farzand)",
            requesterType = "Farzand",
            reason = reason,
            time = "12:25"
        )
        _uiState.update {
            it.copy(disconnectRequests = it.disconnectRequests + newReq)
        }
    }

    // Toggle simulated offline states to demonstrate Triple Shield Failsafe
    fun setSafetyMode(mode: SafetyMode) {
        _uiState.update { state ->
            val newLogs = state.simulationLogs.toMutableList()
            val timestamp = "12:26:${Random.nextInt(10, 59)}"
            
            when (mode) {
                SafetyMode.NORMAL -> {
                    newLogs.add(0, TripleShieldLog(timestamp, "Shield System", "Platforma normal rejimga qaytdi. GPS, Wi-Fi faol.", "INFO"))
                }
                SafetyMode.OFFLINE -> {
                    newLogs.add(0, TripleShieldLog(timestamp, "Shield Active", "INTERNET UZILDI! Failsafe rejimi faollashmoqda...", "WARN"))
                    newLogs.add(0, TripleShieldLog(timestamp, "Mesh Network", "Yaqin-atrofdagi Apple/Google/Veyo tugunlari skanerlandi: 4 ta bepul IoT ob'ekti topildi.", "INFO"))
                    newLogs.add(0, TripleShieldLog(timestamp, "Mesh Network", "Jo'natilgan Mesh paketlari o'lchami: 110 byte shifrlangan telemetry.", "INFO"))
                    newLogs.add(0, TripleShieldLog(timestamp, "SMS Fallback", "SMS Gateway ga aloqa paketi yuborildi. Koordinatalar: 41.3142,69.2483", "INFO"))
                }
                SafetyMode.POWER_OFF -> {
                    newLogs.add(0, TripleShieldLog(timestamp, "Shield Critical", "TELEFON O'CHIRILDI / BATAREYA %1! SOS faollashdi.", "CRITICAL"))
                    newLogs.add(0, TripleShieldLog(timestamp, "Blackbox", "Oxirgi 30 soniyalik audio yozildi. Shovqin: 'Ali maktab hovlisida...'", "WARN"))
                    newLogs.add(0, TripleShieldLog(timestamp, "Blackbox", "Atrof-muhit kameralaridan 10 ta tezkor rasm bulutga yuklandi.", "INFO"))
                    newLogs.add(0, TripleShieldLog(timestamp, "Blackbox", "Favqulodda telemetry portlatma paket (Failsafe SMS) serverga muvaffaqiyatli uzatildi.", "INFO"))
                }
            }

            // Update user tracking status list inside UI state
            val updatedUsers = state.users.map { user ->
                if (user.id == "farzand") {
                    when (mode) {
                        SafetyMode.NORMAL -> user.copy(isOnline = true, locationName = "9-maktabda")
                        SafetyMode.OFFLINE -> user.copy(isOnline = true, locationName = "9-maktab (Internet yo'q, Mesh orqali kuzatilmoqda)")
                        SafetyMode.POWER_OFF -> user.copy(isOnline = true, locationName = "Maktab yaqinida (Telefon o'chgan, Blackbox faol)")
                    }
                } else {
                    user
                }
            }

            state.copy(
                safetyMode = mode,
                simulationLogs = newLogs,
                users = updatedUsers,
                isRecordingBlackbox = (mode == SafetyMode.POWER_OFF)
            )
        }
    }

    // Send a message in chosen chat
    fun sendMessage(threadId: String, text: String) {
        if (text.isEmpty()) return
        _uiState.update { state ->
            val updatedThreads = state.chatThreads.map { thread ->
                if (thread.id == threadId) {
                    val newMsg = ChatMessage("Siz (Admin)", text, "12:25")
                    thread.copy(
                        messages = thread.messages + newMsg,
                        lastMessage = "Siz: $text"
                    )
                } else {
                    thread
                }
            }
            state.copy(chatThreads = updatedThreads)
        }
    }

    fun selectChat(id: String?) {
        _uiState.update { it.copy(activeChatId = id) }
    }

    fun updateProfile(bio: String, avatarType: String, emoji: String, colorHex: String, frameStyle: String) {
        _uiState.update { state ->
            val updatedUsers = state.users.map { u ->
                if (u.id == "me") {
                    u.copy(
                        avatarEmoji = if (avatarType == "emoji") emoji else "👤"
                    )
                } else u
            }
            state.copy(
                profileBio = bio,
                avatarType = avatarType,
                avatarEmoji = emoji,
                avatarBgColorHex = colorHex,
                avatarFrameStyle = frameStyle,
                users = updatedUsers
            )
        }
    }

    fun updatePrivacySettings(locationVis: String, activityVis: String) {
        _uiState.update { state ->
            state.copy(
                privacyLocationVisibility = locationVis,
                privacyActivityVisibility = activityVis
            )
        }
    }

    fun createGroupChat(
        title: String,
        iconEmoji: String,
        members: List<String>,
        eventName: String? = null,
        locationName: String? = null,
        lat: Double? = null,
        lng: Double? = null,
        eventDesc: String? = null
    ) {
        val newId = "group_${System.currentTimeMillis()}"
        val lastMsg = if (!eventName.isNullOrBlank()) "Yangi voqea bog'landi: $eventName" else "Guruh yaratildi"
        val systemMsg = if (!eventName.isNullOrBlank()) {
            ChatMessage("Tizim", "Guruh yaratildi va '$eventName' joylashuviga muvaffaqiyatli bog'landi.", "Hozir", isSystem = true)
        } else {
            ChatMessage("Tizim", "Guruh yaratildi. A'zolar: ${members.joinToString()}", "Hozir", isSystem = true)
        }
        val newThread = ChatThread(
            id = newId,
            title = title,
            isChannel = false,
            lastMessage = lastMsg,
            iconEmoji = iconEmoji,
            messages = listOf(systemMsg),
            activeLiveStream = false,
            isGroup = true,
            members = members,
            linkedEventName = if (eventName.isNullOrBlank()) null else eventName,
            linkedLocationName = if (locationName.isNullOrBlank()) null else locationName,
            linkedLat = lat,
            linkedLng = lng,
            linkedEventDesc = if (eventDesc.isNullOrBlank()) null else eventDesc
        )
        _uiState.update { state ->
            state.copy(
                chatThreads = state.chatThreads + newThread,
                activeChatId = newId
            )
        }
    }
}
