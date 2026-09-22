package com.orme.app.ui.search;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public final class LocationMatcherTest {
    @Test
    public void nearestReturnsClosestRegionAndDistance() {
        RegionCatalog.Entry gunsan = entry("35020", "군산시", 35.874685, 126.443360);
        RegionCatalog.Entry gyeongju = entry("37020", "경주시", 35.811257, 129.294894);

        LocationMatcher.Match match = LocationMatcher.nearest(
                Arrays.asList(gunsan, gyeongju),
                35.88,
                126.45
        );

        assertEquals(gunsan, match.entry);
        assertTrue(match.distanceKm < 5.0);
    }

    @Test
    public void farAwayCoordinatesUseFallback() {
        List<RegionCatalog.Entry> entries = Arrays.asList(
                entry("35020", "군산시", 35.874685, 126.443360)
        );

        assertNull(LocationMatcher.nearest(entries, 0, 0));
    }

    @Test
    public void gunsanCoordinatesDoNotResolveToSeocheon() {
        RegionCatalog.Entry gunsan = entry("35020", "군산시", 35.967, 126.736);
        RegionCatalog.Entry seocheon = entry("34340", "서천군", 36.091751, 126.638351);

        LocationMatcher.Match match = LocationMatcher.nearest(
                Arrays.asList(gunsan, seocheon),
                35.967,
                126.736
        );

        assertEquals(gunsan, match.entry);
    }

    private static RegionCatalog.Entry entry(
            String code,
            String name,
            double latitude,
            double longitude
    ) {
        return new RegionCatalog.Entry(
                code,
                code.substring(0, 2),
                name,
                name,
                latitude,
                longitude,
                "gunsan.jpg"
        );
    }
}
