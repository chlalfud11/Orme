package com.orme.app.ui.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.orme.app.ui.components.ViewUtils;
import com.orme.app.ui.theme.AppColors;

/** 지도 지역 안에서 사진을 드래그/핀치로 배치하는 편집기. */
public final class PhotoPositionEditor extends FrameLayout {
    public interface OnSave {
        void onSave(float scale, float offsetX, float offsetY);
    }

    private final EditorView editor;

    public PhotoPositionEditor(
            Context context,
            Bitmap bitmap,
            MapRegion region,
            OnSave onSave,
            View.OnClickListener onCancel
    ) {
        super(context);
        setBackgroundColor(AppColors.CREAM);
        editor = new EditorView(context, bitmap, region);
        addView(editor, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        ));

        TextView cancel = ViewUtils.text(context, "취소", AppColors.GREEN, 16,
                android.graphics.Typeface.BOLD, Gravity.CENTER);
        cancel.setClickable(true);
        cancel.setContentDescription("사진 편집 취소");
        cancel.setOnClickListener(onCancel);
        FrameLayout.LayoutParams cancelParams = new FrameLayout.LayoutParams(
                ViewUtils.dp(context, 70),
                ViewUtils.dp(context, 56),
                Gravity.BOTTOM | Gravity.LEFT
        );
        cancelParams.leftMargin = ViewUtils.dp(context, 22);
        cancelParams.bottomMargin = ViewUtils.dp(context, 22);
        addView(cancel, cancelParams);

        TextView save = ViewUtils.text(context, "저장", AppColors.ORANGE, 16,
                android.graphics.Typeface.BOLD, Gravity.CENTER);
        save.setClickable(true);
        save.setContentDescription("사진 편집 저장");
        save.setOnClickListener(v -> onSave.onSave(
                editor.scale,
                editor.logicalOffsetX(),
                editor.logicalOffsetY()
        ));
        FrameLayout.LayoutParams saveParams = new FrameLayout.LayoutParams(
                ViewUtils.dp(context, 70),
                ViewUtils.dp(context, 56),
                Gravity.BOTTOM | Gravity.RIGHT
        );
        saveParams.rightMargin = ViewUtils.dp(context, 22);
        saveParams.bottomMargin = ViewUtils.dp(context, 22);
        addView(save, saveParams);
    }

    private static final class EditorView extends View {
        private final Bitmap bitmap;
        private final MapRegion region;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private final Paint regionPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final ScaleGestureDetector scaleDetector;
        private final RectF previewRect = new RectF();
        private final int outputWidth;
        private final int outputHeight;
        private float scale = 1f;
        private float offsetX;
        private float offsetY;
        private float lastX;
        private float lastY;
        private float previewScale;

        EditorView(Context context, Bitmap bitmap, MapRegion region) {
            super(context);
            this.bitmap = bitmap;
            this.region = region;
            float outputScale = 1200f / Math.max(region.bounds.width(), region.bounds.height());
            outputWidth = Math.max(1, Math.round(region.bounds.width() * outputScale));
            outputHeight = Math.max(1, Math.round(region.bounds.height() * outputScale));
            scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector detector) {
                    scale = Math.max(0.5f, Math.min(4f, scale * detector.getScaleFactor()));
                    clampOffsets();
                    invalidate();
                    return true;
                }
            });
            regionPaint.setStyle(Paint.Style.STROKE);
            regionPaint.setStrokeWidth(ViewUtils.dp(context, 2));
            regionPaint.setColor(AppColors.GREEN);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            setContentDescription("사진 위치 편집");
        }

        @Override
        protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
            updatePreviewRect();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(AppColors.CREAM);
            updatePreviewRect();
            float base = Math.max(
                    previewRect.width() / Math.max(1, bitmap.getWidth()),
                    previewRect.height() / Math.max(1, bitmap.getHeight())
            );
            float drawScale = base * scale;
            float width = bitmap.getWidth() * drawScale;
            float height = bitmap.getHeight() * drawScale;
            float left = previewRect.centerX() - width / 2f + offsetX;
            float top = previewRect.centerY() - height / 2f + offsetY;
            canvas.save();
            canvas.clipRect(previewRect);
            canvas.drawBitmap(bitmap, null, new RectF(left, top, left + width, top + height), paint);
            canvas.restore();

            Path path = new Path(region.path);
            Matrix pathMatrix = new Matrix();
            float outputScale = 1200f / Math.max(region.bounds.width(), region.bounds.height());
            pathMatrix.setScale(outputScale, outputScale);
            pathMatrix.preTranslate(-region.bounds.left, -region.bounds.top);
            path.transform(pathMatrix);
            canvas.save();
            canvas.translate(previewRect.left, previewRect.top);
            canvas.scale(previewScale, previewScale);
            canvas.drawPath(path, regionPaint);
            canvas.restore();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            scaleDetector.onTouchEvent(event);
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                lastX = event.getX();
                lastY = event.getY();
                return true;
            }
            if (event.getActionMasked() == MotionEvent.ACTION_MOVE && event.getPointerCount() == 1) {
                offsetX += event.getX() - lastX;
                offsetY += event.getY() - lastY;
                clampOffsets();
                lastX = event.getX();
                lastY = event.getY();
                invalidate();
                return true;
            }
            return true;
        }

        float logicalOffsetX() {
            return previewScale == 0f ? offsetX : offsetX / previewScale;
        }

        float logicalOffsetY() {
            return previewScale == 0f ? offsetY : offsetY / previewScale;
        }

        private void updatePreviewRect() {
            if (getWidth() == 0 || getHeight() == 0) {
                return;
            }
            float horizontalPadding = ViewUtils.dp(getContext(), 22);
            float topPadding = ViewUtils.dp(getContext(), 72);
            float bottomPadding = ViewUtils.dp(getContext(), 150);
            float availableWidth = Math.max(1f, getWidth() - horizontalPadding * 2f);
            float availableHeight = Math.max(1f, getHeight() - topPadding - bottomPadding);
            previewScale = Math.min(
                    availableWidth / outputWidth,
                    availableHeight / outputHeight
            );
            float width = outputWidth * previewScale;
            float height = outputHeight * previewScale;
            previewRect.set(
                    (getWidth() - width) / 2f,
                    topPadding + (availableHeight - height) / 2f,
                    (getWidth() + width) / 2f,
                    topPadding + (availableHeight + height) / 2f
            );
        }

        private void clampOffsets() {
            if (previewRect.isEmpty()) {
                return;
            }
            float base = Math.max(
                    previewRect.width() / Math.max(1, bitmap.getWidth()),
                    previewRect.height() / Math.max(1, bitmap.getHeight())
            );
            float drawWidth = bitmap.getWidth() * base * scale;
            float drawHeight = bitmap.getHeight() * base * scale;
            float centeredLeft = previewRect.centerX() - drawWidth / 2f;
            float centeredTop = previewRect.centerY() - drawHeight / 2f;
            float minOffsetX = previewRect.right - (centeredLeft + drawWidth);
            float maxOffsetX = previewRect.left - centeredLeft;
            float minOffsetY = previewRect.bottom - (centeredTop + drawHeight);
            float maxOffsetY = previewRect.top - centeredTop;
            offsetX = Math.max(minOffsetX, Math.min(maxOffsetX, offsetX));
            offsetY = Math.max(minOffsetY, Math.min(maxOffsetY, offsetY));
        }
    }
}
