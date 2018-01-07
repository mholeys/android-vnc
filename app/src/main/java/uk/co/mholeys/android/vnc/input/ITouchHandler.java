package uk.co.mholeys.android.vnc.input;

import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import uk.co.mholeys.android.vnc.display.AndroidInterface;
import uk.co.mholeys.vnc.data.PointerPoint;
import uk.co.mholeys.vnc.display.IUserInterface;

/**
 * Created by Matthew on 20/09/2017.
 */

public interface ITouchHandler {

    public boolean on1FingerDown(float[] x, float[] y);
    public boolean on1FingerUp(float[] x, float[] y);

    public boolean on2FingerDown(float[] x, float[] y);
    public boolean on2FingerUp(float[] x, float[] y);

    public boolean on3FingerDown(float[] x, float[] y);
    public boolean on3FingerUp(float[] x, float[] y);

    public boolean on4FingerDown(float[] x, float[] y);
    public boolean on4FingerUp(float[] x, float[] y);

    public boolean on1FingerDrag(float[] x, float[] y);
    public boolean on2FingerDrag(float[] x, float[] y);
    public boolean on3FingerDrag(float[] x, float[] y);
    public boolean on4FingerDrag(float[] x, float[] y);

    public boolean onCancelEvent(float[] x, float[] y);
    public boolean onOutsideEvent(float[] x, float[] y);

    public ScaleGestureDetector getScaleGestureDetector();
    public void createScaleGestureDetector(IUserInterface inf);

    public boolean onMouseGenericEvent(MotionEvent event);
    public boolean onMouseHoverEvent(MotionEvent event);

    public boolean sendMouse();
    public PointerPoint getCurrentMousePoint();
    public void updateLocalMouse();

    public void setScale(double mouseScaleW, double mouseScaleH);
}
