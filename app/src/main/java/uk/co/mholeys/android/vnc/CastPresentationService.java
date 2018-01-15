package uk.co.mholeys.android.vnc;

import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.google.android.gms.cast.CastPresentation;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;

import java.io.IOException;
import java.net.SocketException;

import uk.co.mholeys.android.vnc.display.AndroidDisplay;
import uk.co.mholeys.android.vnc.display.AndroidScreen;
import uk.co.mholeys.android.vnc.display.CastInterface;
import uk.co.mholeys.android.vnc.input.AndroidKeyboard;
import uk.co.mholeys.android.vnc.input.AndroidMouse;
import uk.co.mholeys.vnc.VNCConnectionException;
import uk.co.mholeys.vnc.data.Encoding;
import uk.co.mholeys.vnc.data.EncodingSettings;
import uk.co.mholeys.vnc.log.Logger;
import uk.co.mholeys.vnc.net.VNCProtocol;

public class CastPresentationService extends CastRemoteDisplayLocalService {

    private static final String TAG = "VNC.pres.service";
    VNCPresentation mPresentation;

    VNCProtocol protocol;
    ServerData connection;
    //FIXME: Cannot store context statically "Warning:(41, 5) Do not place Android context classes in static fields (static reference to `CastInterface` which has field `context` pointing to `Context`); this is a memory leak (and also breaks Instant Run)"
    CastInterface castInterface;
    AndroidScreen screen;
    AndroidDisplay display;
    AndroidMouse mouse;
    AndroidKeyboard keyboard;
    public Handler mToastHandler;

    public CastPresentationService() {

    }

    @Override
    public void onCreatePresentation(Display display) {
        // Setup vnc thing maybe
        createPresentation(display);
    }

    @Override
    public void onDismissPresentation() {
        // Disconnect
        dismissPresentation();
    }

    private void dismissPresentation() {
        if (mPresentation != null) {
            mPresentation.dismiss();
            mPresentation = null;
            protocol.disconnect();
        }
    }

    private void createPresentation(Display display) {
        // Stop old presentation
        dismissPresentation();

        // Get connection info ready
        if (connection.result == -1) {
            Log.d(TAG, "onCreate: waiting for connection lookup");
            connection.prepare();
            while (connection.result == -1) {
                Log.d(TAG, "onCreate: waiting for connection lookup " + connection.result);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        } else {
            Log.d(TAG, "onCreate: Connection is pre-prepared");
        }

        Log.d(TAG, "createPresentation: Attempting to create vnc presentation");
        mPresentation = new VNCPresentation(this, display, connection);
        try {
            mPresentation.show();
        } catch (WindowManager.InvalidDisplayException ex) {
            Log.e(TAG, "Unable to show presentation, display was " +
                    "removed.", ex);
            dismissPresentation();
        }
    }

    public final class VNCPresentation extends CastPresentation {

        private ServerData connection;
        private boolean mReady = true;
        private Display castDdisplay;

        private static final String TAG = "VNC.pres.cast";

        VNCPresentation(Context context,
                        Display display, ServerData connection) {
            super(context, display);
            this.connection = connection;
            this.castDdisplay = display;
        }

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.presentation_layout);

            // Start vnc stuff
            final Logger logger = new Logger(new LoggerOutStream());
            logger.logLevel = Logger.LOG_LEVEL_NONE;

            if (connection.getAddress() == null) {
                Log.d(TAG, "Could not find address " + connection.address);
                mReady = false;
            }

            Log.d(TAG, "Found address " + connection.address);

            final ServerData connection = this.connection;
            EncodingSettings preferedEncoding = new EncodingSettings()
                    .addEncoding(Encoding.TIGHT_ENCODING)
                    .addEncoding(Encoding.ZLIB_ENCODING)
                    //.addEncoding(Encoding.CORRE_ENCODING)
                    //.addEncoding(Encoding.RRE_ENCODING)
                    .addEncoding(Encoding.RAW_ENCODING)
                    .addEncoding(Encoding.JPEG_QUALITY_LEVEL_2_PSEUDO_ENCODING)
                    .addEncoding(Encoding.COMPRESSION_LEVEL_0_PSEUDO_ENCODING)
                    .addEncoding(Encoding.CURSOR_PSEUDO_ENCODING);
            connection.setPrefferedEncoding(preferedEncoding);

            Log.d(TAG, "onCreate: Creating vnc interface for casting");
            castInterface = new CastInterface(this, new AndroidDisplay(getContext(), null));
            mouse = (AndroidMouse) castInterface.getMouseManager();
            keyboard = (AndroidKeyboard) castInterface.getKeyboardManager();

            RelativeLayout layout = findViewById(R.id.layoutCastVnc);

            Point p = new Point();
            castDdisplay.getSize(p);
            castInterface.androidWidth = p.x;
            castInterface.androidHeight = p.y;

            Log.d(TAG, "onCreate: Creating thread " + mReady);
            if (mReady) {
                Thread mProtoThread = new Thread() {
                    @Override
                    public void run() {
                        if (Looper.myLooper() == null) {
                            Looper.prepare();
                            Looper l = Looper.myLooper();

                            Log.d(TAG, "Starting");

                            try {
                                protocol = new VNCProtocol(connection, castInterface, logger);
                                protocol.run();
                            } catch (VNCConnectionException e) {
                                final String reason = e.toString();
                                Log.e(TAG, "Proto error: \" " + reason + "\"");
                                Message m = new Message();
                                m.arg1 = 0;
                                Bundle b = new Bundle();
                                b.putString("TEXT", reason);
                                m.setData(b);
                                mToastHandler.sendMessage(m);
                            } catch (SocketException e) {
                                final String reason = e.toString();
                                Log.e(TAG, "Connection ended unexpectedly");
                                Message m = new Message();
                                m.arg1 = 2;
                                Bundle b = new Bundle();
                                b.putString("TEXT", reason);
                                m.setData(b);
                                mToastHandler.sendMessage(m);
                            } catch (IOException e) {
                                Log.e(TAG, "Could not connect to " + connection.address + ":" + connection.port);
                                Message m = new Message();
                                m.arg1 = 0;
                                Bundle b = new Bundle();
                                b.putString("TEXT", "Could not connect to " + connection.address + ":" + connection.port);
                                m.setData(b);
                                mToastHandler.sendMessage(m);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                            if (l != null)
                                l.quit();
                        }
                    }
                };
                mProtoThread.start();
                castInterface.display.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
                layout.addView(castInterface.display);
                castInterface.display.setOnHoverListener(mouse);
                castInterface.display.setOnTouchListener(mouse);
                castInterface.display.setOnGenericMotionListener(mouse);

/*            new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    return onTouchEvent(event);
                }
            });*/
            }
        }

    }

}
