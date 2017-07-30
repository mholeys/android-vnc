package uk.co.mholeys.android.vnc.input;

import android.app.Activity;
import android.content.Context;
import android.nfc.Tag;
import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import java.util.LinkedList;
import java.util.Queue;

import uk.co.mholeys.android.vnc.display.AndroidDisplay;
import uk.co.mholeys.android.vnc.display.AndroidInterface;
import uk.co.mholeys.android.vnc.display.AndroidScreen;
import uk.co.mholeys.vnc.data.PointerPoint;
import uk.co.mholeys.vnc.display.IMouseManager;
import uk.co.mholeys.vnc.display.IUserInterface;
import uk.co.mholeys.vnc.log.Logger;

/**
 * Created by Matthew on 02/04/2017.
 */

public class AndroidMouse implements IMouseManager, View.OnTouchListener, View.OnGenericMotionListener, View.OnHoverListener {

    private static final String TAG = "AndroidMouse";
    public Queue<PointerPoint> miceUpdates = new LinkedList<PointerPoint>();

    float lastScrollX = 0;
    float lastScrollY = 0;

    float xOffset = 0;
    float yOffset = 0;

    float lastX = 0;
    float lastY = 0;

    double differenceX;
    double differenceY;

    boolean click = false;
    int clickCount = 0;
    long lastClick = 0;

    double scale = 1;
    public double mouseScaleW = 0;
    public double mouseScaleH = 0;
    static ScaleGestureDetector scaleDetector;

    public boolean left, right, middle, mwUp, mwDown;

    boolean reset = false;

    public short localX, localY;
    public short remoteX, remoteY;

    public IUserInterface inf;
    AndroidScreen screen;

    public AndroidMouse(IUserInterface inf) {
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
        p.middle = middle;
        p.mwUp = mwUp;
        p.mwDown = mwDown;
        miceUpdates.offer(p);
        left = false;
        right = false;
        middle = false;
        mwUp = false;
        mwDown = false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        screen = (AndroidScreen) inf.getScreen();
        if (scaleDetector == null) {
            if (inf instanceof AndroidInterface) {
                scaleDetector = new ScaleGestureDetector(((AndroidDisplay)inf.getDisplay()).getContext(), new ScaleListener());
            } else {
                Log.e(TAG, "Error creating scale gesture on cast device");
            }
        }
        int action = MotionEventCompat.getActionMasked(event);

        int pointerCount = event.getPointerCount();
        int id = MotionEventCompat.getActionIndex(event);
        final int pointerIndex = MotionEventCompat.getActionIndex(event);


        //Log.d("Mouse", "CC:" + clickCount + " PC:" + pointerCount);

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                if (!reset) {
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
                }
                /*if (pointerCount == 2) {
                    lastScrollX = event.getX(pointerIndex);
                    lastScrollY = event.getY(pointerIndex);
                }*/
                break;
            case (MotionEvent.ACTION_POINTER_DOWN):
                click = false;
                if (pointerCount == 2) {
                    float x1 = event.getX(0);
                    float x2 = event.getX(1);

                    float y1 = event.getY(0);
                    float y2 = event.getY(1);

                    float ax = (float)(x1 + x2 / 2.0d);
                    float ay = (float)(y1 + y2 / 2.0d);

                    lastScrollX = ax;
                    lastScrollY = ay;
                }
                break;
            case (MotionEvent.ACTION_MOVE):
                if (!reset) {
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
                    if (pointerCount == 1) {
                        float x = event.getX(pointerIndex);
                        float y = event.getY(pointerIndex);

                        localX += (short) (lastX - x);
                        localY += (short) (lastY - y);
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
                        float x1 = event.getX(0);
                        float x2 = event.getX(1);

                        float y1 = event.getY(0);
                        float y2 = event.getY(1);

                        float ax = (float)(x1 + x2 / 2.0d);
                        float ay = (float)(y1 + y2 / 2.0d);

                        if (lastScrollX == 0 && lastScrollY == 0) {
                            xOffset = ax;
                            yOffset = ay;
                        } else {
                            xOffset = lastScrollX - ax;
                            yOffset = lastScrollY - ay;
                        }
                        screen.cutX -= xOffset;
                        screen.cutY -= yOffset;
                        screen.update();
                        lastScrollX = ax;
                        lastScrollY = ay;
                    }
                    scaleDetector.onTouchEvent(event);
                }
                break;
            case (MotionEvent.ACTION_UP):
                if (!reset) {
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
                    lastScrollX = 0;
                    lastScrollY = 0;
                }
                if (pointerCount == 1) {
                    reset = false;
                }
                addToQueue();
                break;
            case (MotionEvent.ACTION_POINTER_UP):
                if (pointerCount == 4) {
                    reset = true;
                    scale = 1f;

                    xOffset = 0;
                    yOffset = 0;
                    lastScrollX = 0;
                    lastScrollY = 0;
                    screen.zoomScale = scale;
                    screen.cutX = 0;
                    screen.cutY = 0;
                    screen.update();
                    Log.d("Mouse", "Reset PDOWN. " + "xO:" + xOffset + " yO:" + yOffset + " xC:" + screen.cutX + " yC:" + screen.cutY + " s:" + scale);
                }
                if (pointerCount == 3) {
                    //Toggle keyboard
                    InputMethodManager inputMethodManager =
                            (InputMethodManager) v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.toggleSoftInputFromWindow(
                            v.getApplicationWindowToken(),
                            InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
                    Log.d("Mouse", "Opened keyboard");
                }
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

    @Override
    public boolean onGenericMotion(View v, MotionEvent event) {
        Log.d("Mouse", "Scroll " + event.getSource());
        if (event.getSource() == InputDevice.SOURCE_MOUSE) {
            if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) > 0) {
                mwUp = true;
                mwDown = false;
            } else if (event.getAxisValue(MotionEvent.AXIS_VSCROLL) < 0) {
                mwUp = false;
                mwDown = true;
            } else {
                mwUp = false;
                mwDown = false;
            }
            addToQueue();
            return true;
        }
        return false;
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        if (event.getSource() == InputDevice.SOURCE_MOUSE) {
            localX = (short) (mouseScaleW * event.getX());
            localY = (short) (mouseScaleH * event.getY());
            addToQueue();
            return true;
        }
        return false;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale *= detector.getScaleFactor();
            scale = Math.max(0.1f, Math.min(scale, 5.0f));
            screen.zoomScale = scale;
            screen.update();
            return true;
        }
    }

}
