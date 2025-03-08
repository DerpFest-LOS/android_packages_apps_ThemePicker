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
package com.android.customization.picker.qs.ui.view

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import com.android.wallpaper.picker.SectionView

class QSSectionView(context: Context?, attrs: AttributeSet?) : SectionView(context, attrs)
    {
        // Interface for configuration change callbacks
        interface ConfigurationChangeListener {
            fun onConfigurationChanged(newConfig: Configuration)
        }

        private var configChangeListener: ConfigurationChangeListener? = null

        fun setConfigurationChangeListener(listener: ConfigurationChangeListener) {
            configChangeListener = listener
        }

        override fun onConfigurationChanged(newConfig: Configuration) {
            super.onConfigurationChanged(newConfig)
            configChangeListener?.onConfigurationChanged(newConfig)
        }
    }
