package com.android.customization.picker.statusbar;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.android.customization.model.CustomizationManager.Callback;
import com.android.customization.model.CustomizationOption;
import com.android.customization.model.statusbar.StatusBarIconOption;
import com.android.customization.widget.OptionSelectorController;
import com.android.customization.widget.OptionSelectorController.OptionSelectedListener;
import com.android.wallpaper.R;
import com.android.wallpaper.picker.SectionView;

public final class StatusBarIconSectionView extends SectionView {
    private View mContent;
    private TextView mTitle;
    private TextView mError;
    private View mLoading;
    private OptionSelectorController<StatusBarIconOption> mOptionsController;

    public StatusBarIconSectionView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setTitle(R.string.status_bar_icons_title);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mContent = findViewById(R.id.content_section);
        mTitle = findViewById(R.id.section_title);
        mError = findViewById(R.id.error_section);
        mLoading = findViewById(R.id.loading_indicator);
        setTitle(R.string.status_bar_icons_title);
    }

    public void setOptionsController(OptionSelectorController<StatusBarIconOption> controller) {
        mOptionsController = controller;
        mContent.setVisibility(View.VISIBLE);
        mError.setVisibility(View.GONE);
        mLoading.setVisibility(View.GONE);
    }

    public void showError() {
        mContent.setVisibility(View.GONE);
        mError.setVisibility(View.VISIBLE);
        mLoading.setVisibility(View.GONE);
    }

    public void setTitle(String title) {
        mTitle.setText(title);
    }
} 