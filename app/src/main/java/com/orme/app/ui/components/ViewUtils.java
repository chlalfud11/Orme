package com.orme.app.ui.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orme.app.ui.theme.AppTypography;

import java.io.InputStream;

/** Compose의 dp 기반 레이아웃 수치를 View에서도 동일하게 적용하는 작은 도우미. */
public final class ViewUtils {
    private ViewUtils() {
    }

    public static int dp(Context context, float value) {
        return Math.round(value * context.getResources().getDisplayMetrics().density);
    }

    public static int sp(Context context, float value) {
        return Math.round(value * context.getResources().getDisplayMetrics().scaledDensity);
    }

    public static FrameLayout.LayoutParams frame(int width, int height) {
        return new FrameLayout.LayoutParams(width, height);
    }

    public static LinearLayout.LayoutParams linear(int width, int height) {
        return new LinearLayout.LayoutParams(width, height);
    }

    public static LinearLayout.LayoutParams weight(float weight) {
        return new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, weight);
    }

    public static void margins(ViewGroup.MarginLayoutParams params, int left, int top, int right, int bottom) {
        params.setMargins(left, top, right, bottom);
    }

    public static GradientDrawable rounded(int color, float radiusDp, Context context) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(context, radiusDp));
        return drawable;
    }

    public static GradientDrawable roundedBorder(
            int color,
            int strokeColor,
            float strokeDp,
            float radiusDp,
            Context context
    ) {
        GradientDrawable drawable = rounded(color, radiusDp, context);
        drawable.setStroke(dp(context, strokeDp), strokeColor);
        return drawable;
    }

    public static TextView text(
            Context context,
            String value,
            int color,
            float sizeSp,
            int style,
            int gravity
    ) {
        TextView text = new TextView(context);
        text.setText(value);
        text.setTextColor(color);
        text.setTextSize(sizeSp);
        text.setGravity(gravity);
        text.setTypeface(AppTypography.sans(style));
        text.setIncludeFontPadding(true);
        return text;
    }

    public static TextView playfair(
            Context context,
            String value,
            int color,
            float sizeSp,
            int gravity
    ) {
        TextView text = text(context, value, color, sizeSp, android.graphics.Typeface.BOLD, gravity);
        text.setTypeface(AppTypography.playfair(context));
        return text;
    }

    public static TextView clickableText(
            Context context,
            String value,
            int color,
            float sizeSp,
            View.OnClickListener listener
    ) {
        TextView text = text(context, value, color, sizeSp, android.graphics.Typeface.NORMAL, Gravity.CENTER);
        text.setClickable(true);
        text.setOnClickListener(listener);
        return text;
    }

    public static View divider(Context context, int color, float heightDp) {
        View divider = new View(context);
        divider.setBackgroundColor(color);
        divider.setLayoutParams(linear(ViewGroup.LayoutParams.MATCH_PARENT, dp(context, heightDp)));
        return divider;
    }

    public static void clearBackground(View view) {
        view.setBackgroundColor(Color.TRANSPARENT);
        view.setElevation(0f);
    }

    public static void addGap(LinearLayout parent, Context context, int widthDp, boolean vertical) {
        View gap = new View(context);
        parent.addView(gap, vertical
                ? linear(LinearLayout.LayoutParams.MATCH_PARENT, dp(context, widthDp))
                : linear(dp(context, widthDp), 1));
    }

    public static Bitmap assetBitmap(Context context, String name) {
        try (InputStream input = context.getAssets().open("search/" + name + ".jpg")) {
            return BitmapFactory.decodeStream(input);
        } catch (Exception ignored) {
            return null;
        }
    }
}
