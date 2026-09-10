package com.orme.app.ui.map;

import java.util.Map;

/** 전국 모자이크 배치와 도별 시군 수. */
public final class SigunguIndex {
    public final float viewBoxWidth;
    public final float viewBoxHeight;
    public final Map<String, Integer> provinceCounts;
    public final Map<String, SigItem> items;

    public SigunguIndex(
            float viewBoxWidth,
            float viewBoxHeight,
            Map<String, Integer> provinceCounts,
            Map<String, SigItem> items
    ) {
        this.viewBoxWidth = viewBoxWidth;
        this.viewBoxHeight = viewBoxHeight;
        this.provinceCounts = provinceCounts;
        this.items = items;
    }
}
