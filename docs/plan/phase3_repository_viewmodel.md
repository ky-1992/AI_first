# Phase 3: Repository 与 ViewModel 实现

**阶段**: Phase 3  
**日期**: Day 4  
**前置条件**: Phase 2 完成  
**目标**: 实现业务逻辑层和数据仓库

---

## 3.1 阶段目标

- 实现 DiaryRepository
- 实现 DiaryViewModel
- 配置 Hilt 依赖注入

## 3.2 交付物

- data/repository/DiaryRepository.kt
- ui/viewmodel/DiaryViewModel.kt

## 3.3 详细任务

### 3.3.1 创建 Repository

**DiaryRepository.kt**
```kotlin
class DiaryRepository @Inject constructor(
    private val diaryDao: DiaryDao,
    private val mediaDao: MediaDao
) {
    fun getAllDiariesWithMedia(): Flow<List<DiaryWithMedia>> {
        return diaryDao.getAllDiariesWithMedia()
    }
    
    fun getDiaryWithMedia(id: Long): Flow<DiaryWithMedia?> {
        return diaryDao.getDiaryWithMedia(id)
    }
    
    suspend fun getDiaryByDate(date: String): DiaryWithMedia? {
        return diaryDao.getDiaryByDate(date)
    }
    
    suspend fun insertDiary(log: DailyLog): Long {
        return diaryDao.insert(log)
    }
    
    suspend fun insertMedia(media: MediaAttachment): Long {
        return mediaDao.insert(media)
    }
    
    suspend fun insertMediaList(mediaList: List<MediaAttachment>) {
        mediaDao.insertAll(mediaList)
    }
    
    suspend fun updateDiary(log: DailyLog) {
        diaryDao.update(log)
    }
    
    suspend fun deleteDiary(log: DailyLog) {
        diaryDao.delete(log)
    }
    
    suspend fun deleteDiaryById(id: Long) {
        diaryDao.deleteById(id)
    }
    
    suspend fun deleteMedia(media: MediaAttachment) {
        mediaDao.delete(media)
    }
    
    suspend fun deleteMediaByLogId(logId: Long) {
        mediaDao.deleteByLogId(logId)
    }
}
```

### 3.3.2 创建 ViewModel

**DiaryViewModel.kt**
```kotlin
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
                val log = DailyLog(
                    date = date,
                    feeding = feeding,
                    mood = mood,
                    weightKg = weight,
                    note = note
                )
                val logId = repository.insertDiary(log)
                
                if (mediaList.isNotEmpty()) {
                    val mediaWithLogId = mediaList.map { it.copy(logId = logId) }
                    repository.insertMediaList(mediaWithLogId)
                }
                
                _uiState.value = _uiState.value.copy(isSuccess = true)
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "保存失败"
                )
            }
        }
    }
    
    fun deleteDiary(log: DailyLog, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                repository.deleteDiary(log)
                onComplete()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "删除失败"
                )
            }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class DiaryUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)
```

### 3.3.3 添加 Hilt 模块

**AppModule.kt (补充)**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    
    @Provides
    @Singleton
    fun provideDiaryRepository(
        diaryDao: DiaryDao,
        mediaDao: MediaDao
    ): DiaryRepository {
        return DiaryRepository(diaryDao, mediaDao)
    }
}
```

## 3.4 验收标准

- [ ] Repository 方法可正常调用
- [ ] ViewModel 状态管理正常
- [ ] Flow 数据流正常更新
- [ ] 错误处理正常

## 3.5 测试用例

| 用例 | 描述 |
|-----|------|
| TC3-1 | 添加新日记，验证数据保存 |
| TC3-2 | 获取日记列表，验证 Flow 更新 |
| TC3-3 | 删除日记，验证级联删除 |
| TC3-4 | ViewModel 错误状态处理 |

---

**阶段状态**: 待执行
