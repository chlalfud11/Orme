package com.orme.app.ui.diary;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;

import com.orme.app.R;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 다이어리 꾸미기 요소 모델. */
interface DiaryElement {
    long id();
}

final class TextElement implements DiaryElement {
    final long id;
    String text;
    final int color;
    final float fontSizePx;

    TextElement(long id, String text, int color, float fontSizePx) {
        this.id = id;
        this.text = text;
        this.color = color;
        this.fontSizePx = fontSizePx;
    }

    @Override
    public long id() {
        return id;
    }
}

final class StickerElement implements DiaryElement {
    final long id;
    final String emoji;
    final float sizePx;

    StickerElement(long id, String emoji, float sizePx) {
        this.id = id;
        this.emoji = emoji;
        this.sizePx = sizePx;
    }

    @Override
    public long id() {
        return id;
    }
}

final class ImageElement implements DiaryElement {
    final long id;
    final Bitmap bitmap;
    final float widthPx;
    final float heightPx;

    ImageElement(long id, Bitmap bitmap, float widthPx, float heightPx) {
        this.id = id;
        this.bitmap = bitmap;
        this.widthPx = widthPx;
        this.heightPx = heightPx;
    }

    @Override
    public long id() {
        return id;
    }
}

final class StrokeElement implements DiaryElement {
    final long id;
    final List<PointF> points;
    final int color;
    final float widthPx;

    StrokeElement(long id, List<PointF> points, int color, float widthPx) {
        this.id = id;
        this.points = points;
        this.color = color;
        this.widthPx = widthPx;
    }

    @Override
    public long id() {
        return id;
    }
}

/** 내지 배경 스타일. */
enum PageTemplate {
    GRID("모눈", 0xFFF6F1DE),
    LINED("줄노트", 0xFFF6F1DE),
    DOT("도트", 0xFFF6F1DE),
    BLANK("무지", 0xFFF6F1DE),
    GRID_WHITE("흰 모눈", 0xFFFFFFFF),
    LINED_WHITE("흰 줄노트", 0xFFFFFFFF),
    DOT_WHITE("흰 도트", 0xFFFFFFFF),
    BLANK_WHITE("흰 무지", 0xFFFFFFFF);

    final String label;
    final int paperColor;

    PageTemplate(String label, int paperColor) {
        this.label = label;
        this.paperColor = paperColor;
    }
}

/** 표지 이미지. */
enum CoverTemplate {
    FOREST("카모", R.drawable.cover_1),
    SUNSET("체크", R.drawable.cover_2),
    CREAM("리본", R.drawable.cover_3),
    ROSE("트위드", R.drawable.cover_4),
    OCEAN("크라프트", R.drawable.cover_5),
    KRAFT("딸기", R.drawable.cover_6);

    final String label;
    final int imageRes;

    CoverTemplate(String label, int imageRes) {
        this.label = label;
        this.imageRes = imageRes;
    }
}

final class PageState {
    private static final float MIN_ELEMENT_SCALE = 0.5f;
    private static final float MAX_ELEMENT_SCALE = 3f;
    final PageTemplate template;
    final List<DiaryElement> elements = new ArrayList<>();
    final Map<Long, PointF> positions = new HashMap<>();
    final Map<Long, Float> scales = new HashMap<>();
    final ArrayDeque<ElementSnapshot> redoStack = new ArrayDeque<>();

    PageState(PageTemplate template) {
        this.template = template;
    }

    void add(DiaryElement element, PointF position) {
        elements.add(element);
        positions.put(element.id(), new PointF(position.x, position.y));
        scales.put(element.id(), 1f);
        redoStack.clear();
    }

    void updateText(long id, String text) {
        for (int i = 0; i < elements.size(); i++) {
            DiaryElement element = elements.get(i);
            if (element.id() == id && element instanceof TextElement) {
                TextElement old = (TextElement) element;
                elements.set(i, new TextElement(old.id, text, old.color, old.fontSizePx));
                return;
            }
        }
    }

    float scaleOf(long id) {
        Float scale = scales.get(id);
        return scale == null ? 1f : scale;
    }

    void scaleBy(long id, float zoomChange) {
        DiaryElement element = find(id);
        if (element == null || element instanceof StrokeElement) {
            return;
        }
        scales.put(id, Math.max(MIN_ELEMENT_SCALE,
                Math.min(MAX_ELEMENT_SCALE, scaleOf(id) * zoomChange)));
    }

    void undo() {
        if (elements.isEmpty()) {
            return;
        }
        DiaryElement element = elements.remove(elements.size() - 1);
        PointF point = positions.remove(element.id());
        Float scale = scales.remove(element.id());
        redoStack.addLast(new ElementSnapshot(element,
                point == null ? new PointF() : point,
                scale == null ? 1f : scale));
    }

    void redo() {
        ElementSnapshot snapshot = redoStack.pollLast();
        if (snapshot == null) {
            return;
        }
        elements.add(snapshot.element);
        positions.put(snapshot.element.id(), snapshot.position);
        scales.put(snapshot.element.id(), snapshot.scale);
    }

