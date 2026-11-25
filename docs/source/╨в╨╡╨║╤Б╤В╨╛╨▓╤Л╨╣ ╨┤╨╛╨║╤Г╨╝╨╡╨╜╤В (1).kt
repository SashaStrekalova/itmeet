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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
 
// Цветовая палитра из макета IT.МИТ
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
 
data class Candidate(
    val id: Int,
    val name: String,
    val group: String,
    val role: String,
    val online: Boolean,
    val statusText: String,
    val bio: String,
    val superpower: String,
    val skills: List<String>,
    val initialReply: String,
    val replies: List<String>
)
 
data class ChatMessage(
    val sender: String, // "me" | "peer" | "system"
    val text: String
)
 
class ChatThread(
    val id: Int,
    val name: String,
    val online: Boolean,
    val statusText: String,
    val replies: List<String>,
    initialMessages: List<ChatMessage>
) {
    var replyStep by mutableIntStateOf(0)
    val messages = mutableStateListOf<ChatMessage>().apply { addAll(initialMessages) }
}
 
private val defaultCandidates = listOf(
    Candidate(
        id = 1,
        name = "Алина, 18",
        group = "3 курс • Группа 3ис-2-24",
        role = "Frontend-разработчик",
        online = true,
        statusText = "В сети",
        bio = "Верстаю адаптивные интерфейсы на React и TypeScript без лишних костылей.",
        superpower = "Поднимаю фронтенд-архитектуру за пару часов",
        skills = listOf("React", "TypeScript", "CSS3", "Git"),
        initialReply = "Привет! Видела твои прототипы, ищем дизайнера в команду на хакатон.",
        replies = listOf(
            "Отлично! У нас как раз не хватает сильного UI/UX на этап прототипирования.",
            "Давай сегодня вечером созвонимся в Discord и обсудим распределение задач?"
        )
    ),
    Candidate(
        id = 2,
        name = "Максим, 18",
        group = "3 курс • Группа 3ип-4-24",
        role = "Backend & Базы данных",
        online = false,
        statusText = "Был в сети 15 мин назад",
        bio = "Проектирую REST API на Python (FastAPI) и PostgreSQL, настраиваю Docker.",
        superpower = "Оптимизирую медленные SQL-запросы за 10 минут",
        skills = listOf("Python", "FastAPI", "PostgreSQL", "Docker"),
        initialReply = "Здарова! С радостью возьму на себя серверную часть и базу данных.",
        replies = emptyList()
    ),
    Candidate(
        id = 3,
        name = "Дарья, 17",
        group = "2 курс • Группа 2ип-5-25",
        role = "Product & Питчинг",
        online = true,
        statusText = "В сети",
        bio = "Готовлю презентации, считаю экономику проекта и защищаю решение перед жюри.",
        superpower = "Умею уложить суть сложного IT-продукта в 3 минуты питча",
        skills = listOf("Figma", "CustDev", "Презентации", "Аналитика"),
        initialReply = "Привет! Нужен фронтенд и дизайн для сервиса навигации по колледжу.",
        replies = listOf(
            "Супер! Могу скинуть черновик презентации и структуру экранов.",
            "Отлично, тогда фиксируем команду и начинаем собирать MVP!"
        )
    ),
    Candidate(
        id = 4,
        name = "Артём, 19",
        group = "4 курс • Группа 4са-1-23",
        role = "Mobile & DevOps",
        online = false,
        statusText = "Был в сети 42 мин назад",
        bio = "Делаю кроссплатформенные приложения на Flutter и настраиваю CI/CD деплой.",
        superpower = "Собираю релизный билд под Android и Web без единого падения",
        skills = listOf("Flutter", "Dart", "Linux", "Nginx"),
        initialReply = "Привет! Если понадобится мобильная версия проекта — я в деле.",
        replies = emptyList()
    )
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
 
    var isAuthenticated by remember { mutableStateOf(false) }
    var currentTab by remember { mutableStateOf("search") } // "search" | "chats" | "profile"
    var currentIndex by remember { mutableIntStateOf(0) }
    var activeChatId by remember { mutableStateOf<Int?>(null) }
    var showPolicyModal by remember { mutableStateOf(false) }
 
    // Профиль пользователя
    var profName by remember { mutableStateOf("Вероника, 17") }
    var profGroup by remember { mutableStateOf("2 курс • Группа 2ип-7-25") }
    var profRole by remember { mutableStateOf("UI/UX Дизайнер & Frontend") }
    var profSuperpower by remember { mutableStateOf("Собираю кликабельные прототипы в Figma за вечер") }
    var profBio by remember { mutableStateOf("Учусь в IT.МОСКВА, ищу команду для пет-проектов") }
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
 
    // Список чатов
    val chats = remember {
        mutableStateListOf(
            ChatThread(
                id = 1,
                name = "Алина, 18",
                online = true,
                statusText = "В сети",
                replies = defaultCandidates[0].replies,
                initialMessages = listOf(ChatMessage("peer", defaultCandidates[0].initialReply))
            ),
            ChatThread(
                id = 2,
                name = "Максим, 18",
                online = false,
                statusText = "Был в сети 15 мин назад",
                replies = emptyList(),
                initialMessages = listOf(ChatMessage("peer", defaultCandidates[1].initialReply))
            )
        )
    }
 
    // Toast состояние
    var toastText by remember { mutableStateOf<String?>(null) }
    fun showToast(msg: String) {
        scope.launch {
            toastText = msg
            delay(2400)
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
        Column(modifier = Modifier.fillMaxSize()) {
            AppHeader()
 
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                if (!isAuthenticated) {
                    AuthScreen(
                        initialName = profName,
                        initialGroup = profGroup,
                        onOpenPolicy = { showPolicyModal = true },
                        onSuccess = { name, group, isReg ->
                            if (isReg) {
                                profName = name.ifBlank { "Вероника, 17" }
                                profGroup = group.ifBlank { "2 курс • Группа 2ип-7-25" }
                            }
                            currentTab = "search"
                            isAuthenticated = true
                        }
                    )
                } else {
                    when (currentTab) {
                        "search" -> SearchScreen(
                            candidates = defaultCandidates,
                            currentIndex = currentIndex,
                            onSwipeLeft = { currentIndex++ },
                            onSwipeRight = { candidate ->
                                if (chats.none { it.id == candidate.id }) {
                                    chats.add(
                                        0,
                                        ChatThread(
                                            id = candidate.id,
                                            name = candidate.name,
                                            online = candidate.online,
                                            statusText = candidate.statusText,
                                            replies = candidate.replies,
                                            initialMessages = listOf(ChatMessage("peer", candidate.initialReply))
                                        )
                                    )
                                }
                                showToast("Взаимный интерес! Чат с ${candidate.name.substringBefore(",")} создан")
                                currentIndex++
                            },
                            onReset = { currentIndex = 0 }
                        )
 
                        "chats" -> {
                            val selectedChat = chats.find { it.id == activeChatId }
                            if (selectedChat == null) {
                                ChatsListScreen(
                                    chats = chats,
                                    onOpenChat = { activeChatId = it }
                                )
                            } else {
                                DialogScreen(
                                    chat = selectedChat,
                                    onBack = { activeChatId = null },
                                    onReport = { showToast("Жалоба отправлена модераторам IT.МОСКВА") },
                                    onSendMessage = { text ->
                                        selectedChat.messages.add(ChatMessage("me", text))
                                        if (selectedChat.online) {
                                            scope.launch {
                                                delay(650)
                                                val reply = if (selectedChat.replies.isNotEmpty()) {
                                                    selectedChat.replies[selectedChat.replyStep % selectedChat.replies.size]
                                                } else {
                                                    "Принято! Обсудим детали проекта."
                                                }
                                                selectedChat.replyStep++
                                                selectedChat.messages.add(ChatMessage("peer", reply))
                                            }
                                        } else if (selectedChat.messages.none { it.sender == "system" }) {
                                            scope.launch {
                                                delay(350)
                                                selectedChat.messages.add(
                                                    ChatMessage(
                                                        "system",
                                                        "${selectedChat.name.substringBefore(",")} сейчас не в сети (${selectedChat.statusText.lowercase()}). Сообщение доставлено и будет прочитано позже."
                                                    )
                                                )
                                            }
                                        }
                                    }
                                )
                            }
                        }
 
                        "profile" -> ProfileScreen(
                            name = profName,
                            group = profGroup,
                            role = profRole,
                            superpower = profSuperpower,
                            bio = profBio,
                            avatarUri = avatarUri,
                            onAvatarChange = {
                                avatarUri = it
                                showToast("Фото профиля обновлено")
                            },
                            onSave = { n, g, r, s, b ->
                                profName = n.ifBlank { "Вероника, 17" }
                                profGroup = g.ifBlank { "2 курс • Группа 2ип-7-25" }
                                profRole = r
                                profSuperpower = s
                                profBio = b
                                showToast("Изменения профиля сохранены")
                            },
                            onLogout = {
                                activeChatId = null
                                isAuthenticated = false
                            }
                        )
                    }
                }
            }
 
            if (isAuthenticated) {
                BottomTabBar(
                    currentTab = currentTab,
                    onSelectTab = { tab ->
                        currentTab = tab
                        if (tab != "chats") activeChatId = null
                    }
                )
            }
        }
 
        // Toast уведомление
        AnimatedVisibility(
            visible = toastText != null,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 74.dp)
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
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
 
        // Модальное окно политики
        if (showPolicyModal) {
            PolicyModal(onDismiss = { showPolicyModal = false })
        }
    }
}
 
