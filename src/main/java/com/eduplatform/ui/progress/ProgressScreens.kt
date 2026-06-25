package com.eduplatform.ui.progress

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduplatform.data.models.*
import com.eduplatform.data.repository.EduRepository
import com.eduplatform.data.repository.Result
import com.eduplatform.ui.components.*
import com.eduplatform.ui.teacher.StatCard
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val repository: EduRepository
) : ViewModel() {

    private val _progress = MutableStateFlow<List<CourseProgress>>(emptyList())
    val progress: StateFlow<List<CourseProgress>> = _progress.asStateFlow()

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun loadProgress() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repository.getMyProgress()) {
                is Result.Success -> { _progress.value = r.data; _error.value = null }
                is Result.Error -> _error.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun loadProfile() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repository.getMyProfile()) {
                is Result.Success -> { _profile.value = r.data; _error.value = null }
                is Result.Error -> _error.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun updateProfile(bio: String, phone: String, avatar: String) {
        viewModelScope.launch {
            _isLoading.value = true
            val req = UpdateProfileRequest(
                bio = bio.takeIf { it.isNotBlank() },
                phone = phone.takeIf { it.isNotBlank() },
                avatar = avatar.takeIf { it.isNotBlank() }
            )
            when (val r = repository.updateProfile(req)) {
                is Result.Success -> {
                    _profile.value = r.data.copy(user = r.data.user ?: _profile.value?.user)
                    _message.value = "Профіль оновлено"
                }
                is Result.Error -> _message.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun clearMessage() { _message.value = null }
}

// ── Screens ───────────────────────────────────────────────────────────────────

@Composable
fun MyProgressScreen(
    viewModel: ProgressViewModel,
    onCourseClick: (String) -> Unit
) {
    val progress by viewModel.progress.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadProgress() }

    Scaffold(topBar = { EduTopBar("Моє навчання") }) { padding ->
        when {
            isLoading -> LoadingScreen()
            error != null -> ErrorScreen(error!!, onRetry = viewModel::loadProgress)
            progress.isEmpty() -> Column(
                Modifier.fillMaxSize().padding(padding).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(Icons.Default.School, null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.height(16.dp))
                Text("Ви ще не записані на жодний курс",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                Spacer(Modifier.height(8.dp))
                Text("Перейдіть у Каталог і запишіться на курс",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            }
            else -> {
                val avgProgress = progress.map { it.percentage }.average()
                val completedCourses = progress.count { it.isCompleted }
                val totalLessonsCompleted = progress.sumOf { it.completedLessons }

                LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                    item {
                        Row(Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard("Записано курсів", "${progress.size}", Icons.Default.Book, Modifier.weight(1f))
                            StatCard("Завершено", "$completedCourses", Icons.Default.EmojiEvents, Modifier.weight(1f))
                        }
                        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard("Уроків пройдено", "$totalLessonsCompleted", Icons.Default.CheckCircle, Modifier.weight(1f))
                            StatCard("Середній прогрес", "${avgProgress.toInt()}%", Icons.Default.TrendingUp, Modifier.weight(1f))
                        }
                        com.eduplatform.ui.components.SectionHeader("Курси, які я проходжу")
                    }
                    items(progress) { cp ->
                        CourseProgressCard(cp = cp, onClick = { cp.course?.id?.let(onCourseClick) })
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

@Composable
fun CourseProgressCard(cp: CourseProgress, onClick: () -> Unit) {
    Card(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(cp.course?.title ?: "Курс", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically) {
                Text("${cp.completedLessons} / ${cp.totalLessons} уроків",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                Text("${cp.percentage}%", style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (cp.percentage == 100) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { cp.percentage / 100f },
                modifier = Modifier.fillMaxWidth()
            )
            if (cp.percentage == 100) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.EmojiEvents, null, Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(4.dp))
                    Text("Курс завершено!", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(
    viewModel: ProgressViewModel,
    userRole: String,
    userName: String?,
    onLogout: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    var isEditing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.loadProfile() }
    LaunchedEffect(message) {
        if (message != null) { kotlinx.coroutines.delay(2000); viewModel.clearMessage() }
    }

    var bio by remember(profile) { mutableStateOf(profile?.bio ?: "") }
    var phone by remember(profile) { mutableStateOf(profile?.phone ?: "") }
    var avatar by remember(profile) { mutableStateOf(profile?.avatar ?: "") }

    Scaffold(topBar = {
        EduTopBar(
            "Профіль",
            actions = {
                IconButton(onClick = { isEditing = !isEditing }) {
                    Icon(if (isEditing) Icons.Default.Close else Icons.Default.Edit, null)
                }
            }
        )
    }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            // Avatar
            Surface(
                shape = MaterialTheme.shapes.extraLarge,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(96.dp)
            ) {
                if (!profile?.avatar.isNullOrBlank()) {
                    AsyncImage(
                        model = profile?.avatar,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            (userName?.firstOrNull() ?: "?").toString().uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(userName ?: "", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            profile?.user?.email?.let { Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline) }
            Spacer(Modifier.height(8.dp))
            RoleBadge(userRole)

            Spacer(Modifier.height(24.dp))
            message?.let {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Text(it, Modifier.padding(12.dp))
                }
                Spacer(Modifier.height(12.dp))
            }

            if (isEditing) {
                OutlinedTextField(value = avatar, onValueChange = { avatar = it },
                    label = { Text("URL аватара") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = bio, onValueChange = { bio = it.take(1000) },
                    label = { Text("Біографія (до 1000 символів)") }, modifier = Modifier.fillMaxWidth().height(120.dp),
                    supportingText = { Text("${bio.length}/1000") })
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it.take(20) },
                    label = { Text("Телефон") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(16.dp))
                Button(
                    onClick = { viewModel.updateProfile(bio, phone, avatar); isEditing = false },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                ) {
                    if (isLoading) CircularProgressIndicator(Modifier.size(20.dp))
                    else { Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("Зберегти") }
                }
            } else {
                profile?.let { p ->
                    if (!p.bio.isNullOrBlank()) {
                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text("Про себе", style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.outline)
                                Spacer(Modifier.height(4.dp))
                                Text(p.bio, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                    if (!p.phone.isNullOrBlank()) {
                        ListItem(
                            headlineContent = { Text(p.phone) },
                            leadingContent = { Icon(Icons.Default.Phone, null) },
                            supportingContent = { Text("Телефон") }
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.Logout, null)
                Spacer(Modifier.width(8.dp))
                Text("Вийти з акаунту")
            }
        }
    }
}