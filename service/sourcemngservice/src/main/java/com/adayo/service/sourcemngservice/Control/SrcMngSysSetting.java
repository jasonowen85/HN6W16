package com.adayo.service.sourcemngservice.Control;

import com.adayo.proxy.audio.AudioDspManager;
import com.adayo.proxy.settings.SettingExternalManager;
import com.adayo.service.sourcemngservice.Control.Interface.ISrcMngSystemSettings;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;

/**
 * Created by admin on 2018/4/17.
 */

public class SrcMngSysSetting implements ISrcMngSystemSettings {
    private static final String TAG = SrcMngSysSetting.class.getSimpleName();
    private AudioDspManager mAudioDspManager;

    /**
     * 构造函数
     */
    public SrcMngSysSetting()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngSysSetting() begin");

        mAudioDspManager = AudioDspManager.getShareDataManager();
        mAudioDspManager.init();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngSysSetting() end");
    }

    @Override
    /*muteValue:0(UnMute)
                1(Mute All)
                2(Mute All except Navi)
    */
    public void setChannelMute(int muteValue) {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setChannelMute() begin muteValue = " + muteValue);

        mAudioDspManager.setMuteMode(muteValue);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setChannelMute() end");
    }

    @Override
    /**
     * muteValue:1(静音) / 0(解除静音) / -1(不处理)
     */
    public void setSysMute(final int muteValue)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setSysMute() begin muteValue = " + muteValue);
        final int MUTE = 1;
        final int UNMUTE = 0;

        switch (muteValue)
        {
            case MUTE:
                SettingExternalManager.getSettingsManager().setMuteSwitch(true);
                break;

            case UNMUTE:
                SettingExternalManager.getSettingsManager().setMuteSwitch(false);
                break;

            default:
                break;
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setSysMute() end");
    }
}