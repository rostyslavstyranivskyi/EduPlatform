package com.eduplatform.ui.tests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.eduplatform.data.models.*
import com.eduplatform.ui.components.*

@Composable
fun TestScreen(
    courseId: String,
    viewModel: TestViewModel,
    onBack: () -> Unit
) {
    val test by viewModel.test.collectAsState()
    val result by viewModel.result.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(courseId) { viewModel.loadTest(courseId) }

    Scaffold(topBar = { EduTopBar(title = test?.title ?: "Тест", onBack = onBack) }) { padding ->
        when {
            isLoading -> LoadingScreen()
            error != null -> ErrorScreen(error!!, onRetry = { viewModel.loadTest(courseId) })
            result != null -> TestResultScreen(result = result!!, passingScore = test?.passingScore ?: 70,
                onRetry = { viewModel.resetResult(); viewModel.loadTest(courseId) }, onBack = onBack,
                scaffoldPadding = padding)
            test == null -> EmptyScreen("Тест не знайдено")
            else -> TestQuestionsScreen(
                test = test!!,
                modifier = Modifier.padding(padding),
                onSubmit = { answers -> viewModel.submitTest(test!!.id, answers) }
            )
        }
    }
}

@Composable
fun TestQuestionsScreen(
    test: Test,
    modifier: Modifier = Modifier,
    onSubmit: (List<Int>) -> Unit
) {
    val answers = remember { mutableStateListOf<Int>().also { list ->
        repeat(test.questions.size) { list.add(-1) }
    }}
    var showConfirm by remember { mutableStateOf(false) }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            title = { Text("Здати тест?") },
            text = {
                val unanswered = answers.count { it == -1 }
                Text(if (unanswered > 0) "Ви не відповіли на $unanswered питань. Продовжити?"
                else "Ви відповіли на всі питання. Здати тест?")
            },
            confirmButton = {
                Button(onClick = { showConfirm = false; onSubmit(answers.toList()) }) { Text("Здати") }
            },
            dismissButton = {
                TextButton(onClick = { showConfirm = false }) { Text("Продовжити") }
            }
        )
    }

    Column(modifier.fillMaxSize()) {
        Card(Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Quiz, null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(test.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Прохідний бал: ${test.passingScore}% · ${test.questions.size} питань",
                        style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        LazyColumn(Modifier.weight(1f)) {
            itemsIndexed(test.questions) { idx, question ->
                Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("${idx + 1}. ${question.question}",
                            style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(8.dp))
                        question.options.forEachIndexed { optIdx, option ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = answers[idx] == optIdx,
                                    onClick = { answers[idx] = optIdx }
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(option, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
            item { Spacer(Modifier.height(80.dp)) }
        }

        HorizontalDivider()
        Column(Modifier.padding(16.dp)) {
            val answered = answers.count { it != -1 }
            LinearProgressIndicator(
                progress = { answered.toFloat() / test.questions.size },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
            Text("$answered / ${test.questions.size} відповідей",
                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = { showConfirm = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Send, null)
                Spacer(Modifier.width(8.dp))
                Text("Здати тест")
            }
        }
    }
}


@Composable
fun TestResultScreen(
    result: TestResult,
    passingScore: Int,
    onRetry: () -> Unit,
    onBack: () -> Unit,
    scaffoldPadding: PaddingValues = PaddingValues()
) {
    Column(
        Modifier.fillMaxSize().padding(scaffoldPadding).verticalScroll(rememberScrollState()).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))
        Icon(
            if (result.passed) Icons.Default.EmojiEvents else Icons.Default.SentimentDissatisfied,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = if (result.passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(16.dp))
        Text(
            if (result.passed) "Тест пройдено! 🎉" else "Тест не пройдено",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        Text("Ваш результат", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
        Spacer(Modifier.height(4.dp))
        Text("${result.score}%", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold,
            color = if (result.passed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
        Text("Прохідний бал: $passingScore%", style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.outline)

        Spacer(Modifier.height(24.dp))
        Card(Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceAround) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${result.correctCount}", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Правильно", style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${result.totalQuestions - result.correctCount}", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    Text("Неправильно", style = MaterialTheme.typography.labelSmall)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("${result.totalQuestions}", style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    Text("Всього", style = MaterialTheme.typography.labelSmall)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onRetry, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text("Спробувати знову")
            }
            Button(onClick = onBack, modifier = Modifier.weight(1f)) {
                Icon(Icons.Default.Done, null)
                Spacer(Modifier.width(8.dp))
                Text("Готово")
            }
        }
    }
}

@Composable
fun LessonTestScreen(
    lessonId: String,
    viewModel: TestViewModel,
    onBack: () -> Unit
) {
    val test by viewModel.test.collectAsState()
    val result by viewModel.result.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(lessonId) { viewModel.loadLessonTest(lessonId) }

    Scaffold(topBar = { EduTopBar(title = test?.title ?: "Тест блоку", onBack = onBack) }) { padding ->
        when {
            isLoading -> LoadingScreen()
            error != null -> ErrorScreen(error!!, onRetry = { viewModel.loadLessonTest(lessonId) })
            result != null -> TestResultScreen(result = result!!, passingScore = test?.passingScore ?: 70,
                onRetry = { viewModel.resetResult(); viewModel.loadLessonTest(lessonId) }, onBack = onBack,
                scaffoldPadding = padding)
            test == null -> EmptyScreen("Тест не знайдено")
            else -> TestQuestionsScreen(
                test = test!!,
                modifier = Modifier.padding(padding),
                onSubmit = { answers -> viewModel.submitTest(test!!.id, answers) }
            )
        }
    }
}

@Composable
fun TopicTestScreen(
    topicId: String,
    viewModel: TestViewModel,
    onBack: () -> Unit
) {
    val test by viewModel.test.collectAsState()
    val result by viewModel.result.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(topicId) { viewModel.loadTopicTest(topicId) }

    Scaffold(topBar = { EduTopBar(title = test?.title ?: "Тест теми", onBack = onBack) }) { padding ->
        when {
            isLoading -> LoadingScreen()
            error != null -> ErrorScreen(error!!, onRetry = { viewModel.loadTopicTest(topicId) })
            result != null -> TestResultScreen(result = result!!, passingScore = test?.passingScore ?: 70,
                onRetry = { viewModel.resetResult(); viewModel.loadTopicTest(topicId) }, onBack = onBack,
                scaffoldPadding = padding)
            test == null -> EmptyScreen("Тест не знайдено")
            else -> TestQuestionsScreen(
                test = test!!,
                modifier = Modifier.padding(padding),
                onSubmit = { answers -> viewModel.submitTest(test!!.id, answers) }
            )
        }
    }
}

@Composable
fun CreateTestScreen(
    courseId: String,
    viewModel: TestViewModel,
    onBack: () -> Unit,
    isLessonTest: Boolean = false,
    isTopicTest: Boolean = false
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    var testTitle by remember { mutableStateOf("") }
    var passingScore by remember { mutableStateOf("70") }
    val questions = remember { mutableStateListOf<MutableQuestionState>() }

    LaunchedEffect(message) {
        if (message?.contains("створено") == true) { kotlinx.coroutines.delay(500); onBack() }
    }

    Scaffold(topBar = { EduTopBar("Створити тест", onBack = onBack) }) { padding ->
        Column(
            Modifier.fillMaxSize().padding(padding)
                .verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            OutlinedTextField(value = testTitle, onValueChange = { testTitle = it },
                label = { Text("Назва тесту") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = passingScore, onValueChange = { passingScore = it },
                label = { Text("Прохідний бал (0–100)%") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            Spacer(Modifier.height(16.dp))

            questions.forEachIndexed { qi, q ->
                Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Питання ${qi + 1}", style = MaterialTheme.typography.labelLarge,
                                modifier = Modifier.weight(1f))
                            IconButton(onClick = { questions.removeAt(qi) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        OutlinedTextField(value = q.question, onValueChange = { q.question = it },
                            label = { Text("Текст питання") }, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(8.dp))
                        q.options.forEachIndexed { oi, opt ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(selected = q.correctIndex == oi, onClick = { q.correctIndex = oi })
                                OutlinedTextField(value = opt, onValueChange = { q.options[oi] = it },
                                    label = { Text("Варіант ${oi + 1}") },
                                    modifier = Modifier.weight(1f), singleLine = true)
                                if (q.options.size > 2) {
                                    IconButton(onClick = {
                                        if (q.correctIndex == oi) q.correctIndex = 0
                                        q.options.removeAt(oi)
                                    }, modifier = Modifier.size(32.dp)) {
                                        Icon(Icons.Default.Close, null)
                                    }
                                }
                            }
                        }
                        if (q.options.size < 6) {
                            TextButton(onClick = { q.options.add("") }) {
                                Icon(Icons.Default.Add, null); Text("Додати варіант")
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { questions.add(MutableQuestionState()) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Додати питання")
            }

            message?.let {
                Spacer(Modifier.height(8.dp))
                Card(colors = CardDefaults.cardColors(
                    containerColor = if (it.contains("створено")) MaterialTheme.colorScheme.secondaryContainer
                    else MaterialTheme.colorScheme.errorContainer
                )) { Text(it, Modifier.padding(12.dp)) }
            }

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = {
                    val qs = questions.map { q ->
                        TestQuestion(q.question, q.options.toList(), q.correctIndex)
                    }
                    val req = CreateTestRequest(testTitle.trim(), qs, passingScore.toIntOrNull() ?: 70)
                    if (isTopicTest) viewModel.createTopicTest(courseId, req) {}
                    else if (isLessonTest) viewModel.createLessonTest(courseId, req) {}
                    else viewModel.createTest(courseId, req) {}
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && testTitle.isNotBlank() && questions.isNotEmpty()
                        && questions.all { it.question.isNotBlank() && it.options.size >= 2 && it.options.all { o -> o.isNotBlank() } }
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(20.dp))
                else { Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("Зберегти тест") }
            }
        }
    }
}

class MutableQuestionState {
    var question by mutableStateOf("")
    var options = mutableStateListOf("", "")
    var correctIndex by mutableStateOf(0)
}

/**
 * Екран редагування тесту.
 * [testId] — ID тесту (для курсового та уроку-блоку).
 * [topicId] — ID теми (для тесту теми, використовує PATCH /tests/topic/:topicId).
 * Передається лише один з них, залежно від типу тесту.
 */
@Composable
fun EditTestScreen(
    viewModel: TestViewModel,
    onBack: () -> Unit,
    testId: String? = null,
    lessonId: String? = null,
    topicId: String? = null
) {
    val existingTest by viewModel.test.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()

    // Завантажуємо тест при відкритті екрану
    LaunchedEffect(testId, lessonId, topicId) {
        when {
            lessonId != null -> viewModel.loadLessonTest(lessonId)
            topicId != null -> viewModel.loadTopicTest(topicId)
            // Для тесту курсу testId є courseId — використовуємо loadTest
            testId != null -> viewModel.loadTest(testId)
        }
    }

    var initialized by remember { mutableStateOf(false) }
    var testTitle by remember { mutableStateOf("") }
    var passingScore by remember { mutableStateOf("70") }
    val questions = remember { mutableStateListOf<MutableQuestionState>() }

    // Ініціалізуємо форму даними з вже завантаженого тесту
    LaunchedEffect(existingTest) {
        val t = existingTest ?: return@LaunchedEffect
        if (!initialized) {
            testTitle = t.title
            passingScore = t.passingScore.toString()
            questions.clear()
            t.questions.forEach { q ->
                questions.add(MutableQuestionState().also { ms ->
                    ms.question = q.question
                    ms.options.clear()
                    ms.options.addAll(q.options)
                    ms.correctIndex = q.correctIndex ?: 0
                })
            }
            initialized = true
        }
    }

    LaunchedEffect(message) {
        if (message?.contains("оновлено") == true) {
            kotlinx.coroutines.delay(500)
            onBack()
        }
    }

    Scaffold(topBar = { EduTopBar("Редагувати тест", onBack = onBack) }) { padding ->
        when {
            isLoading && !initialized -> LoadingScreen()
            else -> Column(
                Modifier.fillMaxSize().padding(padding)
                    .verticalScroll(rememberScrollState()).padding(16.dp)
            ) {
                OutlinedTextField(
                    value = testTitle, onValueChange = { testTitle = it },
                    label = { Text("Назва тесту") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = passingScore, onValueChange = { passingScore = it },
                    label = { Text("Прохідний бал (0–100)%") }, modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Spacer(Modifier.height(16.dp))

                questions.forEachIndexed { qi, q ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Питання ${qi + 1}", style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.weight(1f))
                                IconButton(onClick = { questions.removeAt(qi) }, modifier = Modifier.size(32.dp)) {
                                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            OutlinedTextField(value = q.question, onValueChange = { q.question = it },
                                label = { Text("Текст питання") }, modifier = Modifier.fillMaxWidth())
                            Spacer(Modifier.height(8.dp))
                            q.options.forEachIndexed { oi, opt ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(selected = q.correctIndex == oi, onClick = { q.correctIndex = oi })
                                    OutlinedTextField(value = opt, onValueChange = { q.options[oi] = it },
                                        label = { Text("Варіант ${oi + 1}") },
                                        modifier = Modifier.weight(1f), singleLine = true)
                                    if (q.options.size > 2) {
                                        IconButton(onClick = {
                                            if (q.correctIndex == oi) q.correctIndex = 0
                                            q.options.removeAt(oi)
                                        }, modifier = Modifier.size(32.dp)) {
                                            Icon(Icons.Default.Close, null)
                                        }
                                    }
                                }
                            }
                            if (q.options.size < 6) {
                                TextButton(onClick = { q.options.add("") }) {
                                    Icon(Icons.Default.Add, null); Text("Додати варіант")
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { questions.add(MutableQuestionState()) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, null); Spacer(Modifier.width(8.dp)); Text("Додати питання")
                }

                message?.let {
                    Spacer(Modifier.height(8.dp))
                    Card(colors = CardDefaults.cardColors(
                        containerColor = if (it.contains("оновлено")) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    )) { Text(it, Modifier.padding(12.dp)) }
                }

                Spacer(Modifier.height(20.dp))
                val isValid = testTitle.isNotBlank() && questions.isNotEmpty()
                        && questions.all { it.question.isNotBlank() && it.options.size >= 2 && it.options.all { o -> o.isNotBlank() } }

                Button(
                    onClick = {
                        val qs = questions.map { q ->
                            TestQuestion(q.question, q.options.toList(), q.correctIndex)
                        }
                        val req = CreateTestRequest(testTitle.trim(), qs, passingScore.toIntOrNull() ?: 70)
                        val currentTestId = existingTest?.id
                        when {
                            topicId != null -> viewModel.updateTopicTest(topicId, req) {}
                            currentTestId != null -> viewModel.updateTest(currentTestId, req) {}
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading && isValid
                ) {
                    if (isLoading) CircularProgressIndicator(Modifier.size(20.dp))
                    else { Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("Зберегти зміни") }
                }
            }
        }
    }
}