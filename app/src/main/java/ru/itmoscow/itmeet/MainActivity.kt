package ru.itmoscow.itmeet

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Цветовая палитра IT.МИТ
private val BgDark = Color(0xFF070D19)
private val CardBg = Color(0xCC11192B)
private val InputBg = Color(0xBF070D19)
private val AccentBlue = Color(0xFF5B82FF)
private val AccentBlueDark = Color(0xFF3D63E6)
private val AccentSoft = Color(0xFF7EA0FF)
private val PurpleGlow = Color(0x73582B91)
private val TealGlow = Color(0x66105652)
private val OnlineGreen = Color(0xFF22C55E)
private val OnlineTextGreen = Color(0xFF4ADE80)
private val OfflineGray = Color(0xFF64748B)
private val DangerRed = Color(0xFFFF6B6B)
private val BorderSubtle = Color(0x1FFFFFFF)

// Модель пользователя (единая для поиска, авторизации и админки)
class UserAccount(
    val id: Int,
    val email: String,
    val password: String = "123456",
    val isAdmin: Boolean = false,
    initialName: String,
    initialGroup: String,
    initialRole: String,
    initialSuperpower: String,
    initialBio: String,
    initialSkills: List<String>,
    val online: Boolean = true,
    val statusText: String = "В сети"
) {
    var name by mutableStateOf(initialName)
    var group by mutableStateOf(initialGroup)
    var role by mutableStateOf(initialRole)
    var superpower by mutableStateOf(initialSuperpower)
    var bio by mutableStateOf(initialBio)
    var skills by mutableStateOf(initialSkills)
    var avatarUri by mutableStateOf<Uri?>(null)
    var isBlocked by mutableStateOf(false)
}

// Входящий отклик (лайк, ожидающий ответа)
data class LikeRequest(
    val id: Int,
    val fromUserId: Int,
    val toUserId: Int
)

// Сообщение в чате
data class ChatMessage(
    val senderId: Int,
    val text: String
)

// Общий чат между двумя пользователями (только реальные сообщения от участников)
class SharedChat(
    val id: Int,
    val user1Id: Int,
    val user2Id: Int,
    initialMessages: List<ChatMessage> = emptyList()
) {
    val messages = mutableStateListOf<ChatMessage>().apply { addAll(initialMessages) }

    fun involves(userId: Int): Boolean = user1Id == userId || user2Id == userId
    fun peerIdFor(userId: Int): Int = if (user1Id == userId) user2Id else user1Id
}

// Жалоба для администратора
data class UserReport(
    val id: Int,
    val reporterName: String,
    val reportedUserId: Int,
    val reportedUserName: String,
    val comment: String,
    var resolved: Boolean = false
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ItMeetApp()
        }
    }
}

