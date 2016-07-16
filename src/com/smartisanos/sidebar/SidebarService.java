package com.smartisanos.sidebar;

import java.io.FileDescriptor;
import java.io.PrintWriter;

import com.smartisanos.sidebar.util.Utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import android.os.SystemProperties;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SidebarService extends Service {
    static final String TAG = "SidebarService";
    public static final boolean DEBUG = (SystemProperties.getInt(
            "ro.debuggable", 0) == 1);

    @Override
    public void onCreate() {
        Log.d("TAG", "onCreate()......");
        SidebarController.getInstance(getApplicationContext()).init();

        TelephonyManager telephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(new SidebarPhoneStateListener(), PhoneStateListener.LISTEN_CALL_STATE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO
    }

    /**
     * Nobody binds to us.
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        // TODO
    }

    class SidebarPhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                Utils.resumeSidebar(getApplicationContext());
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                break;
            }
        }
    }
}
