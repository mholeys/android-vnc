package uk.co.mholeys.android.vnc.display;

import android.content.Context;

import uk.co.mholeys.android.vnc.input.AndroidKeyboard;
import uk.co.mholeys.android.vnc.input.AndroidMouse;
import uk.co.mholeys.vnc.display.IDisplay;
import uk.co.mholeys.vnc.display.IKeyboardManager;
import uk.co.mholeys.vnc.display.IMouseManager;
import uk.co.mholeys.vnc.display.IScreen;
import uk.co.mholeys.vnc.display.IUserInterface;
import uk.co.mholeys.vnc.display.UpdateManager;

/**
 * Created by Matthew on 25/09/2016.
 */
public class AndroidInterface implements IUserInterface {

    public AndroidDisplay display;
    AndroidScreen screen;
    private UpdateManager updateManager;
    Context context;
    AndroidKeyboard keyboard = new AndroidKeyboard();
    AndroidMouse mouse = new AndroidMouse();

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
    public void setUpdateManager(UpdateManager updateManager) {
        this.updateManager = updateManager;
        this.screen.updateManager = updateManager;
    }

    @Override
    public void setSize(int width, int height) {
        if (screen != null) {
            screen.setSize(width, height);
        } else {
            screen = new AndroidScreen(width, height);
        }
        display.screen = screen;
        display.mouse = mouse;
    }

    @Override
    public void show() {
        display.start();
    }

    @Override
    public void exit() {

    }
}
