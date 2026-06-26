package com.eduplatform.ui.lessons

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
import com.eduplatform.data.models.Lesson
import com.eduplatform.ui.components.*

@Composable
fun LessonsListScreen(
    courseId: String,
    viewModel: LessonsViewModel,
    userRole: String,
    onBack: () -> Unit,
    onLessonClick: (String) -> Unit,
    onCreateLesson: () -> Unit,
    onEditLesson: (String) -> Unit = {}
) {
    val lessons by viewModel.lessons.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()

    LaunchedEffect(courseId) { viewModel.loadLessons(courseId) }

    LaunchedEffect(message) {
        if (message != null) { kotlinx.coroutines.delay(2000); viewModel.clearMessage() }
    }

    Scaffold(
        topBar = {
            EduTopBar(
                title = "Уроки курсу",
                onBack = onBack,
                actions = {
                    if (userRole == "teacher") {
                        IconButton(onClick = onCreateLesson) { Icon(Icons.Default.Add, "Додати урок") }
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            message?.let {
                Card(
                    Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) { Text(it, Modifier.padding(12.dp)) }
            }

            progress?.let { p ->
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Прогрес", style = MaterialTheme.typography.labelMedium)
                            Text("${p.percentage}%", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { p.percentage / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(4.dp))
                        Text("${p.completedLessons} з ${p.totalLessons} уроків",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline)
                    }
                }
            }

            when {
                isLoading -> LoadingScreen()
                error != null -> ErrorScreen(error!!, onRetry = { viewModel.loadLessons(courseId) })
                lessons.isEmpty() -> EmptyScreen("Уроків ще немає")
                else -> LazyColumn {
                    items(lessons) { lesson ->
                        val isCompleted = progress?.lessons?.find { it.lessonId == lesson.id }?.completed == true
                        LessonListItem(
                            lesson = lesson,
                            isCompleted = isCompleted,
                            userRole = userRole,
                            onClick = { onLessonClick(lesson.id) },
                            onEdit = if (userRole == "teacher") {{ onEditLesson(lesson.id) }} else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BlocksListScreen(
    courseId: String,
    viewModel: LessonsViewModel,
    userRole: String,
    onBack: () -> Unit,
    onLessonClick: (String) -> Unit,
    onLessonTestClick: (String) -> Unit,
    onCreateLesson: () -> Unit,
    onCreateLessonTest: (String) -> Unit,
    onEditLessonTest: (String, String) -> Unit = { _, _ -> },
    onEditLesson: (String) -> Unit = {}
) {
    val blocks by viewModel.blocks.collectAsState()
    val progress by viewModel.progress.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()

    LaunchedEffect(courseId) { viewModel.loadBlocks(courseId) }

    LaunchedEffect(message) {
        if (message != null) { kotlinx.coroutines.delay(2000); viewModel.clearMessage() }
    }

    Scaffold(
        topBar = {
            EduTopBar(
                title = "Курс: блоки",
                onBack = onBack,
                actions = {
                    if (userRole == "teacher") {
                        IconButton(onClick = onCreateLesson) { Icon(Icons.Default.Add, "Додати урок") }
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            message?.let {
                Card(
                    Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) { Text(it, Modifier.padding(12.dp)) }
            }

            progress?.let { p ->
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Прогрес по курсу", style = MaterialTheme.typography.labelMedium)
                            Text("${p.percentage}%", style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { p.percentage / 100f },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            if (p.isCompleted) "Курс завершено! 🎉"
                            else "${p.completedLessons} з ${p.totalLessons} уроків пройдено",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            when {
                isLoading -> LoadingScreen()
                error != null -> ErrorScreen(error!!, onRetry = { viewModel.loadBlocks(courseId) })
                blocks.isEmpty() -> EmptyScreen("Блоків ще немає")
                else -> LazyColumn {
                    items(blocks) { block ->
                        val lessonDone = progress?.blocks
                            ?.find { it.lesson.id == block.lesson.id }?.lessonCompleted == true
                        BlockListItem(
                            block = block,
                            lessonCompleted = lessonDone,
                            userRole = userRole,
                            onLessonClick = { onLessonClick(block.lesson.id) },
                            onTestClick = { onLessonTestClick(block.lesson.id) },
                            onCreateTest = { onCreateLessonTest(block.lesson.id) },
                            onEditTest = { block.test?.let { t -> onEditLessonTest(t.id, block.lesson.id) } },
                            onDelete = null,
                            onEditLesson = if (userRole == "teacher") {{ onEditLesson(block.lesson.id) }} else null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BlockListItem(
    block: com.eduplatform.data.models.CourseBlock,
    lessonCompleted: Boolean,
    userRole: String,
    onLessonClick: () -> Unit,
    onTestClick: () -> Unit,
    onCreateTest: () -> Unit,
    onEditTest: () -> Unit = {},
    onDelete: (() -> Unit)? = null,
    onEditLesson: (() -> Unit)? = null
) {
    val lesson = block.lesson
    val locked = lesson.locked && userRole == "student"

    // Для вчителя клік на урок = редагування; для студента = перегляд
    val effectiveLessonClick = if (userRole == "teacher" && onEditLesson != null) onEditLesson else onLessonClick

    Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp)) {
        Column(Modifier.padding(4.dp)) {
            ListItem(
                headlineContent = {
                    Text(lesson.title, fontWeight = if (lessonCompleted) FontWeight.Normal else FontWeight.SemiBold)
                },
                supportingContent = {
                    Text("#${lesson.order + 1} · ${lessonTypeLabel(lesson.type)}" + if (locked) " · заблоковано" else "")
                },
                leadingContent = {
                    Icon(
                        if (locked) Icons.Default.Lock
                        else if (lessonCompleted) Icons.Default.CheckCircle
                        else lessonTypeIcon(lesson.type),
                        contentDescription = null,
                        tint = if (locked) MaterialTheme.colorScheme.outline
                        else if (lessonCompleted) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline
                    )
                },
                trailingContent = if (userRole == "teacher") {
                    { Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline) }
                } else null,
                modifier = Modifier.clickable(enabled = !locked, onClick = effectiveLessonClick)
            )

            if (block.test != null) {
                HorizontalDivider()
                // Для вчителя клік на тест = редагування; для студента = проходження
                val testEffectiveClick = if (userRole == "teacher") onEditTest else onTestClick
                ListItem(
                    headlineContent = { Text(block.test.title, style = MaterialTheme.typography.bodyMedium) },
                    supportingContent = { Text("Прохідний бал: ${block.test.passingScore}%") },
                    leadingContent = {
                        Icon(
                            if (block.test.passed == true) Icons.Default.CheckCircle else Icons.Default.Quiz,
                            null,
                            tint = if (block.test.passed == true) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.secondary
                        )
                    },
                    trailingContent = if (userRole == "student" && !lessonCompleted) {
                        { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.outline) }
                    } else {
                        { Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline) }
                    },
                    modifier = Modifier.clickable(
                        enabled = userRole == "teacher" || lessonCompleted,
                        onClick = testEffectiveClick
                    )
                )
            } else if (userRole == "teacher") {
                HorizontalDivider()
                TextButton(onClick = onCreateTest, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Додати тест до блоку")
                }
            }
        }
    }
}

@Composable
fun LessonListItem(
    lesson: Lesson,
    isCompleted: Boolean,
    userRole: String,
    onClick: () -> Unit,
    onDelete: (() -> Unit)? = null,
    onEdit: (() -> Unit)? = null
) {
    // Для вчителя клік = редагування; для студента клік = перегляд
    val effectiveClick = if (userRole == "teacher" && onEdit != null) onEdit else onClick

    ListItem(
        headlineContent = {
            Text(lesson.title, fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.SemiBold)
        },
        supportingContent = { Text("#${lesson.order + 1} · ${lessonTypeLabel(lesson.type)}") },
        leadingContent = {
            Icon(
                if (isCompleted) Icons.Default.CheckCircle else lessonTypeIcon(lesson.type),
                contentDescription = null,
                tint = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            )
        },
        trailingContent = if (userRole == "teacher") {
            { Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.outline) }
        } else null,
        modifier = androidx.compose.ui.Modifier.clickable(onClick = effectiveClick)
    )
    HorizontalDivider()
}

fun lessonTypeLabel(type: String) = when (type) {
    "video" -> "Відео"
    "pdf" -> "PDF"
    else -> "Текст"
}

fun lessonTypeIcon(type: String) = when (type) {
    "video" -> Icons.Default.PlayCircle
    "pdf" -> Icons.Default.PictureAsPdf
    else -> Icons.Default.Article
}

fun openUrl(context: android.content.Context, url: String) {
    try {
        val fixedUrl = if (url.startsWith("http://") || url.startsWith("https://")) url else "https://$url"
        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(fixedUrl))
        context.startActivity(intent)
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Не вдалося відкрити посилання", android.widget.Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun LessonDetailScreen(
    lessonId: String,
    viewModel: LessonsViewModel,
    userRole: String,
    onBack: () -> Unit,
    onTakeTest: ((String) -> Unit)? = null,
    onEditLesson: ((String) -> Unit)? = null
) {
    val lesson by viewModel.lesson.collectAsState()
    val lessonTest by viewModel.lessonTest.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val message by viewModel.message.collectAsState()

    LaunchedEffect(lessonId) { viewModel.loadLesson(lessonId) }
    LaunchedEffect(message) {
        if (message != null) { kotlinx.coroutines.delay(2000); viewModel.clearMessage() }
    }

    Scaffold(
        topBar = {
            EduTopBar(
                title = lesson?.title ?: "Урок",
                onBack = onBack,
                actions = {
                    if (userRole == "teacher" && onEditLesson != null) {
                        IconButton(onClick = { onEditLesson(lessonId) }) {
                            Icon(Icons.Default.Edit, "Редагувати урок")
                        }
                    }
                }
            )
        }
    ) { padding ->
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
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(lessonTypeIcon(l.type), null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text(lessonTypeLabel(l.type), style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(12.dp))
                        Text("Урок #${l.order + 1}", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(l.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(Modifier.height(16.dp))

                    when (l.type) {
                        "text" -> {
                            if (l.content != null) {
                                Text(l.content, style = MaterialTheme.typography.bodyMedium)
                            } else {
                                EmptyScreen("Контент відсутній")
                            }
                        }
                        "video" -> {
                            if (l.videoUrl != null) {
                                val context = androidx.compose.ui.platform.LocalContext.current
                                Card(Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.PlayCircle, null,
                                            Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
                                        Spacer(Modifier.height(8.dp))
                                        Text("Відео урок", style = MaterialTheme.typography.titleMedium)
                                        Spacer(Modifier.height(4.dp))
                                        Text(l.videoUrl, style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.outline)
                                        Spacer(Modifier.height(12.dp))
                                        Button(onClick = { openUrl(context, l.videoUrl) }) {
                                            Icon(Icons.Default.OpenInBrowser, null)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Відкрити відео")
                                        }
                                    }
                                }
                            } else {
                                Text("URL відео не вказано", color = MaterialTheme.colorScheme.error)
                            }
                        }
                        "pdf" -> {
                            if (l.pdfUrl != null) {
                                val context = androidx.compose.ui.platform.LocalContext.current
                                Card(Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(Icons.Default.PictureAsPdf, null,
                                            Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
                                        Spacer(Modifier.height(8.dp))
                                        Text("PDF документ", style = MaterialTheme.typography.titleMedium)
                                        Spacer(Modifier.height(12.dp))
                                        Button(onClick = { openUrl(context, l.pdfUrl) }) {
                                            Icon(Icons.Default.OpenInBrowser, null)
                                            Spacer(Modifier.width(8.dp))
                                            Text("Відкрити PDF")
                                        }
                                    }
                                }
                            } else {
                                Text("URL PDF не вказано", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }

                    if (userRole == "student") {
                        val isCompleted by viewModel.isLessonCompleted.collectAsState()
                        Spacer(Modifier.height(24.dp))
                        message?.let {
                            Card(colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                                Text(it, Modifier.padding(12.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                        }
                        if (isCompleted) {
                            OutlinedButton(
                                onClick = { viewModel.markCompleted(lessonId, completed = false) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Icon(Icons.Default.CheckCircle, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Пройдено · скасувати")
                            }
                        } else {
                            Button(
                                onClick = { viewModel.markCompleted(lessonId) },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.CheckCircle, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Позначити як пройдений")
                            }
                        }

                        // Кнопка тесту — з'являється якщо до уроку є тест
                        if (lessonTest != null && onTakeTest != null) {
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { onTakeTest(lessonId) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = isCompleted,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary
                                )
                            ) {
                                Icon(Icons.Default.Quiz, null)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    if (isCompleted) "Пройти тест уроку"
                                    else "Тест · спочатку завершіть урок"
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
fun CreateLessonScreen(
    courseId: String,
    viewModel: LessonsViewModel,
    onBack: () -> Unit
) {
    LessonFormScreen(
        title = "Новий урок",
        viewModel = viewModel,
        initialLesson = null,
        successWord = "створено",
        onBack = onBack,
        onSubmit = { req -> viewModel.createLesson(courseId, req) {} }
    )
}

@Composable
fun EditLessonScreen(
    lessonId: String,
    courseId: String,
    viewModel: LessonsViewModel,
    onBack: () -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val lesson by viewModel.lesson.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(lessonId) { viewModel.loadLesson(lessonId) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Видалити урок?") },
            text = { Text("Цю дію не можна скасувати. Прогрес студентів по цьому уроку також буде видалено.") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete?.invoke() }) {
                    Text("Видалити", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = false }) { Text("Скасувати") } }
        )
    }

    if (lesson == null && isLoading) {
        Scaffold(topBar = { EduTopBar("Редагувати урок", onBack = onBack) }) { padding ->
            Box(Modifier.fillMaxSize().padding(padding)) { LoadingScreen() }
        }
        return
    }

    LessonFormScreen(
        title = "Редагувати урок",
        viewModel = viewModel,
        initialLesson = lesson,
        successWord = "оновлено",
        onBack = onBack,
        onSubmit = { req -> viewModel.updateLesson(lessonId, courseId, req) {} },
        onDelete = if (onDelete != null) { { showDeleteDialog = true } } else null
    )
}

@Composable
private fun LessonFormScreen(
    title: String,
    viewModel: LessonsViewModel,
    initialLesson: Lesson?,
    successWord: String,
    onBack: () -> Unit,
    onSubmit: (com.eduplatform.data.models.CreateLessonRequest) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    var titleField by remember { mutableStateOf(initialLesson?.title ?: "") }
    var type by remember { mutableStateOf(initialLesson?.type ?: "text") }
    var content by remember { mutableStateOf(initialLesson?.content ?: "") }
    var videoUrl by remember { mutableStateOf(initialLesson?.videoUrl ?: "") }
    var pdfUrl by remember { mutableStateOf(initialLesson?.pdfUrl ?: "") }

    LaunchedEffect(message) {
        if (message?.contains(successWord) == true) {
            kotlinx.coroutines.delay(500); onBack()
        }
    }

    Scaffold(topBar = {
        EduTopBar(title, onBack = onBack, actions = {
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Видалити урок", tint = MaterialTheme.colorScheme.error)
                }
            }
        })
    }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            OutlinedTextField(value = titleField, onValueChange = { titleField = it },
                label = { Text("Назва уроку (2–255 символів)") },
                modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(12.dp))

            Text("Тип уроку", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row {
                listOf("text" to "Текст", "video" to "Відео", "pdf" to "PDF").forEach { (v, l) ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        RadioButton(selected = type == v, onClick = { type = v })
                        Text(l, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            when (type) {
                "text" -> OutlinedTextField(value = content, onValueChange = { content = it },
                    label = { Text("Контент") }, modifier = Modifier.fillMaxWidth().height(200.dp),
                    maxLines = 10)
                "video" -> OutlinedTextField(value = videoUrl, onValueChange = { videoUrl = it },
                    label = { Text("URL відео") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    leadingIcon = { Icon(Icons.Default.PlayCircle, null) })
                "pdf" -> OutlinedTextField(value = pdfUrl, onValueChange = { pdfUrl = it },
                    label = { Text("URL PDF") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
                    leadingIcon = { Icon(Icons.Default.PictureAsPdf, null) })
            }

            message?.let {
                Spacer(Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(
                    containerColor = if (it.contains(successWord)) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.errorContainer)) {
                    Text(it, Modifier.padding(12.dp))
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    val req = com.eduplatform.data.models.CreateLessonRequest(
                        title = titleField.trim(),
                        type = type,
                        content = content.takeIf { type == "text" && it.isNotBlank() },
                        videoUrl = videoUrl.takeIf { type == "video" && it.isNotBlank() },
                        pdfUrl = pdfUrl.takeIf { type == "pdf" && it.isNotBlank() }
                    )
                    onSubmit(req)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && titleField.length >= 2
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(20.dp))
                else { Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("Зберегти урок") }
            }
        }
    }
}