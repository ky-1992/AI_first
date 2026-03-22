package com.example.neonataldiary.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val mediaDir: File by lazy {
        File(context.filesDir, MEDIA_DIR).apply {
            if (!exists()) mkdirs()
        }
    }
    
    fun saveImage(uri: Uri): String? {
        return saveToMedia(uri, "img", "jpg")
    }
    
    fun saveVideo(uri: Uri): String? {
        return saveToMedia(uri, "vid", "mp4")
    }
    
    private fun saveToMedia(uri: Uri, prefix: String, extension: String): String? {
        return try {
            val fileName = "${prefix}_${System.currentTimeMillis()}.$extension"
            val destFile = File(mediaDir, fileName)
            
            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun deleteFile(path: String): Boolean {
        return try {
            File(path).delete()
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
    
    fun deleteFiles(paths: List<String>) {
        paths.forEach { deleteFile(it) }
    }
    
    fun getThumbnail(path: String, isVideo: Boolean = false): Bitmap? {
        return try {
            if (isVideo) {
                getVideoThumbnail(path)
            } else {
                getImageThumbnail(path)
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun getImageThumbnail(path: String): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(path, options)
        
        options.inSampleSize = calculateInSampleSize(options, THUMBNAIL_SIZE, THUMBNAIL_SIZE)
        options.inJustDecodeBounds = false
        
        return BitmapFactory.decodeFile(path, options)
    }
    
    private fun getVideoThumbnail(path: String): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(path)
            val bitmap = retriever.getFrameAtTime(0)
            retriever.release()
            bitmap
        } catch (e: Exception) {
            null
        }
    }
    
    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height, width) = options.outHeight to options.outWidth
        var inSampleSize = 1
        
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            
            while (halfHeight / inSampleSize >= reqHeight && 
                   halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        
        return inSampleSize
    }
    
    fun saveThumbnail(bitmap: Bitmap, originalPath: String): String? {
        return try {
            val file = File(mediaDir, "thumb_${File(originalPath).name}")
            FileOutputStream(file).use { output ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, output)
            }
            file.absolutePath
        } catch (e: Exception) {
            null
        }
    }
    
    fun getFileSize(path: String): Long {
        return try {
            File(path).length()
        } catch (e: Exception) {
            0L
        }
    }
    
    fun isValidMedia(path: String): Boolean {
        return try {
            val file = File(path)
            file.exists() && file.length() > 0
        } catch (e: Exception) {
            false
        }
    }
    
    fun getMediaFiles(): List<File> {
        return try {
            mediaDir.listFiles()?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun clearAllMedia() {
        try {
            mediaDir.listFiles()?.forEach { it.delete() }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    companion object {
        private const val MEDIA_DIR = "media"
        private const val THUMBNAIL_SIZE = 200
    }
}
