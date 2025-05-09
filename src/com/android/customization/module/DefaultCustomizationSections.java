package com.android.customization.module;

import android.app.WallpaperManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;

import com.android.customization.model.font.FontManager;
import com.android.customization.model.font.FontSectionController;
import com.android.customization.model.grid.GridOptionsManager;
import com.android.customization.model.iconpack.IconPackManager;
import com.android.customization.model.iconpack.IconPackSectionController;
import com.android.customization.model.iconshape.IconShapeManager;
import com.android.customization.model.iconshape.IconShapeSectionController;
import com.android.customization.model.lockfont.LockFontSectionController;
import com.android.customization.model.lockfont.LockFontManager;
import com.android.customization.model.theme.OverlayManagerCompat;
import com.android.customization.model.themedicon.ThemedIconSectionController;
import com.android.customization.model.themedicon.ThemedIconSwitchProvider;
import com.android.customization.model.themedicon.domain.interactor.ThemedIconInteractor;
import com.android.customization.model.themedicon.domain.interactor.ThemedIconSnapshotRestorer;
import com.android.customization.model.udfps.UdfpsAnimationSectionController;
import com.android.customization.module.logging.ThemesUserEventLogger;
import com.android.customization.picker.clock.ui.view.ClockViewFactory;
import com.android.customization.picker.clock.ui.viewmodel.ClockCarouselViewModel;
import com.android.customization.picker.color.domain.interactor.ColorPickerInteractor;
import com.android.customization.picker.color.ui.section.ColorSectionController;
import com.android.customization.picker.color.ui.viewmodel.ColorPickerViewModel;
import com.android.customization.picker.grid.domain.interactor.GridInteractor;
import com.android.customization.picker.grid.ui.section.GridSectionController;
import com.android.customization.picker.notifications.ui.section.NotificationSectionController;
import com.android.customization.picker.notifications.ui.viewmodel.NotificationSectionViewModel;
import com.android.customization.picker.qs.QSSectionController;
import com.android.customization.picker.qs.ui.viewmodel.QSSectionViewModel;
import com.android.customization.picker.preview.ui.section.PreviewWithClockCarouselSectionController;
import com.android.customization.picker.preview.ui.section.PreviewWithThemeSectionController;
import com.android.customization.picker.quickaffordance.ui.section.KeyguardQuickAffordanceSectionController;
import com.android.customization.picker.quickaffordance.ui.viewmodel.KeyguardQuickAffordancePickerViewModel;
import com.android.customization.picker.settings.ui.section.ColorContrastSectionController;
import com.android.customization.picker.settings.ui.section.MoreSettingsSectionController;
import com.android.customization.picker.settings.ui.viewmodel.ColorContrastSectionViewModel;
import com.android.wallpaper.config.BaseFlags;
import com.android.wallpaper.model.CustomizationSectionController;
import com.android.wallpaper.model.CustomizationSectionController.CustomizationSectionNavigationController;
import com.android.wallpaper.model.PermissionRequester;
import com.android.wallpaper.model.Screen;
import com.android.wallpaper.model.WallpaperPreviewNavigator;
import com.android.wallpaper.module.CurrentWallpaperInfoFactory;
import com.android.wallpaper.module.CustomizationSections;
import com.android.wallpaper.picker.customization.data.repository.WallpaperColorsRepository;
import com.android.wallpaper.picker.customization.domain.interactor.WallpaperInteractor;
import com.android.wallpaper.picker.customization.ui.section.ConnectedSectionController;
import com.android.wallpaper.picker.customization.ui.section.WallpaperQuickSwitchSectionController;
import com.android.wallpaper.picker.customization.ui.viewmodel.CustomizationPickerViewModel;
import com.android.wallpaper.util.DisplayUtils;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

/** {@link CustomizationSections} for the customization picker. */
public final class DefaultCustomizationSections implements CustomizationSections {