    void remove(long id) {
        elements.removeIf(element -> element.id() == id);
        positions.remove(id);
        scales.remove(id);
    }

    DiaryElement find(long id) {
        for (DiaryElement element : elements) {
            if (element.id() == id) {
                return element;
            }
        }
        return null;
    }

    private static final class ElementSnapshot {
        final DiaryElement element;
        final PointF position;
        final float scale;

        ElementSnapshot(DiaryElement element, PointF position, float scale) {
            this.element = element;
            this.position = position;
            this.scale = scale;
        }
    }
}

final class DiaryRecord {
    final String code;
    final String id;
    final String name;
    final String coverPath;
    final List<String> pagePaths;

    DiaryRecord(
            String code,
            String id,
            String name,
            String coverPath,
            List<String> pagePaths
    ) {
        this.code = code;
        this.id = id;
        this.name = name;
        this.coverPath = coverPath;
        this.pagePaths = pagePaths;
    }
}

final class DiaryStore {
    private DiaryStore() {
    }

    private static File baseDir(Context context, String code) {
        File directory = new File(context.getFilesDir(), "diary/" + code);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    static List<DiaryRecord> listRecords(Context context, String code) {
        File[] records = baseDir(context, code).listFiles(File::isDirectory);
        if (records == null) {
            return new ArrayList<>();
        }
        List<DiaryRecord> result = new ArrayList<>();
        for (File record : records) {
            File[] pageFiles = record.listFiles(file -> file.getName().startsWith("page_"));
            List<File> sortedPages = new ArrayList<>();
            if (pageFiles != null) {
                Collections.addAll(sortedPages, pageFiles);
                sortedPages.sort(Comparator.comparing(File::getName));
            }
            List<String> pages = new ArrayList<>();
            for (File page : sortedPages) {
                pages.add(page.getAbsolutePath());
            }
            File cover = new File(record, "cover.png");
            String coverPath = cover.exists()
                    ? cover.getAbsolutePath()
                    : pages.isEmpty() ? null : pages.get(0);
            if (coverPath != null || !pages.isEmpty()) {
                result.add(new DiaryRecord(
                        code,
                        record.getName(),
                        readRecordName(record),
                        coverPath,
                        pages
                ));
            }
        }
        result.sort((first, second) -> second.id.compareTo(first.id));
        return result;
    }

    static boolean hasAnyRecord(Context context, String code) {
        File[] records = baseDir(context, code).listFiles(File::isDirectory);
        return records != null && records.length > 0;
    }

    static DiaryRecord saveRecord(
            Context context,
            String code,
            String name,
            Bitmap cover,
            List<Bitmap> pages
    )
            throws Exception {
        String id = Long.toString(System.currentTimeMillis());
        File directory = new File(baseDir(context, code), id);
        if (!directory.mkdirs()) {
            throw new IllegalStateException("기록 저장 폴더를 만들 수 없습니다.");
        }
        String displayName = name == null ? "" : name.trim();
        if (displayName.isEmpty()) {
            displayName = "기록 보기";
        }
        File nameFile = new File(directory, "name.txt");
        try (Writer output = new OutputStreamWriter(
                new FileOutputStream(nameFile),
                StandardCharsets.UTF_8
        )) {
            output.write(displayName);
        }
        String coverPath = null;
        if (cover != null) {
            File target = new File(directory, "cover.png");
            try (FileOutputStream output = new FileOutputStream(target)) {
                cover.compress(Bitmap.CompressFormat.PNG, 100, output);
            }
            coverPath = target.getAbsolutePath();
        }
        List<String> pagePaths = new ArrayList<>();
        for (int i = 0; i < pages.size(); i++) {
            File target = new File(directory, String.format("page_%02d.png", i));
            try (FileOutputStream output = new FileOutputStream(target)) {
                pages.get(i).compress(Bitmap.CompressFormat.PNG, 100, output);
            }
            pagePaths.add(target.getAbsolutePath());
        }
        return new DiaryRecord(code, id, displayName, coverPath, pagePaths);
    }

    private static String readRecordName(File record) {
        File nameFile = new File(record, "name.txt");
        if (!nameFile.isFile()) {
            return "기록 보기";
        }
        try (BufferedReader input = new BufferedReader(new InputStreamReader(
                new FileInputStream(nameFile),
                StandardCharsets.UTF_8
        ))) {
            String name = input.readLine();
            if (name != null && !name.trim().isEmpty()) {
                return name.trim();
            }
        } catch (IOException ignored) {
            // 이전 기록은 이름 파일이 없거나 읽을 수 없을 수 있다.
        }
        return "기록 보기";
    }

    static Bitmap loadBitmap(String path) {
        return BitmapFactory.decodeFile(path);
    }

    static void deleteRecord(Context context, DiaryRecord record) {
        deleteRecursively(new File(baseDir(context, record.code), record.id));
    }

    private static void deleteRecursively(File file) {
        File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                deleteRecursively(child);
            }
        }
        file.delete();
    }
}
