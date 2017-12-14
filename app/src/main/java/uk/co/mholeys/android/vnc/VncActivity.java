package uk.co.mholeys.android.vnc;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import uk.co.mholeys.android.vnc.display.AndroidScreen;
import uk.co.mholeys.android.vnc.input.AndroidKeyboard;
import uk.co.mholeys.android.vnc.input.AndroidMouse;
import uk.co.mholeys.android.vnc.input.AndroidMouse2;
import uk.co.mholeys.vnc.VNCConnectionException;
import uk.co.mholeys.vnc.data.Encoding;
import uk.co.mholeys.vnc.data.EncodingSettings;
import uk.co.mholeys.vnc.log.Logger;
import uk.co.mholeys.vnc.net.VNCProtocol;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketException;

import uk.co.mholeys.android.vnc.display.AndroidDisplay;
import uk.co.mholeys.android.vnc.display.AndroidInterface;

public class VncActivity extends AppCompatActivity {

    private Thread mProtoThread;
    private VNCProtocol protocol;
    private boolean mReady = true;
    private ServerData connection;
    private AndroidInterface androidInterface;
    private AndroidScreen screen;
    private AndroidDisplay display;
    private AndroidMouse2 mouse;
    private AndroidKeyboard keyboard;

    private boolean keyboardState = false;

    private View mDecorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vnc);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final Logger logger = new Logger(new LoggerOutStream());
        logger.logLevel = Logger.LOG_LEVEL_NONE;

        final Activity activity = this;

        mDecorView = getWindow().getDecorView();

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/

        Intent intent = getIntent();
        connection = new ServerData();
        connection.inetAddress = (InetAddress) intent.getSerializableExtra(ServerListActivity.SERVER_INFO_CONNECTION);
        connection.address = intent.getStringExtra(ServerListActivity.SERVER_INFO_ADDRESS);
        connection.port = intent.getIntExtra(ServerListActivity.SERVER_INFO_PORT, 0);
        connection.password = intent.getStringExtra(ServerListActivity.SERVER_INFO_PASSWORD);

        connection.prepare();
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
            //Logger.logger.printLn("Failed to connect to host");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, "Failed to connect to host", Toast.LENGTH_LONG);
                }
            });
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


        androidInterface = new AndroidInterface(this, new AndroidDisplay(activity, null));
        mouse = (AndroidMouse2) androidInterface.getMouseManager();
        keyboard = (AndroidKeyboard) androidInterface.getKeyboardManager();

        RelativeLayout layout = (RelativeLayout) findViewById(R.id.layoutVnc);

        Point p = new Point();
        getWindowManager().getDefaultDisplay().getSize(p);
        androidInterface.androidWidth = p.x;
        androidInterface.androidHeight = p.y;

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
                            activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(activity, reason, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (SocketException e) {
                            activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(activity, "Connection ended unexpectedly", Toast.LENGTH_SHORT).show();
                                }
                            });
                            Intent i = new Intent(activity, ServerListActivity.class);
                            activity.startActivity(i);
                        } catch (IOException e) {
                            activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(activity, "Could not connect to ", Toast.LENGTH_SHORT).show();
                                }
                            });
                            Intent i = new Intent(activity, ServerListActivity.class);
                            activity.startActivity(i);
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
            androidInterface.display.setOnHoverListener(mouse);
            androidInterface.display.setOnGenericMotionListener(mouse);

/*            new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    return onTouchEvent(event);
                }
            });*/
        }
    }

    @Override
    public boolean onKeyDown(int code, KeyEvent e) {
        if (super.onKeyDown(code, e)) return true;

        if (protocol != null) {
            if (protocol.ui != null) {
                if (protocol.ui.getKeyboardManager() != null) {
                    //Log.d("VNCActivity", "Key pressed: " + code + " " + e.getModifiers());
                    ((AndroidKeyboard)protocol.ui.getKeyboardManager()).addKey(e, true);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onKeyUp(int code, KeyEvent e) {
        if (super.onKeyUp(code, e)) return true;

        if (protocol != null) {
            if (protocol.ui != null) {
                if (protocol.ui.getKeyboardManager() != null) {
                    //Log.d("VNCActivity", "Key pressed: " + code + " " + e.getModifiers());
                    ((AndroidKeyboard) protocol.ui.getKeyboardManager()).addKey(e, false);
                    return true;
                }
            }
        }
        return false;
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
            mProtoThread.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

