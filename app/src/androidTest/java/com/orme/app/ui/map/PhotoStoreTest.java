package com.orme.app.ui.map;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public final class PhotoStoreTest {
    private static final String TEST_CODE = "__qa_photo_delete__";

    @Test
    public void deleteRemovesStoredRegionPhoto() throws Exception {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        Bitmap bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
        try {
            PhotoStore.save(context, TEST_CODE, bitmap);
            assertTrue(PhotoStore.hasPhoto(context, TEST_CODE));

            PhotoStore.delete(context, TEST_CODE);

            assertFalse(PhotoStore.hasPhoto(context, TEST_CODE));
        } finally {
            PhotoStore.delete(context, TEST_CODE);
            bitmap.recycle();
        }
    }
}
