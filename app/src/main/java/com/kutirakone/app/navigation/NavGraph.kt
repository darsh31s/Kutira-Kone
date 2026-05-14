package com.kutirakone.app.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
// Placeholder imports for screens
import com.kutirakone.app.ui.auth.AuthScreen
import com.kutirakone.app.ui.chat.ChatScreen
import com.kutirakone.app.ui.customer.CustomerDashboardScreen
import com.kutirakone.app.ui.customer.ListingDetailScreen
import com.kutirakone.app.ui.customer.MapViewScreen
import com.kutirakone.app.ui.inspire.InspireScreen
import com.kutirakone.app.ui.review.ReviewScreen
import com.kutirakone.app.ui.vendor.RequestManagementScreen
import com.kutirakone.app.ui.vendor.UploadScreen
import com.kutirakone.app.ui.customer.PreviousOrdersScreen
import com.kutirakone.app.ui.profile.ProfileScreen
import com.kutirakone.app.ui.vendor.VendorDashboardScreen

@Composable
fun NavGraph(navController: NavHostController) {
    // Initial route check should ideally be done in MainActivity or AuthViewModel
    // defaulting to Auth for now
    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route,
        enterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
        exitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, animationSpec = tween(300)) },
        popEnterTransition = { slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) },
        popExitTransition = { slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, animationSpec = tween(300)) }
    ) {
        composable(Screen.Auth.route) { AuthScreen(navController) }
        
        composable(Screen.VendorDashboard.route) { VendorDashboardScreen(navController) }
        composable(Screen.Upload.route) { UploadScreen(navController) }
        composable(
            route = Screen.RequestMgmt.route,
            arguments = listOf(navArgument("initialTab") {
                type = NavType.IntType
                defaultValue = 0
            })
        ) { backStackEntry ->
            val initialTab = backStackEntry.arguments?.getInt("initialTab") ?: 0
            RequestManagementScreen(navController, initialTab = initialTab)
        }
        composable(Screen.CustomerDashboard.route) { CustomerDashboardScreen(navController) }
        
        composable(
            route = Screen.ListingDetail.route,
            arguments = listOf(navArgument("listingId") { type = NavType.StringType })
        ) { backStackEntry ->
            val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
            ListingDetailScreen(navController, listingId)
        }
        
        composable(
            route = Screen.Chat.route,
            arguments = listOf(
                navArgument("conversationId") { type = NavType.StringType },
                navArgument("otherPartyName") { type = NavType.StringType },
                navArgument("vendorId") { type = NavType.StringType },
                navArgument("listingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            val otherPartyName = backStackEntry.arguments?.getString("otherPartyName") ?: ""
            val vendorId = backStackEntry.arguments?.getString("vendorId") ?: ""
            val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
            ChatScreen(navController, conversationId, otherPartyName, vendorId, listingId)
        }
        
        composable(
            route = Screen.Review.route,
            arguments = listOf(
                navArgument("vendorId") { type = NavType.StringType },
                navArgument("listingId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val vendorId = backStackEntry.arguments?.getString("vendorId") ?: ""
            val listingId = backStackEntry.arguments?.getString("listingId") ?: ""
            ReviewScreen(navController, vendorId, listingId)
        }
        
        composable(Screen.Inspire.route) { InspireScreen(navController) }
        composable(Screen.MapView.route) { MapViewScreen(navController) }
        composable(Screen.Profile.route) { ProfileScreen(navController) }
        composable(Screen.PreviousOrders.route) { PreviousOrdersScreen(navController) }
    }
}