@Composable
fun ItMeetApp() {
    val scope = rememberCoroutineScope()

    // Общая база всех пользователей приложения
    val users = remember {
        mutableStateListOf(
            UserAccount(
                id = 1,
                email = "veronika.2ip@it.moscow",
                initialName = "Вероника, 17",
                initialGroup = "2 курс • Группа 2ип-7-25",
                initialRole = "UI/UX Дизайнер & Frontend",
                initialSuperpower = "Собираю кликабельные прототипы в Figma за вечер",
                initialBio = "Учусь в IT.МОСКВА, ищу команду для пет-проектов",
                initialSkills = listOf("Figma", "UI/UX", "HTML/CSS", "Prototyping"),
                online = true,
                statusText = "В сети"
            ),
            UserAccount(
                id = 2,
                email = "alina.3is@it.moscow",
                initialName = "Алина, 18",
                initialGroup = "3 курс • Группа 3ис-2-24",
                initialRole = "Frontend-разработчик",
                initialSuperpower = "Поднимаю фронтенд-архитектуру за пару часов",
                initialBio = "Верстаю адаптивные интерфейсы на React и TypeScript без лишних костылей.",
                initialSkills = listOf("React", "TypeScript", "CSS3", "Git"),
                online = true,
                statusText = "В сети"
            ),
            UserAccount(
                id = 3,
                email = "maxim.3ip@it.moscow",
                initialName = "Максим, 18",
                initialGroup = "3 курс • Группа 3ип-4-24",
                initialRole = "Backend & Базы данных",
                initialSuperpower = "Оптимизирую медленные SQL-запросы за 10 минут",
                initialBio = "Проектирую REST API на Python (FastAPI) и PostgreSQL, настраиваю Docker.",
                initialSkills = listOf("Python", "FastAPI", "PostgreSQL", "Docker"),
                online = false,
                statusText = "Был в сети 15 мин назад"
            ),
            UserAccount(
                id = 4,
                email = "daria.2ip@it.moscow",
                initialName = "Дарья, 17",
                initialGroup = "2 курс • Группа 2ип-5-25",
                initialRole = "Product & Питчинг",
                initialSuperpower = "Умею уложить суть сложного IT-продукта в 3 минуты питча",
                initialBio = "Готовлю презентации, считаю экономику проекта и защищаю решение перед жюри.",
                initialSkills = listOf("Figma", "CustDev", "Презентации", "Аналитика"),
                online = true,
                statusText = "В сети"
            ),
            UserAccount(
                id = 5,
                email = "artem.4sa@it.moscow",
                initialName = "Артём, 19",
                initialGroup = "4 курс • Группа 4са-1-23",
                initialRole = "Mobile & DevOps",
                initialSuperpower = "Собираю релизный билд под Android и Web без единого падения",
                initialBio = "Делаю кроссплатформенные приложения на Flutter и настраиваю CI/CD деплой.",
                initialSkills = listOf("Flutter", "Dart", "Linux", "Nginx"),
                online = false,
                statusText = "Был в сети 42 мин назад"
            ),
            UserAccount(
                id = 999,
                email = "admin@it.moscow",
                isAdmin = true,
                initialName = "Администратор IT.МОСКВА",
                initialGroup = "Модерация и безопасность",
                initialRole = "Главный модератор",
                initialSuperpower = "Поддерживаю порядок и безопасность в экосистеме",
                initialBio = "Слежу за соблюдением правил сообщества IT.МИТ.",
                initialSkills = listOf("Модерация", "Безопасность"),
                online = true,
                statusText = "В сети"
            )
        )
    }

    // Текущий авторизованный пользователь (null = экран входа)
    var currentUserId by remember { mutableStateOf<Int?>(null) }
    val currentUser = users.find { it.id == currentUserId }

    var currentTab by remember { mutableStateOf("search") }
    var activeChatId by remember { mutableStateOf<Int?>(null) }
    var showPolicyModal by remember { mutableStateOf(false) }

    // Просмотренные карточки для каждого пользователя
    val viewedMap = remember { mutableStateMapOf<Int, Set<Int>>() }

    // Очередь откликов (лайков)
    val likeRequests = remember {
        mutableStateListOf(
            LikeRequest(id = 1, fromUserId = 3, toUserId = 1)
        )
    }

    // Общие чаты
    val chats = remember { mutableStateListOf<SharedChat>() }

    // Жалобы пользователей
    val reports = remember { mutableStateListOf<UserReport>() }

    // Toast уведомления
    var toastText by remember { mutableStateOf<String?>(null) }
    fun showToast(msg: String) {
        scope.launch {
            toastText = msg
            delay(2500)
            if (toastText == msg) toastText = null
        }
    }

    val backgroundBrush = Brush.radialGradient(
        colors = listOf(PurpleGlow, Color.Transparent),
        center = Offset(900f, 180f),
        radius = 650f
    )
    val secondaryBrush = Brush.radialGradient(
        colors = listOf(TealGlow, Color.Transparent),
        center = Offset(180f, 1300f),
        radius = 750f
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BgDark)
            .background(backgroundBrush)
            .background(secondaryBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            AppHeader(currentUser = currentUser)

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                if (currentUser == null) {
                    AuthScreen(
                        users = users,
                        onOpenPolicy = { showPolicyModal = true },
                        onLoginSuccess = { user ->
                            currentUserId = user.id
                            activeChatId = null
                            currentTab = if (user.isAdmin) "admin" else "search"
                        },
                        onRegisterSuccess = { name, group, email, pass ->
                            val nextId = (users.maxOfOrNull { it.id } ?: 10) + 1
                            val newUser = UserAccount(
                                id = nextId,
                                email = email.ifBlank { "student$nextId@it.moscow" },
                                password = pass.ifBlank { "123456" },
                                initialName = name.ifBlank { "Студент #$nextId" },
                                initialGroup = group.ifBlank { "1 курс • IT.МОСКВА" },
                                initialRole = "Разработчик / Дизайнер",
                                initialSuperpower = "Быстро учусь и подключаюсь к новым задачам",
                                initialBio = "Новый участник IT.МИТ, ищу команду на хакатоны и пет-проекты!",
                                initialSkills = listOf("Git", "Teamwork", "IT.МОСКВА"),
                                online = true,
                                statusText = "В сети"
                            )
                            users.add(newUser)
                            currentUserId = newUser.id
                            activeChatId = null
                            currentTab = "search"
                            showToast("Анкета создана и добавлена в ленту поиска!")
                        }
                    )
                } else {
                    when (currentTab) {
                        "search" -> {
                            val viewedIds = viewedMap[currentUser.id].orEmpty()
                            val availableCandidates = users.filter { candidate ->
                                candidate.id != currentUser.id &&
                                        !candidate.isAdmin &&
                                        !candidate.isBlocked &&
                                        candidate.id !in viewedIds
                            }

                            SearchScreen(
                                candidates = availableCandidates,
                                onDislike = { candidate ->
                                    viewedMap[currentUser.id] = viewedIds + candidate.id
                                },
                                onLike = { candidate ->
                                    viewedMap[currentUser.id] = viewedIds + candidate.id

                                    val reverseRequest = likeRequests.find {
                                        it.fromUserId == candidate.id && it.toUserId == currentUser.id
                                    }
                                    val existingChat = chats.find {
                                        it.involves(currentUser.id) && it.involves(candidate.id)
                                    }

                                    if (existingChat != null) {
                                        showToast("У вас уже есть активный чат с ${candidate.name.substringBefore(",")}")
                                    } else if (reverseRequest != null) {
                                        likeRequests.remove(reverseRequest)
                                        val nextChatId = (chats.maxOfOrNull { it.id } ?: 0) + 1
                                        chats.add(
                                            0,
                                            SharedChat(
                                                id = nextChatId,
                                                user1Id = candidate.id,
                                                user2Id = currentUser.id
                                            )
                                        )
                                        showToast("Взаимный отклик! Чат с ${candidate.name.substringBefore(",")} создан")
                                    } else {
                                        val alreadySent = likeRequests.any {
                                            it.fromUserId == currentUser.id && it.toUserId == candidate.id
                                        }
                                        if (!alreadySent) {
                                            val nextReqId = (likeRequests.maxOfOrNull { it.id } ?: 0) + 1
                                            likeRequests.add(
                                                LikeRequest(
                                                    id = nextReqId,
                                                    fromUserId = currentUser.id,
                                                    toUserId = candidate.id
                                                )
                                            )
                                        }
                                        showToast("Отклик отправлен ${candidate.name.substringBefore(",")}! Чат откроется после ответа")
                                    }
                                },
                                onReset = {
                                    viewedMap[currentUser.id] = emptySet()
                                }
                            )
                        }

                        "chats" -> {
                            val selectedChat = chats.find { it.id == activeChatId }
                            if (selectedChat == null) {
                                val incomingRequests = likeRequests.filter { req ->
                                    req.toUserId == currentUser.id &&
                                            users.any { it.id == req.fromUserId && !it.isBlocked }
                                }
                                val myChats = chats.filter { ch ->
                                    ch.involves(currentUser.id) &&
                                            users.any { it.id == ch.peerIdFor(currentUser.id) && !it.isBlocked }
                                }

                                ChatsAndRequestsScreen(
                                    currentUserId = currentUser.id,
                                    incomingRequests = incomingRequests,
                                    chats = myChats,
                                    users = users,
                                    onAcceptRequest = { req ->
                                        likeRequests.remove(req)
                                        val sender = users.find { it.id == req.fromUserId }
                                        if (sender != null) {
                                            val exists = chats.any {
                                                it.involves(req.fromUserId) && it.involves(req.toUserId)
                                            }
                                            if (!exists) {
                                                val nextChatId = (chats.maxOfOrNull { it.id } ?: 0) + 1
                                                chats.add(
                                                    0,
                                                    SharedChat(
                                                        id = nextChatId,
                                                        user1Id = req.fromUserId,
                                                        user2Id = req.toUserId
                                                    )
                                                )
                                            }
                                            showToast("Отклик принят! Чат с ${sender.name.substringBefore(",")} создан")
                                        }
                                    },
                                    onDeclineRequest = { req ->
                                        likeRequests.remove(req)
                                        showToast("Отклик отклонён, чат не создан")
                                    },
                                    onOpenChat = { activeChatId = it }
                                )
                            } else {
                                val peerId = selectedChat.peerIdFor(currentUser.id)
                                val peer = users.find { it.id == peerId }
                                if (peer != null) {
                                    DialogScreen(
                                        currentUser = currentUser,
                                        peer = peer,
                                        chat = selectedChat,
                                        onBack = { activeChatId = null },
                                        onSubmitReport = { comment ->
                                            val nextRepId = (reports.maxOfOrNull { it.id } ?: 0) + 1
                                            reports.add(
                                                0,
                                                UserReport(
                                                    id = nextRepId,
                                                    reporterName = currentUser.name,
                                                    reportedUserId = peer.id,
                                                    reportedUserName = peer.name,
                                                    comment = comment
                                                )
                                            )
                                            showToast("Жалоба с комментарием отправлена администратору")
                                        },
                                        onSendMessage = { text ->
                                            selectedChat.messages.add(ChatMessage(currentUser.id, text))
                                        }
                                    )
                                }
                            }
                        }

                        "profile" -> ProfileScreen(
                            user = currentUser,
                            onAvatarChange = {
                                currentUser.avatarUri = it
                                showToast("Фото профиля обновлено")
                            },
                            onSave = { n, g, r, s, b ->
                                currentUser.name = n.ifBlank { currentUser.name }
                                currentUser.group = g.ifBlank { currentUser.group }
                                currentUser.role = r
                                currentUser.superpower = s
                                currentUser.bio = b
                                showToast("Изменения профиля сохранены")
                            },
                            onLogout = {
                                activeChatId = null
                                currentUserId = null
                            }
                        )

                        "admin" -> AdminPanelScreen(
                            users = users,
                            reports = reports,
                            onToggleBlockUser = { targetUser ->
                                targetUser.isBlocked = !targetUser.isBlocked
                                val stateText = if (targetUser.isBlocked) "заблокирован" else "разблокирован"
                                showToast("Пользователь ${targetUser.name} $stateText")
                            },
                            onDeleteUser = { targetUser ->
                                users.remove(targetUser)
                                chats.removeAll { it.involves(targetUser.id) }
                                likeRequests.removeAll {
                                    it.fromUserId == targetUser.id || it.toUserId == targetUser.id
                                }
                                showToast("Анкета ${targetUser.name} полностью удалена")
                            },
                            onResolveReport = { report ->
                                val idx = reports.indexOf(report)
                                if (idx != -1) {
                                    reports[idx] = report.copy(resolved = true)
                                    showToast("Жалоба #${report.id} отмечена как рассмотренная")
                                }
                            }
                        )
                    }
                }
            }

            if (currentUser != null) {
                val incomingCount = likeRequests.count { req ->
                    req.toUserId == currentUser.id && users.any { it.id == req.fromUserId && !it.isBlocked }
                }
                val unresolvedReportsCount = reports.count { !it.resolved }

                BottomTabBar(
                    isAdmin = currentUser.isAdmin,
                    currentTab = currentTab,
                    incomingCount = incomingCount,
                    reportsCount = unresolvedReportsCount,
                    onSelectTab = { tab ->
                        currentTab = tab
                        if (tab != "chats") activeChatId = null
                    }
                )
            }
        }

        // Всплывающее уведомление (Toast)
        AnimatedVisibility(
            visible = toastText != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
                .padding(top = 78.dp, start = 16.dp, end = 16.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF1D2B4A),
                border = BorderStroke(1.dp, AccentBlue)
            ) {
                Text(
                    text = toastText.orEmpty(),
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        if (showPolicyModal) {
            PolicyModal(onDismiss = { showPolicyModal = false })
        }
    }
}

