package com.eduplatform.ui.courses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.eduplatform.data.models.*
import com.eduplatform.ui.components.*
import com.eduplatform.ui.lessons.lessonTypeLabel
import com.eduplatform.ui.theme.AppTheme

@Composable
fun CourseListScreen(
    viewModel: CoursesViewModel,
    userRole: String,
    onCourseClick: (String) -> Unit,
    onCreateCourse: () -> Unit
) {
    val courses by viewModel.courses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val selectedPrice by viewModel.selectedPrice.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val totalPages by viewModel.totalPages.collectAsState()

    Scaffold(
        topBar = {
            EduTopBar(
                title = "Каталог курсів",
                actions = {
                    if (userRole == "teacher") {
                        IconButton(onClick = onCreateCourse) {
                            Icon(Icons.Default.Add, "Створити курс")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            // Search
            OutlinedTextField(
                value = query,
                onValueChange = viewModel::onSearchQuery,
                placeholder = { Text("Пошук курсів (мін. 3 символи)...") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                leadingIcon = { Icon(Icons.Default.Search, null) },
                singleLine = true,
                trailingIcon = if (query.isNotEmpty()) {
                    { IconButton(onClick = { viewModel.onSearchQuery("") }) { Icon(Icons.Default.Close, null) } }
                } else null
            )

            // Filters row
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    listOf("any" to "Всі", "free" to "Безкоштовні", "paid" to "Платні").forEach { (v, l) ->
                        FilterChip(
                            selected = selectedPrice == v,
                            onClick = { viewModel.setPrice(v) },
                            label = { Text(l) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            }
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    listOf(
                        "newest" to "Новіші", "popular" to "Популярні",
                        "price_asc" to "Ціна ↑", "price_desc" to "Ціна ↓"
                    ).forEach { (v, l) ->
                        FilterChip(
                            selected = selectedSort == v,
                            onClick = { viewModel.setSort(v) },
                            label = { Text(l) },
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            }

            when {
                isLoading -> LoadingScreen()
                error != null -> ErrorScreen(error!!, onRetry = viewModel::loadCourses)
                courses.isEmpty() -> EmptyScreen("Курсів не знайдено")
                else -> {
                    LazyColumn(Modifier.weight(1f)) {
                        items(courses) { course ->
                            CourseCard(course = course, onClick = { onCourseClick(course.id) })
                        }
                    }
                    // Pagination
                    if (totalPages > 1) {
                        Row(
                            Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = viewModel::prevPage, enabled = currentPage > 1) {
                                Icon(Icons.Default.ChevronLeft, null)
                            }
                            Text("$currentPage / $totalPages", style = MaterialTheme.typography.bodyMedium)
                            IconButton(onClick = viewModel::nextPage, enabled = currentPage < totalPages) {
                                Icon(Icons.Default.ChevronRight, null)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CourseCard(course: Course, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),

        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        if (course.coverImage != null) {
            AsyncImage(
                model = course.coverImage,
                contentDescription = null,
                modifier = Modifier.fillMaxWidth().height(140.dp),
                contentScale = ContentScale.Crop
            )
        }
        Column(Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(course.title, style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                StatusBadge(course.status)
            }
            course.description?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline, maxLines = 2)
            }
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                course.teacher?.let { t ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.outline)
                        Spacer(Modifier.width(4.dp))
                        Text("${t.name} ${t.surname}", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline)
                    }
                }
                PriceTag(course.price)
            }
            if (course.enrolledCount > 0 || course.lessonsCount > 0) {
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (course.lessonsCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PlayCircle, null, Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.width(4.dp))
                            Text("${course.lessonsCount} уроків", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline)
                        }
                    }
                    if (course.enrolledCount > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.People, null, Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.outline)
                            Spacer(Modifier.width(4.dp))
                            Text("${course.enrolledCount} студентів", style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CourseDetailScreen(
    courseId: String,
    viewModel: CoursesViewModel,
    userRole: String,
    currentUserId: String? = null,
    onBack: () -> Unit,
    onViewLessons: (String) -> Unit,
    onTakeTest: (String) -> Unit,
    onEditCourse: (String) -> Unit,
    onManageLessons: (String) -> Unit,
    onCreateTest: (String) -> Unit,
    lessonsViewModel: com.eduplatform.ui.lessons.LessonsViewModel? = null,
    testViewModel: com.eduplatform.ui.tests.TestViewModel? = null,
    teacherViewModel: com.eduplatform.ui.teacher.TeacherViewModel? = null,
    onOpenLesson: (String) -> Unit = {},
    onOpenLessonTest: (String) -> Unit = {},
    onCreateLessonTest: (String) -> Unit = {},
    onCreateLesson: (String) -> Unit = {},
    onManageTopics: (String) -> Unit = {}
) {
    val course by viewModel.course.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val enrollMessage by viewModel.enrollMessage.collectAsState()
    val isEnrolled by viewModel.isEnrolled.collectAsState()

    LaunchedEffect(courseId) { viewModel.loadCourse(courseId) }

    // Для власних курсів — екран управління; для чужих — перегляд як студент.
    // Перевіряємо через TeacherViewModel.myCourses (список /courses/my) —
    // надійніше ніж порівнювати teacherId, бо userId може бути null під час старту.
    if (userRole == "teacher" &&
        lessonsViewModel != null && testViewModel != null && teacherViewModel != null) {

        val myCourses by teacherViewModel.myCourses.collectAsState()
        val myCoursesLoading by teacherViewModel.isLoading.collectAsState()

        // Завантажуємо список власних курсів якщо ще не завантажено
        LaunchedEffect(Unit) {
            if (myCourses.isEmpty()) teacherViewModel.loadMyCourses()
        }

        val isOwnCourse = myCourses.any { it.id == courseId }

        when {
            // Список ще завантажується — чекаємо
            myCoursesLoading && myCourses.isEmpty() -> {
                Scaffold(topBar = { EduTopBar("Курс", onBack = onBack) }) {
                    com.eduplatform.ui.components.LoadingScreen()
                }
                return
            }
            // Власний курс — відкриваємо управління
            isOwnCourse -> {
                TeacherCourseManageScreen(
                    courseId = courseId,
                    coursesViewModel = viewModel,
                    lessonsViewModel = lessonsViewModel,
                    testViewModel = testViewModel,
                    teacherViewModel = teacherViewModel,
                    onBack = onBack,
                    onOpenLesson = onOpenLesson,
                    onOpenLessonTest = onOpenLessonTest,
                    onCreateLessonTest = onCreateLessonTest,
                    onCreateLesson = onCreateLesson,
                    onManageTopics = onManageTopics
                )
                return
            }
            // Не власний — продовжуємо нижче, показуємо студентський вигляд
        }
    }

    if (enrollMessage != null) {
        LaunchedEffect(enrollMessage) {
            kotlinx.coroutines.delay(3000)
            viewModel.clearEnrollMessage()
        }
    }

    Scaffold(
        topBar = { EduTopBar(title = course?.title ?: "Курс", onBack = onBack) }
    ) { padding ->
        when {
            isLoading -> LoadingScreen()
            error != null -> ErrorScreen(error!!, onRetry = { viewModel.loadCourse(courseId) })
            course == null -> EmptyScreen("Курс не знайдено")
            else -> {
                val c = course!!
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                ) {
                    c.coverImage?.let { url ->
                        AsyncImage(
                            model = url, contentDescription = null,
                            modifier = Modifier.fillMaxWidth().height(200.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(c.title, style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            StatusBadge(c.status)
                        }
                        Spacer(Modifier.height(8.dp))
                        c.teacher?.let { t ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Person, null, Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(6.dp))
                                Text("${t.name} ${t.surname}", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            PriceTag(c.price)
                            c.category?.let { cat ->
                                Text(cat.name, style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary)
                            }
                        }
                        c.description?.let {
                            Spacer(Modifier.height(12.dp))
                            Text(it, style = MaterialTheme.typography.bodyMedium)
                        }

                        enrollMessage?.let { msg ->
                            Spacer(Modifier.height(12.dp))
                            Card(colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )) {
                                Text(msg, modifier = Modifier.padding(12.dp))
                            }
                        }

                        Spacer(Modifier.height(20.dp))

                        // Actions based on role
                        when (userRole) {
                            "student" -> {
                                if (isEnrolled) {
                                    Button(onClick = {}, enabled = false,
                                        modifier = Modifier.fillMaxWidth()) {
                                        Icon(Icons.Default.CheckCircle, null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Ви вже записані")
                                    }
                                } else {
                                    Button(onClick = { viewModel.enroll(courseId) },
                                        modifier = Modifier.fillMaxWidth()) {
                                        Icon(Icons.Default.School, null)
                                        Spacer(Modifier.width(8.dp))
                                        Text(if (c.price > 0) "Записатись (₴${String.format("%.0f", c.price)})" else "Записатись безкоштовно")
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(onClick = { onViewLessons(courseId) },
                                    modifier = Modifier.fillMaxWidth()) {
                                    Icon(Icons.Default.List, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Переглянути уроки")
                                }
                                Spacer(Modifier.height(8.dp))
                                OutlinedButton(onClick = { onTakeTest(courseId) },
                                    modifier = Modifier.fillMaxWidth()) {
                                    Icon(Icons.Default.Quiz, null)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Пройти тест")
                                }
                            }
                            "teacher" -> {
                                // Власний курс — управління; чужий — навчання як студент
                                val isOwnCourse = c.teacherId == c.teacher?.id ||
                                    (c.teacher == null) // fallback: якщо teacher null, вважаємо власним
                                // Визначаємо, чи це курс поточного викладача, через teacherId.
                                // Оскільки ми не маємо поточний userId прямо тут, перевіряємо
                                // через TeacherCourseManageScreen — якщо lessonsViewModel != null,
                                // значить це власний курс (CourseDetailScreen вже переключив на TeacherCourseManageScreen).
                                // Але сюди ми потрапляємо лише для ЧУЖИХ курсів (власні відкриваються через TeacherCourseManageScreen).
                                // Тому тут завжди показуємо режим "навчання як студент".

                                // Секція: навчання як студент
                                Card(
                                    Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                ) {
                                    Row(
                                        Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.School, null,
                                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                            modifier = Modifier.size(18.dp))
                                        Text(
                                            "Ви можете записатись на цей курс і проходити його як студент",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                    }
                                }
                                Spacer(Modifier.height(12.dp))

                                if (isEnrolled) {
                                    Button(
                                        onClick = { onViewLessons(courseId) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.PlayArrow, null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Продовжити навчання")
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = { onTakeTest(courseId) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.Quiz, null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Пройти тест курсу")
                                    }
                                } else {
                                    Button(
                                        onClick = { viewModel.enroll(courseId) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.School, null)
                                        Spacer(Modifier.width(8.dp))
                                        Text(if (c.price > 0) "Записатись як студент (₴${String.format("%.0f", c.price)})" else "Записатись безкоштовно")
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedButton(
                                        onClick = { onViewLessons(courseId) },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.List, null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Переглянути уроки")
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

// ── Teacher course manager: tabbed "Деталі / Уроки / Тест курсу (застарілий)" ──

@Composable
fun TeacherCourseManageScreen(
    courseId: String,
    coursesViewModel: CoursesViewModel,
    lessonsViewModel: com.eduplatform.ui.lessons.LessonsViewModel,
    testViewModel: com.eduplatform.ui.tests.TestViewModel,
    teacherViewModel: com.eduplatform.ui.teacher.TeacherViewModel,
    onBack: () -> Unit,
    onOpenLesson: (String) -> Unit,
    onOpenLessonTest: (String) -> Unit,
    onCreateLessonTest: (String) -> Unit,
    onCreateLesson: (String) -> Unit,
    onManageTopics: (String) -> Unit = {}
) {
    val course by coursesViewModel.course.collectAsState()
    var tab by rememberSaveable { mutableStateOf(0) }

    LaunchedEffect(courseId) { coursesViewModel.loadCourse(courseId) }

    Scaffold(
        topBar = {
            EduTopBar(
                title = course?.title ?: "Курс",
                onBack = onBack,
                actions = {
                    TextButton(onClick = { onManageTopics(courseId) }) {
                        Icon(Icons.Default.FolderOpen, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Теми")
                    }
                    course?.let { c ->
                        if (c.status == "draft") {
                            TextButton(onClick = { coursesViewModel.publishCourse(courseId) }) {
                                Icon(Icons.Default.Public, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Опублікувати")
                            }
                        } else {
                            TextButton(onClick = { coursesViewModel.unpublishCourse(courseId) }) {
                                Icon(Icons.Default.PublicOff, null)
                                Spacer(Modifier.width(4.dp))
                                Text("Зняти з публікації")
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            course?.let { c ->
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(c.status)
                }
            }

            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("Деталі") })
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("Уроки") })
                Tab(selected = tab == 2, onClick = { tab = 2 }, text = { Text("Тест курсу (застарілий)") })
            }

            when (tab) {
                0 -> CourseDetailsTab(courseId, course, coursesViewModel, teacherViewModel)
                1 -> CourseLessonsTab(
                    courseId, lessonsViewModel,
                    onOpenLesson = onOpenLesson,
                    onOpenLessonTest = onOpenLessonTest,
                    onCreateLessonTest = onCreateLessonTest,
                    onCreateLesson = { onCreateLesson(courseId) }
                )
                2 -> CourseLegacyTestTab(courseId, testViewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseDetailsTab(
    courseId: String,
    course: Course?,
    coursesViewModel: CoursesViewModel,
    teacherViewModel: com.eduplatform.ui.teacher.TeacherViewModel
) {
    val categories by coursesViewModel.categories.collectAsState()
    val isSaving by teacherViewModel.isLoading.collectAsState()
    val message by teacherViewModel.message.collectAsState()

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var categoryId by remember { mutableStateOf<String?>(null) }
    var price by remember { mutableStateOf("0") }
    var coverImage by remember { mutableStateOf("") }
    var accessMode by remember { mutableStateOf("open") }
    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var accessMenuExpanded by remember { mutableStateOf(false) }
    var initialized by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { coursesViewModel.loadCategories() }

    LaunchedEffect(course) {
        val c = course
        if (c != null && !initialized) {
            title = c.title
            description = c.description ?: ""
            categoryId = c.categoryId
            price = c.price.toString()
            coverImage = c.coverImage ?: ""
            accessMode = c.accessMode
            initialized = true
        }
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        OutlinedTextField(value = title, onValueChange = { title = it },
            label = { Text("Назва курсу") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = description, onValueChange = { description = it },
            label = { Text("Опис") }, modifier = Modifier.fillMaxWidth().height(120.dp))
        Spacer(Modifier.height(12.dp))

        Text("Категорія", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(expanded = categoryMenuExpanded, onExpandedChange = { categoryMenuExpanded = it }) {
            OutlinedTextField(
                value = categories.find { it.id == categoryId }?.name ?: "Без категорії",
                onValueChange = {}, readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = categoryMenuExpanded, onDismissRequest = { categoryMenuExpanded = false }) {
                DropdownMenuItem(text = { Text("Без категорії") }, onClick = { categoryId = null; categoryMenuExpanded = false })
                categories.forEach { cat ->
                    DropdownMenuItem(text = { Text(cat.name) }, onClick = { categoryId = cat.id; categoryMenuExpanded = false })
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(value = price, onValueChange = { price = it },
            label = { Text("Ціна (₴)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(value = coverImage, onValueChange = { coverImage = it },
            label = { Text("URL обкладинки") }, placeholder = { Text("https://...") },
            supportingText = { Text("Посилання на зображення (необов'язково)") },
            modifier = Modifier.fillMaxWidth(), singleLine = true)
        Spacer(Modifier.height(12.dp))

        Text("Режим доступу до уроків", style = MaterialTheme.typography.labelLarge)
        Spacer(Modifier.height(4.dp))
        ExposedDropdownMenuBox(expanded = accessMenuExpanded, onExpandedChange = { accessMenuExpanded = it }) {
            OutlinedTextField(
                value = if (accessMode == "sequential") "Послідовний — урок за уроком"
                else "Відкритий — всі уроки доступні одразу",
                onValueChange = {}, readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = accessMenuExpanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
            )
            ExposedDropdownMenu(expanded = accessMenuExpanded, onDismissRequest = { accessMenuExpanded = false }) {
                DropdownMenuItem(text = { Text("Відкритий — всі уроки доступні одразу") },
                    onClick = { accessMode = "open"; accessMenuExpanded = false })
                DropdownMenuItem(text = { Text("Послідовний — урок за уроком") },
                    onClick = { accessMode = "sequential"; accessMenuExpanded = false })
            }
        }

        message?.let {
            Spacer(Modifier.height(12.dp))
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
                        categoryId = categoryId,
                        price = price.toDoubleOrNull() ?: 0.0,
                        coverImage = coverImage.takeIf { it.isNotBlank() },
                        accessMode = accessMode
                    )
                ) { coursesViewModel.loadCourse(courseId) }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSaving && title.length >= 5
        ) {
            if (isSaving) CircularProgressIndicator(Modifier.size(20.dp))
            else { Icon(Icons.Default.Save, null); Spacer(Modifier.width(8.dp)); Text("Зберегти зміни") }
        }
    }
}

@Composable
private fun CourseLessonsTab(
    courseId: String,
    viewModel: com.eduplatform.ui.lessons.LessonsViewModel,
    onOpenLesson: (String) -> Unit,
    onOpenLessonTest: (String) -> Unit,
    onCreateLessonTest: (String) -> Unit,
    onCreateLesson: () -> Unit
) {
    val blocks by viewModel.blocks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(courseId) { viewModel.loadBlocks(courseId) }

    Column(Modifier.fillMaxSize()) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Уроки курсу", style = MaterialTheme.typography.titleMedium)
            Button(onClick = onCreateLesson) {
                Icon(Icons.Default.Add, null)
                Spacer(Modifier.width(6.dp))
                Text("Додати урок")
            }
        }

        when {
            isLoading -> LoadingScreen()
            error != null -> ErrorScreen(error!!, onRetry = { viewModel.loadBlocks(courseId) })
            blocks.isEmpty() -> EmptyScreen("Уроків ще немає")
            else -> LazyColumn(Modifier.fillMaxSize()) {
                items(blocks) { block ->
                    Card(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
                        ListItem(
                            headlineContent = { Text(block.lesson.title) },
                            supportingContent = {
                                Text("№${block.lesson.order + 1} · ${lessonTypeLabel(block.lesson.type)}" +
                                    (if (block.lesson.topicId == null) " · без теми" else "") +
                                    if (block.test != null) " · тест додано" else "")
                            },
                            leadingContent = {
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) { Text("${block.lesson.order + 1}", Modifier.padding(8.dp)) }
                            },
                            trailingContent = {
                                Row {
                                    IconButton(onClick = {
                                        if (block.test != null) onOpenLessonTest(block.lesson.id)
                                        else onCreateLessonTest(block.lesson.id)
                                    }) {
                                        Icon(
                                            Icons.Default.Quiz, "Тест уроку",
                                            tint = if (block.test != null) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outline
                                        )
                                    }
                                    IconButton(onClick = { viewModel.deleteLesson(block.lesson.id, courseId) }) {
                                        Icon(Icons.Default.Delete, "Видалити", tint = MaterialTheme.colorScheme.error)
                                    }
                                }
                            },
                            modifier = Modifier.clickable { onOpenLesson(block.lesson.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseLegacyTestTab(
    courseId: String,
    viewModel: com.eduplatform.ui.tests.TestViewModel
) {
    val test by viewModel.test.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(courseId) { viewModel.loadTest(courseId) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
            Text(
                "Це старий формат — один тест на весь курс. Зазвичай зручніше додавати тест безпосередньо до уроку: " +
                    "відкрийте вкладку «Уроки» і натисніть іконку «Тест уроку» біля потрібного уроку.",
                Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(16.dp))

        when {
            isLoading -> LoadingScreen()
            test != null -> Card(Modifier.fillMaxWidth()) {
                ListItem(
                    headlineContent = { Text(test!!.title) },
                    supportingContent = { Text("Прохідний бал: ${test!!.passingScore}% · Питань: ${test!!.questions.size}") },
                    leadingContent = { Icon(Icons.Default.Quiz, null, tint = MaterialTheme.colorScheme.primary) }
                )
            }
            else -> EmptyScreen("Тест ще не створено")
        }
    }
}
