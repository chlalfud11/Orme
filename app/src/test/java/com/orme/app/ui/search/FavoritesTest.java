package com.orme.app.ui.search;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class FavoritesTest {
    @Test
    public void favoriteDestinationsKeepsSavedDestinationsInCatalogOrder() {
        List<TravelSearchScreen.Destination> expected = Arrays.asList(
                TravelSearchScreen.RECOMMENDED.get(0),
                TravelSearchScreen.NEARBY.get(2)
        );
        assertEquals(
                expected,
                FavoritesScreen.favoriteDestinations(new HashSet<>(Arrays.asList("Jeonju", "Gunsan")))
        );
    }
}
