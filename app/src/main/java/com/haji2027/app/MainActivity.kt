package com.haji2027.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.haji2027.app.ui.RootScreen
import com.haji2027.core.designsystem.theme.HajjTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as HajjApplication).container
        setContent {
            HajjTheme {
                RootScreen(container = container)
            }
        }
    }
}
