package com.android.customization.model.statusbar;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;

import com.android.customization.model.CustomizationManager.Callback;
import com.android.customization.model.CustomizationManager.OptionsFetchedListener;
import com.android.customization.model.CustomizationOption;
import com.android.customization.picker.statusbar.StatusBarIconFragment;
import com.android.customization.picker.statusbar.StatusBarIconSectionView;
import com.android.customization.widget.OptionSelectorController;
import com.android.customization.widget.OptionSelectorController.OptionSelectedListener;
import com.android.themepicker.R;
import com.android.wallpaper.model.CustomizationSectionController;
import com.android.wallpaper.util.LaunchUtils;

import java.util.List;

public class StatusBarIconSectionController implements CustomizationSectionController<StatusBarIconSectionView> {
    private static final String TAG = "StatusBarIconSectionController";

    private final StatusBarIconManager mStatusBarIconManager;
    private final CustomizationSectionNavigationController mSectionNavigationController;
    private final Callback mApplyIconCallback = new Callback() {
        @Override
        public void onSuccess() {
        }

        @Override
        public void onError(@Nullable Throwable throwable) {
        }
    };

    public StatusBarIconSectionController(StatusBarIconManager statusBarIconManager,
            CustomizationSectionNavigationController sectionNavigationController) {
        mStatusBarIconManager = statusBarIconManager;
        mSectionNavigationController = sectionNavigationController;
    }

    @Override
    public boolean isAvailable(Context context) {
        return mStatusBarIconManager.isAvailable();
    }

    @Override
    public StatusBarIconSectionView createView(Context context) {
        StatusBarIconSectionView sectionView = (StatusBarIconSectionView) LayoutInflater.from(context)
                .inflate(R.layout.icon_section_view, null);
        sectionView.setOnClickListener(v -> {
            if (mStatusBarIconManager.isAvailable()) {
                mSectionNavigationController.navigateTo(StatusBarIconFragment.newInstance(
                        context.getString(R.string.status_bar_icons_title)));
            }
        });
        return sectionView;
    }

    @Override
    public void onViewActivated(Context context, StatusBarIconSectionView view) {
        mStatusBarIconManager.fetchOptions(new OptionsFetchedListener<StatusBarIconOption>() {
            @Override
            public void onOptionsLoaded(List<StatusBarIconOption> options) {
                StatusBarIconOption activeOption = null;
                for (StatusBarIconOption option : options) {
                    if (option.isActive(mStatusBarIconManager)) {
                        activeOption = option;
                        break;
                    }
                }
                if (activeOption != null) {
                    activeOption.bindThumbnailTile(view);
                }
            }

            @Override
            public void onError(@Nullable Throwable throwable) {
                if (throwable != null) {
                    Log.e(TAG, "Error loading status bar icon options", throwable);
                }
            }
        }, /*reload= */ false);
    }

    @Override
    public void onViewReleased(StatusBarIconSectionView view) {
    }

    @Override
    public void onViewRecycled(StatusBarIconSectionView view) {
    }

    @Override
    public void onViewDestroyed(StatusBarIconSectionView view) {
    }
} 