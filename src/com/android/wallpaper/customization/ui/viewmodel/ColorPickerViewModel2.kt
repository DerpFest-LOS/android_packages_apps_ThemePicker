/*
 * Copyright (C) 2024 The Android Open Source Project
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
package com.android.wallpaper.customization.ui.viewmodel

import android.content.Context
import com.android.customization.model.color.ColorOptionImpl
import com.android.customization.module.logging.ThemesUserEventLogger
import com.android.customization.picker.color.domain.interactor.ColorPickerInteractor
import com.android.customization.picker.color.shared.model.ColorOptionModel
import com.android.customization.picker.color.shared.model.ColorType
import com.android.customization.picker.color.ui.viewmodel.ColorOptionIconViewModel
import com.android.themepicker.R
import com.android.wallpaper.picker.common.icon.ui.viewmodel.Icon
import com.android.wallpaper.picker.common.text.ui.viewmodel.Text
import com.android.wallpaper.picker.customization.ui.viewmodel.FloatingToolbarTabViewModel
import com.android.wallpaper.picker.option.ui.viewmodel.OptionItemViewModel2
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlin.coroutines.resume
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

/** Models UI state for a color picker experience. */
class ColorPickerViewModel2
@AssistedInject
constructor(
    @ApplicationContext context: Context,
    private val interactor: ColorPickerInteractor,
    private val logger: ThemesUserEventLogger,
    @Assisted private val viewModelScope: CoroutineScope,
) {

    private val overridingColorOption = MutableStateFlow<ColorOptionModel?>(null)
    val previewingColorOption = overridingColorOption.asStateFlow()

    private val selectedColorTypeTabId = MutableStateFlow<ColorType?>(null)
    private var onApplyContinuation: CancellableContinuation<Unit>? = null

    /** View-models for each color tab. */
    val colorTypeTabs: Flow<List<FloatingToolbarTabViewModel>> =
        combine(interactor.colorOptions, selectedColorTypeTabId) {
            colorOptions,
            selectedColorTypeIdOrNull ->
            colorOptions.keys.mapIndexed { index, colorType ->
                val isSelected =
                    (selectedColorTypeIdOrNull == null && index == 0) ||
                        selectedColorTypeIdOrNull == colorType

                val name =
                    when (colorType) {
                        ColorType.WALLPAPER_COLOR ->
                            context.resources.getString(R.string.wallpaper_color_tab)
                        ColorType.PRESET_COLOR ->
                            context.resources.getString(R.string.preset_color_tab_2)
                        ColorType.DERPFEST_COLOR ->
                            context.resources.getString(R.string.preset_color_tab_3)
                    }

                FloatingToolbarTabViewModel(
                    Icon.Resource(
                        res =
                            when (colorType) {
                                ColorType.WALLPAPER_COLOR ->
                                    com.android.wallpaper.R.drawable.ic_baseline_wallpaper_24
                                ColorType.PRESET_COLOR -> R.drawable.ic_colors
                                ColorType.DERPFEST_COLOR -> R.drawable.ic_colors
                            },
                        contentDescription = Text.Loaded(name),
                    ),
                    name,
                    isSelected,
                ) {
                    if (!isSelected) {
                        this.selectedColorTypeTabId.value = colorType
                    }
                }
            }
        }

    /** View-models for each color tab subheader */
    val colorTypeTabSubheader: Flow<String> =
        selectedColorTypeTabId.map { selectedColorTypeIdOrNull ->
            when (selectedColorTypeIdOrNull ?: ColorType.WALLPAPER_COLOR) {
                ColorType.WALLPAPER_COLOR ->
                    context.resources.getString(R.string.wallpaper_color_subheader)
                ColorType.PRESET_COLOR ->
                    context.resources.getString(R.string.preset_color_subheader)
                ColorType.DERPFEST_COLOR ->
                    context.resources.getString(R.string.preset_color_subheader)
            }
        }

    /** The list of all color options mapped by their color type */
    private val allColorOptions:
        Flow<Map<ColorType, List<OptionItemViewModel2<ColorOptionIconViewModel>>>> =
        interactor.colorOptions.map { colorOptions ->
            colorOptions
                .map { colorOptionEntry ->
                    colorOptionEntry.key to
                        colorOptionEntry.value.map { colorOptionModel ->
                            val colorOption: ColorOptionImpl =
                                colorOptionModel.colorOption as ColorOptionImpl
                            val lightThemeColors =
                                colorOption.previewInfo.resolveColors(/* darkTheme= */ false)
                            val darkThemeColors =
                                colorOption.previewInfo.resolveColors(/* darkTheme= */ true)
                            val isSelectedFlow: StateFlow<Boolean> =
                                previewingColorOption
                                    .map {
                                        it?.colorOption?.isEquivalent(colorOptionModel.colorOption)
                                            ?: colorOptionModel.isSelected
                                    }
                                    .stateIn(viewModelScope)
                            OptionItemViewModel2<ColorOptionIconViewModel>(
                                key = MutableStateFlow(colorOptionModel.key) as StateFlow<String>,
                                payload =
                                    ColorOptionIconViewModel(
                                        lightThemeColor0 = lightThemeColors[0],
                                        lightThemeColor1 = lightThemeColors[1],
                                        lightThemeColor2 = lightThemeColors[2],
                                        lightThemeColor3 = lightThemeColors[3],
                                        darkThemeColor0 = darkThemeColors[0],
                                        darkThemeColor1 = darkThemeColors[1],
                                        darkThemeColor2 = darkThemeColors[2],
                                        darkThemeColor3 = darkThemeColors[3],
                                    ),
                                text =
                                    Text.Loaded(
                                        colorOption.getContentDescription(context).toString()
                                    ),
                                isTextUserVisible = false,
                                isSelected = isSelectedFlow,
                                onClicked =
                                    isSelectedFlow.map { isSelected ->
                                        if (isSelected) {
                                            null
                                        } else {
                                            {
                                                viewModelScope.launch {
                                                    overridingColorOption.value = colorOptionModel
                                                }
                                            }
                                        }
                                    },
                            )
                        }
                }
                .toMap()
        }

    /**
     * This function suspends until onApplyComplete is called to accommodate for configuration
     * change updates, which are applied with a latency.
     */
    val onApply: Flow<(suspend () -> Unit)?> =
        previewingColorOption.map { previewingColorOption ->
            previewingColorOption?.let {
                if (it.isSelected) {
                    null
                } else {
                    {
                        interactor.select(it)
                        // Suspend until onApplyComplete is called, e.g. on configuration change
                        suspendCancellableCoroutine { continuation: CancellableContinuation<Unit> ->
                            onApplyContinuation?.cancel()
                            onApplyContinuation = continuation
                            continuation.invokeOnCancellation { onApplyContinuation = null }
                        }
                        logger.logThemeColorApplied(
                            previewingColorOption.colorOption.sourceForLogging,
                            previewingColorOption.colorOption.styleForLogging,
                            previewingColorOption.colorOption.seedColor,
                        )
                    }
                }
            }
        }

    fun resetPreview() {
        overridingColorOption.value = null
    }

    /** Resumes the onApply function if apply is in progress, otherwise no-op */
    fun onApplyComplete() {
        onApplyContinuation?.resume(Unit)
        onApplyContinuation = null
    }

    /** The list of all available color options for the selected Color Type. */
    val colorOptions: Flow<List<OptionItemViewModel2<ColorOptionIconViewModel>>> =
        combine(allColorOptions, selectedColorTypeTabId) {
            allColorOptions: Map<ColorType, List<OptionItemViewModel2<ColorOptionIconViewModel>>>,
            selectedColorTypeIdOrNull ->
            val selectedColorTypeId = selectedColorTypeIdOrNull ?: ColorType.WALLPAPER_COLOR
            allColorOptions[selectedColorTypeId]!!
        }

    @ViewModelScoped
    @AssistedFactory
    interface Factory {
        fun create(viewModelScope: CoroutineScope): ColorPickerViewModel2
    }
}
