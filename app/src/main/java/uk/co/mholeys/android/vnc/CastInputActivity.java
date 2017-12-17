package uk.co.mholeys.android.vnc;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.cast.CastRemoteDisplayLocalService;

/**
 * Created by Matthew on 17/12/2017.
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

}
