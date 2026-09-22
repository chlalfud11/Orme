package com.orme.app.ui.search;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.orme.app.MainActivity;
import com.orme.app.navigation.AppNavigator;
import com.orme.app.ui.components.ViewUtils;
import com.orme.app.ui.components.IconViews;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public final class LocationLayoutTest {
    @Test
    public void locationStatusTextHasRoomForFullGlyphs() {
        AtomicBoolean hasEnoughHeight = new AtomicBoolean();
        AtomicBoolean foundStatus = new AtomicBoolean();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                FrameLayout host = new FrameLayout(activity);
                TravelSearchScreen screen = new TravelSearchScreen(
                        activity,
                        new AppNavigator(activity, host)
                );
                host.addView(screen, new FrameLayout.LayoutParams(1080, 2400));
                host.measure(
                        View.MeasureSpec.makeMeasureSpec(1080, View.MeasureSpec.EXACTLY),
                        View.MeasureSpec.makeMeasureSpec(2400, View.MeasureSpec.EXACTLY)
                );
                host.layout(0, 0, 1080, 2400);
                TextView status = findLocationStatus(screen);
                assertNotNull("현재 위치 상태 문구를 찾지 못했습니다.", status);
                foundStatus.set(true);
                hasEnoughHeight.set(status.getHeight() >= ViewUtils.dp(activity, 30));
            });
        }
        assertTrue("현재 위치 상태 문구가 세로로 잘릴 수 있습니다.", foundStatus.get());
        assertTrue("현재 위치 상태 문구에 충분한 높이가 필요합니다.", hasEnoughHeight.get());
    }

    @Test
    public void locationMarkerCanRefreshLocation() {
        AtomicBoolean clickable = new AtomicBoolean();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                FrameLayout host = new FrameLayout(activity);
                TravelSearchScreen screen = new TravelSearchScreen(
                        activity,
                        new AppNavigator(activity, host)
                );
                host.addView(screen, new FrameLayout.LayoutParams(1080, 2400));
                IconViews.Pin pin = findPin(screen);
                assertNotNull("현재 위치 마커를 찾지 못했습니다.", pin);
                clickable.set(pin.isClickable());
            });
        }
        assertTrue("현재 위치 마커를 눌러 재감지할 수 있어야 합니다.", clickable.get());
    }

    private static IconViews.Pin findPin(View view) {
        if (view instanceof IconViews.Pin) {
            return (IconViews.Pin) view;
        }
        if (!(view instanceof ViewGroup)) {
            return null;
        }
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            IconViews.Pin result = findPin(group.getChildAt(i));
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private static TextView findLocationStatus(View view) {
        if (view instanceof TextView && "location-status".equals(view.getTag())) {
            return (TextView) view;
        }
        if (!(view instanceof ViewGroup)) {
            return null;
        }
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            TextView result = findLocationStatus(group.getChildAt(i));
            if (result != null) {
                return result;
            }
        }
        return null;
    }
}
