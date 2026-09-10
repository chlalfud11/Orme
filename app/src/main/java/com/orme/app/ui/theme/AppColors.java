package com.orme.app.ui.theme;

import android.graphics.Color;

/** Orme 브랜드 색상과 밝기별 팔레트. */
public final class AppColors {
    public static final int CREAM = Color.rgb(0xF2, 0xED, 0xD5);
    public static final int SHELL = Color.rgb(0xF7, 0xF7, 0xF7);
    public static final int ORANGE = Color.rgb(0xF2, 0x67, 0x16);
    public static final int GREEN = Color.rgb(0x08, 0x4A, 0x24);
    public static final int SEARCH_SURFACE = Color.rgb(0xF6, 0xF1, 0xDE);
    public static final int HEART_RED = Color.rgb(0xE9, 0x36, 0x2C);
    public static final int SOFT_BORDER = Color.rgb(0x9B, 0xA6, 0x8E);
    public static final int MUTED = Color.rgb(0x7B, 0x76, 0x5F);
    public static final int MAP_PROVINCE_FILL = Color.rgb(0xAC, 0xBC, 0xA0);
    public static final int MAP_PROVINCE_STROKE = Color.rgb(0x6E, 0x7D, 0x64);
    public static final int MAP_SIGUNGU_FILL = Color.rgb(0xDB, 0xDD, 0xC3);
    public static final int MAP_SIGUNGU_STROKE = Color.rgb(0x14, 0x53, 0x2B);
    public static final int MAP_PROVINCE_FILLED = Color.rgb(0x3E, 0x5A, 0x32);
    public static final int NAVER_GREEN = Color.rgb(0x03, 0xC7, 0x5A);
    public static final int KAKAO_YELLOW = Color.rgb(0xFE, 0xE5, 0x00);
    public static final int PAPER = Color.rgb(0xF6, 0xF1, 0xDE);

    private AppColors() {
    }

    public static Palette light() {
        return new Palette(
                CREAM,
                SEARCH_SURFACE,
                GREEN,
                ORANGE,
                MUTED,
                SOFT_BORDER,
                GREEN,
                GREEN,
                MAP_PROVINCE_FILL,
                MAP_PROVINCE_STROKE,
                MAP_PROVINCE_FILLED,
                MAP_SIGUNGU_FILL,
                MAP_SIGUNGU_STROKE,
                false
        );
    }

    public static Palette dark() {
        return new Palette(
                Color.rgb(0x14, 0x25, 0x1C),
                Color.rgb(0x20, 0x38, 0x2A),
                CREAM,
                ORANGE,
                Color.rgb(0xB5, 0xC0, 0xB7),
                Color.rgb(0x5F, 0x77, 0x67),
                GREEN,
                Color.rgb(0x8B, 0xC9, 0x9A),
                MAP_PROVINCE_FILL,
                MAP_PROVINCE_STROKE,
                MAP_PROVINCE_FILLED,
                MAP_SIGUNGU_FILL,
                MAP_SIGUNGU_STROKE,
                true
        );
    }

    public static int withAlpha(int color, int alpha) {
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color));
    }

    public static int lerp(int start, int end, float fraction) {
        float f = Math.max(0f, Math.min(1f, fraction));
        return Color.rgb(
                Math.round(Color.red(start) + (Color.red(end) - Color.red(start)) * f),
                Math.round(Color.green(start) + (Color.green(end) - Color.green(start)) * f),
                Math.round(Color.blue(start) + (Color.blue(end) - Color.blue(start)) * f)
        );
    }

    public static final class Palette {
        public final int background;
        public final int surface;
        public final int primary;
        public final int accent;
        public final int muted;
        public final int border;
        public final int button;
        public final int deleteAction;
        public final int mapProvinceFill;
        public final int mapProvinceStroke;
        public final int mapProvinceFilled;
        public final int mapSigunguFill;
        public final int mapSigunguStroke;
        public final boolean dark;

        private Palette(
                int background,
                int surface,
                int primary,
                int accent,
                int muted,
                int border,
                int button,
                int deleteAction,
                int mapProvinceFill,
                int mapProvinceStroke,
                int mapProvinceFilled,
                int mapSigunguFill,
                int mapSigunguStroke,
                boolean dark
        ) {
            this.background = background;
            this.surface = surface;
            this.primary = primary;
            this.accent = accent;
            this.muted = muted;
            this.border = border;
            this.button = button;
            this.deleteAction = deleteAction;
            this.mapProvinceFill = mapProvinceFill;
            this.mapProvinceStroke = mapProvinceStroke;
            this.mapProvinceFilled = mapProvinceFilled;
            this.mapSigunguFill = mapSigunguFill;
            this.mapSigunguStroke = mapSigunguStroke;
            this.dark = dark;
        }
    }
}
