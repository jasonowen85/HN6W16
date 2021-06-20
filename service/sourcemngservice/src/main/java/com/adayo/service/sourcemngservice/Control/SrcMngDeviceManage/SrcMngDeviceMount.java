package com.adayo.service.sourcemngservice.Control.SrcMngDeviceManage;

import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;

/**
 * Created by admin on 2018/4/18.
 */

public class SrcMngDeviceMount implements ISrcMngDeviceImp {
    private static final String  TAG  = SrcMngDeviceMount.class.getSimpleName();

    @Override
    public void notifyMsg() {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() begin");

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() end");
    }
}
