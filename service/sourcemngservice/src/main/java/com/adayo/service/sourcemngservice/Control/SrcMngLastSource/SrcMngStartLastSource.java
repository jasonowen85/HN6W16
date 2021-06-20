package com.adayo.service.sourcemngservice.Control.SrcMngLastSource;



import com.adayo.adayosource.AdayoSource;
import com.adayo.proxy.devmgr.DevProxyManager;
import com.adayo.proxy.sourcemngproxy.Beans.AppConfigType;
import com.adayo.proxy.sourcemngproxy.Beans.SourceInfo;
import com.adayo.proxy.sourcemngproxy.Control.SrcMngSwitchProxy;
import com.adayo.service.sourcemngservice.Control.SrcMngServiceMng;
import com.adayo.service.sourcemngservice.Module.AppConfigFile;
import com.adayo.service.sourcemngservice.Module.AppInfo;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;
import com.adayo.service.sourcemngservice.Utils.SrcMngOperateShareInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;

public abstract class SrcMngStartLastSource {
    private final static String TAG = SrcMngStartLastSource.class.getSimpleName();
    private static ExecutorService singleThreadPool = Executors.newSingleThreadExecutor();
    protected SourceInfo mDefaultSourceInfo = null;      //定义默认的LastSource

    protected abstract void setmDefaultSourceInfo();

    /**
     * 获取启动的源是否在数组中，若存在，则返回该SourceInfo，若不存在，则返回null
     * @param sourceType ：Last源类型
     * @return 若存在，则返回该SourceInfo，若不存在，则返回null
     */
    private SourceInfo getSourceInfo(final String sourceType)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getSourceInfo() begin sourceType = " + sourceType);

        SourceInfo sourceInfo = null;
        List<AppInfo> mLastSourceList = getLastSourceList();//从配置文件中获取LastSource列表

        for (int index = 0; index < mLastSourceList.size(); index++)
        {
            if (mLastSourceList.get(index) != null && mLastSourceList.get(index).getM_SourceType().equals(sourceType))
            {
                AppInfo inf = AppConfigFile.getInstance().getAppInfoBySourceType(sourceType);
                if (inf != null)
                {
                    Map<String, String> map = new HashMap<>();
                    map.put("SourceType", sourceType);
                    map.put("LastAudioSource", "Y");
                    final String action = "com.adayo.app.action." + sourceType;
                    sourceInfo = new SourceInfo(inf.getM_SourceType(), action, map, AppConfigType.SourceSwitch.APP_ON.getValue(), AppConfigType.SourceType.UI_AUDIO.getValue());
                    LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getSourceInfo() END index = " + index);
                    break;
                }
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getSourceInfo() END sourceInfo = " + sourceInfo);

        return sourceInfo;
    }

    /**
     * 获取启动的源是否在数组中，若存在，则返回该SourceInfo，若不存在，则返回null
     * @param uid ：Last UI类型
     * @return 若存在，则返回该SourceInfo，若不存在，则返回null
     */
    private SourceInfo getUIInfo(final String uid)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getUIInfo() begin uid = " + uid);

        SourceInfo uiInfo = null;
        List<AppInfo> mLastUIList = getLastUIList();//从配置文件中获取LastUISource列表

        for (int index = 0; index < mLastUIList.size(); index++)
        {
            if (mLastUIList.get(index) != null && mLastUIList.get(index).getM_SourceType().equals(uid))
            {
                AppInfo inf = AppConfigFile.getInstance().getAppInfoBySourceType(uid);
                if (inf != null)
                {
                    Map<String, String> map = new HashMap<>();
                    map.put("SourceType", uid);
                    map.put("LastUISource", "Y");
                    final String action = "com.adayo.app.action." + uid;
                    uiInfo = new SourceInfo(inf.getM_SourceType(), action, map, AppConfigType.SourceSwitch.APP_ON.getValue(), AppConfigType.SourceType.UI.getValue());
                    LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getUIInfo() END index = " + index);
                    break;
                }
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getUIInfo() END uiInfo = " + uiInfo);

        return uiInfo;
    }

    /**
     * 从配置文件中获取LastSource列表
     * @return ：lastSource列表
     */
    private List<AppInfo> getLastSourceList()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getLastSourceArray() begin");

        List<AppInfo> mLastSourceList = AppConfigFile.getInstance().getmLastSourceList();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getLastSourceArray() end");

        return mLastSourceList;
    }

    /**
     * 从配置文件中获取LastUI列表
     * @return : LastUI列表
     */
    private List<AppInfo> getLastUIList()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getLastUIList() begin");

        List<AppInfo> mLastUIList = AppConfigFile.getInstance().getmLastUIList();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getLastUIList() end");

        return mLastUIList;
    }

