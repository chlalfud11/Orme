package com.orme.app.ui.diary;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/** 기록 이름이 저장 모델에 함께 전달되는지 확인한다. */
public final class DiaryRecordTest {
    @Test
    public void recordKeepsDisplayName() {
        DiaryRecord record = new DiaryRecord(
                "11",
                "record-1",
                "My first trip",
                "/tmp/cover.png",
                Collections.emptyList()
        );

        assertEquals("My first trip", record.name);
    }
}
