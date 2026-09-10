package com.orme.app.ui.map;

/** viewBox를 화면에 레터박스 방식으로 맞추는 변환. */
public final class MapTransform {
    public final float scale;
    public final float offsetX;
    public final float offsetY;

    private MapTransform(float scale, float offsetX, float offsetY) {
        this.scale = scale;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public float screenToViewX(float screenX) {
        return (screenX - offsetX) / scale;
    }

    public float screenToViewY(float screenY) {
        return (screenY - offsetY) / scale;
    }

    public static MapTransform fit(
            float viewBoxWidth,
            float viewBoxHeight,
            float canvasWidth,
            float canvasHeight
    ) {
        if (viewBoxWidth <= 0f || viewBoxHeight <= 0f) {
            return new MapTransform(1f, 0f, 0f);
        }
        float scale = Math.min(canvasWidth / viewBoxWidth, canvasHeight / viewBoxHeight);
        return new MapTransform(
                scale,
                (canvasWidth - viewBoxWidth * scale) / 2f,
                (canvasHeight - viewBoxHeight * scale) / 2f
        );
    }
}
