package uk.co.mholeys.android.vnc;

import android.app.PendingIntent;
import android.app.Presentation;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;
import com.google.android.gms.common.api.Status;

import java.net.InetAddress;

public class CastActivity extends AppCompatActivity {

    public static final String TAG = "MainAct";
    public static final String REMOTE_DISPLAY_APP_ID = "B461FB4F";

    MediaRouter mMediaRouter;
    MediaRouteSelector mMediaRouteSelector;
    CastDevice mSelectedDevice;

    ServerData connection;

    MyMediaRouterCallback mMediaRouterCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cast);

        // Get a media router to use
        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        // Find a cast device that is compatible
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory( CastMediaControlIntent.categoryForCast(REMOTE_DISPLAY_APP_ID))
                .build();
        mMediaRouterCallback = new MyMediaRouterCallback();

        Intent intent = getIntent();
        connection = new ServerData();
        connection.inetAddress = (InetAddress) intent.getSerializableExtra(ServerListActivity.SERVER_INFO_CONNECTION);
        connection.address = intent.getStringExtra(ServerListActivity.SERVER_INFO_ADDRESS);
        connection.port = intent.getIntExtra(ServerListActivity.SERVER_INFO_PORT, 0);
        connection.password = intent.getStringExtra(ServerListActivity.SERVER_INFO_PASSWORD);
    }

    @Override
    protected void onStart() {
        // Look for cast devices
        super.onStart();
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
    }

    @Override
    protected void onStop() {
        // Stop looking for cast devices
        mMediaRouter.removeCallback(mMediaRouterCallback);
        super.onStop();
    }

    public void setup() {
        Intent intent = new Intent(CastActivity.this,
                CastActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent notificationPendingIntent = PendingIntent.getActivity(
                CastActivity.this, 0, intent, 0);

        CastRemoteDisplayLocalService.NotificationSettings settings =
                new CastRemoteDisplayLocalService.NotificationSettings.Builder()
                        .setNotificationPendingIntent(notificationPendingIntent).build();

        CastRemoteDisplayLocalService.startService(
                getApplicationContext(),
                PresentationService.class, REMOTE_DISPLAY_APP_ID,
                mSelectedDevice, settings,
                new CastRemoteDisplayLocalService.Callbacks() {
                    @Override
                    public void onServiceCreated(
                            CastRemoteDisplayLocalService service) {
                        connection.prepare();
                        PresentationService pService = (PresentationService) service;
                        pService.connection = connection;
                        Log.d(TAG, "onServiceCreated");
                    }

                    @Override
                    public void onRemoteDisplaySessionStarted(
                            CastRemoteDisplayLocalService service) {
                        // initialize sender UI
                        PresentationService pService = (PresentationService) service;
                    }

                    @Override
                    public void onRemoteDisplaySessionError(
                            Status errorReason){
                        initError(errorReason);
                    }
                });
    }

    public void initError(Status errorReason) {
        // Failed to initialise
        Log.e(TAG, "Failed to initialise " + errorReason.getStatusMessage());
    }

    public void stopButtonCall(View view) {
        teardown();
        Intent intent = new Intent(this, ServerListActivity.class);
        startActivity(intent);
    }

    public void teardown() {
        // Stop everything the cast has ended
        Log.e(TAG, "Should have stopped things. TODO URGENT");
        CastRemoteDisplayLocalService.stopService();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Add actionbar menu
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.cast_activity_menu, menu);
        // Setup media router (cast) button
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);
        MediaRouteActionProvider mediaRouteActionProvider =
                (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
        return true;
    }

    private class MyMediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
            mSelectedDevice = CastDevice.getFromBundle(info.getExtras());
            String routeId = info.getId();

            setup();
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
            teardown();
            mSelectedDevice = null;
            CastRemoteDisplayLocalService.stopService();
        }
    }
}
