package uk.co.mholeys.android.vnc;

import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.display.DisplayManager;
import android.media.MediaRouter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import java.net.InetAddress;

import uk.co.mholeys.android.vnc.display.VNCPresentation;

/**
 * Created by Matthew on 17/12/2017.
 */

public class ExternalInputActivity extends InputActivity {

    private final String TAG = "ExtnInput";

    private ServerData connection;
    private VNCPresentation mPresentation;
    private DisplayManager mDisplayManager;
    private Display mDisplay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupPresentation();
    }

    @Override
    public void onResume() {
        super.onResume();
        setupPresentation();
    }

    private void setupPresentation() {
        Log.d(TAG, "setupPresentation: Updating");
        Intent intent = getIntent();
        int displayId = intent.getIntExtra(ServerListActivity.PRESENTATION_DISPLAY_ID, -1);
        if (displayId == -1) {
            returnToServerList();
            Log.d(TAG, "onResume: Invalid display id so cannot render, returning to server list");
            return;
        }
        mDisplayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);
        mDisplay = mDisplayManager.getDisplay(displayId);

        connection = new ServerData();
        connection.inetAddress = (InetAddress) intent.getSerializableExtra(ServerListActivity.SERVER_INFO_CONNECTION);
        connection.address = intent.getStringExtra(ServerListActivity.SERVER_INFO_ADDRESS);
        connection.port = intent.getIntExtra(ServerListActivity.SERVER_INFO_PORT, 0);
        connection.password = intent.getStringExtra(ServerListActivity.SERVER_INFO_PASSWORD);

        connection.prepare();

        updatePresentation();

        if (mPresentation != null) {
            mPresentation.onResume();

            layout.setOnTouchListener(mPresentation.mouse);
            layout.setOnHoverListener(mPresentation.mouse);
            layout.setOnGenericMotionListener(mPresentation.mouse);
        } else {
            returnToServerList();
        }
    }

    private void returnToServerList() {
        Intent serverListIntent = new Intent(ExternalInputActivity.this, ServerListActivity.class);
        startActivity(serverListIntent);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mPresentation != null) {
            Log.i(TAG, "Dismissing presentation because the activity is no longer visible.");
            mPresentation.dismiss();
            mPresentation = null;
        }
    }

    private void updatePresentation() {
        // Dismiss the current presentation if the display has changed.
        if (mPresentation != null && mPresentation.getDisplay() != mDisplay) {
            Log.i(TAG, "Dismissing presentation because the current route no longer "
                    + "has a presentation display.");
            mPresentation.dismiss();
            mPresentation = null;
        }

        // Show a new presentation if needed.
        if (mPresentation == null && mDisplay != null) {
            Log.i(TAG, "Showing presentation on display: " + mDisplay.getName());
            mPresentation = new VNCPresentation(this, mDisplay, connection);
            mPresentation.mToastHandler = new ExternalInputActivity.ToastHandler();
            mPresentation.setOnDismissListener(mOnDismissListener);
            try {
                mPresentation.show();
            } catch (WindowManager.InvalidDisplayException ex) {
                Log.w(TAG, "Couldn't show presentation!  Display was removed in "
                        + "the meantime.", ex);
                mPresentation = null;
            }
        }

        // Update the contents playing in this activity.
        updateContents();
    }

    private void updateContents() {
        // Show either the content in the main activity or the content in the presentation
        // along with some descriptive text about what is happening.
        // TODO: decide if this should just kick back to main server list?
        // TODO: make this do what it says
        if (mPresentation != null) {
            Log.i(TAG, "Remote" + mPresentation.getDisplay().getName());
        } else {
            Log.i(TAG, "Local" + getWindowManager().getDefaultDisplay().getName());
        }
    }

    private final MediaRouter.SimpleCallback mMediaRouterCallback =
            new MediaRouter.SimpleCallback() {
                @Override
                public void onRouteSelected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
                    Log.d(TAG, "onRouteSelected: type=" + type + ", info=" + info);
                    updatePresentation();
                }

                @Override
                public void onRouteUnselected(MediaRouter router, int type, MediaRouter.RouteInfo info) {
                    Log.d(TAG, "onRouteUnselected: type=" + type + ", info=" + info);
                    updatePresentation();
                }

                @Override
                public void onRoutePresentationDisplayChanged(MediaRouter router, MediaRouter.RouteInfo info) {
                    Log.d(TAG, "onRoutePresentationDisplayChanged: info=" + info);
                    updatePresentation();
                }
            };

    /**
     * Listens for when presentations are dismissed.
     */
    private final DialogInterface.OnDismissListener mOnDismissListener =
            new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (dialog == mPresentation) {
                        Log.i(TAG, "Presentation was dismissed.");
                        mPresentation = null;
                        updateContents();
                    }
                }
            };

    public class ToastHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            int state = message.arg1;
            switch (state) {
                case 0:
                    final String text = message.getData().getString("TEXT");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(ExternalInputActivity.this, text, Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case 1:
                    returnToServerList();
            }
        }
    }

}
