package com.orme.app.ui.map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.os.Build;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/** 지역 모양 사진을 앱 내부 저장소에 PNG로 보관한다. */
public final class PhotoStore {
    private PhotoStore() {
    }

    private static File directory(Context context) {
        File directory = new File(context.getFilesDir(), "map_photos");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        return directory;
    }

    private static File file(Context context, String code) {
        return new File(directory(context), code + ".png");
    }

    public static boolean hasPhoto(Context context, String code) {
        return file(context, code).exists();
    }

    public static void delete(Context context, String code) {
        File target = file(context, code);
        if (target.exists()) {
            target.delete();
        }
    }

    public static Bitmap loadShaped(Context context, String code) {
        File target = file(context, code);
        return target.exists() ? BitmapFactory.decodeFile(target.getAbsolutePath()) : null;
    }

    public static Bitmap decode(Context context, Uri uri) {
        try {
            if (Build.VERSION.SDK_INT >= 28) {
                ImageDecoder.Source source = ImageDecoder.createSource(context.getContentResolver(), uri);
                return ImageDecoder.decodeBitmap(source, (decoder, info, src) -> {
                    decoder.setAllocator(ImageDecoder.ALLOCATOR_SOFTWARE);
                    decoder.setMutableRequired(true);
                });
            }
            InputStream input = context.getContentResolver().openInputStream(uri);
            if (input == null) {
                return null;
            }
            try (InputStream stream = input) {
                return BitmapFactory.decodeStream(stream);
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    public static Bitmap cropToRegion(Bitmap source, MapRegion region) {
        RectF bounds = region.bounds;
        float scale = 1200f / Math.max(bounds.width(), bounds.height());
        int width = Math.max(1, Math.round(bounds.width() * scale));
        int height = Math.max(1, Math.round(bounds.height() * scale));
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        float sourceRatio = (float) source.getWidth() / source.getHeight();
        float destinationRatio = (float) width / height;
        Rect sourceRect;
        if (sourceRatio > destinationRatio) {
            int croppedWidth = Math.round(source.getHeight() * destinationRatio);
            int left = (source.getWidth() - croppedWidth) / 2;
            sourceRect = new Rect(left, 0, left + croppedWidth, source.getHeight());
        } else {
            int croppedHeight = Math.round(source.getWidth() / destinationRatio);
            int top = (source.getHeight() - croppedHeight) / 2;
            sourceRect = new Rect(0, top, source.getWidth(), top + croppedHeight);
        }

        Path local = new Path(region.path);
        Matrix pathMatrix = new Matrix();
        pathMatrix.setScale(scale, scale);
        pathMatrix.preTranslate(-bounds.left, -bounds.top);
        local.transform(pathMatrix);

        Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        canvas.drawPath(local, maskPaint);
        Paint photoPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        photoPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, sourceRect, new RectF(0f, 0f, width, height), photoPaint);
        return output;
    }

    public static Bitmap cropWithPlacement(
            Bitmap source,
            MapRegion region,
            float imageScale,
            float offsetX,
            float offsetY
    ) {
        RectF bounds = region.bounds;
        float scale = 1200f / Math.max(bounds.width(), bounds.height());
        int width = Math.max(1, Math.round(bounds.width() * scale));
        int height = Math.max(1, Math.round(bounds.height() * scale));
        Bitmap output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        Path local = new Path(region.path);
        Matrix pathMatrix = new Matrix();
        pathMatrix.setScale(scale, scale);
        pathMatrix.preTranslate(-bounds.left, -bounds.top);
        local.transform(pathMatrix);
        canvas.drawPath(local, new Paint(Paint.ANTI_ALIAS_FLAG));

        float sourceScale = Math.max(
                width / (float) Math.max(1, source.getWidth()),
                height / (float) Math.max(1, source.getHeight())
        );
        Matrix photoMatrix = new Matrix();
        float drawScale = sourceScale * imageScale;
        photoMatrix.setScale(drawScale, drawScale);
        float drawWidth = source.getWidth() * drawScale;
        float drawHeight = source.getHeight() * drawScale;
        photoMatrix.postTranslate(
                (width - drawWidth) / 2f + offsetX * scale,
                (height - drawHeight) / 2f + offsetY * scale
        );
        Paint photoPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
        photoPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(source, photoMatrix, photoPaint);
        return output;
    }

    public static void save(Context context, String code, Bitmap shaped) throws Exception {
        try (FileOutputStream output = new FileOutputStream(file(context, code))) {
            shaped.compress(Bitmap.CompressFormat.PNG, 100, output);
        }
    }

    public static Bitmap cropSaveAndReturn(
            Context context,
            String code,
            Bitmap source,
            MapRegion region
    ) throws Exception {
        Bitmap shaped = cropToRegion(source, region);
        save(context, code, shaped);
        return shaped;
    }

    public static Bitmap placeSaveAndReturn(
            Context context,
            String code,
            Bitmap source,
            MapRegion region,
            float imageScale,
            float offsetX,
            float offsetY
    ) throws Exception {
        Bitmap shaped = cropWithPlacement(source, region, imageScale, offsetX, offsetY);
        save(context, code, shaped);
        return shaped;
    }
}
