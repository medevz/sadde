package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.chat.ChatHistoryScreen
import com.example.ui.home.HomeScreen
import com.example.ui.permissions.OnboardingScreen
import com.example.ui.permissions.PermissionsScreen
import com.example.ui.settings.PrivacyScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.viewmodel.AssistantViewModel
import com.example.ui.viewmodel.SettingsViewModel

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    assistantViewModel: AssistantViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val settings by settingsViewModel.settings.collectAsStateWithLifecycle()
    val startDestination = if (settings.onboardingCompleted) {
        NavRoutes.ROUTE_HOME
    } else {
        NavRoutes.ROUTE_ONBOARDING
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavRoutes.ROUTE_ONBOARDING) {
            OnboardingScreen(
                permissionManager = assistantViewModel.permissionManager,
                onCompleteOnboarding = {
                    settingsViewModel.setOnboardingCompleted(true)
                    navController.navigate(NavRoutes.ROUTE_HOME) {
                        popUpTo(NavRoutes.ROUTE_ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.ROUTE_HOME) {
            HomeScreen(
                viewModel = assistantViewModel,
                onNavigateToHistory = { navController.navigate(NavRoutes.ROUTE_HISTORY) },
                onNavigateToPermissions = { navController.navigate(NavRoutes.ROUTE_PERMISSIONS) },
                onNavigateToSettings = { navController.navigate(NavRoutes.ROUTE_SETTINGS) }
            )
        }

        composable(NavRoutes.ROUTE_HISTORY) {
            ChatHistoryScreen(
                viewModel = assistantViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.ROUTE_PERMISSIONS) {
            PermissionsScreen(
                permissionManager = assistantViewModel.permissionManager,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.ROUTE_SETTINGS) {
            SettingsScreen(
                viewModel = settingsViewModel,
                ttsService = assistantViewModel.ttsService,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToPermissions = { navController.navigate(NavRoutes.ROUTE_PERMISSIONS) },
                onNavigateToPrivacy = { navController.navigate(NavRoutes.ROUTE_PRIVACY) }
            )
        }

        composable(NavRoutes.ROUTE_PRIVACY) {
            PrivacyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
