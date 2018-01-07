package uk.co.mholeys.android.vnc.input;

import android.support.v4.view.MotionEventCompat;
import android.util.Log;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.LinkedList;
import java.util.Queue;

import uk.co.mholeys.android.vnc.display.AndroidDisplay;
import uk.co.mholeys.android.vnc.display.AndroidInterface;
import uk.co.mholeys.android.vnc.display.AndroidScreen;
import uk.co.mholeys.vnc.data.PointerPoint;
import uk.co.mholeys.vnc.display.IMouseManager;
import uk.co.mholeys.vnc.display.IUserInterface;

/**
 * Created by Matthew on 20/09/2017.
 */

public class AndroidMouse2 implements IMouseManager, View.OnTouchListener, View.OnGenericMotionListener, View.OnHoverListener {

    private static final String TAG = "AndroidMouse2";

    private IUserInterface inf;

    private ITouchHandler touchHandler;

    public AndroidMouse2(IUserInterface inf) {
        this.inf = inf;
        touchHandler = new BuiltInDisplayTouchHandler((AndroidInterface) inf);
    }

    @Override
    public boolean sendLocalMouse() {
        return touchHandler.sendMouse();
    }

    @Override
    public PointerPoint getLocalMouse() {
        touchHandler.updateLocalMouse();
        if (touchHandler.sendMouse()) {
            return touchHandler.getCurrentMousePoint();
        }
        return null;
    }

    @Override
    public void setRemoteMouse(PointerPoint remote) {
        Log.d(TAG, "Ignoring setRemoteMouse");
        /*remoteX = remote.x;
        remoteY = remote.y;*/
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        touchHandler.createScaleGestureDetector(inf);

        int action = MotionEventCompat.getActionMasked(event);
        //Log.d(TAG, "Action " + event.getAction() + " " + action);


        int pointerCount = event.getPointerCount();
        //Log.d(TAG, "PointerCount " + pointerCount);
        int id = MotionEventCompat.getActionIndex(event);
        final int pointerIndex = MotionEventCompat.getActionIndex(event);

        float[] x = new float[pointerCount];
        float[] y = new float[pointerCount];

        for (int i = 0; i < pointerCount; i++) {
            x[i] = event.getX(i);
            y[i] = event.getY(i);
        }

        boolean actionPerformed = false;

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                if (pointerCount == 1) {
                    //Log.d(TAG, "1 Finger Down");
                    actionPerformed = touchHandler.on1FingerDown(x, y);
                } else {
                    Log.d(TAG, "Apparently ACTION_DOWN has a different pointer count: " + pointerCount);
                }
                break;
            case (MotionEvent.ACTION_UP):
                if (pointerCount == 1) {
                    //Log.d(TAG, "1 Finger Up");
                    actionPerformed = touchHandler.on1FingerUp(x, y);
                } else {
                    Log.d(TAG, "Apparently ACTION_UP has a different pointer count: " + pointerCount);
                }
                break;
            case (MotionEvent.ACTION_POINTER_DOWN):
                switch (pointerCount) {
                    case 2:
                        //Log.d(TAG, "2 Finger Down");
                        actionPerformed = touchHandler.on2FingerDown(x, y);
                        break;
                    case 3:
                        //Log.d(TAG, "3 Finger Down");
                        actionPerformed = touchHandler.on3FingerDown(x, y);
                        break;
                    case 4:
                        //Log.d(TAG, "4 Finger Down");
                        actionPerformed = touchHandler.on4FingerDown(x, y);
                        break;
                    default:
                        Log.d(TAG, "Apparently ACTION_POINTER_DOWN has a different pointer count: " + pointerCount);
                        break;
                }
                break;
            case (MotionEvent.ACTION_POINTER_UP):
                switch (pointerCount) {
                    case 2:
                        //Log.d(TAG, "2 Finger Up");
                        actionPerformed = touchHandler.on2FingerUp(x, y);
                        break;
                    case 3:
                        //Log.d(TAG, "3 Finger Up");
                        actionPerformed = touchHandler.on3FingerUp(x, y);
                        break;
                    case 4:
                        //Log.d(TAG, "4 Finger Up");
                        actionPerformed = touchHandler.on4FingerUp(x, y);
                        break;
                    default:
                        Log.d(TAG, "Apparently ACTION_POINTER_UP has a different pointer count: " + pointerCount);
                        break;
                }
                break;
            case (MotionEvent.ACTION_MOVE):
                //Log.d(TAG, "Move");
                actionPerformed = touchHandler.getScaleGestureDetector().onTouchEvent(event);
                switch (pointerCount) {
                    case 1:
                        //Log.d(TAG, "1 Finger Drag");
                        actionPerformed = touchHandler.on1FingerDrag(x, y);
                        break;
                    case 2:
                        //Log.d(TAG, "2 Finger Drag");
                        actionPerformed = touchHandler.on2FingerDrag(x, y);
                        break;
                    case 3:
                        //Log.d(TAG, "3 Finger Drag");
                        actionPerformed = touchHandler.on3FingerDrag(x, y);
                        break;
                    case 4:
                        //Log.d(TAG, "4 Finger Drag");
                        actionPerformed = touchHandler.on4FingerDrag(x, y);
                        break;
                    default:
                        //Log.d(TAG, "Apparently ACTION_MOVE has a different pointer count: " + pointerCount);
                        break;
                }
                break;
            case (MotionEvent.ACTION_CANCEL):
                Log.d(TAG, "Cancel");
                actionPerformed = touchHandler.onCancelEvent(x, y);
                break;
            case (MotionEvent.ACTION_OUTSIDE):
                Log.d(TAG, "Outside");
                actionPerformed = touchHandler.onOutsideEvent(x, y);
                break;
        }

        return true;
    }

    @Override
    public boolean onGenericMotion(View v, MotionEvent event) {
        if (event.getSource() == InputDevice.SOURCE_TOUCHSCREEN) {
            return false;
        }
        return touchHandler.onMouseGenericEvent(event);
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        return touchHandler.onMouseHoverEvent(event);
    }

    public void setScale(double mouseScaleW, double mouseScaleH) {
        touchHandler.setScale(mouseScaleW, mouseScaleH);
    }

}
