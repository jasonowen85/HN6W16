package com.adayo.service.sourcemngservice.Control.SrcMngHardKeyManage;



import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;

/**
 * Created by admin on 2018/4/18.
 */

public class SrcMngHardKeyListener{
    private static final String  TAG  = SrcMngHardKeyListener.class.getSimpleName();
    private  String  m_HardKeyID = null;
    private  ISrcMngHardKeyImp m_SrcMngHardKeyImp = null;

    /**
     * 构造函数
     * @param hardKeyID
     * @param hardkeyImp
     */
    public SrcMngHardKeyListener(String hardKeyID, ISrcMngHardKeyImp hardkeyImp)
    {
        m_HardKeyID         =   hardKeyID;
        m_SrcMngHardKeyImp  =   hardkeyImp;
    }

    /**
     * 处理函数
     */
    public void notifyMsg(final String event)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() begin event = " + event);

        if (event == null || event.isEmpty())
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() event is null");
            return;
        }

        m_SrcMngHardKeyImp.notifyMsg(event);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() end");
    }
}
