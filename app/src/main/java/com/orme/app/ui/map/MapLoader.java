package com.orme.app.ui.map;

import android.content.Context;
import android.graphics.Path;
import android.graphics.RectF;

import androidx.core.graphics.PathParser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/** 지도 JSON을 읽는 로더. */
public final class MapLoader {
    private MapLoader() {
    }

    public static MapData loadProvinces(Context context) throws Exception {
        return parse(context, "map/provinces.json");
    }

    public static SigunguIndex loadIndex(Context context) throws Exception {
        String text = readAsset(context, "map/sigungu_index.json");
        JSONObject object = new JSONObject(text);
        JSONArray viewBox = object.getJSONArray("viewBox");
        JSONObject countObject = object.getJSONObject("provinceCounts");
        Map<String, Integer> counts = new HashMap<>();
        Iterator<String> countKeys = countObject.keys();
        while (countKeys.hasNext()) {
            String key = countKeys.next();
            counts.put(key, countObject.getInt(key));
        }

        Map<String, SigItem> items = new HashMap<>();
        JSONObject itemObject = object.getJSONObject("items");
        Iterator<String> itemKeys = itemObject.keys();
        while (itemKeys.hasNext()) {
            String code = itemKeys.next();
            JSONObject entry = itemObject.getJSONObject(code);
            JSONArray bounds = entry.getJSONArray("b");
            String pathData = entry.optString("p", "");
            Path path = pathData.isEmpty() ? null : PathParser.createPathFromPathData(pathData);
            if (path != null) {
                path.setFillType(Path.FillType.EVEN_ODD);
            }
            items.put(code, new SigItem(
                    entry.getString("prov"),
                    new RectF(
                            (float) bounds.getDouble(0),
                            (float) bounds.getDouble(1),
                            (float) bounds.getDouble(2),
                            (float) bounds.getDouble(3)
                    ),
                    path
            ));
        }
        return new SigunguIndex(
                (float) viewBox.getDouble(0),
                (float) viewBox.getDouble(1),
                counts,
                items
        );
    }

    public static MapData loadSigungu(Context context, String provinceCode) {
        try {
            return parse(context, "map/sigungu/" + provinceCode + ".json");
        } catch (Exception ignored) {
            return null;
        }
    }

    public static boolean hasSigungu(Context context, String provinceCode) {
        try {
            context.getAssets().open("map/sigungu/" + provinceCode + ".json").close();
            return true;
        } catch (Exception ignored) {
            return false;
        }
    }

    private static MapData parse(Context context, String assetPath) throws Exception {
        JSONObject object = new JSONObject(readAsset(context, assetPath));
        JSONArray viewBox = object.getJSONArray("viewBox");
        JSONArray regionArray = object.getJSONArray("regions");
        List<MapRegion> regions = new ArrayList<>(regionArray.length());
        for (int i = 0; i < regionArray.length(); i++) {
            JSONObject regionObject = regionArray.getJSONObject(i);
            Path path = PathParser.createPathFromPathData(regionObject.getString("path"));
            if (path == null) {
                continue;
            }
            path.setFillType(Path.FillType.EVEN_ODD);
            RectF bounds = new RectF();
            path.computeBounds(bounds, true);
            regions.add(new MapRegion(
                    regionObject.getString("code"),
                    regionObject.getString("name"),
                    regionObject.optString("name_eng", ""),
                    path,
                    bounds,
                    regionObject.optBoolean("inset", false)
            ));
        }

        JSONArray insetViewBox = object.optJSONArray("insetViewBox");
        boolean hasInset = insetViewBox != null && insetViewBox.length() >= 2;
        JSONArray insetFrame = object.optJSONArray("insetFrame");
        RectF frame = insetFrame != null && insetFrame.length() >= 4
                ? new RectF(
                (float) insetFrame.getDouble(0),
                (float) insetFrame.getDouble(1),
                (float) insetFrame.getDouble(2),
                (float) insetFrame.getDouble(3)
        )
                : null;
        return new MapData(
                (float) viewBox.getDouble(0),
                (float) viewBox.getDouble(1),
                object.optString("province", null),
                object.optString("provinceCode", null),
                regions,
                hasInset ? (float) insetViewBox.getDouble(0) : 0f,
                hasInset ? (float) insetViewBox.getDouble(1) : 0f,
                hasInset,
                frame
        );
    }

    private static String readAsset(Context context, String assetPath) throws IOException {
        StringBuilder result = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                context.getAssets().open(assetPath),
                StandardCharsets.UTF_8
        ))) {
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }
        }
        return result.toString();
    }
}
