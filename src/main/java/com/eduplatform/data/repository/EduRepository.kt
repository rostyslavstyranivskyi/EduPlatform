package com.eduplatform.data.repository

import com.eduplatform.data.api.EduApiService
import com.eduplatform.data.api.PersistentCookieJar
import com.eduplatform.data.api.TokenStorage
import com.eduplatform.data.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val code: Int = 0) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

suspend fun <T, R> safeApiCall(
    call: suspend () -> Response<T>,
    transform: (T) -> R
): Result<R> = withContext(Dispatchers.IO) {
    try {
        val response = call()
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null) {
                Result.Success(transform(body))
            } else {
                Result.Error("Порожня відповідь сервера", response.code())
            }
        } else {
            val errorBody = response.errorBody()?.string() ?: "Невідома помилка"
            Result.Error(parseErrorMessage(errorBody), response.code())
        }
    } catch (e: Exception) {
        Result.Error(e.message ?: "Помилка мережі")
    }
}

fun parseErrorMessage(json: String): String {
    return try {
        val obj = com.google.gson.JsonParser.parseString(json).asJsonObject
        obj.get("message")?.asString ?: "Помилка сервера"
    } catch (e: Exception) {
        "Помилка сервера"
    }
}

@Singleton
class EduRepository @Inject constructor(
    private val api: EduApiService,
    private val tokenStorage: TokenStorage,
    private val cookieJar: PersistentCookieJar
) {
    // ── Auth ─────────────────────────────────────────────────────────────────

    suspend fun register(req: RegisterRequest): Result<AuthData> =
        safeApiCall({ api.register(req) }) { resp ->
            resp.data ?: throw Exception(resp.message)
        }.also { result ->
            if (result is Result.Success) {
                val d = result.data
                // Backend may issue auth purely via httpOnly cookies (see API docs §3)
                // without echoing accessToken in the JSON body. The PersistentCookieJar
                // already captured those cookies, so we just need a local marker so
                // the app knows the user is authenticated.
                val token = d.accessToken ?: "cookie-session"
                tokenStorage.saveAuth(token, d.user.role, d.user.id, "${d.user.name} ${d.user.surname}")
            }
        }

    suspend fun login(req: LoginRequest): Result<AuthData> =
        safeApiCall({ api.login(req) }) { resp ->
            resp.data ?: throw Exception(resp.message)
        }.also { result ->
            if (result is Result.Success) {
                val d = result.data
                val token = d.accessToken ?: "cookie-session"
                tokenStorage.saveAuth(token, d.user.role, d.user.id, "${d.user.name} ${d.user.surname}")
            }
        }

    suspend fun refresh(): Result<AuthData> =
        safeApiCall({ api.refresh() }) { resp ->
            resp.data ?: throw Exception(resp.message)
        }.also { result ->
            if (result is Result.Success) {
                val d = result.data
                val token = d.accessToken ?: "cookie-session"
                tokenStorage.saveAuth(token, d.user.role, d.user.id, "${d.user.name} ${d.user.surname}")
            }
        }

    suspend fun logout(): Result<String> =
        safeApiCall({ api.logout() }) { it.message }.also {
            tokenStorage.clear()
            cookieJar.clear()
        }

    // ── Courses ───────────────────────────────────────────────────────────────

    suspend fun getCategories(): Result<List<Category>> =
        safeApiCall({ api.getCategories() }) { it.data?.categories ?: emptyList() }

    suspend fun getCourses(
        query: String? = null,
        categoryId: String? = null,
        price: String? = null,
        sortBy: String? = null,
        page: Int = 1
    ): Result<CoursesData> =
        safeApiCall({ api.getCourses(query, categoryId, price, sortBy, page) }) {
            it.data ?: CoursesData(emptyList())
        }

    suspend fun getMyCourses(): Result<CoursesData> =
        safeApiCall({ api.getMyCourses() }) { it.data ?: CoursesData(emptyList()) }

    suspend fun getCourse(id: String): Result<Course> =
        safeApiCall({ api.getCourse(id) }) { it.data?.course ?: throw Exception("Not found") }

    suspend fun createCourse(req: CreateCourseRequest): Result<Course> =
        safeApiCall({ api.createCourse(req) }) { it.data?.course ?: throw Exception("Failed") }

    suspend fun updateCourse(id: String, req: CreateCourseRequest): Result<Course> =
        safeApiCall({ api.updateCourse(id, req) }) { it.data?.course ?: throw Exception("Failed") }

    suspend fun publishCourse(id: String): Result<Course> =
        safeApiCall({ api.publishCourse(id) }) { it.data?.course ?: throw Exception("Failed") }

    suspend fun unpublishCourse(id: String): Result<Course> =
        safeApiCall({ api.unpublishCourse(id) }) { it.data?.course ?: throw Exception("Failed") }

    suspend fun enrollCourse(id: String): Result<String> =
        safeApiCall({ api.enrollCourse(id) }) { it.message }

    // ── Lessons ───────────────────────────────────────────────────────────────

    suspend fun getLessons(courseId: String): Result<List<Lesson>> =
        safeApiCall({ api.getLessons(courseId) }) { it.data?.lessons ?: emptyList() }

    suspend fun getCourseBlocks(courseId: String): Result<List<CourseBlock>> =
        safeApiCall({ api.getCourseBlocks(courseId) }) { it.data?.blocks ?: emptyList() }

    suspend fun getLesson(id: String): Result<Lesson> =
        safeApiCall({ api.getLesson(id) }) { it.data?.lesson ?: throw Exception("Not found") }

    suspend fun createLesson(courseId: String, req: CreateLessonRequest): Result<Lesson> =
        safeApiCall({ api.createLesson(courseId, req) }) { it.data?.lesson ?: throw Exception("Failed") }

    suspend fun updateLesson(id: String, req: CreateLessonRequest): Result<Lesson> =
        safeApiCall({ api.updateLesson(id, req) }) { it.data?.lesson ?: throw Exception("Failed") }

    suspend fun deleteLesson(id: String): Result<String> =
        safeApiCall({ api.deleteLesson(id) }) { it.message }

    // ── Topics ───────────────────────────────────────────────────────────────

    suspend fun getTopics(courseId: String): Result<List<TopicWithLessons>> =
        safeApiCall({ api.getTopics(courseId) }) { it.data?.topics ?: emptyList() }

    suspend fun createTopic(courseId: String, req: CreateTopicRequest): Result<TopicWithLessons> =
        safeApiCall({ api.createTopic(courseId, req) }) { it.data?.topic ?: throw Exception("Failed") }

    suspend fun updateTopic(id: String, req: CreateTopicRequest): Result<TopicWithLessons> =
        safeApiCall({ api.updateTopic(id, req) }) { it.data?.topic ?: throw Exception("Failed") }

    suspend fun deleteTopic(id: String): Result<String> =
        safeApiCall({ api.deleteTopic(id) }) { it.message }

    suspend fun assignLessonsToTopic(topicId: String, lessonIds: List<String>): Result<String> =
        safeApiCall({ api.assignLessonsToTopic(topicId, AssignLessonsRequest(lessonIds)) }) { it.message }

    // ── Tests ─────────────────────────────────────────────────────────────────

    suspend fun getTest(courseId: String): Result<Test> =
        safeApiCall({ api.getTest(courseId) }) { it.data?.test ?: throw Exception("Not found") }

    suspend fun createTest(courseId: String, req: CreateTestRequest): Result<Test> =
        safeApiCall({ api.createTest(courseId, req) }) { it.data?.test ?: throw Exception("Failed") }

    suspend fun getLessonTest(lessonId: String): Result<Test> =
        safeApiCall({ api.getLessonTest(lessonId) }) { it.data?.test ?: throw Exception("Not found") }

    suspend fun createLessonTest(lessonId: String, req: CreateTestRequest): Result<Test> =
        safeApiCall({ api.createLessonTest(lessonId, req) }) { it.data?.test ?: throw Exception("Failed") }

    suspend fun getTopicTest(topicId: String): Result<Test> =
        safeApiCall({ api.getTopicTest(topicId) }) { it.data?.test ?: throw Exception("Not found") }

    suspend fun createTopicTest(topicId: String, req: CreateTestRequest): Result<Test> =
        safeApiCall({ api.createTopicTest(topicId, req) }) { it.data?.test ?: throw Exception("Failed") }

    suspend fun updateTest(id: String, req: CreateTestRequest): Result<Test> =
        safeApiCall({ api.updateTest(id, req) }) { it.data?.test ?: throw Exception("Failed") }

    suspend fun updateTopicTest(topicId: String, req: CreateTestRequest): Result<Test> =
        safeApiCall({ api.updateTopicTest(topicId, req) }) { it.data?.test ?: throw Exception("Failed") }

    suspend fun updateLessonTest(lessonId: String, req: CreateTestRequest): Result<Test> =
        safeApiCall({ api.updateLessonTest(lessonId, req) }) { it.data?.test ?: throw Exception("Failed") }

    suspend fun deleteLessonTest(lessonId: String): Result<String> =
        safeApiCall({ api.deleteLessonTest(lessonId) }) { it.message }

    suspend fun submitTest(testId: String, answers: List<Int>): Result<TestResult> =
        safeApiCall({ api.submitTest(testId, SubmitTestRequest(answers)) }) {
            it.data ?: throw Exception("Failed")
        }

    /** Видалення тесту теми (за topicId) */
    suspend fun deleteTopicTest(topicId: String): Result<String> =
        safeApiCall({ api.deleteTopicTest(topicId) }) { it.message }

    // ── Progress ──────────────────────────────────────────────────────────────

    suspend fun getMyProgress(): Result<List<CourseProgress>> =
        safeApiCall({ api.getMyProgress() }) { it.data?.courses ?: emptyList() }

    suspend fun getCourseProgress(courseId: String): Result<LessonProgressData> =
        safeApiCall({ api.getCourseProgress(courseId) }) { it.data ?: throw Exception("Not found") }

    suspend fun markLesson(lessonId: String, completed: Boolean = true): Result<String> =
        safeApiCall({ api.markLessonProgress(lessonId, MarkProgressRequest(completed)) }) { it.message }

    // ── Profile ───────────────────────────────────────────────────────────────

    suspend fun getMyProfile(): Result<UserProfile> =
        safeApiCall({ api.getMyProfile() }) { resp ->
            val u = resp.data?.user ?: throw Exception("Not found")
            val p = u.profile
            UserProfile(
                id = p?.id,
                userId = u.id,
                avatar = p?.avatar,
                bio = p?.bio,
                phone = p?.phone,
                user = User(id = u.id, name = u.name, surname = u.surname, email = u.email, role = u.role)
            )
        }

    suspend fun updateProfile(req: UpdateProfileRequest): Result<UserProfile> =
        safeApiCall({ api.updateProfile(req) }) { it.data?.profile ?: throw Exception("Failed") }

    // ── Analytics ─────────────────────────────────────────────────────────────

    suspend fun getDashboard(): Result<TeacherDashboard> =
        safeApiCall({ api.getAnalyticsDashboard() }) { it.data?.summary ?: throw Exception("Failed") }

    suspend fun getCourseAnalytics(courseId: String): Result<CourseAnalytics> =
        safeApiCall({ api.getCourseAnalytics(courseId) }) { it.data ?: throw Exception("Failed") }

    suspend fun getCourseStudents(courseId: String): Result<List<StudentProgress>> =
        safeApiCall({ api.getCourseStudents(courseId) }) { it.data?.students ?: emptyList() }

    // ── Admin ─────────────────────────────────────────────────────────────────

    suspend fun getUserProfile(userId: String): Result<UserProfile> =
        safeApiCall({ api.getProfile(userId) }) { resp ->
            val user = resp.data?.user ?: throw Exception("Not found")
            // API повертає data.user.profile (та сама форма, що і /profiles/me)
            val profile = user.profile ?: UserProfile(
                id = null, userId = user.id,
                avatar = null, bio = null, phone = null,
                user = User(
                    id = user.id, name = user.name, surname = user.surname,
                    email = user.email, role = user.role
                )
            )
            // Якщо профіль не містить вкладеного user — додаємо його вручну
            if (profile.user == null) profile.copy(
                user = User(
                    id = user.id, name = user.name, surname = user.surname,
                    email = user.email, role = user.role
                )
            ) else profile
        }

    suspend fun getAdminUsers(role: String? = null, query: String? = null, page: Int = 1): Result<UsersListData> =
        safeApiCall({ api.getAdminUsers(role, query, page) }) { it.data ?: throw Exception("Failed") }

    suspend fun changeUserRole(id: String, role: String): Result<String> =
        safeApiCall({ api.changeUserRole(id, ChangeRoleRequest(role)) }) { it.message }

    suspend fun banUser(id: String): Result<String> =
        safeApiCall({ api.banUser(id) }) { it.message }

    suspend fun unbanUser(id: String): Result<String> =
        safeApiCall({ api.unbanUser(id) }) { it.message }

    suspend fun getAdminCourses(status: String? = null, page: Int = 1): Result<CoursesData> =
        safeApiCall({ api.getAdminCourses(status, page) }) { it.data ?: CoursesData(emptyList()) }

    suspend fun adminUnpublishCourse(id: String): Result<String> =
        safeApiCall({ api.adminUnpublishCourse(id) }) { it.message }
}