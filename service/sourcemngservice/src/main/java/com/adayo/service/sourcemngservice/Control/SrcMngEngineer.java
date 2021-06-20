package com.adayo.service.sourcemngservice.Control;

import com.adayo.adayosource.AdayoSource;
import com.adayo.proxy.share.ShareDataManager;
import com.adayo.proxy.share.interfaces.IShareDataListener;
import com.adayo.proxy.sourcemngproxy.Beans.SourceConstants;
import com.adayo.service.sourcemngservice.Control.Interface.ISrcMngEngineer;
import com.adayo.service.sourcemngservice.Module.AppInfo;
import com.adayo.service.sourcemngservice.Module.AppConfigFile;
import com.adayo.proxy.sourcemngproxy.Beans.AppConfigType;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by admin on 2018/4/8.
 */

public class SrcMngEngineer implements ISrcMngEngineer {
    private static final String TAG = SrcMngEngineer.class.getSimpleName();
    private volatile static SrcMngEngineer m_SrcMngEngineer = null;
    private AppConfigFile m_AppConfigFile;
    private final static int SHARE_INFO_NUM = 16;
    private volatile boolean mRvrStatus = false;
    private volatile boolean mBluePhoneCallOn = false; //记录蓝牙电话_接听状态 0---7 是挂断电话

    /**
     * 构造函数
     */
    private SrcMngEngineer()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " startRequestAppOn() begin");

        m_AppConfigFile = AppConfigFile.getInstance();
        try {
            final String s = ShareDataManager.getShareDataManager().getShareData(SHARE_INFO_NUM);
            updateRvcStatus(s); //更新倒车状态

            ShareDataManager.getShareDataManager().registerShareDataListener(SHARE_INFO_NUM, new IShareDataListener() {
                @Override
                public void notifyShareData(int i, String s) {
                    //异常判断
                    if (i != SHARE_INFO_NUM) {
                        return;
                    }

                    updateRvcStatus(s);
                }
            });
        }catch (NullPointerException e)
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " NullPointerException() occurred");
            e.printStackTrace();
        }
        catch (Exception e)
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " Exception() occurred");
            e.printStackTrace();
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " startRequestAppOn() end");
    }

    /**
     * 根据ShareInfo中倒车状态，保存倒车状态值
     * @param s
     */
    private void updateRvcStatus(final String s)
    {
        if (s == null)
        {
            return;
        }

        JSONObject obj = null;
        try
        {
            obj = new JSONObject(s);
            if (obj != null && obj.has("backCarState"))
            {
                boolean status = obj.getBoolean("backCarState");
                if (status != mRvrStatus)
                {
                    mRvrStatus = status;
                }
            }
        }catch (JSONException e)
        {
            e.printStackTrace();
            return;
        }
    }

    /**
     * 单例模式（双重同步锁，线程安全）
     * @return
     */
    public static SrcMngEngineer getInstance()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() begin");

        if (null == m_SrcMngEngineer)
        {
            synchronized (SrcMngEngineer.class)
            {
                if (null == m_SrcMngEngineer)
                {
                    m_SrcMngEngineer = new SrcMngEngineer();
                }
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() end");

        return m_SrcMngEngineer;
    }

    @Override
    public SrcMngRequestRltInfo requestChange(final AppInfo reqInfo) {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " requestChange() begin reqInfo = " + reqInfo);

        SrcMngRequestRltInfo requestRltInfo = executeResult(reqInfo);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " requestChange() end");

        return requestRltInfo;
    }


    /**
     * @param isPhoneCallOn 启动蓝牙后 设置蓝牙电话的接听状态.
     */
    @Override
    public void setBluePhoneCallOn(boolean isPhoneCallOn) {
        if (mBluePhoneCallOn != isPhoneCallOn)
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setBluePhoneCallOn() isPhoneCallOn= " + isPhoneCallOn);
            mBluePhoneCallOn = isPhoneCallOn;
        }
    }

    /**
     * 执行结果
     * @param reqInfo：请求应用
     * @return SrcMngRequestRltInfo 结果
     */
    private SrcMngRequestRltInfo executeResult(final AppInfo reqInfo)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " executeResult() begin");

        String curSrcID = SrcAppMng.getInstance().getM_curSrcID(); //获取当前的音源ID
        String curUID = SrcAppMng.getInstance().getM_curUID(); //获取当前UID

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " curSrcID = " + curSrcID);
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " curUID = " + curUID);

        SrcMngRequestRltInfo requestRltInfo = compareStategy(reqInfo, curSrcID, curUID);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " executeResult() end");

        return requestRltInfo;
    }

    //比较算法
    /**
     * 比较算法
     * @param reqInfo：请求源信息
     * @param curSrcID ： 当前的音频源
     * @param curUID：当前的UI源
     * @return：SrcMngRequestRltInfo 结果
     */
    private SrcMngRequestRltInfo compareStategy(final AppInfo reqInfo, final String curSrcID, final String curUID)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + "compareStategy() begin curSrcID = " + curSrcID + " curUID = " + curUID);

        SrcMngRequestRltInfo info = null;

        AppInfo curSrcInfo = m_AppConfigFile.getAppInfoBySourceType(curSrcID);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + "compareStategy()  mRvrStatus = " + mRvrStatus);

