package com.chris.chipherlink.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.chris.chipherlink.ui.aichat.AiChatListScreen
import com.chris.chipherlink.ui.aichat.AiChatScreen
import com.chris.chipherlink.ui.call.CallScreen
import com.chris.chipherlink.ui.chat.ChatScreen
import com.chris.chipherlink.ui.createchat.CreateConversationScreen
import com.chris.chipherlink.ui.home.HomeScreen
import com.chris.chipherlink.ui.login.LoginScreen
import com.chris.chipherlink.ui.profile.ProfileScreen
import com.chris.chipherlink.ui.recovery.RecoveryScreen
import com.chris.chipherlink.ui.register.RegisterScreen
import com.chris.chipherlink.ui.searchuser.SearchUserScreen
import com.chris.chipherlink.ui.security.SecurityScreen
import com.chris.chipherlink.ui.splash.SplashScreen
import com.chris.chipherlink.ui.usermenu.UserMenuScreen

private const val ANIM_DURATION = 350

private val fadeEnter: EnterTransition = fadeIn(animationSpec = tween(ANIM_DURATION))
private val fadeExit: ExitTransition = fadeOut(animationSpec = tween(ANIM_DURATION))
private val slideEnter: EnterTransition = slideInHorizontally(
    initialOffsetX = { it },
    animationSpec = tween(ANIM_DURATION)
)
private val slideExit: ExitTransition = slideOutHorizontally(
    targetOffsetX = { -it },
    animationSpec = tween(ANIM_DURATION)
)

@Composable
fun CipherLinkNavGraph(
    navController: NavHostController,
    startDestination: String = Screen.Splash.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeEnter },
        exitTransition = { fadeExit }
    ) {
        composable(
            route = Screen.Splash.route,
            enterTransition = { fadeEnter },
            exitTransition = { fadeExit }
        ) {
            SplashScreen(
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Login.route,
            enterTransition = { slideEnter },
            exitTransition = { slideExit },
            popEnterTransition = { fadeIn(animationSpec = tween(ANIM_DURATION)) },
            popExitTransition = { slideExit }
        ) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Register.route,
            enterTransition = { slideEnter },
            exitTransition = { slideExit },
            popEnterTransition = { fadeIn(animationSpec = tween(ANIM_DURATION)) },
            popExitTransition = { slideExit }
        ) {
            RegisterScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Home.route,
            enterTransition = { fadeEnter },
            exitTransition = { fadeExit }
        ) {
            HomeScreen(
                onNavigateToProfile = {
                    navController.navigate(Screen.UserMenu.route)
                },
                onNavigateToSettings = {
                    navController.navigate(Screen.UserMenu.route)
                },
                onNavigateToCreateChat = {
                    navController.navigate(Screen.CreateChat.route)
                },
                onNavigateToChat = { chatId ->
                    navController.navigate(Screen.Chat.createRoute(chatId))
                },
                onNavigateToAiChat = {
                    navController.navigate(Screen.AiChatList.route)
                },
                onNavigateToSearchUser = {
                    navController.navigate(Screen.SearchUser.route)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.SearchUser.route,
            enterTransition = { slideEnter },
            exitTransition = { slideExit },
            popEnterTransition = { fadeIn(animationSpec = tween(ANIM_DURATION)) },
            popExitTransition = { slideExit }
        ) {
            SearchUserScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onStartChat = { contactId, cipherLinkId ->
                    navController.navigate(Screen.CreateChat.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(
            route = Screen.CreateChat.route,
            enterTransition = { slideEnter },
            exitTransition = { slideExit },
            popEnterTransition = { fadeIn(animationSpec = tween(ANIM_DURATION)) },
            popExitTransition = { slideExit }
        ) {
            CreateConversationScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToChat = { chatId ->
                    navController.navigate(Screen.Chat.createRoute(chatId)) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("chatId") { type = NavType.StringType }),
            enterTransition = { slideEnter },
            exitTransition = { slideExit },
            popEnterTransition = { fadeIn(animationSpec = tween(ANIM_DURATION)) },
            popExitTransition = { slideExit }
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
            ChatScreen(
                chatId = chatId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToCall = { cId, name ->
                    navController.navigate(Screen.Call.createRoute(cId, name))
                }
            )
        }

        composable(
            route = Screen.UserMenu.route,
            enterTransition = { slideEnter },
            exitTransition = { slideExit },
            popEnterTransition = { fadeIn(animationSpec = tween(ANIM_DURATION)) },
            popExitTransition = { slideExit }
        ) {
            UserMenuScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToSecurity = {
                    navController.navigate(Screen.Security.route)
                },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = Screen.Profile.route,
            enterTransition = { slideEnter },
            exitTransition = { slideExit },
            popEnterTransition = { fadeIn(animationSpec = tween(ANIM_DURATION)) },
            popExitTransition = { slideExit }
        ) {
            ProfileScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Security.route,
            enterTransition = { slideEnter },
            exitTransition = { slideExit },
            popEnterTransition = { fadeIn(animationSpec = tween(ANIM_DURATION)) },
            popExitTransition = { slideExit }
        ) {
            SecurityScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToRecovery = {
                    navController.navigate(Screen.Recovery.route)
                }
            )
        }

        composable(
            route = Screen.Recovery.route,
            enterTransition = { slideEnter },
            exitTransition = { slideExit },
            popEnterTransition = { fadeIn(animationSpec = tween(ANIM_DURATION)) },
            popExitTransition = { slideExit }
        ) {
            RecoveryScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.AiChatList.route,
            enterTransition = { slideEnter },
            exitTransition = { slideExit },
            popEnterTransition = { fadeIn(animationSpec = tween(ANIM_DURATION)) },
            popExitTransition = { slideExit }
        ) {
            AiChatListScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToChat = { chatId ->
                    navController.navigate(Screen.AiChat.createRoute(chatId))
                }
            )
        }

        composable(
            route = Screen.AiChat.route,
            arguments = listOf(
                navArgument("chatId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            ),
            enterTransition = { slideEnter },
            exitTransition = { slideExit },
            popEnterTransition = { fadeIn(animationSpec = tween(ANIM_DURATION)) },
            popExitTransition = { slideExit }
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId")
            AiChatScreen(
                chatId = chatId,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Call.route,
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("contactName") { type = NavType.StringType }
            ),
            enterTransition = { slideEnter },
            exitTransition = { slideExit },
            popEnterTransition = { fadeIn(animationSpec = tween(ANIM_DURATION)) },
            popExitTransition = { slideExit }
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId") ?: return@composable
            val contactName = backStackEntry.arguments?.getString("contactName") ?: "Unknown"
            CallScreen(
                chatId = chatId,
                contactName = contactName,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
