package uk.co.mholeys.android.vnc.display;

import android.content.Context;

import uk.co.mholeys.android.vnc.CastPresentationService;
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
 * Created by Matthew on 30/07/2017.
 * Vnc interface for when
 */

public class CastInterface implements IUserInterface {


    private final CastPresentationService.VNCPresentation presentation;
    public AndroidDisplay display;
    private AndroidScreen screen;
    private UpdateManager updateManager;
    private Context context;
    private AndroidKeyboard keyboard = new AndroidKeyboard();
    private AndroidMouse mouse = new AndroidMouse(this);
    public int androidWidth;
    public int androidHeight;
    private PixelFormat format;

    public CastInterface(CastPresentationService.VNCPresentation presentation, AndroidDisplay display) {
        this.presentation = presentation;
        this.context = presentation.getContext();
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
        if (screen != null) {
            screen.setSize(width, height);
        } else {
            screen = new AndroidScreen(width, height);
        }
        display.screen = screen;
        //FIXME attach mouse display.mouse = mouse;

        mouse.mouseScaleW = (double)width / (double) androidWidth;
        mouse.mouseScaleH = (double)height / (double) androidHeight;
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
