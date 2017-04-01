package com.kslau.nexus.samplebubbleheadservice;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final int CODE_DRAW_OVER_OTHER_APP_PERMISSION = 2084;

    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = (Button) findViewById(R.id.start_button);
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isLayoutOverlayPermissionGranted(MainActivity.this)) {
                    Intent intent = new Intent(MainActivity.this, BubbleHeadService.class);
                    startService(intent);
                } else {
                    grantLayoutOverlayPermission(MainActivity.this);
                }

            }
        });

    }

    private boolean isLayoutOverlayPermissionGranted(Activity activity) {
        Log.v(TAG, "Granting Layout Overlay Permission..");
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(activity)) {
            Log.v(TAG, "Permission is denied");
            return false;
        } else {
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    private void grantLayoutOverlayPermission(Activity activity) {
        if (Build.VERSION.SDK_INT >= 23 && !Settings.canDrawOverlays(activity)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + activity.getPackageName()));
            activity.startActivityForResult(intent, CODE_DRAW_OVER_OTHER_APP_PERMISSION);
        }
    }
}
