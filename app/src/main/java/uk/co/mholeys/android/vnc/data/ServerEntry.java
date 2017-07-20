package uk.co.mholeys.android.vnc.data;

import uk.co.mholeys.android.vnc.ServerData;

/**
 * Created by Matthew on 20/07/2017.
 */

public class ServerEntry {
    public ServerData serverData;
    public int dbID;

    @Override
    public String toString() {
        return serverData.toString();
    }
}