    private final ColorPickerViewModel.Factory mColorPickerViewModelFactory;
    private final KeyguardQuickAffordancePickerViewModel.Factory
            mKeyguardQuickAffordancePickerViewModelFactory;
    private final ColorContrastSectionViewModel.Factory
            mColorContrastSectionViewModelFactory;
    private final NotificationSectionViewModel.Factory mNotificationSectionViewModelFactory;
    private final QSSectionViewModel.Factory mQSSectionViewModelFactory;
    private final BaseFlags mFlags;
    private final ClockCarouselViewModel.Factory mClockCarouselViewModelFactory;
    private final ClockViewFactory mClockViewFactory;
    private final ThemedIconSnapshotRestorer mThemedIconSnapshotRestorer;
    private final ThemedIconInteractor mThemedIconInteractor;
    private final GridInteractor mGridInteractor;
    private final ColorPickerInteractor mColorPickerInteractor;
    private final ThemesUserEventLogger mThemesUserEventLogger;

    public DefaultCustomizationSections(
            ColorPickerViewModel.Factory colorPickerViewModelFactory,
            KeyguardQuickAffordancePickerViewModel.Factory
                    keyguardQuickAffordancePickerViewModelFactory,
            ColorContrastSectionViewModel.Factory colorContrastSectionViewModelFactory,
            NotificationSectionViewModel.Factory notificationSectionViewModelFactory,
            QSSectionViewModel.Factory qsSectionViewModelFactory,
            BaseFlags flags,
            ClockCarouselViewModel.Factory clockCarouselViewModelFactory,
            ClockViewFactory clockViewFactory,
            ThemedIconSnapshotRestorer themedIconSnapshotRestorer,
            ThemedIconInteractor themedIconInteractor,
            GridInteractor gridInteractor,
            ColorPickerInteractor colorPickerInteractor,
            ThemesUserEventLogger themesUserEventLogger) {
        mColorPickerViewModelFactory = colorPickerViewModelFactory;
        mKeyguardQuickAffordancePickerViewModelFactory =
                keyguardQuickAffordancePickerViewModelFactory;
        mNotificationSectionViewModelFactory = notificationSectionViewModelFactory;
        mQSSectionViewModelFactory = qsSectionViewModelFactory;
        mFlags = flags;
        mClockCarouselViewModelFactory = clockCarouselViewModelFactory;
        mClockViewFactory = clockViewFactory;
        mThemedIconSnapshotRestorer = themedIconSnapshotRestorer;
        mThemedIconInteractor = themedIconInteractor;
        mGridInteractor = gridInteractor;
        mColorPickerInteractor = colorPickerInteractor;
        mThemesUserEventLogger = themesUserEventLogger;
        mColorContrastSectionViewModelFactory = colorContrastSectionViewModelFactory;
    }

