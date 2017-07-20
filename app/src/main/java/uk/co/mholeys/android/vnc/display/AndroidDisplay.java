package uk.co.mholeys.android.vnc.display;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.support.v7.widget.AppCompatImageView;
import android.util.Log;
import android.widget.ImageView;

import uk.co.mholeys.android.vnc.input.AndroidMouse;
import uk.co.mholeys.vnc.display.IDisplay;
import uk.co.mholeys.vnc.log.Logger;

/**
 * Created by Matthew on 25/09/2016.
 */
public class AndroidDisplay extends AppCompatImageView implements IDisplay {

    public AndroidScreen screen;
    public AndroidMouse mouse;
    boolean running = false;

    public AndroidDisplay(Context context, AndroidScreen screen) {
        super(context);
        this.screen = screen;
        running = true;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (screen == null) {
            Logger.logger.printLn("Screen is null cannot draw");
            return;
        }
        if (screen.bitmap == null) {
            Logger.logger.printLn("Screen's Bitmap is null cannot draw");
            return;
        }
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), new Paint(Color.BLACK));
        int scaledWidth, scaledHeight;
        float scale;

        float width = screen.bitmap.getWidth();
        float height = screen.bitmap.getHeight();

        float displayWidth = getWidth();
        float displayHeight = getHeight();
        float wScale = displayWidth / width;
        float hScale = displayHeight / height;

        double zoomScale = screen.zoomScale;
        float cutX = screen.cutX;
        float cutY = screen.cutY;

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
        canvasMatrix.setTranslate(cutX, cutY);
        scaleMatrix.setScale(scale, scale);
        canvas.setMatrix(canvasMatrix);
        canvas.drawBitmap(screen.bitmap, scaleMatrix, new Paint());
        //canvas.drawRect(mouse.remoteX, mouse.remoteY, 5, 5, new Paint(Color.GREEN));


        /*if (screen == null) {
            Logger.logger.printLn("Screen is null cannot draw");
            return;
        }
        if (screen.bitmap == null) {
            Logger.logger.printLn("Screen's Bitmap is null cannot draw");
            return;
        }
        Matrix scaleMatrix = new Matrix();
        Matrix canvasMatrix = new Matrix();

        double scale = 1;
        double width = getWidth();
        double height = getHeight();

        double scaleW = width / screen.bitmap.getWidth();
        double scaleH = height / screen.bitmap.getHeight();

        if (scaleW < scaleH) {
            scale = scaleW;
        } else {
            scale = scaleH;
        }

        double xOffset = (width - (screen.bitmap.getWidth() * scale)) / 2;
        double yOffset = (height - (screen.bitmap.getHeight() * scale)) / 2;

        canvasMatrix.setTranslate((float) xOffset, (float) yOffset);
        scaleMatrix.setScale((float) scale, (float) scale);
        canvas.setMatrix(canvasMatrix);
        canvas.drawBitmap(screen.bitmap, scaleMatrix, new Paint());
        Logger.logger.printLn("Drawing");*/
    }

    @Override
    public void start() {
        screen.display = this;
    }

    @Override
    public Thread getThread() {
        return null;
    }

    @Override
    public void run() {

    }
}
