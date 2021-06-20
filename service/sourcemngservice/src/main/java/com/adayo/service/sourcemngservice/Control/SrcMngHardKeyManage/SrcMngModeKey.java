package com.adayo.service.sourcemngservice.Control.SrcMngHardKeyManage;


import com.adayo.adayosource.AdayoSource;
import com.adayo.commontools.MediaConstants;
import com.adayo.proxy.mediascanner.AdayoMediaScanner;
import com.adayo.proxy.share.ShareDataManager;
import com.adayo.proxy.sourcemngproxy.Beans.SourceInfo;
import com.adayo.service.sourcemngservice.Control.SrcAppMng;
import com.adayo.service.sourcemngservice.Control.SrcMngServiceMng;
import com.adayo.service.sourcemngservice.Control.SrcMngSystemManage.SrcMngSysMng;
import com.adayo.service.sourcemngservice.Module.AppConfigFile;

import com.adayo.service.sourcemngservice.Module.AppInfo;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;
import com.adayo.systemserviceproxy.SystemServiceConst;
import com.android.internal.util.CollectionUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

import static com.adayo.adayosource.AdayoSource.ADAYO_SOURCE_CARBIT;
import static com.adayo.adayosource.AdayoSource.ADAYO_SOURCE_RADIO;
import static com.adayo.adayosource.AdayoSource.ADAYO_SOURCE_RADIO_AM;
import static com.adayo.adayosource.AdayoSource.ADAYO_SOURCE_RADIO_FM;
import static com.adayo.proxy.keyevent.util.Constant.KEYEVENT_ACTION_LONGPRESS;
import static com.adayo.proxy.keyevent.util.Constant.KEYEVENT_ACTION_UP;
import static com.adayo.proxy.sourcemngproxy.Beans.AppConfigType.SourceSwitch.APP_ON;
import static com.adayo.systemserviceproxy.SystemServiceConst.ADAYO_SYSTEM_STATUS.SYS_STATUS_POWEROFF;

/**
 * Created by admin on 2018/4/18.
 */

public class SrcMngModeKey implements ISrcMngHardKeyImp {
    private static final String TAG  = SrcMngModeKey.class.getSimpleName();
    private List<AppInfo> m_List;
    private boolean longPressFlag = false;  //是否是长押
    private int currentRadioType; //记录 当前的radio type AM =1  或者 FM
    private long currentChangeSourceTime;

    //构造函数
    public SrcMngModeKey()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngModeKey() begin");

