package com.orme.app.ui.map;

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
        this.viewBoxWidth = viewBoxWidth;
        this.viewBoxHeight = viewBoxHeight;
        this.province = province;
        this.provinceCode = provinceCode;
        this.regions = regions;
        this.insetViewBoxWidth = insetViewBoxWidth;
        this.insetViewBoxHeight = insetViewBoxHeight;
        this.hasInsetViewBox = hasInsetViewBox;
    }
}
