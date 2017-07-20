package uk.co.mholeys.android.vnc.display;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.provider.Settings;
import android.util.Log;

import java.util.NoSuchElementException;

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

    long last = 0;
    long delta = 0;
    int fps = 0;

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
        fpsCounter();
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
        fpsCounter();
    }

    @Override
    public void drawJPEG(int x, int y, int width, int height, byte[] data) {
        Bitmap jpeg = BitmapFactory.decodeByteArray(data, 0, data.length);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(jpeg, x, y, new Paint());
        jpeg.recycle();
        fpsCounter();
    }

    @Override
    public void copyPixels(int x, int y, int width, int height, int srcX, int srcY) {
        Canvas canvas = new Canvas(bitmap);
        int[] copied = new int[ width * height];
        bitmap.getPixels(copied, 0, bitmap.getWidth(), x, y, width, height);
        Bitmap sub = Bitmap.createBitmap(copied, 0, width, width, height, Bitmap.Config.ARGB_8888);
        canvas.drawBitmap(sub, x, y, new Paint());
        sub.recycle();
        fpsCounter();
    }

    @Override
    public void fillPixels(int x, int y, int width, int height, int color) {
        Canvas canvas = new Canvas(bitmap);
        Paint c = new Paint();
        c.setColor(color);
        canvas.drawRect(x, y, x+width, y+height, c);
        fpsCounter();
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
        if (updateManager.hasUpdates()) {
            /*Thread t = new Thread(new Runnable() {
                @Override
                public void run() {*/
                    while (updateManager.hasUpdates()) {
                        ScreenUpdate update = null;
                        try {
                            update = updateManager.getUpdate();
                        } catch (NoSuchElementException e) {
                            continue;
                        }
                        if (update == null) return;
                        int x = update.x;
                        int y = update.y;
                        int w = update.width;
                        int h = update.height;
                        Logger.logger.printLn(update.getClass().toString() + " x:" + x + " y:" + y + " w:" + w + " h:" + h);

                        try {
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
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d("Screen", "Failed to render " + update.getClass().getCanonicalName() + " x:" + x + " y:" + y + " w:" + w + " h:" + h);
                        }
                    }
                    display.postInvalidate();
                /*}
            });
            t.start();*/
        }
    }

    public void fpsCounter() {
        /*if (last == 0) {
            last = System.currentTimeMillis();
        }
        long now = System.currentTimeMillis();
        if (delta > 1000) {
            delta -= 1000;
            fps = 0;
        } else {
            delta += now - last;
        }
        last = now;
        fps++;
        Canvas c = new Canvas(bitmap);
        Paint paint = new Paint();
        Paint font = new Paint();

        paint.setColor(Color.WHITE); // Back Color
        font.setColor(Color.BLACK); // Text Color

        paint.setStrokeWidth(12);
        font.setStrokeWidth(12); // Text Size
        font.setTextSize(30f);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));
        font.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER));

        c.drawRect(100, 25, 200, 50, paint);
        c.drawText("FPS: " + fps, 100, 50, font);
        System.out.println(fps);*/
    }

}
