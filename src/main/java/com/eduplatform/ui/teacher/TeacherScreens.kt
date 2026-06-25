package com.eduplatform.ui.teacher

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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduplatform.data.models.*
import com.eduplatform.data.repository.EduRepository
import com.eduplatform.data.repository.Result
import com.eduplatform.ui.components.*
import com.eduplatform.ui.courses.CourseCard
import com.eduplatform.ui.courses.CoursesViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── ViewModel ─────────────────────────────────────────────────────────────────

@HiltViewModel
class TeacherViewModel @Inject constructor(
    private val repository: EduRepository
) : ViewModel() {

    private val _myCourses = MutableStateFlow<List<Course>>(emptyList())
    val myCourses: StateFlow<List<Course>> = _myCourses.asStateFlow()

    private val _dashboard = MutableStateFlow<TeacherDashboard?>(null)
    val dashboard: StateFlow<TeacherDashboard?> = _dashboard.asStateFlow()

    private val _courseAnalytics = MutableStateFlow<CourseAnalytics?>(null)
    val courseAnalytics: StateFlow<CourseAnalytics?> = _courseAnalytics.asStateFlow()

    private val _students = MutableStateFlow<List<StudentProgress>>(emptyList())
    val students: StateFlow<List<StudentProgress>> = _students.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun loadMyCourses() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repository.getMyCourses()) {
                is Result.Success -> { _myCourses.value = r.data.courses; _error.value = null }
                is Result.Error -> _error.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repository.getDashboard()) {
                is Result.Success -> { _dashboard.value = r.data; _error.value = null }
                is Result.Error -> _error.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun loadCourseAnalytics(courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repository.getCourseAnalytics(courseId)) {
                is Result.Success -> { _courseAnalytics.value = r.data; _error.value = null }
                is Result.Error -> _error.value = r.message
                else -> {}
            }
            when (val r = repository.getCourseStudents(courseId)) {
                is Result.Success -> _students.value = r.data
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun createCourse(req: CreateCourseRequest, onDone: (String) -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repository.createCourse(req)) {
                is Result.Success -> { _message.value = "Курс створено"; onDone(r.data.id) }
                is Result.Error -> _message.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun updateCourse(id: String, req: CreateCourseRequest, onDone: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repository.updateCourse(id, req)) {
                is Result.Success -> { _message.value = "Курс оновлено"; onDone() }
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
fun TeacherCoursesScreen(
    viewModel: TeacherViewModel,
    onCourseClick: (String) -> Unit,
    onCreateCourse: () -> Unit
) {
    val courses by viewModel.myCourses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadMyCourses() }

    Scaffold(
        topBar = { EduTopBar("Мої курси") },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateCourse) {
                Icon(Icons.Default.Add, "Створити курс")
            }
        }
    ) { padding ->
        when {
            isLoading -> LoadingScreen()
            error != null -> ErrorScreen(error!!, onRetry = viewModel::loadMyCourses)
            courses.isEmpty() -> EmptyScreen("Ви ще не створили жодного курсу")
            else -> LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                items(courses) { course ->
                    CourseCard(course = course, onClick = { onCourseClick(course.id) })
                }
            }
        }
    }
}

@Composable
fun AnalyticsDashboardScreen(
    viewModel: TeacherViewModel,
    onCourseAnalytics: (String) -> Unit,
    progressViewModel: com.eduplatform.ui.progress.ProgressViewModel? = null,
    onProgressCourseClick: (String) -> Unit = {}
) {
    var tab by remember { mutableStateOf(0) }

    Scaffold(topBar = { EduTopBar("Аналітика") }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 },
                    text = { Text("Мої курси") },
                    icon = { Icon(Icons.Default.BarChart, null) })
                Tab(selected = tab == 1, onClick = { tab = 1 },
                    text = { Text("Моє навчання") },
                    icon = { Icon(Icons.Default.TrendingUp, null) })
            }
            when (tab) {
                0 -> TeacherAnalyticsTab(viewModel = viewModel, onCourseAnalytics = onCourseAnalytics)
                1 -> TeacherLearningProgressTab(
                    progressViewModel = progressViewModel,
                    onCourseClick = onProgressCourseClick
                )
            }
        }
    }
}

