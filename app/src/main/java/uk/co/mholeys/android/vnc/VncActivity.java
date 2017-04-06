package uk.co.mholeys.android.vnc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import uk.co.mholeys.android.vnc.display.AndroidScreen;
import uk.co.mholeys.android.vnc.input.AndroidKeyboard;
import uk.co.mholeys.android.vnc.input.AndroidMouse;
import uk.co.mholeys.vnc.VNCConnectionException;
import uk.co.mholeys.vnc.data.Encoding;
import uk.co.mholeys.vnc.data.EncodingSettings;
import uk.co.mholeys.vnc.log.Logger;
import uk.co.mholeys.vnc.net.VNCProtocol;

import java.io.IOException;
import java.net.InetAddress;

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
    private AndroidMouse mouse;
    private AndroidKeyboard keyboard;

    private boolean keyboardState = false;

    private View mDecorView;

    float lastScrollX = 0;
    float lastScrollY = 0;

    float lastX = 0;
    float lastY = 0;

    double differenceX;
    double differenceY;

    double scale = 1;

    boolean click = false;
    boolean left = false;
    boolean right = false;
    int clickCount = 0;
    long lastClick = 0;

    static ScaleGestureDetector scaleDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vnc);

        final Logger logger = new Logger(new LoggerOutStream());
        logger.logLevel = Logger.LOG_LEVEL_NORMAL;

        final Activity activity = this;

        mDecorView = getWindow().getDecorView();
        scaleDetector = new ScaleGestureDetector(getApplicationContext(), new ScaleListener());

        /*Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);*/

        Intent intent = getIntent();
        connection = new ServerData();
        connection.inetAddress = (InetAddress) intent.getSerializableExtra(ServerList.SERVER_INFO_CONNECTION);
        connection.address = intent.getStringExtra(ServerList.SERVER_INFO_ADDRESS);
        connection.port = intent.getIntExtra(ServerList.SERVER_INFO_PORT, 0);
        connection.password = intent.getStringExtra(ServerList.SERVER_INFO_PASSWORD);

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
                .addEncoding(Encoding.JPEG_QUALITY_LEVEL_1_PSEUDO_ENCODING)
                .addEncoding(Encoding.COMPRESSION_LEVEL_0_PSEUDO_ENCODING)
                .addEncoding(Encoding.CURSOR_PSEUDO_ENCODING);
        connection.setPrefferedEncoding(preferedEncoding);


        androidInterface = new AndroidInterface(this, new AndroidDisplay(activity, null));
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
                            activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(activity, reason, Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (IOException e) {
                            activity.runOnUiThread(new Runnable() {
                                public void run() {
                                    Toast.makeText(activity, "Could not connect to ", Toast.LENGTH_SHORT).show();
                                }
                            });
                        /*Intent i = new Intent(activity, MainActivity.class);
                        activity.startActivity(i);*/
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
            androidInterface.display.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return onTouchEvent(event);
                }
            });
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        screen = (AndroidScreen) androidInterface.getScreen();
        int action = MotionEventCompat.getActionMasked(event);

        int pointerCount = event.getPointerCount();
        int id = MotionEventCompat.getActionIndex(event);
        final int pointerIndex = MotionEventCompat.getActionIndex(event);

        long sinceLast = lastClick - System.currentTimeMillis();
        lastClick = System.currentTimeMillis();
        if (sinceLast > 200) {
            Logger.logger.printLn("Reset click count was: " + clickCount);
            clickCount = 0;
        }

        switch (action) {
            case (MotionEvent.ACTION_DOWN):
                if (pointerCount == 1) {
                    lastX = event.getX(pointerIndex);
                    lastY = event.getY(pointerIndex);
                    click = true;
                    clickCount++;
                }
                /*if (pointerCount == 2) {
                    lastScrollX = event.getX(pointerIndex);
                    lastScrollY = event.getY(pointerIndex);
                }*/
                break;
            case (MotionEvent.ACTION_POINTER_DOWN):
                /*if (pointerCount == 4) {
                    //Toggle keyboard
                    if (keyboardState) {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                    } else {
                        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                    }
                    Logger.logger.printLn("Toggled keyboard");
                    keyboardState = !keyboardState;
                }*/
                break;
            case (MotionEvent.ACTION_MOVE):
                if (pointerCount == 1) {
                    if (clickCount == 1) {
                        click = false;
                    }
                    differenceX = lastX - event.getX();
                    differenceY = lastY - event.getY();
                    lastX = event.getX();
                    lastY = event.getY();
                    if (clickCount == 2) {
                        mouse.localX -= differenceX;
                        mouse.localY -= differenceY;
                        mouse.left = click;
                        mouse.right = false;
                        mouse.addToQueue();
                    }
                }
                /*if (pointerCount == 1) {
                    float x = event.getX(pointerIndex);
                    float y = event.getY(pointerIndex);

                    mouse.localX += (short)(lastX - x);
                    mouse.localY += (short)(lastY - y);
                    lastX = x;
                    lastY = y;

                    if (clickCount == 2) {
                        left = true;
                        right = false;
                    } else {
                        left = false;
                        right = false;
                        click = false;
                    }
                } else if (pointerCount == 2) {
                    float x = event.getX(pointerIndex);
                    float y = event.getY(pointerIndex);
                    xOffset = lastScrollX - x;
                    yOffset = lastScrollY - y;
                    screen.cutX -= xOffset;
                    screen.cutY -= yOffset;
                    screen.update();
                    lastScrollX = x;
                    lastScrollY = y;
                } else if (pointerCount == 3) {
                    scale = 1f;

                    xOffset = 0;
                    yOffset = 0;
                    lastScrollX = 0;
                    lastScrollY = 0;
                    screen.zoomScale = scale;
                    screen.cutX = 0;
                    screen.cutY = 0;
                    screen.update();
                }*/
                scaleDetector.onTouchEvent(event);
                break;
            case (MotionEvent.ACTION_UP):
                differenceX = lastX - event.getX();
                differenceY = lastY - event.getY();
                if (click) {
                    if (clickCount == 1) {
                        mouse.localX -= differenceX;
                        mouse.localY -= differenceY;
                        mouse.left = true;
                        mouse.right = false;
                        mouse.addToQueue();
                    } else if (clickCount == 3) {
                        mouse.localX -= differenceX;
                        mouse.localY -= differenceY;
                        mouse.left = false;
                        mouse.right = true;
                        mouse.addToQueue();
                    }
                    click = false;
                    clickCount = 0;
                }
                mouse.left = false;
                mouse.right = false;
                mouse.addToQueue();
                break;
            case (MotionEvent.ACTION_CANCEL):
                left = false;
                right = false;
                click = false;
                clickCount = 0;
                break;
            case (MotionEvent.ACTION_OUTSIDE):
                break;
            default:
                break;
        }
        return true;
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            scale *= detector.getScaleFactor();
            scale = Math.max(1.0f, Math.min(scale, 5.0f));
            screen.zoomScale = scale;
            screen.update();
            return true;
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

