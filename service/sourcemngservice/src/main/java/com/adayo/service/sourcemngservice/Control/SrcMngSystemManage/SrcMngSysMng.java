package com.adayo.service.sourcemngservice.Control.SrcMngSystemManage;

import android.content.Context;
import com.adayo.proxy.share.ShareDataManager;
import com.adayo.service.sourcemngservice.Control.SrcMngServiceMng;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;
import com.adayo.systemserviceproxy.ISystemServiceCallback;
import com.adayo.systemserviceproxy.SystemServiceConst.ADAYO_SYSTEM_STATUS;
import com.adayo.systemserviceproxy.SystemServiceManager;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import static com.adayo.systemserviceproxy.SystemServiceConst.ADAYO_SYSTEM_MODEL.SYS_MODLE_SORUCE;
import static com.adayo.systemserviceproxy.SystemServiceConst.RES_SHUT_OK;

/**
 * Created by admin on 2018/4/18.
 */

public class SrcMngSysMng {
    private static final String  TAG  = SrcMngSysMng.class.getSimpleName();
    private volatile static SrcMngSysMng m_SrcMngSysMng = null;
    private Map<String, SrcMngSystemListener> m_Map = null;
    private ADAYO_SYSTEM_STATUS m_adayoSystemStatus = ADAYO_SYSTEM_STATUS.SYS_STATUS_INITING;
    private Context mContext;

    /**
     * 构造函数
     */
    private SrcMngSysMng()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngSysMng() begin");

        m_Map = new HashMap<>();

        //将所有的需要监听的system事件进行注册
        addSystemListener("MCU_UPDATE_FAIL", new SrcMngSystemListener("MCU_UPDATE_FAIL", new SrcMngSysUpdate()));
        addSystemListener("SYSTEM_OFF", new SrcMngSystemListener("SYSTEM_OFF", new SrcMngSysOFFHandler()));
        addSystemListener("POWER_OFF", new SrcMngSystemListener("POWER_OFF", new SrcMngSysPowerOff()));

        //获取当前的系统状态
        SystemServiceManager m_AdayoSystemServiceManager = SystemServiceManager.getInstance();
        if (m_AdayoSystemServiceManager != null  && m_AdayoSystemServiceManager.conectsystemService())
        {
            byte adayoSystemStatus = m_AdayoSystemServiceManager.getSystemStatus();
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " initSrcMngServiceMng() adayoSystemStatus = " + adayoSystemStatus);
            adayoSystemServiceStatusChange((byte)m_adayoSystemStatus.ordinal(), adayoSystemStatus);

