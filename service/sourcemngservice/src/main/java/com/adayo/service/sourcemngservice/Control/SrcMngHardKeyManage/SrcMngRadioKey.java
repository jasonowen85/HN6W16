package com.adayo.service.sourcemngservice.Control.SrcMngHardKeyManage;



import com.adayo.adayosource.AdayoSource;
import com.adayo.proxy.sourcemngproxy.Beans.AppConfigType;
import com.adayo.proxy.sourcemngproxy.Beans.SourceInfo;
import com.adayo.service.sourcemngservice.Control.SrcMngServiceMng;
import com.adayo.service.sourcemngservice.Module.AppConfigFile;
import com.adayo.service.sourcemngservice.Module.AppInfo;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;

import java.util.HashMap;
import java.util.Map;

import static com.adayo.adayosource.AdayoSource.ADAYO_SOURCE_RADIO;
import static com.adayo.adayosource.AdayoSource.ADAYO_SOURCE_RADIO_AM;
import static com.adayo.adayosource.AdayoSource.ADAYO_SOURCE_RADIO_FM;

public class SrcMngRadioKey implements ISrcMngHardKeyImp  {
    private static final String TAG = SrcMngRadioKey.class.getSimpleName();

    @Override
    public void notifyMsg( final String event) {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() begin event = " + event);
        SrcMngServiceMng srcMngService = SrcMngServiceMng.getInstance();
        //String radioType = getRadioType();
        if (srcMngService != null) {
            //如果当前的activity 不是唯一的时候 不能直接获取activity 来判断栈顶
            String uiSource = srcMngService.getCurrentUID();
            Map<String, String> map = new HashMap<>();
            boolean isNetRadio = AdayoSource.ADAYO_SOURCE_NET_RADIO.equals(uiSource);
            boolean isFMRadio = AdayoSource.ADAYO_SOURCE_RADIO_FM.equals(uiSource);
            boolean isAMRadio = AdayoSource.ADAYO_SOURCE_RADIO_AM.equals(uiSource);
            boolean isRadio = AdayoSource.ADAYO_SOURCE_RADIO.equals(uiSource);
            if (isRadio | isAMRadio | isFMRadio | isNetRadio) {
                if (isFMRadio) {
                    requestMediaSource(srcMngService, map, AdayoSource.ADAYO_SOURCE_RADIO_AM);
                } else
                    requestMediaSource(srcMngService, map, AdayoSource.ADAYO_SOURCE_RADIO_FM);
            } else {
                requestMediaSource(srcMngService, map, AdayoSource.ADAYO_SOURCE_RADIO);
            }
        }
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() end");
    }

    private void requestMediaSource(SrcMngServiceMng srcMngService, Map<String, String> map, String sourceType) {
        map.put("SourceType", sourceType);
        SourceInfo info = new SourceInfo(sourceType, map,
                AppConfigType.SourceSwitch.APP_ON.getValue(),
                AppConfigType.SourceType.UI_AUDIO.getValue());
        srcMngService.onRequest(info);
    }

    /**
     * 获取项目的RadioSourceType
     * @return : ADAYO_SOURCE_RADIO / ADAYO_SOURCE_RADIO_FM / ADAYO_SOURCE_RADIO_AM
     */
    private String getRadioType()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getRadioType()");

        AppInfo info = AppConfigFile.getInstance().getAppInfoBySourceType(ADAYO_SOURCE_RADIO);
        if (info != null)
        {
            return ADAYO_SOURCE_RADIO;
        }

        info = AppConfigFile.getInstance().getAppInfoBySourceType(ADAYO_SOURCE_RADIO_FM);
        if (info != null)
        {
            return ADAYO_SOURCE_RADIO_FM;
        }

        info = AppConfigFile.getInstance().getAppInfoBySourceType(ADAYO_SOURCE_RADIO_AM);
        if (info != null)
        {
            return ADAYO_SOURCE_RADIO_AM;
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getRadioType() end");

        return null;
    }
}
