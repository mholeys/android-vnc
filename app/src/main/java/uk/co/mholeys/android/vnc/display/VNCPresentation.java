package uk.co.mholeys.android.vnc.display;

import android.app.Activity;
import android.app.Presentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;

import uk.co.mholeys.android.vnc.LoggerOutStream;
import uk.co.mholeys.android.vnc.PresentationActivity;
import uk.co.mholeys.android.vnc.R;
import uk.co.mholeys.android.vnc.ServerData;
import uk.co.mholeys.android.vnc.ServerListActivity;
import uk.co.mholeys.android.vnc.input.AndroidKeyboard;
import uk.co.mholeys.android.vnc.input.AndroidMouse;
import uk.co.mholeys.vnc.VNCConnectionException;
import uk.co.mholeys.vnc.data.Encoding;
import uk.co.mholeys.vnc.data.EncodingSettings;
import uk.co.mholeys.vnc.log.Logger;
import uk.co.mholeys.vnc.net.VNCProtocol;

/**
 * Created by Matthew on 28/06/2017.
 */

public class VNCPresentation extends Presentation {

    static final String TAG = "VNCPresentation";

    Context outerConext;

    private Thread mProtoThread;
    private VNCProtocol protocol;
    private boolean mReady = true;
    private ServerData connection;
    private AndroidInterface androidInterface;
    private AndroidScreen vncScreen;
    private AndroidDisplay vncDisplay;
    private AndroidMouse mouse;
    private AndroidKeyboard keyboard;

    private boolean keyboardState = false;

    private View mDecorView;
    public PresentationActivity.ToastHandler mToastHandler;

    public VNCPresentation(Context outerContext, Display display, ServerData connection) {
        super(outerContext, display);
        this.outerConext = outerContext;

        this.connection = connection;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vnc);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);



        final Logger logger = new Logger(new LoggerOutStream());
        logger.logLevel = Logger.LOG_LEVEL_NONE;

        mDecorView = getWindow().getDecorView();

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/

        if (connection.getAddress() == null || connection.result == 0) {
            Log.e(TAG, "Failed to get InetAddress");
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


        androidInterface = new AndroidInterface(outerConext, new AndroidDisplay(outerConext, null));
        mouse = (AndroidMouse) androidInterface.getMouseManager();
        keyboard = (AndroidKeyboard) androidInterface.getKeyboardManager();

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.layoutVnc);

        if (mReady) {
            mProtoThread = new Thread() {
                @Override
                public void run() {
                    if (Looper.myLooper() == null) {
                        Looper.prepare();
                        Looper l = Looper.myLooper();

                        try {
                            protocol = new VNCProtocol(connection, androidInterface, logger);
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
                        l.quit();
                    }
                }
            };
            mProtoThread.start();
            androidInterface.display.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
            layout.addView(androidInterface.display);
            androidInterface.display.setOnTouchListener(mouse);
            /*new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return onTouchEvent(event);
                }
            });*/
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            mDecorView.setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            protocol.disconnect();
        } catch (Exception e) { }
    }

}
