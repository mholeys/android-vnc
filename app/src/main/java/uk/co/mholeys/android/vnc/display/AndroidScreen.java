package uk.co.mholeys.android.vnc.display;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import uk.co.mholeys.vnc.display.IScreen;
import uk.co.mholeys.vnc.display.UpdateManager;
import uk.co.mholeys.vnc.display.data.CopyScreenUpdate;
import uk.co.mholeys.vnc.display.data.FillScreenUpdate;
import uk.co.mholeys.vnc.display.data.JPEGScreenUpdate;
import uk.co.mholeys.vnc.display.data.PaletteScreenUpdate;
import uk.co.mholeys.vnc.display.data.RawScreenUpdate;
import uk.co.mholeys.vnc.display.data.ScreenUpdate;
import uk.co.mholeys.vnc.log.Logger;

/**
 * Created by Matthew on 25/09/2016.
 */
public class AndroidScreen implements IScreen {

    AndroidDisplay display;
    public static Bitmap bitmap;
    public UpdateManager updateManager;
    private int width;
    private int height;
    public double zoomScale = 1;
    public double cutWidth = 0;
    public double cutHeight = 0;
    public float cutX = 0;
    public float cutY = 0;
    int[] pixels;

    public AndroidScreen(int width, int height) {
        setSize(width, height);
    }

    @Override
    public void drawPixels(int x, int y, int width, int height, int[] pixels) {
        for (int yA = y; yA < y + height; yA++) {
            for (int xA = x; xA < x + width; xA++) {
                bitmap.setPixel(xA, yA, pixels[(xA - x) + ((yA - y) * width)]);
            }
        }
    }

    @Override
    public void drawPalette(int x, int y, int width, int height, int[] palette, int paletteSize, byte[] data) {
        if (2 == paletteSize) {
            int dx, dy, n;
            int i = y * this.width + x;
            int rowBytes = (width + 7) / 8;
            byte b;

            for (dy = 0; dy < height; dy++) {
                for (dx = 0; dx < width / 8; dx++) {
                    b = data[dy * rowBytes + dx];
                    for (n = 7; n >= 0; n--) {
                        x = i % this.width;
                        y = i / this.width;
                        bitmap.setPixel(x, y, palette[b >> n & 1]);
                    }
                }
                for (n = 7; n >= 8 - width % 8; n--) {
                    bitmap.setPixel(x, y, palette[data[dy * rowBytes + dx] >> n & 1]);
                }
                i += this.width - width;
            }
        } else {
            // 3..255 colors (assuming bytesPixel == 4).
            int i = 0;
            for (int ly = y; ly < y + height; ++ly) {
                for (int lx = x; lx < x + width; ++lx) {
                    int d = data[i++] & 0xFF;
                    bitmap.setPixel(lx, ly, palette[d]);
                }
            }
        }
    }

    @Override
    public void drawJPEG(int x, int y, int width, int height, byte[] data) {
        Bitmap jpeg = BitmapFactory.decodeByteArray(data, 0, data.length);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(jpeg, x, y, new Paint());
        jpeg.recycle();
    }

    @Override
    public void copyPixels(int x, int y, int width, int height, int srcX, int srcY) {
        Canvas canvas = new Canvas(bitmap);
        int[] copied = new int[ width * height];
        bitmap.getPixels(copied, 0, bitmap.getWidth(), x, y, width, height);
        Bitmap sub = Bitmap.createBitmap(copied, 0, width, width, height, Bitmap.Config.ARGB_8888);
        canvas.drawBitmap(sub, x, y, new Paint());
    }

    @Override
    public void fillPixels(int x, int y, int width, int height, int color) {
        Canvas canvas = new Canvas(bitmap);
        Paint c = new Paint();
        c.setColor(color);
        canvas.drawRect(x, y, width, height, c);
    }

    public void update() {
        display.invalidate();
    }

    @Override
    public int[] getPixels() {
        return new int[0];
    }

    @Override
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setHasAlpha(false);
        pixels = new int[width * height];
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public void drawCursor(int x, int y, int w, int h, byte[] bytes) {
        Canvas canvas = new Canvas(bitmap);
        Paint cursor = new Paint();
        cursor.setColor(0xFF00FF);
        canvas.drawRect(x, y, 10, 10, cursor);
    }

    public void process() {
        while (updateManager.hasUpdates()) {
            ScreenUpdate update = updateManager.getUpdate();
            if (update == null) continue;
            int x = update.x;
            int y = update.y;
            int w = update.width;
            int h = update.height;
            Logger.logger.printLn(update.getClass().toString() + " x:" + x + " y:" + y + " w:" + w + " h:" + h);

            int width = update.width;
            int height = update.height;
            bitmap.prepareToDraw();
            if (update instanceof RawScreenUpdate) {
                RawScreenUpdate raw = (RawScreenUpdate) update;
                drawPixels(x, y, width, height, raw.pixels);
            } else if (update instanceof PaletteScreenUpdate) {
                PaletteScreenUpdate palette = (PaletteScreenUpdate) update;
                drawPalette(x, y, width, height, palette.palette, palette.paletteSize, palette.data);
            } else if (update instanceof JPEGScreenUpdate) {
                JPEGScreenUpdate jpeg = (JPEGScreenUpdate) update;
                drawJPEG(x, y, width, height, jpeg.jpegData);
            } else if (update instanceof CopyScreenUpdate) {
                CopyScreenUpdate copy = (CopyScreenUpdate) update;
                copyPixels(copy.xSrc, copy.ySrc, width, height, x, y);
            } else if (update instanceof FillScreenUpdate) {
                FillScreenUpdate fill = (FillScreenUpdate) update;
                fillPixels(x, y, width, height, fill.pixel);
            }
            display.postInvalidate();
        }
    }

}