@Composable
private fun TeacherAnalyticsTab(
    viewModel: TeacherViewModel,
    onCourseAnalytics: (String) -> Unit
) {
    val dashboard by viewModel.dashboard.collectAsState()
    val courses by viewModel.myCourses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadDashboard(); viewModel.loadMyCourses() }

    when {
        isLoading -> LoadingScreen()
        error != null -> ErrorScreen(error!!)
        else -> LazyColumn(Modifier.fillMaxSize()) {
            item {
                dashboard?.let { d ->
                    Row(Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("Студентів", "${d.totalStudents}", Icons.Default.People, Modifier.weight(1f))
                        StatCard("Дохід", "₴${String.format("%.0f", d.totalRevenue)}", Icons.Default.AttachMoney, Modifier.weight(1f))
                    }
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        StatCard("Баланс", "₴${String.format("%.2f", d.teacherBalance)}", Icons.Default.AccountBalance, Modifier.weight(1f))
                        StatCard("Курсів", "${d.totalCourses}", Icons.Default.Book, Modifier.weight(1f))
                    }
                }
            }
            item { SectionHeader("Аналітика по курсах") }
            items(courses) { course ->
                ListItem(
                    headlineContent = { Text(course.title) },
                    supportingContent = { StatusBadge(course.status) },
                    leadingContent = { Icon(Icons.Default.BarChart, null, tint = MaterialTheme.colorScheme.primary) },
                    trailingContent = { Icon(Icons.Default.ChevronRight, null) },
                    modifier = Modifier.clickable { onCourseAnalytics(course.id) }
                )
                HorizontalDivider()
            }
        }
    }
}

