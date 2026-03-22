# Phase 5: UI 界面开发

**阶段**: Phase 5  
**日期**: Day 6-8  
**前置条件**: Phase 4 完成  
**目标**: 实现所有 Compose UI 界面

---

## 5.1 阶段目标

- 实现主题配置
- 实现导航结构
- 实现日记列表页面
- 实现新增日记页面
- 实现日记详情页面
- 实现媒体查看页面
- 实现可复用组件

## 5.2 交付物

- ui/theme/Color.kt
- ui/theme/Theme.kt
- ui/theme/Type.kt
- ui/navigation/NavGraph.kt
- ui/screen/DiaryListScreen.kt
- ui/screen/DiaryEntryScreen.kt
- ui/screen/DiaryDetailScreen.kt
- ui/screen/MediaViewerScreen.kt
- ui/component/DiaryCard.kt
- ui/component/MediaPicker.kt
- ui/component/DatePickerField.kt

## 5.3 详细任务

### 5.3.1 主题配置

**Color.kt**
```kotlin
val Pink40 = Color(0xFFFFB6C1)
val Pink80 = Color(0xFFFF69B4)
val Blue40 = Color(0xFF87CEEB)
val Background = Color(0xFFFFF5F7)
val Surface = Color(0xFFFFFFFF)
val OnSurface = Color(0xFF333333)
val OnSurfaceVariant = Color(0xFF666666)
```

**Theme.kt**
```kotlin
@Composable
fun NeonatalDiaryTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Pink40,
            secondary = Blue40,
            tertiary = Pink80,
            background = Background,
            surface = Surface,
            onPrimary = Color.White,
            onSecondary = Color.White,
            onBackground = OnSurface,
            onSurface = OnSurface,
            onSurfaceVariant = OnSurfaceVariant
        ),
        typography = Typography,
        content = content
    )
}
```

### 5.3.2 导航结构

**NavGraph.kt**
```kotlin
sealed class Screen(val route: String) {
    object List : Screen("list")
    object Entry : Screen("entry")
    object Detail : Screen("detail/{diaryId}") {
        fun createRoute(diaryId: Long) = "detail/$diaryId"
    }
    object MediaViewer : Screen("media/{path}") {
        fun createRoute(path: String) = "media/${URLEncoder.encode(path, "UTF-8")}"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: DiaryViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.List.route
    ) {
        composable(Screen.List.route) {
            DiaryListScreen(
                diaries = viewModel.diaries.value,
                onAddClick = { navController.navigate(Screen.Entry.route) },
                onDiaryClick = { id -> 
                    navController.navigate(Screen.Detail.createRoute(id))
                }
            )
        }
        
        composable(Screen.Entry.route) {
            DiaryEntryScreen(
                onSave = { /* 保存逻辑 */ },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("diaryId") { type = NavType.LongType })
        ) { backStackEntry ->
            val diaryId = backStackEntry.arguments?.getLong("diaryId") ?: return@composable
            DiaryDetailScreen(
                diary = viewModel.selectedDiary.value,
                onBack = { navController.popBackStack() },
                onMediaClick = { path -> 
                    navController.navigate(Screen.MediaViewer.createRoute(path))
                }
            )
        }
        
        composable(Screen.MediaViewer.route) { backStackEntry ->
            val path = backStackEntry.arguments?.getString("path") ?: return@composable
            MediaViewerScreen(
                path = URLDecoder.decode(path, "UTF-8"),
                onBack = { navController.popBackStack() }
            )
        }
    }
}
```

### 5.3.3 可复用组件

**DiaryCard.kt**
```kotlin
@Composable
fun DiaryCard(
    diaryWithMedia: DiaryWithMedia,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // 日期
            Text(
                text = diaryWithMedia.log.date,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 摘要信息
            diaryWithMedia.log.feeding?.let {
                Text("🍽 $it", style = MaterialTheme.typography.bodyMedium)
            }
            diaryWithMedia.log.mood?.let {
                Text("😊 $it", style = MaterialTheme.typography.bodyMedium)
            }
            diaryWithMedia.log.weightKg?.let {
                Text("⚖️ ${it}kg", style = MaterialTheme.typography.bodyMedium)
            }
            
            // 媒体缩略图
            if (diaryWithMedia.media.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    diaryWithMedia.media.take(3).forEach { media ->
                        MediaThumbnail(media = media, size = 60.dp)
                    }
                    if (diaryWithMedia.media.size > 3) {
                        Text("+${diaryWithMedia.media.size - 3}")
                    }
                }
            }
        }
    }
}
```

