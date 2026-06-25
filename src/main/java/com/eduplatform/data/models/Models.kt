package com.eduplatform.data.models

import com.google.gson.annotations.SerializedName

// ── Auth ─────────────────────────────────────────────────────────────────────

data class RegisterRequest(
    val name: String,
    val surname: String,
    val email: String,
    val password: String,
    val role: String = "student"
)

data class LoginRequest(val email: String, val password: String)

data class AuthResponse(
    val success: Boolean,
    val message: String,
    val data: AuthData?
)

data class AuthData(
    val user: User,
    val accessToken: String? = null
)

// ── User ──────────────────────────────────────────────────────────────────────

data class User(
    val id: String,
    val name: String,
    val surname: String,
    val email: String,
    val role: String,
    val isBanned: Boolean = false,
    val balance: Double = 0.0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class UserProfile(
    val id: String? = null,
    val userId: String? = null,
    val avatar: String?,
    val bio: String?,
    val phone: String?,
    val user: User? = null
)

data class ProfileResponse(
    val success: Boolean,
    val message: String,
    val data: ProfileData?
)

data class ProfileData(val profile: UserProfile)

// GET /profiles/me (and presumably GET /profiles/{id}) return a different shape
// than PATCH /profiles/me: the profile fields are nested inside "user", and
// there's no top-level "profile" key at all — unlike the PATCH response, which
// returns the bare profile row under data.profile. EduRepository.getMyProfile()
// merges this into the same UserProfile used everywhere else in the app.
data class MyProfileUser(
    val id: String,
    val name: String,
    val surname: String,
    val email: String,
    val role: String,
    val profile: UserProfile? = null
)

data class MyProfileResponse(
    val success: Boolean,
    val data: MyProfileData?
)

data class MyProfileData(val user: MyProfileUser)

data class UpdateProfileRequest(
    val avatar: String? = null,
    val bio: String? = null,
    val phone: String? = null
)

// ── Category ─────────────────────────────────────────────────────────────────

data class Category(
    val id: String,
    val name: String,
    val icon: String = "book"
)

data class CategoriesResponse(
    val success: Boolean,
    val data: CategoriesData?
)

data class CategoriesData(val categories: List<Category>)

// ── Course ───────────────────────────────────────────────────────────────────

data class Course(
    val id: String,
    val title: String,
    val description: String?,
    val coverImage: String?,
    val price: Double,
    val status: String,
    val teacherId: String,
    val categoryId: String?,
    val teacher: User?,
    val category: Category?,
    val accessMode: String = "open",
    val enrolledCount: Int = 0,
    val lessonsCount: Int = 0
)

data class CoursesResponse(
    val success: Boolean,
    val message: String,
    val data: CoursesData?
)

data class CoursesData(
    val courses: List<Course>,
    val totalCount: Int = 0,
    val page: Int = 1,
    val totalPages: Int = 1,
    val limit: Int = 10
)

data class CourseResponse(
    val success: Boolean,
    val message: String,
    val data: CourseData?
)

data class CourseData(val course: Course)

data class CreateCourseRequest(
    val title: String,
    val description: String? = null,
    val categoryId: String? = null,
    val price: Double = 0.0,
    val coverImage: String? = null,
    val accessMode: String? = null
)

// ── Lesson ───────────────────────────────────────────────────────────────────

data class Lesson(
    val id: String,
    val courseId: String,
    val topicId: String? = null,
    val title: String,
    val type: String,
    val content: String?,
    val videoUrl: String?,
    val pdfUrl: String?,
    val order: Int,
    val locked: Boolean = false
)

data class LessonsResponse(
    val success: Boolean,
    val message: String,
    val data: LessonsData?
)

data class LessonsData(val lessons: List<Lesson>)

data class LessonResponse(
    val success: Boolean,
    val message: String,
    val data: LessonData?
)

data class LessonData(val lesson: Lesson)

data class CreateLessonRequest(
    val title: String,
    val type: String = "text",
    val content: String? = null,
    val videoUrl: String? = null,
    val pdfUrl: String? = null,
    val order: Int? = null
)

// ── Topic ────────────────────────────────────────────────────────────────────

// Test summary nested inside TopicWithLessons (GET /topics/course/:courseId).
// Mirrors BlockTestInfo's shape for lesson-block tests.
data class TopicTest(
    val id: String,
    val title: String,
    val passingScore: Int,
    val maxAttempts: Int? = null,
    val questionsCount: Int? = null,
    val passed: Boolean? = null
)

data class TopicWithLessons(
    val id: String,
    val courseId: String,
    val title: String,
    val description: String?,
    val order: Int,
    val lessons: List<Lesson> = emptyList(),
    val test: TopicTest? = null
)

data class TopicsResponse(
    val success: Boolean,
    val data: TopicsData?
)

data class TopicsData(val topics: List<TopicWithLessons>)

data class TopicResponse(
    val success: Boolean,
    val message: String,
    val data: TopicData?
)

data class TopicData(val topic: TopicWithLessons)

data class CreateTopicRequest(
    val title: String,
    val description: String? = null,
    val order: Int? = null
)

data class AssignLessonsRequest(val lessonIds: List<String>)

// ── Test ─────────────────────────────────────────────────────────────────────

data class TestQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int? = null
)

