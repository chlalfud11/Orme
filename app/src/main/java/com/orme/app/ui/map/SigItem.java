package com.orme.app.ui.map;

import android.graphics.Path;
import android.graphics.RectF;

/** 전국 모자이크 프레임에서 한 시군의 위치. */
public final class SigItem {
    public final String province;
    public final RectF bounds;
    public final Path path;

    public SigItem(String province, RectF bounds, Path path) {
        this.province = province;
        this.bounds = bounds;
        this.path = path;
    }
}
