package uk.co.mholeys.android.vnc.input;

import uk.co.mholeys.vnc.data.KeyboardUpdate;
import uk.co.mholeys.vnc.display.IKeyboardManager;

/**
 * Created by Matthew on 02/04/2017.
 */

public class AndroidKeyboard implements IKeyboardManager {


    @Override
    public boolean sendKeys() {
        return false;
    }

    @Override
    public KeyboardUpdate getNext() {
        return null;
    }
}
