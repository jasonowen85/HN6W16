package com.adayo.proxy.sourcemngproxy.Control;


import android.os.IBinder;
import android.os.RemoteException;

import com.adayo.proxy.sourcemngproxy.Beans.AppConfigType.SourceAvaliable;
import com.adayo.proxy.sourcemngproxy.Beans.SourceInfo;
import com.adayo.proxy.sourcemngproxy.ISourceActionCallBack;
import com.adayo.proxy.sourcemngproxy.Utils.LogUtils;

import com.adayo.proxy.service.ServiceConnection;
import com.adayo.proxy.sourcemngproxy.ISourceMngMicCallBack;
import com.adayo.proxy.sourcemngproxy.ISrcMngProxyClient;

import java.util.List;
import java.util.Stack;

import static com.adayo.proxy.service.ServiceConstants.SERVICE_NAME_SOURCESWITCH;
import static com.adayo.proxy.sourcemngproxy.Utils.SourceMngLog.LOG_TAG;

/**
 * Created by admin on 2018/4/10.
 */

public class SrcMngSwitchProxy extends ServiceConnection {
    private static final String TAG = SrcMngSwitchProxy.class.getSimpleName();
    private volatile static SrcMngSwitchProxy m_SrcMngSwitchProxy = null;
    private ISrcMngProxyClient m_SrcMngService = null;

    /**
     * 构造函数
     */
    private SrcMngSwitchProxy()
    {
        LogUtils.dL(LOG_TAG, TAG + " SrcMngSwitchProxy() begin");

        getServiceConnection();

        LogUtils.dL(LOG_TAG, TAG + " SrcMngSwitchProxy() end");
    }

    /**
     * 连接系统服务
     * @return true(连接成功) / false(连接失败)
     */
    public boolean getServiceConnection()
    {
        LogUtils.dL(LOG_TAG, TAG + " getServiceConnection() begin");

        boolean ret = false;

        IBinder serviceBinder = connectService();
        if (null != serviceBinder)
        {
            m_SrcMngService = ISrcMngProxyClient.Stub.asInterface((IBinder)serviceBinder);
            ret = true;
        }
        else
        {
            LogUtils.dL(LOG_TAG, TAG + " getServiceConnection() failed");
            m_SrcMngService = null;
            ret = false;
        }

        LogUtils.dL(LOG_TAG, TAG + "SrcMngSwitchProxy getServiceConnection() end ret = " + ret);

        return ret;
    }

    @Override
    public String getServiceName() {
        return SERVICE_NAME_SOURCESWITCH;
    }

    /**
     * 单例模式(线程安全，双重同步锁)
     * @return SrcMngSwitchProxy的实例
     */
    public static SrcMngSwitchProxy getInstance()
    {
        LogUtils.dL(LOG_TAG, TAG + " SrcMngSwitchProxy getInstance() begin");

        if (m_SrcMngSwitchProxy == null)
        {
            synchronized (SrcMngSwitchProxy.class)
            {
                if (m_SrcMngSwitchProxy == null)
                {
                    m_SrcMngSwitchProxy = new SrcMngSwitchProxy();
                }
            }
        }

        LogUtils.dL(LOG_TAG, TAG + " SrcMngSwitchProxy getInstance() end");

        return m_SrcMngSwitchProxy;
    }

    /**
     * 向源管理申请切源
     * @param info：请求源信息
     * @return true(切源成功) / false(切源失败)
     */
    public boolean onRequest(SourceInfo info)
    {
        LogUtils.dL(LOG_TAG, TAG + " SrcMngSwitchProxy onRequest() begin AppPackageName = " + info.getM_SourcePackageName() +
                    "\nAppActionName = " + info.getM_SourceActionName() +
                    "\nAppSwitchType = " + info.getM_SourceSwitchType() +
                    "\nAppAudioType = " + info.getM_AudioType());

        boolean ret = false;
        if (m_SrcMngService == null)
        {
            LogUtils.dL(TAG, "m_SrcMngService is null");
            getServiceConnection();
            return false;
        }

        try
        {
            ret = m_SrcMngService.onRequest(info);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " onRequest() RemoteException Happened");
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " onRequest() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " onRequest() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " SrcMngSwitchProxy onRequest() end");

