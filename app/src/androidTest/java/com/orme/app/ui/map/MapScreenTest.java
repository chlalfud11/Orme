package com.orme.app.ui.map;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;

import androidx.test.core.app.ActivityScenario;

import com.orme.app.MainActivity;
import com.orme.app.navigation.AppNavigator;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;

/** 지도에는 기록 카드 레일을 렌더링하지 않는다. */
public final class MapScreenTest {
    @Test
    public void mapDoesNotContainRecordRail() {
        AtomicBoolean containsRecordRail = new AtomicBoolean();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                FrameLayout host = new FrameLayout(activity);
                MapScreen map = new MapScreen(
                        activity,
                        new AppNavigator(activity, host)
                );
                containsRecordRail.set(containsHorizontalScrollView(map));
            });
        }
        assertFalse("지도에 기록 카드 가로 레일이 남아 있습니다.", containsRecordRail.get());
    }

    private static boolean containsHorizontalScrollView(View view) {
        if (view instanceof HorizontalScrollView) {
            return true;
        }
        if (!(view instanceof ViewGroup)) {
            return false;
        }
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            if (containsHorizontalScrollView(group.getChildAt(i))) {
                return true;
            }
        }
        return false;
    }
}
