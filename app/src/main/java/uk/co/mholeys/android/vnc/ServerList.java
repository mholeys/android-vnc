package uk.co.mholeys.android.vnc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ShareCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class ServerList extends AppCompatActivity {

    public final static String TAG = "mholeys.vnc.ServerList";

    public final static String SERVER_INFO_ADDRESS = "uk.co.mholeys.android.vnc.serverinfo.address";
    public final static String SERVER_INFO_PORT = "uk.co.mholeys.android.vnc.serverinfo.port";
    public final static String SERVER_INFO_PASSWORD = "uk.co.mholeys.android.vnc.serverinfo.password";
    public static final String SERVER_INFO_CONNECTION = "uk.co.mholeys.android.vnc.serverinfo.connection";

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
                            }
                        }).show();
            }
        });

        ListView serverList = (ListView) findViewById(R.id.serverListView);
        ArrayAdapter<ServerData> listItems = new ArrayAdapter<ServerData>(this, R.layout.list_layout);

        ServerData sd = new ServerData();
        sd.address = "raspberrypi";
        sd.port = 5901;
        sd.password = "superuse";
        listItems.add(sd);
        serverList.setAdapter(listItems);

        serverList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Open up vnc
                ServerData server = (ServerData)adapterView.getAdapter().getItem(i);
                startVncViewer(server);
            }
        });
    }

    public void startVncViewer(ServerData server) {
        Intent intent = new Intent(this, VncActivity.class);
        intent.putExtra(SERVER_INFO_CONNECTION, server.inetAddress);
        intent.putExtra(SERVER_INFO_ADDRESS, server.address);
        intent.putExtra(SERVER_INFO_PORT, server.port);
        intent.putExtra(SERVER_INFO_PASSWORD, server.password);
        startActivity(intent);
    }

}

