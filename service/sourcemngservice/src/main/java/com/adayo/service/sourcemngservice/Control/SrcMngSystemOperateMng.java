package com.adayo.service.sourcemngservice.Control;

import android.os.SystemProperties;

import com.adayo.proxy.share.ShareDataManager;
import com.adayo.service.sourcemngservice.Control.Interface.ISrcMngSystemSettings;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 2018/4/11.
 */

public class SrcMngSystemOperateMng {
    private static final String TAG = SrcMngSystemOperateMng.class.getSimpleName();
    private volatile static SrcMngSystemOperateMng m_SrcMngSystemOperateMng = null;
    private ISrcMngSystemSettings m_SrcMngSystemSetting;
    private Map<String, Integer> m_Map = new HashMap<>();
    private static final int MUTE_SHAREINFO_NUM = 21;

    //当前Mute状态
    private enum SYS_MUTE
    {
        UNMUTE,     //解除Mute
        MUTE_ALL,   //全部静音
        MUTE        //Mute(导航以外静音)
    }

    /**
     * 构造函数
     * @param srcMngSysSetting
     */
    private SrcMngSystemOperateMng(ISrcMngSystemSettings srcMngSysSetting)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngSystemOperateMng() begin");

        this.m_SrcMngSystemSetting  =   srcMngSysSetting;

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngSystemOperateMng() end");
    }

    /**
     * 获取实例对象,DCL
     */
    public static SrcMngSystemOperateMng getInstance()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() begin");

        if (m_SrcMngSystemOperateMng == null)
        {
            synchronized (SrcMngSystemOperateMng.class)
            {
                if (m_SrcMngSystemOperateMng == null)
                {
                    m_SrcMngSystemOperateMng = new SrcMngSystemOperateMng(new SrcMngSysSetting());
                }
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() end");

        return m_SrcMngSystemOperateMng;
    }

    /**
     * 系统Mute
     */
    public void muteChannel()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " muteChannel() begin");

        final String muteStatus = SystemProperties.get("persist.systemMuteStatus");
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " muteChannel() muteStatus = " + muteStatus);
        if ("ON".equals(muteStatus))
        {
            return;
        }

        this.m_SrcMngSystemSetting.setChannelMute(SYS_MUTE.MUTE.ordinal());

        saveShareInfo(SYS_MUTE.MUTE.ordinal()); //将静音保存到shareInfo中

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " muteChannel() end");
    }

    /**
     * 解除系统Mute
     */
    public void unMuteChannel()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " unMuteChannel() begin");

        final String muteStatus = SystemProperties.get("persist.systemMuteStatus");
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " unMuteSystem() muteStatus = " + muteStatus);
        if ("ON".equals(muteStatus))
        {
            return;
        }

        this.m_SrcMngSystemSetting.setChannelMute(SYS_MUTE.UNMUTE.ordinal());

        saveShareInfo(SYS_MUTE.UNMUTE.ordinal()); //将取消静音保存到shareInfo中

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " unMuteChannel() end");
    }

    /**
     * 设置系统mute状态
     * @param muteValue ：1（Mute） / 0(解除Mute) / -1（不处理）
     */
    public void setSysMute(final int muteValue)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setSysMute() begin muteValue = " + muteValue);

        m_SrcMngSystemSetting.setSysMute(muteValue);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setSysMute() end");
    }

    /**
     * 将Mute状态保存到ShareInfo中
     * @param muteValue：
     *                 0：解除Mute
     *                 1：全部静音
     *                 2：Mute(导航以外静音)
     */
    private void saveShareInfo(final int muteValue)
    {
        //插入到ShareInfo中
        m_Map.put("IsMute", muteValue);
        ShareDataManager shareDataManager = ShareDataManager.getShareDataManager();
        Gson gson = new Gson();
        String content = gson.toJson(m_Map);
        shareDataManager.sendShareData(MUTE_SHAREINFO_NUM, content);
    }
}
