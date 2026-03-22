package com.example.neonataldiary.domain.model

import androidx.room.Embedded
import androidx.room.Relation
import com.example.neonataldiary.data.local.entity.DailyLog
import com.example.neonataldiary.data.local.entity.MediaAttachment

data class DiaryWithMedia(
    @Embedded
    val log: DailyLog,
    
    @Relation(
        parentColumn = "id",
        entityColumn = "log_id"
    )
    val media: List<MediaAttachment>
) {
    val imageCount: Int
        get() = media.count { it.isImage() }
    
    val videoCount: Int
        get() = media.count { it.isVideo() }
    
    val hasMedia: Boolean
        get() = media.isNotEmpty()
}
