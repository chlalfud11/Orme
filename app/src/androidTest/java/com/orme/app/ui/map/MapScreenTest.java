package com.orme.app.ui.map;

import android.view.View;
import android.view.ViewGroup;
import android.graphics.PathMeasure;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;

import androidx.test.core.app.ActivityScenario;

import com.orme.app.MainActivity;
import com.orme.app.navigation.AppNavigator;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    @Test
    public void gyeongbukMapKeepsIslandFrameAtTheEast() {
        AtomicBoolean hasGeographicInset = new AtomicBoolean();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                MapData data = MapLoader.loadSigungu(activity, "37");
                assertNotNull("경상북도 지도 데이터를 읽지 못했습니다.", data);
                MapRegion ulleung = null;
                for (MapRegion region : data.regions) {
                    if ("37430".equals(region.code)) {
                        ulleung = region;
                        break;
                    }
                }
                assertNotNull("울릉군 경로가 없습니다.", ulleung);
                PathMeasure contours = new PathMeasure(ulleung.path, false);
                int contourCount = 0;
                do {
                    if (contours.getLength() > 0f) {
                        contourCount++;
                    }
                } while (contours.nextContour());
                hasGeographicInset.set(
                        data.insetFrame != null
                                && data.insetFrame.left > data.viewBoxWidth
                                && ulleung.inset
                                && contourCount >= 5
                );
            });
        }
        assertTrue("울릉도·독도는 본토 오른쪽 인셋 프레임에 있어야 합니다.", hasGeographicInset.get());
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
