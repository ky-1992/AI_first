package com.example.neonataldiary.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.neonataldiary.data.local.entity.MediaAttachment
import com.example.neonataldiary.ui.component.DatePickerField
import com.example.neonataldiary.ui.component.MediaPicker
import com.example.neonataldiary.ui.viewmodel.DiaryEvent
import com.example.neonataldiary.ui.viewmodel.DiaryViewModel
import com.example.neonataldiary.util.MediaHelper
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryEntryScreen(
    onSaveSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: DiaryViewModel = hiltViewModel(),
    mediaHelper: MediaHelper = remember { MediaHelper(LocalContext.current) }
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val uiState by viewModel.uiState.collectAsState()
    
    var date by remember { mutableStateOf(LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))) }
    var feeding by remember { mutableStateOf("") }
    var mood by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    
    val selectedImages = remember { mutableStateListOf<Uri>() }
    val selectedVideos = remember { mutableStateListOf<Uri>() }
    
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        selectedImages.addAll(uris)
    }
    
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5)
    ) { uris ->
        selectedVideos.addAll(uris)
    }
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        bitmap?.let {
            // Handle captured image
        }
    }
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DiaryEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                else -> {}
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("新增日记") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DatePickerField(
                    value = date,
                    onValueChange = { date = it },
                    label = "日期"
                )
                
                OutlinedTextField(
                    value = feeding,
                    onValueChange = { feeding = it },
                    label = { Text("喂养情况") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    placeholder = { Text("例如：母乳 5次，配方奶 2次") }
                )
                
                OutlinedTextField(
                    value = mood,
                    onValueChange = { mood = it },
                    label = { Text("情绪状态") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    placeholder = { Text("例如：活泼好动，睡眠良好") }
                )
                
                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("体重 (kg)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    placeholder = { Text("例如：3.5") }
                )
                
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    placeholder = { Text("其他需要记录的信息...") }
                )
                
                MediaPicker(
                    selectedImages = selectedImages.toList(),
                    selectedVideos = selectedVideos.toList(),
                    onPickImage = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onPickVideo = {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    },
                    onRemoveImage = { uri ->
                        selectedImages.remove(uri)
                    },
                    onRemoveVideo = { uri ->
                        selectedVideos.remove(uri)
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = {
                        scope.launch {
                            val mediaList = mutableListOf<MediaAttachment>()
                            
                            selectedImages.forEach { uri ->
                                mediaHelper.saveImage(uri)?.let { path ->
                                    mediaList.add(
                                        MediaAttachment(
                                            logId = 0,
                                            type = MediaAttachment.TYPE_IMAGE,
                                            path = path
                                        )
                                    )
                                }
                            }
                            
                            selectedVideos.forEach { uri ->
                                mediaHelper.saveVideo(uri)?.let { path ->
                                    mediaList.add(
                                        MediaAttachment(
                                            logId = 0,
                                            type = MediaAttachment.TYPE_VIDEO,
                                            path = path
                                        )
                                    )
                                }
                            }
                            
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
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    Text("保存")
                }
                
                Spacer(modifier = Modifier.height(32.dp))
            }
            
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