    /**
     * 启动LastSource
     * @param uiType ：UI源类型
     * @param sourceType ：音源类型
     */
    public void startLastSource(final String uiType, final String sourceType)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " startLastSource() begin uiType = " + uiType + " sourceType = " + sourceType);

        //启动Audio App
        startLastAudioApp(sourceType);

        //如果UI源和Audio源相同，则不进行拉起
        if (!sourceType.equals(uiType))
        {
            //启动UI App 如果Audio source 启动失败才会拉起UI源 防止UI覆盖audio记忆源
            SourceInfo info = getSourceInfo(sourceType);
            if (null == info && !AdayoSource.ADAYO_SOURCE_NULL.equals(sourceType)) {
                startLastUIApp(uiType);
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " startLastSource() end");
    }

    /**
     * 启动LastAudioSource
     * @param sourceType : Last Audio App
     */
    private void startLastAudioApp(@NonNull final String sourceType)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " startLastAudioApp() begin sourceType = " + sourceType);

        List<AppInfo> list = getLastSourceList();   //获取LastSource启动列表

        //如果list中没有lastsource则不进行启动
        if (list == null || list.size() == 0)
        {
            return;
        }

        //如果sourceType为null，则启动默认应用
        if (sourceType == null || sourceType.equals(AdayoSource.ADAYO_SOURCE_NULL))
        {
            startDefaultSource();
        }

        //获取SourceInfo信息
        final SourceInfo inf = getSourceInfo(sourceType);

        //如果是非Last列表内应用 或者 列表内应用无法启动
        if (null == inf || !canSetUp(sourceType)){
            //启动默认的应用 当记忆的源无法启动的时候 此处按照h6的交互调整 不切入默认源
           // startDefaultSource();
        }
        else //如果是列表内应用且能够启动则直接拉起
        {
            singleThreadPool.execute(new Runnable() {
                @Override
                public void run()
                {
                    final String sourceid = inf.getM_SourcePackageName();
                    final AppInfo appinf = AppConfigFile.getInstance().getAppInfoBySourceType(sourceid);
                    if (appinf == null)
                    {
                        return;
                    }

                    final String pn = appinf.getM_AppPackageName();
                    boolean ret = SrcMngServiceMng.getInstance().appInstalled(pn);
                    while (!ret)
                    {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ret = SrcMngServiceMng.getInstance().appInstalled(pn);
                    }
                    SrcMngSwitchProxy.getInstance().onRequest(inf);
                }
            });
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " startLastAudioApp() end");
    }

    /**
     * 启动LastUI App
     * @param UID : Last UI APP
     */
    private void startLastUIApp(@NonNull final String UID)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " startLastUIApp() begin UID = " + UID);

        if (UID == null || AdayoSource.ADAYO_SOURCE_NULL.equals(UID))
        {
            return;
        }

        List<AppInfo> uiList = getLastUIList();     //获取LastUISource启动列表

        //如果uiList中也没有LastUISource则不进行启动
        if (uiList == null || uiList.size() == 0)
        {
            return;
        }

        //获取SourceInfo信息
        final SourceInfo uinf = getUIInfo(UID);
        if (uinf != null)
        {
            singleThreadPool.execute(new Runnable() {
                @Override
                public void run() {
                    //确认项目是否有默认的Last应用，如果有，则启动默认的应用
                    final String sourceid = uinf.getM_SourcePackageName();
                    final AppInfo appInf = AppConfigFile.getInstance().getAppInfoBySourceType(sourceid);
                    if (appInf == null)
                    {
                        return;
                    }

                    final String pn = appInf.getM_AppPackageName();
                    boolean ret = SrcMngServiceMng.getInstance().appInstalled(pn);
                    while (!ret)
                    {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ret = SrcMngServiceMng.getInstance().appInstalled(pn);
                    }
                    SrcMngSwitchProxy.getInstance().onRequest(uinf);
                }
            });
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " startLastSource() end");
    }

    /**
     * 启动默认的App
     */
    private void startDefaultSource()
    {
        if (mDefaultSourceInfo != null) {
            singleThreadPool.execute(new Runnable() {
                @Override
                public void run()
                {
                    //确认项目是否有默认的Last应用，如果有，则启动默认的应用
                    final String sourceid = mDefaultSourceInfo.getM_SourcePackageName();
                    final AppInfo appInf = AppConfigFile.getInstance().getAppInfoBySourceType(sourceid);
                    if (appInf == null)
                    {
                        return;
                    }

                    final String pn = appInf.getM_AppPackageName();
                    boolean ret = SrcMngServiceMng.getInstance().appInstalled(pn);
                    while (!ret)
                    {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        ret = SrcMngServiceMng.getInstance().appInstalled(pn);
                    }
                    SrcMngSwitchProxy.getInstance().onRequest(mDefaultSourceInfo);
                }
            });
        }
    }

    /**
     * 判断Last源是否能够启动
     * @param sourceType：源名
     * @return ：true(能启动) / false(不能启动)
     */
    private boolean canSetUp(String sourceType)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " canSetUp() begin sourceType = " + sourceType);

        boolean ret = true;

        try
        {
            //如果Last源是USB音源
            if (AdayoSource.ADAYO_SOURCE_USB.equals(sourceType) ||
                AdayoSource.ADAYO_SOURCE_USB_VIDEO.equals(sourceType))
            {
                DevProxyManager devProxyManager = DevProxyManager.getInstance();
                //判断现在是否存在USB设备
                if (devProxyManager.isUSBAttached() > 0)
                {
                    LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " canSetUp() attached = " + devProxyManager.isUSBAttached());
                    ret = true;
                }
                else
                {
                    ret = false;
                }
            }
            else if (sourceType != null && sourceType.indexOf("ADAYO_SOURCE_ANDROID_AUTO") != -1)
            {
                //如果是Android Auto相关
                final String androidAutoKey = "androidauto_conn_state";
                final int androidAutoShareId = 71;
                SrcMngOperateShareInfo shareInfo = new SrcMngOperateShareInfo(androidAutoKey, androidAutoShareId);
                Object obj = shareInfo.getmValue();

                if (obj == null)
                {
                    ret = false;
                }
                else
                {
                    //获取Android Auto连接状态,未连接时，退出
                    ret = Boolean.parseBoolean(obj.toString());
                }
            }
            else if (AdayoSource.ADAYO_SOURCE_IPOD.equals(sourceType))
            {
                //IPOD模块相关
                final String ipodKey = "iPodDeviceConnectAction";
                final int ipodKeyShareID = 40;
                SrcMngOperateShareInfo shareInfo = new SrcMngOperateShareInfo(ipodKey, ipodKeyShareID);
                Object obj = shareInfo.getmValue();

                if (obj == null)
                {
                    ret = false;
                }
                else
                {
                    //获取IPOD连接状态,未连接时，退出
                    ret = Boolean.parseBoolean(obj.toString());
                }
            }
            else
            {
                //do nothing
            }
        }catch (Exception e)
        {
            ret = false;
            e.printStackTrace();
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " canSetUp() end ret = " + ret);

        return ret;
    }
}
