/*
 * SPDX-FileCopyrightText: DerpFest AOSP
 * SPDX-License-Identifier: Apache-2.0
 */

package com.android.customization.model.color

import android.content.Context
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.android.customization.model.ResourceConstants
import com.android.customization.picker.color.shared.model.ColorType
import com.android.systemui.monet.Style
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DerpFestColorProviderTest {

    private lateinit var context: Context
    private lateinit var provider: DerpFestColorProvider

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        provider = DerpFestColorProvider(context)
    }

    @Test
    fun testVaderRedHasBackgroundTinting() {
        val colors = provider.getDerpFestColors()
        
        val vaderRed = colors.find { it.title == "Vader Red" }
        assertThat(vaderRed).isNotNull()
        
        val packages = vaderRed!!.getJsonPackages(true)
        
        // Check that vader red has the background color overlay
        assertThat(packages).containsKey(ResourceConstants.OVERLAY_CATEGORY_BG_COLOR)
        assertThat(packages[ResourceConstants.OVERLAY_CATEGORY_BG_COLOR]).isEqualTo("#FFFF0000")
        
        // Check that vader red has the tint background overlay
        assertThat(packages).containsKey(ResourceConstants.OVERLAY_TINT_BACKGROUND)
        assertThat(packages[ResourceConstants.OVERLAY_TINT_BACKGROUND]).isEqualTo("1")
    }

    @Test
    fun testOtherColorsDoNotHaveBackgroundTinting() {
        val colors = provider.getDerpFestColors()
        
        val oceanBlue = colors.find { it.title == "Ocean Blue" }
        assertThat(oceanBlue).isNotNull()
        
        val packages = oceanBlue!!.getJsonPackages(true)
        
        // Check that ocean blue does NOT have the background color overlay
        assertThat(packages).doesNotContainKey(ResourceConstants.OVERLAY_CATEGORY_BG_COLOR)
        
        // Check that ocean blue does NOT have the tint background overlay
        assertThat(packages).doesNotContainKey(ResourceConstants.OVERLAY_TINT_BACKGROUND)
    }

    @Test
    fun testAllColorsHaveBasicOverlays() {
        val colors = provider.getDerpFestColors()
        
        colors.forEach { color ->
            val packages = color.getJsonPackages(true)
            
            // All colors should have the basic color overlays
            assertThat(packages).containsKey(ResourceConstants.OVERLAY_CATEGORY_COLOR)
            assertThat(packages).containsKey(ResourceConstants.OVERLAY_CATEGORY_SYSTEM_PALETTE)
            
            // All colors should have the correct type
            assertThat(color.type).isEqualTo(ColorType.DERPFEST_COLOR)
            
            // All colors should have the correct source
            assertThat(color.source).isEqualTo("preset")
            
            // All colors should have the correct style
            assertThat(color.style).isEqualTo(Style.VIBRANT)
        }
    }

    @Test
    fun testVaderRedColorValue() {
        val colors = provider.getDerpFestColors()
        
        val vaderRed = colors.find { it.title == "Vader Red" }
        assertThat(vaderRed).isNotNull()
        
        // Vader red should have the correct color value (#FF0000)
        assertThat(vaderRed!!.seedColor).isEqualTo(Color.RED)
    }
} 