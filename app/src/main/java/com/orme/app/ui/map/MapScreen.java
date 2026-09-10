package com.orme.app.ui.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.util.TypedValue;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.orme.app.R;
import com.orme.app.navigation.AppNavigator;
import com.orme.app.ui.components.ViewUtils;
import com.orme.app.ui.diary.RegionDiaryFlow;
import com.orme.app.ui.theme.AppColors;
import com.orme.app.ui.theme.AppTheme;
import com.orme.app.ui.theme.AppTypography;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** 전국 지도와 시군 상세 지도를 View/Canvas로 렌더링한다. */
public final class MapScreen extends FrameLayout {
    private enum Level {
        PROVINCES,
        SIGUNGU
    }

    private final AppNavigator navigator;
    private final AppColors.Palette colors;
    private final ExecutorService loader = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final MapCanvasView mapView;
    private final TextView regionButton;
    private final View bottomBar;
    private Level level = Level.PROVINCES;
    private String provinceCode;
    private RegionDiaryFlow diary;
    private PhotoPositionEditor photoEditor;
    private PopupWindow regionMenu;

    public MapScreen(Context context, AppNavigator navigator) {
        super(context);
        this.navigator = navigator;
        this.colors = navigator.colors();
        setBackgroundColor(colors.background);
        setClipChildren(false);

        mapView = new MapCanvasView(context);
        FrameLayout.LayoutParams mapParams = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
        mapParams.topMargin = ViewUtils.dp(context, 50);
        mapParams.bottomMargin = ViewUtils.dp(context, 84);
        addView(mapView, mapParams);

        regionButton = ViewUtils.text(context, "Region  ▼", AppColors.CREAM, 15,
                android.graphics.Typeface.BOLD, Gravity.CENTER);
        regionButton.setTypeface(AppTypography.sans(android.graphics.Typeface.BOLD));
        regionButton.setMaxLines(1);
        regionButton.setAutoSizeTextTypeUniformWithConfiguration(
                10,
                15,
                1,
                TypedValue.COMPLEX_UNIT_SP
        );
        regionButton.setPadding(ViewUtils.dp(context, 18), 0, ViewUtils.dp(context, 18), 0);
        regionButton.setBackground(regionBackground(context));
        regionButton.setElevation(ViewUtils.dp(context, 6));
        regionButton.setContentDescription("Region");
        regionButton.setOnClickListener(v -> {
            if (!mapView.isLoading()) {
                showRegionMenu();
            }
        });
        FrameLayout.LayoutParams regionParams = new FrameLayout.LayoutParams(
                ViewUtils.dp(context, 116),
                ViewUtils.dp(context, 42),
                Gravity.TOP | Gravity.RIGHT
        );
        regionParams.topMargin = ViewUtils.dp(context, 66);
        regionParams.rightMargin = ViewUtils.dp(context, 20);
        addView(regionButton, regionParams);

        ImageButton back = iconButton(context, R.drawable.ic_back, "뒤로");
        back.setVisibility(INVISIBLE);
        back.setOnClickListener(v -> showProvinces());
        FrameLayout.LayoutParams backParams = new FrameLayout.LayoutParams(
                ViewUtils.dp(context, 34),
                ViewUtils.dp(context, 34),
                Gravity.TOP | Gravity.LEFT
        );
        backParams.leftMargin = ViewUtils.dp(context, 20);
        backParams.topMargin = ViewUtils.dp(context, 104);
        addView(back, backParams);
        mapView.setBackButton(back);

        bottomBar = navigator.bottomBar();
        FrameLayout.LayoutParams bottomParams = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 84),
                Gravity.BOTTOM
        );
        addView(bottomBar, bottomParams);
        mapView.setOnRegionClick(this::onRegionClick);
        loadProvinces();
    }

    private GradientDrawable regionBackground(Context context) {
        GradientDrawable drawable = ViewUtils.rounded(colors.button, 14, context);
        drawable.setStroke(0, colors.button);
        return drawable;
    }

    private ImageButton iconButton(Context context, int drawable, String description) {
        ImageButton button = new ImageButton(context);
        button.setImageResource(drawable);
        button.setColorFilter(colors.primary);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setPadding(0, 0, 0, 0);
        button.setContentDescription(description);
        return button;
    }

    private void loadProvinces() {
        mapView.setLoading(true);
        loader.execute(() -> {
            try {
                MapData data = MapLoader.loadProvinces(getContext());
                SigunguIndex index = MapLoader.loadIndex(getContext());
                Map<String, Bitmap> photos = new HashMap<>();
                Map<String, Integer> filled = new HashMap<>();
                for (Map.Entry<String, SigItem> entry : index.items.entrySet()) {
                    Bitmap bitmap = PhotoStore.loadShaped(getContext(), entry.getKey());
                    if (bitmap != null) {
                        photos.put(entry.getKey(), bitmap);
                        String province = entry.getValue().province;
                        filled.put(province, filled.containsKey(province) ? filled.get(province) + 1 : 1);
                    }
                }
                for (MapRegion region : data.regions) {
                    if (!MapLoader.hasSigungu(getContext(), region.code)) {
                        Bitmap bitmap = PhotoStore.loadShaped(getContext(), region.code);
                        if (bitmap != null) {
                            photos.put(region.code, bitmap);
                        }
                    }
                }
                Map<String, Float> completion = new HashMap<>();
                for (Map.Entry<String, Integer> entry : index.provinceCounts.entrySet()) {
                    int count = filled.containsKey(entry.getKey()) ? filled.get(entry.getKey()) : 0;
                    completion.put(entry.getKey(), entry.getValue() == 0
                            ? 0f
                            : (float) count / entry.getValue());
                }
                mainHandler.post(() -> {
                    mapView.setFrame(
                            Level.PROVINCES,
                            data,
                            null,
                            index,
                            photos,
                            completion
                    );
                });
            } catch (Exception error) {
                mainHandler.post(() -> mapView.setLoading(false));
            }
        });
    }

    private void loadSigungu(String code) {
        mapView.setLoading(true);
        loader.execute(() -> {
            MapData data = MapLoader.loadSigungu(getContext(), code);
            if (data == null) {
                mainHandler.post(() -> mapView.setLoading(false));
                return;
            }
            Map<String, Bitmap> photos = new HashMap<>();
            for (MapRegion region : data.regions) {
                Bitmap bitmap = PhotoStore.loadShaped(getContext(), region.code);
                if (bitmap != null) {
                    photos.put(region.code, bitmap);
                }
            }
            mainHandler.post(() -> mapView.setFrame(
                    Level.SIGUNGU,
                    data,
                    code,
                    null,
                    photos,
                    new HashMap<>()
            ));
        });
    }

    private void showRegionMenu() {
        if (mapView.data() == null) {
            return;
        }
        Context context = getContext();
        ScrollView scroll = new ScrollView(context);
        scroll.setBackground(ViewUtils.rounded(colors.background, 8, context));
        LinearLayout list = new LinearLayout(context);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(0, ViewUtils.dp(context, 8), 0, 0);
        scroll.addView(list, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        for (MapRegion region : mapView.data().regions) {
            TextView item = ViewUtils.text(context, region.name, colors.primary, 16,
                    android.graphics.Typeface.NORMAL, Gravity.LEFT | Gravity.CENTER_VERTICAL);
            item.setPadding(ViewUtils.dp(context, 12), 0, ViewUtils.dp(context, 8), 0);
            item.setClickable(true);
            item.setOnClickListener(v -> {
                if (regionMenu != null) {
                    regionMenu.dismiss();
                }
                mapView.post(() -> {
                    if (MapLoader.hasSigungu(context, region.code)) {
                        showSigungu(region.code, region.name);
                    } else {
                        openDiary(region);
                    }
                });
            });
            list.addView(item, ViewUtils.linear(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewUtils.dp(context, 50)
            ));
        }
        regionMenu = new PopupWindow(
                scroll,
                ViewUtils.dp(context, 114),
                Math.min(ViewUtils.dp(context, 420), ViewUtils.dp(context, 50) * mapView.data().regions.size()),
                true
        );
        regionMenu.setBackgroundDrawable(ViewUtils.rounded(colors.background, 8, context));
        regionMenu.setElevation(ViewUtils.dp(context, 8));
        regionMenu.setOutsideTouchable(true);
        regionMenu.showAsDropDown(regionButton, 0, 0);
    }

    private void showSigungu(String code, String name) {
        level = Level.SIGUNGU;
        provinceCode = code;
        regionButton.setText(name + "  ▼");
        mapView.setTouchEnabled(false);
        mapView.clearFrame();
        mapView.setLevel(Level.SIGUNGU);
        mapView.setCameraVisible(true);
        loadSigungu(code);
    }

    private void showProvinces() {
        if (diary != null) {
            closeDiary();
        }
        level = Level.PROVINCES;
        provinceCode = null;
        regionButton.setText("Region  ▼");
        mapView.setLevel(Level.PROVINCES);
        mapView.setCameraVisible(false);
        loadProvinces();
    }

    private void onRegionClick(MapRegion region) {
        if (region == null) {
            return;
        }
        if (level == Level.PROVINCES && MapLoader.hasSigungu(getContext(), region.code)) {
            showSigungu(region.code, region.name);
        } else {
            openDiary(region);
        }
    }

    private void openDiary(MapRegion region) {
        if (diary != null) {
            closeDiary();
        }
        mapView.setVisibility(INVISIBLE);
        regionButton.setVisibility(INVISIBLE);
        bottomBar.setVisibility(INVISIBLE);
        mapView.setCameraVisible(false);
        diary = new RegionDiaryFlow(
                getContext(),
                navigator,
                region,
                v -> closeDiary(),
                v -> {
                    closeDiary();
                    requestPhoto(region);
                },
                v -> {
                    PhotoStore.delete(getContext(), region.code);
                    closeDiary();
                }
        );
        addView(diary, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        ));
        diary.bringToFront();
    }

    private void closeDiary() {
        if (diary != null) {
            removeView(diary);
            diary = null;
        }
        mapView.setVisibility(VISIBLE);
        regionButton.setVisibility(VISIBLE);
        bottomBar.setVisibility(VISIBLE);
        mapView.setCameraVisible(level == Level.SIGUNGU);
        reloadCurrentMap();
    }

    private void requestPhoto(MapRegion region) {
        navigator.openImagePicker(uri -> {
            Bitmap bitmap = PhotoStore.decode(getContext(), uri);
            if (bitmap == null) {
                Toast.makeText(getContext(), "사진을 불러올 수 없습니다.", Toast.LENGTH_SHORT).show();
                return;
            }
            showPhotoEditor(region, bitmap);
        });
    }

    private void showPhotoEditor(MapRegion region, Bitmap bitmap) {
        mapView.setVisibility(INVISIBLE);
        regionButton.setVisibility(INVISIBLE);
        mapView.setCameraVisible(false);
        photoEditor = new PhotoPositionEditor(
                getContext(),
                bitmap,
                region,
                (scale, offsetX, offsetY) -> {
                    try {
                        PhotoStore.placeSaveAndReturn(
                                getContext(),
                                region.code,
                                bitmap,
                                region,
                                scale,
                                offsetX,
                                offsetY
                        );
                        closePhotoEditor();
                        reloadCurrentMap();
                    } catch (Exception error) {
                        Toast.makeText(getContext(), "사진을 저장할 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                },
                v -> closePhotoEditor()
        );
        addView(photoEditor, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        ));
        photoEditor.bringToFront();
    }

    private void closePhotoEditor() {
        if (photoEditor != null) {
            removeView(photoEditor);
            photoEditor = null;
        }
        mapView.setVisibility(VISIBLE);
        regionButton.setVisibility(VISIBLE);
        mapView.setCameraVisible(level == Level.SIGUNGU);
    }

    private void reloadCurrentMap() {
        if (level == Level.SIGUNGU && provinceCode != null) {
            loadSigungu(provinceCode);
        } else {
            loadProvinces();
        }
    }

    public boolean onBackPressed() {
        if (photoEditor != null) {
            closePhotoEditor();
            return true;
        }
        if (diary != null) {
            closeDiary();
            return true;
        }
        if (level == Level.SIGUNGU) {
            showProvinces();
            return true;
        }
        return false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        loader.shutdownNow();
    }

    private final class MapCanvasView extends View {
        private final Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final Paint photoPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        private MapData frameData;
        private SigunguIndex index;
        private Map<String, Bitmap> photos = new HashMap<>();
        private Map<String, Float> completion = new HashMap<>();
        private Level frameLevel = Level.PROVINCES;
        private String frameProvinceCode;
        private boolean loading;
        private boolean touchEnabled = true;
        private View backButton;
        private OnRegionClick listener;
        private float downX;
        private float downY;

        MapCanvasView(Context context) {
            super(context);
            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            fillPaint.setStyle(Paint.Style.FILL);
            strokePaint.setStyle(Paint.Style.STROKE);
            setContentDescription("대한민국 지도");
        }

        void setOnRegionClick(OnRegionClick listener) {
            this.listener = listener;
        }

        void setBackButton(View button) {
            backButton = button;
        }

        void setCameraVisible(boolean visible) {
            if (backButton != null) {
                backButton.setVisibility(visible ? VISIBLE : INVISIBLE);
            }
            touchEnabled = !visible || frameData != null;
        }

        void setLevel(Level level) {
            frameLevel = level;
            invalidate();
        }

        void setTouchEnabled(boolean enabled) {
            touchEnabled = enabled;
        }

        void setLoading(boolean loading) {
            this.loading = loading;
            invalidate();
        }

        boolean isLoading() {
            return loading;
        }

        MapData data() {
            return frameData;
        }

        void clearFrame() {
            frameData = null;
            photos = new HashMap<>();
            completion = new HashMap<>();
            invalidate();
        }

        void setFrame(
                Level level,
                MapData data,
                String provinceCode,
                SigunguIndex index,
                Map<String, Bitmap> photos,
                Map<String, Float> completion
        ) {
            this.frameLevel = level;
            this.frameData = data;
            this.frameProvinceCode = provinceCode;
            this.index = index;
            this.photos = photos;
            this.completion = completion;
            this.loading = false;
            this.touchEnabled = true;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            canvas.drawColor(colors.background);
            if (frameData == null) {
                if (loading) {
                    drawLoading(canvas);
                }
                return;
            }
            MapTransform transform = MapTransform.fit(
                    frameData.viewBoxWidth,
                    frameData.viewBoxHeight,
                    getWidth(),
                    getHeight()
            );
            canvas.save();
            canvas.translate(transform.offsetX, transform.offsetY);
            canvas.scale(transform.scale, transform.scale);

            float strokeWidth = (frameLevel == Level.SIGUNGU ? dp(1.1f) : dp(1.4f)) / transform.scale;
            if (frameLevel == Level.SIGUNGU) {
                drawSigungu(canvas, strokeWidth);
            } else {
                drawProvinces(canvas, strokeWidth, transform.scale);
            }
            canvas.restore();
        }

        private void drawLoading(Canvas canvas) {
            Paint spinner = new Paint(Paint.ANTI_ALIAS_FLAG);
            spinner.setColor(colors.primary);
            spinner.setStyle(Paint.Style.STROKE);
            spinner.setStrokeWidth(dp(3));
            RectF bounds = new RectF(
                    getWidth() / 2f - dp(18),
                    getHeight() / 2f - dp(18),
                    getWidth() / 2f + dp(18),
                    getHeight() / 2f + dp(18)
            );
            canvas.drawArc(bounds, 20, 280, false, spinner);
            postInvalidateDelayed(40);
        }

        private void drawSigungu(Canvas canvas, float strokeWidth) {
            strokePaint.setStrokeWidth(strokeWidth);
            strokePaint.setColor(colors.mapSigunguStroke);
            for (MapRegion region : mainRegions()) {
                Bitmap photo = photos.get(region.code);
                if (photo != null) {
                    canvas.drawBitmap(photo, null, region.bounds, photoPaint);
                } else {
                    fillPaint.setColor(colors.mapSigunguFill);
                    canvas.drawPath(region.path, fillPaint);
                }
            }
            for (MapRegion region : mainRegions()) {
                canvas.drawPath(region.path, strokePaint);
            }
        }

        private void drawProvinces(Canvas canvas, float strokeWidth, float scale) {
            for (MapRegion region : frameData.regions) {
                float fraction = completion.containsKey(region.code) ? completion.get(region.code) : 0f;
                fillPaint.setColor(AppColors.lerp(
                        colors.mapProvinceFill,
                        colors.mapProvinceFilled,
                        Math.min(0.35f, fraction * 0.35f)
                ));
                canvas.drawPath(region.path, fillPaint);
            }
            for (MapRegion region : frameData.regions) {
                Bitmap photo = photos.get(region.code);
                if (photo != null) {
                    canvas.drawBitmap(photo, null, region.bounds, photoPaint);
                }
            }
            if (index != null) {
                strokePaint.setColor(AppColors.withAlpha(colors.mapProvinceStroke, 179));
                strokePaint.setStrokeWidth(dp(0.8f) / scale);
                for (SigItem item : index.items.values()) {
                    if (item.path != null && item.province != null
                            && completion.containsKey(item.province)
                            && completion.get(item.province) > 0f) {
                        canvas.drawPath(item.path, strokePaint);
                    }
                    Bitmap photo = photos.get(findCode(item));
                    if (photo != null && item.bounds != null) {
                        canvas.drawBitmap(photo, null, item.bounds, photoPaint);
                    }
                }
            }
            strokePaint.setColor(colors.mapProvinceStroke);
            strokePaint.setStrokeWidth(strokeWidth);
            for (MapRegion region : frameData.regions) {
                canvas.drawPath(region.path, strokePaint);
            }
        }

        private String findCode(SigItem target) {
            if (index == null) {
                return null;
            }
            for (Map.Entry<String, SigItem> entry : index.items.entrySet()) {
                if (entry.getValue() == target) {
                    return entry.getKey();
                }
            }
            return null;
        }

        private List<MapRegion> mainRegions() {
            List<MapRegion> result = new ArrayList<>();
            for (MapRegion region : frameData.regions) {
                if (!region.inset) {
                    result.add(region);
                }
            }
            return result;
        }

        private float dp(float value) {
            return ViewUtils.dp(getContext(), value);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            if (!touchEnabled || frameData == null) {
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                downX = event.getX();
                downY = event.getY();
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP
                    && Math.abs(event.getX() - downX) < dp(16)
                    && Math.abs(event.getY() - downY) < dp(16)) {
                MapTransform transform = MapTransform.fit(
                        frameData.viewBoxWidth,
                        frameData.viewBoxHeight,
                        getWidth(),
                        getHeight()
                );
                float x = transform.screenToViewX(event.getX());
                float y = transform.screenToViewY(event.getY());
                for (MapRegion region : frameData.regions) {
                    if (!region.inset && region.contains(x, y)) {
                        if (listener != null) {
                            listener.onClick(region);
                        }
                        break;
                    }
                }
                return true;
            }
            return true;
        }
    }

    private interface OnRegionClick {
        void onClick(MapRegion region);
    }
}
