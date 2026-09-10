package com.orme.app.ui.profile;

import android.graphics.PointF;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class ProfilePhotoTransformTest {
    @Test
    public void pinchTransformKeepsThePhotoInsideItsCircularFrame() {
        PointF pan = new PointF();
        pan.x = 1_000f;
        pan.y = -1_000f;
        ProfilePhotoTransform enlarged = new ProfilePhotoTransform().updated(
                4f,
                pan,
                200f
        );

        assertEquals(3f, enlarged.scale, 0.001f);
        assertEquals(200f, enlarged.offsetX, 0.001f);
        assertEquals(-200f, enlarged.offsetY, 0.001f);

        ProfilePhotoTransform reset = enlarged.updated(0.01f, new PointF(), 200f);
        assertEquals(1f, reset.scale, 0.001f);
        assertEquals(0f, reset.offsetX, 0.001f);
        assertEquals(0f, reset.offsetY, 0.001f);
    }
}
