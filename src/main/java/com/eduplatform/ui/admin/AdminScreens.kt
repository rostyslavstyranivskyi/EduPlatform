package com.eduplatform.ui.admin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduplatform.data.models.*
import com.eduplatform.data.repository.EduRepository
import com.eduplatform.data.repository.Result
import com.eduplatform.ui.components.*
import com.eduplatform.ui.topics.TopicsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: EduRepository
) : ViewModel() {

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    private val _userQuery = MutableStateFlow("")
    val userQuery: StateFlow<String> = _userQuery.asStateFlow()

    private val _roleFilter = MutableStateFlow<String?>(null)
    val roleFilter: StateFlow<String?> = _roleFilter.asStateFlow()

    // Profile for user-details dialog
    private val _selectedUserProfile = MutableStateFlow<UserProfile?>(null)
    val selectedUserProfile: StateFlow<UserProfile?> = _selectedUserProfile.asStateFlow()

    private val _profileLoading = MutableStateFlow(false)
    val profileLoading: StateFlow<Boolean> = _profileLoading.asStateFlow()

    fun onUserQuery(q: String) {
        _userQuery.value = q
        viewModelScope.launch {
            kotlinx.coroutines.delay(400)
            if (_userQuery.value == q) loadUsers()
        }
    }

    fun setRoleFilter(role: String?) { _roleFilter.value = role; loadUsers() }

    fun loadUsers() {
        viewModelScope.launch {
            _isLoading.value = true
            val q = _userQuery.value.takeIf { it.isNotBlank() }
            when (val r = repository.getAdminUsers(_roleFilter.value, q)) {
                is Result.Success -> { _users.value = r.data.users; _error.value = null }
                is Result.Error -> _error.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun loadCourses(status: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repository.getAdminCourses(status)) {
                is Result.Success -> { _courses.value = r.data.courses; _error.value = null }
                is Result.Error -> _error.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun loadUserProfile(userId: String) {
        _selectedUserProfile.value = null
        viewModelScope.launch {
            _profileLoading.value = true
            when (val r = repository.getUserProfile(userId)) {
                is Result.Success -> _selectedUserProfile.value = r.data
                is Result.Error -> { /* profile stays null; dialog shows what we have */ }
                else -> {}
            }
            _profileLoading.value = false
        }
    }

    fun clearSelectedProfile() { _selectedUserProfile.value = null }

    fun banUser(id: String) {
        viewModelScope.launch {
            when (val r = repository.banUser(id)) {
                is Result.Success -> { _message.value = "Користувача забанено"; loadUsers() }
                is Result.Error -> _message.value = r.message
                else -> {}
            }
        }
    }

    fun unbanUser(id: String) {
        viewModelScope.launch {
            when (val r = repository.unbanUser(id)) {
                is Result.Success -> { _message.value = "Бан знято"; loadUsers() }
                is Result.Error -> _message.value = r.message
                else -> {}
            }
        }
    }

    fun changeRole(id: String, role: String) {
        viewModelScope.launch {
            when (val r = repository.changeUserRole(id, role)) {
                is Result.Success -> { _message.value = "Роль змінено"; loadUsers() }
                is Result.Error -> _message.value = r.message
                else -> {}
            }
        }
    }

    fun unpublishCourse(id: String) {
        viewModelScope.launch {
            when (val r = repository.adminUnpublishCourse(id)) {
                is Result.Success -> { _message.value = "Курс знято з публікації"; loadCourses() }
                is Result.Error -> _message.value = r.message
                else -> {}
            }
        }
    }

    fun clearMessage() { _message.value = null }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

private fun roleLabel(role: String) = when (role) {
    "student" -> "Студент"
    "teacher" -> "Викладач"
    "admin" -> "Адмін"
    else -> role
}

private fun formatDate(raw: String?): String {
    if (raw == null) return "—"
    return try {
        // Raw is ISO-8601, e.g. "2026-06-20T15:32:00.000Z"
        val date = raw.substringBefore("T")
        val parts = date.split("-")
        if (parts.size == 3) {
            val months = listOf("", "січня", "лютого", "березня", "квітня", "травня", "червня",
                "липня", "серпня", "вересня", "жовтня", "листопада", "грудня")
            val m = parts[1].toIntOrNull() ?: return raw
            "${parts[2].trimStart('0')} ${months.getOrElse(m) { parts[1] }} ${parts[0]} р."
        } else raw
    } catch (e: Exception) { raw }
}

// ── User Details Dialog ───────────────────────────────────────────────────────

@Composable
fun UserDetailsDialog(
    user: User,
    profile: UserProfile?,
    profileLoading: Boolean,
    onDismiss: () -> Unit,
    onChangeRole: () -> Unit,
    onBan: () -> Unit,
    onUnban: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Деталі користувача") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Avatar + Name + Email
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    val avatarUrl = profile?.avatar?.takeIf { it.isNotBlank() }
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        if (avatarUrl != null) {
                            AsyncImage(
                                model = avatarUrl,
                                contentDescription = "Аватар",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${user.name.firstOrNull() ?: ""}${user.surname.firstOrNull() ?: ""}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                    Column {
                        Text("${user.name} ${user.surname}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(user.email, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                    }
                }

                HorizontalDivider()

                // Info grid
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            DetailCell(label = "Роль", value = roleLabel(user.role))
                            DetailCell(
                                label = "Статус",
                                value = if (user.isBanned) "Заблокований" else "Активний",
                                valueColor = if (user.isBanned)
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            DetailCell(label = "Баланс", value = "%.2f ₴".format(user.balance))
                            DetailCell(
                                label = "Телефон",
                                value = if (profileLoading) "…"
                                else profile?.phone?.takeIf { it.isNotBlank() } ?: "—"
                            )
                        }
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            DetailCell(label = "Дата реєстрації", value = formatDate(user.createdAt))
                            DetailCell(label = "Останнє оновлення", value = formatDate(user.updatedAt))
                        }
                    }
                }

                // Bio
                val bio = profile?.bio?.takeIf { it.isNotBlank() }
                if (!profileLoading && bio != null) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Про себе", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline)
                        Text(bio, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onChangeRole,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Icon(Icons.Default.ManageAccounts, null, Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Змінити роль")
                }
                if (user.role != "admin") {
                    if (user.isBanned) {
                        Button(
                            onClick = onUnban,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) { Text("Розбан") }
                    } else {
                        Button(
                            onClick = onBan,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Block, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Бан")
                        }
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Закрити") }
        }
    )
}

@Composable
private fun DetailCell(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = valueColor)
    }
}

// ── AdminUsersScreen ──────────────────────────────────────────────────────────

@Composable
fun AdminUsersScreen(viewModel: AdminViewModel) {
    val users by viewModel.users.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()
    val query by viewModel.userQuery.collectAsState()
    val roleFilter by viewModel.roleFilter.collectAsState()
    val selectedProfile by viewModel.selectedUserProfile.collectAsState()
    val profileLoading by viewModel.profileLoading.collectAsState()

    var detailsUser by remember { mutableStateOf<User?>(null) }
    var showRoleDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadUsers() }
    LaunchedEffect(message) {
        if (message != null) { kotlinx.coroutines.delay(2000); viewModel.clearMessage() }
    }

    // Role change dialog
    detailsUser?.let { u ->
        if (showRoleDialog) {
            AlertDialog(
                onDismissRequest = { showRoleDialog = false },
                title = { Text("Змінити роль") },
                text = {
                    Column {
                        listOf("student" to "Студент", "teacher" to "Викладач", "admin" to "Адмін").forEach { (r, l) ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                RadioButton(selected = u.role == r, onClick = {
                                    viewModel.changeRole(u.id, r)
                                    showRoleDialog = false
                                    detailsUser = null
                                })
                                Text(l)
                            }
                        }
                    }
                },
                confirmButton = { TextButton(onClick = { showRoleDialog = false }) { Text("Скасувати") } }
            )
        }
    }

    // User details dialog
    detailsUser?.let { u ->
        if (!showRoleDialog) {
            UserDetailsDialog(
                user = u,
                profile = selectedProfile,
                profileLoading = profileLoading,
                onDismiss = {
                    detailsUser = null
                    viewModel.clearSelectedProfile()
                },
                onChangeRole = { showRoleDialog = true },
                onBan = {
                    viewModel.banUser(u.id)
                    detailsUser = null
                    viewModel.clearSelectedProfile()
                },
                onUnban = {
                    viewModel.unbanUser(u.id)
                    detailsUser = null
                    viewModel.clearSelectedProfile()
                }
            )
        }
    }

    Scaffold(topBar = { EduTopBar("Управління користувачами") }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = query, onValueChange = viewModel::onUserQuery,
                placeholder = { Text("Пошук користувачів...") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true
            )
            Row(Modifier.padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(null to "Всі", "student" to "Студенти", "teacher" to "Викладачі", "admin" to "Адміни")
                    .forEach { (v, l) ->
                        FilterChip(selected = roleFilter == v, onClick = { viewModel.setRoleFilter(v) },
                            label = { Text(l) })
                    }
            }
            message?.let {
                Card(
                    Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) { Text(it, Modifier.padding(12.dp)) }
            }
            when {
                isLoading -> LoadingScreen()
                error != null -> ErrorScreen(error!!, onRetry = viewModel::loadUsers)
                users.isEmpty() -> EmptyScreen("Користувачів не знайдено")
                else -> LazyColumn(Modifier.weight(1f)) {
                    items(users) { user ->
                        ListItem(
                            modifier = Modifier.clickable {
                                detailsUser = user
                                viewModel.loadUserProfile(user.id)
                            },
                            headlineContent = { Text("${user.name} ${user.surname}") },
                            supportingContent = {
                                Column {
                                    Text(user.email, style = MaterialTheme.typography.bodySmall)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        RoleBadge(user.role)
                                        if (user.isBanned) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.errorContainer,
                                                shape = MaterialTheme.shapes.small
                                            ) {
                                                Text(
                                                    "ЗАБЛОКОВАНО",
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onErrorContainer
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                            trailingContent = {
                                Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

// ── AdminCoursesScreen ────────────────────────────────────────────────────────

@Composable
fun AdminCoursesScreen(
    viewModel: AdminViewModel,
    onCourseClick: (String) -> Unit
) {
    val courses by viewModel.courses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()

    var statusFilter by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(statusFilter) { viewModel.loadCourses(statusFilter) }
    LaunchedEffect(message) {
        if (message != null) { kotlinx.coroutines.delay(2000); viewModel.clearMessage() }
    }

    Scaffold(topBar = { EduTopBar("Модерація курсів") }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Row(
                Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(null to "Всі", "published" to "Опубліковані", "draft" to "Чернетки").forEach { (v, l) ->
                    FilterChip(selected = statusFilter == v, onClick = { statusFilter = v }, label = { Text(l) })
                }
            }
            message?.let {
                Card(
                    Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) { Text(it, Modifier.padding(12.dp)) }
            }
            when {
                isLoading -> LoadingScreen()
                error != null -> ErrorScreen(error!!)
                courses.isEmpty() -> EmptyScreen("Курсів не знайдено")
                else -> LazyColumn(Modifier.weight(1f)) {
                    items(courses) { course ->
                        ListItem(
                            modifier = Modifier.clickable { onCourseClick(course.id) },
                            headlineContent = { Text(course.title) },
                            supportingContent = {
                                Column {
                                    course.teacher?.let { t ->
                                        Text("${t.name} ${t.surname}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    StatusBadge(course.status)
                                }
                            },
                            leadingContent = { Icon(Icons.Default.Book, null) },
                            trailingContent = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (course.status == "published") {
                                        IconButton(onClick = { viewModel.unpublishCourse(course.id) }) {
                                            Icon(
                                                Icons.Default.PublicOff, "Зняти з публікації",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                    Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}

// ── AdminCourseDetailScreen ───────────────────────────────────────────────────

@Composable
fun AdminCourseDetailScreen(
    courseId: String,
    coursesViewModel: com.eduplatform.ui.courses.CoursesViewModel,
    onBack: () -> Unit,
    onViewBlocks: (String) -> Unit,
    onUnpublish: (String) -> Unit
) {
    val course by coursesViewModel.course.collectAsState()
    val isLoading by coursesViewModel.isLoading.collectAsState()
    val error by coursesViewModel.error.collectAsState()

    LaunchedEffect(courseId) { coursesViewModel.loadCourse(courseId) }

    Scaffold(
        topBar = {
            EduTopBar(
                title = course?.title ?: "Деталі курсу",
                onBack = onBack,
                actions = {
                    if (course?.status == "published") {
                        IconButton(onClick = { course?.id?.let(onUnpublish) }) {
                            Icon(Icons.Default.PublicOff, "Зняти з публікації",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            isLoading -> LoadingScreen()
            error != null -> ErrorScreen(error!!, onRetry = { coursesViewModel.loadCourse(courseId) })
            course == null -> EmptyScreen("Курс не знайдено")
            else -> {
                val c = course!!
                LazyColumn(
                    Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Status + Category row
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            StatusBadge(c.status)
                            c.category?.let { cat ->
                                Surface(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = MaterialTheme.shapes.small
                                ) {
                                    Text(
                                        cat.name,
                                        Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                }
                            }
                        }
                    }
                    item {
                        c.description?.let { desc ->
                            Text(desc, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    item {
                        // Info card
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                InfoRow("Викладач", c.teacher?.let { "${it.name} ${it.surname}" } ?: "—")
                                InfoRow("Ціна", if (c.price == 0.0) "Безкоштовно" else "%.2f ₴".format(c.price))
                                InfoRow("Режим доступу", if (c.accessMode == "open") "Відкритий" else "Закритий")
                                InfoRow("Студентів", c.enrolledCount.toString())
                            }
                        }
                    }
                    item {
                        Button(
                            onClick = { onViewBlocks(c.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.ListAlt, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Переглянути уроки та тести")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

// ── AdminCourseBlocksScreen ───────────────────────────────────────────────────

@Composable
fun AdminCourseBlocksScreen(
    courseId: String,
    lessonsViewModel: com.eduplatform.ui.lessons.LessonsViewModel,
    topicsViewModel: com.eduplatform.ui.topics.TopicsViewModel,
    onBack: () -> Unit,
    onLessonClick: (String) -> Unit,
    onLessonTestClick: (String) -> Unit
) {
    val blocks by lessonsViewModel.blocks.collectAsState()
    val topics by topicsViewModel.topics.collectAsState()
    val isLoading by lessonsViewModel.isLoading.collectAsState()
    val error by lessonsViewModel.error.collectAsState()

    LaunchedEffect(courseId) {
        lessonsViewModel.loadBlocks(courseId)
        topicsViewModel.loadTopics(courseId)
    }

    Scaffold(topBar = { EduTopBar("Уроки курсу", onBack = onBack) }) { padding ->
        when {
            isLoading -> LoadingScreen()
            error != null -> ErrorScreen(error!!, onRetry = { lessonsViewModel.loadBlocks(courseId) })
            blocks.isEmpty() -> EmptyScreen("Уроків ще немає")
            else -> {
                // Резервна карта lessonId → topicId з даних тем
                // (блоки з /blocks можуть не містити topicId, якщо сервер його не включає)
                val lessonTopicMap: Map<String, String> = topics
                    .flatMap { topic -> topic.lessons.map { lesson -> lesson.id to topic.id } }
                    .toMap()

                // Групуємо блоки по темах з урахуванням резервної карти
                val blocksByTopicId = blocks.groupBy { block ->
                    block.lesson.topicId?.takeIf { it.isNotBlank() }
                        ?: lessonTopicMap[block.lesson.id]
                }

                // Теми у правильному порядку + секція "Без теми"
                val topicSections: List<Pair<String?, List<com.eduplatform.data.models.CourseBlock>>> =
                    topics.sortedBy { it.order }.map { topic ->
                        topic.id to (blocksByTopicId[topic.id]?.sortedBy { it.lesson.order } ?: emptyList())
                    } + listOf(null to (blocksByTopicId[null]?.sortedBy { it.lesson.order } ?: emptyList()))

                LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                    topicSections.forEach { (topicId, sectionBlocks) ->
                        if (sectionBlocks.isEmpty()) return@forEach

                        val topicTitle = if (topicId == null) "Без теми"
                        else topics.find { it.id == topicId }?.title ?: "Тема"

                        // Заголовок секції
                        item(key = "header_${topicId ?: "none"}") {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    if (topicId != null) Icons.Default.FolderOpen else Icons.Default.Folder,
                                    contentDescription = null,
                                    tint = if (topicId != null) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    topicTitle,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (topicId != null) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.outline
                                )
                                Text(
                                    "· ${sectionBlocks.size} уроків",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        // Уроки секції
                        items(sectionBlocks, key = { "block_${it.lesson.id}" }) { block ->
                            val lesson = block.lesson
                            Card(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 2.dp)
                            ) {
                                Column {
                                    ListItem(
                                        modifier = Modifier.clickable { onLessonClick(lesson.id) },
                                        headlineContent = { Text(lesson.title) },
                                        supportingContent = {
                                            Text(
                                                "#${lesson.order + 1} · " + when (lesson.type) {
                                                    "video" -> "Відео"
                                                    "pdf" -> "PDF"
                                                    else -> "Текст"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        },
                                        leadingContent = {
                                            Icon(
                                                when (lesson.type) {
                                                    "video" -> Icons.Default.PlayCircle
                                                    "pdf" -> Icons.Default.PictureAsPdf
                                                    else -> Icons.Default.Article
                                                },
                                                null,
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        },
                                        trailingContent = {
                                            Icon(Icons.Default.ChevronRight, null,
                                                tint = MaterialTheme.colorScheme.outline)
                                        }
                                    )
                                    block.test?.let { t ->
                                        HorizontalDivider()
                                        ListItem(
                                            modifier = Modifier
                                                .padding(start = 16.dp)
                                                .clickable { onLessonTestClick(lesson.id) },
                                            headlineContent = {
                                                Text(t.title, style = MaterialTheme.typography.bodyMedium)
                                            },
                                            supportingContent = {
                                                Text(
                                                    "Тест · прохідний бал: ${t.passingScore}%",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            },
                                            leadingContent = {
                                                Icon(
                                                    Icons.Default.Quiz, null,
                                                    tint = MaterialTheme.colorScheme.secondary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            },
                                            trailingContent = {
                                                Icon(Icons.Default.ChevronRight, null,
                                                    tint = MaterialTheme.colorScheme.outline)
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        item(key = "divider_${topicId ?: "none"}") {
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                }
            }
        }
    }
}

// ── AdminLessonDetailScreen ───────────────────────────────────────────────────

@Composable
fun AdminLessonDetailScreen(
    lessonId: String,
    lessonsViewModel: com.eduplatform.ui.lessons.LessonsViewModel,
    onBack: () -> Unit
) {
    val lesson by lessonsViewModel.lesson.collectAsState()
    val isLoading by lessonsViewModel.isLoading.collectAsState()
    val error by lessonsViewModel.error.collectAsState()

    LaunchedEffect(lessonId) { lessonsViewModel.loadLesson(lessonId) }

    Scaffold(topBar = { EduTopBar(lesson?.title ?: "Урок", onBack = onBack) }) { padding ->
        when {
            isLoading -> LoadingScreen()
            error != null -> ErrorScreen(error!!)
            lesson == null -> EmptyScreen("Урок не знайдено")
            else -> {
                val l = lesson!!
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Type badge
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            when (l.type) {
                                "video" -> "Відео-урок"
                                "pdf" -> "PDF-урок"
                                else -> "Текстовий урок"
                            },
                            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    when (l.type) {
                        "video" -> {
                            l.videoUrl?.let { url ->
                                Card(Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.PlayCircle, null, tint = MaterialTheme.colorScheme.primary)
                                            Text("Відео", style = MaterialTheme.typography.titleSmall)
                                        }
                                        Text(url, style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                        "pdf" -> {
                            l.pdfUrl?.let { url ->
                                Card(Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Row(verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Icon(Icons.Default.PictureAsPdf, null, tint = MaterialTheme.colorScheme.primary)
                                            Text("PDF-документ", style = MaterialTheme.typography.titleSmall)
                                        }
                                        Text(url, style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        }
                        else -> {
                            l.content?.let { content ->
                                Card(Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text("Зміст уроку", style = MaterialTheme.typography.titleSmall,
                                            modifier = Modifier.padding(bottom = 8.dp))
                                        Text(content, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }

                    // Read-only notice
                    Card(
                        Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.AdminPanelSettings, null,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(18.dp))
                            Text("Режим перегляду адміністратора",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer)
                        }
                    }
                }
            }
        }
    }
}

// ── AdminLessonTestScreen ─────────────────────────────────────────────────────

@Composable
fun AdminLessonTestScreen(
    lessonId: String,
    testViewModel: com.eduplatform.ui.tests.TestViewModel,
    onBack: () -> Unit
) {
    val test by testViewModel.test.collectAsState()
    val isLoading by testViewModel.isLoading.collectAsState()
    val error by testViewModel.error.collectAsState()

    LaunchedEffect(lessonId) { testViewModel.loadLessonTest(lessonId) }

    Scaffold(topBar = { EduTopBar(test?.title ?: "Тест", onBack = onBack) }) { padding ->
        when {
            isLoading -> LoadingScreen()
            error != null -> ErrorScreen(error!!)
            test == null -> EmptyScreen("Тест не знайдено")
            else -> {
                val t = test!!
                LazyColumn(
                    Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                InfoRow("Прохідний бал", "${t.passingScore}%")
                                t.maxAttempts?.let { InfoRow("Макс. спроб", it.toString()) }
                                InfoRow("Питань", t.questions.size.toString())
                            }
                        }
                    }

                    // Read-only notice
                    item {
                        Card(
                            Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                        ) {
                            Row(
                                Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AdminPanelSettings, null,
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(18.dp))
                                Text("Режим перегляду — проходження недоступне",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer)
                            }
                        }
                    }

                    items(t.questions.mapIndexed { i, q -> i to q }) { (index, question) ->
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    "${index + 1}. ${question.question}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Medium
                                )
                                question.options.forEachIndexed { optIdx, option ->
                                    val isCorrect = question.correctIndex == optIdx
                                    Surface(
                                        color = if (isCorrect)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant,
                                        shape = MaterialTheme.shapes.small
                                    ) {
                                        Row(
                                            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (isCorrect) {
                                                Icon(Icons.Default.CheckCircle, null,
                                                    tint = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.size(16.dp))
                                            } else {
                                                Spacer(Modifier.size(16.dp))
                                            }
                                            Text(
                                                option,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (isCorrect)
                                                    MaterialTheme.colorScheme.onPrimaryContainer
                                                else
                                                    MaterialTheme.colorScheme.onSurfaceVariant
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
    }
}