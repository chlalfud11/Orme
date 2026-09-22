package com.orme.app.ui.search;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** 기기 좌표를 지역 카탈로그의 대표 좌표와 비교한다. */
public final class LocationMatcher {
    private static final double MAX_MATCH_DISTANCE_KM = 180.0;

    private LocationMatcher() {
    }

    public static Match nearest(List<RegionCatalog.Entry> entries, double latitude, double longitude) {
        Match nearest = null;
        for (RegionCatalog.Entry entry : entries) {
            double distance = distanceKm(latitude, longitude, entry.latitude, entry.longitude);
            if (nearest == null || distance < nearest.distanceKm) {
                nearest = new Match(entry, distance);
            }
        }
        return nearest == null || nearest.distanceKm > MAX_MATCH_DISTANCE_KM ? null : nearest;
    }

    public static List<Match> sortedByDistance(
            List<RegionCatalog.Entry> entries,
            double latitude,
            double longitude
    ) {
        List<Match> result = new ArrayList<>();
        for (RegionCatalog.Entry entry : entries) {
            result.add(new Match(
                    entry,
                    distanceKm(latitude, longitude, entry.latitude, entry.longitude)
            ));
        }
        result.sort(Comparator.comparingDouble(match -> match.distanceKm));
        return result;
    }

    public static double distanceKm(
            double latitude1,
            double longitude1,
            double latitude2,
            double longitude2
    ) {
        double earthRadiusKm = 6371.0;
        double latDelta = Math.toRadians(latitude2 - latitude1);
        double lonDelta = Math.toRadians(longitude2 - longitude1);
        double a = Math.sin(latDelta / 2) * Math.sin(latDelta / 2)
                + Math.cos(Math.toRadians(latitude1))
                * Math.cos(Math.toRadians(latitude2))
                * Math.sin(lonDelta / 2)
                * Math.sin(lonDelta / 2);
        return earthRadiusKm * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }

    public static final class Match {
        public final RegionCatalog.Entry entry;
        public final double distanceKm;

        Match(RegionCatalog.Entry entry, double distanceKm) {
            this.entry = entry;
            this.distanceKm = distanceKm;
        }
    }
}
