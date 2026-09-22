package com.orme.app.ui.search;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class FavoritesTest {
    @Test
    public void favoriteDestinationsKeepsSavedDestinationsInCatalogOrder() {
        RegionCatalog.Entry jeonju = entry("35110", "전주시", "Jeonju-si");
        RegionCatalog.Entry gunsan = entry("35010", "군산시", "Gunsan-si");
        List<RegionCatalog.Entry> catalog = Arrays.asList(
                jeonju,
                entry("37020", "경주시", "Gyeongju-si"),
                gunsan
        );
        List<RegionCatalog.Entry> expected = Arrays.asList(jeonju, gunsan);
        assertEquals(
            expected,
            FavoritesScreen.favoriteDestinations(
                    catalog,
                    new HashSet<>(Arrays.asList("35110", "35010"))
            )
        );
    }

    private static RegionCatalog.Entry entry(String code, String name, String nameEng) {
        return new RegionCatalog.Entry(code, code.substring(0, 2), name, nameEng,
                0, 0, "jeonju.jpg");
    }
}
