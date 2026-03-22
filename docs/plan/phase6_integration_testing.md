# Phase 6: 集成测试与调优

**阶段**: Phase 6  
**日期**: Day 9-10  
**前置条件**: Phase 5 完成  
**目标**: 集成测试、问题修复、发布准备

---

## 6.1 阶段目标

- 集成所有模块
- 执行功能测试
- 性能优化
- 准备发布版本

## 6.2 交付物

- 可发布的 Debug APK
- 测试报告
- 优化后的应用

## 6.3 详细任务

### 6.3.1 模块集成

#### 1. 更新 MainActivity

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            NeonatalDiaryTheme {
                val navController = rememberNavController()
                val viewModel: DiaryViewModel = hiltViewModel()
                
                NavGraph(
                    navController = navController,
                    viewModel = viewModel
                )
            }
        }
    }
}
```

#### 2. 更新 DiaryEntryScreen 集成

```kotlin
@Composable
fun DiaryEntryScreen(
    viewModel: DiaryViewModel,
    mediaHelper: MediaHelper,
    onSaveSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var feeding by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var images by remember { mutableStateOf(listOf<Uri>()) }
    var videos by remember { mutableStateOf(listOf<Uri>()) }
    
    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        images = images + uris
    }
    
    // Video picker
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        videos = videos + uris
    }
    
    // Save logic
    val coroutineScope = rememberCoroutineScope()
    
    Scaffold(
        // ...
        floatingActionButton = {
            Button(
                onClick = {
                    coroutineScope.launch {
                        val imagePaths = images.mapNotNull { mediaHelper.saveImage(it) }
                        val videoPaths = videos.mapNotNull { mediaHelper.saveVideo(it) }
                        
                        val mediaList = (imagePaths.map { 
                            MediaAttachment(logId = 0, type = "image", path = it)
                        } + videoPaths.map {
                            MediaAttachment(logId = 0, type = "video", path = it)
                        })
                        
                        viewModel.addDiary(
                            date = date,
                            feeding = feeding.ifBlank { null },
                            mood = mood.ifBlank { null },
                            weight = weight.toFloatOrNull(),
                            note = note.ifBlank { null },
                            mediaList = mediaList,
                            onComplete = onSaveSuccess
                        )
                    }
                }
            ) {
                Text("保存")
            }
        }
    ) { padding ->
        // Form fields...
    }
}
```

#### 3. 权限处理

```kotlin
@Composable
fun RequestPermissions(
    onPermissionsGranted: @Composable () -> Unit
) {
    val permissions = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VIDEO
        )
    )
    
    LaunchedEffect(Unit) {
        permissions.launchMultiplePermissionRequest()
    }
    
    when {
        permissions.allPermissionsGranted -> {
            onPermissionsGranted()
        }
        permissions.shouldShowRationale -> {
            PermissionRationale(
                onRequestPermissions = { permissions.launchMultiplePermissionRequest() }
            )
        }
        else -> {
            PermissionDenied()
        }
    }
}
```

### 6.3.2 功能测试

#### 测试用例执行

| 用例ID | 描述 | 预期结果 | 实际结果 | 状态 |
|-------|------|---------|---------|-----|
| TC01 | 创建日记并保存 | 日记成功保存 | | |
| TC02 | 添加图片附件 | 图片保存成功 | | |
| TC03 | 添加视频附件 | 视频保存成功 | | |
| TC04 | 查看日记详情 | 正确显示所有字段 | | |
| TC05 | 删除日记及媒体 | 日记和媒体同时删除 | | |
| TC06 | 权限正常流程 | 授权后正常使用 | | |
| TC07 | 权限拒绝流程 | 提示并引导设置 | | |
| TC08 | 日期验证 | 无效日期提示错误 | | |
| TC09 | 体重范围验证 | 超出范围提示错误 | | |
| TC10 | 空列表状态 | 显示引导提示 | | |

### 6.3.3 性能优化

#### 1. 图片加载优化

```kotlin
// 使用 Coil 配置
val imageLoader = ImageLoader.Builder(context)
    .memoryCache {
        MemoryCache.Builder(context)
            .maxSizePercent(0.25)
            .build()
    }
    .diskCache {
        DiskCache.Builder()
            .directory(cacheDir.resolve("image_cache"))
            .maxSizePercent(0.02)
            .build()
    }
    .crossfade(true)
    .build()
