package com.eduplatform.ui

sealed class Screen(val route: String) {
    // Auth
    object Login : Screen("login")
    object Register : Screen("register")

    // Common
    object CourseList : Screen("courses")
    object CourseDetail : Screen("course/{courseId}") {
        fun create(id: String) = "course/$id"
    }
    object LessonDetail : Screen("lesson/{lessonId}") {
        fun create(id: String) = "lesson/$id"
    }
    object TestScreen : Screen("test/{courseId}") {
        fun create(id: String) = "test/$id"
    }
    object LessonTestScreen : Screen("test_lesson/{lessonId}") {
        fun create(id: String) = "test_lesson/$id"
    }
    object CourseBlocks : Screen("course_blocks/{courseId}") {
        fun create(id: String) = "course_blocks/$id"
    }
    object TestResult : Screen("test_result/{courseId}") {
        fun create(id: String) = "test_result/$id"
    }
    object Profile : Screen("profile")
    object MyProgress : Screen("my_progress")

    // Teacher
    object TeacherCourses : Screen("teacher_courses")
    object CreateCourse : Screen("create_course")
    object EditCourse : Screen("edit_course/{courseId}") {
        fun create(id: String) = "edit_course/$id"
    }
    object ManageLessons : Screen("manage_lessons/{courseId}") {
        fun create(id: String) = "manage_lessons/$id"
    }
    object CreateLesson : Screen("create_lesson/{courseId}") {
        fun create(id: String) = "create_lesson/$id"
    }
    object CreateTest : Screen("create_test/{courseId}") {
        fun create(id: String) = "create_test/$id"
    }
    object CreateLessonTest : Screen("create_test_lesson/{lessonId}") {
        fun create(id: String) = "create_test_lesson/$id"
    }
    object AnalyticsDashboard : Screen("analytics_dashboard")
    object CourseAnalytics : Screen("course_analytics/{courseId}") {
        fun create(id: String) = "course_analytics/$id"
    }

    // Topics
    object TopicList : Screen("topics/{courseId}") {
        fun create(id: String) = "topics/$id"
    }
    object TopicDetail : Screen("topic/{topicId}/{courseId}") {
        fun create(topicId: String, courseId: String) = "topic/$topicId/$courseId"
    }
    object TopicTest : Screen("test_topic/{topicId}") {
        fun create(id: String) = "test_topic/$id"
    }
    object CreateTopicTest : Screen("create_test_topic/{topicId}") {
        fun create(id: String) = "create_test_topic/$id"
    }
    object EditTest : Screen("edit_test/{testId}/{courseId}") {
        fun create(testId: String, courseId: String) = "edit_test/$testId/$courseId"
    }
    object EditLessonTest : Screen("edit_test_lesson/{testId}/{lessonId}") {
        fun create(testId: String, lessonId: String) = "edit_test_lesson/$testId/$lessonId"
    }
    object EditTopicTest : Screen("edit_test_topic/{testId}/{topicId}") {
        fun create(testId: String, topicId: String) = "edit_test_topic/$testId/$topicId"
    }
    object ManageTopics : Screen("manage_topics/{courseId}") {
        fun create(id: String) = "manage_topics/$id"
    }
    object AssignLessonsToTopic : Screen("assign_lessons/{topicId}/{courseId}") {
        fun create(topicId: String, courseId: String) = "assign_lessons/$topicId/$courseId"
    }

    // Admin
    object AdminUsers : Screen("admin_users")
    object AdminCourses : Screen("admin_courses")
    object AdminCourseDetail : Screen("admin_course/{courseId}") {
        fun create(id: String) = "admin_course/$id"
    }
    object AdminCourseBlocks : Screen("admin_course_blocks/{courseId}") {
        fun create(id: String) = "admin_course_blocks/$id"
    }
    object AdminLessonDetail : Screen("admin_lesson/{lessonId}") {
        fun create(id: String) = "admin_lesson/$id"
    }
    object AdminLessonTest : Screen("admin_lesson_test/{lessonId}") {
        fun create(id: String) = "admin_lesson_test/$id"
    }
}
