package uk.co.mholeys.android.vnc;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import uk.co.mholeys.android.vnc.data.ServerDataSQLHelper;

public class EditServerActivity extends AppCompatActivity {

    EditText mName;
    EditText mAddress;
    EditText mPort;
    EditText mPassword;

    int id = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_server);
        setTitle(R.string.title_activity_edit_server);

        Intent intent = getIntent();
        mName = findViewById(R.id.server_name_text);
        mName.setText(intent.getStringExtra(ServerListActivity.SERVER_INFO_NAME));
        mAddress = findViewById(R.id.server_address_text);
        mAddress.setText(intent.getStringExtra(ServerListActivity.SERVER_INFO_ADDRESS));
        mPort = findViewById(R.id.server_port_text);
        mPort.setText(String.valueOf(intent.getIntExtra(ServerListActivity.SERVER_INFO_PORT, 5901)));
        mPassword = findViewById(R.id.server_password_text);
        mPassword.setText(intent.getStringExtra(ServerListActivity.SERVER_INFO_PASSWORD));
        id = intent.getIntExtra(ServerListActivity.SERVER_INFO_DB_ID, -1);
        Log.d("EditServer", "id: " + id);
    }

    public void editServer(View view) {
        String name = mName.getText().toString();
        String address = mAddress.getText().toString();
        int port = Integer.parseInt(mPort.getText().toString());
        String password = mPassword.getText().toString();
        SQLiteDatabase db = new ServerDataSQLHelper(this).getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        values.put(ServerDataSQLHelper.SERVER_COLUMN_NAME, name);
        values.put(ServerDataSQLHelper.SERVER_COLUMN_ADDRESS, address);
        values.put(ServerDataSQLHelper.SERVER_COLUMN_PORT, port);
        values.put(ServerDataSQLHelper.SERVER_COLUMN_PASSWORD, password);
        int rowsEffected = db.update(ServerDataSQLHelper.SERVERS_TABLE_NAME, values, ServerDataSQLHelper.SERVER_COLUMN_ID + "=?", new String[] {""+id});
        Log.d("EditServer", "Changed " + rowsEffected);
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        Intent intent = new Intent(this, ServerListActivity.class);
        startActivity(intent);
    }

    public void cancelEdit(View view) {
        Intent upIntent = NavUtils.getParentActivityIntent(this);
        if (NavUtils.shouldUpRecreateTask(this, upIntent)) {
            // This activity is NOT part of this app's task, so create a new task
            // when navigating up, with a synthesized back stack.
            TaskStackBuilder.create(this)
                    // Add all of this activity's parents to the back stack
                    .addNextIntentWithParentStack(upIntent)
                    // Navigate up to the closest parent
                    .startActivities();
        } else {
            // This activity is part of this app's task, so simply
            // navigate up to the logical parent activity.
            NavUtils.navigateUpTo(this, upIntent);
        }
    }

}