@Composable
private fun TeacherLearningProgressTab(
    progressViewModel: com.eduplatform.ui.progress.ProgressViewModel?,
    onCourseClick: (String) -> Unit
) {
    if (progressViewModel == null) {
        LoadingScreen()
        return
    }

    val progress by progressViewModel.progress.collectAsState()
    val isLoading by progressViewModel.isLoading.collectAsState()
    val error by progressViewModel.error.collectAsState()

    LaunchedEffect(Unit) { progressViewModel.loadProgress() }

    when {
        isLoading -> LoadingScreen()
        error != null -> ErrorScreen(error!!, onRetry = progressViewModel::loadProgress)
        progress.isEmpty() -> Column(
            Modifier.fillMaxSize().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.School, null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(16.dp))
            Text("Ви ще не записані на жодний курс як студент",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Text("Перейдіть у Каталог і запишіться на курс",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                textAlign = TextAlign.Center)
        }
        else -> {
            // Загальна статистика навчання
            val avgProgress = progress.map { it.percentage }.average()
            val completedCourses = progress.count { it.isCompleted }
            val totalLessonsCompleted = progress.sumOf { it.completedLessons }

            LazyColumn(Modifier.fillMaxSize()) {
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
                }
                item { SectionHeader("Курси, які я проходжу") }
                items(progress) { cp ->
                    Card(
                        Modifier.fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { cp.course?.id?.let(onCourseClick) },
                        elevation = CardDefaults.cardElevation(1.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(cp.course?.title ?: "Курс",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f))
                                if (cp.isCompleted) {
                                    Icon(Icons.Default.EmojiEvents, null,
                                        Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.primary)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Row(Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Text("${cp.completedLessons} / ${cp.totalLessons} уроків",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline)
                                Text("${cp.percentage}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (cp.isCompleted) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurface)
                            }
                            Spacer(Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { cp.percentage / 100f },
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (cp.isCompleted) {
                                Spacer(Modifier.height(6.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, null,
                                        Modifier.size(14.dp),
                                        tint = MaterialTheme.colorScheme.primary)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Курс завершено!",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(modifier.padding(vertical = 4.dp)) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun CourseAnalyticsScreen(
    courseId: String,
    viewModel: TeacherViewModel,
    onBack: () -> Unit
) {
    val analytics by viewModel.courseAnalytics.collectAsState()
    val students by viewModel.students.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(courseId) { viewModel.loadCourseAnalytics(courseId) }

    Scaffold(topBar = { EduTopBar(analytics?.course?.title ?: "Аналітика курсу", onBack = onBack) }) { padding ->
        when {
            isLoading -> LoadingScreen()
            error != null -> ErrorScreen(error!!)
            else -> LazyColumn(Modifier.fillMaxSize().padding(padding)) {
                analytics?.let { a ->
                    item {
                        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            StatCard("Студентів", "${a.students.total}", Icons.Default.People, Modifier.weight(1f))
                            StatCard("Прогрес", "${String.format("%.0f", a.progress.averagePercentage)}%", Icons.Default.TrendingUp, Modifier.weight(1f))
                        }
                        Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                            ListItem(
                                headlineContent = { Text("Дохід від курсу") },
                                trailingContent = { Text("₴${String.format("%.2f", a.revenue.teacherNet)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                                leadingContent = { Icon(Icons.Default.AttachMoney, null) }
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                }
                if (students.isNotEmpty()) {
                    item { SectionHeader("Студенти (${students.size})") }
                    items(students) { s ->
                        ListItem(
                            headlineContent = { Text("${s.student?.name.orEmpty()} ${s.student?.surname.orEmpty()}") },
                            supportingContent = { Text(s.student?.email.orEmpty()) },
                            leadingContent = {
                                Box(
                                    Modifier.size(40.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        progress = { s.percentage / 100f },
                                        modifier = Modifier.size(40.dp),
                                        strokeWidth = 3.dp
                                    )
                                    Text("${s.percentage}%", style = MaterialTheme.typography.labelSmall)
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

@Composable
fun CreateCourseScreen(
    viewModel: TeacherViewModel,
    onBack: () -> Unit,
    onCreated: (String) -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("0") }
    var coverImage by remember { mutableStateOf("") }

    LaunchedEffect(message) {
        if (message?.contains("створено") == true) viewModel.clearMessage()
    }

    Scaffold(topBar = { EduTopBar("Новий курс", onBack = onBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it },
                label = { Text("Назва курсу (5–255 символів)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                supportingText = { Text("${title.length}/255") })
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = description, onValueChange = { description = it },
                label = { Text("Опис курсу") }, modifier = Modifier.fillMaxWidth().height(120.dp))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = coverImage, onValueChange = { coverImage = it },
                label = { Text("URL обкладинки") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.Image, null) })
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = price, onValueChange = { price = it },
                label = { Text("Ціна (0 = безкоштовно)") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.AttachMoney, null) })

            message?.let {
                Spacer(Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(
                    containerColor = if (it.contains("створено")) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.errorContainer)) { Text(it, Modifier.padding(12.dp)) }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    viewModel.createCourse(
                        CreateCourseRequest(
                            title = title.trim(),
                            description = description.takeIf { it.isNotBlank() },
                            price = price.toDoubleOrNull() ?: 0.0,
                            coverImage = coverImage.takeIf { it.isNotBlank() }
                        ),
                        onDone = onCreated
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && title.length >= 5
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(20.dp))
                else { Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("Створити курс") }
            }
            Spacer(Modifier.height(8.dp))
            Text("Курс буде збережено як чернетку. Після додавання уроків — опублікуйте його.",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

// The route "edit_course/{courseId}" was declared in Navigation.kt and wired up
// from CourseDetailScreen's "Редагувати курс" button, but this screen was never
// actually built or registered in the NavHost — tapping the button crashed with
// "Navigation destination ... cannot be found in the navigation graph".
@Composable
fun EditCourseScreen(
    courseId: String,
    teacherViewModel: TeacherViewModel,
    coursesViewModel: CoursesViewModel,
    onBack: () -> Unit,
    onUpdated: () -> Unit
) {
    val existingCourse by coursesViewModel.course.collectAsState()
    val isCourseLoading by coursesViewModel.isLoading.collectAsState()
    val isSaving by teacherViewModel.isLoading.collectAsState()
    val message by teacherViewModel.message.collectAsState()

    LaunchedEffect(courseId) { coursesViewModel.loadCourse(courseId) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("0") }
    var coverImage by remember { mutableStateOf("") }
    var initialized by remember { mutableStateOf(false) }

    // Pre-fill the form once the existing course data arrives — only once,
    // so it doesn't stomp on whatever the teacher has already typed.
    LaunchedEffect(existingCourse) {
        val c = existingCourse
        if (c != null && !initialized) {
            title = c.title
            description = c.description ?: ""
            price = c.price.toString()
            coverImage = c.coverImage ?: ""
            initialized = true
        }
    }

    Scaffold(topBar = { EduTopBar("Редагувати курс", onBack = onBack) }) { padding ->
        if (isCourseLoading && !initialized) {
            LoadingScreen()
            return@Scaffold
        }
        Column(
            Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            OutlinedTextField(value = title, onValueChange = { title = it },
                label = { Text("Назва курсу (5–255 символів)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                supportingText = { Text("${title.length}/255") })
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = description, onValueChange = { description = it },
                label = { Text("Опис курсу") }, modifier = Modifier.fillMaxWidth().height(120.dp))
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = coverImage, onValueChange = { coverImage = it },
                label = { Text("URL обкладинки") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.Image, null) })
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = price, onValueChange = { price = it },
                label = { Text("Ціна (0 = безкоштовно)") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                leadingIcon = { Icon(Icons.Default.AttachMoney, null) })

            message?.let {
                Spacer(Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(
                    containerColor = if (it.contains("оновлено")) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.errorContainer)) { Text(it, Modifier.padding(12.dp)) }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    teacherViewModel.updateCourse(
                        courseId,
                        CreateCourseRequest(
                            title = title.trim(),
                            description = description.takeIf { it.isNotBlank() },
                            price = price.toDoubleOrNull() ?: 0.0,
                            coverImage = coverImage.takeIf { it.isNotBlank() }
                        ),
                        onDone = onUpdated
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving && title.length >= 5
            ) {
                if (isSaving) CircularProgressIndicator(Modifier.size(20.dp))
                else { Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("Зберегти зміни") }
            }
        }
    }
}