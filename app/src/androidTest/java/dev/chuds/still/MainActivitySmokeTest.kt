package dev.chuds.still

import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.semantics.getOrNull
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainActivitySmokeTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun homeSurface_showsHintAndClock() {
        composeTestRule.waitForIdle()

        composeTestRule
            .onNodeWithText("long press for all apps")
            .assertIsDisplayed()

        val clockRegex = Regex("""^\d{1,2}:\d{2}$""")
        val clockMatcher = SemanticsMatcher("clock matches HH:mm or h:mm") { node ->
            val text = node.config.getOrNull(SemanticsProperties.Text)
                ?.joinToString(separator = "") { it.text }
                ?: return@SemanticsMatcher false
            clockRegex.matches(text)
        }

        val clockNodes = composeTestRule.onAllNodes(clockMatcher).fetchSemanticsNodes()
        check(clockNodes.isNotEmpty()) { "expected a clock text node matching HH:mm or h:mm" }
    }
}
