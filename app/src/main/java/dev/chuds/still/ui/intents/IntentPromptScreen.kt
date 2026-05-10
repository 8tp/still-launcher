package dev.chuds.still.ui.intents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import dev.chuds.still.ui.components.StillDivider
import dev.chuds.still.ui.components.StillMenuItem
import dev.chuds.still.ui.theme.StillColors
import dev.chuds.still.ui.theme.StillTypography

/**
 * Replaces the binary friction screen. Same Open / Cancel pair, but with an optional intent
 * text field above. A non-empty intent is journaled; an empty intent just launches with no
 * record. The launcher does not block — it only asks.
 */
@Composable
fun IntentPromptScreen(
    slotLabel: String,
    onOpen: (intent: String?) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(StillColors.OledBlack)
            .systemBarsPadding()
            .padding(horizontal = 34.dp, vertical = 28.dp),
    ) {
        Text(
            text = slotLabel,
            style = StillTypography.Kicker,
            color = StillColors.Gray,
        )

        Spacer(modifier = Modifier.height(70.dp))

        Text(
            text = "Use this intentionally.",
            style = StillTypography.Menu,
            color = StillColors.SoftWhite,
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "what for?",
            style = StillTypography.Caption,
            color = StillColors.DimGray,
        )

        Spacer(modifier = Modifier.height(6.dp))

        BasicTextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            textStyle = StillTypography.SecondaryMenu.copy(color = StillColors.SoftWhite),
            cursorBrush = SolidColor(StillColors.SoftWhite),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = { onOpen(text.takeIf { it.isNotBlank() }?.trim()) },
            ),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        )

        Spacer(modifier = Modifier.height(20.dp))
        StillDivider()
        Spacer(modifier = Modifier.height(14.dp))

        StillMenuItem(
            title = "open",
            style = StillTypography.SecondaryMenu,
            bordered = true,
            onClick = { onOpen(text.takeIf { it.isNotBlank() }?.trim()) },
        )

        Spacer(modifier = Modifier.height(8.dp))

        StillMenuItem(
            title = "cancel",
            style = StillTypography.SecondaryMenu,
            bordered = true,
            onClick = onCancel,
        )
    }
}
