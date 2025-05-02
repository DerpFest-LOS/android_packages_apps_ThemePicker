package com.android.customization.model.statusbar;

import static com.android.customization.model.ResourceConstants.SYSUI_PACKAGE;
import static com.android.customization.model.ResourceConstants.OVERLAY_CATEGORY_ICON_WIFI;
import static com.android.customization.model.ResourceConstants.OVERLAY_CATEGORY_ICON_SIGNAL;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
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
import com.android.customization.picker.statusbar.StatusBarIconSectionView;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class StatusBarIconOption implements CustomizationOption<StatusBarIconOption> {
    private final String mTitle;
    private final Map<String, String> mOverlayPackages;
    private final boolean mIsDefault;
    private final Drawable mWifiIcon;
    private final Drawable mSignalIcon;

    public StatusBarIconOption(String title, Map<String, String> overlayPackages, boolean isDefault, Drawable wifiIcon, Drawable signalIcon) {
        mTitle = title;
        mOverlayPackages = overlayPackages;
        mIsDefault = isDefault;
        mWifiIcon = wifiIcon;
        mSignalIcon = signalIcon;
    }

    @Override
    public void bindThumbnailTile(View view) {
        if (view instanceof StatusBarIconSectionView) {
            ((StatusBarIconSectionView) view).setTitle(mTitle);
        }
        Resources res = view.getContext().getResources();
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
        mWifiIcon.setColorFilter(colorFilter, android.graphics.PorterDuff.Mode.SRC_ATOP);
        ((ImageView) view.findViewById(resId)).setImageDrawable(mWifiIcon);
        view.setContentDescription(mTitle);
    }

    @Override
    public boolean isActive(CustomizationManager<StatusBarIconOption> manager) {
        StatusBarIconManager iconManager = (StatusBarIconManager) manager;
        OverlayManagerCompat overlayManager = iconManager.getOverlayManager();
        if (mIsDefault) {
            return overlayManager.getEnabledPackageName(SYSUI_PACKAGE, OVERLAY_CATEGORY_ICON_WIFI) == null &&
                    overlayManager.getEnabledPackageName(SYSUI_PACKAGE, OVERLAY_CATEGORY_ICON_SIGNAL) == null;
        }
        for (Map.Entry<String, String> overlayEntry : getOverlayPackages().entrySet()) {
            if (overlayEntry.getValue() == null || !overlayEntry.getValue().equals(overlayManager.getEnabledPackageName(SYSUI_PACKAGE, overlayEntry.getKey()))) {
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
        ViewGroup cardBody = container.findViewById(R.id.theme_preview_card_body_container);
        if (cardBody.getChildCount() == 0) {
            LayoutInflater.from(container.getContext()).inflate(
                    R.layout.preview_card_icon_content, cardBody, true);
        }
        ((ImageView) container.findViewById(R.id.preview_icon_0)).setImageDrawable(mWifiIcon);
        ((ImageView) container.findViewById(R.id.preview_icon_1)).setImageDrawable(mSignalIcon);
    }

    public Map<String, String> getOverlayPackages() {
        return mOverlayPackages;
    }

    public boolean isDefault() {
        return mIsDefault;
    }
} 