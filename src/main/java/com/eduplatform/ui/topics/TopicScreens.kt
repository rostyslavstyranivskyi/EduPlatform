package com.eduplatform.ui.topics

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eduplatform.data.models.BlockTestInfo
import com.eduplatform.data.models.CourseBlock
import com.eduplatform.data.models.Lesson
import com.eduplatform.data.models.TopicWithLessons
import com.eduplatform.ui.components.*
import com.eduplatform.ui.lessons.LessonsViewModel
import com.eduplatform.ui.lessons.lessonTypeIcon
import com.eduplatform.ui.lessons.lessonTypeLabel

// ── Список тем курсу (студент і викладач) ──────────────────────────────────────

@Composable
fun TopicsListScreen(
    courseId: String,
    viewModel: TopicsViewModel,
    lessonsViewModel: LessonsViewModel,
    userRole: String,
    isOwnCourse: Boolean = false,
    onBack: () -> Unit,
    onTopicClick: (String) -> Unit,
    onTopicTest: (String) -> Unit,
    onLessonClick: (String) -> Unit = {},
    onLessonTestClick: (String) -> Unit = {},
    onManageTopics: () -> Unit = {},
    onCreateTopicTest: (String) -> Unit = {},
    onEditTopicTest: (String, String) -> Unit = { _, _ -> },
    onEnroll: (() -> Unit)? = null
) {
    val topics by viewModel.topics.collectAsState()
    val blocks by lessonsViewModel.blocks.collectAsState()
    val progress by lessonsViewModel.progress.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val needsEnrollment by lessonsViewModel.needsEnrollment.collectAsState()

    LaunchedEffect(courseId) {
        viewModel.loadTopics(courseId)
        lessonsViewModel.loadBlocks(courseId)
    }

    Scaffold(
        topBar = {
            EduTopBar(
                title = "Теми курсу",
                onBack = onBack,
                actions = {
                    // Кнопка управління темами лише для власника курсу
                    if (userRole == "teacher" && isOwnCourse) {
                        IconButton(onClick = onManageTopics) {
                            Icon(Icons.Default.Settings, "Управляти темами")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            val assignedLessonIds = topics.flatMap { it.lessons.map { l -> l.id } }.toSet()
            val unassignedBlocks = blocks.filter { it.lesson.topicId == null && it.lesson.id !in assignedLessonIds }

            when {
                isLoading -> LoadingScreen()
                needsEnrollment && onEnroll != null -> {
                    // Викладач або незаписаний студент — пропонуємо записатись
                    Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        Column(
                            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                "Для перегляду уроків потрібно записатись на курс",
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(onClick = onEnroll, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.School, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Записатись на курс")
                            }
                        }
                    }
                }
                error != null -> ErrorScreen(error!!, onRetry = { viewModel.loadTopics(courseId) })
                topics.isEmpty() && unassignedBlocks.isEmpty() -> EmptyScreen("Уроків ще немає")
                else -> {
                    LazyColumn {
                        items(topics) { topic ->
                            TopicCard(
                                topic = topic,
                                blocks = blocks,
                                progress = progress,
                                userRole = userRole,
                                onLessonClick = onLessonClick,
                                onLessonTestClick = onLessonTestClick,
                                onTestClick = { onTopicTest(topic.id) },
                                onHeaderClick = { onTopicClick(topic.id) },
                                isOwnCourse = isOwnCourse,
                                onCreateTopicTest = onCreateTopicTest,
                                onEditTopicTest = onEditTopicTest
                            )
                        }

                        if (unassignedBlocks.isNotEmpty()) {
                            item {
                                Card(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 6.dp)
                                ) {
                                    Column(Modifier.padding(4.dp)) {
                                        ListItem(
                                            headlineContent = {
                                                Text(
                                                    "Інші уроки",
                                                    fontWeight = FontWeight.SemiBold,
                                                    style = MaterialTheme.typography.titleMedium
                                                )
                                            },
                                            supportingContent = {
                                                Text("${unassignedBlocks.size} уроків без теми")
                                            },
                                            leadingContent = {
                                                Icon(
                                                    Icons.Default.FolderOpen,
                                                    null,
                                                    tint = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        )
                                        HorizontalDivider()
                                        unassignedBlocks.sortedBy { it.lesson.order }.forEach { block ->
                                            val lessonCompleted = progress?.lessons
                                                ?.find { it.lessonId == block.lesson.id }?.completed == true
                                            TopicLessonRow(
                                                lesson = block.lesson,
                                                isCompleted = lessonCompleted,
                                                onClick = { onLessonClick(block.lesson.id) }
                                            )
                                            if (block.test != null) {
                                                LessonTestRow(
                                                    test = block.test,
                                                    userRole = userRole,
                                                    onClick = { onLessonTestClick(block.lesson.id) }
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
}

@Composable
private fun TopicCard(
    topic: TopicWithLessons,
    blocks: List<CourseBlock>,
    progress: com.eduplatform.data.models.LessonProgressData?,
    userRole: String,
    onLessonClick: (String) -> Unit,
    onLessonTestClick: (String) -> Unit,
    onTestClick: () -> Unit,
    onHeaderClick: () -> Unit,
    isOwnCourse: Boolean = false,
    onCreateTopicTest: (String) -> Unit = {},
    onEditTopicTest: (String, String) -> Unit = { _, _ -> }
) {
    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Column(Modifier.padding(4.dp)) {
            ListItem(
                headlineContent = {
                    Text(topic.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.titleMedium)
                },
                supportingContent = {
                    val lessonsLabel = "${topic.lessons.size} уроків"
                    Text(topic.description?.let { "$lessonsLabel · $it" } ?: lessonsLabel)
                },
                leadingContent = {
                    Icon(Icons.Default.FolderOpen, null, tint = MaterialTheme.colorScheme.primary)
                },
                modifier = Modifier.clickable(onClick = onHeaderClick)
            )

            if (topic.lessons.isNotEmpty()) {
                HorizontalDivider()
                topic.lessons.sortedBy { it.order }.forEach { lesson ->
                    val isCompleted = progress?.lessons
                        ?.find { it.lessonId == lesson.id }?.completed == true
                    TopicLessonRow(
                        lesson = lesson,
                        isCompleted = isCompleted,
                        onClick = { onLessonClick(lesson.id) }
                    )
                    // Показуємо тест до уроку, якщо він є
                    val lessonTest = blocks.find { it.lesson.id == lesson.id }?.test
                    if (lessonTest != null) {
                        LessonTestRow(
                            test = lessonTest,
                            userRole = userRole,
                            onClick = { onLessonTestClick(lesson.id) }
                        )
                    }
                }
            }

            if (topic.test != null) {
                HorizontalDivider()
                ListItem(
                    headlineContent = { Text(topic.test.title, style = MaterialTheme.typography.bodyMedium) },
                    supportingContent = { Text("Тест теми · прохідний бал: ${topic.test.passingScore}%") },
                    leadingContent = {
                        Icon(
                            if (topic.test.passed == true) Icons.Default.CheckCircle else Icons.Default.Quiz,
                            null,
                            tint = if (topic.test.passed == true) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.secondary
                        )
                    },
                    trailingContent = if (userRole == "teacher" && isOwnCourse) {
                        {
                            IconButton(onClick = { onEditTopicTest(topic.test.id, topic.id) }) {
                                Icon(Icons.Default.Edit, "Редагувати тест", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    } else null,
                    modifier = Modifier.clickable(onClick = onTestClick)
                )
            } else if (userRole == "teacher" && isOwnCourse) {
                HorizontalDivider()
                TextButton(onClick = { onCreateTopicTest(topic.id) }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Додати тест до теми")
                }
            }
        }
    }
}

@Composable
private fun TopicLessonRow(
    lesson: Lesson,
    isCompleted: Boolean = false,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(lesson.title, style = MaterialTheme.typography.bodyMedium) },
        supportingContent = { Text(lessonTypeLabel(lesson.type), style = MaterialTheme.typography.labelSmall) },
        leadingContent = {
            if (isCompleted) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Пройдено",
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            } else {
                Icon(
                    lessonTypeIcon(lesson.type),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
            }
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@Composable
private fun LessonTestRow(
    test: BlockTestInfo,
    userRole: String,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = {
            Text(
                test.title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        supportingContent = {
            Text(
                "Тест до уроку · прохідний бал: ${test.passingScore}%",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline
            )
        },
        leadingContent = {
            Icon(
                if (test.passed == true) Icons.Default.CheckCircle else Icons.Default.Quiz,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = if (test.passed == true) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.secondary
            )
        },
        modifier = Modifier
            .padding(start = 16.dp)
            .clickable(onClick = onClick)
    )
}

// ── Деталі теми ─────────────────────────────────────────────────────────────────

@Composable
fun TopicDetailScreen(
    topicId: String,
    courseId: String,
    viewModel: TopicsViewModel,
    lessonsViewModel: LessonsViewModel,
    onBack: () -> Unit,
    onLessonClick: (String) -> Unit,
    onTopicTest: (String) -> Unit,
    userRole: String = "student",
    onCreateTopicTest: (String) -> Unit = {},
    onEditTopicTest: (String, String) -> Unit = { _, _ -> }
) {
    val topics by viewModel.topics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val needsEnrollment by lessonsViewModel.needsEnrollment.collectAsState()
    val topic = topics.find { it.id == topicId }

    LaunchedEffect(courseId) {
        if (topics.none { it.id == topicId }) viewModel.loadTopics(courseId)
    }

    Scaffold(topBar = { EduTopBar(title = topic?.title ?: "Тема", onBack = onBack) }) { padding ->
        when {
            isLoading && topic == null -> LoadingScreen()
            error != null && topic == null -> ErrorScreen(error!!, onRetry = { viewModel.loadTopics(courseId) })
            topic == null -> EmptyScreen("Тему не знайдено")
            else -> Column(
                Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp)
            ) {
                topic.description?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline)
                    Spacer(Modifier.height(16.dp))
                }

                Text("Уроки", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                if (topic.lessons.isEmpty()) {
                    Text("У цій темі ще немає уроків", color = MaterialTheme.colorScheme.outline)
                } else {
                    topic.lessons.sortedBy { it.order }.forEach { lesson ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                            TopicLessonRow(lesson = lesson, onClick = { onLessonClick(lesson.id) })
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                if (topic.test != null) {
                    Button(onClick = { onTopicTest(topic.id) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Quiz, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Пройти тест теми")
                    }
                    if (userRole == "teacher") {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { onEditTopicTest(topic.test.id, topic.id) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Edit, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Редагувати тест теми")
                        }
                    }
                } else if (userRole == "teacher") {
                    OutlinedButton(
                        onClick = { onCreateTopicTest(topic.id) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Створити тест теми")
                    }
                }
            }
        }
    }
}

// ── Управління темами (викладач) ──────────────────────────────────────────────

@Composable
fun ManageTopicsScreen(
    courseId: String,
    viewModel: TopicsViewModel,
    lessonsViewModel: LessonsViewModel,
    onBack: () -> Unit,
    onAssignLessons: (String) -> Unit,
    onCreateTopicTest: (String) -> Unit = {},
    onEditTopicTest: (String, String) -> Unit = { _, _ -> }
) {
    val topics by viewModel.topics.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val needsEnrollment by lessonsViewModel.needsEnrollment.collectAsState()
    val message by viewModel.message.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var topicToEdit by remember { mutableStateOf<TopicWithLessons?>(null) }
    var topicToDelete by remember { mutableStateOf<TopicWithLessons?>(null) }

    LaunchedEffect(courseId) { viewModel.loadTopics(courseId) }
    LaunchedEffect(message) {
        if (message != null) { kotlinx.coroutines.delay(2000); viewModel.clearMessage() }
    }

    if (showCreateDialog) {
        CreateTopicDialog(
            onDismiss = { showCreateDialog = false },
            onConfirm = { title, description ->
                viewModel.createTopic(courseId, title, description)
                showCreateDialog = false
            }
        )
    }

    topicToEdit?.let { topic ->
        EditTopicDialog(
            initialTitle = topic.title,
            initialDescription = topic.description,
            onDismiss = { topicToEdit = null },
            onConfirm = { title, description ->
                viewModel.updateTopic(topic.id, courseId, title, description)
                topicToEdit = null
            }
        )
    }

    topicToDelete?.let { topic ->
        AlertDialog(
            onDismissRequest = { topicToDelete = null },
            title = { Text("Видалити тему?") },
            text = {
                Text(
                    "Тему «${topic.title}» буде видалено. Уроки не видаляться, лише " +
                            "від'єднаються від теми. Тест теми (якщо є) буде видалено разом з нею."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteTopic(topic.id, courseId)
                    topicToDelete = null
                }) { Text("Видалити", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { topicToDelete = null }) { Text("Скасувати") } }
        )
    }

    Scaffold(
        topBar = { EduTopBar(title = "Управління темами", onBack = onBack) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showCreateDialog = true }) {
                Icon(Icons.Default.Add, "Додати тему")
            }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            message?.let {
                Card(
                    Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) { Text(it, Modifier.padding(12.dp)) }
            }

            when {
                isLoading -> LoadingScreen()
                error != null -> ErrorScreen(error!!, onRetry = { viewModel.loadTopics(courseId) })
                topics.isEmpty() -> EmptyScreen("Тем ще немає. Натисніть + щоб додати першу.")
                else -> LazyColumn {
                    items(topics) { topic ->
                        Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                            ListItem(
                                headlineContent = { Text(topic.title, fontWeight = FontWeight.SemiBold) },
                                supportingContent = {
                                    Text(
                                        "${topic.lessons.size} уроків" +
                                                if (topic.test != null) " · тест є" else " · без тесту"
                                    )
                                },
                                leadingContent = {
                                    Icon(Icons.Default.FolderOpen, null, tint = MaterialTheme.colorScheme.primary)
                                },
                                trailingContent = {
                                    Row {
                                        // Тест: іконка Quiz для редагування/додавання тесту
                                        if (topic.test != null) {
                                            IconButton(onClick = { onEditTopicTest(topic.test.id, topic.id) }, modifier = Modifier.size(36.dp)) {
                                                Icon(Icons.Default.Quiz, "Редагувати тест", tint = MaterialTheme.colorScheme.secondary)
                                            }
                                        } else {
                                            IconButton(onClick = { onCreateTopicTest(topic.id) }, modifier = Modifier.size(36.dp)) {
                                                Icon(Icons.Default.Quiz, "Додати тест", tint = MaterialTheme.colorScheme.outline)
                                            }
                                        }
                                        // Призначити уроки
                                        IconButton(onClick = { onAssignLessons(topic.id) }, modifier = Modifier.size(36.dp)) {
                                            Icon(Icons.Default.PlaylistAdd, "Призначити уроки")
                                        }
                                        // Видалити тему
                                        IconButton(onClick = { topicToDelete = topic }, modifier = Modifier.size(36.dp)) {
                                            Icon(Icons.Default.Delete, "Видалити тему", tint = MaterialTheme.colorScheme.error)
                                        }
                                        // Шеврон = підказка що картка клікабельна
                                        Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline)
                                    }
                                },
                                modifier = Modifier.clickable { topicToEdit = topic }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CreateTopicDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Нова тема") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it },
                    label = { Text("Назва теми") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text("Опис (необов'язково)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title, description.takeIf { it.isNotBlank() }) },
                enabled = title.isNotBlank()
            ) { Text("Створити") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Скасувати") } }
    )
}

@Composable
private fun EditTopicDialog(
    initialTitle: String,
    initialDescription: String?,
    onDismiss: () -> Unit,
    onConfirm: (title: String, description: String?) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Редагувати тему") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it },
                    label = { Text("Назва теми") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it },
                    label = { Text("Опис (необов'язково)") }, modifier = Modifier.fillMaxWidth())
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(title, description.takeIf { it.isNotBlank() }) },
                enabled = title.isNotBlank()
            ) { Text("Зберегти") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Скасувати") } }
    )
}

// ── Призначення уроків до теми ────────────────────────────────────────────────

@Composable
fun AssignLessonsToTopicScreen(
    topicId: String,
    courseId: String,
    topicsViewModel: TopicsViewModel,
    lessonsViewModel: com.eduplatform.ui.lessons.LessonsViewModel,
    onBack: () -> Unit
) {
    val topics by topicsViewModel.topics.collectAsState()
    val allLessons by lessonsViewModel.lessons.collectAsState()
    val isLoading by topicsViewModel.isLoading.collectAsState()
    val message by topicsViewModel.message.collectAsState()

    val topic = topics.find { it.id == topicId }
    val selected = remember { mutableStateListOf<String>() }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(courseId) {
        if (topics.isEmpty()) topicsViewModel.loadTopics(courseId)
        lessonsViewModel.loadLessons(courseId)
    }

    LaunchedEffect(topic) {
        if (topic != null && !initialized) {
            selected.addAll(topic.lessons.map { it.id })
            initialized = true
        }
    }

    LaunchedEffect(message) {
        if (message?.contains("призначено") == true) { kotlinx.coroutines.delay(500); onBack() }
    }

    Scaffold(topBar = { EduTopBar(title = "Уроки теми: ${topic?.title ?: ""}", onBack = onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Text(
                "Виберіть уроки, які належать до цієї теми",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(16.dp)
            )
            if (allLessons.isEmpty()) {
                EmptyScreen("У курсі ще немає уроків")
            } else {
                LazyColumn(Modifier.weight(1f)) {
                    items(allLessons.sortedBy { it.order }) { lesson ->
                        val isSelected = selected.contains(lesson.id)
                        ListItem(
                            headlineContent = { Text(lesson.title) },
                            supportingContent = {
                                Text(
                                    if (lesson.topicId != null && lesson.topicId != topicId) "Вже в іншій темі"
                                    else lessonTypeLabel(lesson.type)
                                )
                            },
                            leadingContent = {
                                Checkbox(checked = isSelected, onCheckedChange = { checked ->
                                    if (checked) selected.add(lesson.id) else selected.remove(lesson.id)
                                })
                            },
                            modifier = Modifier.clickable {
                                if (isSelected) selected.remove(lesson.id) else selected.add(lesson.id)
                            }
                        )
                        HorizontalDivider()
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { topicsViewModel.assignLessons(topicId, selected.toList(), courseId) },
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                enabled = !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(20.dp))
                else { Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("Зберегти") }
            }
        }
    }
}