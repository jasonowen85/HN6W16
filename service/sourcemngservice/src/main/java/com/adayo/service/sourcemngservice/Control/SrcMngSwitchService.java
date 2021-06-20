package com.adayo.service.sourcemngservice.Control;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.os.RemoteException;

import com.adayo.module.servicecenterproxy.Constant;
import com.adayo.proxy.sourcemngproxy.Beans.SourceInfo;
import com.adayo.proxy.sourcemngproxy.ISourceMngMicCallBack;
import com.adayo.proxy.sourcemngproxy.ISourceActionCallBack;
import com.adayo.proxy.sourcemngproxy.ISrcMngProxyClient;
import com.adayo.service.sourcemngservice.Control.SrcMngBroadCast.SrcMngHardKeyReceiver;
import com.adayo.service.sourcemngservice.Control.SrcMngBroadCast.SrcMngServiceCenter;
import com.adayo.service.sourcemngservice.Control.SrcMngBroadCast.SrcShutDownReceiver;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;
import com.adayo.systemserviceproxy.SystemServiceManager;

import java.lang.reflect.Method;
import java.util.List;

import static com.adayo.proxy.keyevent.util.Constant.KEYEVENT_ACTION_DOWN;
import static com.adayo.proxy.keyevent.util.Constant.KEYEVENT_ACTION_LONGPRESS;
import static com.adayo.proxy.keyevent.util.Constant.KEYEVENT_ACTION_UP;
import static com.adayo.proxy.service.ServiceConstants.SERVICE_NAME_SOURCESWITCH;
import static com.adayo.systemserviceproxy.SystemServiceConst.ADAYO_SYSTEM_MODEL.SYS_MODLE_SORUCE;
import static com.adayo.systemserviceproxy.SystemServiceConst.RES_START_OK;

public class SrcMngSwitchService extends Service {
    private static final String TAG = SrcMngSwitchService.class.getSimpleName();
    private SrcMngServiceMng    m_SrcMngServiceMng  = null;
    private Context             mContext            = null;
    private SystemServiceManager m_AdayoSystemServiceManager = null;

    private BroadcastReceiver mHardKeyReceiver;             //硬件广播短押监听器
    private BroadcastReceiver mShutDownReciver;             //关机广播监听器
    private BroadcastReceiver mServiceCenterReciver;        //服务中心广播监听器

    @Override
    public void onCreate() {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onCreate() begin");

        super.onCreate();

        //创建服务时，context进行关联
        mContext = getApplicationContext();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onCreate() end");
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onBind() begin");

        //初始化SourceMng
        m_SrcMngServiceMng = SrcMngServiceMng.getInstance();
        m_SrcMngServiceMng.setM_Context(this);
        m_SrcMngServiceMng.initSrcMngServiceMng();

        //监听硬件短押广播
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(KEYEVENT_ACTION_DOWN);
        intentFilter.addAction(KEYEVENT_ACTION_UP);
        intentFilter.addAction(KEYEVENT_ACTION_LONGPRESS);
        mHardKeyReceiver = new SrcMngHardKeyReceiver();
        registerReceiver(mHardKeyReceiver, intentFilter);

        //监听Adayo内部关机广播
        IntentFilter shutdownFilter = new IntentFilter();
        shutdownFilter.addAction("android.intent.action.POWER_OFF");
        mShutDownReciver = new SrcShutDownReceiver();
        registerReceiver(mShutDownReciver, shutdownFilter);

        //监听服务中心广播
        IntentFilter servicecenterFilter = new IntentFilter();
        servicecenterFilter.addAction(Constant.SERVICE_CENTER_BROCASTRECEIVER);
        mServiceCenterReciver = new SrcMngServiceCenter();
        registerReceiver(mServiceCenterReciver, servicecenterFilter);
		
		//将sourceMng服务注册到系统服务中
        addServiceToServiceManager(SERVICE_NAME_SOURCESWITCH);

        //通知AdayoSystemService启动OK
        notifyAdayoSystemServiceStartFinished();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onBind() end");

        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onUnbind()");

        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onDestroy() begin");

        //注销广播监听者
        unregisterReceiver(mHardKeyReceiver);
        unregisterReceiver(mShutDownReciver);
        unregisterReceiver(mServiceCenterReciver);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onDestroy() end");

        super.onDestroy();
    }