@Composable
private fun AppHeader(currentUser: UserAccount?) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 28.dp, bottom = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = AccentBlue)) { append("IT") }
                append(".МИТ")
            },
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            color = Color.White,
            letterSpacing = 0.5.sp
        )
        Text(
            text = buildAnnotatedString {
                append("ЭКОСИСТЕМА ")
                withStyle(SpanStyle(color = AccentBlue, fontStyle = FontStyle.Italic)) { append("IT") }
                append(".МОСКВА")
            },
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.4.sp,
            color = Color.White.copy(alpha = 0.65f)
        )
        if (currentUser != null) {
            Text(
                text = "Вы вошли как: ${currentUser.name}",
                color = AccentSoft,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AuthScreen(
    users: List<UserAccount>,
    onOpenPolicy: () -> Unit,
    onLoginSuccess: (UserAccount) -> Unit,
    onRegisterSuccess: (name: String, group: String, email: String, pass: String) -> Unit
) {
    var mode by remember { mutableStateOf("reg") }
    var name by remember { mutableStateOf("Кирилл, 18") }
    var group by remember { mutableStateOf("2 курс • Группа 2ип-3-25") }
    var email by remember { mutableStateOf("veronika.2ip@it.moscow") }
    var password by remember { mutableStateOf("123456") }
    var policyAccepted by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardBg,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xB3070D19))
                        .padding(4.dp)
                ) {
                    listOf("reg" to "Регистрация", "login" to "Вход").forEach { (key, label) ->
                        val active = mode == key
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (active) AccentBlue else Color.Transparent)
                                .clickable {
                                    mode = key
                                    errorText = ""
                                }
                                .padding(vertical = 9.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (active) Color.White else Color.White.copy(alpha = 0.6f),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                if (mode == "reg") {
                    FieldLabel("ИМЯ И ВОЗРАСТ")
                    DarkTextField(value = name, onValueChange = { name = it })

                    FieldLabel("КУРС И УЧЕБНАЯ ГРУППА")
                    DarkTextField(value = group, onValueChange = { group = it })
                } else {
                    FieldLabel("БЫСТРЫЙ ВХОД (ПЕРЕКЛЮЧЕНИЕ АККАУНТОВ)")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(bottom = 4.dp)
                    ) {
                        users.forEach { acc ->
                            val shortTitle = if (acc.isAdmin) "Админ" else acc.name.substringBefore(",")
                            Surface(
                                onClick = {
                                    email = acc.email
                                    password = acc.password
                                    policyAccepted = true
                                    errorText = ""
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (acc.isAdmin) Color(0x33FF5252) else AccentBlue.copy(alpha = 0.16f),
                                border = BorderStroke(
                                    1.dp,
                                    if (acc.isAdmin) DangerRed.copy(alpha = 0.5f) else AccentBlue.copy(alpha = 0.35f)
                                )
                            ) {
                                Text(
                                    text = shortTitle,
                                    color = if (acc.isAdmin) DangerRed else Color(0xFF93B0FF),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                FieldLabel("УЧЕБНЫЙ EMAIL (IT.МОСКВА)")
                DarkTextField(
                    value = email,
                    onValueChange = { email = it },
                    keyboardType = KeyboardType.Email
                )

                FieldLabel("ПАРОЛЬ")
                DarkTextField(
                    value = password,
                    onValueChange = { password = it },
                    isPassword = true
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Checkbox(
                        checked = policyAccepted,
                        onCheckedChange = {
                            policyAccepted = it
                            if (it) errorText = ""
                        },
                        colors = CheckboxDefaults.colors(
                            checkedColor = AccentBlue,
                            uncheckedColor = Color.White.copy(alpha = 0.5f),
                            checkmarkColor = Color.White
                        ),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = buildAnnotatedString {
                            append("Я принимаю ")
                            withStyle(
                                SpanStyle(
                                    color = AccentBlue,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("Политику конфиденциальности и Правила сообщества")
                            }
                        },
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 12.sp,
                        lineHeight = 16.sp,
                        modifier = Modifier.clickable { onOpenPolicy() }
                    )
                }

                PrimaryGradientButton(
                    text = if (mode == "reg") "Создать анкету и войти" else "Войти в аккаунт",
                    onClick = {
                        if (!policyAccepted) {
                            errorText = "Подтвердите согласие с Политикой конфиденциальности"
                            return@PrimaryGradientButton
                        }
                        if (mode == "reg") {
                            val cleanEmail = email.trim()
                            if (users.any { it.email.equals(cleanEmail, ignoreCase = true) }) {
                                errorText = "Аккаунт с таким Email уже зарегистрирован"
                            } else {
                                errorText = ""
                                onRegisterSuccess(name.trim(), group.trim(), cleanEmail, password)
                            }
                        } else {
                            val found = users.find { it.email.equals(email.trim(), ignoreCase = true) }
                            when {
                                found == null -> errorText = "Пользователь с таким Email не найден"
                                found.isBlocked -> errorText = "Доступ запрещён: аккаунт заблокирован администратором"
                                else -> {
                                    errorText = ""
                                    onLoginSuccess(found)
                                }
                            }
                        }
                    },
                    modifier = Modifier.padding(top = 14.dp)
                )

                if (errorText.isNotEmpty()) {
                    Text(
                        text = errorText,
                        color = DangerRed,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    )
                }
            }
        }

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = AccentBlue.copy(alpha = 0.08f),
            border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.22f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                Text(
                    text = "Безопасная среда IT.МОСКВА",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Платформа предназначена исключительно для поиска тиммейтов на хакатоны, учебные и пет-проекты. Спам и токсичность блокируются модерацией.",
                    color = Color.White.copy(alpha = 0.75f),
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SearchScreen(
    candidates: List<UserAccount>,
    onDislike: (UserAccount) -> Unit,
    onLike: (UserAccount) -> Unit,
    onReset: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (candidates.isEmpty()) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = CardBg,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Вы просмотрели все анкеты",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Перейдите во вкладку «Чаты», чтобы проверить входящие отклики, или обновите ленту заново.",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Center
                    )
                    PrimaryGradientButton(
                        text = "Показать карточки снова",
                        onClick = onReset,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }
        } else {
            val c = candidates.first()
            Surface(
                shape = RoundedCornerShape(26.dp),
                color = CardBg,
                border = BorderStroke(1.dp, BorderSubtle),
                shadowElevation = 16.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = AccentBlue.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.32f))
                        ) {
                            Text(
                                text = c.group,
                                color = Color(0xFF93B0FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                            )
                        }

                        StatusPill(online = c.online, text = c.statusText)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        UserAvatar(user = c, size = 60.dp, fontSize = 22.sp)
                        Column {
                            Text(
                                text = c.name,
                                color = Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = c.role,
                                color = AccentSoft,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0x80070D19),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = c.bio,
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 13.5.sp,
                            lineHeight = 19.sp,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentBlue.copy(alpha = 0.09f))
                            .padding(horizontal = 12.dp, vertical = 9.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(3.dp)
                                .height(34.dp)
                                .background(AccentBlue)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(color = Color(0xFF9EB7FF), fontWeight = FontWeight.Bold)) {
                                    append("Суперсила: ")
                                }
                                withStyle(SpanStyle(color = Color.White.copy(alpha = 0.78f))) {
                                    append(c.superpower)
                                }
                            },
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        verticalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        c.skills.forEach { skill ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.07f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
                            ) {
                                Text(
                                    text = skill,
                                    color = Color(0xFFDBE6FF),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(28.dp, Alignment.CenterHorizontally),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            onClick = { onDislike(c) },
                            shape = CircleShape,
                            color = Color(0x24FF5252),
                            border = BorderStroke(1.dp, Color(0x59FF5252)),
                            modifier = Modifier.size(58.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Пропустить",
                                    tint = DangerRed,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Surface(
                            onClick = { onLike(c) },
                            shape = CircleShape,
                            color = AccentBlue,
                            shadowElevation = 8.dp,
                            modifier = Modifier.size(58.dp)
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.background(
                                    Brush.linearGradient(listOf(AccentBlue, Color(0xFF3A60E5)))
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Отправить отклик",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
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
private fun ChatsAndRequestsScreen(
    currentUserId: Int,
    incomingRequests: List<LikeRequest>,
    chats: List<SharedChat>,
    users: List<UserAccount>,
    onAcceptRequest: (LikeRequest) -> Unit,
    onDeclineRequest: (LikeRequest) -> Unit,
    onOpenChat: (Int) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (incomingRequests.isNotEmpty()) {
            item {
                Text(
                    text = "Входящие отклики (${incomingRequests.size})",
                    color = AccentSoft,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            items(incomingRequests, key = { "req_${it.id}" }) { req ->
                val sender = users.find { it.id == req.fromUserId }
                if (sender != null) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = AccentBlue.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                UserAvatar(user = sender, size = 44.dp, fontSize = 16.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${sender.name} хочет в команду!",
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${sender.role} • ${sender.group}",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 11.5.sp
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onDeclineRequest(req) },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.4f)),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        containerColor = Color(0x1FFF5252),
                                        contentColor = DangerRed
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = "Отказать", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }

                                Button(
                                    onClick = { onAcceptRequest(req) },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = "Ответить", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Text(
                text = "Командные чаты",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(top = if (incomingRequests.isNotEmpty()) 6.dp else 0.dp, bottom = 2.dp)
            )
        }

        if (chats.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CardBg,
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Пока нет активных чатов. Поставьте лайк в разделе «Поиск» и дождитесь ответа, либо примите входящий отклик выше.",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        } else {
            items(chats, key = { "chat_${it.id}" }) { chat ->
                val peerId = chat.peerIdFor(currentUserId)
                val peer = users.find { it.id == peerId }
                if (peer != null) {
                    val lastMsg = chat.messages.lastOrNull()?.text ?: "Чат создан — напишите первым"
                    Surface(
                        onClick = { onOpenChat(chat.id) },
                        shape = RoundedCornerShape(18.dp),
                        color = CardBg,
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.09f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(modifier = Modifier.size(46.dp)) {
                                UserAvatar(user = peer, size = 46.dp, fontSize = 17.sp)
                                Box(
                                    modifier = Modifier
                                        .size(11.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(if (peer.online) OnlineGreen else OfflineGray)
                                        .border(2.dp, Color(0xFF11192B), CircleShape)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = peer.name,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = peer.statusText,
                                        color = if (peer.online) OnlineTextGreen else Color.White.copy(alpha = 0.55f),
                                        fontSize = 10.5.sp
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = lastMsg,
                                    color = Color.White.copy(alpha = 0.65f),
                                    fontSize = 12.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
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
private fun DialogScreen(
    currentUser: UserAccount,
    peer: UserAccount,
    chat: SharedChat,
    onBack: () -> Unit,
    onSubmitReport: (String) -> Unit,
    onSendMessage: (String) -> Unit
) {
    var inputText by remember { mutableStateOf("") }
    var showReportDialog by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(chat.messages.size) {
        if (chat.messages.isNotEmpty()) {
            listState.animateScrollToItem(chat.messages.size - 1)
        }
    }

    fun submitText() {
        val trimmed = inputText.trim()
        if (trimmed.isNotEmpty()) {
            onSendMessage(trimmed)
            inputText = ""
        }
    }

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = CardBg,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onBack() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = AccentSoft,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Назад",
                        color = AccentSoft,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = peer.name,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(if (peer.online) OnlineGreen else OfflineGray)
                        )
                        Text(
                            text = peer.statusText,
                            color = if (peer.online) OnlineTextGreen else Color.White.copy(alpha = 0.6f),
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    onClick = { showReportDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0x24FF5252),
                    border = BorderStroke(1.dp, Color(0x4DFF5252))
                ) {
                    Text(
                        text = "Жалоба",
                        color = DangerRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.08f))

            if (chat.messages.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Сообщений пока нет.\nНапишите первым, чтобы обсудить проект!",
                        color = Color.White.copy(alpha = 0.5f),
                        fontSize = 12.5.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    items(chat.messages) { msg ->
                        if (msg.senderId == currentUser.id) {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = 16.dp,
                                        bottomEnd = 4.dp
                                    ),
                                    color = AccentBlue,
                                    modifier = Modifier.fillMaxWidth(0.82f)
                                ) {
                                    Text(
                                        text = msg.text,
                                        color = Color.White,
                                        fontSize = 12.5.sp,
                                        lineHeight = 17.5.sp,
                                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 16.dp,
                                        bottomStart = 4.dp,
                                        bottomEnd = 16.dp
                                    ),
                                    color = Color.White.copy(alpha = 0.09f),
                                    modifier = Modifier.fillMaxWidth(0.82f)
                                ) {
                                    Text(
                                        text = msg.text,
                                        color = Color.White,
                                        fontSize = 12.5.sp,
                                        lineHeight = 17.5.sp,
                                        modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Написать сообщение...",
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 13.sp
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        submitText()
                        keyboardController?.hide()
                    }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xCC070D19),
                        unfocusedContainerColor = Color(0xCC070D19),
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                        cursorColor = AccentBlue
                    ),
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    onClick = { submitText() },
                    shape = CircleShape,
                    color = AccentBlue,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Отправить",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    if (showReportDialog) {
        ReportModal(
            targetName = peer.name,
            onDismiss = { showReportDialog = false },
            onSendReport = { comment ->
                showReportDialog = false
                onSubmitReport(comment)
            }
        )
    }
}

@Composable
private fun ReportModal(
    targetName: String,
    onDismiss: () -> Unit,
    onSendReport: (String) -> Unit
) {
    var comment by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF11192B),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Пожаловаться на $targetName",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Опишите, какая проблема произошла (спам, оскорбления, фейковый аккаунт или реклама). Обращение уйдёт модераторам.",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                OutlinedTextField(
                    value = comment,
                    onValueChange = {
                        comment = it
                        if (it.isNotBlank()) error = ""
                    },
                    placeholder = {
                        Text(
                            text = "Комментарий к жалобе...",
                            color = Color.White.copy(alpha = 0.45f),
                            fontSize = 13.sp
                        )
                    },
                    minLines = 3,
                    maxLines = 5,
                    shape = RoundedCornerShape(12.dp),
                    textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = InputBg,
                        unfocusedContainerColor = InputBg,
                        focusedBorderColor = DangerRed,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.12f),
                        cursorColor = DangerRed
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (error.isNotEmpty()) {
                    Text(text = error, color = DangerRed, fontSize = 11.5.sp)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Отмена", color = Color.White.copy(alpha = 0.8f), fontSize = 12.5.sp)
                    }

                    Button(
                        onClick = {
                            if (comment.trim().isEmpty()) {
                                error = "Напишите короткий комментарий о проблеме"
                            } else {
                                onSendReport(comment.trim())
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Отправить", color = Color.White, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminPanelScreen(
    users: List<UserAccount>,
    reports: List<UserReport>,
    onToggleBlockUser: (UserAccount) -> Unit,
    onDeleteUser: (UserAccount) -> Unit,
    onResolveReport: (UserReport) -> Unit
) {
    var section by remember { mutableStateOf("reports") }
    val managedUsers = users.filter { !it.isAdmin }

    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Панель администратора",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xB3070D19))
                .padding(4.dp)
        ) {
            listOf(
                "reports" to "Жалобы (${reports.count { !it.resolved }})",
                "users" to "Анкеты (${managedUsers.size})"
            ).forEach { (key, label) ->
                val active = section == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (active) AccentBlue else Color.Transparent)
                        .clickable { section = key }
                        .padding(vertical = 9.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (active) Color.White else Color.White.copy(alpha = 0.6f),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (section == "reports") {
            if (reports.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = CardBg,
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Новых жалоб пока нет. Когда пользователи отправят обращение из чата, оно появится здесь.",
                        color = Color.White.copy(alpha = 0.65f),
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(reports, key = { it.id }) { rep ->
                        val targetAccount = users.find { it.id == rep.reportedUserId }
                        Surface(
                            shape = RoundedCornerShape(18.dp),
                            color = CardBg,
                            border = BorderStroke(
                                1.dp,
                                if (rep.resolved) Color.White.copy(alpha = 0.08f) else DangerRed.copy(alpha = 0.45f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "На кого: ${rep.reportedUserName}",
                                        color = DangerRed,
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (rep.resolved) "Решено" else "Новая",
                                        color = if (rep.resolved) OnlineTextGreen else DangerRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Text(
                                    text = "Отправитель: ${rep.reporterName}",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.5.sp
                                )

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0x80070D19),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "«${rep.comment}»",
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 12.5.sp,
                                        modifier = Modifier.padding(10.dp)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (targetAccount != null) {
                                        OutlinedButton(
                                            onClick = { onToggleBlockUser(targetAccount) },
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.45f)),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = DangerRed),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(
                                                text = if (targetAccount.isBlocked) "Разблокировать" else "Заблокировать",
                                                fontSize = 11.5.sp
                                            )
                                        }
                                    }

                                    if (!rep.resolved) {
                                        Button(
                                            onClick = { onResolveReport(rep) },
                                            shape = RoundedCornerShape(10.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Закрыть жалобу", fontSize = 11.5.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(managedUsers, key = { it.id }) { u ->
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = CardBg,
                        border = BorderStroke(
                            1.dp,
                            if (u.isBlocked) DangerRed.copy(alpha = 0.45f) else BorderSubtle
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                UserAvatar(user = u, size = 44.dp, fontSize = 16.sp)
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = u.name,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        if (u.isBlocked) {
                                            Text(
                                                text = "[ЗАБЛОКИРОВАН]",
                                                color = DangerRed,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${u.group} • ${u.email}",
                                        color = Color.White.copy(alpha = 0.6f),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { onToggleBlockUser(u) },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (u.isBlocked) OnlineGreen.copy(alpha = 0.5f) else DangerRed.copy(alpha = 0.45f)
                                    ),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = if (u.isBlocked) OnlineTextGreen else DangerRed
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (u.isBlocked) Icons.Default.LockOpen else Icons.Default.Block,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (u.isBlocked) "Разрешить доступ" else "Запретить доступ",
                                        fontSize = 11.5.sp
                                    )
                                }

                                Button(
                                    onClick = { onDeleteUser(u) },
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0x33FF5252),
                                        contentColor = DangerRed
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = null,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text("Удалить анкету", fontSize = 11.5.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileScreen(
    user: UserAccount,
    onAvatarChange: (Uri) -> Unit,
    onSave: (String, String, String, String, String) -> Unit,
    onLogout: () -> Unit
) {
    var editName by remember(user.id) { mutableStateOf(user.name) }
    var editGroup by remember(user.id) { mutableStateOf(user.group) }
    var editRole by remember(user.id) { mutableStateOf(user.role) }
    var editSuperpower by remember(user.id) { mutableStateOf(user.superpower) }
    var editBio by remember(user.id) { mutableStateOf(user.bio) }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onAvatarChange(uri)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Мой профиль",
            color = Color.White,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardBg,
            border = BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    UserAvatar(user = user, size = 58.dp, fontSize = 20.sp)

                    Column {
                        Text(
                            text = user.name,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = user.group,
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(5.dp))
                        StatusPill(online = true, text = "В сети")
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            onClick = {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            color = AccentBlue.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.35f))
                        ) {
                            Text(
                                text = "Добавить фото",
                                color = Color(0xFF93B0FF),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                FieldLabel("ИМЯ И ВОЗРАСТ")
                DarkTextField(value = editName, onValueChange = { editName = it })

                FieldLabel("КУРС И ГРУППА")
                DarkTextField(value = editGroup, onValueChange = { editGroup = it })

                FieldLabel("ГЛАВНАЯ РОЛЬ")
                DarkTextField(value = editRole, onValueChange = { editRole = it })

                FieldLabel("СУПЕРСИЛА")
                DarkTextField(value = editSuperpower, onValueChange = { editSuperpower = it })

                FieldLabel("О СЕБЕ")
                DarkTextField(value = editBio, onValueChange = { editBio = it })

                PrimaryGradientButton(
                    text = "Сохранить изменения",
                    onClick = { onSave(editName, editGroup, editRole, editSuperpower, editBio) },
                    modifier = Modifier.padding(top = 14.dp)
                )

                OutlinedButton(
                    onClick = onLogout,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, Color(0x4DFF5252)),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0x1FFF5252),
                        contentColor = DangerRed
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                ) {
                    Text(
                        text = "Выйти / Сменить аккаунт",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomTabBar(
    isAdmin: Boolean,
    currentTab: String,
    incomingCount: Int,
    reportsCount: Int,
    onSelectTab: (String) -> Unit
) {
    val tabs = if (isAdmin) {
        listOf(
            "admin" to if (reportsCount > 0) "Админ ($reportsCount)" else "Админ",
            "search" to "Поиск",
            "profile" to "Профиль"
        )
    } else {
        listOf(
            "search" to "Поиск",
            "chats" to if (incomingCount > 0) "Чаты (+$incomingCount)" else "Чаты",
            "profile" to "Профиль"
        )
    }

    Surface(
        color = Color(0xEB0A101E),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            tabs.forEach { (tab, label) ->
                val active = currentTab == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectTab(tab) }
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (active) AccentBlue else Color.White.copy(alpha = 0.55f),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PolicyModal(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xFF11192B),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Политика конфиденциальности и Правила сообщества IT.МИТ",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = buildAnnotatedString {
                        append("1. ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Назначение платформы: ") }
                        append("сервис «IT.МИТ» создан для студентов колледжа IT.МОСКВА с целью формирования команд для хакатонов, курсовых, дипломных и пет-проектов.")
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp
                )
                Text(
                    text = buildAnnotatedString {
                        append("2. ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Персональные данные: ") }
                        append("указывая имя, учебную группу и стек навыков, пользователь соглашается на отображение анкеты другим студентам внутри образовательной экосистемы.")
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp
                )
                Text(
                    text = buildAnnotatedString {
                        append("3. ")
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("Правила общения: ") }
                        append("запрещены оскорбления, спам и публикация сторонней рекламы. Нарушители блокируются модерацией по жалобе.")
                    },
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp
                )
                PrimaryGradientButton(
                    text = "Понятно, закрыть",
                    onClick = onDismiss,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.55f),
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(top = 10.dp, bottom = 5.dp)
    )
}

@Composable
private fun DarkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        textStyle = TextStyle(color = Color.White, fontSize = 13.sp),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = InputBg,
            unfocusedContainerColor = InputBg,
            focusedBorderColor = AccentBlue,
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            cursorColor = AccentBlue
        ),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun PrimaryGradientButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        contentPadding = PaddingValues(0.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        modifier = modifier
            .fillMaxWidth()
            .height(46.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.linearGradient(listOf(AccentBlue, AccentBlueDark)))
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun StatusPill(online: Boolean, text: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color(0xBF070D19),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(if (online) OnlineGreen else OfflineGray)
            )
            Text(
                text = text,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun UserAvatar(
    user: UserAccount,
    size: Dp,
    fontSize: TextUnit
) {
    if (user.avatarUri != null) {
        AsyncImage(
            model = user.avatarUri,
            contentDescription = "Аватар",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .border(2.dp, AccentBlue, CircleShape)
        )
    } else {
        Box(
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(Color(0xFF273C75), AccentBlue)))
                .border(2.dp, Color.White.copy(alpha = 0.18f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.name.firstOrNull()?.uppercase() ?: "?",
                color = Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}