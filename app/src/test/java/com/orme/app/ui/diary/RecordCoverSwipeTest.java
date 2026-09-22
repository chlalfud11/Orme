package com.orme.app.ui.diary;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class RecordCoverSwipeTest {
    @Test
    public void upwardDragRevealsOnlyTheBoundedDeleteAction() {
        assertEquals(-84f, RegionDiaryFlow.nextRecordCoverOffset(0f, -180f, 84f), 0.001f);
        assertEquals(0f, RegionDiaryFlow.nextRecordCoverOffset(-84f, 140f, 84f), 0.001f);
    }

    @Test
    public void deleteIconAppearsOnlyWhenCoverMovesUp() {
        assertFalse(RegionDiaryFlow.shouldShowDeleteIcon(0f));
        assertFalse(RegionDiaryFlow.shouldShowDeleteIcon(12f));
        assertTrue(RegionDiaryFlow.shouldShowDeleteIcon(-1f));
    }

    @Test
    public void centeredRecordGetsLargerThanARecordOneCardAway() {
        assertEquals(1.24f, RegionDiaryFlow.recordScaleForDistance(0f, 100f), 0.001f);
        assertEquals(1.12f, RegionDiaryFlow.recordScaleForDistance(50f, 100f), 0.001f);
        assertEquals(1.00f, RegionDiaryFlow.recordScaleForDistance(100f, 100f), 0.001f);
    }

    @Test
    public void nearestRecordTargetKeepsTheRecordCenteredWithinScrollBounds() {
        assertEquals(0, RegionDiaryFlow.recordScrollTarget(100, 210, 410, 500));
        assertEquals(226, RegionDiaryFlow.recordScrollTarget(326, 210, 410, 500));
        assertEquals(500, RegionDiaryFlow.recordScrollTarget(826, 210, 410, 500));
    }
}
