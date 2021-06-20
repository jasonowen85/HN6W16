package com.adayo.service.sourcemngservice.Control.SrcMngLastSource;

import com.adayo.adayosource.AdayoSource;
import com.adayo.proxy.sourcemngproxy.Beans.AppConfigType;
import com.adayo.proxy.sourcemngproxy.Beans.SourceInfo;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;

import java.util.HashMap;
import java.util.Map;

public class SrcMngStartLastSource6w01A extends SrcMngStartLastSource {
    private final static String TAG = SrcMngStartLastSource6w01A.class.getSimpleName();

    /**
     * 构造函数
     */
    public SrcMngStartLastSource6w01A()
    {
        init();
    }

    /**
     * 初始化函数
     */
    private void init()
    {
        //设置默认启动源
        setmDefaultSourceInfo();
    }

    @Override
    protected void setmDefaultSourceInfo() {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setmDefaultSourceInfo() begin");

        Map<String, String> map = new HashMap();
        map.put("SourceType", AdayoSource.ADAYO_SOURCE_RADIO_FM);
        map.put("LastAudioSource", "Y");
        mDefaultSourceInfo = new SourceInfo(AdayoSource.ADAYO_SOURCE_RADIO_FM, map,
                AppConfigType.SourceSwitch.APP_ON.getValue(), AppConfigType.SourceType.UI_AUDIO.getValue());

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setmDefaultSourceInfo() end");
    }

    @Override
    public void startLastSource(final String uiType, final String sourceType) {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " startLastSource() begin sourceType = " + sourceType);

        super.startLastSource(uiType, sourceType);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " startLastSource() end");
    }
}
