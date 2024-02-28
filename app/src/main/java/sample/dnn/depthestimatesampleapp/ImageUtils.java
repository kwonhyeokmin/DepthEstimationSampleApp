package sample.dnn.depthestimatesampleapp;

import android.graphics.Bitmap;

import androidx.camera.core.ImageProxy;

import java.nio.FloatBuffer;

public class ImageUtils {

    public static Bitmap imageToBitmap(ImageProxy imageProxy, int width, int height) {
        Bitmap bitmap = imageProxy.toBitmap();
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    public static FloatBuffer bitmapToFloatBuffer(Bitmap bitmap) {
        float imageSTD = 255f;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int channel = 3;

        FloatBuffer floatBuffer = FloatBuffer.allocate(channel * height * width);
        floatBuffer.rewind();

        int area = height * width;
        int[] bitmapData = new int[area];
        // input RGB to array
        bitmap.getPixels(bitmapData, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());
        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                int idx = height * j + i;
                int pixelValue = bitmapData[idx];
                // normalize with 0~255
                floatBuffer.put(idx, ((pixelValue >> 16) & 0xff) / imageSTD);
                floatBuffer.put(idx + area, ((pixelValue) & 0xff) / imageSTD);
                floatBuffer.put(idx + area * 2, ((pixelValue) & 0xff) / imageSTD);
            }
        }
        floatBuffer.rewind();
        return floatBuffer;
    }

}
