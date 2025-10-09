package com.example.amulet_android_app

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import com.example.amulet_android_app.presentation.splash.SplashScreen
import com.example.amulet.core.design.AmuletTheme
import org.junit.Rule
import org.junit.Test

class SplashScreenTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

    @Test
    fun splashScreen_showsAppName() {
        composeRule.setContent {
            AmuletTheme {
                SplashScreen()
            }
        }

        composeRule.onNodeWithText("Amulet").assertIsDisplayed()
    }
}
