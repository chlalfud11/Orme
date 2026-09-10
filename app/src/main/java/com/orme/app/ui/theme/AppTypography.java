package com.orme.app.ui.theme;

import android.content.Context;
import android.graphics.Typeface;

import androidx.core.content.res.ResourcesCompat;

import com.orme.app.R;

/** 앱에서 반복 사용하는 폰트 설정. */
public final class AppTypography {
    private AppTypography() {
    }

    public static Typeface playfair(Context context) {
        Typeface typeface = ResourcesCompat.getFont(context, R.font.playfair_display);
        return typeface == null
                ? Typeface.create(Typeface.SERIF, Typeface.BOLD)
                : Typeface.create(typeface, Typeface.BOLD);
    }

    public static Typeface serifBold() {
        return Typeface.create(Typeface.SERIF, Typeface.BOLD);
    }

    public static Typeface sans(int style) {
        return Typeface.create(Typeface.SANS_SERIF, style);
    }
}
