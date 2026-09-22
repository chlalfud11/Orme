package com.orme.app.ui.search;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.Outline;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.orme.app.navigation.AppNavigator;
import com.orme.app.ui.components.AuthComponents;
import com.orme.app.ui.components.IconViews;
import com.orme.app.ui.components.ViewUtils;
import com.orme.app.ui.theme.AppColors;
import com.orme.app.ui.theme.AppTypography;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 여행지 검색/추천 화면. */
public final class TravelSearchScreen extends FrameLayout {
    private static final int LOCATION_PERMISSION_REQUEST = 4108;
    private static final long LOCATION_TIMEOUT_MS = 8000L;
    private static final int CARDS_PER_RAIL = 5;
    private static final String LOCATION_RESOLVING_TEXT = "위치 확인 중…";
    private static final String LOCATION_UNAVAILABLE_TEXT = "위치 확인 불가";

    private final AppNavigator navigator;
    private final AppColors.Palette colors;
    private final HorizontalScrollView recommendedList;
    private final HorizontalScrollView nearList;
    private final List<RegionCatalog.Entry> catalog;
    private final List<RegionCatalog.Entry> recommended = new ArrayList<>();
    private final List<RegionCatalog.Entry> nearby = new ArrayList<>();
    private final Map<String, String> distanceLabels = new HashMap<>();
    private final TextView city;
    private final SharedPreferences recommendationPreferences;
    private final Handler locationHandler = new Handler(Looper.getMainLooper());
    private LocationManager locationManager;
    private LocationListener locationListener;
    private Runnable locationTimeout;
    private boolean locationRequestActive;
    private String query = "";

    public TravelSearchScreen(Context context, AppNavigator navigator) {
        super(context);
        this.navigator = navigator;
        this.colors = navigator.colors();
        this.recommendationPreferences = context.getSharedPreferences(
                RecommendationCache.PREFERENCES,
                Context.MODE_PRIVATE
        );
        this.catalog = RegionCatalog.load(context);
        RecommendationCache.Snapshot cached = RecommendationCache.read(
                recommendationPreferences
        );
        resetOrdering();
        setBackgroundColor(colors.background);

        ScrollView scroll = new ScrollView(context);
        scroll.setFillViewport(true);
        scroll.setClipToPadding(true);
        scroll.setBackgroundColor(colors.background);
        FrameLayout.LayoutParams scrollParams = new FrameLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
        scrollParams.topMargin = 0;
        scrollParams.bottomMargin = AuthComponents.bottomContentInset(context);
        addView(scroll, scrollParams);

        LinearLayout content = new LinearLayout(context);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setClipChildren(true);
        content.setPadding(0, ViewUtils.dp(context, 40),
                0, ViewUtils.dp(context, 32));
        scroll.addView(content, new ScrollView.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT
        ));

