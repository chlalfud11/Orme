package com.orme.app.ui.diary;

import android.graphics.Bitmap;
import android.graphics.Path;
import android.graphics.RectF;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.test.core.app.ActivityScenario;

import com.orme.app.MainActivity;
import com.orme.app.navigation.AppNavigator;
import com.orme.app.ui.components.ViewUtils;
import com.orme.app.ui.map.MapRegion;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** 기록물 확인 화면의 상단 뒤로가기 버튼 상호작용을 검증한다. */
public final class RegionDiaryFlowTest {
    @Test
    public void viewerBackButtonStaysAboveScrollablePages() throws Exception {
        try (ActivityScenario<MainActivity> scenario = ActivityScenario.launch(MainActivity.class)) {
            scenario.onActivity(activity -> {
                String code = "viewer-back-button-test";
                Bitmap bitmap = Bitmap.createBitmap(2, 2, Bitmap.Config.ARGB_8888);
                DiaryRecord record = null;
                try {
                    try {
                        record = DiaryStore.saveRecord(
                                activity,
                                code,
                                "뒤로가기 테스트",
                                bitmap,
                                Collections.singletonList(bitmap)
                        );
                    } catch (Exception error) {
                        throw new AssertionError(error);
                    }
                    FrameLayout host = new FrameLayout(activity);
                    AppNavigator navigator = new AppNavigator(activity, host);
                    Path path = new Path();
                    path.addRect(0, 0, 1, 1, Path.Direction.CW);
                    MapRegion region = new MapRegion(
                            code,
                            "테스트 지역",
                            "Test",
                            path,
                            new RectF(0, 0, 1, 1),
                            false
                    );
                    RegionDiaryFlow flow = new RegionDiaryFlow(
                            activity,
                            navigator,
                            region,
                            v -> {
                            }
                    );

                    HorizontalScrollView rail = (HorizontalScrollView) flow.getChildAt(0);
                    LinearLayout entries = (LinearLayout) rail.getChildAt(0);
                    LinearLayout entry = (LinearLayout) entries.getChildAt(0);
                    View card = entry.getChildAt(0);
                    card.performClick();

                    assertTrue(
                            "뷰어 뒤로가기 버튼이 스크롤 콘텐츠 위에 있어야 합니다.",
                            flow.getChildAt(flow.getChildCount() - 1) instanceof ImageButton
                    );
                    ImageButton back = (ImageButton) flow.getChildAt(flow.getChildCount() - 1);
                    assertTrue(back.isClickable());
                    FrameLayout.LayoutParams backParams =
                            (FrameLayout.LayoutParams) back.getLayoutParams();
                    assertEquals(ViewUtils.dp(activity, 34), backParams.width);
                    assertEquals(ViewUtils.dp(activity, 34), backParams.height);
                    assertEquals(ViewUtils.dp(activity, 20), backParams.leftMargin);
                    assertEquals(ViewUtils.dp(activity, 64), backParams.topMargin);
                    back.performClick();
                    assertTrue(flow.getChildAt(0) instanceof HorizontalScrollView);
                } finally {
                    if (record != null) {
                        DiaryStore.deleteRecord(activity, record);
                    }
                    bitmap.recycle();
                }
            });
        }
    }
}
