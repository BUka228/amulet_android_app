package com.example.amulet.feature.practices.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.example.amulet.feature.practices.presentation.home.PracticesHomeRoute
import com.example.amulet.feature.practices.presentation.details.PracticeDetailsRoute
import com.example.amulet.feature.practices.presentation.course.CourseDetailsRoute
import com.example.amulet.feature.practices.presentation.search.PracticesSearchRoute

object PracticesGraph {
    const val route: String = "practices_graph"
}

object PracticesDestination {
    const val home: String = "practices/home"
    const val practiceDetails: String = "practices/practice/{practiceId}"
    const val courseDetails: String = "practices/course/{courseId}"
    const val search: String = "practices/search"
}

fun NavController.navigateToPracticesHome(popUpToInclusive: Boolean = false) {
    navigate(PracticesDestination.home) {
        if (popUpToInclusive) {
            popUpTo(PracticesDestination.home) { inclusive = true }
        }
        launchSingleTop = true
    }
}

fun NavController.navigateToPracticeDetails(practiceId: String) {
    navigate("practices/practice/$practiceId") { launchSingleTop = true }
}

fun NavController.navigateToCourseDetails(courseId: String) {
    navigate("practices/course/$courseId") { launchSingleTop = true }
}

fun NavController.navigateToPracticesSearch() {
    navigate(PracticesDestination.search) { launchSingleTop = true }
}

fun NavGraphBuilder.practicesGraph(
    navController: NavController,
) {
    navigation(startDestination = PracticesDestination.home, route = PracticesGraph.route) {
        composable(route = PracticesDestination.home) {
            PracticesHomeRoute(
                onOpenPractice = { id -> navController.navigateToPracticeDetails(id) },
                onOpenCourse = { id -> navController.navigateToCourseDetails(id) },
                onOpenSearch = { navController.navigateToPracticesSearch() }
            )
        }
        composable(route = PracticesDestination.practiceDetails) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("practiceId") ?: return@composable
            PracticeDetailsRoute(
                practiceId = id,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = PracticesDestination.courseDetails) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("courseId") ?: return@composable
            CourseDetailsRoute(
                courseId = id,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(route = PracticesDestination.search) {
            PracticesSearchRoute(
                onOpenPractice = { id -> navController.navigateToPracticeDetails(id) },
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
