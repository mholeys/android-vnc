package uk.co.mholeys.android.vnc.input;

import android.support.v4.view.MotionEventCompat;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.LinkedList;
import java.util.Queue;

import uk.co.mholeys.android.vnc.display.AndroidInterface;
import uk.co.mholeys.android.vnc.display.AndroidScreen;
import uk.co.mholeys.vnc.data.PointerPoint;
import uk.co.mholeys.vnc.display.IMouseManager;
import uk.co.mholeys.vnc.log.Logger;

/**
 * Created by Matthew on 02/04/2017.
 */

public class AndroidMouse implements IMouseManager, View.OnTouchListener {

    public Queue<PointerPoint> miceUpdates = new LinkedList<PointerPoint>();

    float lastScrollX = 0;
    float lastScrollY = 0;

    float lastX = 0;
    float lastY = 0;

    double differenceX;
    double differenceY;

    boolean click = false;
    int clickCount = 0;
    long lastClick = 0;

    double scale = 1;
    static ScaleGestureDetector scaleDetector;

    public boolean left, right, middle, mwUp, mwDown;

    public short localX, localY;
    public short remoteX, remoteY;

    public AndroidInterface inf;
    AndroidScreen screen;

    public AndroidMouse(AndroidInterface inf) {
        this.inf = inf;
    }

    public void mouseMoved(int x, int y) {
        localX = (short) x;
        localY = (short) y;
        PointerPoint p = new PointerPoint(localX, localY);
        p.left = left;
        p.right = right;
        p.middle = middle;
        p.mwUp = mwUp;
        p.mwDown = mwDown;
        boolean allowed = miceUpdates.offer(p);
        if (!allowed) {
            Logger.logger.printLn("Could not queue mouse");
        }
    }

    public void mouseClicked(boolean left, boolean right) {
        middle = false; // No middle click for now

        PointerPoint p = new PointerPoint(localX, localY);
        p.left = left;
        p.right = right;
        p.middle = middle;
        p.mwUp = mwUp;
        p.mwDown = mwDown;
        boolean allowed = miceUpdates.offer(p);
        if (!allowed) {
            System.out.println("Could not queue mouse");
        }
    }

    @Override
    public boolean sendLocalMouse() {
        return !miceUpdates.isEmpty();
    }

    @Override
    public PointerPoint getLocalMouse() {
        PointerPoint p = miceUpdates.poll();
        return p;
    }

    @Override
    public void setRemoteMouse(PointerPoint remote) {
        remoteX = remote.x;
        remoteY = remote.y;
    }

    public void addToQueue() {
        PointerPoint p = new PointerPoint(localX, localY);
        p.left = left;
        p.right = right;
        miceUpdates.offer(p);
        left = false;
        right = false;
        middle = false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        screen = (AndroidScreen) inf.getScreen();
        scaleDetector = new ScaleGestureDetector(inf.display.getContext(), new ScaleListener());
        int action = MotionEventCompat.getActionMasked(event);

        int pointerCount = event.getPointerCount();
        int id = MotionEventCompat.getActionIndex(event);
        final int pointerIndex = MotionEventCompat.getActionIndex(event);

        Logger.logger.printLn("ClickCount " + clickCount);

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                long sinceLast = lastClick - System.currentTimeMillis();
                lastClick = System.currentTimeMillis();
                if (sinceLast > 500) {
                    Logger.logger.printLn("Reset click count was: " + clickCount);
                    clickCount = 0;
                }
                if (pointerCount == 1) {
                    lastX = event.getX(pointerIndex);
                    lastY = event.getY(pointerIndex);
                    click = true;
                    clickCount++;
                }
                /*if (pointerCount == 2) {
                    lastScrollX = event.getX(pointerIndex);
                    lastScrollY = event.getY(pointerIndex);
                }*/
                break;
            case (MotionEvent.ACTION_POINTER_DOWN):
                click = false;
                /*if (pointerCount == 4) {
                    //Toggle keyboard
                    if (keyboardState) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                    } else {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    }
                    Logger.logger.printLn("Toggled keyboard");
                    keyboardState = !keyboardState;
                }*/
                break;
            case (MotionEvent.ACTION_MOVE):
                if (pointerCount == 1) {
                    if (clickCount == 1) {
                        click = false;
                    }
                    differenceX = lastX - event.getX();
                    differenceY = lastY - event.getY();
                    lastX = event.getX();
                    lastY = event.getY();
                    localX -= differenceX;
                    localY -= differenceY;
                    if (clickCount == 2) {
                        left = click;
                        right = false;
                    }
                    addToQueue();
                }
                /*if (pointerCount == 1) {
                    float x = event.getX(pointerIndex);
                    float y = event.getY(pointerIndex);

                    mouse.localX += (short)(lastX - x);
                    mouse.localY += (short)(lastY - y);
                    lastX = x;
                    lastY = y;

                    if (clickCount == 2) {
                        left = true;
                        right = false;
                    } else {
                        left = false;
                        right = false;
                        click = false;
                    }
                } else if (pointerCount == 2) {
                    float x = event.getX(pointerIndex);
                    float y = event.getY(pointerIndex);
                    xOffset = lastScrollX - x;
                    yOffset = lastScrollY - y;
                    screen.cutX -= xOffset;
                    screen.cutY -= yOffset;
                    screen.update();
                    lastScrollX = x;
                    lastScrollY = y;
                } else if (pointerCount == 3) {
                    scale = 1f;

                    xOffset = 0;
                    yOffset = 0;
                    lastScrollX = 0;
                    lastScrollY = 0;
                    screen.zoomScale = scale;
                    screen.cutX = 0;
                    screen.cutY = 0;
                    screen.update();
                }*/
                scaleDetector.onTouchEvent(event);
                break;
            case (MotionEvent.ACTION_UP):
                differenceX = lastX - event.getX();
                differenceY = lastY - event.getY();
                if (click) {
                    if (clickCount == 1) {
                        localX -= differenceX;
                        localY -= differenceY;
                        left = true;
                        right = false;
                        //addToQueue();
                    } else if (clickCount == 3) {
                        localX -= differenceX;
                        localY -= differenceY;
                        left = false;
                        right = true;
                        //addToQueue();
                    }
                    click = false;
                    clickCount = 0;
                } else {
                    left = false;
                    right = false;
                }
                addToQueue();
                break;
            case (MotionEvent.ACTION_CANCEL):
                left = false;
                right = false;
                click = false;
                clickCount = 0;
                break;
            case (MotionEvent.ACTION_OUTSIDE):
                break;
            default:
                break;
        }
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale *= detector.getScaleFactor();
            scale = Math.max(1.0f, Math.min(scale, 5.0f));
            screen.zoomScale = scale;
            screen.update();
            return true;
        }
    }

}
