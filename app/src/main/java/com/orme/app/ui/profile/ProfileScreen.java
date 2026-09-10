package com.orme.app.ui.profile;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.orme.app.R;
import com.orme.app.navigation.AppNavigator;
import com.orme.app.ui.components.ViewUtils;
import com.orme.app.ui.map.PhotoStore;
import com.orme.app.ui.theme.AppColors;

/** 프로필/환경설정 화면. */
public final class ProfileScreen extends FrameLayout {
    public ProfileScreen(Context context, AppNavigator navigator) {
        super(context);
        AppColors.Palette colors = navigator.colors();
        setBackgroundColor(colors.background);

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        FrameLayout.LayoutParams scrollParams = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
        scrollParams.bottomMargin = ViewUtils.dp(context, 84);
        addView(scroll, scrollParams);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(ViewUtils.dp(context, 28), ViewUtils.dp(context, 72),
                ViewUtils.dp(context, 28), 0);
        scroll.addView(content, new ScrollView.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        ));

        FrameLayout profileBox = new FrameLayout(context);
        ImageView photo = new ImageView(context);
        photo.setScaleType(ImageView.ScaleType.CENTER_CROP);
        photo.setClipToOutline(true);
        photo.setOutlineProvider(new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
                outline.setOval(0, 0, view.getWidth(), view.getHeight());
            }
        });
        photo.setVisibility(INVISIBLE);
        profileBox.addView(photo, new FrameLayout.LayoutParams(
                ViewUtils.dp(context, 140),
                ViewUtils.dp(context, 140),
                Gravity.CENTER
        ));
        View profileFrame = new View(context);
        profileFrame.setBackground(profileFrame(context, colors));
        profileBox.addView(profileFrame, new FrameLayout.LayoutParams(
                ViewUtils.dp(context, 140),
                ViewUtils.dp(context, 140),
                Gravity.CENTER
        ));
        ImageView profile = new ImageView(context);
        profile.setImageResource(R.drawable.ic_profile_placeholder);
        profile.setColorFilter(AppColors.withAlpha(colors.muted, 107));
        profile.setScaleType(ImageView.ScaleType.FIT_CENTER);
        profileBox.addView(profile, new FrameLayout.LayoutParams(
                ViewUtils.dp(context, 76),
                ViewUtils.dp(context, 76),
                Gravity.CENTER
        ));
        FrameLayout add = new FrameLayout(context);
        add.setBackground(ViewUtils.rounded(colors.background, 14, context));
        FrameLayout.LayoutParams addParams = new FrameLayout.LayoutParams(
                ViewUtils.dp(context, 28),
                ViewUtils.dp(context, 28),
                Gravity.TOP | Gravity.RIGHT
        );
        addParams.rightMargin = ViewUtils.dp(context, 16);
        addParams.topMargin = ViewUtils.dp(context, 17);
        profileBox.addView(add, addParams);
        ImageView addIcon = new ImageView(context);
        addIcon.setImageResource(R.drawable.ic_profile_add);
        addIcon.setColorFilter(colors.background);
        addIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        add.addView(addIcon, new FrameLayout.LayoutParams(
                ViewUtils.dp(context, 20),
                ViewUtils.dp(context, 20),
                Gravity.CENTER
        ));
        add.setOnClickListener(v -> navigator.openImagePicker(uri -> {
            Bitmap bitmap = PhotoStore.decode(context, uri);
            if (bitmap != null) {
                photo.setImageBitmap(bitmap);
                photo.setVisibility(VISIBLE);
            }
        }));
        content.addView(profileBox, ViewUtils.linear(
                LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 152)
        ));
        addSpace(content, context, 8);

        TextView username = ViewUtils.playfair(context, "miryeong", colors.primary, 20,
                Gravity.CENTER);
        content.addView(username, ViewUtils.linear(
                LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 28)
        ));
        addSpace(content, context, 47);

        addSection(content, context, "계정정보", colors.muted, 16, 47);
        addRow(content, context, "내정보 관리", colors, true, null);
        addRow(content, context, "비밀번호 변경", colors, true, null);
        addSpace(content, context, 13);
        addSection(content, context, "화면", colors.muted, 16, 47);
        addRow(content, context, "언어", colors, true, null);

        LinearLayout darkRow = new LinearLayout(context);
        darkRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView darkText = ViewUtils.text(context, "다크모드", colors.primary, 18,
                Typeface.BOLD, Gravity.CENTER_VERTICAL);
        darkRow.addView(darkText, ViewUtils.weight(1f));
        DarkModeToggle darkSwitch = new DarkModeToggle(context, colors, navigator.isDarkMode());
        darkSwitch.setContentDescription("다크모드");
        darkSwitch.setOnClickListener(v -> {
            boolean checked = !darkSwitch.isChecked();
            darkSwitch.setChecked(checked);
            navigator.setDarkMode(checked);
            navigator.showProfile();
        });
        darkRow.addView(darkSwitch, ViewUtils.linear(
                ViewUtils.dp(context, 42),
                ViewUtils.dp(context, 24)
        ));
        content.addView(darkRow, ViewUtils.linear(
                LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 45)
        ));

        addSpace(content, context, 10);
        addSection(content, context, "정보", colors.muted, 16, 39);
        addRow(content, context, "공지사항", colors, true, null);
        addRow(content, context, "앱 정보", colors, true, null);
        View weightSpace = new View(context);
        content.addView(weightSpace, new LinearLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                0,
                1f
        ));

        TextView logout = ViewUtils.clickableText(context, "로그아웃", colors.muted, 12,
                v -> showLogoutDialog(context, navigator, colors));
        logout.setPaintFlags(logout.getPaintFlags() | android.graphics.Paint.UNDERLINE_TEXT_FLAG);
        logout.setGravity(Gravity.CENTER);
        logout.setPadding(0, 0, 0, ViewUtils.dp(context, 24));
        content.addView(logout, ViewUtils.linear(
                LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 48)
        ));

        addView(navigator.bottomBar(), new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 84),
                Gravity.BOTTOM
        ));
    }

    private static void addSection(LinearLayout parent, Context context, String label,
                                   int color, float size, int heightDp) {
        TextView section = ViewUtils.text(context, label, color, size,
                Typeface.NORMAL, Gravity.LEFT | Gravity.TOP);
        section.setPadding(0, ViewUtils.dp(context, 5), 0, 0);
        parent.addView(section, ViewUtils.linear(
                LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, heightDp)
        ));
    }

    private static void addRow(
            LinearLayout parent,
            Context context,
            String label,
            AppColors.Palette colors,
            boolean chevron,
            View.OnClickListener listener
    ) {
        LinearLayout row = new LinearLayout(context);
        row.setGravity(Gravity.CENTER_VERTICAL);
        TextView text = ViewUtils.text(context, label, colors.primary, 18,
                Typeface.BOLD, Gravity.CENTER_VERTICAL);
        row.addView(text, ViewUtils.weight(1f));
        if (chevron) {
            TextView arrow = ViewUtils.text(context, "›", colors.muted, 30,
                    Typeface.NORMAL, Gravity.CENTER);
            row.addView(arrow, ViewUtils.linear(ViewUtils.dp(context, 10), ViewUtils.dp(context, 45)));
        }
        if (listener != null) {
            row.setOnClickListener(listener);
        }
        parent.addView(row, ViewUtils.linear(
                LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 45)
        ));
    }

    private static void addSpace(LinearLayout parent, Context context, int dp) {
        View space = new View(context);
        parent.addView(space, ViewUtils.linear(LayoutParams.MATCH_PARENT, ViewUtils.dp(context, dp)));
    }

    private static void showLogoutDialog(
            Context context,
            AppNavigator navigator,
            AppColors.Palette colors
    ) {
        android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(context)
                .setTitle("로그아웃 하시겠습니까?")
                .setNegativeButton("취소", null)
                .setPositiveButton("로그아웃", null)
                .create();
        dialog.setOnShowListener(ignored -> {
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(colors.accent);
            dialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(colors.primary);
            dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                dialog.dismiss();
                navigator.logout();
            });
        });
        dialog.show();
    }

    private static GradientDrawable profileFrame(Context context, AppColors.Palette colors) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(Color.TRANSPARENT);
        drawable.setStroke(ViewUtils.dp(context, 1), AppColors.withAlpha(colors.border, 89));
        return drawable;
    }

    private static final class DarkModeToggle extends View {
        private final AppColors.Palette colors;
        private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        private boolean checked;

        DarkModeToggle(Context context, AppColors.Palette colors, boolean checked) {
            super(context);
            this.colors = colors;
            this.checked = checked;
            setClickable(true);
        }

        boolean isChecked() {
            return checked;
        }

        void setChecked(boolean checked) {
            this.checked = checked;
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            float width = getWidth();
            float height = getHeight();
            float radius = height / 2f;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(checked ? colors.accent : colors.surface);
            canvas.drawRoundRect(0, 0, width, height, radius, radius, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(ViewUtils.dp(getContext(), 1.5f));
            paint.setColor(checked ? colors.accent : colors.muted);
            canvas.drawRoundRect(
                    paint.getStrokeWidth() / 2,
                    paint.getStrokeWidth() / 2,
                    width - paint.getStrokeWidth() / 2,
                    height - paint.getStrokeWidth() / 2,
                    radius,
                    radius,
                    paint
            );
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(checked ? colors.primary : colors.muted);
            float diameter = ViewUtils.dp(getContext(), 16);
            float left = checked ? width - diameter - ViewUtils.dp(getContext(), 3) : ViewUtils.dp(getContext(), 3);
            canvas.drawCircle(left + diameter / 2f, height / 2f, diameter / 2f, paint);
        }
    }
}

final class ProfilePhotoTransform {
    final float scale;
    final float offsetX;
    final float offsetY;

    ProfilePhotoTransform() {
        this(1f, 0f, 0f);
    }

    private ProfilePhotoTransform(float scale, float offsetX, float offsetY) {
        this.scale = scale;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    ProfilePhotoTransform updated(float zoomChange, PointF panChange, float frameSizePx) {
        float nextScale = Math.max(1f, Math.min(3f, scale * zoomChange));
        float maxOffset = frameSizePx * (nextScale - 1f) / 2f;
        float x = Math.max(-maxOffset, Math.min(maxOffset, offsetX + panChange.x));
        float y = Math.max(-maxOffset, Math.min(maxOffset, offsetY + panChange.y));
        return new ProfilePhotoTransform(nextScale, x, y);
    }
}
