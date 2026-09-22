package com.orme.app.ui.search;

import android.content.Context;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.orme.app.navigation.AppNavigator;
import com.orme.app.ui.components.AuthComponents;
import com.orme.app.ui.components.ViewUtils;
import com.orme.app.ui.theme.AppColors;

import java.util.Set;
import java.util.ArrayList;
import java.util.List;

/** 저장한 여행지를 보여주는 화면. */
public final class FavoritesScreen extends FrameLayout {
    static List<RegionCatalog.Entry> favoriteDestinations(
            List<RegionCatalog.Entry> catalog,
            Set<String> favoriteCodes
    ) {
        List<RegionCatalog.Entry> result = new ArrayList<>();
        for (RegionCatalog.Entry destination : catalog) {
            if (favoriteCodes.contains(destination.code)) {
                result.add(destination);
            }
        }
        return result;
    }

    public FavoritesScreen(Context context, AppNavigator navigator) {
        super(context);
        AppColors.Palette colors = navigator.colors();
        setBackgroundColor(colors.background);

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        FrameLayout.LayoutParams scrollParams = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
        scrollParams.bottomMargin = AuthComponents.bottomContentInset(context);
        addView(scroll, scrollParams);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(0, ViewUtils.dp(context, 63),
                0, ViewUtils.dp(context, 20));
        scroll.addView(content, new ScrollView.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        ));

        TextView title = ViewUtils.playfair(context, "Your favorites", colors.accent, 24,
                Gravity.LEFT | Gravity.CENTER_VERTICAL);
        title.setPadding(ViewUtils.dp(context, 23), 0, 0, 0);
        content.addView(title, ViewUtils.linear(
                LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 54)
        ));

        List<RegionCatalog.Entry> favorites = favoriteDestinations(
                RegionCatalog.load(context),
                navigator.favorites()
        );
        if (favorites.isEmpty()) {
            TextView empty = ViewUtils.text(context, "저장한 여행지가 없습니다", colors.muted, 15,
                    Typeface.NORMAL, Gravity.LEFT | Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams emptyParams = ViewUtils.linear(
                    LayoutParams.MATCH_PARENT,
                    ViewUtils.dp(context, 60)
            );
            emptyParams.topMargin = ViewUtils.dp(context, 17);
            content.addView(empty, emptyParams);
        } else {
            for (int i = 0; i < favorites.size(); i++) {
                TravelSearchScreen.TravelCard item = new TravelSearchScreen.TravelCard(
                        context, favorites.get(i), navigator, "");
                LinearLayout.LayoutParams itemParams = ViewUtils.linear(
                        LayoutParams.MATCH_PARENT,
                        ViewUtils.dp(context, 270)
                );
                itemParams.leftMargin = ViewUtils.dp(context, 23);
                itemParams.rightMargin = ViewUtils.dp(context, 23);
                itemParams.topMargin = i == 0 ? ViewUtils.dp(context, 16) : ViewUtils.dp(context, 18);
                content.addView(item, itemParams);
            }
        }

        View bottomBar = navigator.bottomBar();
        addView(bottomBar, AuthComponents.bottomBarParams(context));
    }
}
