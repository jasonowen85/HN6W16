package com.adayo.service.sourcemngservice.Control.SrcMngBroadCast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by admin on 2018/12/5.
 */
public class SrcShutDownReceiver extends BroadcastReceiver {
    private final String TAG = SrcShutDownReceiver.class.getSimpleName();
    private final String ACTION_POWEROFF = "com.android.internal.intent.action.REQUEST_SHUTDOWN" ;
    private final String ACTION_POWEROFF_N = "android.intent.action.POWER_OFF" ;
    private final String EXTRA_KEY = "android.intent.extra.KEY_CONFIRM" ;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if(ACTION_POWEROFF_N.equals(action)){
            Log.d(TAG,"-----REQUEST POWER OFF -----");
            Intent intentnew = new Intent(ACTION_POWEROFF);
            intentnew.putExtra(EXTRA_KEY,false);
            intentnew.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentnew);
        }
    }
}
