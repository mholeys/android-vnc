package uk.co.mholeys.android.vnc;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class InputActivity extends AppCompatActivity {

    View layout = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_input);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        layout = findViewById(R.id.InputLayout);

        Intent intent = getIntent();
        int mode = intent.getIntExtra(ServerListActivity.DISPLAY_MODE, -1);
        switch (mode) {
            case ServerListActivity.DISPLAY_MODE_CAST:
                CastPresentationService pService = (CastPresentationService) CastPresentationService.getInstance();

                layout.setOnHoverListener(pService.mouse);
                layout.setOnTouchListener(pService.mouse);
                layout.setOnGenericMotionListener(pService.mouse);

                break;
            case ServerListActivity.DISPLAY_MODE_PRESENTATION:
                // TODO implement presentation mode for input

                break;
            default:

        }

    }

}
