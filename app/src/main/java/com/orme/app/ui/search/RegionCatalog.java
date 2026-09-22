package com.orme.app.ui.search;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/** 지도와 추천 화면이 함께 사용하는 전국 지역 카탈로그. */
public final class RegionCatalog {
    private RegionCatalog() {
    }

    public static List<Entry> load(Context context) {
        try {
            StringBuilder json = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                    context.getAssets().open("search/regions.json"),
                    StandardCharsets.UTF_8
            ))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }
            }

            JSONArray entries = new JSONArray(json.toString());
            List<Entry> result = new ArrayList<>(entries.length());
            for (int i = 0; i < entries.length(); i++) {
                JSONObject entry = entries.getJSONObject(i);
                result.add(new Entry(
                        entry.getString("code"),
                        entry.getString("provinceCode"),
                        entry.getString("name"),
                        entry.getString("name_eng"),
                        entry.getDouble("latitude"),
                        entry.getDouble("longitude"),
                        entry.getString("image")
                ));
            }
            return result;
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    public static List<Entry> filter(List<Entry> entries, String query) {
        String normalized = normalize(query);
        if (normalized.isEmpty()) {
            return new ArrayList<>(entries);
        }
        List<Entry> result = new ArrayList<>();
        for (Entry entry : entries) {
            if (normalize(entry.name).contains(normalized)
                    || normalize(entry.nameEng).contains(normalized)) {
                result.add(entry);
            }
        }
        return result;
    }

    static String normalize(String value) {
        return value == null
                ? ""
                : value.trim().toLowerCase(Locale.ROOT);
    }

    public static final class Entry {
        public final String code;
        public final String provinceCode;
        public final String name;
        public final String nameEng;
        public final double latitude;
        public final double longitude;
        public final String image;

        public Entry(
                String code,
                String provinceCode,
                String name,
                String nameEng,
                double latitude,
                double longitude,
                String image
        ) {
            this.code = code;
            this.provinceCode = provinceCode;
            this.name = name;
            this.nameEng = nameEng;
            this.latitude = latitude;
            this.longitude = longitude;
            this.image = image;
        }
    }
}
