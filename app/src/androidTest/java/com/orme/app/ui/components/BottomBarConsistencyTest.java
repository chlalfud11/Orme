package com.orme.app.ui.components;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.orme.app.MainActivity;
import com.orme.app.navigation.AppNavigator;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public final class BottomBarConsistencyTest {
    @Test
    public void mapAndProfileBottomBarsShareTheProfilePosition() {
        AtomicInteger mapTop = new AtomicInteger();
        AtomicInteger profileTop = new AtomicInteger();
        AtomicReference<AppNavigator> navigatorRef = new AtomicReference<>();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                FrameLayout host = new FrameLayout(activity);
                activity.setContentView(host);
                AppNavigator navigator = new AppNavigator(activity, host);
                navigatorRef.set(navigator);
                navigator.showMap();
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(activity -> {
                View mapButton = findByDescription(
                        activity.getWindow().getDecorView(),
                        "프로필"
                );
                assertNotNull("지도 화면에 프로필 버튼이 없습니다.", mapButton);
                mapTop.set(topOf(mapButton));
            });

            scenario.onActivity(activity -> {
                navigatorRef.get().showProfile();
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            scenario.onActivity(activity -> {
                View profileButton = findByDescription(
                        activity.getWindow().getDecorView(),
                        "프로필"
                );
                assertNotNull("프로필 화면에 프로필 버튼이 없습니다.", profileButton);
                profileTop.set(topOf(profileButton));
            });
        }

        assertEquals("모든 화면의 하단 네비게이션 위치가 프로필과 같아야 합니다.", profileTop.get(), mapTop.get());
    }

    private static int topOf(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return location[1];
    }

    private static View findByDescription(View view, String expected) {
        CharSequence description = view.getContentDescription();
        if (description != null && expected.contentEquals(description)) {
            return view;
        }
        if (!(view instanceof ViewGroup)) {
            return null;
        }
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            View found = findByDescription(group.getChildAt(i), expected);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}
