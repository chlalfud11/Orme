package com.orme.app.ui.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;

import com.orme.app.ui.theme.AppColors;

/** 리소스로 제공되지 않는 검색/위치/하트 아이콘을 동일한 선 굵기로 그린다. */
public final class IconViews {
    private IconViews() {
    }

    public static final class Magnifier extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public Magnifier(Context context) {
            super(context);
            paint.setColor(AppColors.GREEN);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(ViewUtils.dp(context, 3));
            paint.setStrokeCap(Paint.Cap.ROUND);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float radius = ViewUtils.dp(getContext(), 6);
            float cx = ViewUtils.dp(getContext(), 8);
            float cy = ViewUtils.dp(getContext(), 8);
            canvas.drawCircle(cx, cy, radius, paint);
            canvas.drawLine(ViewUtils.dp(getContext(), 12), ViewUtils.dp(getContext(), 12),
                    ViewUtils.dp(getContext(), 18), ViewUtils.dp(getContext(), 18), paint);
        }
    }

    public static final class Pin extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public Pin(Context context) {
            super(context);
            paint.setColor(AppColors.GREEN);
            paint.setStyle(Paint.Style.FILL);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float cx = getWidth() / 2f;
            Path path = new Path();
            path.moveTo(cx, getHeight());
            path.cubicTo(getWidth() * 0.14f, getHeight() * 0.58f,
                    getWidth() * 0.16f, getHeight() * 0.12f,
                    cx, getHeight() * 0.12f);
            path.cubicTo(getWidth() * 0.84f, getHeight() * 0.12f,
                    getWidth() * 0.86f, getHeight() * 0.58f,
                    cx, getHeight());
            canvas.drawPath(path, paint);
            paint.setColor(AppColors.CREAM);
            canvas.drawCircle(cx, getHeight() * 0.36f, getWidth() * 0.14f, paint);
            paint.setColor(AppColors.GREEN);
        }
    }

    public static final class Chevron extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public Chevron(Context context) {
            super(context);
            paint.setColor(AppColors.GREEN);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(ViewUtils.dp(context, 2));
            paint.setStrokeCap(Paint.Cap.ROUND);
            paint.setStrokeJoin(Paint.Join.ROUND);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Path path = new Path();
            float centerY = getHeight() / 2f;
            path.moveTo(ViewUtils.dp(getContext(), 2), centerY - ViewUtils.dp(getContext(), 2.5f));
            path.lineTo(ViewUtils.dp(getContext(), 7), centerY + ViewUtils.dp(getContext(), 2.5f));
            path.lineTo(ViewUtils.dp(getContext(), 12), centerY - ViewUtils.dp(getContext(), 2.5f));
            canvas.drawPath(path, paint);
        }
    }

    public static final class Heart extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private boolean selected;
        private final int strokeColor;

        public Heart(Context context, boolean selected) {
            super(context);
            this.selected = selected;
            this.strokeColor = Color.WHITE;
            setClickable(true);
            updateContentDescription();
        }

        public void setSelectedState(boolean selected) {
            this.selected = selected;
            updateContentDescription();
            invalidate();
        }

        public boolean isSelectedState() {
            return selected;
        }

        private void updateContentDescription() {
            setContentDescription(selected ? "즐겨찾기 해제" : "즐겨찾기 추가");
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            float w = getWidth();
            float h = getHeight();
            Path heart = new Path();
            heart.moveTo(w * 0.50f, h * 0.72f);
            heart.cubicTo(w * 0.42f, h * 0.62f, w * 0.24f, h * 0.48f,
                    w * 0.24f, h * 0.32f);
            heart.cubicTo(w * 0.24f, h * 0.18f, w * 0.42f, h * 0.15f,
                    w * 0.50f, h * 0.28f);
            heart.cubicTo(w * 0.58f, h * 0.15f, w * 0.76f, h * 0.18f,
                    w * 0.76f, h * 0.32f);
            heart.cubicTo(w * 0.76f, h * 0.48f, w * 0.58f, h * 0.62f,
                    w * 0.50f, h * 0.72f);
            heart.close();
            paint.setAntiAlias(true);
            paint.setColor(selected ? AppColors.HEART_RED : strokeColor);
            paint.setStyle(selected ? Paint.Style.FILL : Paint.Style.STROKE);
            paint.setStrokeWidth(ViewUtils.dp(getContext(), 1.6f));
            canvas.drawPath(heart, paint);
            if (selected) {
                paint.setColor(Color.WHITE);
                paint.setStyle(Paint.Style.STROKE);
                paint.setStrokeWidth(ViewUtils.dp(getContext(), 1.4f));
                canvas.drawPath(heart, paint);
            }
        }
    }

    public static final class SearchHeartButton extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private boolean selected = true;

        public SearchHeartButton(Context context) {
            super(context);
            setClickable(true);
            setContentDescription("즐겨찾기 목록");
        }

        public void setSelectedState(boolean selected) {
            this.selected = selected;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float stroke = ViewUtils.dp(getContext(), 1.5f);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(stroke);
            paint.setColor(AppColors.HEART_RED);
            float inset = stroke + ViewUtils.dp(getContext(), 2);
            canvas.drawRoundRect(
                    inset,
                    inset,
                    getWidth() - inset,
                    getHeight() - inset,
                    getHeight() / 2f,
                    getHeight() / 2f,
                    paint
            );
            Path heart = new Path();
            float cx = getWidth() / 2f;
            float w = ViewUtils.dp(getContext(), 15);
            float cy = getHeight() / 2f + w * 0.09f;
            heart.moveTo(cx, cy + w * 0.54f);
            heart.cubicTo(cx - w * 0.30f, cy + w * 0.18f, cx - w * 0.62f, cy - w * 0.05f,
                    cx - w * 0.62f, cy - w * 0.34f);
            heart.cubicTo(cx - w * 0.62f, cy - w * 0.65f, cx - w * 0.22f, cy - w * 0.72f,
                    cx, cy - w * 0.44f);
            heart.cubicTo(cx + w * 0.22f, cy - w * 0.72f, cx + w * 0.62f, cy - w * 0.65f,
                    cx + w * 0.62f, cy - w * 0.34f);
            heart.cubicTo(cx + w * 0.62f, cy - w * 0.05f, cx + w * 0.30f, cy + w * 0.18f,
                    cx, cy + w * 0.54f);
            heart.close();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(AppColors.HEART_RED);
            canvas.drawPath(heart, paint);
        }
    }

    public static final class DownArrow extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public DownArrow(Context context) {
            super(context);
            paint.setColor(AppColors.CREAM);
            paint.setStyle(Paint.Style.FILL);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Path path = new Path();
            path.moveTo(0, 0);
            path.lineTo(getWidth(), 0);
            path.lineTo(getWidth() / 2f, getHeight());
            path.close();
            canvas.drawPath(path, paint);
        }
    }

    public static final class GradientOverlay extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        public GradientOverlay(Context context) {
            super(context);
            setWillNotDraw(false);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            paint.setShader(new android.graphics.LinearGradient(
                    0,
                    getHeight() * 0.45f,
                    0,
                    getHeight(),
                    0x00000000,
                    0xD9000000,
                    android.graphics.Shader.TileMode.CLAMP
            ));
            canvas.drawRect(new RectF(0, 0, getWidth(), getHeight()), paint);
            paint.setShader(null);
        }
    }
}
