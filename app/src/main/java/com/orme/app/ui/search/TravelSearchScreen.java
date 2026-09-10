package com.orme.app.ui.search;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Outline;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.orme.app.navigation.AppNavigator;
import com.orme.app.ui.components.IconViews;
import com.orme.app.ui.components.ViewUtils;
import com.orme.app.ui.theme.AppColors;
import com.orme.app.ui.theme.AppTypography;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** 여행지 검색/추천 화면. */
public final class TravelSearchScreen extends FrameLayout {
    static final List<Destination> RECOMMENDED = Arrays.asList(
            new Destination("Jeonju", "35km away", "jeonju"),
            new Destination("Gyeongju", "75km away", "gyeongju"),
            new Destination("Boseong", "142km away", "boseong")
    );
    static final List<Destination> NEARBY = Arrays.asList(
            new Destination("Iksan", "16km away", "iksan"),
            new Destination("Buan", "42km away", "buan"),
            new Destination("Gunsan", "5km away", "gunsan")
    );
    static final List<Destination> ALL_DESTINATIONS = allDestinations();

    private final AppNavigator navigator;
    private final AppColors.Palette colors;
    private final HorizontalScrollView recommendedList;
    private final HorizontalScrollView nearList;
    private String query = "";

    public TravelSearchScreen(Context context, AppNavigator navigator) {
        super(context);
        this.navigator = navigator;
        this.colors = navigator.colors();
        setBackgroundColor(colors.background);

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(false);
        scroll.setBackgroundColor(colors.background);
        FrameLayout.LayoutParams scrollParams = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
        scrollParams.topMargin = 0;
        scrollParams.bottomMargin = ViewUtils.dp(context, 84);
        addView(scroll, scrollParams);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, ViewUtils.dp(context, 40),
                0, ViewUtils.dp(context, 20));
        scroll.addView(content, new ScrollView.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        ));

        LinearLayout location = new LinearLayout(context);
        location.setGravity(Gravity.CENTER);
        IconViews.Pin pin = new IconViews.Pin(context);
        location.addView(pin, ViewUtils.linear(ViewUtils.dp(context, 22), ViewUtils.dp(context, 22)));
        ViewUtils.addGap(location, context, 7, false);
        TextView city = ViewUtils.text(context, "Gunsan", colors.primary, 20,
                Typeface.BOLD, Gravity.CENTER_VERTICAL);
        city.setTypeface(AppTypography.sans(Typeface.BOLD));
        city.setTextScaleX(1.04f);
        location.addView(city, ViewUtils.linear(ViewGroup.LayoutParams.WRAP_CONTENT, ViewUtils.dp(context, 22)));
        ViewUtils.addGap(location, context, 7, false);
        IconViews.Chevron down = new IconViews.Chevron(context);
        down.setContentDescription("지역 선택");
        location.addView(down, ViewUtils.linear(ViewUtils.dp(context, 14), ViewUtils.dp(context, 22)));
        content.addView(location, ViewUtils.linear(
                LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 42)
        ));

        FrameLayout searchRow = new FrameLayout(context);
        searchRow.setBackground(ViewUtils.rounded(colors.surface, 34, context));
        LinearLayout.LayoutParams searchParams = ViewUtils.linear(
                LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 44)
        );
        searchParams.setMargins(ViewUtils.dp(context, 22), ViewUtils.dp(context, 22),
                ViewUtils.dp(context, 63), 0);
        searchRow.setElevation(ViewUtils.dp(context, 5));
        content.addView(searchRow, searchParams);

        IconViews.Magnifier magnifier = new IconViews.Magnifier(context);
        FrameLayout.LayoutParams magnifierParams = new FrameLayout.LayoutParams(
                ViewUtils.dp(context, 22),
                ViewUtils.dp(context, 22),
                Gravity.CENTER_VERTICAL | Gravity.LEFT
        );
        magnifierParams.leftMargin = ViewUtils.dp(context, 16);
        searchRow.addView(magnifier, magnifierParams);

        EditText search = new EditText(context);
        search.setHint("Where to next?");
        search.setHintTextColor(AppColors.withAlpha(colors.muted, 75));
        search.setTextColor(colors.primary);
        search.setTextSize(16);
        search.setSingleLine(true);
        search.setBackground(new ColorDrawable(Color.TRANSPARENT));
        search.setPadding(0, 0, ViewUtils.dp(context, 12), 0);
        FrameLayout.LayoutParams searchTextParams = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
        searchTextParams.leftMargin = ViewUtils.dp(context, 52);
        searchRow.addView(search, searchTextParams);
        search.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                query = s.toString();
                rebuildCards();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        IconViews.SearchHeartButton favorites = new IconViews.SearchHeartButton(context);
        favorites.setOnClickListener(v -> navigator.showFavorites());
        FrameLayout.LayoutParams favoriteParams = new FrameLayout.LayoutParams(
                ViewUtils.dp(context, 36),
                ViewUtils.dp(context, 30),
                Gravity.TOP | Gravity.RIGHT
        );
        favoriteParams.topMargin = ViewUtils.dp(context, 111);
        favoriteParams.rightMargin = ViewUtils.dp(context, 24);
        favorites.setBackground(ViewUtils.rounded(colors.surface, 18, context));
        favorites.setElevation(ViewUtils.dp(context, 4));
        addView(favorites, favoriteParams);

        TextView recommendedTitle = ViewUtils.playfair(context, "Recommended for you",
                colors.accent, 18, Gravity.LEFT | Gravity.CENTER_VERTICAL);
        recommendedTitle.setIncludeFontPadding(false);
        recommendedTitle.setTextScaleX(1.08f);
        recommendedTitle.setPadding(ViewUtils.dp(context, 23), 0, 0, 0);
        LinearLayout.LayoutParams titleParams = ViewUtils.linear(
                LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 36)
        );
        titleParams.setMargins(0, ViewUtils.dp(context, 20), 0, 0);
        content.addView(recommendedTitle, titleParams);

        recommendedList = cardRow(context);
        content.addView(recommendedList, ViewUtils.linear(
                LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 260)
        ));

        TextView nearTitle = ViewUtils.playfair(context, "Near you", colors.accent, 18,
                Gravity.LEFT | Gravity.CENTER_VERTICAL);
        nearTitle.setIncludeFontPadding(false);
        nearTitle.setTextScaleX(1.08f);
        nearTitle.setPadding(ViewUtils.dp(context, 23), 0, 0, 0);
        LinearLayout.LayoutParams nearTitleParams = ViewUtils.linear(
                LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 36)
        );
        nearTitleParams.setMargins(0, ViewUtils.dp(context, 18), 0, 0);
        content.addView(nearTitle, nearTitleParams);

        nearList = cardRow(context);
        content.addView(nearList, ViewUtils.linear(
                LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 260)
        ));

        addView(navigator.bottomBar(), bottomBarParams(context));
        rebuildCards();
    }

    private HorizontalScrollView cardRow(Context context) {
        HorizontalScrollView scroll = new HorizontalScrollView(context);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(context);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(ViewUtils.dp(context, 21), 0, ViewUtils.dp(context, 20), 0);
        scroll.addView(row, new HorizontalScrollView.LayoutParams(
                LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT
        ));
        return scroll;
    }

    private FrameLayout.LayoutParams bottomBarParams(Context context) {
        return new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 84),
                Gravity.BOTTOM
        );
    }

    private void rebuildCards() {
        if (recommendedList == null || nearList == null) {
            return;
        }
        Context context = getContext();
        fillRow(recommendedList, RECOMMENDED, context);
        fillRow(nearList, NEARBY, context);
    }

    private void fillRow(HorizontalScrollView scroll, List<Destination> destinations, Context context) {
        LinearLayout row = (LinearLayout) scroll.getChildAt(0);
        row.removeAllViews();
        String normalized = query.toLowerCase(Locale.ROOT);
        int matches = 0;
        for (Destination destination : destinations) {
            if (!normalized.isEmpty()
                    && !destination.name.toLowerCase(Locale.ROOT).contains(normalized)) {
                continue;
            }
            row.addView(new TravelCard(context, destination, navigator),
                    ViewUtils.linear(ViewUtils.dp(context, 300), ViewUtils.dp(context, 260)));
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)
                    row.getChildAt(row.getChildCount() - 1).getLayoutParams();
            params.setMargins(ViewUtils.dp(context, 0), 0, ViewUtils.dp(context, 16), 0);
            matches++;
        }
        if (matches == 0) {
            TextView empty = ViewUtils.text(context, "No destinations found", colors.muted, 15,
                    Typeface.NORMAL, Gravity.CENTER);
            row.addView(empty, ViewUtils.linear(ViewUtils.dp(context, 300), ViewUtils.dp(context, 260)));
        }
    }

    static List<Destination> allDestinations() {
        List<Destination> all = new ArrayList<>(RECOMMENDED);
        all.addAll(NEARBY);
        return all;
    }

    static final class Destination {
        final String name;
        final String distance;
        final String asset;

        Destination(String name, String distance, String asset) {
            this.name = name;
            this.distance = distance;
            this.asset = asset;
        }
    }

    static final class TravelCard extends FrameLayout {
        TravelCard(Context context, Destination destination, AppNavigator navigator) {
            super(context);
            setClipToOutline(true);
            setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    outline.setRoundRect(0, 0, view.getWidth(), view.getHeight(),
                            ViewUtils.dp(context, 24));
                }
            });
            setBackgroundColor(Color.DKGRAY);

            ImageView image = new ImageView(context);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Bitmap bitmap = ViewUtils.assetBitmap(context, destination.asset);
            if (bitmap != null) {
                image.setImageBitmap(bitmap);
            }
            addView(image, new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
            ));
            addView(new IconViews.GradientOverlay(context), new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT
            ));

            IconViews.Heart heart = new IconViews.Heart(context,
                    navigator.favorites().contains(destination.name));
            heart.setOnClickListener(v -> {
                navigator.toggleFavorite(destination.name);
                heart.setSelectedState(navigator.favorites().contains(destination.name));
            });
            FrameLayout.LayoutParams heartParams = new FrameLayout.LayoutParams(
                    ViewUtils.dp(context, 44),
                    ViewUtils.dp(context, 44),
                    Gravity.TOP | Gravity.RIGHT
            );
            heartParams.topMargin = ViewUtils.dp(context, 8);
            heartParams.rightMargin = ViewUtils.dp(context, 8);
            addView(heart, heartParams);

            TextView distance = ViewUtils.text(context, destination.distance, Color.WHITE, 11,
                    Typeface.BOLD, Gravity.LEFT);
            FrameLayout.LayoutParams distanceParams = new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    ViewUtils.dp(context, 28),
                    Gravity.BOTTOM | Gravity.LEFT
            );
            distanceParams.leftMargin = ViewUtils.dp(context, 14);
            distanceParams.bottomMargin = ViewUtils.dp(context, 42);
            addView(distance, distanceParams);

            TextView name = ViewUtils.playfair(context, destination.name, Color.WHITE, 30,
                    Gravity.LEFT | Gravity.CENTER_VERTICAL);
            FrameLayout.LayoutParams nameParams = new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    ViewUtils.dp(context, 48),
                    Gravity.BOTTOM | Gravity.LEFT
            );
            nameParams.leftMargin = ViewUtils.dp(context, 14);
            nameParams.bottomMargin = ViewUtils.dp(context, 6);
            addView(name, nameParams);
            name.setTypeface(AppTypography.playfair(context), Typeface.BOLD);
        }
    }
}
