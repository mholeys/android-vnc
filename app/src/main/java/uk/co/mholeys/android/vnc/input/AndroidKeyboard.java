package uk.co.mholeys.android.vnc.input;

import android.util.Log;
import android.view.KeyEvent;

import java.security.Key;
import java.util.LinkedList;
import java.util.Queue;

import uk.co.mholeys.vnc.data.KeyboardUpdate;
import uk.co.mholeys.vnc.display.IKeyboardManager;

/**
 * Created by Matthew on 02/04/2017.
 */

public class AndroidKeyboard implements IKeyboardManager {

    final static String TAG = "AKeyboard";

    Queue<KeyboardUpdate> keyboardUpdates = new LinkedList<KeyboardUpdate>();

    boolean ctrl, shift, alt, meta;

    public void addKey(KeyEvent key, boolean pressed) {
        keyboardUpdates.add(lookupKeyCode(key, pressed));
    }

    @Override
    public boolean sendKeys() {
        return !keyboardUpdates.isEmpty();
    }

    @Override
    public KeyboardUpdate getNext() {
        return keyboardUpdates.poll();
    }

    private KeyboardUpdate lookupKeyCode(KeyEvent e, boolean pressed) {
        int modifier = e.getModifiers();

        // TODO: otherwise remove key checks from case? or integrate into mask check
        if ((modifier & KeyEvent.META_ALT_MASK) > 0) {
            if ((modifier & KeyEvent.META_ALT_LEFT_ON) > 0) {
                keyboardUpdates.offer(new KeyboardUpdate(0xffe9, true));
                alt = true;
            } else {
                if (alt) {
                    keyboardUpdates.offer(new KeyboardUpdate(0xffe9, false));
                    alt = false;
                }
            }
            if ((modifier & KeyEvent.META_ALT_RIGHT_ON) > 0) {
                keyboardUpdates.offer(new KeyboardUpdate(0xffea, true));
                alt = true;
            } else {
                if (alt) {
                    keyboardUpdates.offer(new KeyboardUpdate(0xffea, false));
                    alt = false;
                }
            }
        }
        if ((modifier & KeyEvent.META_CTRL_MASK) > 0) {
            if ((modifier & KeyEvent.META_CTRL_LEFT_ON) > 0) {
                keyboardUpdates.offer(new KeyboardUpdate(0xffe3, true));
                ctrl = true;
            } else {
                if (ctrl) {
                    keyboardUpdates.offer(new KeyboardUpdate(0xffe3, false));
                    ctrl = false;
                }
            }
            if ((modifier & KeyEvent.META_CTRL_RIGHT_ON) > 0) {
                keyboardUpdates.offer(new KeyboardUpdate(0xffe4, true));
                ctrl = true;
            } else {
                if (ctrl) {
                    keyboardUpdates.offer(new KeyboardUpdate(0xffe4, false));
                    ctrl = false;
                }
            }
        }
        if ((modifier & KeyEvent.META_META_MASK) > 0) {
            if ((modifier & KeyEvent.META_META_LEFT_ON) > 0) {
                keyboardUpdates.offer(new KeyboardUpdate(0xffe7, true));
                meta = true;
            } else {
                if (meta) {
                    keyboardUpdates.offer(new KeyboardUpdate(0xffe7, false));
                    meta = false;
                }
            }
            if ((modifier & KeyEvent.META_META_RIGHT_ON) > 0) {
                keyboardUpdates.offer(new KeyboardUpdate(0xffe8, true));
                meta = true;
            } else {
                if (meta) {
                    keyboardUpdates.offer(new KeyboardUpdate(0xffe8, false));
                    meta = false;
                }
            }
        }
        if ((modifier & KeyEvent.META_SHIFT_MASK) > 0) {
            if ((modifier & KeyEvent.META_SHIFT_LEFT_ON) > 0) {
                keyboardUpdates.offer(new KeyboardUpdate(0xffe1, true));
                shift = true;
            } else {
                if (shift) {
                    keyboardUpdates.offer(new KeyboardUpdate(0xffe1, false));
                    shift = false;
                }
            }
            if ((modifier & KeyEvent.META_SHIFT_RIGHT_ON) > 0) {
                keyboardUpdates.offer(new KeyboardUpdate(0xffe2, true));
                shift = true;
            } else {
                if (shift) {
                    keyboardUpdates.offer(new KeyboardUpdate(0xffe1, false));
                    shift = false;
                }
            }
        }
        switch (e.getKeyCode()) {
            case KeyEvent.KEYCODE_DEL:
                return new KeyboardUpdate(0xff08, pressed);
            case KeyEvent.KEYCODE_TAB:
                return new KeyboardUpdate(0xff09, pressed);
            case KeyEvent.KEYCODE_ENTER:
                return new KeyboardUpdate(0xff0d, pressed);
            case KeyEvent.KEYCODE_ESCAPE:
                return new KeyboardUpdate(0xff1b, pressed);
            case KeyEvent.KEYCODE_INSERT:
                return new KeyboardUpdate(0xff63, pressed);
            case KeyEvent.KEYCODE_MOVE_HOME:
                return new KeyboardUpdate(0xff50, pressed);
            case KeyEvent.KEYCODE_DPAD_LEFT:
                return new KeyboardUpdate(0xff51, pressed);
            case KeyEvent.KEYCODE_DPAD_UP:
                return new KeyboardUpdate(0xff52, pressed);
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                return new KeyboardUpdate(0xff53, pressed);
            case KeyEvent.KEYCODE_DPAD_DOWN:
                return new KeyboardUpdate(0xff54, pressed);
            case KeyEvent.KEYCODE_PAGE_UP:
                return new KeyboardUpdate(0xff55, pressed);
            case KeyEvent.KEYCODE_PAGE_DOWN:
                return new KeyboardUpdate(0xff56, pressed);
            case KeyEvent.KEYCODE_MOVE_END:
                return new KeyboardUpdate(0xff57, pressed);
            case KeyEvent.KEYCODE_F1:
                return new KeyboardUpdate(0xffbe, pressed);
            case KeyEvent.KEYCODE_F2:
                return new KeyboardUpdate(0xffbf, pressed);
            case KeyEvent.KEYCODE_F3:
                return new KeyboardUpdate(0xffc0, pressed);
            case KeyEvent.KEYCODE_F4:
                return new KeyboardUpdate(0xffc1, pressed);
            case KeyEvent.KEYCODE_F5:
                return new KeyboardUpdate(0xffc2, pressed);
            case KeyEvent.KEYCODE_F6:
                return new KeyboardUpdate(0xffc3, pressed);
            case KeyEvent.KEYCODE_F7:
                return new KeyboardUpdate(0xffc4, pressed);
            case KeyEvent.KEYCODE_F8:
                return new KeyboardUpdate(0xffc5, pressed);
            case KeyEvent.KEYCODE_F9:
                return new KeyboardUpdate(0xffc6, pressed);
            case KeyEvent.KEYCODE_F10:
                return new KeyboardUpdate(0xffc7, pressed);
            case KeyEvent.KEYCODE_F11:
                return new KeyboardUpdate(0xffc8, pressed);
            case KeyEvent.KEYCODE_F12:
                return new KeyboardUpdate(0xffc9, pressed);
            case KeyEvent.KEYCODE_SHIFT_LEFT:
            //case KeyEvent.META_SHIFT_LEFT_ON:
                shift = pressed;
                return new KeyboardUpdate(0xffe1, pressed);
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
            //case KeyEvent.META_SHIFT_RIGHT_ON:
                shift = pressed;
                return new KeyboardUpdate(0xffe2, pressed);
            case KeyEvent.KEYCODE_CTRL_LEFT:
            //case KeyEvent.META_CTRL_LEFT_ON:
                ctrl = pressed;
                return new KeyboardUpdate(0xffe3, pressed);
            case KeyEvent.KEYCODE_CTRL_RIGHT:
            //case KeyEvent.META_CTRL_RIGHT_ON:
                ctrl = pressed;
                return new KeyboardUpdate(0xffe4, pressed);
            case KeyEvent.KEYCODE_META_LEFT:
            //case KeyEvent.META_META_LEFT_ON:
                meta = pressed;
                return new KeyboardUpdate(0xffe7, pressed);
            case KeyEvent.KEYCODE_META_RIGHT:
            //case KeyEvent.META_META_RIGHT_ON:
                meta = pressed;
                return new KeyboardUpdate(0xffe8, pressed);
            case KeyEvent.KEYCODE_ALT_LEFT:
            //case KeyEvent.META_ALT_LEFT_ON:
                alt = pressed;
                return new KeyboardUpdate(0xffe9, pressed);
            case KeyEvent.KEYCODE_ALT_RIGHT:
            //case KeyEvent.META_ALT_RIGHT_ON:
                alt = pressed;
                return new KeyboardUpdate(0xffea, pressed);
            case KeyEvent.KEYCODE_FORWARD_DEL:
                return new KeyboardUpdate(0xffff, pressed);
            default:
                if (e.getKeyCode() != 0xffff) {
                    return new KeyboardUpdate(e.getUnicodeChar(), pressed);
                }
                /*if ((e.getModifiers() & KeyEvent.SHIFT_DOWN_MASK) == KeyEvent.SHIFT_DOWN_MASK) {
                    if (e.getKeyLocation() == KeyEvent.KEY_LOCATION_LEFT)
                        keyboardUpdates.offer(new KeyboardUpdate(0xffe1, true));
                    if (e.getKeyLocation() == KeyEvent.KEY_LOCATION_RIGHT)
                        keyboardUpdates.offer(new KeyboardUpdate(0xffe2, true));
                }*/
                return new KeyboardUpdate(e.getKeyCode(), pressed);
        }
    }
}
