/*
 * Copyright (C) 2018 The Android Open Source Project
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
 */
package com.android.customization.picker.iconpack;

import static com.android.wallpaper.widget.BottomActionBar.BottomAction.APPLY_TEXT;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.Transition;

import com.android.customization.model.CustomizationManager.Callback;
import com.android.customization.model.CustomizationManager.OptionsFetchedListener;
import com.android.customization.model.CustomizationOption;
import com.android.customization.model.iconpack.WifiIconOption;
import com.android.customization.model.iconpack.WifiIconManager;
import com.android.customization.model.theme.OverlayManagerCompat;
import com.android.customization.module.logging.ThemesUserEventLogger;
import com.android.customization.picker.WallpaperPreviewer;
import com.android.customization.widget.OptionSelectorController;
import com.android.customization.widget.OptionSelectorController.CheckmarkStyle;
import com.android.themepicker.R;
import com.android.wallpaper.picker.AppbarFragment;
import com.android.wallpaper.widget.BottomActionBar;

import java.util.List;

/**
 * Fragment that contains the UI for selecting and applying a WifiIconOption.
 */
public class WifiIconFragment extends AppbarFragment {

    private static final String TAG = "WifiIconFragment";
    private static final String KEY_STATE_SELECTED_OPTION = "WifiIconFragment.selectedOption";
    private static final String KEY_STATE_BOTTOM_ACTION_BAR_VISIBLE =
            "WifiIconFragment.bottomActionBarVisible";

    public static WifiIconFragment newInstance(CharSequence title) {
        WifiIconFragment fragment = new WifiIconFragment();
        fragment.setArguments(AppbarFragment.createArguments(title));
        return fragment;
    }

    private RecyclerView mOptionsContainer;
    private OptionSelectorController<WifiIconOption> mOptionsController;
    private WifiIconManager mWifiIconManager;
    private WifiIconOption mSelectedOption;
    private ContentLoadingProgressBar mLoading;
    private ViewGroup mContent;
    private View mError;
    private BottomActionBar mBottomActionBar;
    private Boolean mEnterTransitionEnded = false, mOptionsLoaded = false;

    private final Callback mApplyWifiIconCallback = new Callback() {
        @Override
        public void onSuccess() {
        }

        @Override
        public void onError(@Nullable Throwable throwable) {
            // Since we disabled it when clicked apply button.
            mBottomActionBar.enableActions();
            mBottomActionBar.hide();
            //TODO(chihhangchuang): handle
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(
                R.layout.fragment_wifi_icon_pack_picker, container, /* attachToRoot */ false);
        setUpToolbar(view);
        mContent = view.findViewById(R.id.content_section);
        mOptionsContainer = view.findViewById(R.id.options_container);
        mLoading = view.findViewById(R.id.loading_indicator);
        mError = view.findViewById(R.id.error_section);

        // For nav bar edge-to-edge effect.
        view.setOnApplyWindowInsetsListener((v, windowInsets) -> {
            v.setPadding(
                    v.getPaddingLeft(),
                    windowInsets.getSystemWindowInsetTop(),
                    v.getPaddingRight(),
                    windowInsets.getSystemWindowInsetBottom());
            return windowInsets.consumeSystemWindowInsets();
        });

        Transition enterTransition = (Transition) getEnterTransition();
        if (enterTransition != null) {
            enterTransition.addListener(new Transition.TransitionListener() {
                @Override
                public void onTransitionStart(Transition transition) {}

                @Override
                public void onTransitionEnd(Transition transition) {
                    mEnterTransitionEnded = true;
                    maybeSetSelectedOption();
                }

                @Override
                public void onTransitionCancel(Transition transition) {
                    mEnterTransitionEnded = true;
                    maybeSetSelectedOption();
                }

                @Override
                public void onTransitionPause(Transition transition) {}

                @Override
                public void onTransitionResume(Transition transition) {}
            });
        } else {
            mEnterTransitionEnded = true;
        }

        mWifiIconManager = WifiIconManager.getInstance(getContext(), new OverlayManagerCompat(getContext()));
        setUpOptions(savedInstanceState);

        return view;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mBottomActionBar != null) {
            outState.putBoolean(KEY_STATE_BOTTOM_ACTION_BAR_VISIBLE, mBottomActionBar.isVisible());
        }
    }

    @Override
    protected void onBottomActionBarReady(BottomActionBar bottomActionBar) {
        super.onBottomActionBarReady(bottomActionBar);
        mBottomActionBar = bottomActionBar;
        mBottomActionBar.showActionsOnly(APPLY_TEXT);
        mBottomActionBar.setActionClickListener(APPLY_TEXT, v -> applyWifiIconOption(mSelectedOption));
    }

    private void applyWifiIconOption(WifiIconOption wifiIconOption) {
        mBottomActionBar.disableActions();
        mWifiIconManager.apply(wifiIconOption, mApplyWifiIconCallback);
    }

    private void setUpOptions(@Nullable Bundle savedInstanceState) {
        hideError();
        mLoading.show();
        mWifiIconManager.fetchOptions(new OptionsFetchedListener<WifiIconOption>() {
            @Override
            public void onOptionsLoaded(List<WifiIconOption> options) {
                mLoading.hide();
                mOptionsController = new OptionSelectorController<>(
                        mOptionsContainer, options, /* useGrid= */ false, CheckmarkStyle.CORNER);
                mOptionsController.initOptions(mWifiIconManager);
                mSelectedOption = getActiveOption(options);
                onOptionSelected(mSelectedOption);
                restoreBottomActionBarVisibility(savedInstanceState);
                mOptionsLoaded = true;
                maybeSetSelectedOption();

                mOptionsController.addListener(selectedOption -> {
                    onOptionSelected(selectedOption);
                    // Show the apply button only when it isn't already the active option
                    if (((WifiIconOption) selectedOption).isActive(mWifiIconManager)) {
                        mBottomActionBar.hide();
                    } else {
                        mBottomActionBar.show();
                    }
                });
            }

            @Override
            public void onError(@Nullable Throwable throwable) {
                if (throwable != null) {
                    Log.e(TAG, "Error loading wifi icon options", throwable);
                }
                showError();
            }
        }, /*reload= */ true);
    }

    private void maybeSetSelectedOption() {
        if (mEnterTransitionEnded && mOptionsLoaded) {
            mOptionsController.setSelectedOption(mSelectedOption);
        }
    }

    private WifiIconOption getActiveOption(List<WifiIconOption> options) {
        return options.stream()
                .filter(option -> option.isActive(mWifiIconManager))
                .findAny()
                // For development only, as there should always be a wifi icon set.
                .orElse(options.get(0));
    }

    private void hideError() {
        mContent.setVisibility(View.VISIBLE);
        mError.setVisibility(View.GONE);
    }

    private void showError() {
        mLoading.hide();
        mContent.setVisibility(View.GONE);
        mError.setVisibility(View.VISIBLE);
    }

    private void onOptionSelected(CustomizationOption selectedOption) {
        mSelectedOption = (WifiIconOption) selectedOption;
        refreshPreview();
    }

    private void refreshPreview() {
        mSelectedOption.bindPreview(mContent);
    }

    private void restoreBottomActionBarVisibility(@Nullable Bundle savedInstanceState) {
        boolean isBottomActionBarVisible = savedInstanceState != null
                && savedInstanceState.getBoolean(KEY_STATE_BOTTOM_ACTION_BAR_VISIBLE);
        if (mBottomActionBar == null) return;
        if (isBottomActionBarVisible) {
            mBottomActionBar.show();
        } else {
            mBottomActionBar.hide();
        }
    }
} 