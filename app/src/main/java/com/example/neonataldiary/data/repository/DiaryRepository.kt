package com.example.neonataldiary.data.repository

import com.example.neonataldiary.data.local.dao.DiaryDao
import com.example.neonataldiary.data.local.dao.MediaDao
import com.example.neonataldiary.data.local.entity.DailyLog
import com.example.neonataldiary.data.local.entity.MediaAttachment
import com.example.neonataldiary.domain.model.DiaryWithMedia
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
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
    
    suspend fun getDiaryWithMediaSync(id: Long): DiaryWithMedia? {
        return diaryDao.getDiaryWithMediaSync(id)
    }
    
    suspend fun getDiaryByDate(date: String): DiaryWithMedia? {
        return diaryDao.getDiaryByDate(date)
    }
    
    suspend fun getDiaryById(id: Long): DailyLog? {
        return diaryDao.getDiaryById(id)
    }
    
    suspend fun insertDiary(log: DailyLog): Long {
        return diaryDao.insert(log)
    }
    
    suspend fun insertDiaryWithMedia(
        log: DailyLog,
        mediaList: List<MediaAttachment>
    ): Long {
        val logId = diaryDao.insert(log)
        if (mediaList.isNotEmpty()) {
            val mediaWithLogId = mediaList.map { it.copy(logId = logId) }
            mediaDao.insertAll(mediaWithLogId)
        }
        return logId
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
    
    suspend fun insertMedia(media: MediaAttachment): Long {
        return mediaDao.insert(media)
    }
    
    suspend fun insertMediaList(mediaList: List<MediaAttachment>) {
        mediaDao.insertAll(mediaList)
    }
    
    suspend fun deleteMedia(media: MediaAttachment) {
        mediaDao.delete(media)
    }
    
    suspend fun deleteMediaById(id: Long) {
        mediaDao.deleteById(id)
    }
    
    suspend fun getDiaryCount(): Int {
        return diaryDao.getDiaryCount()
    }
}
