package com.example.neonataldiary.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

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
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "log_id")
    val logId: Long,
    
    @ColumnInfo(name = "type")
    val type: String,
    
    @ColumnInfo(name = "path")
    val path: String,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
) {
    companion object {
        const val TYPE_IMAGE = "image"
        const val TYPE_VIDEO = "video"
    }
    
    fun isImage(): Boolean = type == TYPE_IMAGE
    fun isVideo(): Boolean = type == TYPE_VIDEO
}
