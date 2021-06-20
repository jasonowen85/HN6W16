package com.adayo.service.sourcemngservice.Module;


import com.adayo.proxy.sourcemngproxy.Beans.AppConfigType.SourceType;

import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;
import com.google.gson.JsonArray;
import com.google.gson.JsonIOException;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by admin on 2018/4/5.
 */

public class AppConfigFile {
    private static final String TAG = AppConfigFile.class.getSimpleName();
    private final String CONFIG_FILE_PATH = "/system/etc/adayo/SourceMng/AdayoSourceMng.conf";  //配置文件路径信息
    private List<AppInfo> mList;
    private List<AppInfo> mLastSourceList;      //音源列表
    private List<AppInfo> mLastUIList;          //UI列表

    private static AppConfigFile m_AppConfigFile = null;

    /**
     * 构造函数
     */
    private AppConfigFile()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " AppConfigFile() Begin");

        mList = new ArrayList<>();
        mLastSourceList = new ArrayList<>();
        mLastUIList = new ArrayList<>();

        //将配置文件信息读取到初始化列表中
        boolean ret = readJsonConfigFile();

        //读取失败
        if (!ret)
        {
            //讀取默認的App信息
            readDefaultApp();
        }
        else
        {

        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " AppConfigFile() end");
    }

    /**
     * 将配置文件信息读取到初始化列表中
     * @return TRUE(读取成功) / FALSE(读取失败)
     */
    private boolean readJsonConfigFile()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " readJsonConfigFile() Begin");

        boolean ret = false;

        JsonParser parser = new JsonParser();

        try
        {
            JsonObject object = (JsonObject)parser.parse(new FileReader(CONFIG_FILE_PATH));
            JsonArray array = object.get("AppInfo").getAsJsonArray();
            for (int index = 0; index < array.size(); index++)
            {
                JsonObject subObject = array.get(index).getAsJsonObject();

                int     sourceId        = subObject.get("SourceID").getAsInt();
                String  captionName     = subObject.get("CaptionName").getAsString();
                String  priorLevel      = subObject.get("PriorLevel").getAsString();
                String  storeProperty   = subObject.get("StoreProperty").getAsString();
                String  modeSequence    = subObject.get("ModeSequence").getAsString();
                String  appTypeString   = subObject.get("AppType").getAsString();
                int     appType         = 0;
                if (appTypeString.equals("UI"))
                {
                    appType = 1;
                }
                else if (appTypeString.equals("UI_AUDIO"))
                {
                    appType = 2;
                }
                else
                {
                    appType = 0;
                }
                String  packageName     = subObject.get("PackageName").getAsString();
                String  autoProperty    = subObject.get("AutoProperty").getAsString();
                int     audioChannel    = subObject.get("AudioChannel").getAsInt();
                String  appExist        = subObject.get("AppExist").getAsString();
                String  sourceType      = subObject.get("SourceType").getAsString();

                AppInfo info = new AppInfo(sourceId, captionName, priorLevel, storeProperty, modeSequence,
                        SourceType.getSourceType(appType), packageName, autoProperty, audioChannel, appExist, sourceType);

                mList.add(info);

                //如果是LastSource，则保存到LastList中
                if ("Store".equals(storeProperty))
                {
                    mLastSourceList.add(info);
                    // #592806 当audioId 是VR的时候 记忆源启动失败 注意源管理配置文件 只有Store源 无Store_UI源
                    mLastUIList.add(info);
                }
                else if ("Store_UI".equals(storeProperty))  //如果是LastUISource，则保存到LastUIList中
                {
                    mLastUIList.add(info);
                }
                else
                {

                }
            }

            ret = true;
        }catch (JsonIOException e)
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " JsonIOException");
            e.printStackTrace();
        }catch (JsonSyntaxException e)
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " JsonSyntaxException");
            e.printStackTrace();
        }catch (FileNotFoundException e)
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " FileNotFoundException");
            e.printStackTrace();
        }
        catch (Exception e)
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " Exception");
            e.printStackTrace();
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " readJsonConfigFile() end ret = " + ret);

        return ret;
    }

    /**
     * 读取静态值
     */
    private void readDefaultApp()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " readDefaultApp() Begin");

        AppInfo appInfo[] = {
                /*      APPId                   CaptionName     PriorLevel      StoreProperty   ModeSequence        APP Type            PackageName                 AutoProperty    AppExist*/
                new AppInfo(0, "Idle", "100", "UnStore", "0", SourceType.IDLE, "", "MANU" ,0, "DISENABLE", "SOURCE_NULL"),
                new AppInfo(2, "Music", "90", "Store", "2", SourceType.UI_AUDIO, "com.adayo.app.music", "AUTO" , 3, "ENABLE", "SOURCE_USB"),
                new AppInfo(5, "Radio", "90", "Store", "1", SourceType.UI_AUDIO, "com.adayo.radio", "AUTO" ,5, "ENABLE", "SOURCE_RADIO"),
                new AppInfo(6, "BtPhone", "70", "UnStore", "0", SourceType.UI_AUDIO, "com.nforetek.bt.phone", "AUTO" ,12, "ENABLE", "SOURCE_BT_PHONE"),
                new AppInfo(7, "BtAudio", "90", "UnStore", "0", SourceType.UI_AUDIO, "com.nforetek.bt.music", "AUTO" , 13, "ENABLE", "SOURCE_BT_AUDIO"),
                new AppInfo(8, "Camera", "50", "UnStore", "0", SourceType.UI_AUDIO, "com.adayo.camera", "AUTO" , 15, "ENABLE", "SOURCE_CAMERA"),
                new AppInfo(9, "Tpms", "60", "UnStore", "0", SourceType.UI_AUDIO, "com.adayo.app.tpms", "AUTO" , 19, "ENABLE", "SOURCE_TPMS"),
                new AppInfo(10, "BtSetting", "90", "UnStore", "0", SourceType.UI, "com.nforetek.bt.setting", "AUTO" , 0, "ENABLE", "SOURCE_BT_SETTING"),
                new AppInfo(11, "Setting", "90", "UnStore", "0", SourceType.UI, "com.adayo.settings", "AUTO" , 0, "ENABLE", "SOURCE_SETTING"),
                new AppInfo(12, "Home", "90", "UnStore", "0", SourceType.UI, "com.adayo.launcher", "AUTO" , 0, "ENABLE", "SOURCE_HOME"),
                new AppInfo(13, "Clock", "90", "UnStore", "0", SourceType.UI, "com.adayo.clock", "AUTO" , 0 ,"ENABLE", "SOURCE_CLOCK")
        };

        mList.addAll(Arrays.asList(appInfo));

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " readDefaultApp() end");
    }

    /**
     * 单例模式
     * @return
     */
    public static AppConfigFile getInstance()
    {
        if (m_AppConfigFile == null)
        {
            m_AppConfigFile = new AppConfigFile();
        }

        return m_AppConfigFile;
    }

    /**
     * 通过packageName寻找APP信息
     */
    public AppInfo getAppInfoByPackageName(String packageName)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getAppInfo Begin packageName = " + packageName);

        if (packageName == null)
        {
            return null;
        }

        for (int index = 0; index < mList.size(); index++)
        {
            if (mList.get(index).getM_AppPackageName().equals(packageName))
            {
                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getAppInfo index = " + index);
                return mList.get(index);
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getAppInfo end");

        return null;
    }

    /**
     * 通过sourceType寻找APP信息
     * @param sourceType：源类型
     * @return
     */
    public AppInfo getAppInfoBySourceType(String sourceType)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getAppInfo Begin sourceType = " + sourceType);
        for (int index = 0, size = mList.size(); index < size; index++)
        {
            if (mList.get(index).getM_SourceType().equals(sourceType))
            {
                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getAppInfo index = " + index);
                return mList.get(index);
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getAppInfo end");

        return null;
    }

    public List<AppInfo> getAppConfigList()
    {
        return mList;
    }

    /**
     * 获取Last Source列表
     * @return
     */
    public List<AppInfo> getmLastSourceList()
    {
        return mLastSourceList;
    }

    /**
     * 获取Last UI列表
     * @return
     */
    public List<AppInfo> getmLastUIList()
    {
        return mLastUIList;
    }
}