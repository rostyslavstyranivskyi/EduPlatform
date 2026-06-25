package com.eduplatform.ui.tests

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
class TestViewModel @Inject constructor(
    private val repository: EduRepository
) : ViewModel() {

    private val _test = MutableStateFlow<Test?>(null)
    val test: StateFlow<Test?> = _test.asStateFlow()

    private val _result = MutableStateFlow<TestResult?>(null)
    val result: StateFlow<TestResult?> = _result.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun loadTest(courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _result.value = null
            when (val r = repository.getTest(courseId)) {
                is Result.Success -> { _test.value = r.data; _error.value = null }
                is Result.Error -> _error.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun loadLessonTest(lessonId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _result.value = null
            when (val r = repository.getLessonTest(lessonId)) {
                is Result.Success -> { _test.value = r.data; _error.value = null }
                is Result.Error -> _error.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun loadTopicTest(topicId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _result.value = null
            when (val r = repository.getTopicTest(topicId)) {
                is Result.Success -> { _test.value = r.data; _error.value = null }
                is Result.Error -> _error.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun submitTest(testId: String, answers: List<Int>) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repository.submitTest(testId, answers)) {
                is Result.Success -> { _result.value = r.data; _error.value = null }
                is Result.Error -> _error.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun createTest(courseId: String, req: CreateTestRequest, onDone: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repository.createTest(courseId, req)) {
                is Result.Success -> { _message.value = "Тест створено"; onDone() }
                is Result.Error -> _message.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun createLessonTest(lessonId: String, req: CreateTestRequest, onDone: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repository.createLessonTest(lessonId, req)) {
                is Result.Success -> { _message.value = "Тест створено"; onDone() }
                is Result.Error -> _message.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun createTopicTest(topicId: String, req: CreateTestRequest, onDone: () -> Unit) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repository.createTopicTest(topicId, req)) {
                is Result.Success -> { _message.value = "Тест створено"; onDone() }
                is Result.Error -> _message.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun clearMessage() { _message.value = null }
    fun resetResult() { _result.value = null }
}
