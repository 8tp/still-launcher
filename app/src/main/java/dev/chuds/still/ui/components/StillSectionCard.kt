package dev.chuds.still.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.chuds.still.ui.theme.StillColors

/**
 * Outlined container that groups related menu rows. Borrowed from minimalist launchers like
 * clauncher — softens the visual rhythm of a long settings list without introducing chrome.
 */
@Composable
fun StillSectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = StillColors.Hairline,
                shape = RoundedCornerShape(14.dp),
            )
            .padding(horizontal = 18.dp, vertical = 6.dp),
    ) {
        content()
    }
}
