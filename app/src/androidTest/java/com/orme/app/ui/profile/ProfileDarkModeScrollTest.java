package com.orme.app.ui.profile;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;

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
public final class ProfileDarkModeScrollTest {
    @Test
    public void darkModeKeepsProfileScrollPosition() {
        AtomicReference<AppNavigator> navigatorRef = new AtomicReference<>();
        AtomicReference<ScrollView> scrollRef = new AtomicReference<>();
        AtomicInteger scrollBefore = new AtomicInteger();
        AtomicInteger scrollAfter = new AtomicInteger();

        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                FrameLayout host = new FrameLayout(activity);
                activity.setContentView(host);
                AppNavigator navigator = new AppNavigator(activity, host);
                navigatorRef.set(navigator);
                navigator.showProfile();
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            scenario.onActivity(activity -> {
                ScrollView scroll = findScrollView(activity.getWindow().getDecorView());
                assertNotNull("프로필 스크롤 뷰가 없습니다.", scroll);
                LinearLayout content = (LinearLayout) scroll.getChildAt(0);
                content.addView(new View(activity), new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        1200
                ));
                scrollRef.set(scroll);
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            scenario.onActivity(activity -> {
                scrollRef.get().scrollTo(0, 300);
                scrollBefore.set(scrollRef.get().getScrollY());
                View darkMode = findByDescription(
                        activity.getWindow().getDecorView(),
                        "다크모드"
                );
                assertNotNull("다크모드 토글이 없습니다.", darkMode);
                darkMode.performClick();
            });
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            scenario.onActivity(activity -> scrollAfter.set(
                    findScrollView(activity.getWindow().getDecorView()).getScrollY()
            ));
        }

        assertEquals("스크롤 기준점이 다크모드 전환 후 유지되어야 합니다.",
                scrollBefore.get(), scrollAfter.get());
    }

    private static ScrollView findScrollView(View view) {
        if (view instanceof ScrollView) {
            return (ScrollView) view;
        }
        if (!(view instanceof ViewGroup)) {
            return null;
        }
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            ScrollView found = findScrollView(group.getChildAt(i));
            if (found != null) {
                return found;
            }
        }
        return null;
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