        return ret;
    }

    /**
     * 源是否持有AudioFocus
     * @param sourceType:源ID
     * @return true(持有AudioFocus) / false(不持有AudioFocus)
     */
    public boolean hasAudioFocus(final String sourceType)
    {
        LogUtils.dL(LOG_TAG, TAG + " SrcMngSwitchProxy hasAudioFocus() begin sourceType = " + sourceType);

        boolean ret = false;

        if (m_SrcMngService == null)
        {
            LogUtils.dL(TAG, " m_SrcMngService is null");
            getServiceConnection();
            return false;
        }

        try
        {
            ret = m_SrcMngService.hasAudioFocus(sourceType);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " hasAudioFocus() RemoteException Happened");
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " hasAudioFocus() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " hasAudioFocus() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " hasAudioFocus() end");

        return ret;
    }

    /**
     * 获取当前持有AudioFocus的源ID
     * @return 当前持有音频焦点的源ID
     */
    public String getCurrentAudioFocus()
    {
        LogUtils.dL(LOG_TAG, TAG + " getCurrentAudioFocus() begin");

        String curAudioFocusSource = null;

        if (m_SrcMngService == null)
        {
            LogUtils.dL(TAG, "m_SrcMngService is null");
            getServiceConnection();
            return null;
        }

        try
        {
            curAudioFocusSource = m_SrcMngService.getCurrentAudioFocus();
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " getCurrentAudioFocus() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " getCurrentAudioFocus() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " getCurrentAudioFocus() end curAudioFocusSource = " + curAudioFocusSource);

        return curAudioFocusSource;
    }

    /**
     * 获取当前前台显示的应用的ID
     * @return 显示的应用的ID
     */
    public String getCurrentUID()
    {
        LogUtils.dL(LOG_TAG, TAG + " getCurrentUID() begin");

        String curUID = null;

        if (m_SrcMngService == null)
        {
            LogUtils.dL(TAG, " m_SrcMngService is null");
            getServiceConnection();
            return null;
        }

        try
        {
            curUID = m_SrcMngService.getCurrentUID();
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " getCurrentUID() RemoteException Happened");
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " getCurrentUID() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " getCurrentUID() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " getCurrentUID() end curUID = " + curUID);

        return curUID;
    }

    /**
     * 通知sourceMng源AudioFocus变更
     * @param sourceType：源类型
     * @param dorationHint：抢占方式(AUDIOFOCUS_GAIN / AUDIOFOCUS_GAIN_TRANSIENT / AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
     * @return true(变更成功) / false(变更失败)
     */
    public boolean notifyServiceAudioChange(final String sourceType, final int dorationHint)
    {
        LogUtils.dL(LOG_TAG, TAG + " notifyServiceAudioChange() begin sourceType = " + sourceType + " dorationHint = " + dorationHint);

        boolean ret = false;

        if (m_SrcMngService == null)
        {
            LogUtils.dL(TAG, " m_SrcMngService is null");
            getServiceConnection();
            return false;
        }

        try
        {
            ret = m_SrcMngService.notifyServiceAudioChange(sourceType, dorationHint);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " notifyServiceAudioChange() RemoteException Happened");
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " notifyServiceAudioChange() NullException Happened");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " notifyServiceAudioChange() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " notifyServiceAudioChange() end ret = " + ret);

        return ret;
    }

    /**
     * 通知sourceMng源UI变更
     * @param sourceType:源类型
     * @return true(变更成功) / false(变更失败)
     */
    public boolean notifyServiceUIChange(final String sourceType)
    {
        LogUtils.dL(LOG_TAG, TAG + " notifyServiceUIChange() begin sourceType = " + sourceType);

        boolean ret = false;

        if (m_SrcMngService == null)
        {
            LogUtils.dL(TAG, "m_SrcMngService is null");
            getServiceConnection();
            return false;
        }

        try
        {
            ret = m_SrcMngService.notifyServiceUIChange(sourceType);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " notifyServiceUIChange() RemoteException Happened");
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " notifyServiceUIChange() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " notifyServiceUIChange() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " notifyServiceUIChange() end ret = " + ret);

        return ret;
    }

    /**
     * 申请Mic
     * @param Callback
     * @return:true(申请成功) / false(申请失败)
     */
    public synchronized boolean requestMicFocus(final ISourceMngMicCallBack Callback)
    {
        LogUtils.dL(LOG_TAG, TAG + " requestMicFocus() begin callbackId = " + Callback);

        boolean ret = false;

        if (m_SrcMngService == null)
        {
            LogUtils.dL(TAG, " m_SrcMngService is null");
            getServiceConnection();
            return false;
        }

        try
        {
            ret = m_SrcMngService.requestMicFocus(Callback);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " requestMicFocus() RemoteException Happened");
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " requestMicFocus() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " requestMicFocus() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " requestMicFocus() end ret = " + ret);

        return ret;
    }

    /**
     * 向sourceMng模块放弃Mic Focus
     * @param cb
     * @return
     */
    public boolean abandonMicFocus(final ISourceMngMicCallBack cb)
    {
        LogUtils.dL(LOG_TAG, TAG + " abandonMicFocus() begin cb = " + cb);

        boolean ret = false;

        if (m_SrcMngService == null)
        {
            LogUtils.dL(LOG_TAG, TAG + " m_SrcMngService is Null");
            getServiceConnection();
            return false;
        }

        try
        {
            ret = m_SrcMngService.abandonMicFocus(cb);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " abandonMicFocus() RemoteException Happened");
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " abandonMicFocus() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " abandonMicFocus() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " abandonMicFocus() end ret = " + ret);

        return ret;
    }

    /**
     * launcher画面结束时通知源管理
     */
    public void notifyLauncherFinished()
    {
        LogUtils.dL(LOG_TAG, TAG + " notifyLauncherFinished() begin");

        if (m_SrcMngService == null)
        {
            LogUtils.dL(LOG_TAG, TAG + " m_SrcMngService is null");
            getServiceConnection();
            return;
        }

        try
        {
            m_SrcMngService.notifyLauncherFinished();
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " notifyLauncherFinished() RemoteException Happened");
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " notifyLauncherFinished() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " notifyLauncherFinished() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " notifyLauncherFinished() end");
    }

    /**
     * 获取Source是否有效
     * @param packageName：包名
     * @return ENABLE(有效) / DISABLE(无效)
     */
    public SourceAvaliable getSourceAvailable(final String packageName)
    {
        LogUtils.dL(LOG_TAG, TAG + " notifyLauncherFinished() begin packageName = " + packageName);

        if (m_SrcMngService == null)
        {
            LogUtils.dL(LOG_TAG, TAG + " m_SrcMngService is null");
            getServiceConnection();
            return SourceAvaliable.DISABLE;
        }

        SourceAvaliable avaliable = SourceAvaliable.DISABLE;
        try {
            avaliable = m_SrcMngService.getSourceAvailable(packageName) ? SourceAvaliable.ENABLE : SourceAvaliable.DISABLE;
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " getSourceAvailable() RemoteException Happened");
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " getSourceAvailable() NullException Happened");
        }
        catch (Exception e) {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " getSourceAvailable() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " notifyLauncherFinished() end avaliable = " + avaliable);

        return avaliable;
    }

    /**
     * 通知APP结束
     * @param sourceType：前台UID
     * @return true(App关闭成功) / false(App关闭失败)
     */
    public boolean notifyAppFinished(final String sourceType)
    {
        LogUtils.dL(LOG_TAG, TAG + " notifyAppFinished() begin sourceType = " + sourceType);

        boolean ret = false;

        if (m_SrcMngService == null)
        {
            LogUtils.dL(LOG_TAG, TAG + " m_SrcMngService is null");
            getServiceConnection();
            return false;
        }

        try
        {
            ret = m_SrcMngService.notifyAppFinished(sourceType);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " notifyAppFinished() RemoteException Happened");
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " notifyAppFinished() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " notifyAppFinished() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " notifyAppFinished() end ret = " + ret);

        return ret;
    }

    /**
     * 通知放弃AudioFocus
     * @param sourceType：源ID
     * @return true(通知成功) / false(通知失败)
     */
    public boolean notifyAbandonAudioFocus(final String sourceType)
    {
        LogUtils.dL(LOG_TAG, TAG + " notifyAbandonAudioFocus() begin sourceType = " + sourceType);

        boolean ret = false;
        if (m_SrcMngService == null)
        {
            LogUtils.dL(LOG_TAG, TAG + " m_SrcMngService is null");
            getServiceConnection();
            return false;
        }

        try
        {
            ret = m_SrcMngService.notifyAbandonAudioFocus(sourceType);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " notifyAbandonAudioFocus() RemoteException Happened");
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " notifyAbandonAudioFocus() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " notifyAbandonAudioFocus() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " notifyAbandonAudioFocus() end ret = " + ret);

        return ret;
    }

    /**
     * 通知申请到了AudioFocus
     * @param sourceType：源ID
     * @return true(通知成功) / false(通知失败)
     */
    public boolean notifyRequestAudioFocus(final String sourceType)
    {
        LogUtils.dL(LOG_TAG, TAG + " notifyRequestAudioFocus() begin sourceType = " + sourceType);

        boolean ret = false;
        if (m_SrcMngService == null)
        {
            LogUtils.dL(LOG_TAG, TAG + " m_SrcMngService is null");
            getServiceConnection();
            return false;
        }

        try
        {
            ret = m_SrcMngService.notifyRequestAudioFocus(sourceType);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " notifyRequestAudioFocus() RemoteException Happened");
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " notifyRequestAudioFocus() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " notifyRequestAudioFocus() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " notifyRequestAudioFocus() end ret = " + ret);

        return ret;
    }

    /**
     * 注册App侧关闭自身的回调函数
     * @param sourceType：源ID
     * @param callback：回调函数
     * @return true(注册成功) / false(注册失败)
     */
    public boolean registeSourceActionCallBackFunc(final String sourceType, final ISourceActionCallBack callback)
    {
        LogUtils.dL(LOG_TAG, TAG + " registeSourceActionCallBackFunc() begin sourceType = " + sourceType);

        boolean ret = false;
        if (m_SrcMngService == null)
        {
            LogUtils.dL(LOG_TAG, TAG + " m_SrcMngService is null");
            getServiceConnection();
            return false;
        }

        try
        {
            ret = m_SrcMngService.registeSourceActionCallBackFunc(sourceType, callback);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " registeSourceActionCallBackFunc() RemoteException Happened");
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " registeSourceActionCallBackFunc() NullException Happened");
        }
        catch(Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " registeSourceActionCallBackFunc() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " registeSourceActionCallBackFunc() end ret = " + ret);

        return ret;
    }

    /**
     * 注销App侧关闭自身的回调函数
     * @param sourceType：源ID
     * @return true(注销成功) / false(注销失败)
     */
    public boolean unRegisteSourceActionCallBackFunc(final String sourceType)
    {
        LogUtils.dL(LOG_TAG, TAG + " unRegisteSourceActionCallBackFunc() begin sourceType = " + sourceType);

        boolean ret = false;
        if (m_SrcMngService == null)
        {
            LogUtils.dL(LOG_TAG, TAG + " m_SrcMngService is null");
            getServiceConnection();
            return false;
        }

        try
        {
            ret = m_SrcMngService.unRegisteSourceActionCallBackFunc(sourceType);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " unRegisteSourceActionCallBackFunc() RemoteException Happened");
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " unRegisteSourceActionCallBackFunc() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " unRegisteSourceActionCallBackFunc() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " unRegisteSourceActionCallBackFunc() end ret = " + ret);

        return ret;
    }

    /**
     * 中断切源申请
     * @return : true(中断成功) / false(中断失败)
     */
    public boolean pauseSwitchSource()
    {
        LogUtils.dL(LOG_TAG, TAG + " pauseSwitchSource() begin");

        boolean ret = false;

        if (m_SrcMngService == null)
        {
            LogUtils.dL(LOG_TAG, TAG + " m_SrcMngService is null");
            getServiceConnection();
            return false;
        }

        try
        {
            ret = m_SrcMngService.pauseSwitchSource();
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " pauseSwitchSource() RemoteException Happened");
        }
        catch (NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " pauseSwitchSource() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " pauseSwitchSource() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " pauseSwitchSource() end ret = " + ret);

        return ret;
    }

    /**
     * 获取当前的声音通道
     * @return：当前的声音通道
     */
    public int onGetCurrentChannel()
    {
        LogUtils.dL(LOG_TAG, TAG + " onGetCurrentChannel() begin");

        int ret = -1;

        if (m_SrcMngService == null)
        {
            LogUtils.dL(LOG_TAG, TAG + " m_SrcMngService is null");
            getServiceConnection();
            return -1;
        }

        try
        {
            ret = m_SrcMngService.onGetCurrentChannel();
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " onGetCurrentChannel() RemoteException Happened");
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " onGetCurrentChannel() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " onGetCurrentChannel() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " onGetCurrentChannel() end ret = " + ret);

        return ret;
    }

    /**
     * 判断是否有音频焦点被占用
     * @return：true（被占用） / false(未被占用)
     */
    public boolean onIsAudioFocusOccupy()
    {
        LogUtils.dL(LOG_TAG, TAG + " onIsAudioFocusOccupy() begin");

        if (m_SrcMngService == null)
        {
            LogUtils.dL(LOG_TAG, TAG + " m_SrcMngService is null");
            getServiceConnection();
            return false;
        }

        boolean ret = false;

        try
        {
            ret = m_SrcMngService.onIsAudioFocusOccupy();
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " onIsAudioFocusOccupy() RemoteException Happened");
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " onIsAudioFocusOccupy() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " onIsAudioFocusOccupy() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " onIsAudioFocusOccupy() end ret = " + ret);

        return ret;
    }

    /**
     * 切替声音通道
     * @param channelId：声音通道ID
     * @return:切换后的声音通道ID
     */
    public int onSwitchChannel(int channelId)
    {
        LogUtils.dL(LOG_TAG, TAG + " onSwitchChannel() begin channelID = " + channelId);

        int ret = -1;

        if (m_SrcMngService == null)
        {
            LogUtils.dL(LOG_TAG, TAG + " m_SrcMngService is null");
            getServiceConnection();
            return -1;
        }

        try
        {
            ret = m_SrcMngService.onSwitchChannel(channelId);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " onSwitchChannel() RemoteException Happened");
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " onSwitchChannel() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " onSwitchChannel() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " onSwitchChannel() end ret = " + ret);

        return ret;
    }

    /**
     * 切替声音通道
     * @return: 当前的StreamType
     */
    public int getCurrentStreamType()
    {
        LogUtils.dL(LOG_TAG, TAG + " getCurrentStreamType() begin");

        int ret = -1;

        if (m_SrcMngService == null)
        {
            LogUtils.dL(LOG_TAG, TAG + " m_SrcMngService is null");
            getServiceConnection();
            return -1;
        }

        try
        {
            ret = m_SrcMngService.getCurrentStreamType();
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " getCurrentStreamType() RemoteException Happened");
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " getCurrentStreamType() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " getCurrentStreamType() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " getCurrentStreamType() end ret = " + ret);

        return ret;
    }

    /**
     * 获取源声音通道
     * @Para sourceId : 原名称
     * @return: 该源名称对应的声音通道值
     */
    public int getAudioChannelBySourceId(String sourceId)
    {
        LogUtils.dL(LOG_TAG, TAG + " getAudioChannelBySourceId() begin");

        int ret = -1;

        if (m_SrcMngService == null)
        {
            LogUtils.dL(LOG_TAG, TAG + " m_SrcMngService is null");
            getServiceConnection();
            return -1;
        }

        try
        {
            ret = m_SrcMngService.getAudioChannelBySourceId(sourceId);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " getAudioChannelBySourceId() RemoteException Happened");
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " getAudioChannelBySourceId() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " getAudioChannelBySourceId() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " getAudioChannelBySourceId() end ret = " + ret);

        return ret;
    }

    /**
     * DucKAudioStream
     * @param streamType:streamType
     * @return:true(成功) / false(失败)
     */
    public boolean duckAudioStream(int streamType)
    {
        LogUtils.dL(LOG_TAG, TAG + " duckAudioStream() begin streamType = " + streamType);

        if (m_SrcMngService == null)
        {
            LogUtils.dL(LOG_TAG, TAG + "duckAudioStream() m_SrcMngService is null");
            getServiceConnection();
            return false;
        }

        boolean ret = false;

        try
        {
            ret = m_SrcMngService.duckAudioStream(streamType);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " duckAudioStream() RemoteException Happened");
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " duckAudioStream() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " duckAudioStream() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " duckAudioStream() end ret = " + ret);

        return ret;
    }

    /**
     * unDuckAudioStream
     * @param streamType:streamType
     * @return:true(成功) / false(失败)
     */
    public boolean unDuckAudioStream(int streamType)
    {
        LogUtils.dL(LOG_TAG, TAG + " unDuckAudioStream() begin streamType = " + streamType);

        if (m_SrcMngService == null)
        {
            LogUtils.dL(LOG_TAG, TAG + "unDuckAudioStream() m_SrcMngService is null");
            getServiceConnection();
            return false;
        }

        boolean ret = false;

        try
        {
            ret = m_SrcMngService.unDuckAudioStream(streamType);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " unDuckAudioStream() RemoteException Happened");
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " unDuckAudioStream() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " unDuckAudioStream() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " unDuckAudioStream() end ret = " + ret);

        return ret;
    }

    /**
     * 设置系统mute状态
     * @param muteStatus ：1（系统mute） / 0(解除系统Mute) / -1(不处理)
     * @return
     */
    public boolean setSystemMuteStatus(int streamType, int muteStatus)
    {
        LogUtils.dL(LOG_TAG, TAG + " setSystemMuteStatus() begin streamType = " + streamType + " muteStatus = " + muteStatus);

        if (m_SrcMngService == null)
        {
            LogUtils.dL(LOG_TAG, TAG + "unDuckAudioStream() m_SrcMngService is null");
            getServiceConnection();
            return false;
        }

        boolean ret = false;

        try
        {
            ret = m_SrcMngService.setSystemMuteStatus(streamType, muteStatus);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " setSystemMuteStatus() RemoteException Happened");
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " setSystemMuteStatus() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " setSystemMuteStatus() exception Happened");
        }

        LogUtils.dL(LOG_TAG, TAG + " setSystemMuteStatus() end");

        return ret;
    }

    /**
     * 能否申请音频焦点
     * @param audioType ： 音频类型
     * @return ：true(能申请) / false(不能申请)
     */
    public boolean canRequestAudioFocus(String audioType)
    {
        LogUtils.dL(LOG_TAG, TAG + " canRequestAudioFocus() Begin audioType = " + audioType);

        if (audioType == null || audioType.isEmpty())
        {
            LogUtils.dL(LOG_TAG, TAG + " canRequestAudioFocus() End audioType is null");
            return false;
        }

        boolean ret = false;

        try
        {
            ret = m_SrcMngService.canRequestAudioFocus(audioType);
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " canRequestAudioFocus() RemoteException Happened");
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " canRequestAudioFocus() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " canRequestAudioFocus() exception Happened");
        }


        LogUtils.dL(LOG_TAG, TAG + " canRequestAudioFocus() End");

        return ret;
    }

    /**
     * 获取源栈信息
     * @return：栈信息
     */
    public Stack<String> getUIStack()
    {
        LogUtils.dL(LOG_TAG, TAG + " getUIStack() Begin");

        List<String> uiList = null;
        try
        {
            uiList = m_SrcMngService.getUIList();
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " getUIStack() RemoteException Happened");
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " getUIStack() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " getUIStack() exception Happened");
        }

        Stack<String> stack = changeListToStack(uiList);

        LogUtils.dL(LOG_TAG, TAG + " getUIStack() End");

        return stack;
    }

    /**
     * 获取源栈信息
     * @return：栈信息
     */
    public Stack<String> getAudioStack()
    {
        LogUtils.dL(LOG_TAG, TAG + " getAudioStack() Begin");

        List<String> audioList = null;
        try
        {
            audioList = m_SrcMngService.getAudioList();
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " getAudioStack() RemoteException Happened");
        }
        catch(NullPointerException e)
        {
            e.printStackTrace();
            LogUtils.dL(TAG, " getAudioStack() NullException Happened");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(LOG_TAG, TAG + " getAudioStack() exception Happened");
        }

        Stack<String> stack = changeListToStack(audioList);

        LogUtils.dL(LOG_TAG, TAG + " getAudioStack() End");

        return stack;
    }

    @Override
    public void serviceDied() {
        LogUtils.dL(LOG_TAG, TAG + " serviceDied() begin");

        super.serviceDied();

        m_SrcMngService = null;

        LogUtils.dL(LOG_TAG, TAG + " serviceDied() end");
    }

    /**
     * 将list转换成stack
     * @param list ： 参数list
     * @return
     */
    private Stack<String> changeListToStack(final List<String> list)
    {
        LogUtils.dL(LOG_TAG, TAG + " changeListToStack() begin");

        if (list == null)
        {
            LogUtils.dL(LOG_TAG, TAG + " changeListToStack() list is null");
            return null;
        }

        LogUtils.dL(LOG_TAG, TAG + " changeListToStack() list = " + list.toString());
        Stack<String> stack = new Stack<>();
        for (int i = 0; i < list.size(); i++)
        {
            final String listElement = list.get(i);
            if (listElement != null)
            {
                stack.push(listElement);
            }
        }

        LogUtils.dL(LOG_TAG, TAG + " changeListToStack() end");

        return stack;
    }
}