        LinearLayout location = new LinearLayout(context);
        location.setGravity(Gravity.CENTER);
        IconViews.Pin pin = new IconViews.Pin(context);
        pin.setContentDescription("현재 위치 새로고침");
        pin.setOnClickListener(v -> refreshLocation());
        location.addView(pin, ViewUtils.linear(ViewUtils.dp(context, 22), ViewUtils.dp(context, 22)));
        ViewUtils.addGap(location, context, 7, false);
        this.city = ViewUtils.text(context, LOCATION_RESOLVING_TEXT, colors.primary, 20,
                Typeface.BOLD, Gravity.CENTER_VERTICAL);
        city.setTag("location-status");
        city.setTypeface(AppTypography.sans(Typeface.BOLD));
        city.setTextScaleX(1.04f);
        location.addView(city, ViewUtils.linear(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewUtils.dp(context, 32)
        ));
        if (cached != null && !restoreOrdering(cached)) {
            resetOrdering();
        }
        content.addView(location, ViewUtils.linear(
                LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 42)
        ));

        FrameLayout searchControls = new FrameLayout(context);
        content.addView(searchControls, ViewUtils.linear(
                LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 76)
        ));

        FrameLayout searchRow = new FrameLayout(context);
        searchRow.setBackground(ViewUtils.rounded(colors.surface, 34, context));
        LinearLayout.LayoutParams searchParams = ViewUtils.linear(
                LayoutParams.MATCH_PARENT,
                ViewUtils.dp(context, 48)
        );
        searchParams.setMargins(ViewUtils.dp(context, 22), ViewUtils.dp(context, 18),
                ViewUtils.dp(context, 63), 0);
        searchRow.setElevation(ViewUtils.dp(context, 5));
        searchControls.addView(searchRow, searchParams);

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
        ViewUtils.enableKoreanInput(search);
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
        favoriteParams.topMargin = ViewUtils.dp(context, 27);
        favoriteParams.rightMargin = ViewUtils.dp(context, 24);
        favorites.setBackground(ViewUtils.rounded(colors.surface, 18, context));
        favorites.setElevation(ViewUtils.dp(context, 4));
        searchControls.addView(favorites, favoriteParams);

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

        View bottomBar = navigator.bottomBar();
        addView(bottomBar, AuthComponents.bottomBarParams(context));
        rebuildCards();
        location.setOnClickListener(v -> refreshLocation());
        post(this::requestLocation);
    }

    private void resetOrdering() {
        recommended.clear();
        nearby.clear();
        distanceLabels.clear();
        int recommendedEnd = Math.min(CARDS_PER_RAIL, catalog.size());
        int nearbyEnd = Math.min(recommendedEnd + CARDS_PER_RAIL, catalog.size());
        recommended.addAll(catalog.subList(0, recommendedEnd));
        nearby.addAll(catalog.subList(recommendedEnd, nearbyEnd));
    }

    private boolean restoreOrdering(RecommendationCache.Snapshot snapshot) {
        List<RegionCatalog.Entry> cachedRecommended = resolve(snapshot.recommendedCodes);
        List<RegionCatalog.Entry> cachedNearby = resolve(snapshot.nearbyCodes);
        if (cachedRecommended == null || cachedNearby == null
                || (cachedRecommended.isEmpty() && cachedNearby.isEmpty())) {
            return false;
        }
        recommended.clear();
        nearby.clear();
        distanceLabels.clear();
        recommended.addAll(cachedRecommended);
        nearby.addAll(cachedNearby);
        distanceLabels.putAll(snapshot.distanceLabels);
        return true;
    }

    static String locationStatusText(RegionCatalog.Entry entry) {
        return entry == null ? LOCATION_RESOLVING_TEXT : entry.name;
    }

    private List<RegionCatalog.Entry> resolve(List<String> codes) {
        Map<String, RegionCatalog.Entry> byCode = new HashMap<>();
        for (RegionCatalog.Entry entry : catalog) {
            byCode.put(entry.code, entry);
        }
        List<RegionCatalog.Entry> result = new ArrayList<>(codes.size());
        for (String code : codes) {
            RegionCatalog.Entry entry = byCode.get(code);
            if (entry == null) {
                return null;
            }
            result.add(entry);
        }
        return result;
    }

    private void requestLocation() {
        if (ContextCompat.checkSelfPermission(
                getContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            city.setText("위치 권한 필요");
            ActivityCompat.requestPermissions(
                    navigator.activity(),
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST
            );
            return;
        }
        startLocationLookup();
    }

    private void refreshLocation() {
        stopLocationLookup();
        city.setText(LOCATION_RESOLVING_TEXT);
        requestLocation();
    }

    public void onLocationPermissionResult(int requestCode, int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST) {
            return;
        }
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startLocationLookup();
        } else {
            showLocationFallback();
        }
    }

    private void startLocationLookup() {
        if (locationRequestActive) {
            return;
        }
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            showLocationFallback();
            return;
        }

        Location lastKnown = null;
        boolean providerAvailable = false;
        for (String provider : new String[]{
                LocationManager.GPS_PROVIDER,
                LocationManager.NETWORK_PROVIDER
        }) {
            try {
                if (!locationManager.isProviderEnabled(provider)) {
                    continue;
                }
                providerAvailable = true;
                Location candidate = locationManager.getLastKnownLocation(provider);
                if (candidate != null
                        && (lastKnown == null || candidate.getTime() > lastKnown.getTime())) {
                    lastKnown = candidate;
                }
            } catch (SecurityException ignored) {
                showLocationFallback();
                return;
            }
        }
        if (lastKnown != null
                && LocationMatcher.nearest(
                        catalog,
                        lastKnown.getLatitude(),
                        lastKnown.getLongitude()
                ) != null) {
            applyLocation(lastKnown);
            return;
        }
        if (!providerAvailable) {
            showLocationFallback();
            return;
        }

        locationRequestActive = true;
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                finishLocation(location);
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };
        try {
            for (String provider : new String[]{
                    LocationManager.GPS_PROVIDER,
                    LocationManager.NETWORK_PROVIDER
            }) {
                if (locationManager.isProviderEnabled(provider)) {
                    locationManager.requestLocationUpdates(
                            provider,
                            0L,
                            0f,
                            locationListener,
                            Looper.getMainLooper()
                    );
                }
            }
        } catch (SecurityException ignored) {
            finishLocation(null);
            return;
        }
        locationTimeout = () -> finishLocation(null);
        locationHandler.postDelayed(locationTimeout, LOCATION_TIMEOUT_MS);
    }

    private void finishLocation(Location location) {
        if (location == null) {
            stopLocationLookup();
            showLocationFallback();
            return;
        }
        if (LocationMatcher.nearest(
                catalog,
                location.getLatitude(),
                location.getLongitude()
        ) == null) {
            return;
        }
        stopLocationLookup();
        applyLocation(location);
    }

    private void stopLocationLookup() {
        if (locationManager != null && locationListener != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException ignored) {
            }
        }
        if (locationTimeout != null) {
            locationHandler.removeCallbacks(locationTimeout);
            locationTimeout = null;
        }
        locationListener = null;
        locationRequestActive = false;
    }

    private void applyLocation(Location location) {
        LocationMatcher.Match match = LocationMatcher.nearest(
                catalog,
                location.getLatitude(),
                location.getLongitude()
        );
        if (match == null) {
            showLocationFallback();
            return;
        }
        List<LocationMatcher.Match> sorted = LocationMatcher.sortedByDistance(
                catalog,
                location.getLatitude(),
                location.getLongitude()
        );
        recommended.clear();
        nearby.clear();
        distanceLabels.clear();
        int recommendedEnd = Math.min(CARDS_PER_RAIL, sorted.size());
        int nearbyEnd = Math.min(recommendedEnd + CARDS_PER_RAIL, sorted.size());
        for (int i = 0; i < sorted.size(); i++) {
            LocationMatcher.Match item = sorted.get(i);
            if (i < recommendedEnd) {
                recommended.add(item.entry);
            } else if (i < nearbyEnd) {
                nearby.add(item.entry);
            }
            distanceLabels.put(item.entry.code, formatDistance(item.distanceKm));
        }
        city.setText(locationStatusText(match.entry));
        RecommendationCache.write(
                recommendationPreferences,
                match.entry.name,
                recommended,
                nearby,
                distanceLabels
        );
        rebuildCards();
    }

    private void showLocationFallback() {
        stopLocationLookup();
        city.setText(LOCATION_UNAVAILABLE_TEXT);
        RecommendationCache.Snapshot cached = RecommendationCache.read(
                recommendationPreferences
        );
        if (cached == null || !restoreOrdering(cached)) {
            resetOrdering();
        }
        rebuildCards();
    }

    private String formatDistance(double distanceKm) {
        if (distanceKm < 1.0) {
            return "<1km away";
        }
        return Math.round(distanceKm) + "km away";
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

    private void rebuildCards() {
        if (recommendedList == null || nearList == null) {
            return;
        }
        Context context = getContext();
        fillRow(recommendedList, recommended, context);
        fillRow(nearList, nearby, context);
    }

    private void fillRow(
            HorizontalScrollView scroll,
            List<RegionCatalog.Entry> destinations,
            Context context
    ) {
        LinearLayout row = (LinearLayout) scroll.getChildAt(0);
        row.removeAllViews();
        List<RegionCatalog.Entry> matchingEntries = RegionCatalog.filter(destinations, query);
        int matchCount = 0;
        for (RegionCatalog.Entry destination : matchingEntries) {
            row.addView(new TravelCard(
                            context,
                            destination,
                            navigator,
                            distanceLabels.get(destination.code)
                    ),
                    ViewUtils.linear(ViewUtils.dp(context, 300), ViewUtils.dp(context, 260)));
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams)
                    row.getChildAt(row.getChildCount() - 1).getLayoutParams();
            params.setMargins(ViewUtils.dp(context, 0), 0, ViewUtils.dp(context, 16), 0);
            matchCount++;
        }
        if (matchCount == 0) {
            TextView empty = ViewUtils.text(context, "No destinations found", colors.muted, 15,
                    Typeface.NORMAL, Gravity.CENTER);
            row.addView(empty, ViewUtils.linear(ViewUtils.dp(context, 300), ViewUtils.dp(context, 260)));
        }
    }

    static final class TravelCard extends FrameLayout {
        TravelCard(
                Context context,
                RegionCatalog.Entry destination,
                AppNavigator navigator,
                String distance
        ) {
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
            Bitmap bitmap = ViewUtils.assetBitmap(context, destination.image);
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
                    navigator.favorites().contains(destination.code));
            heart.setOnClickListener(v -> {
                navigator.toggleFavorite(destination.code);
                heart.setSelectedState(navigator.favorites().contains(destination.code));
            });
            FrameLayout.LayoutParams heartParams = new FrameLayout.LayoutParams(
                    ViewUtils.dp(context, 44),
                    ViewUtils.dp(context, 44),
                    Gravity.TOP | Gravity.RIGHT
            );
            heartParams.topMargin = ViewUtils.dp(context, 8);
            heartParams.rightMargin = ViewUtils.dp(context, 8);
            addView(heart, heartParams);

            TextView distanceText = ViewUtils.text(
                    context,
                    distance == null ? "" : distance,
                    Color.WHITE,
                    11,
                    Typeface.BOLD, Gravity.LEFT);
            FrameLayout.LayoutParams distanceParams = new FrameLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT,
                    ViewUtils.dp(context, 28),
                    Gravity.BOTTOM | Gravity.LEFT
            );
            distanceParams.leftMargin = ViewUtils.dp(context, 14);
            distanceParams.bottomMargin = ViewUtils.dp(context, 42);
            addView(distanceText, distanceParams);

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

    @Override
    protected void onDetachedFromWindow() {
        stopLocationLookup();
        super.onDetachedFromWindow();
    }
}
