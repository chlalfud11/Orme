package com.orme.app.ui.signup;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.orme.app.R;
import com.orme.app.navigation.AppNavigator;
import com.orme.app.ui.components.AuthComponents;
import com.orme.app.ui.components.ViewUtils;
import com.orme.app.ui.theme.AppColors;
import com.orme.app.ui.theme.AppTypography;

/** 회원가입 화면을 Android View로 구성한다. */
public final class SignupScreen extends FrameLayout {
    public SignupScreen(Context context, AppNavigator navigator) {
        super(context);
        AppColors.Palette colors = navigator.colors();
        setBackgroundColor(colors.background);

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        addView(scroll, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        ));

        FrameLayout viewport = new FrameLayout(context);
        scroll.addView(viewport, new ScrollView.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        ));

        LinearLayout shell = new LinearLayout(context);
        shell.setOrientation(LinearLayout.VERTICAL);
        shell.setGravity(Gravity.CENTER_HORIZONTAL);
        shell.setPadding(ViewUtils.dp(context, 22), ViewUtils.dp(context, 25),
                ViewUtils.dp(context, 22), ViewUtils.dp(context, 27.5f));
        shell.setBackgroundColor(colors.accent);

        FrameLayout cardHolder = new FrameLayout(context);
        cardHolder.setPadding(0, ViewUtils.dp(context, 60), 0, 0);
        cardHolder.addView(shell, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        ));

        FrameLayout centered = new FrameLayout(context);
        centered.setPadding(ViewUtils.dp(context, 34), 0, ViewUtils.dp(context, 34), 0);
        centered.addView(cardHolder, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        ));
        centered.setTranslationY(-ViewUtils.dp(context, 35));
        viewport.addView(centered, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        ));

        TextView title = ViewUtils.playfair(context, "Signup", colors.primary, 56, Gravity.LEFT);
        title.setIncludeFontPadding(false);
        title.setTextScaleX(1.50f);
        title.setTypeface(AppTypography.playfair(context), Typeface.BOLD);
        title.setTranslationX(-ViewUtils.dp(context, 5));
        title.setTranslationY(ViewUtils.dp(context, 1));
        FrameLayout.LayoutParams titleParams = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        titleParams.gravity = Gravity.TOP | Gravity.LEFT;
        titleParams.topMargin = 0;
        centered.addView(title, titleParams);

        addField(shell, context, R.drawable.ic_id, "Username", false);
        addSpace(shell, context, 12);
        addField(shell, context, R.drawable.ic_lock, "Password", true);
        addSpace(shell, context, 12);
        addField(shell, context, R.drawable.ic_person, "Name", false);
        addSpace(shell, context, 12);
        addField(shell, context, R.drawable.ic_mail, "Email", false);
        addSpace(shell, context, 22);

        TextView signupWith = ViewUtils.text(context, "Or Signup with", Color.WHITE, 13,
                Typeface.NORMAL, Gravity.CENTER);
        shell.addView(signupWith, ViewUtils.linear(LayoutParams.MATCH_PARENT, ViewUtils.dp(context, 24)));
        addSpace(shell, context, 14);
        shell.addView(AuthComponents.socialButtons(context, navigator),
                ViewUtils.linear(LayoutParams.MATCH_PARENT, ViewUtils.dp(context, 46)));
        addSpace(shell, context, 24);

        TextView login = new TextView(context);
        login.setTextColor(Color.WHITE);
        login.setTextSize(12);
        login.setGravity(Gravity.CENTER);
        login.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        SpannableString loginText = new SpannableString("Already have an account? Login");
        loginText.setSpan(new StyleSpan(Typeface.BOLD), 0, loginText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        int loginStart = loginText.toString().indexOf("Login");
        loginText.setSpan(new UnderlineSpan(), loginStart, loginText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        login.setText(loginText);
        login.setClickable(true);
        login.setOnClickListener(v -> navigator.showLogin());
        shell.addView(login, ViewUtils.linear(LayoutParams.MATCH_PARENT, ViewUtils.dp(context, 22)));
    }

    private static void addField(
            LinearLayout parent,
            Context context,
            int icon,
            String hint,
            boolean password
    ) {
        parent.addView(AuthComponents.underlineField(context, icon, hint, password),
                ViewUtils.linear(LayoutParams.MATCH_PARENT, ViewUtils.dp(context, 41)));
    }

    private static void addSpace(LinearLayout parent, Context context, int dp) {
        View space = new View(context);
        parent.addView(space, ViewUtils.linear(LayoutParams.MATCH_PARENT, ViewUtils.dp(context, dp)));
    }
}
