package com.adayo.service.sourcemngservice.Control.SrcMngBroadCast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.adayo.proxy.share.ShareDataManager;
import com.adayo.service.sourcemngservice.Control.SrcMngServiceMng;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class SrcMngBootCompleteReceiver extends BroadcastReceiver {
    private final static String TAG = SrcMngBootCompleteReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onReceive() Begin");

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onReceive() End");
    }
}
