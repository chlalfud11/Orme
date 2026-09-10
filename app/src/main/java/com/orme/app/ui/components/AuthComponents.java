package com.orme.app.ui.components;

import android.content.Context;
import android.graphics.Color;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.orme.app.R;
import com.orme.app.navigation.AppNavigator;
import com.orme.app.ui.theme.AppColors;

/** 로그인/회원가입에 공통으로 쓰는 View 구성 요소. */
public final class AuthComponents {
    private AuthComponents() {
    }

    public static LinearLayout underlineField(
            Context context,
            int iconRes,
            String hint,
            boolean password
    ) {
        LinearLayout field = new LinearLayout(context);
        field.setOrientation(LinearLayout.VERTICAL);

        LinearLayout row = new LinearLayout(context);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, ViewUtils.dp(context, 4), 0, ViewUtils.dp(context, 4));
        field.addView(row, ViewUtils.linear(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 40)
        ));

        ImageView icon = new ImageView(context);
        icon.setImageResource(iconRes);
        icon.setColorFilter(Color.WHITE);
        row.addView(icon, ViewUtils.linear(ViewUtils.dp(context, 24), ViewUtils.dp(context, 32)));

        ViewUtils.dp(context, 12);
        View spacer = new View(context);
        row.addView(spacer, ViewUtils.linear(ViewUtils.dp(context, 12), 1));

        EditText input = new EditText(context);
        input.setHint(hint);
        input.setHintTextColor(Color.argb(153, 255, 255, 255));
        input.setTextColor(Color.WHITE);
        input.setTextSize(14);
        input.setSingleLine(true);
        input.setPadding(0, 0, 0, ViewUtils.dp(context, 6));
        input.setBackgroundColor(Color.TRANSPARENT);
        input.setInputType(password
                ? InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD
                : InputType.TYPE_CLASS_TEXT);
        if (password) {
            input.setTransformationMethod(PasswordTransformationMethod.getInstance());
        }
        row.addView(input, ViewUtils.weight(1f));

        View underline = new View(context);
        underline.setBackgroundColor(Color.argb(230, 255, 255, 255));
        LinearLayout.LayoutParams underlineParams = ViewUtils.linear(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 1)
        );
        underlineParams.setMargins(ViewUtils.dp(context, 36), 0, 0, 0);
        field.addView(underline, underlineParams);
        field.setTag(input);
        return field;
    }

    public static EditText fieldInput(LinearLayout field) {
        return (EditText) field.getTag();
    }

    public static LinearLayout socialButtons(Context context, AppNavigator navigator) {
        LinearLayout row = new LinearLayout(context);
        row.setGravity(Gravity.CENTER);
        int gap = ViewUtils.dp(context, 20);
        row.setPadding(gap, 0, gap, 0);
        int[] icons = {R.drawable.ic_google, R.drawable.ic_naver, R.drawable.ic_kakao_login};
        for (int iconRes : icons) {
            ImageButton button = new ImageButton(context);
            button.setImageResource(iconRes);
            button.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
            button.setPadding(ViewUtils.dp(context, 9), ViewUtils.dp(context, 9),
                    ViewUtils.dp(context, 9), ViewUtils.dp(context, 9));
            button.setBackground(ViewUtils.rounded(AppColors.CREAM, 23, context));
            button.setColorFilter(null);
            button.setContentDescription(iconRes == R.drawable.ic_google
                    ? "Google 로그인"
                    : iconRes == R.drawable.ic_naver ? "Naver 로그인" : "KakaoTalk 로그인");
            button.setOnClickListener(v -> {
                // 현재 앱과 동일하게 외부 소셜 인증은 연결하지 않는다.
            });
            LinearLayout.LayoutParams params = ViewUtils.linear(ViewUtils.dp(context, 46), ViewUtils.dp(context, 46));
            params.setMargins(ViewUtils.dp(context, 10), 0, ViewUtils.dp(context, 10), 0);
            row.addView(button, params);
        }
        return row;
    }

    public static LinearLayout bottomBar(Context context, AppNavigator navigator) {
        AppColors.Palette colors = navigator.colors();
        LinearLayout column = new LinearLayout(context);
        column.setOrientation(LinearLayout.VERTICAL);
        column.setBackgroundColor(colors.background);
        View divider = new View(context);
        divider.setBackgroundColor(AppColors.withAlpha(colors.border, 77));
        column.addView(divider, ViewUtils.linear(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 1)
        ));

        LinearLayout row = new LinearLayout(context);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(ViewUtils.dp(context, 28), 0, ViewUtils.dp(context, 28), 0);
        column.addView(row, ViewUtils.linear(
                LinearLayout.LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 83)
        ));

        addNavIcon(context, row, R.drawable.ic_nav_map, "지도", v -> navigator.showMap(), 30, colors.primary);
        addSpacer(row);
        addNavIcon(context, row, R.drawable.ic_nav_flight, "여행지 추천", v -> navigator.showSearch(), 30, colors.primary);
        addSpacer(row);
        addNavIcon(context, row, R.drawable.ic_nav_ticket, "저장한 여행지", v -> navigator.showFavorites(), 30, colors.primary);
        addSpacer(row);
        addNavIcon(context, row, R.drawable.ic_nav_account, "프로필", v -> navigator.showProfile(), 34, colors.primary);
        return column;
    }

    private static void addSpacer(LinearLayout row) {
        View spacer = new View(row.getContext());
        row.addView(spacer, new LinearLayout.LayoutParams(0, 1, 1f));
    }

    private static void addNavIcon(
            Context context,
            LinearLayout row,
            int iconRes,
            String description,
            View.OnClickListener listener,
            int sizeDp,
            int tint
    ) {
        ImageButton icon = new ImageButton(context);
        icon.setImageResource(iconRes);
        icon.setContentDescription(description);
        icon.setColorFilter(tint);
        icon.setBackgroundColor(Color.TRANSPARENT);
        icon.setPadding(0, 0, 0, 0);
        icon.setOnClickListener(listener);
        LinearLayout.LayoutParams params = ViewUtils.linear(ViewUtils.dp(context, sizeDp),
                ViewUtils.dp(context, 83));
        row.addView(icon, params);
    }
}
