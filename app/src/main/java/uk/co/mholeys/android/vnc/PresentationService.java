package uk.co.mholeys.android.vnc;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.cast.CastPresentation;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;

import java.io.IOException;

import uk.co.mholeys.android.vnc.display.AndroidDisplay;
import uk.co.mholeys.android.vnc.display.AndroidInterface;
import uk.co.mholeys.android.vnc.display.AndroidScreen;
import uk.co.mholeys.android.vnc.display.CastInterface;
import uk.co.mholeys.android.vnc.input.AndroidKeyboard;
import uk.co.mholeys.android.vnc.input.AndroidMouse;
import uk.co.mholeys.vnc.VNCConnectionException;
import uk.co.mholeys.vnc.data.Encoding;
import uk.co.mholeys.vnc.data.EncodingSettings;
import uk.co.mholeys.vnc.log.Logger;
import uk.co.mholeys.vnc.net.VNCProtocol;

public class PresentationService extends CastRemoteDisplayLocalService {

    private static final String TAG = "VNC.pres.service";
    VNCPresentation mPresentation;

    private static Thread mProtoThread;
    private static VNCProtocol protocol;
    static ServerData connection;
    private static CastInterface castInterface;
    private static AndroidScreen screen;
    private static AndroidDisplay display;
    private static AndroidMouse mouse;
    private static AndroidKeyboard keyboard;

    public PresentationService() {

    }

    @Override
    public void onCreatePresentation(Display display) {
        // Setup vnc thing maybe?
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
        dismissPresentation();
        mPresentation = new VNCPresentation(this, display, connection);
        try {
            mPresentation.show();
        } catch (WindowManager.InvalidDisplayException ex) {
            Log.e(TAG, "Unable to show presentation, display was " +
                    "removed.", ex);
            dismissPresentation();
        }
    }

    public final static class VNCPresentation extends CastPresentation {

        private ServerData connection;
        private boolean mReady = true;
        private Display castDdisplay;

        public VNCPresentation(Context context,
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

            while (connection.result != -1) {
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (connection.getAddress() == null) {
                Logger.logger.printLn("CAST: Failed to connect to host");
                mReady = false;
            }

            final ServerData connection = this.connection;
            EncodingSettings preferedEncoding = new EncodingSettings()
                    .addEncoding(Encoding.TIGHT_ENCODING)
                    .addEncoding(Encoding.ZLIB_ENCODING)
                    .addEncoding(Encoding.CORRE_ENCODING)
                    .addEncoding(Encoding.RRE_ENCODING)
                    .addEncoding(Encoding.RAW_ENCODING)
                    .addEncoding(Encoding.JPEG_QUALITY_LEVEL_2_PSEUDO_ENCODING)
                    .addEncoding(Encoding.COMPRESSION_LEVEL_0_PSEUDO_ENCODING)
                    //.addEncoding(Encoding.CURSOR_PSEUDO_ENCODING)
                    ;
            connection.setPrefferedEncoding(preferedEncoding);


            castInterface = new CastInterface(this, new AndroidDisplay(getContext(), null));
            mouse = (AndroidMouse) castInterface.getMouseManager();
            keyboard = (AndroidKeyboard) castInterface.getKeyboardManager();

            RelativeLayout layout = (RelativeLayout) findViewById(R.id.layoutCastVnc);

            Point p = new Point();
            castDdisplay.getSize(p);
            castInterface.androidWidth = p.x;
            castInterface.androidHeight = p.y;

            if (mReady) {
                mProtoThread = new Thread() {
                    @Override
                    public void run() {
                        if (Looper.myLooper() == null) {
                            Looper.prepare();
                            Looper l = Looper.myLooper();

                            try {
                                protocol = new VNCProtocol(connection, castInterface, logger);
                                protocol.run();
                            } catch (VNCConnectionException e) {
                                final String reason = e.toString();
                                logger.printLn(reason);
                                /*getContext().runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(activity, reason, Toast.LENGTH_SHORT).show();
                                    }
                                });*/
                            } catch (IOException e) {
                                logger.printLn("Could not connect to ");
                                /*activity.runOnUiThread(new Runnable() {
                                    public void run() {
                                        Toast.makeText(activity, "Could not connect to ", Toast.LENGTH_SHORT).show();
                                    }
                                });*/
                                Intent i = new Intent(getContext(), ServerListActivity.class);
                                getContext().startActivity(i);
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
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