        //初始化
        init();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngModeKey() end");
    }

    /**
     * 初始化
     */
    private void init()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " init() begin");

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " run() begin");

                m_List = new ArrayList<>();

                List<AppInfo> m_ConfigList = AppConfigFile.getInstance().getAppConfigList();

                //将AppInfo[]中有Mode切替顺序的存放到list中
                readModeSeqFromAppConfigFile(m_ConfigList);

                //根據ModeSequence值调整list内App顺序
                switchListPackageSequence();

                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " run() end");
            }
        }, "SourceMng_ModeKeyThread");

        t.start();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " init() end");
    }

    //发送消息，启动某个APP
    @Override
    public void notifyMsg(@NonNull final String event) {
        SystemServiceConst.ADAYO_SYSTEM_STATUS status = SrcMngSysMng.getInstance().getM_adayoSystemStatus();
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() begin event = " + event+", status="+status);

        //如果是长押Mode时，标记flag，退出
        if (KEYEVENT_ACTION_LONGPRESS.equals(event))
        {
            longPressFlag = true;
            return;
        }

        //如果当前Mode长押结束，但是长押结束，则清空flag，退出
        if (KEYEVENT_ACTION_UP.equals(event) && longPressFlag)
        {
            longPressFlag = false;
            return;
        }

        SrcMngServiceMng srcMngService = SrcMngServiceMng.getInstance();
        SrcAppMng srcAppMng = SrcAppMng.getInstance();
        AppConfigFile appConfig = AppConfigFile.getInstance();

        //异常处理
        if (srcMngService == null || srcAppMng == null || appConfig == null || m_List == null || m_List.size() == 0)
        {
            return;
        }

        //找到当前APP应用
        final String curSrcID = srcAppMng.getM_curSrcID();
        final String curUID = srcAppMng.getM_curUID();
        // 当前项目Tod 没有申请clock audio
        if (AdayoSource.ADAYO_SOURCE_CLOCK.equals(curSrcID) || SYS_STATUS_POWEROFF == status) {
            return;
        }

        //如果UI源是Carbit并且手机互联处于连接中时，则不进行mode切源处理
        if (ADAYO_SOURCE_CARBIT.equals(curUID) && carbitIsConnected())
        {
            return;
        }

        //获取下一个要启动的APP应用
        String nextAudioId = findNextAppFromList(curSrcID, curUID);
        if (nextAudioId == null)
        {
            return;
        }
        if (Math.abs(System.currentTimeMillis() - currentChangeSourceTime) <= 500)
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + "not enough switch time return= ");
            return;
        }
        AppInfo inf = AppConfigFile.getInstance().getAppInfoBySourceType(nextAudioId);

        //启动下一个APP应用
        Map<String, String> map = new HashMap<>();
        map.put("SourceType", nextAudioId);
        String action = "com.adayo.app.action." + nextAudioId;
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() action = " + action);

        SourceInfo info = new SourceInfo(nextAudioId,
                action,
                map,
                APP_ON.getValue(),
                inf.getM_AppType().getValue());

        srcMngService.onRequest(info);
        currentChangeSourceTime = System.currentTimeMillis();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() end");
    }

    //将AppInfo[]中有Mode切替顺序的存放到list中
    private void readModeSeqFromAppConfigFile(List<AppInfo> list)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " readModeSeqFromAppConfigFile() begin size = " + list.size());
        for (int index = 0; index < list.size(); index++)
        {
            int value = list.get(index).getM_ModeSeqValue();
            if (value > 0)
            {
                m_List.add(list.get(index));
            }
        }
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " readModeSeqFromAppConfigFile() end size = " + m_List.size());
    }

    //查找下个App的SourceId
    private synchronized String findNextAppFromList(@NonNull final String curSrcId, @NonNull final String curUID)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " findNextAppFromList() begin curSrcId = " + curSrcId + " curUID = " + curUID);

        int index = 0;
        List<String> m_uiList = SrcAppMng.getInstance().getM_UIList();
        AppInfo curAppInfo = AppConfigFile.getInstance().getAppInfoBySourceType(curSrcId);
        AppInfo curAppUiInfo = AppConfigFile.getInstance().getAppInfoBySourceType(curUID);
