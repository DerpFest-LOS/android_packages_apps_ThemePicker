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
import static com.android.customization.model.ResourceConstants.SYSUI_PACKAGE;
import static com.android.customization.model.ResourceConstants.ICONS_FOR_PREVIEW;
import static com.android.customization.model.ResourceConstants.WIFI_ICONS_FOR_PREVIEW;
import static com.android.customization.model.ResourceConstants.OVERLAY_CATEGORY_ICON_WIFI;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.drawable.Drawable;
import android.os.UserHandle;
import android.util.Log;

import com.android.customization.model.ResourceConstants;
import com.android.customization.model.theme.OverlayManagerCompat;
import com.android.themepicker.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WifiIconOptionProvider {

    private static final String TAG = "WifiIconOptionProvider";

    private Context mContext;
    private PackageManager mPm;
    private final List<String> mOverlayPackages;
    private final List<WifiIconOption> mOptions = new ArrayList<>();
    private final List<String> mWifiIconsOverlayPackages = new ArrayList<>();

    public WifiIconOptionProvider(Context context, OverlayManagerCompat manager) {
        mContext = context;
        mPm = context.getPackageManager();
        String[] targetPackages = ResourceConstants.getPackagesToOverlay(context);
        mWifiIconsOverlayPackages.addAll(manager.getOverlayPackagesForCategory(
                OVERLAY_CATEGORY_ICON_WIFI, UserHandle.myUserId(), targetPackages));
        mOverlayPackages = new ArrayList<>();
        mOverlayPackages.addAll(manager.getOverlayPackagesForCategory(OVERLAY_CATEGORY_ICON_WIFI,
                UserHandle.myUserId(), ResourceConstants.getPackagesToOverlay(mContext)));
    }

    public List<WifiIconOption> getOptions() {
        if (mOptions.isEmpty()) loadOptions();
        return mOptions;
    }

    private void loadOptions() {
        addDefault();

        Map<String, WifiIconOption> optionsByPrefix = new HashMap<>();

        for (String overlayPackage : mOverlayPackages) {
            WifiIconOption option = addOrUpdateOption(optionsByPrefix, overlayPackage,
                    OVERLAY_CATEGORY_ICON_WIFI);
            if (option != null) {
                loadIconsForOption(option, overlayPackage);
            }
        }

        for (String overlayPackage : mWifiIconsOverlayPackages) {
            WifiIconOption option = addOrUpdateOption(optionsByPrefix, overlayPackage, OVERLAY_CATEGORY_ICON_WIFI);
            if (option != null) {
                loadIconsForOption(option, overlayPackage);
            }
        }

        List<WifiIconOption> customOptions = new ArrayList<>();
        for (WifiIconOption option : optionsByPrefix.values()) {
            if (option.isValid(mContext) && !option.getIcons().isEmpty()) {
                customOptions.add(option);
            } else {
                Log.w(TAG, String.format("Skipping option %s: valid=%b, icons=%d", 
                    option.getTitle(), option.isValid(mContext), option.getIcons().size()));
            }
        }

        customOptions.sort((o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle()));

        mOptions.addAll(customOptions);
    }

    private void loadIconsForOption(WifiIconOption option, String overlayPackage) {
        try {
            boolean hasIcons = false;
            // Try the predefined WiFi icon names
            for (String iconName : WIFI_ICONS_FOR_PREVIEW) {
                try {
                    option.addIcon(loadIconPreviewDrawable(iconName, overlayPackage));
                    hasIcons = true;
                    Log.d(TAG, String.format("Found WiFi icon %s in %s", iconName, overlayPackage));
                } catch (NotFoundException e) {
                    // Icon not found, continue to next
                }
            }
            
            // If no icons found, try to discover available WiFi icons
            if (!hasIcons) {
                hasIcons = tryDiscoverWifiIcons(option, overlayPackage);
            }
            
            if (!hasIcons) {
                Log.w(TAG, String.format("No WiFi icons found in overlay package %s", overlayPackage));
            }
        } catch (NameNotFoundException e) {
            Log.w(TAG, String.format("Couldn't load wifi icon overlay details for %s, will skip it",
                    overlayPackage), e);
        }
    }

    private boolean tryDiscoverWifiIcons(WifiIconOption option, String overlayPackage) {
        try {
            Resources resources = mPm.getResourcesForApplication(overlayPackage);
            // Try common WiFi icon patterns
            String[] patterns = {"ic_wifi", "wifi", "ic_signal_wifi", "signal_wifi"};
            
            for (String pattern : patterns) {
                try {
                    int resId = resources.getIdentifier(pattern, "drawable", overlayPackage);
                    if (resId != 0) {
                        option.addIcon(resources.getDrawable(resId, null));
                        Log.d(TAG, String.format("Discovered WiFi icon %s in %s", pattern, overlayPackage));
                        return true;
                    }
                } catch (Exception e) {
                    // Continue to next pattern
                }
            }
        } catch (Exception e) {
            Log.d(TAG, String.format("Could not discover icons in %s", overlayPackage));
        }
        return false;
    }

    private WifiIconOption addOrUpdateOption(Map<String, WifiIconOption> optionsByPrefix,
            String overlayPackage, String category) {
        String prefix = overlayPackage.substring(0, overlayPackage.lastIndexOf("."));
        WifiIconOption option = null;
        try {
            if (!optionsByPrefix.containsKey(prefix)) {
                option = new WifiIconOption(mPm.getApplicationInfo(overlayPackage, 0).loadLabel(mPm).toString());
                optionsByPrefix.put(prefix, option);
            } else {
                option = optionsByPrefix.get(prefix);
            }
            option.addOverlayPackage(category, overlayPackage);
        } catch (NameNotFoundException e) {
            Log.e(TAG, String.format("Package %s not found", overlayPackage), e);
        }
        return option;
    }

    private Drawable loadIconPreviewDrawable(String drawableName, String packageName)
            throws NameNotFoundException, NotFoundException {
        final Resources resources = ANDROID_PACKAGE.equals(packageName)
                ? Resources.getSystem()
                : mPm.getResourcesForApplication(packageName);
        return resources.getDrawable(
                resources.getIdentifier(drawableName, "drawable", packageName), null);
    }

    private void addDefault() {
        WifiIconOption option = new WifiIconOption(mContext.getString(R.string.default_theme_title), true);
        boolean hasIcons = false;
        
        // Try to load from system resources first
        try {
            for (String iconName : WIFI_ICONS_FOR_PREVIEW) {
                try {
                    option.addIcon(loadIconPreviewDrawable(iconName, ANDROID_PACKAGE));
                    hasIcons = true;
                } catch (NotFoundException e) {
                    // Icon not found, continue to next
                }
            }
        } catch (NameNotFoundException e) {
            Log.w(TAG, "Couldn't load default WiFi icons from system", e);
        }

        // If no WiFi icons found in system, try to load from SystemUI
        if (!hasIcons) {
            try {
                for (String iconName : WIFI_ICONS_FOR_PREVIEW) {
                    try {
                        option.addIcon(loadIconPreviewDrawable(iconName, SYSUI_PACKAGE));
                        hasIcons = true;
                    } catch (NotFoundException e) {
                        // Icon not found, continue to next
                    }
                }
            } catch (NameNotFoundException e) {
                Log.w(TAG, "Couldn't load default WiFi icons from SystemUI", e);
            }
        }

        // If still no icons, try to discover them
        if (!hasIcons) {
            hasIcons = tryDiscoverWifiIcons(option, ANDROID_PACKAGE);
        }

        // If still no icons, try SystemUI
        if (!hasIcons) {
            hasIcons = tryDiscoverWifiIcons(option, SYSUI_PACKAGE);
        }

        // If still no icons, add a fallback icon or skip this option
        if (!hasIcons) {
            Log.w(TAG, "No default WiFi icons found, skipping default option");
            return;
        }

        option.addOverlayPackage(OVERLAY_CATEGORY_ICON_WIFI, null);
        mOptions.add(option);
    }
} 
