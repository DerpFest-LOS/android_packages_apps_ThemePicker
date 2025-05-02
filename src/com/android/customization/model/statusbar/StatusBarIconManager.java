package com.android.customization.model.statusbar;

import static com.android.customization.model.ResourceConstants.OVERLAY_CATEGORY_ICON_WIFI;
import static com.android.customization.model.ResourceConstants.OVERLAY_CATEGORY_ICON_SIGNAL;

import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.customization.model.CustomizationManager;
import com.android.customization.model.CustomizationManager.OptionsFetchedListener;
import com.android.customization.model.CustomizationOption;
import com.android.customization.model.theme.OverlayManagerCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StatusBarIconManager extends CustomizationManager<StatusBarIconOption> {
    private static final String TAG = "StatusBarIconManager";
    private static StatusBarIconManager sStatusBarIconManager;
    private final OverlayManagerCompat mOverlayManager;
    private Context mContext;
    private StatusBarIconOption mActiveOption;
    private StatusBarIconOptionProvider mProvider;
    private static final String KEY_STATE_CURRENT_SELECTION = "StatusBarIconManager.currentSelection";
    private static final String[] mCurrentCategories = new String[]{OVERLAY_CATEGORY_ICON_WIFI, OVERLAY_CATEGORY_ICON_SIGNAL};

    public static StatusBarIconManager getInstance(Context context, OverlayManagerCompat overlayManager) {
        if (sStatusBarIconManager == null) {
            Context applicationContext = context.getApplicationContext();
            sStatusBarIconManager = new StatusBarIconManager(context, overlayManager, new StatusBarIconOptionProvider(applicationContext, overlayManager));
        }
        return sStatusBarIconManager;
    }

    private StatusBarIconManager(Context context, OverlayManagerCompat overlayManager, StatusBarIconOptionProvider provider) {
        super(context);
        mOverlayManager = overlayManager;
        mContext = context;
        mProvider = provider;
    }

    @Override
    public boolean isAvailable() {
        return mOverlayManager.isAvailable();
    }

    @Override
    public void apply(StatusBarIconOption option, Callback callback) {
        if (!persistOverlay(option)) {
            Toast failed = Toast.makeText(mContext, "Failed to apply status bar icons, reboot to try again.", Toast.LENGTH_SHORT);
            failed.show();
            if (callback != null) {
                callback.onError(null);
            }
            return;
        }
        if (option.isDefault()) {
            if (mActiveOption.isDefault()) return;
            mActiveOption.getOverlayPackages().forEach((category, overlay) -> mOverlayManager.disableOverlay(overlay, UserHandle.myUserId()));
        }
        if (callback != null) {
            callback.onSuccess();
        }
        mActiveOption = option;
    }

    @Override
    public void fetchOptions(OptionsFetchedListener<StatusBarIconOption> callback, boolean reload) {
        List<StatusBarIconOption> options = mProvider.getOptions();
        for (StatusBarIconOption option : options) {
            if (option.isActive(this)) {
                mActiveOption = option;
                break;
            }
        }
        callback.onOptionsLoaded(options);
    }

    public OverlayManagerCompat getOverlayManager() {
        return mOverlayManager;
    }

    private boolean persistOverlay(StatusBarIconOption toPersist) {
        String value = Settings.Secure.getStringForUser(mContext.getContentResolver(),
                Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES, UserHandle.myUserId());
        JSONObject json;
        if (value == null) {
            json = new JSONObject();
        } else {
            try {
                json = new JSONObject(value);
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing current settings value:\n" + e.getMessage());
                return false;
            }
        }
        // removing all currently enabled overlays from the json
        for (String categoryName : mCurrentCategories) {
            json.remove(categoryName);
        }
        // adding the new ones
        for (String categoryName : mCurrentCategories) {
            try {
                json.put(categoryName, toPersist.getOverlayPackages().get(categoryName));
            } catch (JSONException e) {
                Log.e(TAG, "Error adding new settings value:\n" + e.getMessage());
                return false;
            }
        }
        // updating the setting
        Settings.Secure.putStringForUser(mContext.getContentResolver(),
                Settings.Secure.THEME_CUSTOMIZATION_OVERLAY_PACKAGES,
                json.toString(), UserHandle.myUserId());
        return true;
    }
} 