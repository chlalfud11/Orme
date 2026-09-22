package com.orme.app.navigation;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.widget.FrameLayout;

import com.orme.app.ui.components.AuthComponents;
import com.orme.app.ui.profile.ProfileScreen;
import com.orme.app.ui.map.MapScreen;
import com.orme.app.ui.search.FavoritesScreen;
import com.orme.app.ui.search.TravelSearchScreen;
import com.orme.app.ui.signup.SignupScreen;
import com.orme.app.ui.login.LoginScreen;
import com.orme.app.ui.splash.SplashScreen;
import com.orme.app.ui.theme.AppColors;
import com.orme.app.ui.theme.AppTheme;

import java.util.LinkedHashSet;
import java.util.Set;

/** Compose NavHost를 대신하는 단일 Activity용 화면 전환기. */
public final class AppNavigator {
    public static final int IMAGE_REQUEST = 4107;

    public interface ImagePickCallback {
        void onPicked(Uri uri);
    }

    private final Activity activity;
    private final FrameLayout root;
    private final Set<String> favorites = new LinkedHashSet<>();
    private boolean darkMode;
    private ImagePickCallback imagePickCallback;
    private View currentView;

    public AppNavigator(Activity activity, FrameLayout root) {
        this.activity = activity;
        this.root = root;
    }

    public Activity activity() {
        return activity;
    }

    public AppColors.Palette colors() {
        return darkMode ? AppColors.dark() : AppColors.light();
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public Set<String> favorites() {
        return new LinkedHashSet<>(favorites);
    }

    public void toggleFavorite(String name) {
        if (!favorites.add(name)) {
            favorites.remove(name);
        }
    }

    public void setDarkMode(boolean enabled) {
        darkMode = enabled;
    }

    public void openImagePicker(ImagePickCallback callback) {
        imagePickCallback = callback;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        activity.startActivityForResult(intent, IMAGE_REQUEST);
    }

    public void onImageResult(int resultCode, Intent data) {
        ImagePickCallback callback = imagePickCallback;
        imagePickCallback = null;
        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null && callback != null) {
            callback.onPicked(data.getData());
        }
    }

    public void onLocationPermissionResult(int requestCode, int[] grantResults) {
        if (currentView instanceof TravelSearchScreen) {
            ((TravelSearchScreen) currentView).onLocationPermissionResult(requestCode, grantResults);
        }
    }

    public void showSplash() {
        show(new SplashScreen(activity, this));
    }

    public void showLogin() {
        show(new LoginScreen(activity, this));
    }

    public void showSignup() {
        show(new SignupScreen(activity, this));
    }

    public void showMap() {
        show(new MapScreen(activity, this));
    }

    public void showSearch() {
        show(new TravelSearchScreen(activity, this));
    }

    public void showFavorites() {
        show(new FavoritesScreen(activity, this));
    }

    public void showProfile() {
        if (currentView instanceof ProfileScreen) {
            AppTheme.apply(activity, colors(), true);
            ((ProfileScreen) currentView).refreshTheme();
            AppTheme.hideSystemBars(activity);
            return;
        }
        showProfileBarsHidden(new ProfileScreen(activity, this));
    }

    public void logout() {
        showLogin();
    }

    public void show(View content) {
        show(content, true);
    }

    private void show(View content, boolean systemBarsVisible) {
        root.removeAllViews();
        root.addView(content, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
        ));
        currentView = content;
        AppTheme.apply(activity, colors(), true);
        if (systemBarsVisible) {
            AppTheme.showSystemBars(activity);
        } else {
            AppTheme.hideSystemBars(activity);
        }
    }

    public FrameLayout root() {
        return root;
    }

    public void showProfileBarsHidden(View content) {
        show(content, false);
    }

    public boolean onBackPressed() {
        if (currentView instanceof com.orme.app.ui.map.MapScreen) {
            return ((com.orme.app.ui.map.MapScreen) currentView).onBackPressed();
        }
        if (currentView instanceof com.orme.app.ui.diary.RegionDiaryFlow) {
            return ((com.orme.app.ui.diary.RegionDiaryFlow) currentView).onBackPressed();
        }
        return false;
    }

    public View bottomBar() {
        return AuthComponents.bottomBar(activity, this);
    }
}
