package com.eduplatform.ui.courses

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduplatform.data.models.*
import com.eduplatform.data.repository.EduRepository
import com.eduplatform.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoursesViewModel @Inject constructor(
    private val repository: EduRepository
) : ViewModel() {

    private val _courses = MutableStateFlow<List<Course>>(emptyList())
    val courses: StateFlow<List<Course>> = _courses.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedPrice = MutableStateFlow("any")
    val selectedPrice: StateFlow<String> = _selectedPrice.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _selectedSort = MutableStateFlow("newest")
    val selectedSort: StateFlow<String> = _selectedSort.asStateFlow()

    private val _totalPages = MutableStateFlow(1)
    val totalPages: StateFlow<Int> = _totalPages.asStateFlow()

    private val _currentPage = MutableStateFlow(1)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    fun loadCategories() {
        viewModelScope.launch {
            when (val r = repository.getCategories()) {
                is Result.Success -> _categories.value = r.data
                else -> {}
            }
        }
    }

    init {
        loadCategories()
        loadCourses()
    }

    @OptIn(FlowPreview::class)
    fun onSearchQuery(q: String) {
        _searchQuery.value = q
        viewModelScope.launch {
            kotlinx.coroutines.delay(400)
            if (_searchQuery.value == q) {
                _currentPage.value = 1
                loadCourses()
            }
        }
    }

    fun setPrice(price: String) { _selectedPrice.value = price; _currentPage.value = 1; loadCourses() }
    fun setCategory(categoryId: String?) { _selectedCategory.value = categoryId; _currentPage.value = 1; loadCourses() }
    fun setSort(sort: String) { _selectedSort.value = sort; _currentPage.value = 1; loadCourses() }
    fun nextPage() { if (_currentPage.value < _totalPages.value) { _currentPage.value++; loadCourses() } }
    fun prevPage() { if (_currentPage.value > 1) { _currentPage.value--; loadCourses() } }

    fun loadCourses() {
        viewModelScope.launch {
            _isLoading.value = true
            val q = _searchQuery.value.takeIf { it.length >= 3 }
            when (val r = repository.getCourses(q, _selectedCategory.value, _selectedPrice.value, _selectedSort.value, _currentPage.value)) {
                is Result.Success -> {
                    _courses.value = r.data.courses
                    _totalPages.value = r.data.totalPages
                    _error.value = null
                }
                is Result.Error -> _error.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
    }

    // Course detail
    private val _course = MutableStateFlow<Course?>(null)
    val course: StateFlow<Course?> = _course.asStateFlow()

    private val _enrollMessage = MutableStateFlow<String?>(null)
    val enrollMessage: StateFlow<String?> = _enrollMessage.asStateFlow()

    // The backend has no "amI enrolled" flag on the course itself, so we infer
    // it from the student's own enrolled-courses list (the same one the
    // Progress tab uses). Harmless no-op for teacher/admin viewers — it'll
    // just come back empty for them.
    private val _isEnrolled = MutableStateFlow(false)
    val isEnrolled: StateFlow<Boolean> = _isEnrolled.asStateFlow()

    fun loadCourse(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            when (val r = repository.getCourse(id)) {
                is Result.Success -> { _course.value = r.data; _error.value = null }
                is Result.Error -> _error.value = r.message
                else -> {}
            }
            _isLoading.value = false
        }
        checkEnrollment(id)
    }

    private fun checkEnrollment(courseId: String) {
        viewModelScope.launch {
            when (val r = repository.getMyProgress()) {
                is Result.Success -> _isEnrolled.value = r.data.any { it.course?.id == courseId }
                else -> {}
            }
        }
    }

    fun enroll(courseId: String) {
        viewModelScope.launch {
            when (val r = repository.enrollCourse(courseId)) {
                is Result.Success -> { _enrollMessage.value = r.data; _isEnrolled.value = true }
                is Result.Error -> _enrollMessage.value = r.message
                else -> {}
            }
        }
    }

    fun publishCourse(id: String) {
        viewModelScope.launch {
            repository.publishCourse(id)
            loadCourse(id)
        }
    }

    fun unpublishCourse(id: String) {
        viewModelScope.launch {
            repository.unpublishCourse(id)
            loadCourse(id)
        }
    }

    fun clearEnrollMessage() { _enrollMessage.value = null }
}
