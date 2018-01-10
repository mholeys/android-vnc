package uk.co.mholeys.android.vnc;

import android.app.Dialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.view.Display;
import android.view.Menu;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;
import com.google.android.gms.common.api.Status;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import uk.co.mholeys.android.vnc.data.SQLHelper;
import uk.co.mholeys.android.vnc.data.ServerEntry;

public class ServerListActivity extends AppCompatActivity {

    public final static String TAG = "vnc.ServerListActivity";

    public final static String SERVER_INFO_DB_ID = "uk.co.mholeys.android.vnc.serverinfo.db_id";
    public final static String SERVER_INFO_NAME = "uk.co.mholeys.android.vnc.serverinfo.name";
    public final static String SERVER_INFO_ADDRESS = "uk.co.mholeys.android.vnc.serverinfo.address";
    public final static String SERVER_INFO_PORT = "uk.co.mholeys.android.vnc.serverinfo.port";
    public final static String SERVER_INFO_PASSWORD = "uk.co.mholeys.android.vnc.serverinfo.password";
    public static final String SERVER_INFO_CONNECTION = "uk.co.mholeys.android.vnc.serverinfo.connection";

    public static final String DISPLAY_MODE = "uk.co.mholeys.android.vnc-display-mode";
    public static final String PRESENTATION_DISPLAY_ID = "uk.co.mholeys.android.vnc-presentation-display-id";
    public static final int DISPLAY_MODE_CAST = 0;
    public static final int DISPLAY_MODE_PRESENTATION = 1;

            ArrayAdapter<ServerEntry> listItems;
    ListView serverList;

    public static final String REMOTE_DISPLAY_APP_ID = "B461FB4F";

    MediaRouter mCastMediaRouter;
    MediaRouteSelector mCastMediaRouteSelector;
    MyMediaRouterCallback mCastMediaRouterCallback;
    CastDevice mSelectedCastDevice;
    boolean mCasting = false;

