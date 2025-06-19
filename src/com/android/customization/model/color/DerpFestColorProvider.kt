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
    
    // New overlay categories from DerpFest-AOSP frameworks_base
    companion object {
        private const val OVERLAY_CATEGORY_BG_COLOR = "android.theme.customization.bg_color"
        private const val OVERLAY_LUMINANCE_FACTOR = "android.theme.customization.luminance_factor"
        private const val OVERLAY_CHROMA_FACTOR = "android.theme.customization.chroma_factor"
        private const val OVERLAY_TINT_BACKGROUND = "android.theme.customization.tint_background"
    }

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

        // Available styles for enhanced theming
        val styles = listOf(
            Style.VIBRANT to "Vibrant",
            Style.TONAL_SPOT to "Tonal",
            Style.SPRITZ to "Spritz",
            Style.EXPRESSIVE to "Expressive"
        )

        // Create color options for each DerpFest color with multiple styles
        colorMap.forEachIndexed { colorIndex, (colorRes, colorName) ->
            val color = context.resources.getColor(colorRes, context.theme)
            
            styles.forEachIndexed { styleIndex, (style, styleName) ->
                val builder = ColorOptionImpl.Builder()
                builder.title = "$colorName ($styleName)"
                builder.seedColor = color
                builder.source = COLOR_SOURCE_PRESET
                builder.type = ColorType.DERPFEST_COLOR
                builder.style = style
                builder.index = (colorIndex * styles.size) + styleIndex + 1

                // Set light and dark theme colors with enhanced color scheme
                val lightColorScheme = ColorScheme(color, /* darkTheme= */ false, style)
                val darkColorScheme = ColorScheme(color, /* darkTheme= */ true, style)
                
                builder.lightColors = getEnhancedLightColorPreview(lightColorScheme)
                builder.darkColors = getEnhancedDarkColorPreview(darkColorScheme)

                // Add comprehensive overlay packages using DerpFest-AOSP enhancements
                addEnhancedOverlayPackages(builder, color, lightColorScheme, darkColorScheme, style)

                colors.add(builder.build())
            }
        }

        return colors
    }

    private fun addEnhancedOverlayPackages(
        builder: ColorOptionImpl.Builder,
        seedColor: Int,
        lightColorScheme: ColorScheme,
        darkColorScheme: ColorScheme,
        style: Style
    ) {
        // Core color overlays
        builder.addOverlayPackage(ResourceConstants.OVERLAY_CATEGORY_COLOR, toColorString(seedColor))
        builder.addOverlayPackage(ResourceConstants.OVERLAY_CATEGORY_SYSTEM_PALETTE, toColorString(seedColor))
        
        // Theme style overlay for consistent styling
        builder.addOverlayPackage(ResourceConstants.OVERLAY_CATEGORY_THEME_STYLE, style.toString())
        
        // NEW: Background color control from DerpFest-AOSP
        // Use a complementary or neutral background color
        val bgColor = generateBackgroundColor(seedColor, lightColorScheme)
        builder.addOverlayPackage(OVERLAY_CATEGORY_BG_COLOR, toColorString(bgColor))
        
        // NEW: Luminance factor for fine-tuning brightness
        val luminanceFactor = calculateLuminanceFactor(seedColor, style)
        builder.addOverlayPackage(OVERLAY_LUMINANCE_FACTOR, luminanceFactor.toString())
        
        // NEW: Chroma factor for fine-tuning saturation
        val chromaFactor = calculateChromaFactor(seedColor, style)
        builder.addOverlayPackage(OVERLAY_CHROMA_FACTOR, chromaFactor.toString())
        
        // NEW: Background tinting option
        val tintBackground = shouldTintBackground(seedColor, style)
        builder.addOverlayPackage(OVERLAY_TINT_BACKGROUND, if (tintBackground) "1" else "0")
        
        // Enhanced accent color overlays for different system components
        val primaryAccent = lightColorScheme.accent1.s500
        val secondaryAccent = lightColorScheme.accent2.s500
        val tertiaryAccent = lightColorScheme.accent3.s500
        
        // Add accent color variations for different system elements
        builder.addOverlayPackage("android.theme.customization.primary_accent", toColorString(primaryAccent))
        builder.addOverlayPackage("android.theme.customization.secondary_accent", toColorString(secondaryAccent))
        builder.addOverlayPackage("android.theme.customization.tertiary_accent", toColorString(tertiaryAccent))
        
        // Add neutral color overlays for better contrast and theming
        val neutral1 = lightColorScheme.neutral1.s500
        val neutral2 = lightColorScheme.neutral2.s500
        builder.addOverlayPackage("android.theme.customization.neutral1", toColorString(neutral1))
        builder.addOverlayPackage("android.theme.customization.neutral2", toColorString(neutral2))
        
        // Add surface color overlays for better UI consistency
        val surfaceColor = lightColorScheme.neutral1.s50
        val surfaceVariantColor = lightColorScheme.neutral1.s100
        builder.addOverlayPackage("android.theme.customization.surface", toColorString(surfaceColor))
        builder.addOverlayPackage("android.theme.customization.surface_variant", toColorString(surfaceVariantColor))
        
        // Add Material Design 3 color token overlays for comprehensive theming
        addMaterialDesign3Overlays(builder, lightColorScheme, darkColorScheme)
        
        // Add icon pack overlays for system icons
        addIconOverlays(builder, seedColor, style)
        
        // Add typography and shape overlays
        addTypographyAndShapeOverlays(builder, seedColor, style)
    }

    private fun generateBackgroundColor(seedColor: Int, colorScheme: ColorScheme): Int {
        // Generate a complementary or neutral background color
        // Use the neutral colors from the color scheme for better harmony
        return when {
            Color.red(seedColor) > 200 && Color.green(seedColor) > 200 && Color.blue(seedColor) > 200 -> {
                // Light colors get dark backgrounds
                colorScheme.neutral1.s900
            }
            Color.red(seedColor) < 100 && Color.green(seedColor) < 100 && Color.blue(seedColor) < 100 -> {
                // Dark colors get light backgrounds
                colorScheme.neutral1.s50
            }
            else -> {
                // Medium colors get neutral backgrounds
                colorScheme.neutral1.s100
            }
        }
    }

    private fun calculateLuminanceFactor(seedColor: Int, style: Style): Float {
        // Calculate luminance factor based on color brightness and style
        val luminance = (0.299 * Color.red(seedColor) + 0.587 * Color.green(seedColor) + 0.114 * Color.blue(seedColor)) / 255.0
        
        return when (style) {
            Style.VIBRANT -> 1.2f // Increase brightness for vibrant styles
            Style.TONAL_SPOT -> 1.0f // Normal brightness for tonal styles
            Style.SPRITZ -> 0.8f // Decrease brightness for spritz styles
            Style.EXPRESSIVE -> 1.1f // Slightly increase for expressive styles
            else -> 1.0f
        }
    }

    private fun calculateChromaFactor(seedColor: Int, style: Style): Float {
        // Calculate chroma factor based on color saturation and style
        val max = maxOf(Color.red(seedColor), Color.green(seedColor), Color.blue(seedColor))
        val min = minOf(Color.red(seedColor), Color.green(seedColor), Color.blue(seedColor))
        val saturation = if (max == 0) 0.0 else (max - min).toDouble() / max
        
        return when (style) {
            Style.VIBRANT -> 1.3f // Increase saturation for vibrant styles
            Style.TONAL_SPOT -> 1.0f // Normal saturation for tonal styles
            Style.SPRITZ -> 0.7f // Decrease saturation for spritz styles
            Style.EXPRESSIVE -> 1.2f // Increase saturation for expressive styles
            else -> 1.0f
        }
    }

    private fun shouldTintBackground(seedColor: Int, style: Style): Boolean {
        // Determine if background should be tinted based on color and style
        return when (style) {
            Style.VIBRANT -> true // Always tint for vibrant styles
            Style.TONAL_SPOT -> false // No tint for tonal styles
            Style.SPRITZ -> true // Tint for spritz styles
            Style.EXPRESSIVE -> true // Tint for expressive styles
            else -> false
        }
    }

    private fun addMaterialDesign3Overlays(
        builder: ColorOptionImpl.Builder,
        lightColorScheme: ColorScheme,
        darkColorScheme: ColorScheme
    ) {
        val primaryAccent = lightColorScheme.accent1.s500
        val secondaryAccent = lightColorScheme.accent2.s500
        val tertiaryAccent = lightColorScheme.accent3.s500
        
        // Primary colors
        builder.addOverlayPackage("android.theme.customization.primary", toColorString(primaryAccent))
        builder.addOverlayPackage("android.theme.customization.on_primary", toColorString(lightColorScheme.neutral1.s10))
        builder.addOverlayPackage("android.theme.customization.primary_container", toColorString(lightColorScheme.accent1.s100))
        builder.addOverlayPackage("android.theme.customization.on_primary_container", toColorString(lightColorScheme.accent1.s900))
        
        // Secondary colors
        builder.addOverlayPackage("android.theme.customization.secondary", toColorString(secondaryAccent))
        builder.addOverlayPackage("android.theme.customization.on_secondary", toColorString(lightColorScheme.neutral1.s10))
        builder.addOverlayPackage("android.theme.customization.secondary_container", toColorString(lightColorScheme.accent2.s100))
        builder.addOverlayPackage("android.theme.customization.on_secondary_container", toColorString(lightColorScheme.accent2.s900))
        
        // Tertiary colors
        builder.addOverlayPackage("android.theme.customization.tertiary", toColorString(tertiaryAccent))
        builder.addOverlayPackage("android.theme.customization.on_tertiary", toColorString(lightColorScheme.neutral1.s10))
        builder.addOverlayPackage("android.theme.customization.tertiary_container", toColorString(lightColorScheme.accent3.s100))
        builder.addOverlayPackage("android.theme.customization.on_tertiary_container", toColorString(lightColorScheme.accent3.s900))
        
        // Surface colors
        builder.addOverlayPackage("android.theme.customization.surface", toColorString(lightColorScheme.neutral1.s50))
        builder.addOverlayPackage("android.theme.customization.on_surface", toColorString(lightColorScheme.neutral1.s900))
        builder.addOverlayPackage("android.theme.customization.surface_variant", toColorString(lightColorScheme.neutral1.s100))
        builder.addOverlayPackage("android.theme.customization.on_surface_variant", toColorString(lightColorScheme.neutral1.s700))
        
        // Background colors
        builder.addOverlayPackage("android.theme.customization.background", toColorString(lightColorScheme.neutral1.s10))
        builder.addOverlayPackage("android.theme.customization.on_background", toColorString(lightColorScheme.neutral1.s900))
        
        // Outline colors
        builder.addOverlayPackage("android.theme.customization.outline", toColorString(lightColorScheme.neutral1.s400))
        builder.addOverlayPackage("android.theme.customization.outline_variant", toColorString(lightColorScheme.neutral1.s200))
        
        // Dark theme specific colors
        builder.addOverlayPackage("android.theme.customization.primary_dark", toColorString(darkColorScheme.accent1.s200))
        builder.addOverlayPackage("android.theme.customization.on_primary_dark", toColorString(darkColorScheme.neutral1.s900))
        builder.addOverlayPackage("android.theme.customization.primary_container_dark", toColorString(darkColorScheme.accent1.s900))
        builder.addOverlayPackage("android.theme.customization.on_primary_container_dark", toColorString(darkColorScheme.accent1.s100))
        
        builder.addOverlayPackage("android.theme.customization.surface_dark", toColorString(darkColorScheme.neutral1.s900))
        builder.addOverlayPackage("android.theme.customization.on_surface_dark", toColorString(darkColorScheme.neutral1.s10))
        builder.addOverlayPackage("android.theme.customization.surface_variant_dark", toColorString(darkColorScheme.neutral1.s800))
        builder.addOverlayPackage("android.theme.customization.on_surface_variant_dark", toColorString(darkColorScheme.neutral1.s300))
        
        builder.addOverlayPackage("android.theme.customization.background_dark", toColorString(darkColorScheme.neutral1.s900))
        builder.addOverlayPackage("android.theme.customization.on_background_dark", toColorString(darkColorScheme.neutral1.s10))
    }

    private fun addIconOverlays(
        builder: ColorOptionImpl.Builder,
        seedColor: Int,
        style: Style
    ) {
        val styleSuffix = "_${style.toString().lowercase()}"
        
        // Add icon pack overlays for system icons
        builder.addOverlayPackage(ResourceConstants.OVERLAY_CATEGORY_ICON_ANDROID, "derpfest_icon_${toColorString(seedColor).substring(1)}$styleSuffix")
        builder.addOverlayPackage(ResourceConstants.OVERLAY_CATEGORY_ICON_SETTINGS, "derpfest_icon_${toColorString(seedColor).substring(1)}$styleSuffix")
        builder.addOverlayPackage(ResourceConstants.OVERLAY_CATEGORY_ICON_SYSUI, "derpfest_icon_${toColorString(seedColor).substring(1)}$styleSuffix")
        builder.addOverlayPackage(ResourceConstants.OVERLAY_CATEGORY_ICON_LAUNCHER, "derpfest_icon_${toColorString(seedColor).substring(1)}$styleSuffix")
        builder.addOverlayPackage(ResourceConstants.OVERLAY_CATEGORY_ICON_THEMEPICKER, "derpfest_icon_${toColorString(seedColor).substring(1)}$styleSuffix")
        
        // Add WiFi and signal icon overlays
        builder.addOverlayPackage(ResourceConstants.OVERLAY_CATEGORY_ICON_WIFI, "derpfest_wifi_${toColorString(seedColor).substring(1)}$styleSuffix")
        builder.addOverlayPackage(ResourceConstants.OVERLAY_CATEGORY_ICON_SIGNAL, "derpfest_signal_${toColorString(seedColor).substring(1)}$styleSuffix")
    }

    private fun addTypographyAndShapeOverlays(
        builder: ColorOptionImpl.Builder,
        seedColor: Int,
        style: Style
    ) {
        val styleSuffix = "_${style.toString().lowercase()}"
        
        // Add font overlays for consistent typography
        builder.addOverlayPackage(ResourceConstants.OVERLAY_CATEGORY_FONT, "derpfest_font_${toColorString(seedColor).substring(1)}$styleSuffix")
        builder.addOverlayPackage(ResourceConstants.OVERLAY_CATEGORY_LOCKFONT, "derpfest_lockfont_${toColorString(seedColor).substring(1)}$styleSuffix")
        
        // Add shape overlays for adaptive icons
        builder.addOverlayPackage(ResourceConstants.OVERLAY_CATEGORY_SHAPE, "derpfest_shape_${toColorString(seedColor).substring(1)}$styleSuffix")
        
        // Add global theme overlay for comprehensive theming
        builder.addOverlayPackage(ResourceConstants.OVERLAY_CATEGORY_ANDROID_THEME, "derpfest_theme_${toColorString(seedColor).substring(1)}$styleSuffix")
    }

    private fun getEnhancedLightColorPreview(colorScheme: ColorScheme): IntArray {
        return intArrayOf(
            colorScheme.accent1.s500,  // Primary accent
            colorScheme.accent2.s500,  // Secondary accent
            colorScheme.accent3.s500,  // Tertiary accent
            colorScheme.neutral1.s500  // Neutral color
        )
    }

    private fun getEnhancedDarkColorPreview(colorScheme: ColorScheme): IntArray {
        return intArrayOf(
            colorScheme.accent1.s200,  // Primary accent (darker for dark theme)
            colorScheme.accent2.s200,  // Secondary accent (darker for dark theme)
            colorScheme.accent3.s200,  // Tertiary accent (darker for dark theme)
            colorScheme.neutral1.s200  // Neutral color (darker for dark theme)
        )
    }

    private fun toColorString(color: Int): String {
        return String.format("#%08X", color)
    }
} 
