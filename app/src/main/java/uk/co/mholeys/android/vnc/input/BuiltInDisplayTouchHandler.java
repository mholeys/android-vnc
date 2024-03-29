package uk.co.mholeys.android.vnc.input;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Queue;

import uk.co.mholeys.android.vnc.display.AndroidDisplay;
import uk.co.mholeys.android.vnc.display.AndroidInterface;
import uk.co.mholeys.android.vnc.display.AndroidScreen;
import uk.co.mholeys.vnc.data.PointerPoint;
import uk.co.mholeys.vnc.display.IUserInterface;

/**
 * Created by Matthew on 20/09/2017.
 */

public class BuiltInDisplayTouchHandler implements ITouchHandler {

    private Queue<PointerPoint> miceUpdates = new LinkedList<PointerPoint>();

    private float[] lastX = new float[4];
    private float[] lastY = new float[4];

    private float lastScrollX, lastScrollY;
    private float xOffset, yOffset;

    private long lastClick = 0;

    private int clickCount;

    private int lastPointerCount;

    private boolean moveMouse, pan;

    private AndroidInterface inf;
    private View view;
    private AndroidScreen screen;

    private PointerPoint pointerPoint = new PointerPoint((short) 0, (short) 0);
    double scale = 1;
    public double mouseScaleW = 0;
    public double mouseScaleH = 0;
    private ScaleGestureDetector scaleDetector;

    public BuiltInDisplayTouchHandler(AndroidInterface inf) {
        this.inf = inf;
        this.screen = (AndroidScreen) inf.getScreen();
        this.view = (AndroidDisplay) inf.getDisplay();
    }

    @Override
    public boolean on1FingerDown(float[] x, float[] y) {
        long sinceLast = System.currentTimeMillis() - lastClick;
        lastClick = System.currentTimeMillis();
        Log.d("Drag", "1FD mm:" + moveMouse + " x:" + pointerPoint.x + " y:" + pointerPoint.y + " cc:" + clickCount + " sl:" + sinceLast);
        if (sinceLast > 200) {
            clickCount = 0;
            //moveMouse = true;
        } else {
            //clickCount++;
            //moveMouse = true;
        }
        updateLastPos(x, y);
        return false;
    }

    @Override
    public boolean on1FingerUp(float[] x, float[] y) {
        Log.d("Drag", "1FU mm:" + moveMouse + " x:" + pointerPoint.x + " y:" + pointerPoint.y + " cc:" + clickCount + " sl:" + (System.currentTimeMillis() - lastClick));
        if (!moveMouse) {
            //pointerPoint.x = (short) x[0];
            //pointerPoint.y = (short) y[0];
            updateLastPos(x, y);
            clickCount++;
            switch (clickCount) {
                case 0:
                    break;
                case 1:
                    pointerPoint.left = false;
                    pointerPoint.right = false;
                    break;
                case 2:
                    pointerPoint.left = true;
                    break;
                case 3:
                    pointerPoint.right = true;
                    break;
                default:
                    clickCount = 0;
            }
            miceUpdates.add(pointerPoint.clone());
        }
        moveMouse = false;
        pointerPoint.left = false;
        pointerPoint.right = false;
        miceUpdates.add(pointerPoint.clone());
        Log.d("Drag", "1FU mm:" + moveMouse + " x:" + pointerPoint.x + " y:" + pointerPoint.y + " cc:" + clickCount + " sl:" + (System.currentTimeMillis() - lastClick));
        return true;
    }

    @Override
    public boolean on2FingerDown(float[] x, float[] y) {
        Log.d("Drag", "2FD");
        return false;
    }

    @Override
    public boolean on2FingerUp(float[] x, float[] y) {
        Log.d("Drag", "2FU mm:" + moveMouse + " x:" + pointerPoint.x + " y:" + pointerPoint.y + " cc:" + clickCount + " sl:" + (System.currentTimeMillis() - lastClick));
        pan = false;
        if (moveMouse) {
            pointerPoint.x = (short) x[0];
            pointerPoint.y = (short) y[0];
        }

        boolean performed = false;
        if (clickCount == 1) {
            pointerPoint.left = false;
            pointerPoint.right = true;
            performed = true;
        }
        miceUpdates.add(pointerPoint.clone());
        pointerPoint.left = false;
        pointerPoint.right = false;
        miceUpdates.add(pointerPoint.clone());
        return performed;
    }

    @Override
    public boolean on3FingerDown(float[] x, float[] y) {
        return false;
    }

