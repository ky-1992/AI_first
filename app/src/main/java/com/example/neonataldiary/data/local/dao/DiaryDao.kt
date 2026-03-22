package com.example.neonataldiary.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.neonataldiary.data.local.entity.DailyLog
import com.example.neonataldiary.domain.model.DiaryWithMedia
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {
    
    @Query("SELECT * FROM daily_logs ORDER BY date DESC")
    fun getAllDiaries(): Flow<List<DailyLog>>
    
    @Transaction
    @Query("SELECT * FROM daily_logs ORDER BY date DESC")
    fun getAllDiariesWithMedia(): Flow<List<DiaryWithMedia>>
    
    @Transaction
    @Query("SELECT * FROM daily_logs WHERE id = :id")
    fun getDiaryWithMedia(id: Long): Flow<DiaryWithMedia?>
    
    @Transaction
    @Query("SELECT * FROM daily_logs WHERE id = :id")
    suspend fun getDiaryWithMediaSync(id: Long): DiaryWithMedia?
    
    @Transaction
    @Query("SELECT * FROM daily_logs WHERE date = :date LIMIT 1")
    suspend fun getDiaryByDate(date: String): DiaryWithMedia?
    
    @Query("SELECT * FROM daily_logs WHERE id = :id")
    suspend fun getDiaryById(id: Long): DailyLog?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: DailyLog): Long
    
    @Update
    suspend fun update(log: DailyLog)
    
    @Delete
    suspend fun delete(log: DailyLog)
    
    @Query("DELETE FROM daily_logs WHERE id = :id")
    suspend fun deleteById(id: Long)
    
    @Query("SELECT COUNT(*) FROM daily_logs")
    suspend fun getDiaryCount(): Int
}
