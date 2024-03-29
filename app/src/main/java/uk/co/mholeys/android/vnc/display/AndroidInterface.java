package uk.co.mholeys.android.vnc.display;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.util.Log;

import uk.co.mholeys.android.vnc.input.AndroidKeyboard;
import uk.co.mholeys.android.vnc.input.AndroidMouse;
import uk.co.mholeys.android.vnc.input.AndroidMouse2;
import uk.co.mholeys.vnc.data.PixelFormat;
import uk.co.mholeys.vnc.display.IDisplay;
import uk.co.mholeys.vnc.display.IKeyboardManager;
import uk.co.mholeys.vnc.display.IMouseManager;
import uk.co.mholeys.vnc.display.IScreen;
import uk.co.mholeys.vnc.display.IUserInterface;
import uk.co.mholeys.vnc.display.UpdateManager;

/**
 * Created by Matthew on 25/09/2016.
 * VNC interface for managing and holding access to all needed components
 */
public class AndroidInterface implements IUserInterface {

    private static final String TAG = "AndInterface";
    // Canvas/View being drawn
    public AndroidDisplay display;
    // Controller for drawing to the display's canvas and manging how things are drawn
    private AndroidScreen screen;
    // Queue for updates
    private UpdateManager updateManager;
    private Context context;
    private AndroidKeyboard keyboard = new AndroidKeyboard();
    private AndroidMouse2 mouse = new AndroidMouse2(this);
    public int androidWidth;
    public int androidHeight;
    private PixelFormat format;

    public AndroidInterface(Context context, AndroidDisplay display) {
        this.context = context;
        this.display = display;
    }

    @Override
    public IDisplay getDisplay() {
        return display;
    }

    @Override
    public IScreen getScreen() {
        return screen;
    }

    @Override
    public IMouseManager getMouseManager() {
        return mouse;
    }

    public IKeyboardManager getKeyboardManager() {
        return keyboard;
    }

    @Override
    public UpdateManager getUpdateManager() {
        return updateManager;
    }

    @Override
    public PixelFormat getServerFormat() {
        return format;
    }

    @Override
    public void setUpdateManager(UpdateManager updateManager) {
        this.updateManager = updateManager;
        this.screen.updateManager = updateManager;
    }

    @Override
    public void setSize(int width, int height) {
        // Create the screen if we dont already have one
        if (screen != null) {
            screen.setSize(width, height);
        } else {
            // Or just update the screen with the new size
            screen = new AndroidScreen(width, height);
        }
        // Attach the mouse and screen
        display.screen = screen;
        display.mouse = mouse;

        // Work out the orientation and scale for the mouse input
        int orientation = context.getResources().getConfiguration().orientation;
        double wS = 1, hS = 1;
        if (androidWidth == 0 || androidHeight == 0) {
            Log.d(TAG, "setSize: Zero! W:" + androidWidth + " H:" + androidHeight);
        } else {
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                // height < width
                wS = width / androidWidth;
                hS = height / androidHeight;
            } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                // width < height
                wS = height / androidWidth;
                hS = width / androidHeight;
            }
        }

        Log.d(TAG, "setSize: " + androidWidth + " " + androidHeight);
        mouse.setScale(wS, hS);
    }

    @Override
    public void setServerFormat(PixelFormat format) {
        this.format = format;
    }

    @Override
    public void show() {
        display.start();
    }

    @Override
    public void exit() {

    }
}
