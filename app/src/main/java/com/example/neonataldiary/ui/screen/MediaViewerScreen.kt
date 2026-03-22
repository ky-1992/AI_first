package com.example.neonataldiary.ui.screen

import android.net.Uri
import android.view.ViewGroup
import android.widget.MediaController
import android.widget.VideoView
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import java.io.File

@Composable
fun MediaViewerScreen(
    path: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVideo = path.endsWith(".mp4") || path.endsWith(".mov") || path.endsWith(".avi")
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { onBack() }
                )
            }
    ) {
        if (isVideo) {
            VideoPlayer(
                path = path,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            AsyncImage(
                model = File(path),
                contentDescription = "图片",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(40.dp)
                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "关闭",
                tint = Color.White
            )
        }
    }
}

@Composable
private fun VideoPlayer(
    path: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isPlaying by remember { mutableStateOf(true) }
    
    DisposableEffect(Unit) {
        onDispose {
            // Cleanup if needed
        }
    }
    
    AndroidView(
        factory = { ctx ->
            VideoView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                
                val mediaController = MediaController(ctx)
                mediaController.setAnchorView(this)
                setMediaController(mediaController)
                
                setVideoURI(Uri.parse(path))
                
                setOnPreparedListener { mp ->
                    mp.isLooping = true
                    start()
                    isPlaying = true
                }
                
                setOnErrorListener { _, _, _ ->
                    false
                }
            }
        },
        modifier = modifier,
        update = { videoView ->
            // Update if needed
        }
    )
}