//        if (curUID.contains(ADAYO_SOURCE_RADIO))
//            currentRadioType = ADAYO_SOURCE_RADIO_FM.equals(curUID) ? 0 : 1;
//        else if (ADAYO_SOURCE_RADIO.equals(curSrcId) & !CollectionUtils.isEmpty(m_uiList)){ //需要判断 是AM 还是FM
//            for (int i = m_uiList.size() - 1; i >= 0; i--) {
//                String sourceType = m_uiList.get(i);
//                if (null != sourceType & sourceType.contains(ADAYO_SOURCE_RADIO)) {
//                    curAppInfo = AppConfigFile.getInstance().getAppInfoBySourceType(sourceType);
//                    break;
//                }
//            }
//        }
        if (m_List.contains(curAppUiInfo))      //当前的UID处于Mode列表内
        {
            index = (m_List.indexOf(curAppUiInfo) + 1) % m_List.size();
        }
        else if (m_List.contains(curAppInfo))   //UID不在Mode列表内，AudioID在Mode列表内
        {
            index = (m_List.indexOf(curAppInfo)) % m_List.size();
        }else if (ADAYO_SOURCE_CARBIT.equals(curUID)) //获取到mAudioList栈顶的第二个 有声源的位置
        {
            index = getPreSourceIndex(index);
        }
        else                                    //UID不在Mode列表内，且AudioID也不在Mode列表内
        {
            index = 0;
        }

        //循环判断应用能否启动
        for (; index < m_List.size(); index++)
        {
            //判断是否应用能否启动
            if (canStartUp(index))
            {
                break;
            }
        }

        String nextSrcId = null;

        //如果没有能够启动的，则默认启动首个（首个一般情况下为Radio）
        if (index >= m_List.size())
        {
            nextSrcId = m_List.get(currentRadioType).getM_SourceType();
        }
        else
        {
            nextSrcId = m_List.get(index).getM_SourceType();
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " findNextAppFromList() begin nextSrcId = " + nextSrcId + "index =" + index);

        return nextSrcId;
    }

    private int getPreSourceIndex(int index) {
        SrcAppMng srcAppMng = SrcAppMng.getInstance();
        List<String> mAudioList = srcAppMng.getM_AudioList();
        if (!CollectionUtils.isEmpty(mAudioList)){
            int size = mAudioList.size();
            if (size > 1) {
                String preSourceType = mAudioList.get(size - 2);
                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " findNextAppFromList mAudioList = "+mAudioList.toString());
                // 565770 car bit 不释放音频焦点 重复操作与6个源交互
                if (preSourceType.contains(ADAYO_SOURCE_CARBIT) & size > 2) {
                    for (int i = size - 3; i >= 0; i--) {
                        String beanSourceName = mAudioList.get(i);
                        if (!beanSourceName.contains(ADAYO_SOURCE_CARBIT)) {
                            preSourceType = beanSourceName;
                            break;
                        }
                    }
                }
                //if (ADAYO_SOURCE_RADIO.equals(preSourceType))  return 2;
                AppInfo prevAppBean = AppConfigFile.getInstance().getAppInfoBySourceType(preSourceType);
                boolean isContain = m_List.contains(prevAppBean);
                index = isContain ? (m_List.indexOf(prevAppBean) + 1) % m_List.size() : 0;
                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " findNextAppFromList preSourceType = "+preSourceType +"  index = "  +index);
            }
        }
        return index;
    }

    /**
     * 判断应用能否启动
     * @return : true(能启动) / false(不能启动)
     */
    private boolean canStartUp(final int i)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " canStartUp() begin i = " + i);

        AppInfo appInf = m_List.get(i);

        //异常条件判断
        if (appInf == null)
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " canStartUp() appInf is null end");
            return false;
        }

        boolean ret = true;

        final String condition = appInf.getM_ModeSwitchCondition();
        if (condition == null)
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " canStartUp() condition is null end");
            return true;
        }
        else
        {
            int conditionValue = 0;
            try
            {
                conditionValue = Integer.parseInt(condition);
            }
            catch (NumberFormatException e)
            {
                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " canStartUp() NumberFormatException");
                e.printStackTrace();
            }
            catch (Exception e)
            {
                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " canStartUp() Exception");
                e.printStackTrace();
            }
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " canStartUp() conditionValue = " + conditionValue);

            //存在条件判断，则对具体的源ID进行判断
            if (conditionValue > 0)
            {
                ret = isSetUp(appInf.getM_SourceType());
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " canStartUp() end ret = " + ret);

        return ret;
    }

    /**
     * 源能启动
     * @param srcId:源ID
     * @return ：true（能启动） / false(不能启动)
     */
    private boolean isSetUp(@NonNull final String srcId)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " isSetUp() begin srcId = " + srcId);

        boolean ret = false;
        try
        {
            //如果是USB相关
            if (AdayoSource.ADAYO_SOURCE_USB.equals(srcId) ||
                    AdayoSource.ADAYO_SOURCE_USB_PHOTO.equals(srcId) ||
                    AdayoSource.ADAYO_SOURCE_USB_VIDEO.equals(srcId))
            {
                //先对USB连接状态进行判断，若USB状态为未连接，则连接USB
                if (!AdayoMediaScanner.getAdayoMediaScanner().isServiceConnected()) {
                    AdayoMediaScanner.getAdayoMediaScanner().connService();
                }

                //获取目前连接的端口号
                MediaConstants.STORAGE_PORT[] storage = AdayoMediaScanner.getAdayoMediaScanner().getMountedStorage();
                if (storage == null || storage.length == 0)
                {
                     ret = false;
                }
                else
                {
                    //能启动USB相关源
                    ret = isSetupUSB(srcId, storage);
                }
            }
            else if (AdayoSource.ADAYO_SOURCE_IPOD.equals(srcId))   //IPOD相关
            {
                //从ShareInfo从获取ipod连接字符串
                final int IPOD_SHAREINFO_ID = 40;
                final String IPOD_CONNECT = "iPodDeviceConnectAction";

                ret = readShareInfoStatus(IPOD_SHAREINFO_ID, IPOD_CONNECT);
            }
            else if (AdayoSource.ADAYO_SOURCE_BT_AUDIO.equals(srcId))
            {
                //从ShareInfo从获取BTAudio连接字符串
                final int BT_SHAREINFO_ID = 27;
                final String BT_CONNECT = "is_a2dp_connected";

                ret = readShareInfoStatus(BT_SHAREINFO_ID, BT_CONNECT);
            }
            else if (AdayoSource.ADAYO_SOURCE_NET_MUSIC.equals(srcId) || AdayoSource.ADAYO_SOURCE_ONLINE_MUSIC_1.equals(srcId) ||
                    AdayoSource.ADAYO_SOURCE_NET_RADIO.equals(srcId))
            {
                //获取网络连接状态
                boolean netConStatus = SrcMngServiceMng.getInstance().getNetConnectStatus();
                if (netConStatus)
                {
                    ret = netConStatus;
                }
            }
            else
            {
                ret = true;
            }
        }catch (Exception e)
        {
            e.printStackTrace();
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " isSetUp() Exception");
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " isSetUp() end ret = " + ret);

        return ret;
    }

    /**
     * 从ShareInfo中读取IPOD和BTAudio的状态
     * @param shareId
     * @param key
     * @return
     */
    private boolean readShareInfoStatus(final int shareId, @NonNull final String key)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " readShareInfoStatus() Begin shareId = " + shareId + " key = " + key);

        boolean ret = false;

        final String s = ShareDataManager.getShareDataManager().getShareData(shareId);
        if (s != null)
        {
            JSONObject jsonObj = null;
            try
            {
                jsonObj = new JSONObject(s);
                if (jsonObj != null && jsonObj.has(key))
                {
                    ret = jsonObj.getBoolean(key);
                }
                else
                {
                    ret = false;
                }
            }catch (JSONException e)
            {
                e.printStackTrace();
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " readShareInfoStatus() end ret = " + ret);

        return ret;
    }

    /**
     * US能否启动
     * @param usbType:ADAYO_SOURCE_USB_PHOTO(USB Photo) / ADAYO_SOURCE_USB_VIDEO(USB Video) /...
     * @return : false(不能启动USB) / true(能启动USB)
     */
    private boolean isSetupUSB(@NonNull final String usbType, MediaConstants.STORAGE_PORT[] storage)
    {
        boolean ret = false;

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " isSetupUSB() Begin usbType = " + usbType);

        for (int i = 0; i < storage.length; i++)
        {
            //通过端口获取挂载路径
            final String[] mountedPath = AdayoMediaScanner.getAdayoMediaScanner().getMountedPaths(storage[i]);
            for (int j = 0; j < mountedPath.length; j++)
            {
                String[] files;

                if (AdayoSource.ADAYO_SOURCE_USB_PHOTO.equals(usbType))
                {
                    files = AdayoMediaScanner.getAdayoMediaScanner().getAllImagePaths(mountedPath[j]);
                }
                else if (AdayoSource.ADAYO_SOURCE_USB_VIDEO.equals(usbType))
                {
                    files = AdayoMediaScanner.getAdayoMediaScanner().getAllVideoPaths(mountedPath[j]);
                }
                else
                {
                    files = AdayoMediaScanner.getAdayoMediaScanner().getAllAudioPaths(mountedPath[j]);
                }

                if (files != null && files.length > 0)
                {
                    ret = true;
                }
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " isSetupUSB() end ret = " + ret);

        return ret;
    }

    //按照配置文件中Mode按键的切换顺序，重新排列
    private void switchListPackageSequence()
    {
        for (int index = 0; index < m_List.size(); index++)
        {
            for (int secondIndex = index + 1; secondIndex < m_List.size(); secondIndex++)
            {
                if (m_List.get(index).getM_ModeSeqValue() > m_List.get(secondIndex).getM_ModeSeqValue())
                {
                    AppInfo appInfo = m_List.get(secondIndex);
                    m_List.set(secondIndex, m_List.get(index));
                    m_List.set(index, appInfo);
                }
            }
        }
    }

    /**
     * 判断当前Carbit是否处于连接状态
     * @return ：true (连接中) / false (未连接)
     */
    private boolean carbitIsConnected()
    {
        final int CARBIT_SHARE_INFO = 70;

        final String s = ShareDataManager.getShareDataManager().getShareData(CARBIT_SHARE_INFO);
        if (s == null)
        {
            return false;
        }

        boolean ret = false;
        JSONObject obj = null;
        try
        {
            obj = new JSONObject(s);
            if (obj != null && obj.has("carbit_conn_state"))
            {
                ret = obj.getBoolean("carbit_conn_state");
            }
        }catch (JSONException e)
        {
            e.printStackTrace();
        }

        return ret;
    }
}
