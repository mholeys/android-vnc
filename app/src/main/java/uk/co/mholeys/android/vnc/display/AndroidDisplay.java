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
import uk.co.mholeys.android.vnc.input.AndroidMouse2;
import uk.co.mholeys.vnc.display.IDisplay;
import uk.co.mholeys.vnc.log.Logger;

/**
 * Created by Matthew on 25/09/2016.
 * Vnc display, holding the surface that the app will draw on to.
 */
public class AndroidDisplay extends AppCompatImageView implements IDisplay {

    private static final String TAG = "AndroidDisplay";
    public AndroidScreen screen;
    public AndroidMouse2 mouse;
    boolean running = false;

    public AndroidDisplay(Context context, AndroidScreen screen) {
        super(context);
        this.screen = screen;
        running = true;
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        // We cannot draw if the canvas we are working with doesnt exist
        if (screen == null) {
            Log.d(TAG, "draw: Screen is null cannot draw");
            return;
        }
        if (screen.bitmap == null) {
            Log.d(TAG, "draw: Screen's Bitmap is null cannot draw");
            return;
        }
        // Fill whole canvas with black, showing that it has started working
        canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), new Paint(Color.BLACK));

        float scale; // Scale for both width and height for "best fit"

        float width = screen.bitmap.getWidth();
        float height = screen.bitmap.getHeight();

        float displayWidth = getWidth();
        float displayHeight = getHeight();
        // Scale difference for width / height
        float wScale = displayWidth / width;
        float hScale = displayHeight / height;

        // How much the user has zoomed in
        double zoomScale = screen.zoomScale;
        // The x and y offset that the user has moved the screen by
        float cutX = screen.cutX;
        float cutY = screen.cutY;

        // Ensure that we have a actual number for the scale, in case one is zero
        if (Float.isInfinite(wScale) || Float.isInfinite(hScale)) {
            wScale = 1;
            hScale = 1;
        }

        // Determine the "best fit" scale
        if (wScale < hScale) {
            scale = wScale;
            //yOffset = (displayHeight - (height*wScale)) / 2;
        } else {
            scale = hScale;
            //xOffset = (displayWidth - (width*hScale)) / 2;
        }
        scale *= zoomScale; // Scale up by the zoom amount

        // Ensure that ~10% of the screen is visible
        // the offset cannot be more than 90% of the screen scaled up
        int xLimit = (int)(0.9 * width*scale);
        int yLimit = (int)(0.9 * height*scale);

        if (cutX < 0.03*xLimit && cutX > -0.03*xLimit) cutX = 0;
        if (cutY < 0.03*yLimit && cutY > -0.03*yLimit) cutY = 0;

        // Ensure that the offsets are now in the needed ranges (limit otherwise)
        if (cutX < -xLimit) cutX = -xLimit;
        if (cutX > xLimit) cutX = xLimit;

        if (cutY < -yLimit) cutY = -yLimit;
        if (cutY > yLimit) cutY = yLimit;

        // Limit the screens movement
        screen.cutX = cutX;
        screen.cutY = cutY;

        Matrix scaleMatrix = new Matrix();
        Matrix canvasMatrix = new Matrix();
        canvasMatrix.setTranslate(cutX, cutY);
        scaleMatrix.setScale(scale, scale);
        canvas.setMatrix(canvasMatrix);
        canvas.drawBitmap(screen.bitmap, scaleMatrix, new Paint());
        Matrix mouseMatrix = new Matrix();
        if (screen.mouseBitmap != null) {
            mouseMatrix.setTranslate(screen.mouseX - screen.mouseCenterX, screen.mouseY - screen.mouseCenterY);
            canvas.drawBitmap(screen.mouseBitmap, mouseMatrix, new Paint());
        }
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
