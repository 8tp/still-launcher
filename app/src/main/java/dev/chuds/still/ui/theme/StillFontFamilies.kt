package dev.chuds.still.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import dev.chuds.still.R

/**
 * One typography role can be filled by multiple bundled OFL faces depending on the active
 * preset. Variable fonts use FontVariation to map FontWeight onto the wght axis.
 */
@OptIn(ExperimentalTextApi::class)
internal object StillFontFamilies {
    val CormorantGaramond: FontFamily = FontFamily(
        Font(
            R.font.cormorant_garamond,
            weight = FontWeight.Light,
            variationSettings = FontVariation.Settings(FontVariation.weight(300)),
        ),
        Font(
            R.font.cormorant_garamond,
            weight = FontWeight.Normal,
            variationSettings = FontVariation.Settings(FontVariation.weight(400)),
        ),
    )

    val InstrumentSerif: FontFamily = FontFamily(
        Font(R.font.instrument_serif_regular, weight = FontWeight.Light),
        Font(R.font.instrument_serif_regular, weight = FontWeight.Normal),
    )

    val Inter: FontFamily = FontFamily(
        Font(
            R.font.inter,
            weight = FontWeight.Light,
            variationSettings = FontVariation.Settings(FontVariation.weight(300)),
        ),
        Font(
            R.font.inter,
            weight = FontWeight.Normal,
            variationSettings = FontVariation.Settings(FontVariation.weight(400)),
        ),
    )

    val SpaceGrotesk: FontFamily = FontFamily(
        Font(
            R.font.space_grotesk,
            weight = FontWeight.Light,
            variationSettings = FontVariation.Settings(FontVariation.weight(300)),
        ),
        Font(
            R.font.space_grotesk,
            weight = FontWeight.Normal,
            variationSettings = FontVariation.Settings(FontVariation.weight(400)),
        ),
    )

    val IbmPlexMono: FontFamily = FontFamily(
        Font(R.font.ibm_plex_mono_light, weight = FontWeight.Light),
        Font(R.font.ibm_plex_mono_regular, weight = FontWeight.Normal),
    )
}
