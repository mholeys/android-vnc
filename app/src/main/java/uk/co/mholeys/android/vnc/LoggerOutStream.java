package uk.co.mholeys.android.vnc;

import android.util.Log;

import java.io.OutputStream;

/**
 * Created by Matthew on 04/04/2017.
 */
class LoggerOutStream extends OutputStream {

    String message = "";

    public void write(int i) {
        if (i == '\n') {
            Log.e("Logger", message);
            message = "";
        } else {
            message += (char)i;
        }
    }

}
