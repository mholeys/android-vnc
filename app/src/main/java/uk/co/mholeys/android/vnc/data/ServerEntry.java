package uk.co.mholeys.android.vnc.data;

import uk.co.mholeys.android.vnc.ServerData;

/**
 * Created by Matthew on 20/07/2017.
 * Holds server data and the database id for it.
 */

public class ServerEntry {
    /** The server data used to hold the fields for the connection */
    public ServerData serverData;
    /** The database index for this server */
    public int dbID;

    @Override
    public String toString() {
        return serverData.toString();
    }
}
