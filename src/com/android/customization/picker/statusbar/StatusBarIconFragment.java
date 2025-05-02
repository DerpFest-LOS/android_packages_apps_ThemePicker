package com.android.customization.picker.statusbar;

import static com.android.wallpaper.widget.BottomActionBar.BottomAction.APPLY_TEXT;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.RecyclerView;

import com.android.customization.model.CustomizationManager.Callback;
import com.android.customization.model.CustomizationManager.OptionsFetchedListener;
import com.android.customization.model.CustomizationOption;
import com.android.customization.model.statusbar.StatusBarIconOption;
import com.android.customization.model.statusbar.StatusBarIconManager;
import com.android.customization.model.theme.OverlayManagerCompat;
import com.android.customization.module.logging.ThemesUserEventLogger;
import com.android.customization.picker.WallpaperPreviewer;
import com.android.customization.widget.OptionSelectorController;
import com.android.customization.widget.OptionSelectorController.CheckmarkStyle;
import com.android.themepicker.R;
import com.android.wallpaper.picker.AppbarFragment;
import com.android.wallpaper.widget.BottomActionBar;

import java.util.List;

public class StatusBarIconFragment extends AppbarFragment {
    private static final String TAG = "StatusBarIconFragment";
    private static final String KEY_STATE_SELECTED_OPTION = "StatusBarIconFragment.selectedOption";
    private static final String KEY_STATE_BOTTOM_ACTION_BAR_VISIBLE =
            "StatusBarIconFragment.bottomActionBarVisible";

    public static StatusBarIconFragment newInstance(CharSequence title) {
        StatusBarIconFragment fragment = new StatusBarIconFragment();
        fragment.setArguments(AppbarFragment.createArguments(title));
        return fragment;
    }

    private RecyclerView mOptionsContainer;
    private OptionSelectorController<StatusBarIconOption> mOptionsController;
    private StatusBarIconManager mStatusBarIconManager;
    private StatusBarIconOption mSelectedOption;
    private ContentLoadingProgressBar mLoading;
    private ViewGroup mContent;
    private View mError;
    private BottomActionBar mBottomActionBar;
    private Boolean mEnterTransitionEnded = false, mOptionsLoaded = false;

    private final Callback mApplyStatusBarIconCallback = new Callback() {
        @Override
        public void onSuccess() {
        }

        @Override
        public void onError(@Nullable Throwable throwable) {
            mBottomActionBar.enableActions();
            mBottomActionBar.hide();
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStatusBarIconManager = StatusBarIconManager.getInstance(getContext(),
                new OverlayManagerCompat(getContext()));
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_icon_pack_picker, container, false);
        mOptionsContainer = view.findViewById(R.id.options_container);
        mLoading = view.findViewById(R.id.loading_indicator);
        mContent = view.findViewById(R.id.content_section);
        mError = view.findViewById(R.id.error_section);

        mStatusBarIconManager = StatusBarIconManager.getInstance(getContext(),
                new OverlayManagerCompat(getContext()));
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
        mBottomActionBar.setActionClickListener(APPLY_TEXT, v -> applyStatusBarIconOption(mSelectedOption));
    }

    private void applyStatusBarIconOption(StatusBarIconOption statusBarIconOption) {
        mBottomActionBar.disableActions();
        mStatusBarIconManager.apply(statusBarIconOption, mApplyStatusBarIconCallback);
    }

    private void setUpOptions(@Nullable Bundle savedInstanceState) {
        hideError();
        mLoading.show();
        mStatusBarIconManager.fetchOptions(new OptionsFetchedListener<StatusBarIconOption>() {
            @Override
            public void onOptionsLoaded(List<StatusBarIconOption> options) {
                mLoading.hide();
                mOptionsController = new OptionSelectorController<>(
                        mOptionsContainer, options, /* useGrid= */ false, CheckmarkStyle.CORNER);
                mOptionsController.initOptions(mStatusBarIconManager);
                mSelectedOption = getActiveOption(options);
                onOptionSelected(mSelectedOption);
                restoreBottomActionBarVisibility(savedInstanceState);
                mOptionsLoaded = true;
                maybeSetSelectedOption();

                mOptionsController.addListener(selectedOption -> {
                    onOptionSelected(selectedOption);
                    if (((StatusBarIconOption) selectedOption).isActive(mStatusBarIconManager)) {
                        mBottomActionBar.hide();
                    } else {
                        mBottomActionBar.show();
                    }
                });
            }

            @Override
            public void onError(@Nullable Throwable throwable) {
                if (throwable != null) {
                    Log.e(TAG, "Error loading status bar icon options", throwable);
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

    private StatusBarIconOption getActiveOption(List<StatusBarIconOption> options) {
        return options.stream()
                .filter(option -> option.isActive(mStatusBarIconManager))
                .findAny()
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
        mSelectedOption = (StatusBarIconOption) selectedOption;
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