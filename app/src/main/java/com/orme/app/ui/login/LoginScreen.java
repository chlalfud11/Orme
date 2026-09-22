package com.orme.app.ui.login;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.orme.app.R;
import com.orme.app.navigation.AppNavigator;
import com.orme.app.ui.components.AuthComponents;
import com.orme.app.ui.components.ViewUtils;
import com.orme.app.ui.theme.AppColors;
import com.orme.app.ui.theme.AppTypography;

/** 로그인 화면을 Android View로 구성한다. */
public final class LoginScreen extends FrameLayout {
    public LoginScreen(Context context, AppNavigator navigator) {
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
        shell.setPadding(ViewUtils.dp(context, 22), ViewUtils.dp(context, 28),
                ViewUtils.dp(context, 22), ViewUtils.dp(context, 27));
        shell.setGravity(Gravity.CENTER_HORIZONTAL);
        shell.setBackgroundColor(colors.accent);

        FrameLayout cardHolder = new FrameLayout(context);
        cardHolder.setPadding(0, ViewUtils.dp(context, 58), 0, 0);
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
        centered.setTranslationY(-ViewUtils.dp(context, 55.5f));
        viewport.addView(centered, new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        ));

        TextView title = ViewUtils.playfair(context, "Login", colors.primary, 56, Gravity.LEFT);
        title.setIncludeFontPadding(false);
        title.setTextScaleX(1.50f);
        title.setTypeface(AppTypography.playfair(context), Typeface.BOLD);
        FrameLayout.LayoutParams titleParams = new FrameLayout.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT
        );
        titleParams.gravity = Gravity.TOP | Gravity.LEFT;
        centered.addView(title, titleParams);

        LinearLayout idField = AuthComponents.underlineField(context, R.drawable.ic_id, "Username", false);
        LinearLayout passwordField = AuthComponents.underlineField(context, R.drawable.ic_lock, "Password", true);
        shell.addView(idField, ViewUtils.linear(
                LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 41)
        ));
        addSpace(shell, context, 18);
        shell.addView(passwordField, ViewUtils.linear(
                LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 41)
        ));
        addSpace(shell, context, 18);

        LinearLayout actionRow = new LinearLayout(context);
        actionRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView forgot = ViewUtils.text(context, "Forgot password?", Color.WHITE, 13,
                Typeface.BOLD, Gravity.LEFT);
        forgot.setPaintFlags(forgot.getPaintFlags() | TextPaint.UNDERLINE_TEXT_FLAG);
        actionRow.addView(forgot, ViewUtils.weight(1f));

        ImageButton loginButton = new ImageButton(context);
        loginButton.setImageResource(R.drawable.ic_login);
        loginButton.setColorFilter(Color.WHITE);
        loginButton.setBackgroundColor(Color.TRANSPARENT);
        loginButton.setContentDescription("Login");
        loginButton.setPadding(0, 0, 0, 0);
        actionRow.addView(loginButton, ViewUtils.linear(ViewUtils.dp(context, 30), ViewUtils.dp(context, 30)));
        shell.addView(actionRow, ViewUtils.linear(LayoutParams.MATCH_PARENT, ViewUtils.dp(context, 30)));

        TextView error = ViewUtils.text(context, "아이디 또는 비밀번호가 올바르지 않습니다.",
                Color.WHITE, 12, Typeface.NORMAL, Gravity.CENTER);
        error.setVisibility(View.GONE);
        LinearLayout.LayoutParams errorParams = ViewUtils.linear(
                LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 28)
        );
        errorParams.topMargin = ViewUtils.dp(context, 14);
        shell.addView(error, errorParams);

        addSpace(shell, context, 22);
        TextView continueText = ViewUtils.text(context, "Or continue with", Color.WHITE, 13,
                Typeface.NORMAL, Gravity.CENTER);
        shell.addView(continueText, ViewUtils.linear(LayoutParams.MATCH_PARENT, ViewUtils.dp(context, 24)));
        addSpace(shell, context, 14);
        shell.addView(AuthComponents.socialButtons(context, navigator),
                ViewUtils.linear(LayoutParams.MATCH_PARENT, ViewUtils.dp(context, 46)));
        addSpace(shell, context, 28);

        TextView signup = new TextView(context);
        signup.setTextColor(Color.WHITE);
        signup.setTextSize(12);
        signup.setGravity(Gravity.CENTER);
        signup.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        SpannableString signupText = new SpannableString("Don't you have an account? Signup");
        signupText.setSpan(new StyleSpan(Typeface.BOLD), 0, signupText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        int signupStart = signupText.toString().indexOf("Signup");
        signupText.setSpan(new ForegroundColorSpan(0xFFF4D64A), signupStart,
                signupText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        signupText.setSpan(new UnderlineSpan(), signupStart,
                signupText.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        signup.setText(signupText);
        signup.setClickable(true);
        signup.setOnClickListener(v -> navigator.showSignup());
        shell.addView(signup, ViewUtils.linear(LayoutParams.MATCH_PARENT, ViewUtils.dp(context, 22)));

        EditText idInput = AuthComponents.fieldInput(idField);
        EditText passwordInput = AuthComponents.fieldInput(passwordField);
        View.OnFocusChangeListener clearError = (v, hasFocus) -> {
            if (hasFocus) {
                error.setVisibility(View.GONE);
            }
        };
        idInput.setOnFocusChangeListener(clearError);
        passwordInput.setOnFocusChangeListener(clearError);
        loginButton.setOnClickListener(v -> {
            if (isAdminLogin(idInput.getText().toString(), passwordInput.getText().toString())) {
                navigator.showMap();
            } else {
                error.setVisibility(View.VISIBLE);
            }
        });
    }

    private static void addSpace(LinearLayout parent, Context context, int dp) {
        View space = new View(context);
        parent.addView(space, ViewUtils.linear(LayoutParams.MATCH_PARENT, ViewUtils.dp(context, dp)));
    }

    public static boolean isAdminLogin(String id, String password) {
        return "admin".equals(id) && "admin".equals(password);
    }
}
