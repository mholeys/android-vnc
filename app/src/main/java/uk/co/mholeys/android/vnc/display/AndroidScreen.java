package uk.co.mholeys.android.vnc.display;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
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

/**
 * Created by Matthew on 25/09/2016.
 * Drawing manager
 */
public class AndroidScreen implements IScreen {

    AndroidDisplay display;
    Bitmap bitmap;
    Bitmap mouseBitmap;
    UpdateManager updateManager;
    private int width;
    private int height;
    public double zoomScale = 1;
    public float cutX = 0;
    public float cutY = 0;

    int mouseX;
    int mouseY;
    int mouseCenterX;
    int mouseCenterY;

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
            // Binary colours
            int dx, dy, n;
            int xA = x;
            int yA = y;
            int i = y * this.width + x;
            int rowBytes = (width + 7) / 8;
            byte b;

            for (dy = 0; dy < height; dy++) {
                for (dx = 0; dx < width / 8; dx++) {
                    b = data[dy * rowBytes + dx];
                    for (n = 7; n >= 0; n--) {
                        xA = i % this.width;
                        yA = i / this.width;
                        i++;
                        bitmap.setPixel(xA, yA, palette[b >> n & 1]);
                    }
                }
                for (n = 7; n >= 8 - width % 8; n--) {
                    xA = i % this.width;
                    yA = i / this.width;
                    i++;
                    bitmap.setPixel(xA, yA, palette[data[dy * rowBytes + dx] >> n & 1]);
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
        sub.recycle();
    }

    @Override
    public void fillPixels(int x, int y, int width, int height, int color) {
        Canvas canvas = new Canvas(bitmap);
        Paint c = new Paint();
        c.setColor(color | 0xFF000000);
        canvas.drawRect(x, y, x+width, y+height, c);
    }

    public void update() {
        display.invalidate();
    }

    /**
     * Does not work on android. Do not use
     */
    @Override
    public int[] getPixels() {
        return null;
    }

    @Override
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setHasAlpha(false);
        bitmap.setPremultiplied(false);
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
    public void setupCursor(int x, int y, int w, int h, int[] pixels) {
        mouseCenterX = x;
        mouseCenterY = y;
        mouseBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        if (mouseBitmap != null) {
            for (int yA = 0; yA < h; yA++) {
                for (int xA = 0; xA < w; xA++) {
                    if (((pixels[xA+yA*w] & 0xFF000000)) == 0x99000000) {
                        // Skip colour so make transparent
                        mouseBitmap.setPixel(xA, yA, 0);
                    } else {
                        mouseBitmap.setPixel(xA, yA, 0xFF000000 | pixels[xA+yA*w]);
                    }
                }
            }
        }
    }

    public void moveCursor(int x, int y) {
        mouseX = x;
        mouseY = y;
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

}
