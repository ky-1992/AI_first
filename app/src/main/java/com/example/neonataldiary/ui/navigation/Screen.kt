package com.example.neonataldiary.ui.navigation

sealed class Screen(val route: String) {
    object List : Screen("list")
    object Entry : Screen("entry")
    object Detail : Screen("detail/{diaryId}") {
        fun createRoute(diaryId: Long) = "detail/$diaryId"
    }
    object MediaViewer : Screen("media/{path}") {
        fun createRoute(path: String) = "media/${java.net.URLEncoder.encode(path, "UTF-8")}"
    }
    
    companion object {
        const val DIARY_ID_ARG = "diaryId"
        const val MEDIA_PATH_ARG = "path"
    }
}