data class Test(
    val id: String,
    val courseId: String? = null,
    val lessonId: String? = null,
    val topicId: String? = null,
    val title: String,
    val questions: List<TestQuestion>,
    val passingScore: Int,
    val maxAttempts: Int? = null,
    val attemptsUsed: Int? = null,
    val attemptsLeft: Int? = null,
    val questionsCount: Int? = null
)

data class TestResponse(
    val success: Boolean,
    val message: String,
    val data: TestData?
)

data class TestData(val test: Test)

data class CreateTestRequest(
    val title: String,
    val questions: List<TestQuestion>,
    val passingScore: Int = 70,
    val maxAttempts: Int? = null
)

data class SubmitTestRequest(val answers: List<Int>)

data class TestResult(
    val testId: String = "",
    val score: Int,
    val passed: Boolean,
    val passingScore: Int = 0,
    val correctCount: Int,
    val totalQuestions: Int,
    val details: List<TestDetail> = emptyList()
)

data class TestDetail(
    val question: String,
    val yourAnswer: String,
    val correctAnswer: String,
    val isCorrect: Boolean
)

data class TestResultResponse(
    val success: Boolean,
    val message: String,
    val data: TestResult?
)

// ── Course blocks (lesson + optional lesson test) ───────────────────────────

// Block test summary as embedded in GET /lessons/course/:courseId/blocks.
// `passed` is null for teacher/admin (not tied to a specific student).
data class BlockTestInfo(
    val id: String,
    val title: String,
    val passingScore: Int,
    val maxAttempts: Int? = null,
    val passed: Boolean? = null
)

data class CourseBlock(
    val lesson: Lesson,
    val test: BlockTestInfo?
)

data class CourseBlocksResponse(
    val success: Boolean,
    val data: CourseBlocksData?
)

data class CourseBlocksData(val blocks: List<CourseBlock>)



// The /progress/me endpoint embeds a lightweight course summary, not the full
// Course object (no description/status/teacherId/teacher/etc.) — keep it as
// its own type instead of reusing Course, which has several non-null fields
// that would silently end up null here.
data class CourseProgressSummary(
    val id: String,
    val title: String,
    val coverImage: String?,
    val price: Double = 0.0,
    val category: Category?
)

data class LegacyTestProgress(
    val hasTest: Boolean = false,
    val passed: Boolean? = null,
    val bestScore: Int? = null,
    val attemptsCount: Int = 0
)

data class BlockProgressTest(
    val id: String,
    val title: String,
    val passingScore: Int,
    val passed: Boolean = false,
    val bestScore: Int? = null,
    val attemptsCount: Int = 0
)

data class BlockLessonSummary(
    val id: String,
    val title: String,
    val type: String,
    val order: Int
)

data class BlockProgress(
    val lesson: BlockLessonSummary,
    val lessonCompleted: Boolean,
    val completedAt: String? = null,
    val test: BlockProgressTest? = null,
    val isCompleted: Boolean = false
)

data class CourseProgress(
    val course: CourseProgressSummary?,
    val enrolledAt: String? = null,
    val completedLessons: Int,
    val totalLessons: Int,
    val percentage: Int,
    val allLessonsDone: Boolean = false,
    val allBlocksDone: Boolean = false,
    val isCompleted: Boolean = false
)

data class ProgressResponse(
    val success: Boolean,
    val data: ProgressData?
)

data class ProgressData(val courses: List<CourseProgress>)

data class LessonProgressResponse(
    val success: Boolean,
    val message: String,
    val data: LessonProgressData?
)

