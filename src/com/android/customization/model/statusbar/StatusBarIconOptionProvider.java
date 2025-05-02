package com.android.customization.model.statusbar;

import static com.android.customization.model.ResourceConstants.ANDROID_PACKAGE;
import static com.android.customization.model.ResourceConstants.OVERLAY_CATEGORY_ICON_WIFI;
import static com.android.customization.model.ResourceConstants.OVERLAY_CATEGORY_ICON_SIGNAL;

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

public class StatusBarIconOptionProvider {
    private static final String TAG = "StatusBarIconOptionProvider";

    private Context mContext;
    private PackageManager mPm;
    private final List<String> mOverlayPackages;
    private final List<StatusBarIconOption> mOptions = new ArrayList<>();
    private final List<String> mWifiIconsOverlayPackages = new ArrayList<>();
    private final List<String> mSignalIconsOverlayPackages = new ArrayList<>();

    public StatusBarIconOptionProvider(Context context, OverlayManagerCompat manager) {
        mContext = context;
        mPm = context.getPackageManager();
        String[] targetPackages = ResourceConstants.getPackagesToOverlay(context);
        mWifiIconsOverlayPackages.addAll(manager.getOverlayPackagesForCategory(
                OVERLAY_CATEGORY_ICON_WIFI, UserHandle.myUserId(), targetPackages));
        mSignalIconsOverlayPackages.addAll(manager.getOverlayPackagesForCategory(
                OVERLAY_CATEGORY_ICON_SIGNAL, UserHandle.myUserId(), targetPackages));
        mOverlayPackages = new ArrayList<>();
        mOverlayPackages.addAll(manager.getOverlayPackagesForCategory(OVERLAY_CATEGORY_ICON_WIFI,
                UserHandle.myUserId(), ResourceConstants.getPackagesToOverlay(mContext)));
    }

    public List<StatusBarIconOption> getOptions() {
        if (mOptions.isEmpty()) loadOptions();
        return mOptions;
    }

    private void loadOptions() {
        addDefault();

        Map<String, StatusBarIconOption> optionsByPrefix = new HashMap<>();

        for (String overlayPackage : mOverlayPackages) {
            StatusBarIconOption option = addOrUpdateOption(optionsByPrefix, overlayPackage,
                    OVERLAY_CATEGORY_ICON_WIFI);
            try {
                option.addIcon(loadIconPreviewDrawable("ic_wifi", overlayPackage));
            } catch (NotFoundException | NameNotFoundException e) {
                Log.w(TAG, String.format("Couldn't load icon overlay details for %s, will skip it",
                        overlayPackage), e);
            }
        }

        for (String overlayPackage : mWifiIconsOverlayPackages) {
            addOrUpdateOption(optionsByPrefix, overlayPackage, OVERLAY_CATEGORY_ICON_WIFI);
        }

        for (String overlayPackage : mSignalIconsOverlayPackages) {
            addOrUpdateOption(optionsByPrefix, overlayPackage, OVERLAY_CATEGORY_ICON_SIGNAL);
        }

        List<StatusBarIconOption> customOptions = new ArrayList<>();
        for (StatusBarIconOption option : optionsByPrefix.values()) {
            if (option.isValid(mContext)) {
                customOptions.add(option);
            }
        }

        customOptions.sort((o1, o2) -> o1.getTitle().compareToIgnoreCase(o2.getTitle()));

        mOptions.addAll(customOptions);
    }

    private StatusBarIconOption addOrUpdateOption(Map<String, StatusBarIconOption> optionsByPrefix,
            String overlayPackage, String category) {
        String prefix = overlayPackage.substring(0, overlayPackage.lastIndexOf("."));
        StatusBarIconOption option = null;
        try {
            if (!optionsByPrefix.containsKey(prefix)) {
                option = new StatusBarIconOption(mPm.getApplicationInfo(overlayPackage, 0).loadLabel(mPm).toString());
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
        StatusBarIconOption option = new StatusBarIconOption(mContext.getString(R.string.default_theme_title), true);
        try {
            option.addIcon(loadIconPreviewDrawable("ic_wifi", ANDROID_PACKAGE));
        } catch (NameNotFoundException | NotFoundException e) {
            Log.w(TAG, "Didn't find SystemUi package icons, will skip option", e);
        }
        option.addOverlayPackage(OVERLAY_CATEGORY_ICON_WIFI, null);
        option.addOverlayPackage(OVERLAY_CATEGORY_ICON_SIGNAL, null);
        mOptions.add(option);
    }
} 