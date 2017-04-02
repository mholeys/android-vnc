package uk.co.mholeys.android.vnc;

import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import uk.co.mholeys.vnc.data.EncodingSettings;
import uk.co.mholeys.vnc.data.PixelFormat;
import uk.co.mholeys.vnc.display.input.FixedPassword;
import uk.co.mholeys.vnc.display.input.IConnectionInformation;
import uk.co.mholeys.vnc.display.input.IPasswordRequester;
import uk.co.mholeys.vnc.log.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.xml.transform.Result;

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
    public int result = -1;

    public ServerData() {}

    public String toString() {
        return name + "\n" + address + ":" + port;
    }

    @Override
    public InetAddress getAddress() {
        return inetAddress;
    }

    public void prepare() {
        AsyncTask<String, InetAddress, Integer> getAddressTask = new AsyncTask<String, InetAddress, Integer>() {
            protected void onPreExecute() { }

            protected Integer doInBackground(String... aParams) {
                if (aParams != null && aParams.length > 0) {
                    String address = aParams[0];
                    try {
                        inetAddress = InetAddress.getByName(address);
                        return 1;
                    } catch (UnknownHostException e) {
                        return 0;
                    }
                }
                return 0;
            }

            protected void onPostExecute(Integer result) {
                result = result;
                Logger.logger.printLn("Result of look up was " + result);
            }
        }.execute(address);
    }

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

    @Override
    public boolean hasPrefferedEncoding() {
        return encodingSettings != null;
    }

    @Override
    public EncodingSettings getPrefferedEncoding() {
        return encodingSettings;
    }

    @Override
    public IPasswordRequester getPasswordRequester() {
        return new FixedPassword(password);
    }
}
