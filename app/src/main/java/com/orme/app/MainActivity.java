package com.orme.app;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;

import com.orme.app.navigation.AppNavigator;

/** Orme의 단일 Activity 진입점. */
public final class MainActivity extends ComponentActivity {
    private AppNavigator navigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout root = new FrameLayout(this);
        navigator = new AppNavigator(this, root);
        setContentView(root);
        navigator.showSplash();
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (navigator.onBackPressed()) {
                    return;
                }
                setEnabled(false);
                getOnBackPressedDispatcher().onBackPressed();
                setEnabled(true);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (navigator != null && requestCode == AppNavigator.IMAGE_REQUEST) {
            navigator.onImageResult(resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            String[] permissions,
            int[] grantResults
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (navigator != null) {
            navigator.onLocationPermissionResult(requestCode, grantResults);
        }
    }

}
