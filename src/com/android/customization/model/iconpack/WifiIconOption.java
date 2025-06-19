/*
 * Copyright (C) 2019 The Android Open Source Project
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

import static com.android.customization.model.ResourceConstants.ANDROID_PACKAGE;
import static com.android.customization.model.ResourceConstants.SETTINGS_PACKAGE;
import static com.android.customization.model.ResourceConstants.SYSUI_PACKAGE;
import static com.android.customization.model.ResourceConstants.OVERLAY_CATEGORY_ICON_WIFI;
import static com.android.customization.model.ResourceConstants.OVERLAY_CATEGORY_ICON_SYSUI;
import static com.android.customization.model.ResourceConstants.OVERLAY_CATEGORY_ICON_SETTINGS;
import static com.android.customization.model.ResourceConstants.OVERLAY_CATEGORY_ICON_ANDROID;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.PorterDuff.Mode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.customization.model.CustomizationManager;
import com.android.customization.model.CustomizationOption;
import com.android.customization.model.ResourceConstants;
import com.android.customization.model.theme.OverlayManagerCompat;
import com.android.themepicker.R;
import com.android.wallpaper.util.ResourceUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WifiIconOption implements CustomizationOption<WifiIconOption> {

    public static final int THUMBNAIL_ICON_POSITION = 0;
    private static int[] mIconIds = {
            R.id.preview_icon_0, R.id.preview_icon_1, R.id.preview_icon_2, R.id.preview_icon_3,
            R.id.preview_icon_4, R.id.preview_icon_5
    };

    private List<Drawable> mIcons = new ArrayList<>();
    private String mTitle;
    private boolean mIsDefault;

    // Mapping from category to overlay package name
    private final Map<String, String> mOverlayPackageNames = new HashMap<>();

    public WifiIconOption(String title, boolean isDefault) {
        mTitle = title;
        mIsDefault = isDefault;
    }

    public WifiIconOption(String title) {
        this(title, false);
    }

    @Override
    public void bindThumbnailTile(View view) {
        if (mIcons == null || mIcons.isEmpty()) {
            // If no icons are available, hide the icon view
            ImageView iconView = view.findViewById(R.id.icon_section_tile);
            if (iconView != null) {
                iconView.setVisibility(View.GONE);
            }
            ImageView optionIcon = view.findViewById(R.id.option_icon);
            if (optionIcon != null) {
                optionIcon.setVisibility(View.GONE);
            }
            view.setContentDescription(mTitle);
            return;
        }

        Resources res = view.getContext().getResources();
        Drawable icon = mIcons.get(THUMBNAIL_ICON_POSITION)
                .getConstantState().newDrawable().mutate();
        int resId = R.id.icon_section_tile;
        int colorFilter = view.getContext().getResources().getColor(
                com.android.wallpaper.R.color.system_on_surface);
        if (view.findViewById(R.id.option_icon) != null) {
            resId = R.id.option_icon;
            colorFilter = view.getContext().getResources().getColor(
                    view.isActivated()
                            ? com.android.wallpaper.R.color.system_on_surface
                            : com.android.wallpaper.R.color.system_on_surface_variant);
        }
        icon.setColorFilter(colorFilter, Mode.SRC_ATOP);
        ((ImageView) view.findViewById(resId)).setImageDrawable(icon);
        view.setContentDescription(mTitle);
    }

    @Override
    public boolean isActive(CustomizationManager<WifiIconOption> manager) {
        WifiIconManager iconManager = (WifiIconManager) manager;
        OverlayManagerCompat overlayManager = iconManager.getOverlayManager();
        if (mIsDefault) {
            return overlayManager.getEnabledPackageName(SYSUI_PACKAGE, OVERLAY_CATEGORY_ICON_WIFI) == null;
        }
        for (Map.Entry<String, String> overlayEntry : getOverlayPackages().entrySet()) {
            if (overlayEntry.getValue() == null || !overlayEntry.getValue().equals(overlayManager.getEnabledPackageName(determinePackage(overlayEntry.getKey()), overlayEntry.getKey()))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public int getLayoutResId() {
        return R.layout.theme_icon_option;
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    public void bindPreview(ViewGroup container) {
        if (mIcons == null || mIcons.isEmpty()) {
            // If no icons are available, don't try to bind preview
            return;
        }

        ViewGroup cardBody = container.findViewById(R.id.theme_preview_card_body_container);
        if (cardBody.getChildCount() == 0) {
            LayoutInflater.from(container.getContext()).inflate(
                    R.layout.preview_card_icon_content, cardBody, true);
        }
        for (int i = 0; i < mIconIds.length && i < mIcons.size(); i++) {
            ((ImageView) container.findViewById(mIconIds[i])).setImageDrawable(
                    mIcons.get(i));
        }
    }

    private String determinePackage(String category) {
       switch(category) {
           case OVERLAY_CATEGORY_ICON_SYSUI:
               return SYSUI_PACKAGE;
           case OVERLAY_CATEGORY_ICON_SETTINGS:
               return SETTINGS_PACKAGE;
           case OVERLAY_CATEGORY_ICON_ANDROID:
               return ANDROID_PACKAGE;
           case OVERLAY_CATEGORY_ICON_WIFI:
               return SYSUI_PACKAGE;
           default:
               return null;
       }
    }

    public void addIcon(Drawable previewIcon) {
        mIcons.add(previewIcon);
    }

    public void addOverlayPackage(String category, String overlayPackage) {
        mOverlayPackageNames.put(category, overlayPackage);
    }

    public Map<String, String> getOverlayPackages() {
        return mOverlayPackageNames;
    }

    /**
     * @return whether this icon option has overlays and previews for all the required packages
     */
    public boolean isValid(Context context) {
        return mOverlayPackageNames.containsKey(OVERLAY_CATEGORY_ICON_WIFI);
    }

    public boolean isDefault() {
        return mIsDefault;
    }

    public List<Drawable> getIcons() {
        return mIcons;
    }
} 
