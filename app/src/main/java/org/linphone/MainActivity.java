package org.linphone;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import static android.content.Intent.ACTION_MAIN;

public class MainActivity extends AppCompatActivity {
    private MainActivity activity;
    private Handler mHandler;
    private ServiceWaitThread mThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 避免在平板电脑上画两次LinphoneActivity
        setContentView(R.layout.activity_main);
        activity = this;
        mHandler = new Handler();

        if (LinphoneService.isReady()) {
            onServiceReady();
        } else {
            // start linphone as background
            startService(new Intent(ACTION_MAIN).setClass(this, LinphoneService.class));
            mThread = new ServiceWaitThread();
            mThread.start();
        }
    }

    protected void onServiceReady() {
        final Class<? extends Activity> classToStart = null;
//        if (getResources().getBoolean(R.bool.show_tutorials_instead_of_app)) {
//            classToStart = TutorialLauncherActivity.class;
//        } else if (getResources().getBoolean(R.bool.display_sms_remote_provisioning_activity) && LinphonePreferences.instance().isFirstRemoteProvisioning()) {
//            classToStart = RemoteProvisioningActivity.class;
//        } else {
//            classToStart = LinphoneActivity.class;
//        }
//
//        // We need LinphoneService to start bluetoothManager
//        if (Version.sdkAboveOrEqual(Version.API11_HONEYCOMB_30)) {
//            BluetoothManager.getInstance().initBluetooth();
//        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent().setClass(activity, classToStart).setData(getIntent().getData()));
                finish();
            }
        }, 1000);
    }

    private class ServiceWaitThread extends Thread {
        public void run() {
            while (!LinphoneService.isReady()) {
                try {
                    sleep(30);
                } catch (InterruptedException e) {
                    throw new RuntimeException("waiting thread sleep() has been interrupted");
                }
            }
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onServiceReady();
                }
            });
            mThread = null;
        }
    }
}
