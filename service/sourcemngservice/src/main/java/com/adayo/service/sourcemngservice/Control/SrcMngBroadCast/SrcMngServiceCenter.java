package com.adayo.service.sourcemngservice.Control.SrcMngBroadCast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.adayo.adayosource.AdayoSource;
import com.adayo.module.servicecenterproxy.Constant;
import com.adayo.module.servicecenterproxy.IFunction;
import com.adayo.module.servicecenterproxy.ServiceCenterManager;
import com.adayo.proxy.sourcemngproxy.Beans.AppConfigType;
import com.adayo.proxy.sourcemngproxy.Beans.SourceInfo;
import com.adayo.service.sourcemngservice.Control.SrcMngServiceMng;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SrcMngServiceCenter extends BroadcastReceiver {
    private final static String TAG = SrcMngServiceCenter.class.getSimpleName();
    private final static int FAILED = 0;
    private final static int SUCCESS = 1;

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onReceive() begin");

        final String actionName = intent.getAction();
        if (Constant.SERVICE_CENTER_BROCASTRECEIVER.equals(actionName))
        {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    addSourceMngAllFunction();
                }
            }).start();
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " onReceive() end");
    }

    /**
     * 添加源管理所有的功能
     */
    private void addSourceMngAllFunction()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " addSourceMngAllFunction() begin");

        List<IFunction> funcList = new ArrayList<>();

        funcList.add(addAppOnFunction());           //增加打开App的应用
        funcList.add(addAppOffFunction());          //增加关闭App的应用
        funcList.add(addGetCurrentUID());           //获取当前的UID
        funcList.add(addGetCurrentAudioId());       //获取当前的音频ID

        //注册服务中心
        ServiceCenterManager.getInstance().registerFunctionEntry(AdayoSource.ADAYO_SOURCE_MNG, funcList);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " addSourceMngAllFunction() end");
    }

    /**
     * 请求打开应用
     */
    private IFunction addAppOnFunction()
    {
        IFunction function = new IFunction() {
            @Override
            public int functionCall(String fromModule, String function, long l, Bundle bundle) {
                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " functionCall() fromModule = " + fromModule + " function = " + function);

                if (bundle == null)
                {
                    return FAILED;
                }

                final String sourceType = bundle.getString("SourceType");
                final int appType = bundle.getInt("UI_AUDIO");

                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " functionCall() sourceType = " + sourceType + " appType = " + appType);

                //异常判断
                if (appType > AppConfigType.SourceType.UI_AUDIO.getValue()
                        || appType <= AppConfigType.SourceType.IDLE.getValue()
                        || sourceType == null)
                {
                    return FAILED;
                }

                Map<String, String> map = new HashMap<>();
                Set<String> keys = bundle.keySet();
                for (String key : keys)
                {
                    if (!"UI_AUDIO".equals(key))
                    {
                        final String value = bundle.getString(key);
                        map.put(key, value);
                    }
                }

                SourceInfo info = new SourceInfo(sourceType, map,
                        AppConfigType.SourceSwitch.APP_ON.getValue(),
                        appType);

                SrcMngServiceMng.getInstance().onRequest(info);

                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " functionCall() end");

                return SUCCESS;
            }

            @Override
            public String getName() {
                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getName() APP_ON");

                return "APP_ON";
            }

            @Override
            public void syncRequsetGet(String s, String s1, long l, Bundle bundle, Bundle bundle1) {
                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " syncRequsetGet() begin");

                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " syncRequsetGet() end");
            }
        };

        return function;
    }

    /**
     * 请求关闭应用
     */
    private IFunction addAppOffFunction()
    {
        IFunction function = new IFunction() {
            @Override
            public int functionCall(String fromModule, String function, long l, Bundle bundle) {
                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " functionCall() fromModule = " + fromModule + " function = " + function);

                if (bundle == null)
                {
                    return FAILED;
                }

                final String sourceType = bundle.getString("SourceType");
                final int appType = bundle.getInt("UI_AUDIO");

                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " functionCall() sourceType = " + sourceType + " appType = " + appType);

                Map<String, String> map = new HashMap<>();
                Set<String> keys = bundle.keySet();
                for (String key : keys)
                {
                    if (!"UI_AUDIO".equals(key))
                    {
                        final String value = bundle.getString(key);
                        map.put(key, value);
                    }
                }

                SourceInfo info = new SourceInfo(sourceType, map,
                        AppConfigType.SourceSwitch.APP_OFF.getValue(),
                        appType);

                SrcMngServiceMng.getInstance().onRequest(info);

                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " functionCall() end");

                return SUCCESS;
            }

            @Override
            public String getName() {
                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getName() APP_OFF");

                return "APP_OFF";
            }

            @Override
            public void syncRequsetGet(String s, String s1, long l, Bundle bundle, Bundle bundle1) {
                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " syncRequsetGet() begin");

                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " syncRequsetGet() end");
            }
        };

        return function;
    }

    /**
     * 获取当前的UID
     * @return
     */
    private IFunction addGetCurrentUID()
    {
        IFunction function = new IFunction() {
            @Override
            public int functionCall(String s, String s1, long l, Bundle bundle) {
                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " functionCall()");

                return SUCCESS;
            }

            @Override
            public String getName() {
                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getName() GET_CURRENT_UID");

                return "GET_CURRENT_UID";
            }

            @Override
            public void syncRequsetGet(String s, String s1, long l, Bundle bundle, Bundle bundle1) {
                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " syncRequsetGet() Begin");

                if (bundle1 == null)
                {
                    return;
                }

                final String uid = SrcMngServiceMng.getInstance().getCurrentUID();
                bundle1.putString("UID", uid);

                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " syncRequsetGet() end uid = " + uid);
            }
        };

        return function;
    }

    /**
     * 获取当前的AUDIOID
     */
    private IFunction addGetCurrentAudioId()
    {
        IFunction function = new IFunction() {
            @Override
            public int functionCall(String s, String s1, long l, Bundle bundle) {
                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " functionCall()");

                return SUCCESS;
            }

            @Override
            public String getName() {
                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getName()");

                return "GET_CURRENT_AUDIOID";
            }

            @Override
            public void syncRequsetGet(String s, String s1, long l, Bundle bundle, Bundle bundle1) {
                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " syncRequsetGet() Begin");

                if (bundle1 == null)
                {
                    return;
                }

                final String audioId = SrcMngServiceMng.getInstance().getCurrentAudioFocus();
                bundle1.putString("AUDIOID", audioId);

                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " syncRequsetGet() end audioId = " + audioId);
            }
        };

        return function;
    }
}