//        //当前为倒车状态时
//        if (mRvrStatus & !mBluePhoneCallOn)
//        {
//            info = new SrcMngRequestRltInfo(true, "", false);
//
//            return info;
//        }

        //车机刚刚启动时，当前UI源和当前音频源相同，并且为IDLE时
        if (curSrcID == AdayoSource.ADAYO_SOURCE_NULL && curUID == AdayoSource.ADAYO_SOURCE_NULL)
        {
            info = new SrcMngRequestRltInfo(true, "", false);
        }
        else
        {
            //如果为纯UI源时，可以直接进行切替
            if (reqInfo.getM_AppType() == AppConfigType.SourceType.UI)
            {
                info = new SrcMngRequestRltInfo(true, "", false);
            }
            //如果为UI & Audio源，则与当前后台源进行比较
            else
            {
                info = getCompareStategyResult(reqInfo, curSrcInfo);
            }
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " compareStategy() reqInfo sourceType = " + reqInfo.getM_SourceType());
            //#551501 蓝牙电话启动的同时,(申请音频焦点耗时) 可以切入其他源 导致蓝牙UI被挡住.
            if (mBluePhoneCallOn & info.ismReqRlt() & !AdayoSource.ADAYO_SOURCE_CLOCK.equals(reqInfo.getM_SourceType()))
            {
                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " compareStategy setReqRlt= false");
                info.setReqRlt(false);
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " compareStategy() end ret = " + info.ismReqRlt());

        return info;
    }

    /**
     * 根据优先级比较
     * @param reqAppInfo：请求源
     * @param curAppInfo：当前源
     * @return ： SrcMngRequestRltInfo 结果
     */
    private SrcMngRequestRltInfo getCompareStategyResult(final AppInfo reqAppInfo, final AppInfo curAppInfo)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getCompareStategyResult() begin");

        SrcMngRequestRltInfo info = null;

        boolean ret = false;
        final int DEFAULT_PRIORLEVEL = 90;
        int reqPriorLevel = -1;
        int curPriorLevel = -1;

        //如果请求源为第三方源
        if (reqAppInfo == null)
        {
            //请求源优先级设置默认值
            reqPriorLevel = DEFAULT_PRIORLEVEL;
        }
        else if (curAppInfo == null)
        {
            reqPriorLevel = reqAppInfo.getM_PriorLevel();

            //当前源优先级设置默认值
            curPriorLevel = DEFAULT_PRIORLEVEL;
        }
        else
        {
            reqPriorLevel = reqAppInfo.getM_PriorLevel();
            curPriorLevel = curAppInfo.getM_PriorLevel();
        }

        if (reqPriorLevel <= curPriorLevel)
        {
            info = new SrcMngRequestRltInfo(true, "", false);
        }
        else
        {
            //如果当前是电话应用中，则提示用户
            if (curAppInfo.getM_AppPackageName().equals(SourceConstants.SOURCE_BT_PHONE))
            {
                info = new SrcMngRequestRltInfo(false, "通话状态下无法使用该功能", true);
            }
            else
            {
                info = new SrcMngRequestRltInfo(false, "", false);
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getCompareStategyResult() end ret = " + info.ismReqRlt());

        return info;
    }
}
