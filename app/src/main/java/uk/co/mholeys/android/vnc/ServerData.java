package uk.co.mholeys.android.vnc;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

import uk.co.mholeys.vnc.data.EncodingSettings;
import uk.co.mholeys.vnc.data.PixelFormat;
import uk.co.mholeys.vnc.display.input.FixedPassword;
import uk.co.mholeys.vnc.display.input.IConnectionInformation;
import uk.co.mholeys.vnc.display.input.IPasswordRequester;
import uk.co.mholeys.vnc.log.Logger;

import java.lang.ref.WeakReference;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Created by Matthew on 25/09/2016.
 */
public class ServerData implements IConnectionInformation {

    private PixelFormat format = PixelFormat.DEFAULT_FORMAT;
    private EncodingSettings encodingSettings = EncodingSettings.DEFAULT_ENCODINGS;

    public String name;
    public String address;
    public int port;
    public String password;
    public InetAddress inetAddress;
    public volatile int result = -1;

    public ServerData() {}

    public String toString() {
        return name + "\n" + address + ":" + port;
    }

    @Override
    public InetAddress getAddress() {
        return inetAddress;
    }

    public void prepare() {
        Log.d("ServerData", "preparing ");
        new ServerData.AddressFindTask(this).execute(address);
    }

    static class AddressFindTask extends AsyncTask<String, InetAddress, Integer>  {
        private WeakReference<ServerData> serverDataWeakReference;

        AddressFindTask(ServerData sd) {
            serverDataWeakReference = new WeakReference<>(sd);
        }

        @Override
        protected void onPreExecute() { }

        @Override
        protected Integer doInBackground(String... aParams) {
            Log.d("AsyncAddress", "doInBackground: running");
            if (aParams != null && aParams.length > 0) {
                String address = aParams[0];
                try {
                    Log.d("AsyncAddress", "doInBackground: getting");
                    serverDataWeakReference.get().inetAddress = InetAddress.getByName(address);
                    return 1;
                } catch (UnknownHostException e) {
                    return 0;
                }
            }
            return 0;
        }

        @Override
        protected void onPostExecute(Integer r) {
            Log.d("Async", "onPostExecute: Set result to " + r);
            serverDataWeakReference.get().result = r;
        }
    };

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public boolean hasPrefferedFormat() {
        return format != null;
    }

    @Override
    public PixelFormat getPrefferedFormat() {
        return format;
    }

    public void setPrefferedFormat(PixelFormat format) {
        this.format = format;
    }

    @Override
    public boolean hasPrefferedEncoding() {
        return encodingSettings != null;
    }

    @Override
    public EncodingSettings getPrefferedEncoding() {
        return encodingSettings;
    }

    public void setPrefferedEncoding(EncodingSettings encodingSettings) {
        this.encodingSettings = encodingSettings;
    }

    @Override
    public IPasswordRequester getPasswordRequester() {
        return new FixedPassword(password);
    }
}
