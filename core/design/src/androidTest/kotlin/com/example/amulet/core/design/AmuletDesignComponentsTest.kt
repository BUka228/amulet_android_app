package com.example.amulet.core.design

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.amulet.core.design.components.button.AmuletButton
import com.example.amulet.core.design.components.progress.SessionProgress
import com.example.amulet.core.design.foundation.theme.AmuletTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AmuletDesignComponentsTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<androidx.activity.ComponentActivity>()

    @Test
    fun amuletButton_displaysText() {
        composeRule.setContent {
            AmuletTheme {
                AmuletButton(text = "Send", onClick = {})
            }
        }

        composeRule.onNodeWithText("Send").assertIsDisplayed()
    }

    @Test
    fun sessionProgress_showsPercentage() {
        composeRule.setContent {
            AmuletTheme {
                SessionProgress(progress = 0.5f)
            }
        }

        composeRule.onNodeWithText("50%").assertIsDisplayed()
    }
}
