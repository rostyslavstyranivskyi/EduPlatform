package com.eduplatform.data.api

import com.eduplatform.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface EduApiService {

    // ── Auth ─────────────────────────────────────────────────────────────────

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<GenericResponse>

    @POST("auth/refresh")
    suspend fun refresh(): Response<AuthResponse>

    // ── Courses ───────────────────────────────────────────────────────────────

    @GET("categories")
    suspend fun getCategories(): Response<CategoriesResponse>

    @GET("courses")
    suspend fun getCourses(
        @Query("q") query: String? = null,
        @Query("categoryId") categoryId: String? = null,
        @Query("price") price: String? = null,
        @Query("sortBy") sortBy: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<CoursesResponse>

    @GET("courses/my")
    suspend fun getMyCourses(): Response<CoursesResponse>

    @GET("courses/{id}")
    suspend fun getCourse(@Path("id") id: String): Response<CourseResponse>

    @POST("courses")
    suspend fun createCourse(@Body request: CreateCourseRequest): Response<CourseResponse>

    @PATCH("courses/{id}")
    suspend fun updateCourse(
        @Path("id") id: String,
        @Body request: CreateCourseRequest
    ): Response<CourseResponse>

    @PATCH("courses/{id}/publish")
    suspend fun publishCourse(@Path("id") id: String): Response<CourseResponse>

    @PATCH("courses/{id}/unpublish")
    suspend fun unpublishCourse(@Path("id") id: String): Response<CourseResponse>

    @POST("courses/{id}/enroll")
    suspend fun enrollCourse(@Path("id") id: String): Response<EnrollResponse>

    // ── Lessons ───────────────────────────────────────────────────────────────

    @GET("lessons/course/{courseId}")
    suspend fun getLessons(@Path("courseId") courseId: String): Response<LessonsResponse>

    @GET("lessons/course/{courseId}/blocks")
    suspend fun getCourseBlocks(@Path("courseId") courseId: String): Response<CourseBlocksResponse>

    @GET("lessons/{id}")
    suspend fun getLesson(@Path("id") id: String): Response<LessonResponse>

    @POST("lessons/course/{courseId}")
    suspend fun createLesson(
        @Path("courseId") courseId: String,
        @Body request: CreateLessonRequest
    ): Response<LessonResponse>

    @PATCH("lessons/{id}")
    suspend fun updateLesson(
        @Path("id") id: String,
        @Body request: CreateLessonRequest
    ): Response<LessonResponse>

    @DELETE("lessons/{id}")
    suspend fun deleteLesson(@Path("id") id: String): Response<GenericResponse>

    // ── Topics ────────────────────────────────────────────────────────────────

    @GET("topics/course/{courseId}")
    suspend fun getTopics(@Path("courseId") courseId: String): Response<TopicsResponse>

    @POST("topics/course/{courseId}")
    suspend fun createTopic(
        @Path("courseId") courseId: String,
        @Body request: CreateTopicRequest
    ): Response<TopicResponse>

    @PATCH("topics/{id}")
    suspend fun updateTopic(
        @Path("id") id: String,
        @Body request: CreateTopicRequest
    ): Response<TopicResponse>

    @DELETE("topics/{id}")
    suspend fun deleteTopic(@Path("id") id: String): Response<GenericResponse>

    @PUT("topics/{id}/lessons")
    suspend fun assignLessonsToTopic(
        @Path("id") id: String,
        @Body request: AssignLessonsRequest
    ): Response<GenericResponse>

    // ── Tests ─────────────────────────────────────────────────────────────────

    @GET("tests/course/{courseId}")
    suspend fun getTest(@Path("courseId") courseId: String): Response<TestResponse>

    @POST("tests/course/{courseId}")
    suspend fun createTest(
        @Path("courseId") courseId: String,
        @Body request: CreateTestRequest
    ): Response<TestResponse>

    // Block tests (new): one optional test per lesson
    @GET("tests/lesson/{lessonId}")
    suspend fun getLessonTest(@Path("lessonId") lessonId: String): Response<TestResponse>

    @POST("tests/lesson/{lessonId}")
    suspend fun createLessonTest(
        @Path("lessonId") lessonId: String,
        @Body request: CreateTestRequest
    ): Response<TestResponse>

    // Topic tests: one optional test per topic
    @GET("tests/topic/{topicId}")
    suspend fun getTopicTest(@Path("topicId") topicId: String): Response<TestResponse>

    @POST("tests/topic/{topicId}")
    suspend fun createTopicTest(
        @Path("topicId") topicId: String,
        @Body request: CreateTestRequest
    ): Response<TestResponse>

    @PATCH("tests/{id}")
    suspend fun updateTest(
        @Path("id") id: String,
        @Body request: CreateTestRequest
    ): Response<TestResponse>

    @PATCH("tests/topic/{topicId}")
    suspend fun updateTopicTest(
        @Path("topicId") topicId: String,
        @Body request: CreateTestRequest
    ): Response<TestResponse>

    @PATCH("tests/lesson/{lessonId}")
    suspend fun updateLessonTest(
        @Path("lessonId") lessonId: String,
        @Body request: CreateTestRequest
    ): Response<TestResponse>

    @DELETE("tests/lesson/{lessonId}")
    suspend fun deleteLessonTest(@Path("lessonId") lessonId: String): Response<GenericResponse>

    // Видалення тесту теми. Бекенд має лише окремі маршрути за lessonId/topicId,
    // спільного DELETE /tests/{id} не існує.
    @DELETE("tests/topic/{topicId}")
    suspend fun deleteTopicTest(@Path("topicId") topicId: String): Response<GenericResponse>

    @POST("tests/{id}/submit")
    suspend fun submitTest(
        @Path("id") id: String,
        @Body request: SubmitTestRequest
    ): Response<TestResultResponse>

    // ── Progress ──────────────────────────────────────────────────────────────

    @GET("progress/me")
    suspend fun getMyProgress(): Response<ProgressResponse>

    @GET("progress/courses/{courseId}")
    suspend fun getCourseProgress(@Path("courseId") courseId: String): Response<LessonProgressResponse>

    @POST("progress/lessons/{lessonId}")
    suspend fun markLessonProgress(
        @Path("lessonId") lessonId: String,
        @Body request: MarkProgressRequest
    ): Response<GenericResponse>

    // ── Profiles ──────────────────────────────────────────────────────────────

    @GET("profiles/me")
    suspend fun getMyProfile(): Response<MyProfileResponse>

    @PATCH("profiles/me")
    suspend fun updateProfile(@Body request: UpdateProfileRequest): Response<ProfileResponse>

    @GET("profiles/{id}")
    suspend fun getProfile(@Path("id") id: String): Response<MyProfileResponse>

    // ── Analytics ─────────────────────────────────────────────────────────────

    @GET("analytics/dashboard")
    suspend fun getAnalyticsDashboard(): Response<DashboardResponse>

    @GET("analytics/courses/{courseId}")
    suspend fun getCourseAnalytics(@Path("courseId") courseId: String): Response<CourseAnalyticsResponse>

    @GET("analytics/courses/{courseId}/students")
    suspend fun getCourseStudents(@Path("courseId") courseId: String): Response<StudentsProgressResponse>

    // ── Admin ─────────────────────────────────────────────────────────────────

    @GET("admin/users")
    suspend fun getAdminUsers(
        @Query("role") role: String? = null,
        @Query("q") query: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<UsersListResponse>

    @PATCH("admin/users/{id}/role")
    suspend fun changeUserRole(
        @Path("id") id: String,
        @Body request: ChangeRoleRequest
    ): Response<GenericResponse>

    @PATCH("admin/users/{id}/ban")
    suspend fun banUser(@Path("id") id: String): Response<GenericResponse>

    @PATCH("admin/users/{id}/unban")
    suspend fun unbanUser(@Path("id") id: String): Response<GenericResponse>

    @GET("admin/courses")
    suspend fun getAdminCourses(
        @Query("status") status: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<CoursesResponse>

    @PATCH("admin/courses/{id}/unpublish")
    suspend fun adminUnpublishCourse(@Path("id") id: String): Response<GenericResponse>
}