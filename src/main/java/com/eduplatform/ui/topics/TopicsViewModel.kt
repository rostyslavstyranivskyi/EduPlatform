package com.eduplatform.ui.topics

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
class TopicsViewModel @Inject constructor(
    private val repository: EduRepository
) : ViewModel() {

    private val _topics = MutableStateFlow<List<TopicWithLessons>>(emptyList())
    val topics: StateFlow<List<TopicWithLessons>> = _topics.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    fun loadTopics(courseId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repository.getTopics(courseId)) {
                is Result.Success -> { _topics.value = r.data; _error.value = null }
                is Result.Error -> {
                    if (r.code == 403) {
                        // Не власник курсу — теми недоступні через API,
                        // але уроки можуть бути доступні. Показуємо порожній список тем
                        // без помилки, щоб студент/чужий викладач бачив уроки без тем.
                        _topics.value = emptyList()
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

    fun createTopic(courseId: String, title: String, description: String?, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val req = CreateTopicRequest(title = title.trim(), description = description?.takeIf { it.isNotBlank() })
            when (val r = repository.createTopic(courseId, req)) {
                is Result.Success -> { _message.value = "Тему створено"; loadTopics(courseId); onDone() }
                is Result.Error -> _message.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun updateTopic(id: String, courseId: String, title: String, description: String?, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val req = CreateTopicRequest(title = title.trim(), description = description?.takeIf { it.isNotBlank() })
            when (val r = repository.updateTopic(id, req)) {
                is Result.Success -> { _message.value = "Тему оновлено"; loadTopics(courseId); onDone() }
                is Result.Error -> _message.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun deleteTopic(topicId: String, courseId: String) {
        viewModelScope.launch {
            when (val r = repository.deleteTopic(topicId)) {
                is Result.Success -> { _message.value = "Тему видалено"; loadTopics(courseId) }
                is Result.Error -> _message.value = r.message
                else -> {}
            }
        }
    }

    fun assignLessons(topicId: String, lessonIds: List<String>, courseId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repository.assignLessonsToTopic(topicId, lessonIds)) {
                is Result.Success -> { _message.value = "Уроки призначено"; loadTopics(courseId); onDone() }
                is Result.Error -> _message.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    fun clearMessage() { _message.value = null }
}