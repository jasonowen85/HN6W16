package com.adayo.service.sourcemngservice.Control.SrcMngBroadCast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.adayo.service.sourcemngservice.Control.SrcMngHardKeyManage.SrcMngKeyMng;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;

public class SrcMngHardKeyReceiver extends BroadcastReceiver {
    private static final String TAG = SrcMngHardKeyReceiver.class.getSimpleName();
    private SrcMngKeyMng mSrcMngKeyMng = null;

    public SrcMngHardKeyReceiver() {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngHardKeyReceiver Begin");

        mSrcMngKeyMng = SrcMngKeyMng.getInstance();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngHardKeyReceiver end");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onReceive Begin");

        String action = intent.getAction();
        String event = intent.getStringExtra("hardKey");

        mSrcMngKeyMng.notifyMsg(event, action);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onReceive end");
    }
}
