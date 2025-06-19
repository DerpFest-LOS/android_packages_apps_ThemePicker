/*
 * SPDX-FileCopyrightText: DerpFest AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.customization.model.color

import android.content.Context
import android.graphics.Color
import com.android.customization.model.ResourceConstants
import com.android.customization.model.color.ColorOptionsProvider.COLOR_SOURCE_PRESET
import com.android.customization.picker.color.shared.model.ColorType
import com.android.systemui.monet.ColorScheme
import com.android.systemui.monet.Style
import com.android.themepicker.R

class DerpFestColorProvider(private val context: Context) {
    fun getDerpFestColors(): List<ColorOptionImpl> {
        val colors = mutableListOf<ColorOptionImpl>()
        
        // Get DerpFest colors from resources
        val colorMap = listOf(
            Pair(R.color.derpfest_vader_red, "Vader Red"),
            Pair(R.color.derpfest_ocean_blue, "Ocean Blue"),
            Pair(R.color.derpfest_forest_green, "Forest Green"),
            Pair(R.color.derpfest_sunset_orange, "Sunset Orange"),
            Pair(R.color.derpfest_purple_haze, "Purple Haze"),
            Pair(R.color.derpfest_teal_dream, "Teal Dream"),
            Pair(R.color.derpfest_golden_hour, "Golden Hour"),
            Pair(R.color.derpfest_midnight_blue, "Midnight Blue"),
            Pair(R.color.derpfest_rose_gold, "Rose Gold"),
            Pair(R.color.derpfest_emerald_city, "Emerald City"),
            Pair(R.color.derpfest_crimson_tide, "Crimson Tide"),
            Pair(R.color.derpfest_azure_sky, "Azure Sky")
        )

        // Create color options for each DerpFest color
        colorMap.forEachIndexed { index, (colorRes, name) ->
            val color = context.resources.getColor(colorRes, context.theme)
            val builder = ColorOptionImpl.Builder()
            builder.title = name
            builder.seedColor = color
            builder.source = COLOR_SOURCE_PRESET
            builder.type = ColorType.DERPFEST_COLOR
            builder.style = Style.TONAL_SPOT
            builder.index = index + 1

            // Set light and dark theme colors
            val lightColorScheme = ColorScheme(color, /* darkTheme= */ false, Style.TONAL_SPOT)
            val darkColorScheme = ColorScheme(color, /* darkTheme= */ true, Style.TONAL_SPOT)
            
            builder.lightColors = getLightColorPreview(lightColorScheme)
            builder.darkColors = getDarkColorPreview(darkColorScheme)

            // Add overlay packages
            builder.addOverlayPackage(ResourceConstants.OVERLAY_CATEGORY_COLOR, toColorString(color))
            builder.addOverlayPackage(ResourceConstants.OVERLAY_CATEGORY_SYSTEM_PALETTE, toColorString(color))

            colors.add(builder.build())
        }

        return colors
    }

    private fun getLightColorPreview(colorScheme: ColorScheme): IntArray {
        return intArrayOf(
            colorScheme.accent1.s600,
            colorScheme.accent1.s600,
            colorScheme.accent2.s500,
            colorScheme.accent3.s300
        )
    }

    private fun getDarkColorPreview(colorScheme: ColorScheme): IntArray {
        return intArrayOf(
            colorScheme.accent1.s200,
            colorScheme.accent1.s200,
            colorScheme.accent2.s500,
            colorScheme.accent3.s300
        )
    }

    private fun toColorString(color: Int): String {
        return String.format("#%08X", color)
    }
} 
