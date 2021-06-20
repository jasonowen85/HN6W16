package com.adayo.service.sourcemngservice.Control;

import com.adayo.service.sourcemngservice.Control.Interface.ISrcMngChannelChange;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;

/**
 * Created by admin on 2018/4/17.
 */

public class SrcMngChannelMng {
    private static final String TAG = SrcMngChannelMng.class.getSimpleName();
    private volatile static SrcMngChannelMng m_SrcMngChannelMng = null;
    private ISrcMngChannelChange m_SrcMngChannelChg;

    /**
     * 构造函数
     */
    private SrcMngChannelMng()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngChannelMng() begin");

        m_SrcMngChannelChg = new SrcMngChannelChg();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngChannelMng() end");
    }

    /**
     * 获取单例模式
     * @return
     */
    public static SrcMngChannelMng getInstance()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() begin");

        if (null == m_SrcMngChannelMng)
        {
            synchronized (SrcMngChannelMng.class)
            {
                if (null == m_SrcMngChannelMng)
                {
                    m_SrcMngChannelMng = new SrcMngChannelMng();
                }
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() end");

        return m_SrcMngChannelMng;
    }

    /**
     * 切替音频声道
     * @param channeValue：音频声道值
     * @return ：切换完的音频通道
     */
    public synchronized int switchAudioChannel(final int channeValue)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " switchAudioChannel() channeValue = " + channeValue);

        return m_SrcMngChannelChg.switchAudioChannel(channeValue);
    }

    /**
     * 获取当前的音频通道
     * @return ：获取当前的音频通道
     */
    public synchronized int getCurrentAudioChannel()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getCurrentChannel()");

        return m_SrcMngChannelChg.getCurrentAudioChannel();
    }

    /**
     * 设置WorkMode
     * @param value
     * @return
     */
    public synchronized int setWorkMode(final int value)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setWorkMode() value = " + value);

        return m_SrcMngChannelChg.setWorkMode(value);
    }
}
