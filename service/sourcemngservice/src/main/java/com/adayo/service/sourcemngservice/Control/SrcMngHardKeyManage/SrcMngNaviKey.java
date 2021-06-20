package com.adayo.service.sourcemngservice.Control.SrcMngHardKeyManage;

import com.adayo.adayosource.AdayoSource;
import com.adayo.proxy.sourcemngproxy.Beans.AppConfigType;
import com.adayo.proxy.sourcemngproxy.Beans.SourceInfo;
import com.adayo.service.sourcemngservice.Control.SrcMngServiceMng;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;

import java.util.HashMap;
import java.util.Map;


public class SrcMngNaviKey implements ISrcMngHardKeyImp {
    public final static String TAG = SrcMngNaviKey.class.getSimpleName();

    @Override
    public void notifyMsg( final String event) {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngNaviKey() begin event = " + event);

        SrcMngServiceMng srcMngService = SrcMngServiceMng.getInstance();
        if (srcMngService != null)
        {
            Map<String, String> map = new HashMap<>();
            map.put("SourceType", AdayoSource.ADAYO_SOURCE_NAVI);
            SourceInfo info = new SourceInfo(AdayoSource.ADAYO_SOURCE_NAVI,
                    AppConfigType.SourceSwitch.APP_ON.getValue(),
                    AppConfigType.SourceType.UI_AUDIO.getValue());

            srcMngService.onRequest(info);
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngNaviKey() end");
    }
}
