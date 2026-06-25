package com.eduplatform.ui.lessons

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduplatform.data.models.*
import com.eduplatform.data.repository.EduRepository
import com.eduplatform.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LessonsViewModel @Inject constructor(
    private val repository: EduRepository
) : ViewModel() {

    private val _lessons = MutableStateFlow<List<Lesson>>(emptyList())
    val lessons: StateFlow<List<Lesson>> = _lessons.asStateFlow()

    private val _blocks = MutableStateFlow<List<CourseBlock>>(emptyList())
    val blocks: StateFlow<List<CourseBlock>> = _blocks.asStateFlow()

    private val _lesson = MutableStateFlow<Lesson?>(null)
    val lesson: StateFlow<Lesson?> = _lesson.asStateFlow()

    private val _progress = MutableStateFlow<LessonProgressData?>(null)
    val progress: StateFlow<LessonProgressData?> = _progress.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    // true — користувач не записаний на курс (сервер повернув 403)
    private val _needsEnrollment = MutableStateFlow(false)
    val needsEnrollment: StateFlow<Boolean> = _needsEnrollment.asStateFlow()

    fun loadBlocks(courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _needsEnrollment.value = false
            when (val r = repository.getCourseBlocks(courseId)) {
                is Result.Success -> {
                    _blocks.value = r.data
                    _error.value = null
                    // Прогрес завантажуємо тільки якщо блоки доступні
                    when (val p = repository.getCourseProgress(courseId)) {
                        is Result.Success -> _progress.value = p.data
                        else -> {}
                    }
                }
                is Result.Error -> {
                    if (r.code == 403) {
                        // Не записаний — показуємо CTA «Записатись», а не помилку
                        _needsEnrollment.value = true
                        _error.value = null
                    } else {
                        _error.value = r.message
                    }
                }
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun loadLessons(courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repository.getLessons(courseId)) {
                is Result.Success -> { _lessons.value = r.data; _error.value = null }
                is Result.Error -> _error.value = r.message
                else -> {}
            }
            when (val r = repository.getCourseProgress(courseId)) {
                is Result.Success -> _progress.value = r.data
                else -> {}
            }
            _isLoading.value = false
        }
    }

    private val _isLessonCompleted = MutableStateFlow(false)
    val isLessonCompleted: StateFlow<Boolean> = _isLessonCompleted.asStateFlow()

    // Тест, прив'язаний до поточного уроку (null — якщо тесту немає)
    private val _lessonTest = MutableStateFlow<com.eduplatform.data.models.BlockTestInfo?>(null)
    val lessonTest: StateFlow<com.eduplatform.data.models.BlockTestInfo?> = _lessonTest.asStateFlow()

    fun loadLesson(lessonId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repository.getLesson(lessonId)) {
                is Result.Success -> {
                    _lesson.value = r.data
                    _error.value = null
                    checkLessonCompletion(r.data.courseId, lessonId)
                    // Завантажуємо блоки курсу, щоб знайти тест для цього уроку
                    when (val b = repository.getCourseBlocks(r.data.courseId)) {
                        is Result.Success -> _lessonTest.value =
                            b.data.find { it.lesson.id == lessonId }?.test
                        else -> _lessonTest.value = null
                    }
                }
                is Result.Error -> _error.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    private fun checkLessonCompletion(courseId: String, lessonId: String) {
        viewModelScope.launch {
            when (val r = repository.getCourseProgress(courseId)) {
                is Result.Success -> _isLessonCompleted.value =
                    r.data.lessons.find { it.lessonId == lessonId }?.completed == true
                else -> {}
            }
        }
    }

    fun markCompleted(lessonId: String, completed: Boolean = true) {
        viewModelScope.launch {
            when (val r = repository.markLesson(lessonId, completed)) {
                is Result.Success -> {
                    _message.value = if (completed) "Урок позначено пройденим!" else "Прогрес скасовано"
                    _isLessonCompleted.value = completed
                }
                is Result.Error -> _message.value = r.message
                else -> {}
            }
        }
    }

    fun createLesson(courseId: String, req: CreateLessonRequest, onDone: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repository.createLesson(courseId, req)) {
                is Result.Success -> { _message.value = "Урок створено"; onDone() }
                is Result.Error -> _message.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun deleteLesson(id: String, courseId: String) {
        viewModelScope.launch {
            when (repository.deleteLesson(id)) {
                is Result.Success -> { _message.value = "Урок видалено"; loadLessons(courseId) }
                is Result.Error -> {}
                else -> {}
            }
        }
    }

    fun clearMessage() { _message.value = null }
}