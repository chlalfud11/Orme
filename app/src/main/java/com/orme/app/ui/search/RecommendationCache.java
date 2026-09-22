package com.orme.app.ui.search;

import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** 마지막 추천 순서를 앱 재실행 뒤에도 복원하는 작은 캐시. */
public final class RecommendationCache {
    public static final String PREFERENCES = "orme_recommendation_cache";
    public static final String SNAPSHOT_KEY = "last";

    private static final int VERSION = 1;

    private RecommendationCache() {
    }

    public static Snapshot read(SharedPreferences preferences) {
        String raw = preferences.getString(SNAPSHOT_KEY, null);
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            JSONObject object = new JSONObject(raw);
            if (object.optInt("version", -1) != VERSION) {
                return null;
            }
            List<String> recommended = readCodes(object.optJSONArray("recommended"));
            List<String> nearby = readCodes(object.optJSONArray("nearby"));
            if (recommended == null || nearby == null
                    || (recommended.isEmpty() && nearby.isEmpty())) {
                return null;
            }

            Map<String, String> distanceLabels = new HashMap<>();
            JSONObject distances = object.optJSONObject("distanceLabels");
            if (distances != null) {
                Iterator<String> keys = distances.keys();
                while (keys.hasNext()) {
                    String key = keys.next();
                    distanceLabels.put(key, distances.optString(key, ""));
                }
            }
            return new Snapshot(
                    object.optString("city", ""),
                    recommended,
                    nearby,
                    distanceLabels
            );
        } catch (Exception ignored) {
            return null;
        }
    }

    public static void write(
            SharedPreferences preferences,
            String city,
            List<RegionCatalog.Entry> recommended,
            List<RegionCatalog.Entry> nearby,
            Map<String, String> distanceLabels
    ) {
        try {
            JSONObject object = new JSONObject();
            object.put("version", VERSION);
            object.put("city", city == null ? "" : city);
            object.put("recommended", codes(recommended));
            object.put("nearby", codes(nearby));

            JSONObject distances = new JSONObject();
            for (Map.Entry<String, String> entry : distanceLabels.entrySet()) {
                distances.put(entry.getKey(), entry.getValue());
            }
            object.put("distanceLabels", distances);
            preferences.edit().putString(SNAPSHOT_KEY, object.toString()).apply();
        } catch (Exception ignored) {
            // 추천 화면은 캐시 없이도 현재 위치 결과를 계속 보여준다.
        }
    }

    private static JSONArray codes(List<RegionCatalog.Entry> entries) {
        JSONArray result = new JSONArray();
        for (RegionCatalog.Entry entry : entries) {
            result.put(entry.code);
        }
        return result;
    }

    private static List<String> readCodes(JSONArray values) {
        if (values == null) {
            return null;
        }
        List<String> result = new ArrayList<>(values.length());
        for (int i = 0; i < values.length(); i++) {
            String code = values.optString(i, "");
            if (code.isEmpty()) {
                return null;
            }
            result.add(code);
        }
        return result;
    }

    public static final class Snapshot {
        public final String city;
        public final List<String> recommendedCodes;
        public final List<String> nearbyCodes;
        public final Map<String, String> distanceLabels;

        Snapshot(
                String city,
                List<String> recommendedCodes,
                List<String> nearbyCodes,
                Map<String, String> distanceLabels
        ) {
            this.city = city;
            this.recommendedCodes = Collections.unmodifiableList(
                    new ArrayList<>(recommendedCodes)
            );
            this.nearbyCodes = Collections.unmodifiableList(
                    new ArrayList<>(nearbyCodes)
            );
            this.distanceLabels = Collections.unmodifiableMap(
                    new HashMap<>(distanceLabels)
            );
        }
    }
}
