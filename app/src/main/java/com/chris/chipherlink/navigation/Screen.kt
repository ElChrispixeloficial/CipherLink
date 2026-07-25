package com.chris.chipherlink.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Login : Screen("login")
    data object Register : Screen("register")
    data object Home : Screen("home")
    data object CreateChat : Screen("create_chat")
    data object Profile : Screen("profile")
    data object UserMenu : Screen("user_menu")
    data object Security : Screen("security")
    data object Recovery : Screen("recovery")
    data object AiChatList : Screen("ai_chat_list")
    data object AiChat : Screen("ai_chat?chatId={chatId}") {
        fun createRoute(chatId: String? = null): String {
            return if (chatId != null) "ai_chat?chatId=$chatId" else "ai_chat"
        }
    }
    data object Chat : Screen("chat/{chatId}") {
        fun createRoute(chatId: String) = "chat/$chatId"
    }
    data object SearchUser : Screen("search_user")
    data object Contacts : Screen("contacts")
    data object Call : Screen("call/{chatId}/{contactName}") {
        fun createRoute(chatId: String, contactName: String) = "call/$chatId/$contactName"
    }
}
