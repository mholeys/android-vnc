package uk.co.mholeys.android.vnc.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import uk.co.mholeys.android.vnc.ServerData;

/**
 * Created by Matthew on 02/04/2017.
 * Helper class, to save/fetch saved servers from the database on the device.
 */

public class ServerDataSQLHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "server_database";
    public static final String SERVERS_TABLE_NAME = "servers";
    public static final String SERVER_COLUMN_ID = "_id";
    public static final String SERVER_COLUMN_NAME = "name";
    public static final String SERVER_COLUMN_ADDRESS = "address";
    public static final String SERVER_COLUMN_PORT = "port";
    public static final String SERVER_COLUMN_PASSWORD = "password";

    public ServerDataSQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        // Creates the database when needed
        db.execSQL("CREATE TABLE " + SERVERS_TABLE_NAME + " (" +
                SERVER_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                SERVER_COLUMN_NAME + " TEXT, " +
                SERVER_COLUMN_ADDRESS + " TEXT," +
                SERVER_COLUMN_PORT + " INT UNSIGNED, " +
                SERVER_COLUMN_PASSWORD + " TEXT" + ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + SERVERS_TABLE_NAME);
        onCreate(db);
    }

}
