package com.adayo.service.sourcemngservice.Control;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.RemoteException;

import android.os.SystemProperties;

import com.adayo.adayosource.AdayoSource;
import com.adayo.proxy.share.ShareDataManager;
import com.adayo.proxy.sourcemngproxy.Beans.AppConfigType;
import com.adayo.proxy.sourcemngproxy.Beans.SourceInfo;
import com.adayo.proxy.sourcemngproxy.ISourceActionCallBack;
import com.adayo.service.sourcemngservice.Control.SrcMngAudioManage.SrcAudioManager;
import com.adayo.service.sourcemngservice.Control.SrcMngLastSource.SrcMngStartLastSource;
import com.adayo.service.sourcemngservice.Control.SrcMngLastSource.SrcMngStartLastSource6w01A;
import com.adayo.service.sourcemngservice.Utils.LogUtils;

import com.adayo.proxy.sourcemngproxy.ISourceMngMicCallBack;
import com.adayo.service.sourcemngservice.Control.SrcMngSystemManage.SrcMngSysMng;
import com.adayo.service.sourcemngservice.Module.AppInfo;
import com.adayo.service.sourcemngservice.Module.AppSwitchInfo;
import com.adayo.service.sourcemngservice.Control.SrcMngDeviceManage.SrcMngDeviceMng;
import com.adayo.service.sourcemngservice.Control.SrcMngHardKeyManage.SrcMngKeyMng;
import com.adayo.service.sourcemngservice.Module.AppConfigFile;
import com.adayo.proxy.sourcemngproxy.Beans.AppConfigType.SourceSwitch;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;
import com.adayo.service.sourcemngservice.Utils.SrcMngOperateShareInfo;
import com.google.gson.Gson;
import com.kaolafm.sdk.client.IServiceConnection;
import com.kaolafm.sdk.client.KLClientAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

import static android.content.Context.ACTIVITY_SERVICE;
import static com.adayo.adayosource.AdayoSource.ADAYO_SOURCE_CARPLAY_MEDIA;
import static com.adayo.adayosource.AdayoSource.ADAYO_SOURCE_FAKESHUT;
import static com.adayo.adayosource.AdayoSource.ADAYO_SOURCE_HOME;
import static com.adayo.service.sourcemngservice.Control.SrcMngServiceMng.MNG_STATUS.MNG_FAKESHUT;
import static com.adayo.service.sourcemngservice.Control.SrcMngServiceMng.MNG_STATUS.MNG_RUN_NORMAL;
import static com.adayo.service.sourcemngservice.Control.SrcMngServiceMng.MNG_STATUS.MNG_RUN_READY;
import static com.adayo.service.sourcemngservice.Control.SrcMngServiceMng.MNG_STATUS.MNG_RUN;
import static com.adayo.service.sourcemngservice.Control.SrcMngServiceMng.MNG_STATUS.MNG_WAIT;

/**
 * Created by admin on 2018/4/6.
 */

public class SrcMngServiceMng {
    private     static  final   String                   TAG = SrcMngServiceMng.class.getSimpleName();
    private     volatile static SrcMngServiceMng         m_SrcMngServiceMng;
    private                     SrcMngAppQueue           m_SrcMngAppQueue;
    private                     SrcAppMng                m_SrcAppMng;
    private                     SrcMngSharePreference    m_SrcMngSp;
    private                     SrcMngSystemOperateMng   m_SrcMngSysOperateMng;
    private                     SrcMngChannelMng         m_SrcMngChannelMng;
    private                     AppConfigFile            m_AppConfigFile;
    private                     SrcMngKeyMng             m_SrcMngKeyMng;
    private                     SrcMngDeviceMng          m_SrcMngDeviceMng;
    private                     SrcMngSysMng             m_SrcMngSysMng;
    private                     SrcAudioManager          m_SrcAudioManager;
    private      static         MNG_STATUS               m_mngStatus = MNG_STATUS.MNG_INITIAL;
    private                     Context                  m_Context;

    private                     List<ISourceMngMicCallBack> m_MicList;
    private                     HashMap<String, ISourceActionCallBack> m_destroyHashMap;
    private                     Map<MNG_STATUS, List<String>> mStatusMap;
    private                     SourceInfo               m_LastSourceInfo = null;
    private                     SrcMngStartLastSource    mSrcMngStartLastSource;
    private                     String lastSourceType = null;   //记忆的last音源
    private                     String lastUISourceType = null;        //记忆的last UI源
    private      static         LAST_SOURCE_STATUS  isLastSourceStarted = LAST_SOURCE_STATUS.LAST_STATUS_IDLE;
    private      static final   String LASTSOURCEKEY = "adayo.lastsource.finish";
    private      volatile       boolean switchSourceStatus = true;
    private      volatile       boolean netRadioStatus = false;
    private      Object         mLock = new Object();

    //启动lastSource的状态
    private enum LAST_SOURCE_STATUS
    {
        LAST_STATUS_IDLE,       //IDLE状态
        LAST_STATUS_START,      //START状态
        LAST_STATUS_FINISH      //完成状态
    }

    //处理请求状态
    public enum MNG_STATUS
    {
        MNG_INITIAL,        //首次初始化状态
        MNG_WAIT,           //等待状态
        MNG_RUN_READY,      //READY状态
        MNG_RUN_NORMAL,     //NORMAL状态
        MNG_RUN_UPDATE,     //升级状态
        MNG_FAKESHUT,       //假关机
        MNG_RUN             //运行状态
    }

    /**
     * 构造函数
     */
    private SrcMngServiceMng()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngServiceMng() begin");

        m_MicList = new ArrayList<>();
        m_destroyHashMap = new HashMap<>();
        mStatusMap = new HashMap<>();

        List<String> m_listReady = new ArrayList<>();
        m_listReady.add(AdayoSource.ADAYO_SOURCE_BT_PHONE);
		m_listReady.add(AdayoSource.ADAYO_SOURCE_CARPLAY_BT_PHONE);
		m_listReady.add(AdayoSource.ADAYO_SOURCE_ANDROID_AUTO_BT_PHONE);

        List<String> m_listNormal = new ArrayList<>();
        m_listNormal.add(AdayoSource.ADAYO_SOURCE_BT_PHONE);
		m_listNormal.add(AdayoSource.ADAYO_SOURCE_CARPLAY_BT_PHONE);
		m_listNormal.add(AdayoSource.ADAYO_SOURCE_ANDROID_AUTO_BT_PHONE);

