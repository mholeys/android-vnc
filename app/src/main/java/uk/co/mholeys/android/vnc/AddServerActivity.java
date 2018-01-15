package uk.co.mholeys.android.vnc;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import uk.co.mholeys.android.vnc.data.SQLHelper;

public class AddServerActivity extends AppCompatActivity {

    EditText mName;
    EditText mAddress;
    EditText mPort;
    EditText mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_server);
        setTitle(R.string.title_activity_add_server);

        mName = (EditText) findViewById(R.id.server_name_text);
        mAddress = (EditText) findViewById(R.id.server_address_text);
        mPort = (EditText) findViewById(R.id.server_port_text);
        mPassword = (EditText) findViewById(R.id.server_password_text);

    }

    public void addServer(View view) {
        String name = mName.getText().toString();
        String address = mAddress.getText().toString();
        int port = Integer.parseInt(mPort.getText().toString());
        String password = mPassword.getText().toString();
        SQLiteDatabase db = new SQLHelper(this).getWritableDatabase();
        db.beginTransaction();
        ContentValues values = new ContentValues();
        values.put(SQLHelper.SERVER_COLUMN_NAME, name);
        values.put(SQLHelper.SERVER_COLUMN_ADDRESS, address);
        values.put(SQLHelper.SERVER_COLUMN_PORT, port);
        values.put(SQLHelper.SERVER_COLUMN_PASSWORD, password);
        long newRowId = db.insert(SQLHelper.SERVERS_TABLE_NAME, null, values);
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();

        Intent intent = new Intent(this, ServerListActivity.class);
        startActivity(intent);
    }

    public void cancelAdd(View view) {
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
