package com.adayo.service.sourcemngservice.Control;

import com.adayo.mcucommproxy.McuCommManager;
import com.adayo.proxy.audio.AudioDspManager;
import com.adayo.proxy.audio.constants.AudioDspConstantsDef.EQ_SOURCE;

import com.adayo.service.sourcemngservice.Control.Interface.ISrcMngChannelChange;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;

/**
 * Created by admin on 2018/4/17.
 */

public class SrcMngChannelChg implements ISrcMngChannelChange {
    private static final String TAG = SrcMngChannelChg.class.getSimpleName();
    private AudioDspManager mAudioDspManager;

    /**
     * 构造函数
     */
    public SrcMngChannelChg()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngChannelChg() begin");

        mAudioDspManager = AudioDspManager.getShareDataManager();
        McuCommManager.connectMcuCommServer();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngChannelChg() end");
    }

    @Override
    public int switchAudioChannel(final int channeValue) {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " switchAudioChannel() begin channeValue = " + channeValue);

        int switchRet = mAudioDspManager.setAudioChannel(EQ_SOURCE.getByIndex(channeValue));

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " switchAudioChannel() end switchRet = " + switchRet);

        return switchRet;
    }

    @Override
    public int getCurrentAudioChannel() {
        //获取当前的声音通道
        int [] channelArray = mAudioDspManager.getAudioChannel();
        return channelArray[0];
    }

    @Override
    public int setWorkMode(int modeValue) {
        return mAudioDspManager.setWorkMode(modeValue);
    }
}