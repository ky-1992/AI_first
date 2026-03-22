package com.example.neonataldiary.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.neonataldiary.ui.screen.DiaryDetailScreen
import com.example.neonataldiary.ui.screen.DiaryEntryScreen
import com.example.neonataldiary.ui.screen.DiaryListScreen
import com.example.neonataldiary.ui.screen.MediaViewerScreen
import com.example.neonataldiary.ui.viewmodel.DiaryViewModel
import java.net.URLDecoder

@Composable
fun NavGraph(
    navController: NavHostController,
    viewModel: DiaryViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.List.route
    ) {
        composable(Screen.List.route) {
            DiaryListScreen(
                diaries = viewModel.diaries.value,
                onAddClick = { navController.navigate(Screen.Entry.route) },
                onDiaryClick = { id ->
                    viewModel.loadDiary(id)
                    navController.navigate(Screen.Detail.createRoute(id))
                }
            )
        }
        
        composable(Screen.Entry.route) {
            DiaryEntryScreen(
                onSaveSuccess = {
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument(Screen.DIARY_ID_ARG) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val diaryId = backStackEntry.arguments?.getLong(Screen.DIARY_ID_ARG) ?: return@composable
            DiaryDetailScreen(
                diary = viewModel.selectedDiary.value,
                onBack = {
                    viewModel.clearSelectedDiary()
                    navController.popBackStack()
                },
                onMediaClick = { path ->
                    navController.navigate(Screen.MediaViewer.createRoute(path))
                },
                onDeleteSuccess = {
                    viewModel.clearSelectedDiary()
                    navController.popBackStack()
                }
            )
        }
        
        composable(
            route = Screen.MediaViewer.route,
            arguments = listOf(
                navArgument(Screen.MEDIA_PATH_ARG) { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val encodedPath = backStackEntry.arguments?.getString(Screen.MEDIA_PATH_ARG) ?: return@composable
            val path = URLDecoder.decode(encodedPath, "UTF-8")
            MediaViewerScreen(
                path = path,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