            //向Adayo SystemService注册回调
            m_AdayoSystemServiceManager.registSystemServiceCallback(new ISystemServiceCallback(){
                @Override
                public void systemStatusNotify(byte b) {
                    LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " systemStatusNotify() b = " + b);
                    adayoSystemServiceStatusChange((byte)m_adayoSystemStatus.ordinal(), b);
                }
            }, (byte)SYS_MODLE_SORUCE.ordinal());
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngSysMng() end");
    }

    public void init(Context context)
    {
        mContext = context;
    }

    /**
     * 电源状态影响当前源管理的运行状态
     * @param currentSystemServiceStatus:当前的电源状态
     * @param changeStatus：来的电源状态
     */
    private void adayoSystemServiceStatusChange(byte currentSystemServiceStatus, byte changeStatus)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " adayoSystemServiceStatusChange() currentSystemServiceStatus = " + currentSystemServiceStatus + " changeStatus = " + changeStatus);

        if (changeStatus == ADAYO_SYSTEM_STATUS.SYS_STATUS_INITING.ordinal())
        {
            SrcMngServiceMng.getInstance().setM_mngStatus(SrcMngServiceMng.MNG_STATUS.MNG_WAIT);
        }
        else if (changeStatus == ADAYO_SYSTEM_STATUS.SYS_STATUS_READY.ordinal())
        {
            SrcMngServiceMng.getInstance().setM_mngStatus(SrcMngServiceMng.MNG_STATUS.MNG_RUN_READY);
        }
        else if (changeStatus == ADAYO_SYSTEM_STATUS.SYS_STATUS_NORMAL.ordinal())
        {
            SrcMngServiceMng.getInstance().setM_mngStatus(SrcMngServiceMng.MNG_STATUS.MNG_RUN_NORMAL);
        }
        else if (changeStatus == ADAYO_SYSTEM_STATUS.SYS_STATUS_FAKESHUT.ordinal())
        {
            SrcMngServiceMng.getInstance().setM_mngStatus(SrcMngServiceMng.MNG_STATUS.MNG_FAKESHUT);

            //启动黑屏画面
            notifyMsg("SYSTEM_OFF");

            //通知Adayo System Service完毕
            SystemServiceManager.getInstance().responsStatus((byte)RES_SHUT_OK, (byte)SYS_MODLE_SORUCE.ordinal());
        }
        else if (changeStatus == ADAYO_SYSTEM_STATUS.SYS_STATUS_POWEROFF.ordinal())
        {
            SrcMngServiceMng.getInstance().setM_mngStatus(SrcMngServiceMng.MNG_STATUS.MNG_RUN);

            //启动待机时钟画面
            notifyMsg("POWER_OFF");
        }
        else if (changeStatus == ADAYO_SYSTEM_STATUS.SYS_STATUS_UPDATA.ordinal())
        {
            SrcMngServiceMng.getInstance().setM_mngStatus(SrcMngServiceMng.MNG_STATUS.MNG_RUN_UPDATE);

            //启动升级模块
            notifyMsg("MCU_UPDATE_FAIL");
        }
        else
        {

        }

        m_adayoSystemStatus = ADAYO_SYSTEM_STATUS.getType((int)changeStatus);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " adayoSystemServiceStatusChange() end m_adayoSystemStatus = " + m_adayoSystemStatus.ordinal());
    }

    /**
     * 申请视频控制权
     */
    private boolean requestVideoCtroller()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " requestVideoCtroller() begin");

        final int HASVIDEO = 1;     //获取到视频控制权
        final int NOHASVIDEO = 0;   //没有获取到视频控制权

        boolean ret = true;

        //将结果保存到shareInfo中
        saveShareInfo(ret ? HASVIDEO : NOHASVIDEO);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " requestVideoCtroller() end ret = " + ret);

        return ret;
    }

    /**
     * 将视频控制权状态保存到ShareInfo中
     * @param hasVideo：
     *                 0：没有视频控制权
     *                 1：有视频控制权
     */
    private void saveShareInfo(final int hasVideo)
    {
        Map<String, Integer> m_Map = new HashMap<>();
        final int MPU_VIDEO_SHAREINFO = 20;

        //插入到ShareInfo中
        m_Map.put("HASVIDEOCONTROL", new Integer(hasVideo));
        ShareDataManager shareDataManager = ShareDataManager.getShareDataManager();
        Gson gson = new Gson();
        String content = gson.toJson(m_Map);
        shareDataManager.sendShareData(MPU_VIDEO_SHAREINFO, content);
    }

    /**
     *获取单例
     */
    public static SrcMngSysMng getInstance()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() begin");

        if (m_SrcMngSysMng == null)
        {
            synchronized (SrcMngSysMng.class)
            {
                if (m_SrcMngSysMng == null)
                {
                    m_SrcMngSysMng = new SrcMngSysMng();
                }
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() end");

        return m_SrcMngSysMng;
    }

    /**
     * 增加System模块某事件的监听
     * @param id
     * @param listener
     * @return
     */
    public boolean addSystemListener(String id, SrcMngSystemListener listener)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " addSystemListener() begin");

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " addSystemListener() id = " + id);

        //如果已经存在该keyid的处理，则直接退出
        if (m_Map.containsKey(id))
        {
            return true;
        }

        //如果map中不存在该id的处理，则插入到map中
        m_Map.put(id, listener);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " addSystemListener() end");

        return true;
    }

    /**
     * 删除硬件的监听
     * @param id
     * @return
     */
    public boolean removeSystemListener(String id)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " removeSystemListener() begin");

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " removeSystemListener() id = " + id);

        //如果包含该hardkey的注册
        if (m_Map.containsKey(id))
        {
            //从map中删除
            m_Map.remove(id);

            return true;
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " removeSystemListener() end");

        return true;
    }

    /**
     * 发送硬件消息
     * @param id
     */
    public void notifyMsg(String id)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() begin");
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() id = " + id);

        boolean ret = m_Map.containsKey(id);

        //如果未找到该id
        if (ret == false)
        {
            return;
        }

        //如果找到该id的处理，则进行处理
        SrcMngSystemListener listener = (SrcMngSystemListener)m_Map.get(id);
        listener.notifyMsg();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() end");
    }

    public ADAYO_SYSTEM_STATUS getM_adayoSystemStatus() {
        return m_adayoSystemStatus;
    }
}
