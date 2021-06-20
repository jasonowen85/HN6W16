package com.adayo.service.sourcemngservice.Control.SrcMngSystemManage;

import com.adayo.adayosource.AdayoSource;
import com.adayo.proxy.sourcemngproxy.Beans.AppConfigType;
import com.adayo.proxy.sourcemngproxy.Beans.SourceInfo;
import com.adayo.service.sourcemngservice.Control.SrcMngServiceMng;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 2018/7/24.
 */

public class SrcMngSysPowerOff implements ISrcMngSystemImp {
    private static final String  TAG  = SrcMngSysPowerOff.class.getSimpleName();

    @Override
    public void notifyMsg() {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() begin");

        SrcMngServiceMng srcMngService = SrcMngServiceMng.getInstance();

        if (srcMngService != null)
        {
            Map<String, String> map = new HashMap<>();
            map.put("CLOCK_TYPE", "SYSTEM_ON");
            map.put("SourceType", AdayoSource.ADAYO_SOURCE_CLOCK);
            SourceInfo info = new SourceInfo(AdayoSource.ADAYO_SOURCE_CLOCK, map,
                    AppConfigType.SourceSwitch.APP_ON.getValue(),
                    AppConfigType.SourceType.UI_AUDIO.getValue());

            srcMngService.onRequest(info);
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() end");
    }
}
