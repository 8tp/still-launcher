package dev.chuds.still.ui.home

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
import dev.chuds.still.data.LaunchableApp
import dev.chuds.still.ui.components.StillDivider
import dev.chuds.still.ui.components.StillMenuItem
import dev.chuds.still.ui.theme.StillColors
import dev.chuds.still.ui.theme.StillTypography

@Composable
fun SlotRenameScreen(
    app: LaunchableApp?,
    initialLabel: String?,
    onSave: (String?) -> Unit,
    onClearLabel: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember { mutableStateOf(initialLabel.orEmpty()) }
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
            text = "rename",
            style = StillTypography.Kicker,
            color = StillColors.Gray,
        )

        Spacer(modifier = Modifier.height(34.dp))

        app?.let {
            Text(
                text = it.label,
                style = StillTypography.Caption,
                color = StillColors.DimGray,
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        BasicTextField(
            value = text,
            onValueChange = { text = it },
            singleLine = true,
            textStyle = StillTypography.Menu.copy(color = StillColors.SoftWhite),
            cursorBrush = SolidColor(StillColors.SoftWhite),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onSave(text.takeIf { it.isNotBlank() }) }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
        )

        Spacer(modifier = Modifier.height(12.dp))
        StillDivider()
        Spacer(modifier = Modifier.height(12.dp))

        StillMenuItem(
            title = "save",
            style = StillTypography.SecondaryMenu,
            onClick = { onSave(text.takeIf { it.isNotBlank() }) },
        )

        StillMenuItem(
            title = "use app name",
            subtitle = app?.label?.let { "reset to \"$it\"" },
            style = StillTypography.SecondaryMenu,
            onClick = onClearLabel,
        )

        StillMenuItem(
            title = "cancel",
            style = StillTypography.SecondaryMenu,
            onClick = onBack,
        )
    }
}
