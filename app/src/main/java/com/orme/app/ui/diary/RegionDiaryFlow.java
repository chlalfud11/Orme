package com.orme.app.ui.diary;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.text.InputType;
import android.text.TextUtils;

import com.orme.app.R;
import com.orme.app.navigation.AppNavigator;
import com.orme.app.ui.components.ViewUtils;
import com.orme.app.ui.map.MapRegion;
import com.orme.app.ui.map.PhotoStore;
import com.orme.app.ui.theme.AppColors;
import com.orme.app.ui.theme.AppTypography;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/** 지역 기록 목록부터 표지/내지 선택, 꾸미기, 기록 보기까지의 흐름. */
public final class RegionDiaryFlow extends FrameLayout {
    private enum Screen {
        LIST,
        COVER_PICK,
        PAGE_PICK,
        DECORATE,
        VIEWER
    }

    private final AppNavigator navigator;
    private final MapRegion region;
    private final View.OnClickListener onClose;
    private final View.OnClickListener onChangeCover;
    private final View.OnClickListener onDeletePhoto;
    private Screen screen = Screen.LIST;
    private CoverTemplate selectedCover;
    private PageTemplate selectedPage;
    private DiaryRecord selectedRecord;
    private String pendingRecordName;

    public RegionDiaryFlow(
            Context context,
            AppNavigator navigator,
            MapRegion region,
            View.OnClickListener onClose
    ) {
        this(context, navigator, region, onClose, v -> {
        }, v -> {
        });
    }

    public RegionDiaryFlow(
            Context context,
            AppNavigator navigator,
            MapRegion region,
            View.OnClickListener onClose,
            View.OnClickListener onChangeCover
    ) {
        this(context, navigator, region, onClose, onChangeCover, v -> {
        });
    }

    public RegionDiaryFlow(
            Context context,
            AppNavigator navigator,
            MapRegion region,
            View.OnClickListener onClose,
            View.OnClickListener onChangeCover,
            View.OnClickListener onDeletePhoto
    ) {
        super(context);
        this.navigator = navigator;
        this.region = region;
        this.onClose = onClose;
        this.onChangeCover = onChangeCover;
        this.onDeletePhoto = onDeletePhoto;
        render();
    }

    private void render() {
        setBackgroundColor(
                screen == Screen.LIST
                        ? AppColors.withAlpha(navigator.colors().background, 230)
                        : navigator.colors().background
        );
        removeAllViews();
        switch (screen) {
            case LIST:
                renderList();
                break;
            case COVER_PICK:
                renderCoverPicker();
                break;
            case PAGE_PICK:
                renderPagePicker();
                break;
            case DECORATE:
                renderDecorator();
                break;
            case VIEWER:
                renderViewer();
                break;
        }
    }

