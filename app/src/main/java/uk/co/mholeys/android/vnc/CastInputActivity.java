package uk.co.mholeys.android.vnc;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.cast.CastRemoteDisplayLocalService;

/**
 * Created by Matthew on 17/12/2017.
 * Activity to capture user input directly, for use with a chromecast device as the output
 */

public class CastInputActivity extends InputActivity {

    private static final String TAG = "CastInput";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        CastPresentationService pService = (CastPresentationService) CastPresentationService.getInstance();

        if (pService != null) {
            pService.mToastHandler = new CastInputActivity.ToastHandler();

            layout.setOnHoverListener(pService.mouse);
            layout.setOnTouchListener(pService.mouse);
            layout.setOnGenericMotionListener(pService.mouse);
        } else {
            Log.d(TAG, "onResume: Failed to init input handlers, input might be broken");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        CastPresentationService pService = (CastPresentationService) CastPresentationService.getInstance();

        if (pService != null) {
            Log.d(TAG, "onStop: Input for cast closed. So ending service");
            CastRemoteDisplayLocalService.stopService();
        }
    }

    private void returnToServerList() {
        Intent serverListIntent = new Intent(CastInputActivity.this, ServerListActivity.class);
        startActivity(serverListIntent);
    }

    public class ToastHandler extends Handler {
        @Override
        public void handleMessage(Message message) {
            int state = message.arg1;
            switch (state) {
                case 0:
                    final String text1 = message.getData().getString("TEXT");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CastInputActivity.this, text1, Toast.LENGTH_SHORT).show();
                        }
                    });
                    break;
                case 1:
                    returnToServerList();
                    break;
                case 2:
                    final String text2 = message.getData().getString("TEXT");
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CastInputActivity.this, text2, Toast.LENGTH_SHORT).show();
                        }
                    });
                    returnToServerList();
                    break;
            }
        }
    }

}
