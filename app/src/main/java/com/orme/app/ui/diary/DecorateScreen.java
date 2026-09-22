package com.orme.app.ui.diary;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.orme.app.R;
import com.orme.app.navigation.AppNavigator;
import com.orme.app.ui.components.ViewUtils;
import com.orme.app.ui.map.PhotoStore;
import com.orme.app.ui.theme.AppColors;
import com.orme.app.ui.theme.AppTypography;

import java.util.ArrayList;
import java.util.List;

/** 다이어리 한 페이지를 꾸미는 Java View 화면. */
public final class DecorateScreen extends FrameLayout {
    public interface OnSave {
        void onSave(List<Bitmap> pages);
    }

    private enum Tool {
        SELECT,
        PEN
    }

    private final AppNavigator navigator;
    private final PageState pageState;
    private final PageCanvas pageCanvas;
    private final OnSave onSave;
    private Tool tool = Tool.SELECT;
    private long nextId = 1L;

    public DecorateScreen(
            Context context,
            AppNavigator navigator,
            PageTemplate template,
            View.OnClickListener onBack,
            OnSave onSave
    ) {
        super(context);
        this.navigator = navigator;
        this.onSave = onSave;
        pageState = new PageState(template);
        pageCanvas = new PageCanvas(context);
        setBackgroundColor(navigator.colors().background);

        LinearLayout toolbar = new LinearLayout(context);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(ViewUtils.dp(context, 22), 0, ViewUtils.dp(context, 16), 0);
        toolbar.setBackgroundColor(AppColors.CREAM);
        addView(toolbar, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 113),
                Gravity.TOP
        ));

        addToolIcon(context, toolbar, R.drawable.ic_undo, "실행취소", v -> {
            pageState.undo();
            pageCanvas.invalidate();
        });
        addToolIcon(context, toolbar, R.drawable.ic_redo, "재실행", v -> {
            pageState.redo();
            pageCanvas.invalidate();
        });
        addToolIcon(context, toolbar, R.drawable.ic_pen, "펜", v -> tool = Tool.PEN);

        TextView textTool = ViewUtils.playfair(context, "T", AppColors.GREEN, 22, Gravity.CENTER);
        textTool.setContentDescription("텍스트");
        textTool.setClickable(true);
        textTool.setOnClickListener(v -> showTextDialog());
        toolbar.addView(textTool, toolParams(context));

        addToolIcon(context, toolbar, R.drawable.ic_sticker, "스티커", v -> {
            pageState.add(new StickerElement(nextId++, "☺", ViewUtils.dp(context, 42)),
                    new PointF(ViewUtils.dp(context, 80), ViewUtils.dp(context, 160)));
            pageCanvas.invalidate();
        });

        addToolIcon(context, toolbar, R.drawable.ic_image, "사진", v -> navigator.openImagePicker(this::addPhoto));

        TextView save = ViewUtils.text(context, "저장", AppColors.ORANGE, 16,
                Typeface.BOLD, Gravity.CENTER);
        save.setTypeface(AppTypography.sans(Typeface.BOLD));
        save.setClickable(true);
        save.setOnClickListener(v -> {
            List<Bitmap> pages = new ArrayList<>();
            pages.add(pageCanvas.renderBitmap());
            onSave.onSave(pages);
        });
        LinearLayout.LayoutParams saveParams = toolParams(context);
        saveParams.width = ViewUtils.dp(context, 62);
        toolbar.addView(save, saveParams);
        int[] horizontalOffsets = {-13, -17, 4, 5, 7, 9, 43};
        for (int i = 0; i < toolbar.getChildCount() && i < horizontalOffsets.length; i++) {
            View child = toolbar.getChildAt(i);
            child.setTranslationX(ViewUtils.dp(context, horizontalOffsets[i]));
            child.setTranslationY(ViewUtils.dp(context, 25));
        }
        toolbar.getChildAt(3).setTranslationY(ViewUtils.dp(context, 23));

        FrameLayout.LayoutParams pageParams = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
        pageParams.topMargin = ViewUtils.dp(context, 113);
        pageParams.bottomMargin = ViewUtils.dp(context, 86);
        addView(pageCanvas, pageParams);

        ImageButton back = ViewUtils.backButton(
                context,
                R.drawable.ic_back,
                navigator.colors().primary,
                onBack
        );
        addView(back, ViewUtils.backButtonParams(context));
    }

    private LinearLayout.LayoutParams toolParams(Context context) {
        return new LinearLayout.LayoutParams(
                0,
                ViewUtils.dp(context, 70),
                1f
        );
    }

    private void addToolIcon(
            Context context,
            LinearLayout toolbar,
            int resource,
            String description,
            View.OnClickListener listener
    ) {
        ImageButton button = new ImageButton(context);
        button.setImageResource(resource);
        button.setColorFilter(AppColors.GREEN);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setPadding(ViewUtils.dp(context, 10), ViewUtils.dp(context, 10),
                ViewUtils.dp(context, 10), ViewUtils.dp(context, 10));
        button.setContentDescription(description);
        button.setOnClickListener(listener);
        toolbar.addView(button, toolParams(context));
    }

    private void showTextDialog() {
        EditText input = new EditText(getContext());
        input.setHint("텍스트 입력");
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle("텍스트")
                .setView(input)
                .setNegativeButton("취소", null)
                .setPositiveButton("확인", null)
                .create();
        dialog.setOnShowListener(ignored -> dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String value = input.getText().toString().trim();
            if (!value.isEmpty()) {
                pageState.add(new TextElement(nextId++, value, AppColors.GREEN,
                                ViewUtils.sp(getContext(), 18)),
                        new PointF(ViewUtils.dp(getContext(), 54), ViewUtils.dp(getContext(), 110)));
                pageCanvas.invalidate();
            }
            dialog.dismiss();
        }));
        dialog.show();
    }

    private void addPhoto(Uri uri) {
        Bitmap bitmap = PhotoStore.decode(getContext(), uri);
        if (bitmap == null) {
            return;
        }
        float width = Math.min(ViewUtils.dp(getContext(), 240), bitmap.getWidth());
        float height = width * bitmap.getHeight() / Math.max(1f, bitmap.getWidth());
        pageState.add(new ImageElement(nextId++, bitmap, width, height),
                new PointF(ViewUtils.dp(getContext(), 54), ViewUtils.dp(getContext(), 180)));
        pageCanvas.invalidate();
    }

    private final class PageCanvas extends View {
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private final List<PointF> strokePoints = new ArrayList<>();
        private long selectedId = -1L;
        private float lastX;
        private float lastY;

        PageCanvas(Context context) {
            super(context);
            setBackgroundColor(pageState.template.paperColor);
            setContentDescription("다이어리 페이지");
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            drawContent(canvas, 1f);
        }

        private void drawContent(Canvas canvas, float scale) {
            canvas.drawColor(pageState.template.paperColor);
            drawTemplate(canvas);
            for (DiaryElement element : pageState.elements) {
                PointF position = pageState.positions.get(element.id());
                if (position == null) {
                    continue;
                }
                float elementScale = pageState.scaleOf(element.id());
                canvas.save();
                canvas.translate(position.x, position.y);
                canvas.scale(elementScale, elementScale);
                if (element instanceof TextElement) {
                    TextElement text = (TextElement) element;
                    paint.setColor(text.color);
                    paint.setTextSize(text.fontSizePx);
                    paint.setTypeface(Typeface.DEFAULT_BOLD);
                    paint.setStyle(Paint.Style.FILL);
                    canvas.drawText(text.text, 0, -paint.ascent(), paint);
                } else if (element instanceof StickerElement) {
                    StickerElement sticker = (StickerElement) element;
                    paint.setColor(Color.DKGRAY);
                    paint.setTextSize(sticker.sizePx);
                    paint.setTypeface(Typeface.DEFAULT);
                    canvas.drawText(sticker.emoji, 0, sticker.sizePx, paint);
                } else if (element instanceof ImageElement) {
                    ImageElement image = (ImageElement) element;
                    canvas.drawBitmap(image.bitmap, null,
                            new android.graphics.RectF(0, 0, image.widthPx, image.heightPx), paint);
                } else if (element instanceof StrokeElement) {
                    StrokeElement stroke = (StrokeElement) element;
                    paint.setColor(stroke.color);
                    paint.setStrokeWidth(stroke.widthPx);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setStrokeCap(Paint.Cap.ROUND);
                    for (int i = 1; i < stroke.points.size(); i++) {
                        PointF first = stroke.points.get(i - 1);
                        PointF second = stroke.points.get(i);
                        canvas.drawLine(first.x, first.y, second.x, second.y, paint);
                    }
                    paint.setStyle(Paint.Style.FILL);
                }
                canvas.restore();
            }
        }

        private void drawTemplate(Canvas canvas) {
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(ViewUtils.dp(getContext(), 0.7f));
            paint.setColor(0x50A69D86);
            float gap = ViewUtils.dp(getContext(), 20);
            if (pageState.template == PageTemplate.GRID
                    || pageState.template == PageTemplate.GRID_WHITE) {
                for (float x = gap; x < getWidth(); x += gap) {
                    canvas.drawLine(x, 0, x, getHeight(), paint);
                }
                for (float y = gap; y < getHeight(); y += gap) {
                    canvas.drawLine(0, y, getWidth(), y, paint);
                }
            } else if (pageState.template == PageTemplate.LINED
                    || pageState.template == PageTemplate.LINED_WHITE) {
                for (float y = gap; y < getHeight(); y += gap) {
                    canvas.drawLine(0, y, getWidth(), y, paint);
                }
            } else if (pageState.template == PageTemplate.DOT
                    || pageState.template == PageTemplate.DOT_WHITE) {
                paint.setStyle(Paint.Style.FILL);
                for (float x = gap; x < getWidth(); x += gap) {
                    for (float y = gap; y < getHeight(); y += gap) {
                        canvas.drawCircle(x, y, ViewUtils.dp(getContext(), 1), paint);
                    }
                }
            }
            paint.setStyle(Paint.Style.FILL);
        }

        Bitmap renderBitmap() {
            Bitmap bitmap = Bitmap.createBitmap(720, 1000, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            float scaleX = 720f / Math.max(1, getWidth());
            float scaleY = 1000f / Math.max(1, getHeight());
            canvas.scale(scaleX, scaleY);
            drawContent(canvas, 1f);
            return bitmap;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                lastX = x;
                lastY = y;
                if (tool == Tool.PEN) {
                    strokePoints.clear();
                    strokePoints.add(new PointF(x, y));
                } else {
                    selectedId = findElement(x, y);
                }
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_MOVE) {
                if (tool == Tool.PEN) {
                    strokePoints.add(new PointF(x, y));
                    invalidate();
                } else if (selectedId != -1L) {
                    PointF position = pageState.positions.get(selectedId);
                    if (position != null) {
                        position.x += x - lastX;
                        position.y += y - lastY;
                        invalidate();
                    }
                }
                lastX = x;
                lastY = y;
                return true;
            }
            if (event.getAction() == MotionEvent.ACTION_UP && tool == Tool.PEN
                    && strokePoints.size() > 1) {
                pageState.add(new StrokeElement(nextId++, new ArrayList<>(strokePoints),
                                AppColors.GREEN, ViewUtils.dp(getContext(), 2)),
                        new PointF(0, 0));
                strokePoints.clear();
                invalidate();
                return true;
            }
            return true;
        }

        private long findElement(float x, float y) {
            for (int i = pageState.elements.size() - 1; i >= 0; i--) {
                DiaryElement element = pageState.elements.get(i);
                PointF position = pageState.positions.get(element.id());
                if (position == null) {
                    continue;
                }
                float width = ViewUtils.dp(getContext(), 120);
                float height = ViewUtils.dp(getContext(), 60);
                if (x >= position.x && x <= position.x + width
                        && y >= position.y && y <= position.y + height) {
                    return element.id();
                }
            }
            return -1L;
        }
    }
}