        //MNG_FAKESHUT状态下可以启动的应用
        List<String> m_listRunFakeShut = new ArrayList<>();
        m_listRunFakeShut.add(AdayoSource.ADAYO_SOURCE_CAMERA);
        m_listRunFakeShut.add(AdayoSource.ADAYO_SOURCE_CLOCK);
        m_listRunFakeShut.add(AdayoSource.ADAYO_SOURCE_FAKESHUT);
		// fake shut 假关机状态允许切蓝牙电话
		m_listRunFakeShut.add(AdayoSource.ADAYO_SOURCE_BT_PHONE);

        //添加进Map中
        mStatusMap.put(MNG_RUN_READY, m_listReady);
        mStatusMap.put(MNG_RUN_NORMAL, m_listNormal);
        mStatusMap.put(MNG_FAKESHUT, m_listRunFakeShut);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngServiceMng() end");
    }

    /**
     * 获取该类的实例（双重同步锁，线程安全）
     * @return
     */
    public static SrcMngServiceMng getInstance()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() begin");

        if (m_SrcMngServiceMng == null)
        {
            synchronized (SrcMngServiceMng.class)
            {
                if (m_SrcMngServiceMng == null)
                {
                    m_SrcMngServiceMng = new SrcMngServiceMng();
                }
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() end");

        return m_SrcMngServiceMng;
    }

    /**
     * 设置上下文
     * @param context
     */
    public void setM_Context(Context context)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() begin");

        m_Context = context;

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() end");
    }

    /**
     * 执行初始化函数
     */
    public void initSrcMngServiceMng()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " initSrcMngServiceMng() begin");

		m_mngStatus             = MNG_WAIT;
		
		initSharedPrefrenced(); //初始化SharedPrefrenced
		
		lastUISourceType = m_SrcMngSp.getKeyValue("persist.CURRENT_UID");       //获取Last UI Source
        lastSourceType = m_SrcMngSp.getKeyValue("persist.CURRENT_SOURCEID");    //获取Last Audio Source
		
        m_SrcMngAppQueue        = SrcMngAppQueue.getInstance();                 //消息队列实例创建

        m_SrcAppMng             = SrcAppMng.getInstance();                      //源管理实例创建
        m_SrcMngSysOperateMng   = SrcMngSystemOperateMng.getInstance();         //系统操作（Mute/UnMute）实例创建
        m_SrcMngChannelMng      = SrcMngChannelMng.getInstance();               //声道切替实例创建
        m_AppConfigFile         = AppConfigFile.getInstance();                  //AppConfig实例创建

        m_SrcMngKeyMng          = SrcMngKeyMng.getInstance();                   //硬件Key监听管理创建
        m_SrcMngDeviceMng       = SrcMngDeviceMng.getInstance();                //外部设备插拔管理创建
        m_SrcMngSysMng          = SrcMngSysMng.getInstance();                   //System（电源等）管理创建
        m_SrcMngSysMng.init(m_Context);
        mSrcMngStartLastSource = new SrcMngStartLastSource6w01A();              //Last源启动方式初始化

        m_SrcAudioManager       = SrcAudioManager.getInstance();                //获取SrcAudioManager的实例

        //启动SourceMngThread
        startSourceMngThread();

        //重新启动处理
        Restart();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " initSrcMngServiceMng() end");
    }


    /**
     * 设定源管理的运行状态
     * @param status
     */
    public void setM_mngStatus(MNG_STATUS status) {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setM_mngStatus() begin status = " + status);

        m_mngStatus = status;

        //更新MNG状态
        updateMngStatus(status);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setM_mngStatus() end");
    }

    /**
     * 获取当前的状态
     * @return
     */
    public static MNG_STATUS getM_mngStatus() {
        return m_mngStatus;
    }

    public void setNetRadioStatus(boolean netRadioStatus){
        this.netRadioStatus = netRadioStatus;
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setNetRadioStatus  end netRadioStatus="+netRadioStatus);
    }

    /**
     * 初始化SharedPrefrenced
     */
    private void initSharedPrefrenced()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " initSharedPrefrenced() begin");

        //SharePreferenced实例获取
        m_SrcMngSp              = SrcMngSharePreference.getInstance();
        m_SrcMngSp.setM_Context(m_Context);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " initSharedPrefrenced() end");
    }

    /**
     * 发出请求切源请求
     * @param sourceInfo：请求源信息
     * @return : true(请求源加入到队列中) / false(当前不允许加入到切源队列中)
     */
    public synchronized boolean onRequest(SourceInfo sourceInfo)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onRequest() begin");

        if (sourceInfo == null)
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onRequest() sourceInfo is null");
            return false;
        }

        boolean ret = false;

        //手机互联检查
        SourceInfo srcInfo = checkPhoneConnect(sourceInfo);
        if (srcInfo == null)
        {
            srcInfo = sourceInfo;
        }

        //获取请求源信息
        AppInfo appInfo = AppConfigFile.getInstance().getAppInfoBySourceType(srcInfo.getM_SourcePackageName());
        AppSwitchInfo appSwitchInfo = null;

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onRequest() m_mngStatus = " + m_mngStatus);

        //运行状态 且 获取的APP不为null，所有的源都可以启动
        if (m_mngStatus == MNG_STATUS.MNG_RUN && srcInfo != null)
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onRequest() appInfo = " + appInfo);

            //adayo源
            if (appInfo != null) {
                appSwitchInfo = new AppSwitchInfo(appInfo,
                        SourceSwitch.getSourceSwitch(srcInfo.getM_SourceSwitchType()),
                        srcInfo.getMapFromBundle(),
                        srcInfo.getM_SourceActionName());
                appSwitchInfo.setOption(sourceInfo.getM_Options());
            }
            else
            {
                final int THIRD_APP_ID = 1000;
                final String THIRD_APP_PRIORLEVEL = "90";

                //3rd源
                AppInfo thirdAppInfo = new AppInfo(
                        THIRD_APP_ID,
                        "UNKNOWN",
                        THIRD_APP_PRIORLEVEL,
                        "UnStore",
                        "0",
                        AppConfigType.SourceType.getSourceType(srcInfo.getM_AudioType()),
                        srcInfo.getM_SourcePackageName(),
                        ""  ,
                        srcInfo.getM_AudioType() == AppConfigType.SourceType.UI.ordinal() ? 0 : 3,
                        "ENABLE",
                        AdayoSource.ADAYO_SOURCE_THIRD);    //第三方应用

                appSwitchInfo = new AppSwitchInfo(thirdAppInfo,
                        SourceSwitch.getSourceSwitch(srcInfo.getM_SourceSwitchType()),
                        null,
                        null);
                appSwitchInfo.setOption(null);
            }
            m_SrcMngAppQueue.addElement(appSwitchInfo);
            ret = true;
        }
        else if ((m_mngStatus == MNG_STATUS.MNG_INITIAL || m_mngStatus == MNG_WAIT.MNG_WAIT)
                && srcInfo != null)
        {
            //只将Carplay作为lastSource
            final String sourceType = srcInfo.getM_SourcePackageName();
            if ( ADAYO_SOURCE_CARPLAY_MEDIA.equals(sourceType))
            {
                //保存成开机启动的应用
                m_LastSourceInfo = sourceInfo;
            }
        }
        else if (m_mngStatus == MNG_RUN_READY && srcInfo != null)
        {
            ret = addElementIntoQueueByStatus(srcInfo, appInfo, MNG_RUN_READY);
            if (ret)
            {
                isLastSourceStarted = LAST_SOURCE_STATUS.LAST_STATUS_FINISH;
                updateMngStatus(m_mngStatus);
                startLastAudioSource(false);
            }
        }
        else if (m_mngStatus == MNG_RUN_NORMAL && srcInfo != null)
        {
            ret = addElementIntoQueueByStatus(srcInfo, appInfo, MNG_RUN_NORMAL);
            if (ret)
            {
                isLastSourceStarted = LAST_SOURCE_STATUS.LAST_STATUS_FINISH;
                updateMngStatus(m_mngStatus);
                startLastAudioSource(false);
            }
        }
        else if (m_mngStatus == MNG_FAKESHUT && srcInfo != null)  //在FAKESHUT态下只允许特定的APP启动
        {
            ret = addElementIntoQueueByStatus(srcInfo, appInfo, MNG_FAKESHUT);
        }
        else
        {

        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onRequest() end ret = " + ret);

        return ret;
    }

    /**
     * 将启动源加入到队列中
     * @param sourceInfo
     * @param appInfo
     * @param status
     * @return 加入成功（true） / 加入失败（false）
     */
    private boolean addElementIntoQueueByStatus(SourceInfo sourceInfo, AppInfo appInfo, MNG_STATUS status)
    {
        boolean ret = false;

        List<String> list = mStatusMap.get(status);
        if (list != null && list.contains(sourceInfo.getM_SourcePackageName()) && appInfo != null)
        {
            AppSwitchInfo appSwitchInfo = new AppSwitchInfo(appInfo,
                    SourceSwitch.getSourceSwitch(sourceInfo.getM_SourceSwitchType()),
                    sourceInfo.getMapFromBundle(),
                    sourceInfo.getM_SourceActionName());
            m_SrcMngAppQueue.addElement(appSwitchInfo);
            ret = true;
        }

        return ret;
    }

    /**
     * 判断sourceId是否具有音频焦点
     * @param sourceType : 源类型
     * @return ： true(持有音频焦点) / false(不持有音频焦点)
     */
    public boolean hasAudioFocus(final String sourceType)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " hasAudioFocus() begin sourceType = " + sourceType);

        String curAudioFocus = m_SrcAppMng.getM_curSrcID();
        boolean ret = false;

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " hasAudioFocus() curAudioFocus = " + curAudioFocus);

        if (sourceType != null && curAudioFocus.equals(sourceType))
        {
            ret = true;
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " hasAudioFocus() end ret = " + ret);

        return ret;
    }

    /**
     * 获取当前的Audio音频
     * @return 当前持有音频焦点的音源
     */
    public String getCurrentAudioFocus()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getCurrentAudioFocus() begin");

        String curAudioFocus = m_SrcAppMng.getM_curSrcID();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getCurrentAudioFocus() end curAudioFocus = " + curAudioFocus);

        return curAudioFocus;
    }

    /**
     * 获取当前的UID
     * @return
     */
    public String getCurrentUID()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getCurrentUID() begin");

        String curUID = m_SrcAppMng.getM_curUID();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getCurrentUID() end UID = " + curUID);

        return curUID;
    }

    /**
     * 电源状态变为Normal时，启动默认源
     */
    private void startLastAudioSource(boolean flag)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " startLastAudioSource() begin flag = " + flag);

        if (flag)
        {
            //启动默认源
            startRequestAppOn();
        }

        //更新shareinfo
        final int LAST_SOURCE_SHARE_ID = 200;
        updateShareInfo(LAST_SOURCE_SHARE_ID, "isStartUp", "ON");

        SystemProperties.set(LASTSOURCEKEY, "OVER");

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " startLastAudioSource() end");
    }

    /**
     * 启动SourceMngThread
     */
    private void startSourceMngThread()
    {
        //启动Thread
        SourceMngThread srcThread = new SourceMngThread();
        srcThread.init(m_Context);

        Thread thread = new Thread(srcThread);
        thread.start();
    }

    /**Launcher启动完毕
     */
    public void notifyLauncherFinished()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyLauncherFinished() begin m_mngStatus="+m_mngStatus);

        if (m_mngStatus == MNG_RUN)
        {
            return;
        }

        isLastSourceStarted = LAST_SOURCE_STATUS.LAST_STATUS_START;

        updateMngStatus(m_mngStatus);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyLauncherFinished() end");
    }

    /**
     * 系统冷启动，启动Last源
     */
    public void startRequestAppOn() {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " startRequestAppOn() begin");

        //首次启动时调用时，启动过程中是否有外接设备接入（CarPlay/USB）
        if (null != m_LastSourceInfo)
        {
            String packageName = m_LastSourceInfo.getM_SourcePackageName();
            AppInfo appInfo = AppConfigFile.getInstance().getAppInfoBySourceType(packageName);
            if (appInfo != null)
            {
                lastUISourceType = null;
                lastSourceType = appInfo.getM_SourceType();
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " startRequestAppOn() lastUISourceType = " + lastUISourceType + " lastSourceType = " + lastSourceType);

        //如果mSrcMngStartLastSource不为null，则拉起LastSource
        if (mSrcMngStartLastSource != null)
        {
            mSrcMngStartLastSource.startLastSource(lastUISourceType, lastSourceType);
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " startRequestAppOn() end");
    }

    /**
     * 通知SrcMng音源发生改变
     * @param sourceType ： 源类型
     * @param dorationHint：dorationHint
     * @return
     */
    public synchronized boolean notifyServiceAudioChange(final String sourceType, final int dorationHint)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyServiceAudioChange() begin sourceType = " + sourceType + " dorationHint = " + dorationHint);

        switch (dorationHint)
        {
            //获取到音源
            case AudioManager.AUDIOFOCUS_GAIN:
                onAudioFocusGain(sourceType);
                break;

            //失去音源 | 一时失去音源时
            case AudioManager.AUDIOFOCUS_LOSS:
                onAudioFocusLoss(sourceType);
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                onAudioFocusLossTransient(sourceType);
                break;

            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                onAudioFocusLossTransientCanDuck(sourceType);
                break;

            default:
                break;
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyServiceAudioChange() end");

        return true;
    }

    /**
     * 音源重新获取焦点后的动作
     * @param sourceType：源类型
     */
    private synchronized void onAudioFocusGain(final String sourceType)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onAudioFocusGain() begin sourceType = " + sourceType);

        //将获取到音频焦点的源保存
        m_SrcAppMng.setM_curSrcID(sourceType);

        //调整声道
        SrcAudioManager.getInstance().switchAudioChannel(sourceType);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onAudioFocusGain() end");
    }

    /**
     * 音频失去焦点后的动作
     * @param sourceType：源类型
     */
    private synchronized void onAudioFocusLoss(final String sourceType)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onAudioFocusLoss() Begin sourceType = " + sourceType);
        //释放sourceType #594162  
		//2020-07-08[  669654 602622]  modify by gj start
        //m_SrcAppMng.abandonAdayoFocus(sourceType);
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onAudioFocusLoss() end + "+sourceType);
    }

    /**
     * 音频一时失去焦点后的动作
     * @param sourceType：源类型
     */
    private synchronized void onAudioFocusLossTransient(final String sourceType)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onAudioFocusLossTransient() sourceType = " + sourceType);
    }

    /**
     * 音频一时失去焦点 & 降低音量后的动作
     * @param sourceType：源类型
     */
    private synchronized void onAudioFocusLossTransientCanDuck(final String sourceType)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onAudioFocusLossTransientCanDuck() sourceType = " + sourceType);
    }

    /**
     * 通知SrcMng模块UI发生变更
     * @param sourceType：源类型
     * @return
     */
    public boolean notifyServiceUIChange(final String sourceType)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyServiceUIChange() begin sourceType = " + sourceType);

        m_SrcAppMng.setM_curUID(sourceType);
		// bug #632135 WarningApp概率性无法启动  add by Y4008 Luj  20/06/16 
        if (ADAYO_SOURCE_HOME.equals(sourceType) &&
                MNG_STATUS.MNG_RUN_NORMAL == m_mngStatus &&
                LAST_SOURCE_STATUS.LAST_STATUS_IDLE == isLastSourceStarted) {
            notifyLauncherFinished();
        }
		// add by Y4008 Luj  20/06/16 end

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyServiceUIChange() end");

        return true;
    }

    /**
     * Mic权限注册回调函数
     * @param callback：回调函数
     * @return
     */
    public boolean requestMicFocus(final ISourceMngMicCallBack callback)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " requestMicFocus() begin");

        //如果当前的List中存在该source的映射关系
        if (m_MicList != null)
        {
            m_MicList.add(callback);
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " requestMicFocus() end");

        return true;
    }

    /**
     * Mic权限注销回调函数
     * @param callback：回调函数
     * @return
     */
    public boolean abandonMicFocus(final ISourceMngMicCallBack callback)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " abandonMicFocus() begin");

        int listSize = m_MicList.size();
        for (int index = 0; index < listSize; index++)
        {
            ISourceMngMicCallBack func = m_MicList.get(index);
            if (func == callback)
            {
                m_MicList.remove(func);
                func = null;
                if (index == listSize - 1)
                {
                    onMicChange();
                }
                break;
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " abandonMicFocus() end");

        return true;
    }

    /**
     * 栈顶Mic通知
     */
    private void onMicChange()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onMicChange() begin");

        int listSize = m_MicList.size();

        try
        {
            if (listSize > 0)
            {
                m_MicList.get(listSize - 1).onChange();
            }
        }
        catch (RemoteException e)
        {
            e.printStackTrace();
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onMicChange() RemoteException");
        }
        catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onMicChange() Exception");
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onMicChange() end");
    }

    /**
     * 切替应用操作
     * @param requestAppInfo：请求App信息
     * @return
     * @throws RemoteException
     */
    public boolean switchSource(final AppSwitchInfo requestAppInfo) throws RemoteException {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " switchSource() begin");

        boolean ret = false;

        //将当前请求应用保存
        String reqAppId = requestAppInfo.getM_AppInfo().getM_SourceType();
        SourceSwitch switchType = requestAppInfo.getM_SrcSwitchType();

        if (switchType == SourceSwitch.APP_ON)
        {
            //通知请求应用ON
            if (reqAppId != null && !reqAppId.equals(AdayoSource.ADAYO_SOURCE_NULL))
            {
                SrcMngAppOn(requestAppInfo);
            }
        }
        else if (switchType == SourceSwitch.APP_OFF)
        {
			LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " switchSource() APP_OFF");

			//确认Map是否为NUll
			if (m_destroyHashMap != null)
			{
			    //在Map中遍历是否存在应用的关闭回调函数
                ISourceActionCallBack callback = m_destroyHashMap.get(reqAppId);
                if (callback != null)
                {
                    callback.SourceOff();
                }
                else
                {
                    //若不在Map中，可以认为是第三方应用，强制关闭
                    final String packageName = requestAppInfo.getM_AppInfo().getM_AppPackageName();
                    forceCloseApp(packageName);
                }
            }
        }
        else
        {

        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " switchSource() end");

        return ret;
    }

    /**
     * 强制关闭应用（适用于第三方应用或者未注册关闭回调接口的应用）
     */
    private void forceCloseApp(@NonNull final String packageName)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " forceCloseApp() begin packageName = " + packageName);

        if (packageName == null)
        {
            return;
        }

        try
        {
            ActivityManager m= (ActivityManager)m_Context.getSystemService(ACTIVITY_SERVICE);

            Method method = m.getClass().getMethod("forceStopPackage", String.class);

            method.setAccessible(true);

            method.invoke(m, packageName);

        }catch(Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " forceCloseApp() interrupt");
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " forceCloseApp() end");
    }

    /**
     * AppOn处理,启动请求应用APP
     * @param requestAppInfo：请求应用
     * @return
     */
    private boolean SrcMngAppOn(final AppSwitchInfo requestAppInfo)
    {
        boolean ret = false;
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngAppOn() begin switchSourceStatus :" + switchSourceStatus);
        
        //current status is false,exit
        if (!switchSourceStatus)
        {
            return false;
        }

        String reqPackageName = requestAppInfo.getM_AppInfo().getM_AppPackageName();
        String actionName = requestAppInfo.getM_actionName();
        Map<String, String> reqMap = requestAppInfo.getM_hashMap();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngAppOn() reqPackageName = " + reqPackageName + " reqMap = " + reqMap + " actionName = " + actionName);

        PackageInfo pif = null;
        try {
            pif = m_Context.getPackageManager().getPackageInfo(reqPackageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        if (pif == null)
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngAppOn() no pif");
            return false;
        }

        final String reqSrcId = requestAppInfo.getM_AppInfo().getM_SourceType();
        // add by Y4008 兼容伴听启动方式2020-8-19
        boolean isNetRadio = AdayoSource.ADAYO_SOURCE_NET_RADIO.equals(reqSrcId);
        //如果是启动Launcher
        if (AdayoSource.ADAYO_SOURCE_HOME.equals(reqSrcId))
        {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addCategory(Intent.CATEGORY_HOME);
            // 2020-7-8 add Y4008 ［674436］Home源传map
            Bundle bundle = new Bundle();
            if (reqMap != null)
            {
                if (!reqMap.containsKey("SourceType"))
                {
                    reqMap.put("SourceType", requestAppInfo.getM_AppInfo().getM_SourceType());
                }
            }
            else
            {
                reqMap = new HashMap<>();
                reqMap.put("SourceType", requestAppInfo.getM_AppInfo().getM_SourceType());
            }
            bundle.putSerializable("map", (HashMap)reqMap);
            intent.putExtras(bundle);
            // 2020-7-8 add Y4008 ［674436］Home源传map
            m_SrcAppMng.setM_curUID(AdayoSource.ADAYO_SOURCE_HOME);

            m_Context.startActivity(intent, requestAppInfo.getOption());
			
			sendAppChangeBroadCast(reqPackageName);
        }
        else
        {
            Intent intent = null;

            //action不为null时，指定启动action的activity
            if (actionName != null)
            {
                intent = new Intent();
                Intent resolveIntent = new Intent(actionName);
                resolveIntent.addCategory(Intent.CATEGORY_DEFAULT);
                resolveIntent.setPackage(reqPackageName);

                List<ResolveInfo> resolveInfoList = m_Context.getPackageManager().queryIntentActivities(resolveIntent, 0);
                if (resolveInfoList != null && resolveInfoList.size() > 0)
                {
                    ResolveInfo resolveInfo = resolveInfoList.get(0);
                    if (resolveInfo != null)
                    {
                        String packageName = resolveInfo.activityInfo.packageName;
                        String className = resolveInfo.activityInfo.name;
                        ComponentName cn = new ComponentName(packageName, className);
                        intent.setComponent(cn);
                    }
                    else
                    {
                        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngAppOn() resolveInfo is null");
                        intent = startLauncherApp(reqPackageName);
                        if (intent == null)
                        {
                            return false;
                        }
                    }
                }
                else
                {
                    LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngAppOn() resolveInfoList == null || resolveInfoList.size() <= 0");
                    intent = startLauncherApp(reqPackageName);
                    if (intent == null)
                    {
                        return false;
                    }
                }
            }
            else
            {
                intent = startLauncherApp(reqPackageName);
                if (intent == null)
                {
                    return false;
                }
            }
            if (null != intent)
            {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }

            //如果有其他参数
            Bundle bundle = new Bundle();
            if (reqMap != null)
            {
                if (!reqMap.containsKey("SourceType"))
                {
                    reqMap.put("SourceType", requestAppInfo.getM_AppInfo().getM_SourceType());
                }
            }
            else
            {
                reqMap = new HashMap<>();
                reqMap.put("SourceType", requestAppInfo.getM_AppInfo().getM_SourceType());
            }
            bundle.putSerializable("map", (HashMap)reqMap);
            if (null != intent) intent.putExtras(bundle);
            if (isNetRadio) {// add by Y4008 兼容伴听启动方式2020-8-19 service not bind  success onCreate
                KLClientAPI api = KLClientAPI.getInstance();
                api.init(m_Context, KLClientAPI.KEY_AUTO, new IServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName componentName) {
                        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngAppOn onServiceConnected");
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName componentName) {
                        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngAppOn onServiceDisconnected");
                    }
                });
                api.launchApp(true);
            }
            else
            {
                m_Context.startActivity(intent,requestAppInfo.getOption());

            }
			sendAppChangeBroadCast(reqPackageName);
        }

        AppInfo info = AppConfigFile.getInstance().getAppInfoBySourceType(reqSrcId);
        if (info == null)
        {
            m_SrcAppMng.setM_curUID(reqPackageName);
        }
        else
        {
            m_SrcAppMng.setM_curUID(info.getM_SourceType());
        }

        ret = true;

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngAppOn() end ret = " + ret);

        return ret;
    }


    /**
     * 从应用的主入口进入
     * @param requestAppPackageName : 请求的切源包名
     * @return ：
     */
    private Intent startLauncherApp(@NonNull final String requestAppPackageName)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " startLauncherApp() Begin requestAppPackageName = " + requestAppPackageName);

        if (requestAppPackageName == null || requestAppPackageName.isEmpty())
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " startLauncherApp() requestAppPackageName = " + requestAppPackageName);
            return null;
        }

        Intent intent = m_Context.getPackageManager().getLaunchIntentForPackage(requestAppPackageName);
        if (intent == null)
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " startLauncherApp() end intent is null");
            return null;
        }
        intent.setPackage(requestAppPackageName);
        boolean isNetRadio = "com.edog.car".equals(requestAppPackageName);

        Intent intentRlt = new Intent();
        intentRlt.setPackage(requestAppPackageName);

        ComponentName cName = intent.getComponent();
        if (cName == null || isNetRadio)
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " startLauncherApp() cName is null or net radio");
            intentRlt = intent;
        }
        else
        {
            intentRlt.setComponent(cName);
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " startLauncherApp() end Successed");

        return intentRlt;
    }

    /**
     * 通过包名判断是否有效显示
     * @param packageName：包名
     * @return Android原生应用（false） / 第三方应用（true）/Adayo应用（true）
     */
    public boolean getSourceAvailable(final String packageName)
    {
        boolean ret = false;

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getSourceAvailable() begin packageName = " + packageName);

        //从配置名单中获取APP
        AppInfo info = AppConfigFile.getInstance().getAppInfoByPackageName(packageName);

        //如果APP不在配置名单中，则认为是可以被拉起的
        String exists = (info == null) ? "ENABLE" : info.getM_AppExists();

        if (exists.equals("ENABLE"))
        {
            ret = true;
        }
        else
        {
            ret = false;
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getSourceAvailable() end ret = " + ret);

        return ret;
    }

    /**
     * 通知App关闭
     * @param sourceType：源类型
     * @return
     */
    public boolean notifyAppFinished(final String sourceType)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyAppFinished() begin sourceType = " + sourceType);

        m_SrcAppMng.AppFinished(sourceType);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyAppFinished() end");

        return true;
    }

    /**
     * 通知放弃AbandonAudioFocus
     * @param sourceType：源类型
     * @return
     */
    public boolean notifyAbandonAudioFocus(final String sourceType)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyAbandonAudioFocus() begin sourceType = " + sourceType);

        m_SrcAppMng.abandonAdayoFocus(sourceType);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyAbandonAudioFocus() end");

        return true;
    }

    /**
     * 根据sourceType获取audioChannel的值
     * @param sourceType ： sourceType值
     * @return ： audioChannel的值
     */
    public int getAudioChannelBySourceId(@NonNull final String sourceType)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getAudioChannelBySourceId() begin sourceType = " + sourceType);

        AppInfo aif = AppConfigFile.getInstance().getAppInfoBySourceType(sourceType);
        int ret = (aif == null) ? -1 : aif.getM_AudioChannel();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getAudioChannelBySourceId() end ret = " + ret);

        return ret;
    }

    /**
     * 通知切换到指定声道 & 将新的音频信息记录下来
     * @param sourceType：源类型
     * @return: true(通知成功) / false(通知失败)
     */
    public boolean notifyRequestAudioFocus(@NonNull final String sourceType)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyRequestAudioFocus() begin sourceType = " + sourceType);

		if (sourceType != null && (sourceType.equals(m_SrcAppMng.getM_curSrcID()) || sourceType.equals(AdayoSource.ADAYO_SOURCE_NULL)))
		{
			return true;
		}

        if (MNG_FAKESHUT == m_mngStatus)
        { //565790 假关机的时候 VR可以申请音频焦点导致记忆源启动失败
            if (ADAYO_SOURCE_FAKESHUT.equals(sourceType))
                m_SrcAppMng.setM_curSrcID(sourceType);
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + "notifyRequestAudioFocus forbid FAKE SHUT other audio setM_curSrcID ");
        }
        else
        {
            m_SrcAppMng.setM_curSrcID(sourceType);
        }
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyRequestAudioFocus() end");

        return true;
    }

    /**
     * 获取当前的StreamType
     * @return
     */
    public int getCurrentStreamType()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getCurrentStreamType() begin");

        AudioManager am = (AudioManager)m_Context.getSystemService(Service.AUDIO_SERVICE);
        int currentStreamType = am.getCurrentPlayingTopPolicyStream();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getCurrentStreamType() end currentStreamType = " + currentStreamType);

        return currentStreamType;
    }

    /**
     * 注册各APP关闭的回调函数
     * @param sourceType：源类型
     * @param callback
     * @return
     */
    public synchronized boolean registeSourceActionCallBackFunc(final String sourceType, final ISourceActionCallBack callback)
    {
		LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " registeSourceActionCallBackFunc() begin sourceType = " + sourceType);

		//异常情况判断
		if (sourceType == null || callback == null || sourceType.equals(AdayoSource.ADAYO_SOURCE_NULL))
        {
            return false;
        }

        //插入到Map中
        m_destroyHashMap.put(sourceType, callback);
		
		LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " registeSourceActionCallBackFunc() end");

        return true;
    }

    /**
     * 注销各APP关闭的回调函数
     * @param sourceType：源类型
     * @return
     */
    public synchronized boolean unRegisteSourceActionCallBackFunc(final String sourceType)
    {
		LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " unRegisteSourceActionCallBackFunc() begin sourceType = " + sourceType);
		
        boolean ret = false;

        //异常情况判断
        if (sourceType == null || sourceType.equals(AdayoSource.ADAYO_SOURCE_NULL))
        {
            return false;
        }

        //判断Map中是否存在packageName
        if (m_destroyHashMap.containsKey(sourceType))
        {
            ISourceActionCallBack callback = m_destroyHashMap.get(sourceType);
            if (callback != null)
            {
                callback = null;
            }

            m_destroyHashMap.remove(sourceType);
            ret = true;
        }
		
		LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " unRegisteSourceActionCallBackFunc() end ret = " + ret);

        return ret;
    }

    /**
     * 设定源管理状态
     * @param status:
     * @return
     */
    public boolean setSrcMngStatus(final MNG_STATUS status)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setSrcMngStatus() begin status = " + status);

        m_mngStatus = status;

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setSrcMngStatus() end");

        return true;
    }

    /**
     * 获取当前声音通道
     * @return ：当前声音通道ID
     */
    public int onGetCurrentChannel(){
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onGetCurrentChannel() begin");

        int ret = m_SrcMngChannelMng.getCurrentAudioChannel();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onGetCurrentChannel() end ret = " + ret);

        return ret;
    }

    /**
     * 判断音频焦点是否被占用
     * @return : true（被占用） / false(未被占用)
     */
    public boolean onIsAudioFocusOccupy(){
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onIsAudioFocusOccupy() begin");

        boolean ret = AdayoSource.ADAYO_SOURCE_NULL.equals(m_SrcAppMng.getM_curSrcID()) ? false : true;

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onIsAudioFocusOccupy() end ret = " + ret);

        return ret;
    }

    /**
     * 切替音频通道
     * @param channelId ： 音频通道ID
     * @return : 音频通道ID
     */
    public synchronized int onSwitchChannel(final int channelId){
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onSwitchChannel() begin channelId = " + channelId);

        boolean ret = SrcAudioManager.getInstance().switchAudioChannel(channelId);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onSwitchChannel() end ret = " + ret);

        return ret == true ? 0 : -1;
    }

    /**
     * 更新MNG状态
     */
    private synchronized void updateMngStatus(final MNG_STATUS powerStatus)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " updateMngStatus() begin powerStatus = " + powerStatus + " isLastSourceStarted = " + isLastSourceStarted);

        if (powerStatus == MNG_STATUS.MNG_RUN_READY || powerStatus == MNG_STATUS.MNG_RUN_NORMAL)
        {
            if (LAST_SOURCE_STATUS.LAST_STATUS_START == isLastSourceStarted)
            {
                m_mngStatus = MNG_RUN;

                isLastSourceStarted = LAST_SOURCE_STATUS.LAST_STATUS_FINISH;

                //启动LastSource
                startLastAudioSource(true);
            }
            else if (LAST_SOURCE_STATUS.LAST_STATUS_FINISH == isLastSourceStarted)
            {
                m_mngStatus = MNG_RUN;
            }
            else
            {
                //do nothing
            }
        }else if (powerStatus == MNG_RUN){
            //#698313 #690717 执行2次 导致记忆源状态错误
            if (LAST_SOURCE_STATUS.LAST_STATUS_START == isLastSourceStarted)
            {
                isLastSourceStarted = LAST_SOURCE_STATUS.LAST_STATUS_FINISH;
            }

        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " updateMngStatus() end m_mngStatus = " + m_mngStatus);
    }

    /**
     * 获取Context
     * @return
     */
    public Context getM_Context() {
        return m_Context;
    }

    /**
     * 调用Framework的duckAudioStream
     * @param streamType：streamType
     * @return
     */
    public boolean duckAudioStream(final int streamType){
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " duckAudioStream() begin streamType = " + streamType);

        AudioManager am = (AudioManager)m_Context.getSystemService(Service.AUDIO_SERVICE);
        boolean ret = am.duckAudioStream(streamType);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " duckAudioStream() end ret = " + ret);

        return ret;
    }

    /**
     * 调用Framework的duckAudioStream
     * @param streamType：streamType
     * @return
     */
    public boolean unDuckAudioStream(final int streamType){
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " unDuckAudioStream() begin streamType = " + streamType);

        AudioManager am = (AudioManager)m_Context.getSystemService(Service.AUDIO_SERVICE);
        boolean ret = am.unduckAudioStream(streamType);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " unDuckAudioStream() end ret = " + ret);

        return ret;
    }
 /**
     * 设置系统mute状态
     * @param muteStatus ：1（系统Mute） / 0(解除系统Mute) / -1(不处理)
     * @return ： true(设置成功) / false(设置失败)
     */
    public boolean setSystemMuteStatus(final int requestStreamType, final int muteStatus)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setSystemMuteStatus() begin requestStreamType = " + requestStreamType + " muteStatus = " + muteStatus);

        if (muteStatus < -1 || muteStatus > 1)
        {
            return false;
        }

        final int NAVI_STREAM = 11; //导航音音流
        final int RADAR_STREAM = 12; //雷达音音流

        //如果是导航音申请焦点播放
        //如果是导航/倒车/AVM音频流时音申请焦点播放
        if (RADAR_STREAM == requestStreamType || requestStreamType == NAVI_STREAM)
        {
            //维持Mute状态
        }
        else
        {
            //解除Mute状态
            SrcMngSystemOperateMng.getInstance().setSysMute(muteStatus);
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setSystemMuteStatus() end Success");

        return true;
    }
    /**
     * 判断app是否已经安装OK
     * @return：true（已安装） / false(未安装)
     */
    public boolean appInstalled(@NonNull final String pn)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " appInstalled() begin pn = " + pn);

        Intent intent = m_Context.getPackageManager().getLaunchIntentForPackage(pn);
        if (intent == null)
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " startLauncherApp() end install NG");
            return false;
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " appInstalled() end install OK");

        return true;
    }

    /**
     * 获取能否申请音频焦点
     * @param sourceType：源ID
     * @return: true(允许申请音频焦点) / false(不云讯申请音频焦点)
     */
    public boolean canRequestAudioFocus(@NonNull final String sourceType)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " canRequestAudioFocus() begin sourceType = " + sourceType);

        if (sourceType == null || sourceType.isEmpty())
        {
            return false;
        }

        boolean ret = false;

        //获取当前的音频焦点
        final String curAudioID = getCurrentAudioFocus();
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " canRequestAudioFocus() curAudioID = " + curAudioID);

        //音频焦点是RVC或者AVM时，直接返回true
        if ((AdayoSource.ADAYO_SOURCE_BT_PHONE.equals(curAudioID) &&
                (AdayoSource.ADAYO_SOURCE_RVC.equals(sourceType) ||
                        AdayoSource.ADAYO_SOURCE_AVM.equals(sourceType))) ||
                AdayoSource.ADAYO_SOURCE_RVC.equals(curAudioID) ||
                AdayoSource.ADAYO_SOURCE_AVM.equals(curAudioID))
        {
            return true;
        }

        // #536912 按car bit的要求  当前音频焦点是VR的时候 car bit导航申请不要音频焦点
        if (AdayoSource.ADAYO_SOURCE_VR.equals(curAudioID) & AdayoSource.ADAYO_SOURCE_CARBIT_NAVI.equals(sourceType))
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " current audioFocus() vr canRequestAudioFocus result= false  " + sourceType);
            return false;
        }
        //当前音频App信息
        final AppInfo curAppInf = AppConfigFile.getInstance().getAppInfoBySourceType(curAudioID);
        final AppInfo reqAppInf = AppConfigFile.getInstance().getAppInfoBySourceType(sourceType);

        if (curAppInf == null && reqAppInf == null)
        {
            ret = false;
        }
        else if (curAppInf == null && reqAppInf != null)
        {
            ret = true;
        }
        else if (curAppInf != null && reqAppInf == null)
        {
            ret = false;
        }
        else
        {
            final int curPriority = curAppInf.getM_PriorLevel();    //获取当前音源的优先级别
            final int reqPriority = reqAppInf.getM_PriorLevel();    //获取请求源的优先级别
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " canRequestAudioFocus() curPriority = " + curPriority + " reqPriority = " + reqPriority);
            if (curPriority >= reqPriority)
            {
                ret = true;
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " canRequestAudioFocus() end ret = " + ret);

        return ret;
    }
    /**
     * 获取当前的倒车状态
     * @return : true(倒车中) / false(非倒车中)
     */
    public boolean getBackCarStatus()
    {
        final int SHARE_INFO_NUM = 16;
        final String s = ShareDataManager.getShareDataManager().getShareData(SHARE_INFO_NUM);
        if (s == null)
        {
            return false;
        }

        JSONObject obj = null;
        boolean ret = false;
        try
        {
            obj = new JSONObject(s);
            if (obj != null && obj.has("backCarState"))
            {
                ret = obj.getBoolean("backCarState");
            }
        }catch (JSONException e)
        {
            e.printStackTrace();
        }

        return ret;
    }

    /**
     * 获取UIlist
     * @return
     */
    public List<String> getUIList()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getUIList() begin");

        List<String> list = SrcAppMng.getInstance().getM_UIList();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getUIList() end");

        return list;
    }

    /**
     * 获取Audio list
     * @return
     */
    public List<String> getAudioList()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getAudioList() begin");

        List<String> list = SrcAppMng.getInstance().getM_AudioList();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getAudioList() end");

        return list;
    }

    /**
     * 判断AudioID是否和当前的UID相同
     * @param s : 源ID
     * @return : true(相同) / false (不相同)
     */
    public boolean isCurUIDAndAudioId(final String s)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " isCurUIDAndAudioId() begin s = " + s);

        if (s == null || s.isEmpty())
        {
            return false;
        }

        boolean ret = false;

        final String curUID = SrcAppMng.getInstance().getM_curUID();
        if (s.equals(curUID))
        {
            ret = true;
        }
        else
        {
            final AppInfo appInfo = AppConfigFile.getInstance().getAppInfoBySourceType(s);
            if (appInfo != null)
            {
                final AppConfigType.SourceType appType = appInfo.getM_AppType();
                if (AppConfigType.SourceType.UI.equals(appType))
                {
                    ret = true;
                }
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " isCurUIDAndAudioId() end ret = " + ret);

        return ret;
    }

    /**
     * 检查手机互联
     * @param srcInfo
     */
    private SourceInfo checkPhoneConnect(SourceInfo srcInfo)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " checkPhoneConnect() begin");

        SourceInfo sourceInfo = null;

        //检查Android Auto连接状态，并转换
        Map<String, String> androidAutoMap = new HashMap<>();
        androidAutoMap.put(AdayoSource.ADAYO_SOURCE_BT_PHONE, AdayoSource.ADAYO_SOURCE_ANDROID_AUTO_BT_PHONE);
        androidAutoMap.put(AdayoSource.ADAYO_SOURCE_BT_AUDIO, AdayoSource.ADAYO_SOURCE_ANDROID_AUTO_BT_AUDIO);

        final String androidAutoKey = "androidauto_conn_state";
        final int androidAutoShareId = 71;
        sourceInfo = changeSourceInfo(srcInfo, androidAutoKey, androidAutoShareId, androidAutoMap);

        if (sourceInfo != null)
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " checkPhoneConnect() Android Auto end");
            return sourceInfo;
        }

        //检查CARPLAY连接状态，并转换
        Map<String, String> carPlayMap = new HashMap<>();
        carPlayMap.put(AdayoSource.ADAYO_SOURCE_BT_PHONE, AdayoSource.ADAYO_SOURCE_CARPLAY_BT_PHONE);
        carPlayMap.put(AdayoSource.ADAYO_SOURCE_BT_AUDIO, AdayoSource.ADAYO_SOURCE_CARPLAY_BT_AUDIO);

        final String carPlayKey = "connection";
        final int carPlayShareId = 4;
        sourceInfo = changeSourceInfo(srcInfo, carPlayKey, carPlayShareId, carPlayMap);

        if (sourceInfo != null)
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " checkPhoneConnect() Carplay end");
            return sourceInfo;
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " checkPhoneConnect() end");

        return sourceInfo;
    }

    /**
     * 将请求的源进行判断是否需要转换为Android Auto
     * @param srcInfo
     */
    private SourceInfo changeSourceInfo(SourceInfo srcInfo, final String key, final int shareId, final Map<String, String> phoneMap)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " changeSourceInfo() begin");

        SrcMngOperateShareInfo shareInfo = new SrcMngOperateShareInfo(key, shareId);
        Object obj = shareInfo.getmValue();
        if (obj == null)
        {
            return null;
        }

        //获取Android Auto连接状态,未连接时，退出
        boolean ret = Boolean.parseBoolean(obj.toString());
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " changeSourceInfo() ret = " + ret);
        if (!ret)
        {
            return null;
        }

        String audioId = srcInfo.getM_SourcePackageName();
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " changeSourceInfo() audioId = " + audioId);

        SourceInfo sourceInfo = null;
        if (phoneMap.containsKey(audioId))
        {
            String changeAudioID = phoneMap.get(audioId);
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " changeSourceInfo() changeAudioID = " + changeAudioID);

            Map<String, String> map = srcInfo.getMapFromBundle();
            if (map == null)
            {
                map = new HashMap<>();
            }
            map.put("SourceType", changeAudioID);
            String action = "com.adayo.app.action." + changeAudioID;

            sourceInfo = new SourceInfo(changeAudioID, action, map, srcInfo.getM_SourceSwitchType(), srcInfo.getM_AudioType(), srcInfo.getM_Options());
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " changeSourceInfo() end");

        return sourceInfo;
    }

    /**
     * 获取网络状态
     * @return : true(连接中) / false(未连接)
     */
    public boolean getNetConnectStatus()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getNetConnectStatus()");

        ConnectivityManager connectivityManager = (ConnectivityManager)m_Context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnected())
        {
            return true;
        }

        return false;
    }

    /**
     * 更新shareinfo信息
     * @param shareId:shareId
     * @param key : key
     * @param value : value
     */
    private void updateShareInfo(final int shareId, final String key, final String value)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " updateShareInfo() begin shareId = " + shareId + " key = " + key + " value = " + value);

        if (key == null || key.isEmpty() || value == null || value.isEmpty())
        {
            return;
        }

        HashMap<String, String> map = new HashMap<>();
        map.put(key, value);
        ShareDataManager shareDataManager = ShareDataManager.getShareDataManager();
        Gson gson = new Gson();
        String content = gson.toJson(map);
        shareDataManager.sendShareData(shareId, content);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " updateShareInfo() end");
    }

    /**
     * 如果SourceMng运行过程中重启，启动以后需要对当前的状态进行判断，是否能够进行切源
     */
    private void Restart()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " Restart() begin");

        String value = SystemProperties.get(LASTSOURCEKEY);
        if ("OVER".equals(value))
        {
            isLastSourceStarted = LAST_SOURCE_STATUS.LAST_STATUS_FINISH;
            updateMngStatus(m_mngStatus);
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " Restart() end");
    }

    /**
     * 当前UI是Android Auto
     * @return : false(不是) / true(是)
     */
    private boolean isCurUIDAndroidAuto(final SourceInfo srcInfo)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " isCurUIDAndroidAuto() begin");
        final String curUID = getCurrentUID();
        boolean ret = false;

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " isCurUIDAndroidAuto() curUID = " + curUID);
        if (curUID != null && curUID.indexOf("ADAYO_SOURCE_ANDROID_AUTO") != -1)
        {
            final String reqAppId = srcInfo.getM_SourcePackageName();
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " isCurUIDAndroidAuto() reqAppId = " + reqAppId);
            if (srcInfo != null && AdayoSource.ADAYO_SOURCE_BT_PHONE.equals(reqAppId))
            {
                ret = true;
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " isCurUIDAndroidAuto() end ret = " + ret);

        return ret;
    }
	
		
	/**
     * 发送切源广播
     * @param appName:应用包名
     */
    private void sendAppChangeBroadCast(final String appName)
    {
        Intent intent = new Intent("com.adayo.app.change");
        intent.putExtra("AppName", appName);
        m_Context.sendBroadcast(intent);
    }
    
    /**
     * 恢复切源
     */
    public void resumeSwitchSource()
    {
        synchronized(mLock)
        {
            switchSourceStatus = true;
        }
    }
    
    /**
     * 暂停切源
     */
    public void pauseSwitchSource()
    {
        synchronized(mLock)
        {
            switchSourceStatus = false;
        }
    }
}
