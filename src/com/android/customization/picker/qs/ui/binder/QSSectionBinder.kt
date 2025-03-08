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
package com.android.customization.picker.qs.ui.binder

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.android.customization.picker.qs.ui.view.QSSectionView
import com.android.customization.picker.qs.ui.viewmodel.QSSectionViewModel
import com.android.themepicker.R
import com.google.android.material.materialswitch.MaterialSwitch
import kotlinx.coroutines.launch

/**
 * Binds between view and view-model for a section that lets the user control notification settings.
 */
object QSSectionBinder {
    @SuppressLint("UseSwitchCompatOrMaterialCode") // We're using Switch and that's okay for SysUI.
    fun bind(
        view: View,
        viewModel: QSSectionViewModel,
        lifecycleOwner: LifecycleOwner,
    ) {
        val qsSectionView = view as QSSectionView
        val switch: MaterialSwitch = view.requireViewById(R.id.switcher)
        val summary: TextView = view.requireViewById(R.id.summary)

        view.setOnClickListener {
            viewModel.onClicked(switch)
        }

        // Set up configuration change listener to detect theme changes
        qsSectionView.setConfigurationChangeListener(object : QSSectionView.ConfigurationChangeListener {
            override fun onConfigurationChanged(newConfig: Configuration) {
                viewModel.onConfigurationChanged(newConfig)
                updateUI(view, viewModel, switch, summary)
            }
        })

        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    updateUI(view, viewModel, switch, summary)
                }
            }
        }
    }
    
    private fun updateUI(view: View, viewModel: QSSectionViewModel, switch: MaterialSwitch, summary: TextView) {
        switch.isChecked = viewModel.isSwitchOn()
        
        // Update the UI based on dark theme state
        val isDarkThemeEnabled = viewModel.isDarkThemeEnabled()
        if (!isDarkThemeEnabled) {
            // In light theme, show summary and disable switch
            summary.text = view.context.getString(R.string.qs_gradient_dark_theme_only)
            summary.visibility = View.VISIBLE
            switch.isEnabled = false
            view.isEnabled = false
        } else {
            // In dark theme, hide summary and enable switch
            summary.visibility = View.GONE
            switch.isEnabled = true
            view.isEnabled = true
        }
    }
}
