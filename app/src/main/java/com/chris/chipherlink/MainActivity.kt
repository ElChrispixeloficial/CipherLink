package com.chris.chipherlink

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.chris.chipherlink.navigation.CipherLinkNavGraph
import com.chris.chipherlink.ui.theme.ChipherlinkTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChipherlinkTheme {
                val navController = rememberNavController()
                CipherLinkNavGraph(navController = navController)
            }
        }
    }
}