data class LessonProgressData(
    val courseId: String? = null,
    val totalLessons: Int,
    val completedLessons: Int,
    val percentage: Int,
    val allLessonsDone: Boolean = false,
    val allBlocksDone: Boolean = false,
    val isCompleted: Boolean = false,
    val test: LegacyTestProgress = LegacyTestProgress(),
    val blocks: List<BlockProgress> = emptyList(),
    val lessons: List<LessonProgressItem>
)

data class LessonProgressItem(
    @com.google.gson.annotations.SerializedName("id")
    val lessonId: String,
    val title: String,
    val completed: Boolean,
    val completedAt: String?
)

data class MarkProgressRequest(val completed: Boolean = true)

// ── Enrollment ───────────────────────────────────────────────────────────────

data class EnrollResponse(
    val success: Boolean,
    val message: String,
    val data: Any?
)

// ── Analytics ────────────────────────────────────────────────────────────────

data class TeacherDashboard(
    val totalCourses: Int = 0,
    val publishedCourses: Int = 0,
    val totalStudents: Int = 0,
    val totalRevenue: Double = 0.0,
    val teacherBalance: Double = 0.0
)

data class DashboardCourseRevenue(
    val gross: Double = 0.0,
    val teacherNet: Double = 0.0
)

// Per-course breakdown embedded in /analytics/dashboard's "courses" array.
// Not the full Course model — only what the dashboard actually returns.
data class DashboardCourseSummary(
    val id: String,
    val title: String,
    val status: String,
    val price: Double = 0.0,
    val category: Category?,
    val students: Int = 0,
    val revenue: DashboardCourseRevenue? = null
)

data class DashboardResponse(
    val success: Boolean,
    val data: DashboardData?
)

data class DashboardData(
    val summary: TeacherDashboard,
    val courses: List<DashboardCourseSummary> = emptyList()
)

data class AnalyticsCourseSummary(
    val id: String,
    val title: String,
    val status: String,
    val price: Double = 0.0
)

data class AnalyticsStudentsSummary(
    val total: Int = 0,
    val lastEnrollmentAt: String? = null
)

data class AnalyticsLessonsSummary(
    val total: Int = 0
)

data class AnalyticsProgressSummary(
    val averagePercentage: Double = 0.0
)

data class AnalyticsRevenueSummary(
    val gross: Double = 0.0,
    val platformFee: Double = 0.0,
    val teacherNet: Double = 0.0
)

data class CourseAnalytics(
    val course: AnalyticsCourseSummary?,
    val students: AnalyticsStudentsSummary = AnalyticsStudentsSummary(),
    val lessons: AnalyticsLessonsSummary = AnalyticsLessonsSummary(),
    val blocks: AnalyticsBlocksSummary = AnalyticsBlocksSummary(),
    val progress: AnalyticsProgressSummary = AnalyticsProgressSummary(),
    val revenue: AnalyticsRevenueSummary = AnalyticsRevenueSummary()
)

data class AnalyticsBlocksSummary(
    val total: Int = 0,
    val withTest: Int = 0
)

data class CourseAnalyticsResponse(
    val success: Boolean,
    val data: CourseAnalytics?
)

data class StudentSummary(
    val id: String,
    val name: String,
    val surname: String,
    val email: String
)

data class StudentProgress(
    val student: StudentSummary?,
    val enrolledAt: String? = null,
    val completedLessons: Int,
    val totalLessons: Int,
    val percentage: Int,
    val allLessonsDone: Boolean = false,
    val allBlocksDone: Boolean = false,
    val isCompleted: Boolean = false,
    val blocks: List<BlockProgress> = emptyList(),
    val legacyTest: LegacyTestProgress = LegacyTestProgress()
)

data class StudentsProgressResponse(
    val success: Boolean,
    val data: StudentsProgressData?
)

data class StudentsProgressData(val students: List<StudentProgress>)

// ── Admin ─────────────────────────────────────────────────────────────────────

data class UsersListResponse(
    val success: Boolean,
    val message: String,
    val data: UsersListData?
)

data class UsersListData(
    val users: List<User>,
    val totalCount: Int,
    val page: Int,
    val totalPages: Int,
    val limit: Int
)

data class ChangeRoleRequest(val role: String)

data class GenericResponse(
    val success: Boolean,
    val message: String,
    val data: Any?
)
