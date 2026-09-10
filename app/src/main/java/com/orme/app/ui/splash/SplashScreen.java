package com.orme.app.ui.splash;

import android.content.Context;
import android.view.Gravity;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.orme.app.R;
import com.orme.app.navigation.AppNavigator;
import com.orme.app.ui.components.ViewUtils;

/** 앱 시작 화면. 기존 PNG를 같은 비율로 전체 배경에 깐다. */
public final class SplashScreen extends FrameLayout {
    private final Runnable loginNavigation;

    public SplashScreen(Context context, AppNavigator navigator) {
        super(context);
        loginNavigation = navigator::showLogin;
        setBackgroundColor(0xFFF5EFE3);

        ImageView image = new ImageView(context);
        image.setImageResource(R.drawable.splash_bg);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        addView(image, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        ));

        ScaleAnimation animation = new ScaleAnimation(
                0f,
                1f,
                0f,
                1f,
                Animation.RELATIVE_TO_SELF,
                0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f
        );
        animation.setDuration(600);
        animation.setInterpolator(new OvershootInterpolator());
        image.startAnimation(animation);
        postDelayed(loginNavigation, 1600);
    }

    @Override
    protected void onDetachedFromWindow() {
        removeCallbacks(loginNavigation);
        super.onDetachedFromWindow();
    }
}
