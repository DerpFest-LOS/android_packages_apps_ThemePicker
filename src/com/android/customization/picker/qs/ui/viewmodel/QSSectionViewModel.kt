/*
 * Copyright (C) 2025 FloraOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.android.customization.picker.qs.ui.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.UserHandle
import android.widget.Toast
import androidx.annotation.VisibleForTesting
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.customization.model.theme.OverlayManagerCompat
import com.android.themepicker.R
import com.google.android.material.materialswitch.MaterialSwitch
import kotlinx.coroutines.launch

/** Models UI state for a section that lets the user control the notification settings. */
class QSSectionViewModel
@VisibleForTesting
constructor(
    private val context: Context,
    private val overlayManagerCompat: OverlayManagerCompat,
) : ViewModel() {

    private val uiModeChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_CONFIGURATION_CHANGED) {
                val currentConfig = context.resources.configuration
                val isDarkMode = (currentConfig.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0
                
                // If switching to light mode and gradient is enabled, disable it
                if (!isDarkMode && isSwitchOn()) {
                    viewModelScope.launch {
                        disableGradient()
                    }
                }
            }
        }
    }

    init {
        // Register for UI mode changes
        val filter = IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED)
        context.applicationContext.registerReceiver(uiModeChangeReceiver, filter)
    }

    override fun onCleared() {
        super.onCleared()
        // Unregister the receiver when the ViewModel is cleared
        try {
            context.applicationContext.unregisterReceiver(uiModeChangeReceiver)
        } catch (e: Exception) {
            // Ignore if receiver wasn't registered
        }
    }

    /** Whether the switch should be on. */
    fun isSwitchOn(): Boolean = overlayManagerCompat.getEnabledPackageName(
        SYSTEMUI_PACKAGE, QS_GRADIENT_OVERLAY_CATEGORY
    ) != null

    /** Check if dark theme is enabled */
    fun isDarkThemeEnabled(): Boolean {
        return (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0
    }

    /** Handle configuration changes, particularly theme changes */
    fun onConfigurationChanged(newConfig: Configuration) {
        val isDarkMode = (newConfig.uiMode and Configuration.UI_MODE_NIGHT_YES) != 0
        
        // If switching to light mode and gradient is enabled, disable it
        if (!isDarkMode && isSwitchOn()) {
            viewModelScope.launch {
                disableGradient()
            }
        }
    }

    /** Disable the gradient overlay */
    private suspend fun disableGradient() {
        overlayManagerCompat.disableOverlay(
            QS_GRADIENT_OVERLAY_PACKAGAE,
            UserHandle.myUserId()
        )
    }

    /** Enable the gradient overlay */
    private suspend fun enableGradient() {
        overlayManagerCompat.setEnabledExclusiveInCategory(
            QS_GRADIENT_OVERLAY_PACKAGAE,
            UserHandle.myUserId()
        )
    }

    /** Notifies that the section has been clicked. */
    fun onClicked(switch: MaterialSwitch) {
        // Only process clicks if dark theme is enabled
        if (!isDarkThemeEnabled()) {
            return
        }
        
        viewModelScope.launch {
            if (switch.isChecked) {
                disableGradient()
                switch.isChecked = false
            } else {
                enableGradient()
                switch.isChecked = true
            }
        }
    }

    class Factory(
        private val context: Context,
        private val overlayManagerCompat: OverlayManagerCompat,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = QSSectionViewModel(
            context = context.applicationContext, // Use application context to prevent leaks
            overlayManagerCompat = overlayManagerCompat,
        ) as T
    }

    companion object {
        const val SYSTEMUI_PACKAGE = "com.android.systemui"
        const val QS_GRADIENT_OVERLAY_CATEGORY = "android.theme.customization.qs_panel_gradient"
        const val QS_GRADIENT_OVERLAY_PACKAGAE = "org.lineageos.overlay.customization.qs.qsgradient"
    }
}