    @Override
    public List<CustomizationSectionController<?>> getSectionControllersForScreen(
            Screen screen,
            FragmentActivity activity,
            LifecycleOwner lifecycleOwner,
            WallpaperColorsRepository wallpaperColorsRepository,
            PermissionRequester permissionRequester,
            WallpaperPreviewNavigator wallpaperPreviewNavigator,
            CustomizationSectionNavigationController sectionNavigationController,
            @Nullable Bundle savedInstanceState,
            CurrentWallpaperInfoFactory wallpaperInfoFactory,
            DisplayUtils displayUtils,
            CustomizationPickerViewModel customizationPickerViewModel,
            WallpaperInteractor wallpaperInteractor,
            WallpaperManager wallpaperManager,
            boolean isTwoPaneAndSmallWidth) {
        List<CustomizationSectionController<?>> sectionControllers = new ArrayList<>();

        // Wallpaper section.
        sectionControllers.add(
                mFlags.isCustomClocksEnabled(activity)
                        ? new PreviewWithClockCarouselSectionController(
                        activity,
                        lifecycleOwner,
                        screen,
                        wallpaperInfoFactory,
                        wallpaperColorsRepository,
                        displayUtils,
                        mClockCarouselViewModelFactory,
                        mClockViewFactory,
                        wallpaperPreviewNavigator,
                        sectionNavigationController,
                        wallpaperInteractor,
                        mThemedIconInteractor,
                        mGridInteractor,
                        mColorPickerInteractor,
                        wallpaperManager,
                        isTwoPaneAndSmallWidth,
                        customizationPickerViewModel)
                        : new PreviewWithThemeSectionController(
                                activity,
                                lifecycleOwner,
                                screen,
                                wallpaperInfoFactory,
                                wallpaperColorsRepository,
                                displayUtils,
                                wallpaperPreviewNavigator,
                                wallpaperInteractor,
                                mThemedIconInteractor,
                                mGridInteractor,
                                mColorPickerInteractor,
                                wallpaperManager,
                                isTwoPaneAndSmallWidth,
                                customizationPickerViewModel));

        sectionControllers.add(
                new ConnectedSectionController(
                        // Theme color section.
                        new ColorSectionController(
                                sectionNavigationController,
                                new ViewModelProvider(
                                        activity,
                                        mColorPickerViewModelFactory)
                                        .get(ColorPickerViewModel.class),
                                lifecycleOwner),
                        // Wallpaper quick switch section.
                        new WallpaperQuickSwitchSectionController(
                                customizationPickerViewModel.getWallpaperQuickSwitchViewModel(
                                        screen),
                                lifecycleOwner,
                                sectionNavigationController,
                                savedInstanceState == null),
                        /* reverseOrderWhenHorizontal= */ true));

        switch (screen) {
            case LOCK_SCREEN:
                // Lock screen quick affordances section.
                sectionControllers.add(
                        new KeyguardQuickAffordanceSectionController(
                                sectionNavigationController,
                                new ViewModelProvider(
                                        activity,
                                        mKeyguardQuickAffordancePickerViewModelFactory)
                                        .get(KeyguardQuickAffordancePickerViewModel.class),
                                lifecycleOwner));

                String clockFaceJson = Settings.Secure.getString(
                        activity.getContentResolver(), "lock_screen_custom_clock_face");

                boolean shouldAddLockFontSection = false;
                if (clockFaceJson == null || clockFaceJson.isEmpty()) {
                    shouldAddLockFontSection = true;
                } else {
                    try {
                        JSONObject clockFace = new JSONObject(clockFaceJson);
                        if (!clockFace.has("clockId") || "DEFAULT".equals(clockFace.optString("clockId"))) {
                            shouldAddLockFontSection = true;
                        }
                    } catch (JSONException e) {
                        Log.w("CustomizationSections", "Failed to parse lock_screen_custom_clock_face: " + clockFaceJson, e);
                    }
                }

                if (shouldAddLockFontSection) {
                    sectionControllers.add(new LockFontSectionController(
                            LockFontManager.getInstance(activity, new OverlayManagerCompat(activity)),
                            sectionNavigationController));
                }

                // Notifications section.
                sectionControllers.add(
                        new NotificationSectionController(
                                new ViewModelProvider(
                                        activity,
                                        mNotificationSectionViewModelFactory)
                                        .get(NotificationSectionViewModel.class),
                                lifecycleOwner));

               // Udfps animation selection section
               sectionControllers.add(new UdfpsAnimationSectionController(sectionNavigationController));

                // More settings section.
                sectionControllers.add(new MoreSettingsSectionController());
                break;

            case HOME_SCREEN:
                // Themed app icon section.
                sectionControllers.add(
                        new ThemedIconSectionController(
                                ThemedIconSwitchProvider.getInstance(activity),
                                mThemedIconInteractor,
                                savedInstanceState,
                                mThemedIconSnapshotRestorer,
                                mThemesUserEventLogger));

                // QS gradient section controller
                sectionControllers.add(new QSSectionController(
                        new ViewModelProvider(activity, mQSSectionViewModelFactory)
                                .get(QSSectionViewModel.class),
                        lifecycleOwner));

                // Color contrast section
                if (mFlags.isColorContrastControlEnabled()) {
                    sectionControllers.add(
                            new ColorContrastSectionController(new ViewModelProvider(activity,
                                    mColorContrastSectionViewModelFactory)
                                    .get(ColorContrastSectionViewModel.class), lifecycleOwner));
                }
                // App grid section.
                sectionControllers.add(
                        new GridSectionController(
                                GridOptionsManager.getInstance(activity),
                                sectionNavigationController,
                                lifecycleOwner));

                // Icon pack selection section.
                sectionControllers.add(new IconPackSectionController(
                        IconPackManager.getInstance(activity, new OverlayManagerCompat(activity)),
                        sectionNavigationController));

                // Font selection section.
                sectionControllers.add(new FontSectionController(
                        FontManager.getInstance(activity, new OverlayManagerCompat(activity)),
                        sectionNavigationController));

                // Icon shape selection section.
                sectionControllers.add(new IconShapeSectionController(
                        IconShapeManager.getInstance(activity, new OverlayManagerCompat(activity)),
                        sectionNavigationController));
                break;
        }

        return sectionControllers;
    }
}
