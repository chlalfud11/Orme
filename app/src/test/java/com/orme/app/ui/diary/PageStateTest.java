package com.orme.app.ui.diary;

import android.graphics.PointF;
import android.graphics.Color;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public final class PageStateTest {
    @Test
    public void inlineTextAndPinchScaleUpdateTheSameElement() {
        PageState page = new PageState(PageTemplate.BLANK);
        TextElement text = new TextElement(1, "", Color.BLACK, 64f);
        page.add(text, new PointF());

        page.updateText(text.id, "군산");
        page.scaleBy(text.id, 10f);

        assertEquals("군산", ((TextElement) page.elements.get(0)).text);
        assertEquals(3f, page.scaleOf(text.id), 0.001f);
    }
}
