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
 * Created by admin on 2018/4/18.
 */

public class SrcMngSysUpdate implements ISrcMngSystemImp {
    private static final String  TAG  = SrcMngSysUpdate.class.getSimpleName();
    @Override
    public void notifyMsg() {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() begin");

        SrcMngServiceMng srcMngService = SrcMngServiceMng.getInstance();

        if (srcMngService != null)
        {
            Map<String, String> map = new HashMap<>();
            map.put("SourceType", AdayoSource.ADAYO_SOURCE_FOTA);
            SourceInfo info = new SourceInfo(AdayoSource.ADAYO_SOURCE_FOTA,
                    AppConfigType.SourceSwitch.APP_ON.getValue(),
                    AppConfigType.SourceType.UI.getValue());

            srcMngService.onRequest(info);
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() end");
    }
}