@Composable
private fun AppHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 8.dp),
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
    }
}
 
@Composable
private fun AuthScreen(
    initialName: String,
    initialGroup: String,
    onOpenPolicy: () -> Unit,
    onSuccess: (name: String, group: String, isReg: Boolean) -> Unit
) {
    var mode by remember { mutableStateOf("reg") } // "reg" | "login"
    var name by remember { mutableStateOf(initialName) }
    var group by remember { mutableStateOf(initialGroup) }
    var email by remember { mutableStateOf("veronika.2ip@it.moscow") }
    var password by remember { mutableStateOf("••••••••") }
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
                // Вкладки Регистрация / Вход
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
                        } else {
                            errorText = ""
                            onSuccess(name.trim(), group.trim(), mode == "reg")
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
 
        // Блок безопасности
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
    candidates: List<Candidate>,
    currentIndex: Int,
    onSwipeLeft: () -> Unit,
    onSwipeRight: (Candidate) -> Unit,
    onReset: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (currentIndex >= candidates.size) {
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
                        text = "Перейдите во вкладку «Чаты», чтобы написать тиммейтам, или обновите ленту заново.",
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
            val c = candidates[currentIndex]
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
                    // Верхняя строка: группа и статус
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
 
                    // Аватар, имя, роль
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        AvatarCircle(
                            letter = c.name.first().toString(),
                            size = 60.dp,
                            fontSize = 22.sp
                        )
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
 
                    // О себе
                    Text(
                        text = c.bio,
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.5.sp,
                        lineHeight = 19.sp
                    )
 
                    // Суперсила
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = AccentBlue.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "СУПЕРСИЛА В КОМАНДЕ",
                                color = AccentSoft,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = c.superpower,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
 
                    // Навыки
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        c.skills.forEach { skill ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.06f),
                                border = BorderStroke(1.dp, BorderSubtle)
                            ) {
                                Text(
                                    text = skill,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
 
                    // Кнопки свайпа (пропустить / мэтч)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSwipeLeft,
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, BorderSubtle),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White.copy(alpha = 0.75f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Пропустить",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Пропустить", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
 
                        Button(
                            onClick = { onSwipeRight(c) },
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentBlue,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "В команду",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("В команду", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
 
@Composable
private fun ChatsListScreen(
    chats: List<ChatThread>,
    onOpenChat: (Int) -> Unit
) {
    if (chats.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "Пока нет активных чатов. Свайпните анкеты в поиске!",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }
        return
    }
 
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(chats, key = { it.id }) { chat ->
            val lastText = chat.messages.lastOrNull()?.text.orEmpty()
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = CardBg,
                border = BorderStroke(1.dp, BorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenChat(chat.id) }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AvatarCircle(
                        letter = chat.name.first().toString(),
                        size = 48.dp,
                        fontSize = 18.sp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = chat.name,
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (chat.online) "В сети" else "Не в сети",
                                color = if (chat.online) OnlineTextGreen else OfflineGray,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = lastText,
                            color = Color.White.copy(alpha = 0.65f),
                            fontSize = 12.5.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}
 
@Composable
private fun DialogScreen(
    chat: ChatThread,
    onBack: () -> Unit,
    onReport: () -> Unit,
    onSendMessage: (String) -> Unit
) {
    var draft by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current
 
    LaunchedEffect(chat.messages.size) {
        if (chat.messages.isNotEmpty()) {
            listState.animateScrollToItem(chat.messages.size - 1)
        }
    }
 
    fun submit() {
        val clean = draft.trim()
        if (clean.isNotEmpty()) {
            onSendMessage(clean)
            draft = ""
            keyboardController?.hide()
        }
    }
 
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = CardBg,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Шапка диалога
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Назад",
                        tint = Color.White
                    )
                }
                AvatarCircle(
                    letter = chat.name.first().toString(),
                    size = 38.dp,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = chat.name,
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = chat.statusText,
                        color = if (chat.online) OnlineTextGreen else OfflineGray,
                        fontSize = 11.sp
                    )
                }
                Text(
                    text = "Жалоба",
                    color = DangerRed.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onReport() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
 
            HorizontalDivider(color = BorderSubtle)
 
            // Сообщения
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chat.messages) { msg ->
                    when (msg.sender) {
                        "system" -> {
                            Text(
                                text = msg.text,
                                color = Color.White.copy(alpha = 0.55f),
                                fontSize = 11.5.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                            )
                        }
 
                        "me" -> {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp),
                                    color = AccentBlue,
                                    modifier = Modifier.widthIn(max = 260.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        color = Color.White,
                                        fontSize = 13.5.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }
 
                        else -> {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp),
                                    color = Color.White.copy(alpha = 0.08f),
                                    border = BorderStroke(1.dp, BorderSubtle),
                                    modifier = Modifier.widthIn(max = 260.dp)
                                ) {
                                    Text(
                                        text = msg.text,
                                        color = Color.White.copy(alpha = 0.9f),
                                        fontSize = 13.5.sp,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
 
            HorizontalDivider(color = BorderSubtle)
 
            // Строка ввода
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    placeholder = {
                        Text(
                            text = "Написать сообщение...",
                            color = Color.White.copy(alpha = 0.4f),
                            fontSize = 13.sp
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = { submit() }),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = InputBg,
                        unfocusedContainerColor = InputBg,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = BorderSubtle,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = AccentBlue
                    ),
                    modifier = Modifier.weight(1f)
                )
 
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = AccentBlue,
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { submit() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Отправить",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}
 
@Composable
private fun ProfileScreen(
    name: String,
    group: String,
    role: String,
    superpower: String,
    bio: String,
    avatarUri: Uri?,
    onAvatarChange: (Uri?) -> Unit,
    onSave: (String, String, String, String, String) -> Unit,
    onLogout: () -> Unit
) {
    var editName by remember(name) { mutableStateOf(name) }
    var editGroup by remember(group) { mutableStateOf(group) }
    var editRole by remember(role) { mutableStateOf(role) }
    var editSuperpower by remember(superpower) { mutableStateOf(superpower) }
    var editBio by remember(bio) { mutableStateOf(bio) }
 
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) onAvatarChange(uri)
    }
 
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = CardBg,
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                AvatarCircle(
                    letter = editName.firstOrNull()?.toString() ?: "V",
                    size = 64.dp,
                    fontSize = 24.sp,
                    imageUri = avatarUri
                )
                Column {
                    Text(
                        text = "Мой профиль",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Сменить фото",
                        color = AccentSoft,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clickable {
                                photoPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            }
                    )
                }
            }
 
            FieldLabel("ИМЯ И ВОЗРАСТ")
            DarkTextField(value = editName, onValueChange = { editName = it })
 
            FieldLabel("КУРС И ГРУППА")
            DarkTextField(value = editGroup, onValueChange = { editGroup = it })
 
            FieldLabel("РОЛЬ В КОМАНДЕ")
            DarkTextField(value = editRole, onValueChange = { editRole = it })
 
            FieldLabel("СУПЕРСИЛА")
            DarkTextField(value = editSuperpower, onValueChange = { editSuperpower = it })
 
            FieldLabel("О СЕБЕ")
            DarkTextField(value = editBio, onValueChange = { editBio = it }, singleLine = false)
 
            PrimaryGradientButton(
                text = "Сохранить изменения",
                onClick = { onSave(editName, editGroup, editRole, editSuperpower, editBio) },
                modifier = Modifier.padding(top = 16.dp)
            )
 
            OutlinedButton(
                onClick = onLogout,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White.copy(alpha = 0.75f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(46.dp)
            ) {
                Text("Выйти из аккаунта", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
 
@Composable
private fun BottomTabBar(
    currentTab: String,
    onSelectTab: (String) -> Unit
) {
    val tabs = listOf(
        "search" to "Поиск",
        "chats" to "Чаты",
        "profile" to "Профиль"
    )
 
    Surface(
        color = Color(0xE60B1222),
        border = BorderStroke(1.dp, BorderSubtle),
        shape = RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            tabs.forEach { (key, label) ->
                val selected = currentTab == key
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selected) AccentBlue.copy(alpha = 0.2f) else Color.Transparent)
                        .clickable { onSelectTab(key) }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = label,
                        color = if (selected) AccentSoft else Color.White.copy(alpha = 0.6f),
                        fontSize = 13.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
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
            color = Color(0xFF10192D),
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Политика и правила IT.МИТ",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "1. Уважительное общение и академическая этика.\n" +
                        "2. Запрещены спам, реклама и передача личных данных третьим лицам без согласия.\n" +
                        "3. Профиль используется для поиска команды на учебные, хакатонные и пет-проекты.",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.5.sp,
                    lineHeight = 18.sp
                )
                PrimaryGradientButton(
                    text = "Понятно",
                    onClick = onDismiss,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}
 
// Вспомогательные UI-элементы
@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.55f),
        fontSize = 10.5.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 0.8.sp,
        modifier = Modifier.padding(top = 12.dp, bottom = 5.dp)
    )
}
 
@Composable
private fun DarkTextField(
    value: String,
    onValueChange: (String) -> Unit,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        maxLines = if (singleLine) 1 else 5,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = InputBg,
            unfocusedContainerColor = InputBg,
            focusedBorderColor = AccentBlue,
            unfocusedBorderColor = BorderSubtle,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.horizontalGradient(listOf(AccentBlue, AccentBlueDark)))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
 
@Composable
private fun AvatarCircle(
    letter: String,
    size: Dp,
    fontSize: TextUnit,
    imageUri: Uri? = null
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(AccentBlue, Color(0xFF7C3AED))))
            .border(1.5.dp, Color.White.copy(alpha = 0.2f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (imageUri != null) {
            AsyncImage(
                model = imageUri,
                contentDescription = "Аватар",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text(
                text = letter.uppercase(),
                color = Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}
 
@Composable
private fun StatusPill(online: Boolean, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (online) OnlineGreen.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.06f)
            )
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (online) OnlineGreen else OfflineGray)
        )
        Text(
            text = text,
            color = if (online) OnlineTextGreen else Color.White.copy(alpha = 0.6f),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

Ответить