    /**
     * 将该服务添加到ServiceManager中
     */
    public void addServiceToServiceManager(String serviceName)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " addServiceToServiceManager() begin serviceName = " + serviceName);
        try
        {
            Object object = new Object();
            Method addService;
            addService = Class.forName("android.os.ServiceManager").getMethod("addService", String.class,IBinder.class);
            addService.invoke(object, serviceName, mBinder);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " addServiceToServiceManager() end");
    }

    /**
     * 通知Adayo SystemSerivce 源管理服务启动OK
     */
    private void notifyAdayoSystemServiceStartFinished()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyAdayoSystemServiceStartFinished() Begin");

        //通知Adayo SystemService启动OK
        m_AdayoSystemServiceManager = SystemServiceManager.getInstance();
        if (m_AdayoSystemServiceManager != null && m_AdayoSystemServiceManager.conectsystemService())
        {
            m_AdayoSystemServiceManager.responsStatus((byte)RES_START_OK, (byte)SYS_MODLE_SORUCE.ordinal());
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyAdayoSystemServiceStartFinished() end");
    }

    //AIDL
    private final ISrcMngProxyClient.Stub mBinder = new ISrcMngProxyClient.Stub(){

        @Override
        public boolean onRequest(SourceInfo sourceInfo) throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onRequest() begin");

            boolean ret = m_SrcMngServiceMng.onRequest(sourceInfo);

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onRequest() end");

            return ret;
        }

        @Override
        public boolean notifyServiceAudioChange(String sourceType, int dorationHint) throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyServiceAudioChange() begin sourceType = " + sourceType + "dorationHint = " + dorationHint);

            boolean ret = m_SrcMngServiceMng.notifyServiceAudioChange(sourceType, dorationHint);

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyServiceAudioChange() end ret = " + ret);

            return ret;
        }

        @Override
        public boolean notifyServiceUIChange(String sourceType) throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyServiceUIChange() begin sourceType = " + sourceType);

            boolean ret = m_SrcMngServiceMng.notifyServiceUIChange(sourceType);

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyServiceUIChange() end ret = " + ret);

            return ret;
        }

        @Override
        public boolean hasAudioFocus(String sourceType) throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " hasAudioFocus() begin sourceType = " + sourceType);

            boolean ret = m_SrcMngServiceMng.hasAudioFocus(sourceType);

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " hasAudioFocus() end ret = " + ret);

            return ret;
        }

        @Override
        public String getCurrentAudioFocus() throws RemoteException {
            String curAudioFocus = m_SrcMngServiceMng.getCurrentAudioFocus();

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getCurrentAudioFocus() end curAudioFocusID = " + curAudioFocus);

            return curAudioFocus;
        }

        @Override
        public String getCurrentUID() throws RemoteException {
            String curID = m_SrcMngServiceMng.getCurrentUID();

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getCurrentUID() curID = " + curID);

            return curID;
        }

        @Override
        public boolean requestMicFocus(ISourceMngMicCallBack Callback) throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " requestMicFocus() begin Callback = " + Callback);

            boolean ret = m_SrcMngServiceMng.requestMicFocus(Callback);

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " requestMicFocus() end ret = " + ret);

            return ret;
        }

        @Override
        public boolean abandonMicFocus(ISourceMngMicCallBack Callback) throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " abandonMicFocus() begin Callback = " + Callback);

            boolean ret = m_SrcMngServiceMng.abandonMicFocus(Callback);

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " abandonMicFocus() end ret = " + ret);

            return ret;
        }

        @Override
        public void notifyLauncherFinished() throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyLauncherFinished() begin");

            m_SrcMngServiceMng.notifyLauncherFinished();

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyLauncherFinished() end");
        }

        @Override
        public boolean getSourceAvailable(String packageName) throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getSourceAvailable() begin packageName = " + packageName);

            boolean ret = m_SrcMngServiceMng.getSourceAvailable(packageName);

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getSourceAvailable() end");

            return ret;
        }

        @Override
        public boolean registeSourceActionCallBackFunc(String sourceType, ISourceActionCallBack callback) throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " registeSourceActionCallBackFunc() begin sourceType = " + sourceType);

            boolean ret = m_SrcMngServiceMng.registeSourceActionCallBackFunc(sourceType, callback);

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " registeSourceActionCallBackFunc() end");

            return ret;
        }

        @Override
        public boolean unRegisteSourceActionCallBackFunc(String sourceType) throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " unRegisteSourceActionCallBackFunc() begin sourceType = " + sourceType);

            boolean ret = m_SrcMngServiceMng.unRegisteSourceActionCallBackFunc(sourceType);

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " unRegisteSourceActionCallBackFunc() end");

            return ret;
        }

        @Override
        public boolean notifyAppFinished(String sourceType) throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyAppFinished() begin sourceType = " + sourceType);

            boolean ret = m_SrcMngServiceMng.notifyAppFinished(sourceType);

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyAppFinished() end");

            return ret;
        }

        @Override
        public boolean notifyAbandonAudioFocus(String sourceType) throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyAbandonAudioFocus() begin sourceType = " + sourceType);

            boolean ret = m_SrcMngServiceMng.notifyAbandonAudioFocus(sourceType);

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyAbandonAudioFocus() end");

            return ret;
        }

        @Override
        public boolean pauseSwitchSource()
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " pauseSwitchSource() begin");

            m_SrcMngServiceMng.pauseSwitchSource();

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " pauseSwitchSource() end");

            return true;
        }

        @Override
        public int onGetCurrentChannel() throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onGetCurrentChannel() begin");

            int ret = m_SrcMngServiceMng.onGetCurrentChannel();

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onGetCurrentChannel() end ret = " + ret);

            return ret;
        }

        @Override
        public boolean onIsAudioFocusOccupy() throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onIsAudioFocusOccupy() begin");

            boolean ret = m_SrcMngServiceMng.getCurrentStreamType() == -1 ? false : true;

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onIsAudioFocusOccupy() end ret = " + ret);

            return ret;
        }

        @Override
        public int onSwitchChannel(int channelId) throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onSwitchChannel() begin channelId = " + channelId);

            int ret = m_SrcMngServiceMng.onSwitchChannel(channelId);

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onSwitchChannel() end ret = " + ret);

            return ret;
        }

        @Override
        public int getCurrentStreamType() throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getCurrentStreamType() begin");

            int ret = m_SrcMngServiceMng.getCurrentStreamType();

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getCurrentStreamType() end ret = " + ret);

            return ret;
        }

        @Override
        public boolean notifyRequestAudioFocus(String sourceType) throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyRequestAudioFocus() begin sourceType = " + sourceType);

            boolean ret = m_SrcMngServiceMng.notifyRequestAudioFocus(sourceType);

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyRequestAudioFocus() end ret = " + ret);

            return ret;
        }

        @Override
        public int getAudioChannelBySourceId(String sourceId) throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getAudioChannelBySourceId() begin sourceId = " + sourceId);

            int ret = m_SrcMngServiceMng.getAudioChannelBySourceId(sourceId);

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getAudioChannelBySourceId() end ret = " + ret);

            return ret;
        }

        @Override
        public boolean setSystemMuteStatus(int streamType, int muteStatus) throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setSystemMuteStatus() begin streamType = " + streamType + " muteStatus = " + muteStatus);

            boolean ret = m_SrcMngServiceMng.setSystemMuteStatus(streamType, muteStatus);

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setSystemMuteStatus() end ret = " + ret);

            return ret;
        }

        @Override
        public boolean duckAudioStream(int streamType) throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " duckAudioStream() begin streamType = " + streamType);

            boolean ret = m_SrcMngServiceMng.duckAudioStream(streamType);

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " duckAudioStream() end ret = " + ret);

            return ret;
        }

        @Override
        public boolean unDuckAudioStream(int streamType) throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " unDuckAudioStream() begin streamType = " + streamType);

            boolean ret = m_SrcMngServiceMng.unDuckAudioStream(streamType);

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " unDuckAudioStream() end ret = " + ret);

            return ret;
        }

        @Override
        public boolean canRequestAudioFocus(String sourceType) throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " canRequestAudioFocus() begin sourceType = " + sourceType);

            boolean ret = m_SrcMngServiceMng.canRequestAudioFocus(sourceType);

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " canRequestAudioFocus() end ret = " + ret);

            return ret;
        }

        @Override
        public List<String> getUIList() throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getUIList() begin");

            List<String> list = m_SrcMngServiceMng.getUIList();

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getUIList() end");

            return list;
        }

        @Override
        public List<String> getAudioList() throws RemoteException {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getAudioList() begin");

            List<String> list = m_SrcMngServiceMng.getAudioList();

            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getAudioList() end");

            return list;
        }

//        @Override 暂时这个接口 不添加 如果后期其他模块需要这个功能 再添加
//        public boolean isCurUIDAndAudioId(String s) throws RemoteException {
//            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " isCurUIDAndAudioId() begin s = " + s);
//
//            boolean ret = m_SrcMngServiceMng.isCurUIDAndAudioId(s);
//
//            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " isCurUIDAndAudioId() end ret = " + ret);
//
//            return ret;
//        }
//
//        @Override
//        public boolean resumeSwitchSource() throws RemoteException {
//            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " resumeSwitchSource() begin");
//
//            m_SrcMngServiceMng.resumeSwitchSource();
//
//            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " resumeSwitchSource() end");
//
//            return true;
//        }
    };
}