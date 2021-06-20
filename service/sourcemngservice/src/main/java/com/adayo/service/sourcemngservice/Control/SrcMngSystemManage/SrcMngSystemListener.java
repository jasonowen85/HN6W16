package com.adayo.service.sourcemngservice.Control.SrcMngSystemManage;


import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;

/**
 * Created by admin on 2018/4/18.
 */

public class SrcMngSystemListener {
    private static final String  TAG  = SrcMngSystemListener.class.getSimpleName();
    private  String  m_id = null;
    private  ISrcMngSystemImp m_SrcMngSysHandler = null;

    public SrcMngSystemListener(String id, ISrcMngSystemImp srcMngSystemImp)
    {
        m_id = id;
        m_SrcMngSysHandler = srcMngSystemImp;
    }

    /**
     * 处理函数
     */
    public void notifyMsg()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() begin");

        m_SrcMngSysHandler.notifyMsg();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() begin");
    }
}
