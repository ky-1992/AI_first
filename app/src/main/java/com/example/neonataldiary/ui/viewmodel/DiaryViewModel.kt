package com.example.neonataldiary.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.neonataldiary.data.local.entity.DailyLog
import com.example.neonataldiary.data.local.entity.MediaAttachment
import com.example.neonataldiary.data.repository.DiaryRepository
import com.example.neonataldiary.domain.model.DiaryWithMedia
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiaryViewModel @Inject constructor(
    private val repository: DiaryRepository
) : ViewModel() {
    
    val diaries: StateFlow<List<DiaryWithMedia>> = repository
        .getAllDiariesWithMedia()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    
    private val _selectedDiary = MutableStateFlow<DiaryWithMedia?>(null)
    val selectedDiary: StateFlow<DiaryWithMedia?> = _selectedDiary.asStateFlow()
    
    private val _uiState = MutableStateFlow(DiaryUiState())
    val uiState: StateFlow<DiaryUiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<DiaryEvent>()
    val events = _events.asSharedFlow()
    
    fun loadDiary(id: Long) {
        viewModelScope.launch {
            repository.getDiaryWithMedia(id).collect { diary ->
                _selectedDiary.value = diary
            }
        }
    }
    
    fun addDiary(
        date: String,
        feeding: String?,
        mood: String?,
        weight: Float?,
        note: String?,
        mediaList: List<MediaAttachment>,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                
                val log = DailyLog(
                    date = date,
                    feeding = feeding,
                    mood = mood,
                    weightKg = weight,
                    note = note
                )
                
                repository.insertDiaryWithMedia(log, mediaList)
                
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                _events.emit(DiaryEvent.SaveSuccess)
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "保存失败"
                )
                _events.emit(DiaryEvent.ShowError(e.message ?: "保存失败"))
            }
        }
    }
    
    fun updateDiary(
        log: DailyLog,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                repository.updateDiary(log)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                _events.emit(DiaryEvent.UpdateSuccess)
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "更新失败"
                )
                _events.emit(DiaryEvent.ShowError(e.message ?: "更新失败"))
            }
        }
    }
    
    fun deleteDiary(log: DailyLog, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                repository.deleteDiary(log)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                _events.emit(DiaryEvent.DeleteSuccess)
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "删除失败"
                )
                _events.emit(DiaryEvent.ShowError(e.message ?: "删除失败"))
            }
        }
    }
    
    fun deleteDiaryById(id: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                repository.deleteDiaryById(id)
                _uiState.value = _uiState.value.copy(isLoading = false, isSuccess = true)
                _events.emit(DiaryEvent.DeleteSuccess)
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "删除失败"
                )
                _events.emit(DiaryEvent.ShowError(e.message ?: "删除失败"))
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    fun clearSelectedDiary() {
        _selectedDiary.value = null
    }
}

data class DiaryUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

sealed class DiaryEvent {
    object SaveSuccess : DiaryEvent()
    object UpdateSuccess : DiaryEvent()
    object DeleteSuccess : DiaryEvent()
    data class ShowError(val message: String) : DiaryEvent()
}
