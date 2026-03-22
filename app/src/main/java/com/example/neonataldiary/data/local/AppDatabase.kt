package com.example.neonataldiary.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.neonataldiary.data.local.dao.DiaryDao
import com.example.neonataldiary.data.local.dao.MediaDao
import com.example.neonataldiary.data.local.entity.DailyLog
import com.example.neonataldiary.data.local.entity.MediaAttachment

@Database(
    entities = [
        DailyLog::class,
        MediaAttachment::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun diaryDao(): DiaryDao
    abstract fun mediaDao(): MediaDao
    
    companion object {
        const val DATABASE_NAME = "neonatal_diary_db"
    }
}
