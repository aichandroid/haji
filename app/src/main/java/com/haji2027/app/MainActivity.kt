package com.haji2027.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.haji2027.core.designsystem.theme.HajjTheme
import com.haji2027.core.designsystem.theme.TypographyElderlyTitle

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HajjTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = "Panduan Haji 2027",
                        style = TypographyElderlyTitle
                    )
                }
            }
        }
    }
}
