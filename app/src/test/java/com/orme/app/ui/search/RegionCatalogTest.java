package com.orme.app.ui.search;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class RegionCatalogTest {
    @Test
    public void filterMatchesKoreanAndEnglishNames() {
        RegionCatalog.Entry gunsan = entry("35010", "군산시", "Gunsan-si");
        RegionCatalog.Entry jeonju = entry("35110", "전주시", "Jeonju-si");
        List<RegionCatalog.Entry> catalog = Arrays.asList(gunsan, jeonju);

        assertEquals(Arrays.asList(gunsan), RegionCatalog.filter(catalog, "군산"));
        assertEquals(Arrays.asList(gunsan), RegionCatalog.filter(catalog, "gunsan"));
        assertEquals(Arrays.asList(jeonju), RegionCatalog.filter(catalog, "JEONJU"));
    }

    private static RegionCatalog.Entry entry(String code, String name, String nameEng) {
        return new RegionCatalog.Entry(code, code.substring(0, 2), name, nameEng,
                0, 0, "jeonju.jpg");
    }
}
