package com.adayo.service.sourcemngservice.Control.SrcMngDeviceManage;

/**
 * Created by admin on 2018/4/18.
 */

public class SrcMngDeviceListener {
    private String m_Id = null;
    private ISrcMngDeviceImp m_SrcMngDeviceHandle = null;

    public SrcMngDeviceListener(String id, ISrcMngDeviceImp handle)
    {
        m_Id = id;
        m_SrcMngDeviceHandle = handle;
    }

    public void notifyMsg()
    {
        m_SrcMngDeviceHandle.notifyMsg();
    }
}
