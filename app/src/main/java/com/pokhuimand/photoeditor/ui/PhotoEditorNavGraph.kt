package com.pokhuimand.photoeditor.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.pokhuimand.photoeditor.data.AppContainer
import com.pokhuimand.photoeditor.filters.FilterFactory
import com.pokhuimand.photoeditor.ui.Destinations.EDIT_ROUTE
import com.pokhuimand.photoeditor.ui.Destinations.HOME_ROUTE
import com.pokhuimand.photoeditor.ui.screens.edit.EditRoute
import com.pokhuimand.photoeditor.ui.screens.edit.EditViewModel
import com.pokhuimand.photoeditor.ui.screens.gallery.GalleryRoute
import com.pokhuimand.photoeditor.ui.screens.gallery.GalleryViewModel

object Destinations {
    const val HOME_ROUTE = "gallery"
    const val EDIT_ROUTE = "edit/{photoId}"
    const val CUBE_ROUTE = "cube"
}

@Composable
fun PhotoEditorNavGraph(
    appContainer: AppContainer,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = HOME_ROUTE
) {
    NavHost(
        navController,
        startDestination,
        modifier
    ) {
        composable(route = HOME_ROUTE) {
            val galleryViewModel: GalleryViewModel =
                viewModel(
                    factory = GalleryViewModel.provideFactory(
                        appContainer.photosRepository,
                        onOpenEditor = { photoId ->
                            navController.navigate("edit/$photoId")
                        })
                )
            GalleryRoute(galleryViewModel = galleryViewModel)
        }
        composable(
            route = EDIT_ROUTE,
            enterTransition = { this.slideIntoContainer(towards = AnimatedContentTransitionScope.SlideDirection.Left) },
            exitTransition = { this.slideOutOfContainer(towards = AnimatedContentTransitionScope.SlideDirection.Right) }) {
            val photoId = checkNotNull(checkNotNull(it.arguments).getString("photoId"))
            val filters = FilterFactory(appContainer).buildSet()
            val editViewModel: EditViewModel =
                viewModel(
                    factory = EditViewModel.provideFactory(
                        photoId,
                        appContainer.photosRepository,
                        navigateBack = navController::navigateUp,
                        onPhotoSave = { img ->
                            appContainer.photosRepository.savePhoto(
                                photoId,
                                img
                            )
                        },
                        filters
                    )
                )
            EditRoute(viewModel = editViewModel)
        }
    }
}