    private void renderList() {
        Context context = getContext();
        AppColors.Palette colors = navigator.colors();
        List<DiaryRecord> records = DiaryStore.listRecords(context, region.code);

        ImageButton back = ViewUtils.backButton(context, R.drawable.ic_back,
                colors.primary, onClose);
        back.setOnClickListener(onClose);
        addView(back, ViewUtils.backButtonParams(context));

        boolean hasRepresentativePhoto = PhotoStore.hasPhoto(context, region.code);
        ImageButton photoButton = icon(
                context,
                R.drawable.ic_add_photo,
                hasRepresentativePhoto ? "대표 사진 변경" : "대표 사진 추가"
        );
        photoButton.setOnClickListener(onChangeCover);
        addTop(photoButton, Gravity.RIGHT, 24, 91, 34, 34);

        ImageButton deletePhoto = null;
        if (hasRepresentativePhoto) {
            deletePhoto = icon(context, R.drawable.ic_trash, "대표 사진 삭제");
            deletePhoto.setOnClickListener(this::confirmDeletePhoto);
            addTop(deletePhoto, Gravity.RIGHT, 70, 91, 34, 34);
        }

        if (records.isEmpty()) {
            TextView empty = ViewUtils.text(context, "아직 기록이 없어요.\n첫 기록을 만들어 보세요!",
                    AppColors.withAlpha(colors.primary, 191), 15.5f,
                    Typeface.NORMAL, Gravity.CENTER);
            empty.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
            empty.setTextScaleX(1.04f);
            empty.setTranslationY(ViewUtils.dp(context, 14));
            addView(empty, new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    ViewUtils.dp(context, 80),
                    Gravity.CENTER
            ));
        } else {
            HorizontalScrollView scroll = new HorizontalScrollView(context);
            scroll.setHorizontalScrollBarEnabled(false);
            scroll.setOverScrollMode(View.OVER_SCROLL_NEVER);
            scroll.setClipToPadding(false);
            LinearLayout list = new LinearLayout(context);
            list.setOrientation(LinearLayout.HORIZONTAL);
            int cardWidth = ViewUtils.dp(context, 210);
            int cardHeight = coverHeightForWidth(cardWidth);
            int horizontalInset = Math.max(
                    ViewUtils.dp(context, 16),
                    (getResources().getDisplayMetrics().widthPixels - cardWidth) / 2
            );
            list.setGravity(Gravity.CENTER_VERTICAL);
            list.setPadding(horizontalInset, 0, horizontalInset, 0);
            int recordGap = ViewUtils.dp(context, 20);
            int focusDistance = cardWidth + recordGap;
            for (DiaryRecord record : records) {
                LinearLayout entry = new LinearLayout(context);
                entry.setOrientation(LinearLayout.VERTICAL);
                entry.setGravity(Gravity.CENTER_HORIZONTAL);
                DiaryCard card = new DiaryCard(context, record, () -> {
                    selectedRecord = record;
                    screen = Screen.VIEWER;
                    render();
                }, () -> {
                    DiaryStore.deleteRecord(context, record);
                    render();
                });
                entry.addView(card, new LinearLayout.LayoutParams(
                        cardWidth,
                        cardHeight
                ));
                TextView name = ViewUtils.text(
                        context,
                        record.name,
                        colors.primary,
                        15.5f,
                        Typeface.BOLD,
                        Gravity.CENTER
                );
                name.setSingleLine(true);
                name.setEllipsize(TextUtils.TruncateAt.END);
                name.setPadding(ViewUtils.dp(context, 2), 0, ViewUtils.dp(context, 2), 0);
                entry.addView(name, new LinearLayout.LayoutParams(
                        cardWidth,
                        ViewUtils.dp(context, 38)
                ));
                LinearLayout.LayoutParams entryParams = new LinearLayout.LayoutParams(
                        cardWidth,
                        cardHeight + ViewUtils.dp(context, 38)
                );
                entryParams.setMargins(0, 0, recordGap, 0);
                list.addView(entry, entryParams);
            }
            scroll.addView(list, new HorizontalScrollView.LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.MATCH_PARENT
            ));
            scroll.setOnScrollChangeListener((view, scrollX, scrollY, oldScrollX, oldScrollY) ->
                    updateRecordScales(scroll, list, focusDistance));
            scroll.setOnTouchListener((view, event) -> {
                int action = event.getActionMasked();
                if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    scroll.post(() -> snapToNearestRecord(scroll, list));
                }
                return false;
            });
            addView(scroll, new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
            ));
            scroll.post(() -> updateRecordScales(scroll, list, focusDistance));
        }

        TextView add = ViewUtils.text(context, "+", colors.primary, 34,
                Typeface.NORMAL, Gravity.CENTER);
        add.setBackground(ViewUtils.roundedBorder(Color.TRANSPARENT, colors.primary, 2, 40, context));
        add.setClickable(true);
        add.setContentDescription("기록 추가");
        add.setOnClickListener(v -> {
            showRecordNameDialog();
        });
        FrameLayout.LayoutParams addParams = new FrameLayout.LayoutParams(
                ViewUtils.dp(context, 52),
                ViewUtils.dp(context, 52),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL
        );
        addParams.bottomMargin = ViewUtils.dp(context, 48);
        addView(add, addParams);
        back.bringToFront();
        photoButton.bringToFront();
        if (deletePhoto != null) {
            deletePhoto.bringToFront();
        }
        add.bringToFront();
    }

    private void showRecordNameDialog() {
        Context context = getContext();
        EditText input = new EditText(context);
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        ViewUtils.enableKoreanInput(input);
        input.setHint("예: 제주도 여행");
        input.setContentDescription("기록물 이름");
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle("기록물 이름")
                .setView(input)
                .setNegativeButton("취소", null)
                .setPositiveButton("다음", null)
                .create();
        dialog.setOnShowListener(ignored -> {
            Button next = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
            next.setOnClickListener(v -> {
                String name = input.getText().toString().trim();
                if (name.isEmpty()) {
                    input.setError("기록물 이름을 입력해 주세요.");
                    return;
                }
                pendingRecordName = name;
                dialog.dismiss();
                screen = Screen.COVER_PICK;
                render();
            });
        });
        dialog.show();
    }

    private void confirmDeletePhoto(View source) {
        new AlertDialog.Builder(getContext())
                .setTitle("대표 사진 삭제")
                .setMessage("이 지역의 대표 사진을 삭제할까요?")
                .setNegativeButton("취소", null)
                .setPositiveButton("삭제", (dialog, which) -> onDeletePhoto.onClick(source))
                .show();
    }

    private void renderCoverPicker() {
        Context context = getContext();
        ImageButton back = addBackButton(context, v -> {
            screen = Screen.LIST;
            render();
        });

        ScrollView scroll = new ScrollView(context);
        GridLayout grid = new GridLayout(context);
        grid.setColumnCount(2);
        grid.setPadding(ViewUtils.dp(context, 16), ViewUtils.dp(context, 110),
                ViewUtils.dp(context, 16), ViewUtils.dp(context, 30));
        for (CoverTemplate cover : CoverTemplate.values()) {
            ImageView image = new ImageView(context);
            image.setImageResource(cover.imageRes);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setClipToOutline(true);
            image.setOutlineProvider(roundOutline(context, 14));
            image.setClickable(true);
            image.setContentDescription(cover.label);
            image.setOnClickListener(v -> {
                selectedCover = cover;
                screen = Screen.PAGE_PICK;
                render();
            });
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = ViewUtils.dp(context, 183);
            params.height = ViewUtils.dp(context, 254);
            params.setMargins(0, ViewUtils.dp(context, 7),
                    ViewUtils.dp(context, 14), ViewUtils.dp(context, 7));
            grid.addView(image, params);
        }
        scroll.addView(grid, new ScrollView.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        ));
        addView(scroll, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        ));
        back.bringToFront();
    }

    private void renderPagePicker() {
        Context context = getContext();
        ImageButton back = addBackButton(context, v -> {
            screen = Screen.COVER_PICK;
            render();
        });

        ScrollView scroll = new ScrollView(context);
        GridLayout grid = new GridLayout(context);
        grid.setColumnCount(2);
        grid.setPadding(ViewUtils.dp(context, 16), ViewUtils.dp(context, 110),
                ViewUtils.dp(context, 16), ViewUtils.dp(context, 30));
        for (PageTemplate template : PageTemplate.values()) {
            TemplatePreview preview = new TemplatePreview(context, template, navigator.colors());
            preview.setClickable(true);
            preview.setContentDescription(template.label);
            preview.setOnClickListener(v -> {
                selectedPage = template;
                screen = Screen.DECORATE;
                render();
            });
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = ViewUtils.dp(context, 183);
            params.height = ViewUtils.dp(context, 254);
            params.setMargins(0, ViewUtils.dp(context, 7),
                    ViewUtils.dp(context, 14), ViewUtils.dp(context, 7));
            grid.addView(preview, params);
        }
        scroll.addView(grid, new ScrollView.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        ));
        addView(scroll, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        ));
        back.bringToFront();
    }

    private void renderDecorator() {
        DecorateScreen decorate = new DecorateScreen(
                getContext(),
                navigator,
                selectedPage,
                v -> {
                    screen = Screen.LIST;
                    render();
                },
                pages -> {
                    try {
                        DiaryStore.saveRecord(
                                getContext(),
                                region.code,
                                pendingRecordName,
                                renderCoverToBitmap(getContext(), selectedCover, 720, 1000),
                                pages
                        );
                        pendingRecordName = null;
                        screen = Screen.LIST;
                        render();
                    } catch (Exception ignored) {
                    }
                }
        );
        addView(decorate, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        ));
    }

    private void renderViewer() {
        Context context = getContext();
        ImageButton back = addBackButton(context, v -> {
            screen = Screen.LIST;
            render();
        });
        if (selectedRecord == null) {
            return;
        }
        ScrollView scroll = new ScrollView(context);
        LinearLayout pages = new LinearLayout(context);
        pages.setOrientation(LinearLayout.VERTICAL);
        pages.setPadding(ViewUtils.dp(context, 32), ViewUtils.dp(context, 120),
                ViewUtils.dp(context, 32), ViewUtils.dp(context, 40));
        for (String path : selectedRecord.pagePaths) {
            Bitmap bitmap = DiaryStore.loadBitmap(path);
            if (bitmap == null) {
                continue;
            }
            ImageView page = new ImageView(context);
            page.setImageBitmap(bitmap);
            page.setAdjustViewBounds(true);
            pages.addView(page, new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    ViewUtils.dp(context, 460)
            ));
        }
        scroll.addView(pages, new ScrollView.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        ));
        addView(scroll, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        ));
        back.bringToFront();
    }

    public boolean onBackPressed() {
        if (screen == Screen.LIST) {
            onClose.onClick(this);
            return true;
        }
        if (screen == Screen.COVER_PICK) {
            screen = Screen.LIST;
        } else if (screen == Screen.PAGE_PICK) {
            screen = Screen.COVER_PICK;
        } else {
            screen = Screen.LIST;
        }
        render();
        return true;
    }

    private ImageButton addBackButton(
            Context context,
            View.OnClickListener listener
    ) {
        ImageButton back = ViewUtils.backButton(
                context,
                R.drawable.ic_back,
                navigator.colors().primary,
                listener
        );
        addView(back, ViewUtils.backButtonParams(context));
        return back;
    }

    private void addTop(View view, int horizontalGravity, int margin, int top,
                        int width, int height) {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewUtils.dp(getContext(), width),
                ViewUtils.dp(getContext(), height),
                Gravity.TOP | horizontalGravity
        );
        if (horizontalGravity == Gravity.RIGHT) {
            params.rightMargin = ViewUtils.dp(getContext(), margin);
        } else {
            params.leftMargin = ViewUtils.dp(getContext(), margin);
        }
        params.topMargin = ViewUtils.dp(getContext(), top);
        addView(view, params);
    }

    private ImageButton icon(Context context, int resource, String description) {
        ImageButton button = new ImageButton(context);
        button.setImageResource(resource);
        button.setColorFilter(navigator.colors().primary);
        button.setBackgroundColor(Color.TRANSPARENT);
        button.setPadding(0, 0, 0, 0);
        button.setScaleType(ImageView.ScaleType.FIT_CENTER);
        button.setContentDescription(description);
        return button;
    }

    private static ViewOutlineProvider roundOutline(Context context, float radius) {
        return new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(),
                        ViewUtils.dp(context, radius));
            }
        };
    }

    static Bitmap renderCoverToBitmap(Context context, CoverTemplate cover, int width, int height) {
        Bitmap source = android.graphics.BitmapFactory.decodeResource(context.getResources(), cover.imageRes);
        return Bitmap.createScaledBitmap(source, Math.max(1, width), Math.max(1, height), true);
    }

    private static final class DiaryCard extends FrameLayout {
        DiaryCard(Context context, DiaryRecord record, Runnable open, Runnable delete) {
            super(context);
            setClipToOutline(true);
            setOutlineProvider(roundOutline(context, 18));
            setBackgroundColor(AppColors.SEARCH_SURFACE);
            setClickable(true);
            setContentDescription(record.name + " 기록 열기");
            setOnClickListener(v -> open.run());
            ImageView cover = new ImageView(context);
            cover.setScaleType(ImageView.ScaleType.FIT_XY);
            if (record.coverPath != null) {
                cover.setImageBitmap(DiaryStore.loadBitmap(record.coverPath));
            }
            addView(cover, new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
            ));
            setOnLongClickListener(v -> {
                delete.run();
                return true;
            });
        }
    }

    private static final class TemplatePreview extends View {
        private final PageTemplate template;
        private final AppColors.Palette colors;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        TemplatePreview(Context context, PageTemplate template, AppColors.Palette colors) {
            super(context);
            this.template = template;
            this.colors = colors;
            setClipToOutline(true);
            setOutlineProvider(roundOutline(context, 10));
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(template.paperColor);
            paint.setColor(0x45A69D86);
            paint.setStrokeWidth(ViewUtils.dp(getContext(), 0.8f));
            float gap = ViewUtils.dp(getContext(), 18);
            if (template == PageTemplate.GRID || template == PageTemplate.GRID_WHITE) {
                for (float x = gap; x < getWidth(); x += gap) {
                    canvas.drawLine(x, 0, x, getHeight(), paint);
                }
                for (float y = gap; y < getHeight(); y += gap) {
                    canvas.drawLine(0, y, getWidth(), y, paint);
                }
            } else if (template == PageTemplate.LINED
                    || template == PageTemplate.LINED_WHITE) {
                for (float y = gap; y < getHeight(); y += gap) {
                    canvas.drawLine(0, y, getWidth(), y, paint);
                }
            } else if (template == PageTemplate.DOT
                    || template == PageTemplate.DOT_WHITE) {
                paint.setStyle(Paint.Style.FILL);
                for (float x = gap; x < getWidth(); x += gap) {
                    for (float y = gap; y < getHeight(); y += gap) {
                        canvas.drawCircle(x, y, ViewUtils.dp(getContext(), 1), paint);
                    }
                }
            }
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(ViewUtils.dp(getContext(), 1));
            paint.setColor(AppColors.withAlpha(colors.primary, 64));
            float inset = paint.getStrokeWidth() / 2f;
            float radius = ViewUtils.dp(getContext(), 10);
            canvas.drawRoundRect(
                    inset,
                    inset,
                    getWidth() - inset,
                    getHeight() - inset,
                    radius,
                    radius,
                    paint
            );
        }
    }

    static boolean shouldShowDeleteIcon(float verticalOffset) {
        return verticalOffset < 0f;
    }

    static float recordScaleForDistance(float distance, float focusDistance) {
        if (focusDistance <= 0f) {
            return 1.24f;
        }
        float clampedDistance = Math.max(0f, Math.min(distance, focusDistance));
        return 1.24f - 0.24f * (clampedDistance / focusDistance);
    }

    static int coverHeightForWidth(int width) {
        return Math.max(1, Math.round(width * 1000f / 720f));
    }

    static int recordScrollTarget(
            int entryLeft,
            int entryWidth,
            int viewportWidth,
            int maxScroll
    ) {
        int desired = entryLeft + entryWidth / 2 - viewportWidth / 2;
        return Math.max(0, Math.min(desired, Math.max(0, maxScroll)));
    }

    private static void updateRecordScales(
            HorizontalScrollView scroll,
            LinearLayout list,
            int focusDistance
    ) {
        float viewportCenter = scroll.getWidth() / 2f;
        for (int i = 0; i < list.getChildCount(); i++) {
            View entry = list.getChildAt(i);
            if (entry.getWidth() == 0) {
                continue;
            }
            float entryCenter = entry.getLeft() + entry.getWidth() / 2f - scroll.getScrollX();
            float distance = Math.abs(entryCenter - viewportCenter);
            float scale = recordScaleForDistance(distance, focusDistance);
            entry.setPivotX(entry.getWidth() / 2f);
            entry.setPivotY(entry.getHeight() / 2f);
            entry.setScaleX(scale);
            entry.setScaleY(scale);
        }
    }

    private static void snapToNearestRecord(
            HorizontalScrollView scroll,
            LinearLayout list
    ) {
        if (list.getChildCount() == 0 || scroll.getWidth() == 0) {
            return;
        }
        int viewportCenter = scroll.getScrollX() + scroll.getWidth() / 2;
        View nearest = list.getChildAt(0);
        int nearestDistance = Math.abs(
                nearest.getLeft() + nearest.getWidth() / 2 - viewportCenter
        );
        for (int i = 1; i < list.getChildCount(); i++) {
            View entry = list.getChildAt(i);
            int distance = Math.abs(entry.getLeft() + entry.getWidth() / 2 - viewportCenter);
            if (distance < nearestDistance) {
                nearest = entry;
                nearestDistance = distance;
            }
        }
        int maxScroll = Math.max(0, list.getWidth() - scroll.getWidth());
        int target = recordScrollTarget(
                nearest.getLeft(),
                nearest.getWidth(),
                scroll.getWidth(),
                maxScroll
        );
        if (target != scroll.getScrollX()) {
            scroll.smoothScrollTo(target, 0);
        }
    }

    static float nextRecordCoverOffset(
            float currentOffset,
            float dragAmount,
            float deleteRevealHeight
    ) {
        return Math.max(-deleteRevealHeight,
                Math.min(0f, currentOffset + dragAmount));
    }
}
