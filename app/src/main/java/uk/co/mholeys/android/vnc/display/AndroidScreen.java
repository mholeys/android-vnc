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

    public AndroidScreen(int width, int height) {
        bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        setSize(width, height);
    }

    @Override
    public void drawPixels(int x, int y, int width, int height, int[] pixels) {
        //synchronized (bitmap) {
        Canvas canvas = new Canvas(bitmap);
        for (int yA = y; yA < y + height; yA++) {
            for (int xA = x; xA < x + width; xA++) {
                //canvas.drawRect(xA, yA, 1, 1, new Paint(pixels[(xA - x) + ((yA - y) * width)]));
                bitmap.setPixel(xA, yA, pixels[(xA - x) + ((yA - y) * width)]);
            }
        };
        //}
    }

    @Override
    public void drawPalette(int x, int y, int width, int height, int[] palette, int paletteSize, byte[] data) {
        //synchronized (bitmap) {
        Canvas canvas = new Canvas(bitmap);
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
                    //canvas.drawRect(x, y, 1, 1, new Paint(palette[data[dy * rowBytes + dx] >> n & 1]));
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
                    //canvas.drawRect(lx, ly, 1, 1, new Paint(palette[d]));
                    bitmap.setPixel(lx, ly, palette[d]);
                }
            }
        }
        //}
    }

    @Override
    public void drawJPEG(int x, int y, int width, int height, byte[] data) {
        //synchronized (bitmap) {
        Bitmap jpeg = BitmapFactory.decodeByteArray(data, 0, data.length);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(jpeg, x, y, new Paint());
        //}
    }

    @Override
    public void copyPixels(int x, int y, int width, int height, int srcX, int srcY) {
        //synchronized (bitmap) {
        for (int yA = y; yA < y + height; yA++) {
            for (int xA = x; xA < x + width; xA++) {
                bitmap.setPixel(xA, yA, bitmap.getPixel(srcX + xA - x, srcY + yA - y));
            }
        }
        //}
    }

    @Override
    public void fillPixels(int x, int y, int width, int height, int color) {
        //synchronized (bitmap) {
        Canvas canvas = new Canvas(bitmap);
        canvas.drawRect(x, y, width, height, new Paint(color));
        //}
    }

    /*public void update() {
        if (surfaceHolder.getSurface().isValid()) {
            Log.d("Canvas", "Drew");
            Canvas canvas = surfaceHolder.lockCanvas();
            if (canvas == null) {
                return;
            }
            if (bitmap == null) {
                bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            } else {
                canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), new Paint(Color.BLACK));
                int scaledWidth, scaledHeight;
                float scale;
                Point displaySize = new Point();
                presentation.getDisplay().getSize(displaySize);
                float displayWidth = displaySize.x;
                float displayHeight = displaySize.y;
                float wScale = displayWidth / width;
                float hScale = displayHeight / height;

                if (Float.isInfinite(wScale) || Float.isInfinite(hScale)) {
                    wScale = 1;
                    hScale = 1;
                }
                float xOffset = 0;
                float yOffset = 0;
                if (wScale < hScale) {
                    scale = wScale;
                    yOffset = (displayHeight - (height*wScale)) / 2;
                } else {
                    scale = hScale;
                    xOffset = (displayWidth - (width*hScale)) / 2;
                }
                scale *= zoomScale;

                int xLimit = (int)(0.9 * width*scale);
                int yLimit = (int)(0.9 * height*scale);

                if (cutX < 0.03*xLimit && cutX > -0.03*xLimit) cutX = 0;
                if (cutY < 0.03*yLimit && cutY > -0.03*yLimit) cutY = 0;

                if (cutX < -xLimit) cutX = -xLimit;
                if (cutX > xLimit) cutX = xLimit;

                if (cutY < -yLimit) cutY = -yLimit;
                if (cutY > yLimit) cutY = yLimit;

                Matrix scaleMatrix = new Matrix();
                Matrix canvasMatrix = new Matrix();
                canvasMatrix.setTranslate(xOffset + cutX, yOffset + cutY);
                scaleMatrix.setScale(scale, scale);
                canvas.setMatrix(canvasMatrix);
                canvas.drawBitmap(bitmap, scaleMatrix, new Paint());
                //canvas.drawRect(presentation.vncSurface.getMouseManager().remoteX, presentation.vncSurface.getMouseManager().remoteY, 5, 5, new Paint(Color.GREEN));
            }
            surfaceHolder.unlockCanvasAndPost(canvas);
        }
    }*/

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
    public void drawCursor(int x, int y, int i2, int i3, byte[] bytes) {

    }

    public void process() {
        while (updateManager.hasUpdates()) {
            Logger.logger.printLn("Has updates");
            ScreenUpdate update = updateManager.getUpdate();
            if (update == null) continue;
            int x = update.x;
            int y = update.y;
            int width = update.width;
            int height = update.height;
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
