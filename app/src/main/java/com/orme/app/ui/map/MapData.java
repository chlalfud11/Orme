package com.orme.app.ui.map;

import android.graphics.RectF;

import java.util.List;

/** 한 화면의 지도 데이터. */
public final class MapData {
    public final float viewBoxWidth;
    public final float viewBoxHeight;
    public final String province;
    public final String provinceCode;
    public final List<MapRegion> regions;
    public final float insetViewBoxWidth;
    public final float insetViewBoxHeight;
    public final boolean hasInsetViewBox;
    public final RectF insetFrame;

    public MapData(
            float viewBoxWidth,
            float viewBoxHeight,
            String province,
            String provinceCode,
            List<MapRegion> regions,
            float insetViewBoxWidth,
            float insetViewBoxHeight,
            boolean hasInsetViewBox
    ) {
        this(
                viewBoxWidth,
                viewBoxHeight,
                province,
                provinceCode,
                regions,
                insetViewBoxWidth,
                insetViewBoxHeight,
                hasInsetViewBox,
                null
        );
    }

    public MapData(
            float viewBoxWidth,
            float viewBoxHeight,
            String province,
            String provinceCode,
            List<MapRegion> regions,
            float insetViewBoxWidth,
            float insetViewBoxHeight,
            boolean hasInsetViewBox,
            RectF insetFrame
    ) {
        this.viewBoxWidth = viewBoxWidth;
        this.viewBoxHeight = viewBoxHeight;
        this.province = province;
        this.provinceCode = provinceCode;
        this.regions = regions;
        this.insetViewBoxWidth = insetViewBoxWidth;
        this.insetViewBoxHeight = insetViewBoxHeight;
        this.hasInsetViewBox = hasInsetViewBox;
        this.insetFrame = insetFrame;
    }
}