    DisplayManager.DisplayListener mDisplayListener;
    DisplayManager mDisplayManager;
    ArrayList<Display> mDisplays = new ArrayList<Display>();
    Display mSelectedDisplay = null;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.server_list_menu, menu);

        // Setup media router (cast) button
        MenuItem mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item);

        MediaRouteActionProvider mediaRouteActionProvider =
                (MediaRouteActionProvider) MenuItemCompat.getActionProvider(mediaRouteMenuItem);
        mediaRouteActionProvider.setRouteSelector(mCastMediaRouteSelector);

        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_servers);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Servers", Snackbar.LENGTH_LONG)
                        .setAction("Add", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Log.d(TAG, "FAB: add action");
                                addServerIntent();
                            }
                        }).show();
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        // groupId, itemId, order, title
        menu.add(0, v.getId(), 0, "Connect");
        //menu.add(0, v.getId(), 0, "Cast");
        menu.add(0, v.getId(), 0, "Delete");
        menu.add(0, v.getId(), 0, "Edit");
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        String option = (String) item.getTitle();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ServerEntry serverEntry = listItems.getItem((int) info.id);
        ServerData server = serverEntry.serverData;
        switch (option) {
            case "Connect":
                startVncViewer(server);
                break;
            case "Delete":
                deleteServer(serverEntry);
                loadServers();
                break;
            case "Edit":
                editServerIntent(serverEntry);
                break;
            default:
                return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.add_server_action_bar_button) {
            Log.d(TAG, "ACTION: add action");
            addServerIntent();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void addServerIntent() {
        Intent intent = new Intent(this, AddServerActivity.class);
        startActivity(intent);
    }

    public void editServerIntent(ServerEntry server) {
        Intent intent = new Intent(this, EditServerActivity.class);
        intent.putExtra(SERVER_INFO_DB_ID, server.dbID);
        intent.putExtra(SERVER_INFO_NAME, server.serverData.name);
        intent.putExtra(SERVER_INFO_ADDRESS, server.serverData.address);
        intent.putExtra(SERVER_INFO_PASSWORD, server.serverData.password);
        intent.putExtra(SERVER_INFO_PORT, server.serverData.port);
        startActivity(intent);
    }

    public void deleteServer(ServerEntry server) {
        listItems.remove(server);
        SQLiteDatabase db = new SQLHelper(this).getWritableDatabase();
        db.beginTransaction();
        db.delete(SQLHelper.SERVERS_TABLE_NAME,
                SQLHelper.SERVER_COLUMN_ID + "=?",
                new String[] {""+server.dbID});
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public void startVncViewer(final ServerData server) {
        if (mSelectedCastDevice != null) {
            startCastViewer(server);
        } else if (mDisplays.size() > 0) {
            if (mSelectedDisplay != null) {
                Point size = new Point();
                mSelectedDisplay.getSize(size);
                Log.d(TAG, "Selected display: " + mSelectedDisplay.getName() + " " + size.x + "x" + size.y);
                startVncPresentation(server, mSelectedDisplay);
                return;
            }
            if (mDisplays.size() == 1 && mDisplays.contains(getWindowManager().getDefaultDisplay())) {
                startVncBuiltInDisplay(server);
                return;
            }

            // Show display picker dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(ServerListActivity.this);
            builder.setTitle(R.string.display_list_select_title);
            List<MediaRouter.RouteInfo> castRoutes = mCastMediaRouter.getRoutes();
            Log.d("Cast menu", ""+castRoutes.size());

            int castDevices = 0;
            for (int i = 0; i < castRoutes.size(); i++) {
                if (castRoutes.get(i).getDeviceType() == MediaRouter.RouteInfo.DEVICE_TYPE_TV) {
                    castDevices++;
                }
            }

            final String[] displayNames = new String[castDevices + mDisplays.size()];
            final HashMap<String, Object> displays = new HashMap<String, Object>();
            int c = 0;
            for (MediaRouter.RouteInfo r : castRoutes) {
                if (r.getDeviceType() == MediaRouter.RouteInfo.DEVICE_TYPE_TV) {
                    displayNames[c] = r.getName();
                    displays.put(displayNames[c], r);
                    c++;
                }
            }
            for (int i = 0; i < mDisplays.size(); i++) {
                displayNames[i + castDevices] = mDisplays.get(i).getName();
                displays.put(mDisplays.get(i).getName(), mDisplays.get(i));
            }
            final int castDeviceOffset = castDevices;
            builder.setItems(displayNames, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    String name = displayNames[which];

                    if (displays.get(name) instanceof Display) {
                        if (mDisplays.get(which-castDeviceOffset).equals(getWindowManager().getDefaultDisplay())) {
                            startVncBuiltInDisplay(server);
                            return;
                        }
                        mSelectedDisplay = mDisplays.get(which-castDeviceOffset);
                        startVncPresentation(server, mSelectedDisplay);
                        return;
                    } else if (displays.get(name) instanceof MediaRouter.RouteInfo) {
                        MediaRouter.RouteInfo routeInfo = (MediaRouter.RouteInfo) displays.get(name);
                        mSelectedCastDevice = CastDevice.getFromBundle(routeInfo.getExtras());
                        startCastViewer(server);
                        return;
                    }
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            Log.d(TAG, "Dialog shown");
        } else {
            startVncBuiltInDisplay(server);
        }
    }

    public void startVncPresentation(final ServerData server, Display display) {
        if (display == getWindowManager().getDefaultDisplay()) {
            startVncBuiltInDisplay(server);
            return;
        }
        Toast.makeText(this, "Vnc presentation mode started", Toast.LENGTH_LONG).show();
        // Start presentation
        Intent intent = new Intent(this, ExternalInputActivity.class);
        intent.putExtra(SERVER_INFO_CONNECTION, server.inetAddress);
        intent.putExtra(SERVER_INFO_ADDRESS, server.address);
        intent.putExtra(SERVER_INFO_PORT, server.port);
        intent.putExtra(SERVER_INFO_PASSWORD, server.password);
        intent.putExtra(PRESENTATION_DISPLAY_ID, mSelectedDisplay.getDisplayId());
        startActivity(intent);
    }

    public void startVncBuiltInDisplay(final ServerData server) {
        mSelectedDisplay = null;
        Intent intent = new Intent(this, VncActivity.class);
        intent.putExtra(SERVER_INFO_CONNECTION, server.inetAddress);
        intent.putExtra(SERVER_INFO_ADDRESS, server.address);
        intent.putExtra(SERVER_INFO_PORT, server.port);
        intent.putExtra(SERVER_INFO_PASSWORD, server.password);
        startActivity(intent);
    }

    public void startCastViewer(final ServerData server) {
        if (server == null) {
            if (mSelectedCastDevice != null) {
                Toast.makeText(this, "Failed to cast to " + mSelectedCastDevice.getFriendlyName(), Toast.LENGTH_LONG).show();
            }
            return;
        }

        Intent intent = new Intent(ServerListActivity.this,
                ServerListActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent notificationPendingIntent = PendingIntent.getActivity(
                ServerListActivity.this, 0, intent, 0);

        // TODO: improve notification if possible without v3/caf
        CastRemoteDisplayLocalService.NotificationSettings settings =
                new CastRemoteDisplayLocalService.NotificationSettings.Builder()
                        .setNotificationPendingIntent(notificationPendingIntent).build();

        CastRemoteDisplayLocalService.startService(
                getApplicationContext(),
                CastPresentationService.class, REMOTE_DISPLAY_APP_ID,
                mSelectedCastDevice, settings,
                new CastRemoteDisplayLocalService.Callbacks() {
                    @Override
                    public void onServiceCreated(
                            CastRemoteDisplayLocalService service) {
                        CastPresentationService pService = (CastPresentationService) service;
                        Log.d(TAG, "CAST onServiceCreated: Starting service");
                        pService.connection = server;
                        pService.connection.prepare();

                        mCasting = true;
                    }

                    @Override
                    public void onRemoteDisplaySessionStarted(
                            CastRemoteDisplayLocalService service) {
                        Log.d(TAG, "CAST onRemoteDisplaySessionStarted: Started service");
                        // Initialize sender UI
                        CastPresentationService pService = (CastPresentationService) service;
                        mCasting = true;
                    }

                    @Override
                    public void onRemoteDisplaySessionError(
                            Status errorReason){
                        Log.d(TAG, "CAST onRemoteDisplaySessionError: ERROR in service");
                        initError(errorReason);
                    }
                });
        //TODO: change to not need any params in input activity
        Intent inputIntent = new Intent(this, CastInputActivity.class);
        startActivity(inputIntent);
    }

    private void loadServers() {
        SQLiteDatabase db = new SQLHelper(this).getReadableDatabase();

        db.beginTransaction();
        String[] projection = {
                SQLHelper.SERVER_COLUMN_ID,
                SQLHelper.SERVER_COLUMN_NAME,
                SQLHelper.SERVER_COLUMN_ADDRESS,
                SQLHelper.SERVER_COLUMN_PORT,
                SQLHelper.SERVER_COLUMN_PASSWORD
        };

        Cursor cursor = db.rawQuery("select * from " + SQLHelper.SERVERS_TABLE_NAME + " ORDER BY " + SQLHelper.SERVER_COLUMN_NAME + " ASC", null);

        listItems.clear();

        if (cursor.moveToFirst()) {
            while (cursor.isAfterLast() == false) {
                int id = cursor.getInt(cursor.getColumnIndex(SQLHelper.SERVER_COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(SQLHelper.SERVER_COLUMN_NAME));
                String address = cursor.getString(cursor.getColumnIndex(SQLHelper.SERVER_COLUMN_ADDRESS));
                int port = cursor.getInt(cursor.getColumnIndex(SQLHelper.SERVER_COLUMN_PORT));
                String password = cursor.getString(cursor.getColumnIndex(SQLHelper.SERVER_COLUMN_PASSWORD));

                ServerData sd = new ServerData();
                sd.name = name;
                sd.address = address;
                sd.port = port;
                sd.password = password;

                ServerEntry se = new ServerEntry();
                se.serverData = sd;
                se.dbID = id;
                listItems.add(se);
                serverList.setAdapter(listItems);

                cursor.moveToNext();
            }
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
    }

    public void initError(Status errorReason) {
        // Failed to initialise
        Log.e(TAG, "Failed to initialise " + errorReason.getStatusMessage());
        mCasting = false;
    }

    public void teardown() {
        // Stop everything the cast has ended
        CastRemoteDisplayLocalService.stopService();
        mCasting = false;
    }

    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Called");

        if (mSelectedCastDevice != null) {
            if (mCastMediaRouter != null) {
                if (mCastMediaRouter.getSelectedRoute() != null) {
                    // See if this was selected using the cast selection button
                    if (!mCastMediaRouter.getSelectedRoute().equals(mSelectedCastDevice)) {
                        // Assume that this was picked using the popup so we need to ignore it
                        Log.d(TAG, "onResume: Selected device does not match cast selected");
                        mSelectedCastDevice = null;

                    }
                }
            }
        }
        // Always reset the display selection
        mSelectedDisplay = null;

        // Setup server list
        serverList = (ListView) findViewById(R.id.server_list_view);
        listItems = new ArrayAdapter<ServerEntry>(this, R.layout.list_layout);

        serverList.setOnCreateContextMenuListener(this);

        serverList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Open up vnc
                ServerData server = ((ServerEntry)adapterView.getAdapter().getItem(i)).serverData;
                startVncViewer(server);
            }
        });

        loadServers();
        setupDisplays();
    }

    protected void onPause() {
        // Stop looking for cast devices
        mCastMediaRouter.removeCallback(mCastMediaRouterCallback);
        mDisplayManager.unregisterDisplayListener(mDisplayListener);
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    protected void setupDisplays() {
        // Setup cast button
        // Get a media router to use
        mCastMediaRouter = MediaRouter.getInstance(getApplicationContext());
        // Find a cast device that is compatible
        mCastMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory( CastMediaControlIntent.categoryForCast(REMOTE_DISPLAY_APP_ID))
                .build();
        mCastMediaRouterCallback = new MyMediaRouterCallback();


        // Setup presentation media routing
        mDisplayManager = (DisplayManager) getSystemService(DISPLAY_SERVICE);

        mDisplayListener = new DisplayManager.DisplayListener() {
            @Override
            public void onDisplayAdded(int displayId) {
                Display d = mDisplayManager.getDisplay(displayId);
                Log.d("DisplayListener", "Adding a display");
                mDisplays.add(d);
            }

            @Override
            public void onDisplayRemoved(int displayId) {
                Display d = mDisplayManager.getDisplay(displayId);
                Log.d("DisplayListener", "Removing a display");
                mDisplays.remove(d);
            }

            @Override
            public void onDisplayChanged(int displayId) {
                Log.d("DisplayListener", "Updating a display");
                mDisplays.remove(mDisplayManager.getDisplay(displayId));
                mDisplays.add(mDisplayManager.getDisplay(displayId));
            }
        };
        mDisplayManager.registerDisplayListener(mDisplayListener, null);


        // Look for cast devices
        mCastMediaRouter.addCallback(mCastMediaRouteSelector, mCastMediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY);
        // Remove previously selected display
        mSelectedDisplay = null;
        // Clear list of displays
        mDisplays.clear();

        // Re-add built in display
        if (!mDisplays.contains(getWindow().getWindowManager().getDefaultDisplay())) {
            mDisplays.add(getWindow().getWindowManager().getDefaultDisplay());
        }

        // Add all displays
        for (Display d : mDisplayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)) {
            Log.d(TAG, "Adding a display");
            mDisplays.add(d);
        }
    }

    private class MyMediaRouterCallback extends MediaRouter.Callback {

        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo info) {
            mSelectedCastDevice = CastDevice.getFromBundle(info.getExtras());
            String routeId = info.getId();
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo info) {
            teardown();
            mSelectedCastDevice = null;
            CastRemoteDisplayLocalService.stopService();
        }
    }

}

