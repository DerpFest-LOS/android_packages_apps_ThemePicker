/*
 * Copyright (C) 2021 The Android Open Source Project
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
package com.android.customization.model.iconpack;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.android.customization.model.CustomizationManager.Callback;
import com.android.customization.model.CustomizationManager.OptionsFetchedListener;
import com.android.customization.model.CustomizationOption;
import com.android.customization.picker.iconpack.WifiIconFragment;
import com.android.customization.picker.iconpack.WifiIconSectionView;
import com.android.customization.widget.OptionSelectorController;
import com.android.customization.widget.OptionSelectorController.OptionSelectedListener;
import com.android.themepicker.R;
import com.android.wallpaper.model.CustomizationSectionController;
import com.android.wallpaper.util.LaunchUtils;

import java.util.List;

/** A {@link CustomizationSectionController} for wifi icons. */

public class WifiIconSectionController implements CustomizationSectionController<WifiIconSectionView> {

    private static final String TAG = "WifiIconSectionController";

    private final WifiIconManager mWifiIconOptionsManager;
    private final CustomizationSectionNavigationController mSectionNavigationController;
    private final Callback mApplyWifiIconCallback = new Callback() {
        @Override
        public void onSuccess() {
        }

        @Override
        public void onError(@Nullable Throwable throwable) {
        }
    };

    public WifiIconSectionController(WifiIconManager wifiIconOptionsManager,
            CustomizationSectionNavigationController sectionNavigationController) {
        mWifiIconOptionsManager = wifiIconOptionsManager;
        mSectionNavigationController = sectionNavigationController;
    }

    @Override
    public boolean isAvailable(Context context) {
        return mWifiIconOptionsManager.isAvailable();
    }

    @Override
    public WifiIconSectionView createView(Context context) {
        WifiIconSectionView wifiIconSectionView = (WifiIconSectionView) LayoutInflater.from(context)
                .inflate(R.layout.wifi_icon_section_view, /* root= */ null);

        TextView sectionDescription = wifiIconSectionView.findViewById(R.id.icon_section_description);
        View sectionTile = wifiIconSectionView.findViewById(R.id.icon_section_tile);

        mWifiIconOptionsManager.fetchOptions(new OptionsFetchedListener<WifiIconOption>() {
            @Override
            public void onOptionsLoaded(List<WifiIconOption> options) {
                WifiIconOption activeOption = getActiveOption(options);
                sectionDescription.setText(activeOption.getTitle());
                activeOption.bindThumbnailTile(sectionTile);
            }

            @Override
            public void onError(@Nullable Throwable throwable) {
                if (throwable != null) {
                    Log.e(TAG, "Error loading wifi icon options", throwable);
                }
                sectionDescription.setText(R.string.something_went_wrong);
                sectionTile.setVisibility(View.GONE);
            }
        }, /* reload= */ true);

        wifiIconSectionView.setOnClickListener(v -> mSectionNavigationController.navigateTo(
                WifiIconFragment.newInstance(context.getString(R.string.preview_name_wifi_icon))));

        return wifiIconSectionView;
    }

    private WifiIconOption getActiveOption(List<WifiIconOption> options) {
        return options.stream()
                .filter(option -> option.isActive(mWifiIconOptionsManager))
                .findAny()
                // For development only, as there should always be a grid set.
                .orElse(options.get(0));
    }
} 