    @Override
    public boolean on3FingerUp(float[] x, float[] y) {
        //Toggle keyboard
        // TODO investigate null view
        view = ((AndroidDisplay) inf.getDisplay());
        InputMethodManager inputMethodManager =
                (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(
                view.getApplicationWindowToken(),
                InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        Log.d("Mouse", "Opened keyboard");
        return true;
    }

    @Override
    public boolean on4FingerDown(float[] x, float[] y) {
        return false;
    }

    @Override
    public boolean on4FingerUp(float[] x, float[] y) {
        xOffset = 0;
        yOffset = 0;
        scale = 1d;
        return true;
    }

    @Override
    public boolean on1FingerDrag(float[] x, float[] y) {
        Log.d("Drag", "1FD x: " + pointerPoint.x + " y: " + pointerPoint.y);
        pointerPoint.x -= (short) (lastX[0] - x[0]);
        pointerPoint.y -= (short) (lastY[0] - y[0]);

        miceUpdates.add(pointerPoint.clone());
        updateLastPos(x, y);
        return moveMouse;
    }

    @Override
    public boolean on2FingerDrag(float[] x, float[] y) {
        // FIXME
        float ax = (float)(x[0] + x[1] / 2.0d);
        float ay = (float)(y[0] + y[1]/ 2.0d);
        Log.d("Drag", "2FD ax: " + ax + " ay: " + ay);
        Log.d("Drag", "2FD sx: " + screen.cutX + " sy: " + screen.cutY);

        if (pan) {
            xOffset = lastScrollX - ax;
            yOffset = lastScrollY - ay;
        } else {
            pan = true;
            xOffset = ax;
            yOffset = ay;
        }
        screen.cutX -= xOffset;
        screen.cutY -= yOffset;
        screen.update();
        lastScrollX = ax;
        lastScrollY = ay;

        return true;
    }

    @Override
    public boolean on3FingerDrag(float[] x, float[] y) {
        return false;
    }

    @Override
    public boolean on4FingerDrag(float[] x, float[] y) {
        return false;
    }

    @Override
    public boolean onCancelEvent(float[] x, float[] y) {
        clickCount = 0;
        return true;
    }

    @Override
    public boolean onOutsideEvent(float[] x, float[] y) {
        clickCount = 0;
        return true;
    }

    @Override
    public boolean onMouseHoverEvent(MotionEvent event) {
        if (event.getSource() == InputDevice.SOURCE_MOUSE) {
            pointerPoint.x = (short) (mouseScaleW * event.getX());
            pointerPoint.y = (short) (mouseScaleH * event.getY());
            miceUpdates.add(pointerPoint.clone());
            return true;
        }
        return false;
    }

    @Override
    public boolean onMouseGenericEvent(MotionEvent event) {
        Log.d("Mouse", "Scroll " + event.getSource());
        if (event.getSource() == InputDevice.SOURCE_MOUSE) {
            if (event.isButtonPressed(MotionEvent.BUTTON_PRIMARY)) {
                pointerPoint.left = true;
            } else {
                pointerPoint.left = false;
            }

            if (event.isButtonPressed(MotionEvent.BUTTON_SECONDARY)) {
                pointerPoint.right = true;
            } else {
                pointerPoint.right = false;
            }
            if (event.isButtonPressed(MotionEvent.BUTTON_TERTIARY)) {
                pointerPoint.middle = true;
            } else {
                pointerPoint.middle = false;
            }
            if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) > 0) {
                pointerPoint.mwUp = true;
                pointerPoint.mwDown = false;
            } else if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0) {
                pointerPoint.mwUp = false;
                pointerPoint.mwDown = true;
            } else {
                pointerPoint.mwUp = false;
                pointerPoint.mwDown = false;
            }
            miceUpdates.add(pointerPoint.clone());
            return true;
        }
        return false;
    }

    @Override
    public ScaleGestureDetector getScaleGestureDetector() {
        return scaleDetector;
    }

    @Override
    public void createScaleGestureDetector(IUserInterface inf) {
        this.inf = (AndroidInterface) inf;
        this.screen = (AndroidScreen) inf.getScreen();
        if (scaleDetector == null) {
            scaleDetector = new ScaleGestureDetector(((AndroidDisplay) inf.getDisplay()).getContext(), new ScaleListener());
        }
    }

    @Override
    public boolean sendMouse() {
        return !miceUpdates.isEmpty();
    }

    @Override
    public PointerPoint getCurrentMousePoint() {
        return miceUpdates.poll();
    }

    public void updateLocalMouse() {
        PointerPoint p = miceUpdates.peek();
        inf.getScreen().moveCursor(p.x, p.y);
    }

    @Override
    public void setScale(double mouseScaleW, double mouseScaleH) {
        this.mouseScaleW = mouseScaleW;
        this.mouseScaleH = mouseScaleH;
    }

    private void updateLastPos(float[] x, float[] y) {
        /*String methodCall = Thread.getAllStackTraces().get(Thread.currentThread())[1].getMethodName();
        if (methodCall.contains("1")) {
            lastPointerCount = 1;
        } else if (methodCall.contains("2")) {
            lastPointerCount = 2;
        } else if (methodCall.contains("3")) {
            lastPointerCount = 3;
        } else if (methodCall.contains("4")) {
            lastPointerCount = 4;
        }*/
        for (int i = 0; i < x.length; i++) {
            lastX[i] = x[i];
            lastY[i] = y[i];
        }
    }

    public PointerPoint getCurrentPointerPoint() {
        /*PointerPoint p = new PointerPoint(localX, localY);
        p.left = left;
        p.right = right;
        p.middle = middle;
        p.mwUp = mwUp;
        p.mwDown = mwDown;
        miceUpdates.offer(p);
        left = false;
        right = false;
        middle = false;
        mwUp = false;
        mwDown = false;*/
        return pointerPoint;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale *= detector.getScaleFactor();
            scale = Math.max(0.1f, Math.min(scale, 5.0f));
            if (screen != null) {
                screen.zoomScale = scale;
                screen.update();
            }
            return true;
        }
    }
}
