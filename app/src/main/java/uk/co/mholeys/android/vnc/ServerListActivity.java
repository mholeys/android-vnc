package uk.co.mholeys.android.vnc;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import uk.co.mholeys.android.vnc.data.SQLHelper;

public class ServerListActivity extends AppCompatActivity {

    public final static String TAG = "vnc.ServerListActivity";

    public final static String SERVER_INFO_ADDRESS = "uk.co.mholeys.android.vnc.serverinfo.address";
    public final static String SERVER_INFO_PORT = "uk.co.mholeys.android.vnc.serverinfo.port";
    public final static String SERVER_INFO_PASSWORD = "uk.co.mholeys.android.vnc.serverinfo.password";
    public static final String SERVER_INFO_CONNECTION = "uk.co.mholeys.android.vnc.serverinfo.connection";

    ArrayAdapter<ServerData> listItems;
    ListView serverList;

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

        serverList = (ListView) findViewById(R.id.serverListView);
        listItems = new ArrayAdapter<ServerData>(this, R.layout.list_layout);

        loadServers();

        serverList.setOnCreateContextMenuListener(this);

        serverList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Open up vnc
                ServerData server = (ServerData)adapterView.getAdapter().getItem(i);
                startVncViewer(server);
            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);

        // groupId, itemId, order, title
        menu.add(0, v.getId(), 0, "Connect");
        menu.add(0, v.getId(), 0, "Delete");
        menu.add(0, v.getId(), 0, "Edit");
    }
    @Override
    public boolean onContextItemSelected(MenuItem item){
        String option = (String) item.getTitle();
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        ServerData server = listItems.getItem((int) info.id);
        switch (option) {
            case "Connect":
                startVncViewer(server);
                break;
            case "Delete":
                deleteServer(server);
                loadServers();
                break;
            case "Edit":
                // TODO: Implement
                break;
            default:
                return false;
        }
        return true;
    }

    public void addServerIntent() {
        Intent intent = new Intent(this, AddServerActivity.class);
        startActivity(intent);
    }

    public void deleteServer(ServerData server) {
        listItems.remove(server);
        SQLiteDatabase db = new SQLHelper(this).getWritableDatabase();
        db.beginTransaction();
        db.delete(SQLHelper.SERVERS_TABLE_NAME,
                SQLHelper.SERVER_COLUMN_ADDRESS + "=? AND " +
                SQLHelper.SERVER_COLUMN_NAME + "=? AND " +
                SQLHelper.SERVER_COLUMN_PORT + "=?",
                new String[] {server.address, server.name, ""+server.port});
        db.endTransaction();
    }

    public void startVncViewer(ServerData server) {
        // Check for chrome casts
        // Give option?

        Intent intent = new Intent(this, VncActivity.class);
        intent.putExtra(SERVER_INFO_CONNECTION, server.inetAddress);
        intent.putExtra(SERVER_INFO_ADDRESS, server.address);
        intent.putExtra(SERVER_INFO_PORT, server.port);
        intent.putExtra(SERVER_INFO_PASSWORD, server.password);
        startActivity(intent);
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
                String name = cursor.getString(cursor.getColumnIndex(SQLHelper.SERVER_COLUMN_NAME));
                String address = cursor.getString(cursor.getColumnIndex(SQLHelper.SERVER_COLUMN_ADDRESS));
                int port = cursor.getInt(cursor.getColumnIndex(SQLHelper.SERVER_COLUMN_PORT));
                String password = cursor.getString(cursor.getColumnIndex(SQLHelper.SERVER_COLUMN_PASSWORD));

                ServerData sd = new ServerData();
                sd.name = name;
                sd.address = address;
                sd.port = port;
                sd.password = password;

                listItems.add(sd);
                serverList.setAdapter(listItems);

                cursor.moveToNext();
            }
        }
        db.endTransaction();
    }

}

