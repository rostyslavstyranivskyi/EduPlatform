package com.eduplatform.ui

import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.eduplatform.ui.admin.*
import com.eduplatform.ui.auth.AuthViewModel
import com.eduplatform.ui.courses.*
import com.eduplatform.ui.lessons.*
import com.eduplatform.ui.progress.*
import com.eduplatform.ui.teacher.*
import com.eduplatform.ui.tests.*
import com.eduplatform.ui.topics.*

data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: String
)

@Composable
fun EduPlatformApp() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val userRole by authViewModel.userRole.collectAsState()
    val accessToken by authViewModel.accessToken.collectAsState()
    val userName by authViewModel.userName.collectAsState()
    val userId by authViewModel.userId.collectAsState()
    val isRefreshing by authViewModel.isRefreshing.collectAsState()

    // On first launch, if the stored token is the "cookie-session" placeholder,
    // attempt to restore the session via the refresh endpoint before showing any UI.
    LaunchedEffect(Unit) {
        if (accessToken == "cookie-session") {
            authViewModel.refreshSession()
        }
    }

    // While refresh is in-flight, show a neutral loading screen so the user
    // doesn't see a flash of the Login screen before being redirected to Main.
    if (isRefreshing) {
        androidx.compose.foundation.layout.Box(
            modifier = androidx.compose.ui.Modifier.fillMaxSize(),
            contentAlignment = androidx.compose.ui.Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val isLoggedIn = accessToken != null

    // Key on isLoggedIn so each NavHost (auth vs main) gets its own fresh
    // NavController instead of reusing one tied to a different graph.
    if (!isLoggedIn) {
        key("auth") {
            val authNavController = rememberNavController()
            NavHost(navController = authNavController, startDestination = Screen.Login.route) {
                composable(Screen.Login.route) {
                    com.eduplatform.ui.auth.LoginScreen(
                        viewModel = authViewModel,
                        onNavigateToRegister = { authNavController.navigate(Screen.Register.route) },
                        onLoginSuccess = { /* isLoggedIn flips automatically once accessToken is saved */ }
                    )
                }
                composable(Screen.Register.route) {
                    com.eduplatform.ui.auth.RegisterScreen(
                        viewModel = authViewModel,
                        onNavigateToLogin = { authNavController.popBackStack() },
                        onRegisterSuccess = { /* isLoggedIn flips automatically once accessToken is saved */ }
                    )
                }
            }
        }
    } else {
        key("main") {
            val mainNavController = rememberNavController()
            MainScaffold(
                navController = mainNavController,
                userRole = userRole ?: "student",
                userName = userName,
                userId = userId,
                authViewModel = authViewModel
            )
        }
    }
}

@Composable
fun MainScaffold(
    navController: NavHostController,
    userRole: String,
    userName: String?,
    userId: String? = null,
    authViewModel: AuthViewModel
) {
    val bottomItems = when (userRole) {
        "teacher" -> listOf(
            BottomNavItem("Каталог", Icons.Default.Search, Screen.CourseList.route),
            BottomNavItem("Мої курси", Icons.Default.Book, Screen.TeacherCourses.route),
            BottomNavItem("Аналітика", Icons.Default.BarChart, Screen.AnalyticsDashboard.route),
            BottomNavItem("Профіль", Icons.Default.Person, Screen.Profile.route)
        )
        "admin" -> listOf(
            BottomNavItem("Каталог", Icons.Default.Search, Screen.CourseList.route),
            BottomNavItem("Користувачі", Icons.Default.People, Screen.AdminUsers.route),
            BottomNavItem("Курси", Icons.Default.LibraryBooks, Screen.AdminCourses.route),
            BottomNavItem("Профіль", Icons.Default.Person, Screen.Profile.route)
        )
        else -> listOf(
            BottomNavItem("Каталог", Icons.Default.Search, Screen.CourseList.route),
            BottomNavItem("Навчання", Icons.Default.TrendingUp, Screen.MyProgress.route),
            BottomNavItem("Профіль", Icons.Default.Person, Screen.Profile.route)
        )
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            NavigationBar {
                bottomItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (userRole == "teacher") Screen.TeacherCourses.route else Screen.CourseList.route,
            modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)
        ) {
            // Catalog
            composable(Screen.CourseList.route) {
                val vm: CoursesViewModel = hiltViewModel()
                CourseListScreen(
                    viewModel = vm, userRole = userRole,
                    onCourseClick = { navController.navigate(Screen.CourseDetail.create(it)) },
                    onCreateCourse = { navController.navigate(Screen.CreateCourse.route) }
                )
            }
            composable(Screen.CourseDetail.route) { back ->
                val courseId = back.arguments?.getString("courseId") ?: return@composable
                val vm: CoursesViewModel = hiltViewModel()
                val lessonsVm: LessonsViewModel = hiltViewModel()
                val testVm: TestViewModel = hiltViewModel()
                val teacherVm: TeacherViewModel = hiltViewModel()
                CourseDetailScreen(
                    courseId = courseId, viewModel = vm, userRole = userRole,
                    currentUserId = userId,
                    onBack = { navController.popBackStack() },
                    onViewLessons = { navController.navigate(Screen.TopicList.create(it)) },
                    onTakeTest = { navController.navigate(Screen.TestScreen.create(it)) },
                    onEditCourse = { navController.navigate(Screen.EditCourse.create(it)) },
                    onManageLessons = { navController.navigate(Screen.CourseBlocks.create(it)) },
                    onCreateTest = { navController.navigate(Screen.CreateTest.create(it)) },
                    lessonsViewModel = lessonsVm,
                    testViewModel = testVm,
                    teacherViewModel = teacherVm,
                    onOpenLesson = { navController.navigate(Screen.LessonDetail.create(it)) },
                    onOpenLessonTest = { navController.navigate(Screen.LessonTestScreen.create(it)) },
                    onCreateLessonTest = { navController.navigate(Screen.CreateLessonTest.create(it)) },
                    onEditLessonTest = { testId, lessonId -> navController.navigate(Screen.EditLessonTest.create(testId, lessonId)) },
                    onEditCourseTest = { testId, cId -> navController.navigate(Screen.EditTest.create(testId, cId)) },
                    onCreateLesson = { navController.navigate(Screen.CreateLesson.create(it)) },
                    onManageTopics = { navController.navigate(Screen.ManageTopics.create(it)) }
                )
            }

            // Lessons
            composable(Screen.ManageLessons.route) { back ->
                val courseId = back.arguments?.getString("courseId") ?: return@composable
                val vm: LessonsViewModel = hiltViewModel()
                LessonsListScreen(
                    courseId = courseId, viewModel = vm, userRole = userRole,
                    onBack = { navController.popBackStack() },
                    onLessonClick = { navController.navigate(Screen.LessonDetail.create(it)) },
                    onCreateLesson = { navController.navigate(Screen.CreateLesson.create(courseId)) }
                )
            }
            composable(Screen.CourseBlocks.route) { back ->
                val courseId = back.arguments?.getString("courseId") ?: return@composable
                val vm: LessonsViewModel = hiltViewModel()
                BlocksListScreen(
                    courseId = courseId, viewModel = vm, userRole = userRole,
                    onBack = { navController.popBackStack() },
                    onLessonClick = { navController.navigate(Screen.LessonDetail.create(it)) },
                    onLessonTestClick = { navController.navigate(Screen.LessonTestScreen.create(it)) },
                    onCreateLesson = { navController.navigate(Screen.CreateLesson.create(courseId)) },
                    onCreateLessonTest = { navController.navigate(Screen.CreateLessonTest.create(it)) }
                )
            }
            composable(Screen.LessonDetail.route) { back ->
                val lessonId = back.arguments?.getString("lessonId") ?: return@composable
                val vm: LessonsViewModel = hiltViewModel()
                LessonDetailScreen(
                    lessonId = lessonId,
                    viewModel = vm,
                    userRole = userRole,
                    onBack = { navController.popBackStack() },
                    onTakeTest = { navController.navigate(Screen.LessonTestScreen.create(it)) }
                )
            }
            composable(Screen.CreateLesson.route) { back ->
                val courseId = back.arguments?.getString("courseId") ?: return@composable
                val vm: LessonsViewModel = hiltViewModel()
                CreateLessonScreen(courseId = courseId, viewModel = vm, onBack = { navController.popBackStack() })
            }

            // Tests
            composable(Screen.TestScreen.route) { back ->
                val courseId = back.arguments?.getString("courseId") ?: return@composable
                val vm: TestViewModel = hiltViewModel()
                TestScreen(courseId = courseId, viewModel = vm, onBack = { navController.popBackStack() })
            }
            composable(Screen.LessonTestScreen.route) { back ->
                val lessonId = back.arguments?.getString("lessonId") ?: return@composable
                val vm: TestViewModel = hiltViewModel()
                LessonTestScreen(lessonId = lessonId, viewModel = vm, onBack = { navController.popBackStack() })
            }
            composable(Screen.CreateTest.route) { back ->
                val courseId = back.arguments?.getString("courseId") ?: return@composable
                val vm: TestViewModel = hiltViewModel()
                CreateTestScreen(courseId = courseId, viewModel = vm, onBack = { navController.popBackStack() })
            }
            composable(Screen.CreateLessonTest.route) { back ->
                val lessonId = back.arguments?.getString("lessonId") ?: return@composable
                val vm: TestViewModel = hiltViewModel()
                CreateTestScreen(courseId = lessonId, viewModel = vm, onBack = { navController.popBackStack() }, isLessonTest = true)
            }

            // Topics
            composable(Screen.TopicList.route) { back ->
                val courseId = back.arguments?.getString("courseId") ?: return@composable
                val vm: TopicsViewModel = hiltViewModel()
                val lessonsVm: LessonsViewModel = hiltViewModel()
                val teacherVm: TeacherViewModel = hiltViewModel()
                val coursesVm: CoursesViewModel = hiltViewModel()

                // Визначаємо чи є цей курс власним для поточного викладача
                val isOwnCourse = if (userRole == "teacher") {
                    val myCourses by teacherVm.myCourses.collectAsState()
                    LaunchedEffect(Unit) {
                        if (myCourses.isEmpty()) teacherVm.loadMyCourses()
                    }
                    myCourses.any { it.id == courseId }
                } else {
                    false
                }

                TopicsListScreen(
                    courseId = courseId,
                    viewModel = vm,
                    lessonsViewModel = lessonsVm,
                    userRole = userRole,
                    isOwnCourse = isOwnCourse,
                    onBack = { navController.popBackStack() },
                    onTopicClick = { navController.navigate(Screen.TopicDetail.create(it, courseId)) },
                    onTopicTest = { navController.navigate(Screen.TopicTest.create(it)) },
                    onLessonClick = { navController.navigate(Screen.LessonDetail.create(it)) },
                    onLessonTestClick = { navController.navigate(Screen.LessonTestScreen.create(it)) },
                    onManageTopics = { navController.navigate(Screen.ManageTopics.create(courseId)) },
                    onCreateTopicTest = { topicId -> navController.navigate(Screen.CreateTopicTest.create(topicId)) },
                    onEditTopicTest = { testId, topicId -> navController.navigate(Screen.EditTopicTest.create(testId, topicId)) },
                    onEnroll = if (!isOwnCourse) {
                        {
                            coursesVm.enroll(courseId)
                            lessonsVm.loadBlocks(courseId)
                        }
                    } else null
                )
            }
            composable(Screen.TopicDetail.route) { back ->
                val topicId = back.arguments?.getString("topicId") ?: return@composable
                val courseId = back.arguments?.getString("courseId") ?: return@composable
                val vm: TopicsViewModel = hiltViewModel()
                val lessonsVm: LessonsViewModel = hiltViewModel()
                TopicDetailScreen(
                    topicId = topicId, courseId = courseId, viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onLessonClick = { navController.navigate(Screen.LessonDetail.create(it)) },
                    onTopicTest = { navController.navigate(Screen.TopicTest.create(it)) },
                    lessonsViewModel = lessonsVm,
                    userRole = userRole,
                    onCreateTopicTest = { navController.navigate(Screen.CreateTopicTest.create(it)) },
                    onEditTopicTest = { testId, tId -> navController.navigate(Screen.EditTopicTest.create(testId, tId)) }
                )
            }
            composable(Screen.TopicTest.route) { back ->
                val topicId = back.arguments?.getString("topicId") ?: return@composable
                val vm: TestViewModel = hiltViewModel()
                TopicTestScreen(topicId = topicId, viewModel = vm, onBack = { navController.popBackStack() })
            }
            composable(Screen.CreateTopicTest.route) { back ->
                val topicId = back.arguments?.getString("topicId") ?: return@composable
                val vm: TestViewModel = hiltViewModel()
                CreateTestScreen(courseId = topicId, viewModel = vm, onBack = { navController.popBackStack() }, isTopicTest = true)
            }
            composable(Screen.EditTopicTest.route) { back ->
                val testId = back.arguments?.getString("testId") ?: return@composable
                val topicId = back.arguments?.getString("topicId") ?: return@composable
                val vm: TestViewModel = hiltViewModel()
                EditTestScreen(viewModel = vm, onBack = { navController.popBackStack() }, topicId = topicId)
            }
            composable(Screen.EditLessonTest.route) { back ->
                val testId = back.arguments?.getString("testId") ?: return@composable
                val lessonId = back.arguments?.getString("lessonId") ?: return@composable
                val vm: TestViewModel = hiltViewModel()
                EditTestScreen(viewModel = vm, onBack = { navController.popBackStack() }, lessonId = lessonId)
            }
            composable(Screen.EditTest.route) { back ->
                val testId = back.arguments?.getString("testId") ?: return@composable
                val courseId = back.arguments?.getString("courseId") ?: return@composable
                val vm: TestViewModel = hiltViewModel()
                // Для курсового тесту testId == courseId для loadTest; зберігаємо за testId
                EditTestScreen(viewModel = vm, onBack = { navController.popBackStack() }, testId = courseId)
            }
            composable(Screen.ManageTopics.route) { back ->
                val courseId = back.arguments?.getString("courseId") ?: return@composable
                val vm: TopicsViewModel = hiltViewModel()
                val lessonsVm: LessonsViewModel = hiltViewModel()
                ManageTopicsScreen(
                    courseId = courseId, viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onAssignLessons = { navController.navigate(Screen.AssignLessonsToTopic.create(it, courseId)) },
                    onCreateTopicTest = { navController.navigate(Screen.CreateTopicTest.create(it)) },
                    onEditTopicTest = { testId, topicId -> navController.navigate(Screen.EditTopicTest.create(testId, topicId)) },
                    lessonsViewModel = lessonsVm
                )
            }
            composable(Screen.AssignLessonsToTopic.route) { back ->
                val topicId = back.arguments?.getString("topicId") ?: return@composable
                val courseId = back.arguments?.getString("courseId") ?: return@composable
                val topicsVm: TopicsViewModel = hiltViewModel()
                val lessonsVm: LessonsViewModel = hiltViewModel()
                AssignLessonsToTopicScreen(
                    topicId = topicId, courseId = courseId,
                    topicsViewModel = topicsVm, lessonsViewModel = lessonsVm,
                    onBack = { navController.popBackStack() }
                )
            }

            // Progress
            composable(Screen.MyProgress.route) {
                val vm: ProgressViewModel = hiltViewModel()
                MyProgressScreen(viewModel = vm,
                    onCourseClick = { navController.navigate(Screen.CourseDetail.create(it)) })
            }

            // Profile
            composable(Screen.Profile.route) {
                val vm: ProgressViewModel = hiltViewModel()
                ProfileScreen(
                    viewModel = vm, userRole = userRole, userName = userName,
                    onLogout = {
                        authViewModel.logout()
                    }
                )
            }

            // Teacher
            composable(Screen.TeacherCourses.route) {
                val vm: TeacherViewModel = hiltViewModel()
                TeacherCoursesScreen(
                    viewModel = vm,
                    onCourseClick = { navController.navigate(Screen.CourseDetail.create(it)) },
                    onCreateCourse = { navController.navigate(Screen.CreateCourse.route) }
                )
            }
            composable(Screen.CreateCourse.route) {
                val vm: TeacherViewModel = hiltViewModel()
                CreateCourseScreen(viewModel = vm, onBack = { navController.popBackStack() },
                    onCreated = { id ->
                        navController.navigate(Screen.CourseDetail.create(id)) {
                            popUpTo(Screen.TeacherCourses.route)
                        }
                    })
            }
            composable(Screen.EditCourse.route) { back ->
                val courseId = back.arguments?.getString("courseId") ?: return@composable
                val teacherVm: TeacherViewModel = hiltViewModel()
                val coursesVm: CoursesViewModel = hiltViewModel()
                EditCourseScreen(
                    courseId = courseId,
                    teacherViewModel = teacherVm,
                    coursesViewModel = coursesVm,
                    onBack = { navController.popBackStack() },
                    onUpdated = { navController.popBackStack() }
                )
            }
            composable(Screen.AnalyticsDashboard.route) {
                val vm: TeacherViewModel = hiltViewModel()
                val progressVm: com.eduplatform.ui.progress.ProgressViewModel = hiltViewModel()
                AnalyticsDashboardScreen(
                    viewModel = vm,
                    onCourseAnalytics = { navController.navigate(Screen.CourseAnalytics.create(it)) },
                    progressViewModel = progressVm,
                    onProgressCourseClick = { navController.navigate(Screen.TopicList.create(it)) }
                )
            }
            composable(Screen.CourseAnalytics.route) { back ->
                val courseId = back.arguments?.getString("courseId") ?: return@composable
                val vm: TeacherViewModel = hiltViewModel()
                CourseAnalyticsScreen(courseId = courseId, viewModel = vm,
                    onBack = { navController.popBackStack() })
            }

            // Admin
            composable(Screen.AdminUsers.route) {
                val vm: AdminViewModel = hiltViewModel()
                AdminUsersScreen(viewModel = vm)
            }
            composable(Screen.AdminCourses.route) {
                val vm: AdminViewModel = hiltViewModel()
                AdminCoursesScreen(
                    viewModel = vm,
                    onCourseClick = { navController.navigate(Screen.AdminCourseDetail.create(it)) }
                )
            }
            composable(Screen.AdminCourseDetail.route) { back ->
                val courseId = back.arguments?.getString("courseId") ?: return@composable
                val coursesVm: CoursesViewModel = hiltViewModel()
                val adminVm: AdminViewModel = hiltViewModel()
                AdminCourseDetailScreen(
                    courseId = courseId,
                    coursesViewModel = coursesVm,
                    onBack = { navController.popBackStack() },
                    onViewBlocks = { navController.navigate(Screen.AdminCourseBlocks.create(it)) },
                    onUnpublish = { id -> adminVm.unpublishCourse(id); navController.popBackStack() }
                )
            }
            composable(Screen.AdminCourseBlocks.route) { back ->
                val courseId = back.arguments?.getString("courseId") ?: return@composable
                val lessonsVm: LessonsViewModel = hiltViewModel()
                val topicsVm: TopicsViewModel = hiltViewModel()
                AdminCourseBlocksScreen(
                    courseId = courseId,
                    lessonsViewModel = lessonsVm,
                    topicsViewModel = topicsVm,
                    onBack = { navController.popBackStack() },
                    onLessonClick = { navController.navigate(Screen.AdminLessonDetail.create(it)) },
                    onLessonTestClick = { navController.navigate(Screen.AdminLessonTest.create(it)) }
                )
            }
            composable(Screen.AdminLessonDetail.route) { back ->
                val lessonId = back.arguments?.getString("lessonId") ?: return@composable
                val lessonsVm: LessonsViewModel = hiltViewModel()
                AdminLessonDetailScreen(
                    lessonId = lessonId,
                    lessonsViewModel = lessonsVm,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AdminLessonTest.route) { back ->
                val lessonId = back.arguments?.getString("lessonId") ?: return@composable
                val testVm: TestViewModel = hiltViewModel()
                AdminLessonTestScreen(
                    lessonId = lessonId,
                    testViewModel = testVm,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}