```

#### 2. 数据库查询优化

```kotlin
@Query("SELECT * FROM daily_logs ORDER BY date DESC")
fun getAllDiaries(): Flow<List<DailyLog>>
// 添加 LIMIT 分页，避免一次性加载所有数据
```

#### 3. 列表优化

```kotlin
LazyColumn(
    modifier = Modifier.fillMaxSize(),
    state = rememberLazyListState(),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp)
) {
    items(
        items = diaries,
        key = { it.log.id }
    ) { diary ->
        DiaryCard(diaryWithMedia = diary)
    }
}
```

### 6.3.4 错误处理完善

```kotlin
// ViewModel 中添加错误处理
viewModelScope.launch {
    try {
        // 操作
    } catch (e: Exception) {
        _uiState.value = _uiState.value.copy(
            error = when (e) {
                is IOException -> "存储空间不足"
                is SecurityException -> "权限被拒绝"
                else -> "操作失败，请重试"
            }
        )
    }
}

// UI 层展示错误
val uiState by viewModel.uiState.collectAsState()

LaunchedEffect(uiState.error) {
    uiState.error?.let { error ->
        snackbarHostState.showSnackbar(error)
        viewModel.clearError()
    }
}
```

### 6.3.5 UI/UX 完善

#### 1. 加载状态

```kotlin
@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary
        )
    }
}
```

#### 2. 空状态

```kotlin
@Composable
fun EmptyState(onAddClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Book,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(Modifier.height(16.dp))
        Text(
            text = "暂无日记",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "记录宝宝成长的每一天",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("添加日记")
        }
    }
}
```

### 6.3.6 发布准备

#### 1. 配置应用图标

在 `res/mipmap-*` 目录添加应用图标

#### 2. 配置混淆规则

**proguard-rules.pro**
```kotlin
# Keep Room entities
-keep class com.example.neonataldiary.data.** { *; }

# Keep Hilt
-keep class dagger.hilt.** { *; }

# Keep Coil
-dontwarn coil.**
```

#### 3. 构建 Release APK

```bash
./gradlew assembleRelease
```

### 6.4 验收标准

- [ ] 所有功能测试通过
- [ ] 应用启动时间 < 2秒
- [ ] 列表滚动流畅 (60fps)
- [ ] 图片加载无明显延迟
- [ ] Release APK 可正常安装运行
- [ ] 无崩溃或严重错误

### 6.5 输出文档

#### 测试报告模板

```
# NeonatalDiary v1.0.0 测试报告

## 测试概况
- 测试日期: 
- 测试人员: 
- 测试版本: 

## 测试结果汇总
| 测试类型 | 通过数 | 失败数 | 总数 |
|---------|-------|-------|-----|
| 功能测试 |   |   |   |
| 界面测试 |   |   |   |
| 性能测试 |   |   |   |

## 问题列表
| ID | 描述 | 严重程度 | 状态 |
|----|------|---------|-----|
|    |      |         |     |

## 结论
-
```

---

**阶段状态**: 待执行

---

## 整体项目计划总结

| 阶段 | 内容 | 时间 | 状态 |
|-----|------|-----|-----|
| Phase 1 | 项目搭建与依赖配置 | Day 1 | 待执行 |
| Phase 2 | 数据层实现 | Day 2-3 | 待执行 |
| Phase 3 | Repository 与 ViewModel | Day 4 | 待执行 |
| Phase 4 | 媒体存储工具 | Day 5 | 待执行 |
| Phase 5 | UI 界面开发 | Day 6-8 | 待执行 |
| Phase 6 | 集成测试与调优 | Day 9-10 | 待执行 |

**预计总工期**: 10 个工作日
