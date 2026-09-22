package com.orme.app.ui.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.orme.app.MainActivity;
import com.orme.app.navigation.AppNavigator;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public final class TravelRecommendationCacheTest {
    private static final String PREFERENCES = "orme_recommendation_cache";
    private static final String SNAPSHOT_KEY = "last";

    @Test
    public void restoresSeededRecommendationWithoutShowingStaleCity() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        JSONObject snapshot = new JSONObject()
                .put("version", 1)
                .put("city", "부산광역시")
                .put("recommended", new JSONArray().put("21"))
                .put("nearby", new JSONArray().put("11"));
        SharedPreferences preferences = context.getSharedPreferences(
                PREFERENCES,
                Context.MODE_PRIVATE
        );
        preferences.edit().putString(SNAPSHOT_KEY, snapshot.toString()).commit();

        AtomicBoolean restored = new AtomicBoolean();
        AtomicBoolean showsStaleCity = new AtomicBoolean();
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                FrameLayout host = new FrameLayout(activity);
                TravelSearchScreen screen = new TravelSearchScreen(
                        activity,
                        new AppNavigator(activity, host)
                );
                restored.set(containsText(screen, "부산광역시"));
                showsStaleCity.set(locationStatusIs(screen, "부산광역시"));
            });
        } finally {
            preferences.edit().remove(SNAPSHOT_KEY).commit();
        }

        assertTrue("직전 추천 정보가 캐시에서 복원되어야 합니다.", restored.get());
        assertFalse("캐시된 이전 지역을 현재 위치처럼 표시하면 안 됩니다.", showsStaleCity.get());
    }

    private static boolean containsText(View view, String expected) {
        if (view instanceof TextView
                && expected.contentEquals(((TextView) view).getText())) {
            return true;
        }
        if (!(view instanceof ViewGroup)) {
            return false;
        }
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            if (containsText(group.getChildAt(i), expected)) {
                return true;
            }
        }
        return false;
    }

    private static boolean locationStatusIs(View view, String expected) {
        if (view instanceof TextView
                && "location-status".equals(view.getTag())
                && expected.contentEquals(((TextView) view).getText())) {
            return true;
        }
        if (!(view instanceof ViewGroup)) {
            return false;
        }
        ViewGroup group = (ViewGroup) view;
        for (int i = 0; i < group.getChildCount(); i++) {
            if (locationStatusIs(group.getChildAt(i), expected)) {
                return true;
            }
        }
        return false;
    }
}
