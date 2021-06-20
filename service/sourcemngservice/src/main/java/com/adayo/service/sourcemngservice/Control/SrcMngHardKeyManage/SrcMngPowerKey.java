package com.adayo.service.sourcemngservice.Control.SrcMngHardKeyManage;


import com.adayo.adayosource.AdayoSource;
import com.adayo.service.sourcemngservice.Control.SrcMngServiceMng;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;
import com.adayo.systemserviceproxy.SystemServiceManager;

import static com.adayo.service.sourcemngservice.Control.SrcMngServiceMng.MNG_STATUS.MNG_RUN;

public class SrcMngPowerKey implements ISrcMngHardKeyImp {
    private static final String TAG  = SrcMngPowerKey.class.getSimpleName();

    @Override
    public void notifyMsg( final String event) {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() begin event = " + event);

        boolean curBackSts = SrcMngServiceMng.getInstance().getBackCarStatus();
        final String curAudioFocus = SrcMngServiceMng.getInstance().getCurrentAudioFocus();

        SrcMngServiceMng.MNG_STATUS mStatus = SrcMngServiceMng.getM_mngStatus();
        if (MNG_RUN != mStatus)
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngNaviKey() end status is wrong");
            return;
        }

        if (!curBackSts && !AdayoSource.ADAYO_SOURCE_BT_PHONE.equals(curAudioFocus))
        {
            SystemServiceManager.getInstance().requestPowerAction();
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() end");
    }
}
