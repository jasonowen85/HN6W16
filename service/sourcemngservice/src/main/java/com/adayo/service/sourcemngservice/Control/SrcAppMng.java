package com.adayo.service.sourcemngservice.Control;

import com.adayo.adayosource.AdayoSource;
import com.adayo.proxy.share.ShareDataManager;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.adayo.service.sourcemngservice.Utils.SrcMngOperateShareInfo;
import com.google.gson.Gson;

/**
 * Created by admin on 2018/4/8.
 */

public class SrcAppMng {
    private static final String TAG = SrcAppMng.class.getSimpleName();
    private static final int SRC_SHAREINFO_NUM = 14;    //ShareInfo中记录源信息的ID

    private List<String>    m_AudioList;                //音频焦点栈
    private List<String>    m_UIList;                   //前台UI栈
    private Map<String, String> m_Map;                  //Map

    private volatile static SrcAppMng m_SrcAppMng;

    private ExecutorService mSinglePools = Executors.newSingleThreadExecutor();

    /**
     * 构造函数
     */
    private SrcAppMng()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcAppMng() begin");

        m_AudioList = new ArrayList<>();
        m_UIList    = new ArrayList<>();
        m_Map       = new HashMap<>();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcAppMng() end");
    }

    /**
     * 获取到实例对象(线程安全，双重同步锁)
     * @return
     */
    public static SrcAppMng getInstance()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() begin");

        if (m_SrcAppMng == null)
        {
            synchronized (SrcAppMng.class)
            {
                if (m_SrcAppMng == null)
                {
                    m_SrcAppMng = new SrcAppMng();
                }
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() end");

        return m_SrcAppMng;
    }

    /**
     * 将当前的音频源加入到音频栈中
     * 并保存到ShareInfo中，通知各关注者
     * @param sourceType：源类型
     */
    public synchronized void setM_curSrcID(final String sourceType)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setM_curSrcID() begin sourceType = " + sourceType);

        //获取当前倒车状态
        final boolean backStatus = getCurBackStatus();
        final String curAudioId = getM_curSrcID();

        //插入到音频栈中
        int listIndex = findElementInList(sourceType, m_AudioList);
        if (listIndex > -1)
        {
            //栈中存在则先进行删除，在插入到队列尾部
            deleteElementFromListStack(listIndex, m_AudioList);
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setM_curSrcID() backStatus = " + backStatus);

        //#564946 btPhone-SVC-退出SVC 拔出U盘, 显示Radio 画面,蓝牙电话的声音.
        if (AdayoSource.ADAYO_SOURCE_BT_PHONE.equals(sourceType) ||
                AdayoSource.ADAYO_SOURCE_CALLCENTER.equals(sourceType))
        {
            insertList(m_AudioList, sourceType);

            SrcMngSharePreference m_SrcMngSp = SrcMngSharePreference.getInstance();
            m_SrcMngSp.setKeyValue("persist.CURRENT_SOURCEID", sourceType);

            //插入到ShareInfo中
            updateShareInfo(getM_curUID(), sourceType);
        }
        else if (AdayoSource.ADAYO_SOURCE_BT_PHONE.equals(curAudioId) ||
                AdayoSource.ADAYO_SOURCE_CALLCENTER.equals(curAudioId))  //如果当前是通话源
        {
            insertList(m_AudioList, sourceType, m_AudioList.size() - 1);
        }
        else if (backStatus)         //当前是倒车状态时
        {
            //因为AVM/RVC/phone/callcenter都可以存在倒车状态，所以即使当前是倒车状态时，也可以放在栈顶
            if (AdayoSource.ADAYO_SOURCE_RVC.equals(sourceType) ||
                    AdayoSource.ADAYO_SOURCE_AVM.equals(sourceType))
            {
                insertList(m_AudioList, sourceType);

                SrcMngSharePreference m_SrcMngSp = SrcMngSharePreference.getInstance();
                m_SrcMngSp.setKeyValue("persist.CURRENT_SOURCEID", sourceType);

                //插入到ShareInfo中
                updateShareInfo(getM_curUID(), sourceType);
            }
            else
            {
                insertList(m_AudioList, sourceType, m_AudioList.size() - 1);
            }
        }
        else
        {
            insertList(m_AudioList, sourceType);

            //保存到SharePreference中
            if (!AdayoSource.ADAYO_SOURCE_FAKESHUT.equals(sourceType))
            {
                SrcMngSharePreference m_SrcMngSp = SrcMngSharePreference.getInstance();
                m_SrcMngSp.setKeyValue("persist.CURRENT_SOURCEID", sourceType);
            }

            //插入到ShareInfo中
            updateShareInfo(getM_curUID(), sourceType);
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setM_curSrcID() end");
    }

    /**
     * 设定当前的UI源
     * @param sourceType：源类型
     */
    public synchronized void setM_curUID(final String sourceType)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setM_curUID() begin sourceType = " + sourceType);

        //插入到UI栈中
        int listIndex = findElementInList(sourceType, m_UIList);
        if (listIndex > -1)
        {
            //栈中存在则先进行删除，在插入到队列尾部
            deleteElementFromListStack(listIndex, m_UIList);
        }

        //获取当前倒车状态
        final boolean backStatus = getCurBackStatus();
        final String curUID = getM_curUID();
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setM_curUID() backStatus = " + backStatus + " curUID = " + curUID);
        //当前是倒车状态时
        if (backStatus)
        {
            if (AdayoSource.ADAYO_SOURCE_RVC.equals(sourceType) ||
                    AdayoSource.ADAYO_SOURCE_AVM.equals(sourceType))
            {
                insertList(m_UIList, sourceType);

                SrcMngSharePreference m_SrcMngSp = SrcMngSharePreference.getInstance();
                m_SrcMngSp.setKeyValue("persist.CURRENT_UID", sourceType);

                //插入到ShareInfo中
                updateShareInfo(sourceType, getM_curSrcID());
            }
            else
            {
                insertList(m_UIList, sourceType, m_UIList.size() - 1);
            }
        }
        else
        {
            if (!AdayoSource.ADAYO_SOURCE_RVC.equals(curUID) &&
                    !AdayoSource.ADAYO_SOURCE_AVM.equals(curUID))
            {
                insertList(m_UIList, sourceType);

                //保存到SharePreference中
                if (sourceType != null && !sourceType.isEmpty() && !AdayoSource.ADAYO_SOURCE_FAKESHUT.equals(sourceType)) {
                    SrcMngSharePreference m_SrcMngSp = SrcMngSharePreference.getInstance();
                    m_SrcMngSp.setKeyValue("persist.CURRENT_UID", sourceType);
                }

                //插入到ShareInfo中
                updateShareInfo(sourceType, getM_curSrcID());
            }
            else
            {
                insertList(m_UIList, sourceType, m_UIList.size() - 1);
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setM_curUID() end");
    }

    /**
     * 放弃音频焦点时，从音频栈内删除
     * @param sourceType：源类型
     */
    public synchronized void abandonAdayoFocus(String sourceType)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " abandonAdayoFocus() begin sourceType = " + sourceType);

        //音频栈中删除放弃的音源
        int listIndex = findElementInList(sourceType, m_AudioList);
        if (listIndex > -1)
        {
            //栈中存在则先进行删除
            deleteElementFromListStack(listIndex, m_AudioList);
        }
        if (AdayoSource.ADAYO_SOURCE_BT_PHONE.equals(sourceType)) //部分挂断电话收不到安富通知
            SrcMngEngineer.getInstance().setBluePhoneCallOn(false);

        //重新获取当前栈顶的Audio ID
        final String topSrcId = getM_curSrcID();

        //保存到SharePreference中
        if (topSrcId != null && !topSrcId.isEmpty() && !AdayoSource.ADAYO_SOURCE_FAKESHUT.equals(topSrcId))
        {
            SrcMngSharePreference m_SrcMngSp = SrcMngSharePreference.getInstance();
            m_SrcMngSp.setKeyValue("persist.CURRENT_SOURCEID", topSrcId);
        }

        //插入到ShareInfo中
        updateShareInfo(getM_curUID(), topSrcId);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " abandonAdayoFocus() end");
    }

    /**
     * 从UI栈内删除
     * @param sourceType:源类型
     */
    public synchronized void AppFinished(final String sourceType)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " AppFinished() begin sourceType = " + sourceType);

        //UI栈中删除放弃的音源
        int listIndex = findElementInList(sourceType, m_UIList);
        if (listIndex > -1)
        {
            //栈中存在则先进行删除
            deleteElementFromListStack(listIndex, m_UIList);
        }

        final String topUID = getM_curUID();

        //保存到SharePreference中
        if (topUID != null && !topUID.isEmpty() && !AdayoSource.ADAYO_SOURCE_FAKESHUT.equals(topUID))
        {
            SrcMngSharePreference m_SrcMngSp = SrcMngSharePreference.getInstance();
            m_SrcMngSp.setKeyValue("persist.CURRENT_UID", topUID);
        }

        //插入到ShareInfo中
        updateShareInfo(topUID, getM_curSrcID());

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " AppFinished() end");
    }

    /**
     * 获取当前音源
     * @return
     */
    public synchronized String getM_curSrcID()
    {
        return m_AudioList.size() > 0 ? m_AudioList.get(m_AudioList.size() - 1) : AdayoSource.ADAYO_SOURCE_NULL;
    }

    /**
     * 获取到当前UI源
     * @return
     */
    public synchronized String getM_curUID()
    {
        return m_UIList.size() > 0 ? m_UIList.get(m_UIList.size() - 1) : AdayoSource.ADAYO_SOURCE_NULL;
    }

    /**
     * 在音源栈中找到相同元素
     * @param sourceType：源类型
     * @param list：list
     * @return 位置
     */
    private int findElementInList(String sourceType, List<String> list)
    {
        for (int index = list.size() - 1; index >= 0; index--)
        {
            String target = list.get(index);
            if (target.equals(sourceType))
            {
                return index;
            }
        }

        return -1;
    }

    /**
     * 获取UIlist
     * @return
     */
    public List<String> getM_UIList()
    {
        return m_UIList;
    }

    /**
     * 获取AudioList
     * @return
     */
    public List<String> getM_AudioList() {
        return m_AudioList;
    }

    /**
     * 删除音源栈中的指定元素
     * @param index
     * @param list
     */
    private void deleteElementFromListStack(int index, List<String> list)
    {
        list.remove(index);
    }

    /**
     * 更新ShareInfo内容
     */
    private void updateShareInfo(final String uid, final String audioId)
    {
        m_Map.put("UID", uid);
        m_Map.put("AudioID", audioId);
        final ShareDataManager shareDataManager = ShareDataManager.getShareDataManager();
        Gson gson = new Gson();
        final String content = gson.toJson(m_Map);
        mSinglePools.execute(new Runnable() {
        @Override
            public void run() {
                shareDataManager.sendShareData(SRC_SHAREINFO_NUM, content);
            }
        });
    }

    /**
     * 插入list尾部
     * @param list
     * @param element
     */
    private void insertList(List<String> list, final String element)
    {
        insertList(list, element, -1);
    }

    /**
     * 插入list
     * @param list：list
     * @param element：元素
     * @param location：坐标位置
     */
    private void insertList(List<String> list, final String element, final int location)
    {
        if (list == null || element == null || element.isEmpty())
        {
            return;
        }

        if (location == -1)
        {
            list.add(element);
        }
        else
        {
            list.add(location, element);
        }
    }

    /**
     * 获取当前的倒车状态
     * @return : true(倒车) / false(非倒车)
     */
    private boolean getCurBackStatus()
    {
        final int shareId = 16;
        final String key = "backCarState";
        SrcMngOperateShareInfo shareInfo = new SrcMngOperateShareInfo(key, shareId);
        Object obj = shareInfo.getmValue();
        if (obj == null)
        {
            return false;
        }

        //获取当前的倒车状态
        boolean ret = Boolean.parseBoolean(obj.toString());

        return ret;
    }
}
