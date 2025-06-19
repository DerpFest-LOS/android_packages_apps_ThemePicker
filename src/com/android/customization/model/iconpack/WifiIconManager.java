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

import static com.android.customization.model.ResourceConstants.OVERLAY_CATEGORY_ICON_WIFI;

import android.content.Context;
import android.os.UserHandle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.android.customization.model.CustomizationManager;
import com.android.customization.model.theme.OverlayManagerCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class WifiIconManager implements CustomizationManager<WifiIconOption> {

    private static WifiIconManager sWifiIconOptionManager;
    private Context mContext;
    private WifiIconOption mActiveOption;
    private OverlayManagerCompat mOverlayManager;
    private WifiIconOptionProvider mProvider;
    private static final String TAG = "WifiIconManager";
    private static final String KEY_STATE_CURRENT_SELECTION = "WifiIconManager.currentSelection";
    private static final String[] mCurrentCategories = new String[]{OVERLAY_CATEGORY_ICON_WIFI};

    WifiIconManager(Context context, OverlayManagerCompat overlayManager, WifiIconOptionProvider provider) {
        mContext = context;
        mProvider = provider;
        mOverlayManager = overlayManager;
    }

    @Override
    public boolean isAvailable() {
        return mOverlayManager.isAvailable();
    }

    @Override
    public void apply(WifiIconOption option, @Nullable Callback callback) {
        if (!persistOverlay(option)) {
            Toast failed = Toast.makeText(mContext, "Failed to apply wifi icon pack, reboot to try again.", Toast.LENGTH_SHORT);
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
    public void fetchOptions(OptionsFetchedListener<WifiIconOption> callback, boolean reload) {
        List<WifiIconOption> options = mProvider.getOptions();
        for (WifiIconOption option : options) {
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

    private boolean persistOverlay(WifiIconOption toPersist) {
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

    public static WifiIconManager getInstance(Context context, OverlayManagerCompat overlayManager) {
        if (sWifiIconOptionManager == null) {
            Context applicationContext = context.getApplicationContext();
            sWifiIconOptionManager = new WifiIconManager(context, overlayManager, new WifiIconOptionProvider(applicationContext, overlayManager));
        }
        return sWifiIconOptionManager;
    }
} 
