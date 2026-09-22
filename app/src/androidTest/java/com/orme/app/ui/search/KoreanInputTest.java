package com.orme.app.ui.search;

import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.orme.app.MainActivity;
import com.orme.app.ui.components.ViewUtils;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
public final class KoreanInputTest {
    @Test
    public void resolvedLocationStatusUsesActualRegionName() {
        RegionCatalog.Entry seoul = new RegionCatalog.Entry(
                "11",
                "11",
                "서울특별시",
                "Seoul",
                37.5665,
                126.9780,
                "region_11.jpg"
        );

        assertEquals("서울특별시", TravelSearchScreen.locationStatusText(seoul));
    }

    @Test
    public void textFieldCommitsHangulThroughImeConnection() {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                EditText input = new EditText(activity);
                ViewUtils.enableKoreanInput(input);
                EditorInfo editorInfo = new EditorInfo();
                InputConnection connection = input.onCreateInputConnection(editorInfo);

                assertNotNull("텍스트 입력 연결을 만들 수 있어야 합니다.", connection);
                assertEquals(
                        Locale.KOREA.toLanguageTag(),
                        editorInfo.hintLocales.get(0).toLanguageTag()
                );
                connection.setComposingText("한", 1);
                connection.setComposingText("한글", 1);
                connection.finishComposingText();
                assertEquals("한글", input.getText().toString());
            });
        }
    }
}
