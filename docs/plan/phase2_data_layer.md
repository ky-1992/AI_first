# Phase 2: 数据层实现

**阶段**: Phase 2  
**日期**: Day 2-3  
**前置条件**: Phase 1 完成  
**目标**: 实现数据库实体、DAO 和 Room 数据库

---

## 2.1 阶段目标

- 实现 DailyLog 和 MediaAttachment 实体
- 实现 DiaryDao 和 MediaDao
- 实现 AppDatabase

## 2.2 交付物

- data/local/entity/DailyLog.kt
- data/local/entity/MediaAttachment.kt
- data/local/dao/DiaryDao.kt
- data/local/dao/MediaDao.kt
- data/local/AppDatabase.kt

## 2.3 详细任务

### 2.3.1 创建实体 (Entity)

**DailyLog.kt**
```kotlin
@Entity(tableName = "daily_logs")
data class DailyLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val date: String, // YYYY-MM-DD
    val feeding: String? = null,
    val mood: String? = null,
    @ColumnInfo(name = "weight_kg") val weightKg: Float? = null,
    val note: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
```

**MediaAttachment.kt**
```kotlin
@Entity(
    tableName = "media_attachments",
    foreignKeys = [
        ForeignKey(
            entity = DailyLog::class,
            parentColumns = ["id"],
            childColumns = ["log_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("log_id")]
)
data class MediaAttachment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "log_id") val logId: Long,
    val type: String, // "image" | "video"
    val path: String,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
```

### 2.3.2 创建关联模型

**DiaryWithMedia.kt**
```kotlin
data class DiaryWithMedia(
    @Embedded val log: DailyLog,
    @Relation(
        parentColumn = "id",
        entityColumn = "log_id"
    )
    val media: List<MediaAttachment>
)
```

### 2.3.3 创建 DAO

**DiaryDao.kt**
```kotlin
@Dao
interface DiaryDao {
    @Query("SELECT * FROM daily_logs ORDER BY date DESC")
    fun getAllDiaries(): Flow<List<DailyLog>>
    
    @Transaction
    @Query("SELECT * FROM daily_logs WHERE id = :id")
    fun getDiaryWithMedia(id: Long): Flow<DiaryWithMedia?>
    
    @Transaction
    @Query("SELECT * FROM daily_logs ORDER BY date DESC")
    fun getAllDiariesWithMedia(): Flow<List<DiaryWithMedia>>
    
    @Transaction
    @Query("SELECT * FROM daily_logs WHERE date = :date LIMIT 1")
    suspend fun getDiaryByDate(date: String): DiaryWithMedia?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: DailyLog): Long
    
    @Update
    suspend fun update(log: DailyLog)
    
    @Delete
    suspend fun delete(log: DailyLog)
    
    @Query("DELETE FROM daily_logs WHERE id = :id")
    suspend fun deleteById(id: Long)
}
```

**MediaDao.kt**
```kotlin
@Dao
interface MediaDao {
    @Query("SELECT * FROM media_attachments WHERE log_id = :logId")
    fun getMediaForLog(logId: Long): Flow<List<MediaAttachment>>
    
    @Query("SELECT * FROM media_attachments WHERE log_id = :logId")
    suspend fun getMediaForLogSync(logId: Long): List<MediaAttachment>
    
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
}
```

### 2.3.4 创建 Database

**AppDatabase.kt**
```kotlin
@Database(
    entities = [DailyLog::class, MediaAttachment::class],
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
```

## 2.4 创建 Hilt 模块

**AppModule.kt**
```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()
    }
    
    @Provides
    @Singleton
    fun provideDiaryDao(database: AppDatabase): DiaryDao {
        return database.diaryDao()
    }
    
    @Provides
    @Singleton
    fun provideMediaDao(database: AppDatabase): MediaDao {
        return database.mediaDao()
    }
}
```

## 2.5 验收标准

- [ ] DailyLog 实体可正常创建和读取
- [ ] MediaAttachment 实体可正常创建和读取
- [ ] 外键约束生效（级联删除）
- [ ] DiaryWithMedia 联合查询正常
- [ ] 数据库迁移配置正确

## 2.6 测试用例

| 用例 | 描述 |
|-----|------|
| TC2-1 | 插入新日记并查询 |
| TC2-2 | 插入日记和媒体，验证关联查询 |
| TC2-3 | 删除日记，验证媒体级联删除 |
| TC2-4 | 按日期查询日记 |

---

**阶段状态**: 待执行
