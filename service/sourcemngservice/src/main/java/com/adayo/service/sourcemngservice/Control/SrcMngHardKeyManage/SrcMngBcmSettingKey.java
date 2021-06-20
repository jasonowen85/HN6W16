package com.adayo.service.sourcemngservice.Control.SrcMngHardKeyManage;

import com.adayo.adayosource.AdayoSource;
import com.adayo.proxy.sourcemngproxy.Beans.AppConfigType;
import com.adayo.proxy.sourcemngproxy.Beans.SourceInfo;
import com.adayo.service.sourcemngservice.Control.SrcMngServiceMng;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;

import java.util.HashMap;
import java.util.Map;

public class SrcMngBcmSettingKey implements ISrcMngHardKeyImp {
    private final static String TAG = SrcMngBcmSettingKey.class.getSimpleName();

    @Override
    public void notifyMsg(final String event) {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() begin event = " + event);

        SrcMngServiceMng srcMngService = SrcMngServiceMng.getInstance();

        if (srcMngService != null)
        {
            Map<String, String> map = new HashMap<>();
            map.put("SourceType", AdayoSource.ADAYO_SOURCE_BCM);
            SourceInfo info = new SourceInfo(AdayoSource.ADAYO_SOURCE_BCM,
                    AppConfigType.SourceSwitch.APP_ON.getValue(),
                    AppConfigType.SourceType.UI.getValue());

            srcMngService.onRequest(info);
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() end");
    }
}
