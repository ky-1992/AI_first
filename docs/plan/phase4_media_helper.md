# Phase 4: 媒体存储工具

**阶段**: Phase 4  
**日期**: Day 5  
**前置条件**: Phase 3 完成  
**目标**: 实现媒体文件的保存、读取和删除功能

---

## 4.1 阶段目标

- 实现 MediaHelper 工具类
- 支持图片和视频的保存
- 支持缩略图生成
- 支持文件删除

## 4.2 交付物

- util/MediaHelper.kt

## 4.3 详细任务

### 4.3.1 创建 MediaHelper

**MediaHelper.kt**
```kotlin
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
            File(path).exists() && File(path).length() > 0
        } catch (e: Exception) {
            false
        }
    }
    
    companion object {
        private const val MEDIA_DIR = "media"
        private const val THUMBNAIL_SIZE = 200
    }
}
```

### 4.3.2 更新 Hilt 模块

**AppModule.kt (补充)**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object UtilModule {
    
    @Provides
    @Singleton
    fun provideMediaHelper(@ApplicationContext context: Context): MediaHelper {
        return MediaHelper(context)
    }
}
```

### 4.3.3 添加系统服务依赖

在 build.gradle.kts 中确保以下依赖存在：
```kotlin
implementation("androidx.media:media:1.7.0")
```

## 4.4 验收标准

- [ ] 图片可保存到私有目录
- [ ] 视频可保存到私有目录
- [ ] 文件删除功能正常
- [ ] 缩略图生成正常
- [ ] 文件大小限制检查

## 4.5 测试用例

| 用例 | 描述 |
|-----|------|
| TC4-1 | 保存图片，验证文件创建 |
| TC4-2 | 保存视频，验证文件创建 |
| TC4-3 | 删除文件，验证文件删除 |
| TC4-4 | 生成缩略图，验证尺寸 |
| TC4-5 | 验证不存在的文件 |

## 4.6 注意事项

- 处理大文件时注意内存管理
- 视频缩略图可能耗时，考虑异步处理
- 文件名使用时间戳避免冲突

---

**阶段状态**: 待执行
