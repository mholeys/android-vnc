package uk.co.mholeys.android.vnc;

import android.app.Activity;
import android.app.MediaRouteActionProvider;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.media.MediaRouter;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;

import java.net.InetAddress;
import java.net.UnknownHostException;

import uk.co.mholeys.android.vnc.display.VNCPresentation;

public class PresentationActivity extends Activity {

    private final String TAG = "PresentationVNC";

    private MediaRouter mMediaRouter;
    private VNCPresentation mPresentation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get the media router service.
        mMediaRouter = (MediaRouter)getSystemService(Context.MEDIA_ROUTER_SERVICE);
        setContentView(R.layout.activity_presentation);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mMediaRouter.addCallback(MediaRouter.ROUTE_TYPE_LIVE_VIDEO, mMediaRouterCallback);
        updatePresentation();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mMediaRouter.removeCallback(mMediaRouterCallback);

        updateContents();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Be sure to call the super class.
        super.onCreateOptionsMenu(menu);

        // Inflate the menu and configure the media router action provider.
        getMenuInflater().inflate(R.menu.presentation_vnc_menu, menu);

        MenuItem mediaRouteMenuItem = menu.findItem(R.id.menu_media_route);

        MediaRouteActionProvider mediaRouteActionProvider =
                (MediaRouteActionProvider)mediaRouteMenuItem.getActionProvider();
        mediaRouteActionProvider.setRouteTypes(MediaRouter.ROUTE_TYPE_LIVE_VIDEO);

        return true;
    }

    private void updatePresentation() {
        // Get the current route and its presentation display.
        MediaRouter.RouteInfo route = mMediaRouter.getSelectedRoute(
                MediaRouter.ROUTE_TYPE_LIVE_VIDEO);
        Display presentationDisplay = route != null ? route.getPresentationDisplay() : null;

        // Dismiss the current presentation if the display has changed.
        if (mPresentation != null && mPresentation.getDisplay() != presentationDisplay) {
            Log.i(TAG, "Dismissing presentation because the current route no longer "
                    + "has a presentation display.");
            mPresentation.dismiss();
            mPresentation = null;
        }

        // Show a new presentation if needed.
        if (mPresentation == null && presentationDisplay != null) {
            Log.i(TAG, "Showing presentation on display: " + presentationDisplay);
            InetAddress address = null;
            mPresentation = new VNCPresentation(this, presentationDisplay, address, "Server", 5901, "superuser");
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
}