**MediaPicker.kt**
```kotlin
@Composable
fun MediaPicker(
    images: List<Uri>,
    videos: List<Uri>,
    onPickImage: () -> Unit,
    onPickVideo: () -> Unit,
    onRemoveImage: (Uri) -> Unit,
    onRemoveVideo: (Uri) -> Unit
) {
    Column {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onPickImage) {
                Icon(Icons.Default.Image, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("添加图片")
            }
            OutlinedButton(onClick = onPickVideo) {
                Icon(Icons.Default.VideoCameraBack, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("添加视频")
            }
        }
        
        // 图片预览
        if (images.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(images) { uri ->
                    Box {
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )
                        IconButton(
                            onClick = { onRemoveImage(uri) },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Default.Close, "移除")
                        }
                    }
                }
            }
        }
        
        // 视频预览
        if (videos.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(videos) { uri ->
                    Box {
                        Icon(
                            Icons.Default.VideoFile,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp)
                        )
                        IconButton(
                            onClick = { onRemoveVideo(uri) },
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Icon(Icons.Default.Close, "移除")
                        }
                    }
                }
            }
        }
    }
}
```

### 5.3.4 页面实现

**DiaryListScreen.kt**
```kotlin
@Composable
fun DiaryListScreen(
    diaries: List<DiaryWithMedia>,
    onAddClick: () -> Unit,
    onDiaryClick: (Long) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新生儿日记") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加")
            }
        }
    ) { padding ->
        if (diaries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Book,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "暂无日记",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "点击 + 按钮添加第一条日记",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                items(diaries, key = { it.log.id }) { diary ->
                    DiaryCard(
                        diaryWithMedia = diary,
                        onClick = { onDiaryClick(diary.log.id) }
                    )
                }
            }
        }
    }
}
```

**DiaryEntryScreen.kt**
```kotlin
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEntryScreen(
    onSave: (String, String?, String?, Float?, String?, List<Uri>, List<Uri>) -> Unit,
    onBack: () -> Unit
) {
    var date by remember { mutableStateOf(LocalDate.now().toString()) }
    var feeding by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var images by remember { mutableStateOf(listOf<Uri>()) }
    var videos by remember { mutableStateOf(listOf<Uri>()) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新增日记") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 日期选择
            OutlinedTextField(
                value = date,
                onValueChange = { },
                label = { Text("日期") },
                readOnly = true,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, "选择日期")
                    }
                }
            )
            
            // 喂养情况
            OutlinedTextField(
                value = feeding,
                onValueChange = { feeding = it },
                label = { Text("喂养情况") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            
            // 情绪状态
            OutlinedTextField(
                value = mood,
                onValueChange = { mood = it },
                label = { Text("情绪状态") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            
            // 体重
            OutlinedTextField(
                value = weight,
                onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
                label = { Text("体重 (kg)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            
            // 备注
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("备注") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            // 媒体选择
            MediaPicker(
                images = images,
                videos = videos,
                onPickImage = { /* Activity Result */ },
                onPickVideo = { /* Activity Result */ },
                onRemoveImage = { images = images - it },
                onRemoveVideo = { videos = videos - it }
            )
            
            // 保存按钮
            Button(
                onClick = {
                    onSave(
                        date,
                        feeding.ifBlank { null },
                        mood.ifBlank { null },
                        weight.toFloatOrNull(),
                        note.ifBlank { null },
                        images,
                        videos
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存")
            }
        }
    }
    
    if (showDatePicker) {
        DatePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateSelected = { date = it }
        )
    }
}
```

**DiaryDetailScreen.kt**
```kotlin
@Composable
fun DiaryDetailScreen(
    diary: DiaryWithMedia?,
    onBack: () -> Unit,
    onMediaClick: (String) -> Unit,
    onDelete: () -> Unit
) {
    if (diary == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(diary.log.date) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                },
                actions = {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "删除")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 喂养情况
            diary.log.feeding?.let {
                DetailSection(title = "喂养情况", content = it)
            }
            
            // 情绪状态
            diary.log.mood?.let {
                DetailSection(title = "情绪状态", content = it)
            }
            
            // 体重
            diary.log.weightKg?.let {
                DetailSection(title = "体重", content = "${it}kg")
            }
            
            // 备注
            diary.log.note?.let {
                DetailSection(title = "备注", content = it)
            }
            
            // 媒体
            if (diary.media.isNotEmpty()) {
                Text("媒体", style = MaterialTheme.typography.titleMedium)
                MediaGrid(
                    media = diary.media,
                    onMediaClick = onMediaClick
                )
            }
        }
    }
}
```

### 5.3.5 媒体查看器

**MediaViewerScreen.kt**
```kotlin
@Composable
fun MediaViewerScreen(
    path: String,
    onBack: () -> Unit
) {
    val isVideo = path.endsWith(".mp4") || path.endsWith(".mov")
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (isVideo) {
            // Video Player implementation
            AndroidView(
                factory = { ctx ->
                    VideoView(ctx).apply {
                        setVideoPath(path)
                        setOnPreparedListener { 
                            it.isLooping = true
                            start()
                        }
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AsyncImage(
                model = File(path),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "关闭",
                tint = Color.White
            )
        }
    }
}
```

## 5.4 验收标准

- [ ] 主题样式统一
- [ ] 导航流畅
- [ ] 日记列表显示正常
- [ ] 新增日记表单可用
- [ ] 媒体选择器正常
- [ ] 详情页显示完整
- [ ] 媒体查看器正常

## 5.5 注意事项

- 处理 Activity Result 契约
- 权限请求处理
- 大图片加载优化
- 视频播放兼容

---

**阶段状态**: 待执行
