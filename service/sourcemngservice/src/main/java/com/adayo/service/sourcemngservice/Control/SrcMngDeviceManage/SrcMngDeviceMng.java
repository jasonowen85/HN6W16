package com.adayo.service.sourcemngservice.Control.SrcMngDeviceManage;

import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by admin on 2018/4/18.
 */

public class SrcMngDeviceMng {
    private static final String  TAG  = SrcMngDeviceMng.class.getSimpleName();
    private volatile static SrcMngDeviceMng m_SrcMngDeviceMng = null;
    private Map<String, SrcMngDeviceListener> m_map = null;
    private String [] packageNames = new String[DEVICE_ENUM.DEVICE_NUM.ordinal()];

    private enum DEVICE_ENUM{
        DEVICE_MOUNT,
        DEVICE_NUM
    }

    /**
     * 构造函数
     */
    private SrcMngDeviceMng()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngDeviceMng() begin");

        m_map = new HashMap<String, SrcMngDeviceListener>();

        packageNames[DEVICE_ENUM.DEVICE_MOUNT.ordinal()] = "UsbMount";

        //将所有的需要监听的Device事件进行注册
        addDeviceListener(packageNames[DEVICE_ENUM.DEVICE_MOUNT.ordinal()], new SrcMngDeviceListener("UsbMount", new SrcMngDeviceMount()));

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngDeviceMng() end");
    }

    /**
     * 获取DeviceMng实例
     * @return
     */
    public static SrcMngDeviceMng getInstance()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() begin");

        if (m_SrcMngDeviceMng == null)
        {
            synchronized (SrcMngDeviceMng.class)
            {
                if (m_SrcMngDeviceMng == null)
                {
                    m_SrcMngDeviceMng = new SrcMngDeviceMng();
                }
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() end");

        return m_SrcMngDeviceMng;
    }

    /**
     * 增加对某种设备的监听
     * @param packageName
     * @param listener
     * @return
     */
    public boolean addDeviceListener(String packageName, SrcMngDeviceListener listener)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " addDeviceListener() begin");

        boolean ret = false;

        //如果已经存在该keyid的处理，则直接退出
        if (m_map.containsKey(packageName))
        {
            return true;
        }

        //如果map中不存在该keyid的处理，则插入到map中
        m_map.put(packageName, listener);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " addDeviceListener() end");

        return true;
    }

    /**
     * 删除对某种设备的监听
     * @param packageName
     * @return
     */
    public boolean removeDeviceListener(String packageName)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " removeDeviceListener() begin");

        boolean ret = false;

        //如果包含该hardkey的注册
        if (m_map.containsKey(packageName))
        {
            //从map中删除
            m_map.remove(packageName);

            return true;
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " removeDeviceListener() end");

        return true;
    }

    /**
     * 发送某设备的处理消息
     * @param packageName
     */
    public synchronized void notifyMsg(String packageName)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() begin packageName = " + packageName);

        boolean ret = m_map.containsKey(packageName);

        //如果未找到该id
        if (ret == false)
        {
            return;
        }

        //如果找到该id的处理，则进行处理
        SrcMngDeviceListener listener = (SrcMngDeviceListener)m_map.get(packageName);
        listener.notifyMsg();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() end");
    }
}
