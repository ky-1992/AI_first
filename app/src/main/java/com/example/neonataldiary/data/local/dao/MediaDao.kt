package com.example.neonataldiary.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.neonataldiary.data.local.entity.MediaAttachment
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    
    @Query("SELECT * FROM media_attachments WHERE log_id = :logId ORDER BY created_at ASC")
    fun getMediaForLog(logId: Long): Flow<List<MediaAttachment>>
    
    @Query("SELECT * FROM media_attachments WHERE log_id = :logId ORDER BY created_at ASC")
    suspend fun getMediaForLogSync(logId: Long): List<MediaAttachment>
    
    @Query("SELECT * FROM media_attachments WHERE id = :id")
    suspend fun getMediaById(id: Long): MediaAttachment?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(media: MediaAttachment): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(media: List<MediaAttachment>)
    
    @Delete
    suspend fun delete(media: MediaAttachment)
    
    @Query("DELETE FROM media_attachments WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("DELETE FROM media_attachments WHERE log_id = :logId")
    suspend fun deleteByLogId(logId: Long)
    
    @Query("SELECT * FROM media_attachments WHERE log_id = :logId AND type = :type")
    suspend fun getMediaByType(logId: Long, type: String): List<MediaAttachment>
    
    @Query("SELECT COUNT(*) FROM media_attachments WHERE log_id = :logId")
    suspend fun getMediaCountForLog(logId: Long): Int
}
