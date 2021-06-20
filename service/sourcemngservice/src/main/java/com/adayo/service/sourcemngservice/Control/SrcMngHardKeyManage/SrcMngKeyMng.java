package com.adayo.service.sourcemngservice.Control.SrcMngHardKeyManage;

import com.adayo.service.sourcemngservice.Control.SrcMngSystemManage.SrcMngSysMng;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;
import com.adayo.systemserviceproxy.SystemServiceConst.ADAYO_SYSTEM_STATUS;

import java.util.HashMap;
import java.util.Map;

import static com.adayo.proxy.keyevent.util.Constant.KEYEVENT_ACTION_DOWN;
import static com.adayo.proxy.keyevent.util.Constant.KEYEVENT_ACTION_LONGPRESS;
import static com.adayo.proxy.keyevent.util.Constant.KEYEVENT_ACTION_UP;
import static com.adayo.proxy.keyevent.util.Constant.K_MEDIA;
import static com.adayo.proxy.keyevent.util.Constant.K_NAVI;
import static com.adayo.proxy.keyevent.util.Constant.K_POWER;
import static com.adayo.proxy.keyevent.util.Constant.K_RADIO;
import static com.adayo.proxy.keyevent.util.Constant.K_SETING;
import static com.adayo.proxy.keyevent.util.Constant.K_SOURCE;
import static com.adayo.proxy.keyevent.util.Constant.K_SOURCE_MENU;

/**
 * Created by admin on 2018/4/18.
 */

public class SrcMngKeyMng {
    private static final String  TAG  = SrcMngKeyMng.class.getSimpleName();
    private volatile static SrcMngKeyMng m_SrcMngKeyMng = null;
    private Map<String, SrcMngHardKeyListener> m_keyMap = null;
    private Map<String, SrcMngHardKeyListener> m_keyLongMap = null;
    private Map<String, SrcMngHardKeyListener> m_keyUpMap = null;

    /**
     * 构造函数
     */
    private SrcMngKeyMng()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngKeyMng() begin");

        //初始化
        m_keyMap = new HashMap<>();
        m_keyLongMap = new HashMap<>();
        m_keyUpMap = new HashMap<>();

        //监听硬件Key Down
        addHardKeyListener(K_SOURCE_MENU, KEYEVENT_ACTION_DOWN, new SrcMngHardKeyListener(K_SOURCE_MENU, new SrcMngHomeKey()));
        addHardKeyListener(K_RADIO, KEYEVENT_ACTION_DOWN, new SrcMngHardKeyListener(K_RADIO, new SrcMngRadioKey()));
        addHardKeyListener(K_SETING, KEYEVENT_ACTION_DOWN, new SrcMngHardKeyListener(K_SETING, new SrcMngBcmSettingKey()));
        addHardKeyListener(K_NAVI, KEYEVENT_ACTION_DOWN, new SrcMngHardKeyListener(K_NAVI, new SrcMngNaviKey()));

		//监听硬件Key LongPress
        SrcMngHardKeyListener modeKeyListener = new SrcMngHardKeyListener(K_SOURCE, new SrcMngModeKey());
        addHardKeyListener(K_SOURCE,  KEYEVENT_ACTION_LONGPRESS, modeKeyListener);

        //监听硬件Key Up
        addHardKeyListener(K_POWER, KEYEVENT_ACTION_UP, new SrcMngHardKeyListener(K_POWER, new SrcMngPowerKey()));
        addHardKeyListener(K_SOURCE,  KEYEVENT_ACTION_UP, modeKeyListener);
		addHardKeyListener(K_MEDIA, KEYEVENT_ACTION_UP, new SrcMngHardKeyListener(K_MEDIA, new SrcMngMediaKey()));

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngKeyMng() end");
    }

    /**
     * 获取实例
     * @return
     */
    public static SrcMngKeyMng getInstance()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() begin");

        if (m_SrcMngKeyMng == null)
        {
            synchronized (SrcMngKeyMng.class)
            {
                if (m_SrcMngKeyMng == null)
                {
                    m_SrcMngKeyMng = new SrcMngKeyMng();
                }
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() end");

        return m_SrcMngKeyMng;
    }

    //增加硬件的监听
    private boolean addHardKeyListener(final String event, final String action, SrcMngHardKeyListener listener)
    {
        LogUtils.dL(TAG, " addHardKeyListener() begin event = " + event + " action = " + action);

        Map<String, SrcMngHardKeyListener> mapTemp = null;
        if (KEYEVENT_ACTION_DOWN.equals(action))
        {
            mapTemp = m_keyMap;
        }
        else if (KEYEVENT_ACTION_LONGPRESS.equals(action))
        {
            mapTemp = m_keyLongMap;
        }
        else if (KEYEVENT_ACTION_UP.equals(action))
        {
            mapTemp = m_keyUpMap;
        }
        else
        {
            return false;
        }

        //如果map中不存在该event的处理，则插入到map中
        mapTemp.put(event, listener);

        LogUtils.dL(TAG, " addHardKeyListener() end");

        return true;
    }

    /**
     * 是正确处理按键的电源状态吗
     * @return : true（是） / false(不是)
     */
    private boolean isCorrectStatus() {
        LogUtils.dL(TAG, " isCorrectStatus() begin");

        boolean ret = false;

        ADAYO_SYSTEM_STATUS status = SrcMngSysMng.getInstance().getM_adayoSystemStatus();
        switch (status)
        {
            case SYS_STATUS_NORMAL:
            case SYS_STATUS_READY:
            case SYS_STATUS_POWEROFF:
                ret = true;
                break;

            default:
                ret = false;
                break;
        }

        LogUtils.dL(TAG, " isCorrectStatus() end ret = " + ret);

        return ret;
    }

    //发送硬件消息
    public synchronized void notifyMsg(final String keyEvent, final String action)
    {
        LogUtils.dL(TAG, " notifyMsg() begin keyEvent = " + keyEvent);

        if (!isCorrectStatus())
        {
            return;
        }

        Map<String, SrcMngHardKeyListener> mapTemp;
        if (KEYEVENT_ACTION_DOWN.equals(action))
        {
            mapTemp = m_keyMap;
        }
        else if (KEYEVENT_ACTION_LONGPRESS.equals(action))
        {
            mapTemp = m_keyLongMap;
        }
        else if (KEYEVENT_ACTION_UP.equals(action))
        {
            mapTemp = m_keyUpMap;
        }
        else
        {
            return;
        }

        boolean ret = mapTemp.containsKey(keyEvent);

        //如果未找到该id
        if (ret == false)
        {
            return;
        }

        //如果找到该key的处理，则进行处理
        SrcMngHardKeyListener listener = (SrcMngHardKeyListener)mapTemp.get(keyEvent);
        listener.notifyMsg(action);

        LogUtils.dL(TAG, " notifyMsg() end");
    }
}
