package uk.co.mholeys.android.vnc;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
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
        Intent intent = new Intent(this, ServerListActivity.class);
        startActivity(intent);
    }

}
