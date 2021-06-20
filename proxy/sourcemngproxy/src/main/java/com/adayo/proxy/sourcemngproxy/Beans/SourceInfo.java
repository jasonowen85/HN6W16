package com.adayo.proxy.sourcemngproxy.Beans;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;


import com.adayo.proxy.sourcemngproxy.Utils.LogUtils;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

import static com.adayo.proxy.sourcemngproxy.Utils.SourceMngLog.LOG_TAG;

/**
 * Created by admin on 2018/7/6.
 */

public class SourceInfo implements Parcelable {
    private static final String TAG = SourceInfo.class.getSimpleName();
    private String  m_SourcePackageName;    //APP包名
    private String  m_SourceActionName;     //APP Action名
    private Map     mSourceMap;             //Map
    private int     m_SourceSwitchType;     //APP ON/OFF
    private int     m_AudioType;            //APP是否含有音源(UI / UI_Audio)
    private Bundle  m_Options;              //动画Option

    /**
     *
     * @param packageName
     * @param switchType
     * @param audioType
     */
    public SourceInfo(String packageName, int switchType, int audioType){
        this(packageName, null, null, switchType, audioType, null);
    }

    /**
     * 构造函数
     * @param packageName:包名
     * @param map：参数的键值对的集合
     * @param switchType：APP_ON / APP_OFF
     * @param audioType:UI / UI_AUDIO
     */
    public SourceInfo(String packageName, Map<String, String> map, int switchType, int audioType)
    {
        this(packageName, null, map, switchType, audioType, null);
    }

    /**
     * 构造函数
     * @param packageName:包名
     * @param actionName:actionName
     * @param switchType：APP_ON / APP_OFF
     * @param audioType:UI / UI_AUDIO
     */
    public SourceInfo(String packageName, String actionName, int switchType, int audioType)
    {
        this(packageName, actionName, null, switchType, audioType, null);
    }

    /**
     * 构造函数
     * @param packageName
     * @param actionName
     * @param map
     * @param switchType
     * @param audioType
     */
    public SourceInfo(String packageName, String actionName, Map<String, String> map, int switchType, int audioType){
        this(packageName, actionName, map, switchType, audioType, null);
    }

    /**
     * 构造函数
     * @param packageName:包名 （不为null）
     * @param actionName:ACTION NAME
     * @param map : 参数Map
     * @param switchType ：APP_ON/APP_OFF
     * @param audioType:AUDIO_UI / UI
     * @param bundleOption:动画参数
     */
    public SourceInfo(@NonNull String packageName, String actionName, Map<String, String> map, int switchType, int audioType, Bundle bundleOption){
        LogUtils.dL(LOG_TAG, TAG + " SourceInfo() begin packageName = " + packageName + " map = " + map + " switchType = " + switchType + " audioType = " + audioType);

        m_SourcePackageName = packageName;
        m_SourceActionName = actionName;
        m_SourceSwitchType = switchType;
        m_AudioType = audioType;
        mSourceMap = map;
        m_Options = bundleOption;
    }

    //获取APP包名
    public String getM_SourcePackageName()
    {
        return m_SourcePackageName;
    }

    //获取APP Action名
    public String getM_SourceActionName()
    {
        return m_SourceActionName;
    }

    //获取Source Switch Type(APP ON/OFF)
    public int getM_SourceSwitchType()
    {
        return m_SourceSwitchType;
    }

    //获取Audio Type
    public int getM_AudioType()
    {
        return m_AudioType;
    }

    /**
     * 获取动画Option
     * @return
     */
    public Bundle getM_Options() {
        return m_Options;
    }

    //获取Map
    public Map<String, String> getMapFromBundle()
    {
        LogUtils.dL(LOG_TAG, TAG + " getMapFromBundle() begin mSourceMap = " + mSourceMap);

        return mSourceMap;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        LogUtils.dL(LOG_TAG, TAG + " writeToParcel() begin");

        //写入PackageName
        dest.writeString(m_SourcePackageName);

        //写入ActionName
        dest.writeString(m_SourceActionName);

        //写入Map
        dest.writeMap(mSourceMap);

        //写入SwitchType
        dest.writeInt(m_SourceSwitchType);

        //写入是否存在AudioType
        dest.writeInt(m_AudioType);

        //写入动画Option
        dest.writeParcelable(m_Options,flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<SourceInfo> CREATOR = new Creator<SourceInfo>(){
        @Override
        public SourceInfo createFromParcel(Parcel source) {

            if (source == null)
            {
                return null;
            }

            //获取AIDL中的APP包名
            String scrPackageName = source.readString();

            //获取AIDL中的APP Action名
            String scrActionName = source.readString();

            //获取Map
            HashMap<String, String> map = source.readHashMap(HashMap.class.getClassLoader());

            //获取SwitchType
            int scrSwitchType = source.readInt();

            //获取是否存在声音
            int audioType = source.readInt();

            if (map == null)
            {
                LogUtils.dL(LOG_TAG, TAG + " createFromParcel() readMap is null");
            }

            //获取动画Bundle
            Bundle bundle = source.readParcelable(Bundle.class.getClassLoader());
            if (bundle == null)
            {
                LogUtils.dL(LOG_TAG, TAG + " createFromParcel() bundle is null");
            }

            return new SourceInfo(scrPackageName, scrActionName, map, scrSwitchType, audioType, bundle);
        }

        @Override
        public SourceInfo[] newArray(int size) {
            return new SourceInfo[0];
        }
    };
}
