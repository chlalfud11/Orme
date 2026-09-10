package com.orme.app.ui.map;

import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Region;

/** 지도 한 지역의 경로와 탭 판정 영역. */
public final class MapRegion {
    public final String code;
    public final String name;
    public final String nameEng;
    public final Path path;
    public final RectF bounds;
    public final boolean inset;
    private final Region hitRegion;

    public MapRegion(String code, String name, String nameEng, Path path, RectF bounds, boolean inset) {
        this.code = code;
        this.name = name;
        this.nameEng = nameEng;
        this.path = path;
        this.bounds = bounds;
        this.inset = inset;

        RectF pathBounds = new RectF();
        path.computeBounds(pathBounds, true);
        Region clip = new Region(
                (int) Math.floor(pathBounds.left),
                (int) Math.floor(pathBounds.top),
                (int) Math.ceil(pathBounds.right),
                (int) Math.ceil(pathBounds.bottom)
        );
        hitRegion = new Region();
        hitRegion.setPath(path, clip);
    }

    public boolean contains(float x, float y) {
        return hitRegion.contains((int) x, (int) y);
    }
}
