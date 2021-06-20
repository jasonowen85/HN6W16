package com.adayo.service.sourcemngservice.Control.SrcMngAudioManage;

import com.adayo.adayosource.AdayoSource;
import com.adayo.mcucommproxy.McuCommCmd0x0200;
import com.adayo.mcucommproxy.McuCommManager;
import com.adayo.service.sourcemngservice.Control.SrcAppMng;
import com.adayo.service.sourcemngservice.Control.SrcMngChannelMng;
import com.adayo.service.sourcemngservice.Control.SrcMngSystemOperateMng;
import com.adayo.service.sourcemngservice.Module.AppConfigFile;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;
import com.adayo.struct.JavaStruct;
import com.adayo.struct.StructException;

/**
 * Created by ThinkPad on 2018/9/18.
 */
public class SrcAudioManager {
    public  static final String TAG = SrcAudioManager.class.getSimpleName();
    private volatile static SrcAudioManager mSrcAudioManager;

    /*
    单例模式（双重同步锁）
     */
    public static SrcAudioManager getInstance()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() begin");

        if(null == mSrcAudioManager)
        {
            synchronized (SrcAudioManager.class)
            {
                if (null == mSrcAudioManager)
                {
                    mSrcAudioManager = new SrcAudioManager();
                }
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() end");

        return mSrcAudioManager;
    }

    /**
     * 构造函数
     */
    private SrcAudioManager()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcAudioManager() Begin");
    }


    /**切替声道
     * @param sourceType : 源类型
     * @return true：切替声道成功 false：切替声道失败
     */
    public synchronized boolean switchAudioChannel(final String sourceType)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " switchAudioChannel Begin sourceType = " + sourceType);

        final int DEFAULT_CHANNEL = 3;

        if (sourceType == null || AdayoSource.ADAYO_SOURCE_NULL.equals(sourceType))
        {
            return false;
        }

        final int channelValue = AppConfigFile.getInstance().getAppInfoBySourceType(sourceType) == null ? DEFAULT_CHANNEL : AppConfigFile.getInstance().getAppInfoBySourceType(sourceType).getM_AudioChannel();

        //通过声道值进行切替
        switchAudioChannel(channelValue);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " switchAudioChannel end");

        return true;
    }

    /**切替声道
     * @param channelId : 声道值
     * @return true：切替声道成功 false：切替声道失败
     */
    public synchronized boolean switchAudioChannel(final int channelId)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " switchAudioChannel Begin channelId = " + channelId);

        if (channelId < 0)
        {
            return false;
        }

        //如果是相同channel则退出
        final int currentAudioChannel = SrcMngChannelMng.getInstance().getCurrentAudioChannel();
        final String currentAudioFocus = SrcAppMng.getInstance().getM_curSrcID();
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " switchAudioChannel currentAudioChannel = " + currentAudioChannel + " currentAudioFocus = " + currentAudioFocus);

        //如果当前是相同的声音通道，则不进行切替
        if (currentAudioChannel == channelId)
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " switchAudioChannel same channel");
            return true;
        }

        final int BT_PHONE_CHANNEL = 12;        //蓝牙电话声音通道
        final int CALL_CENTER_CHANNEL = 11;     //TBOX电话声音通道
        final int PHONE_WORK_MODE = 2;          //电话工作模式
        final int UN_PHONE_WORK_MODE = 4;       //非电话工作模式

        //如果当前是电话声音源并且计划切替声道不是蓝牙电话声道
        //如果当前是TBOX音源并且计划切替声道不是TBOX声道
        //则不进行切替
        if ((AdayoSource.ADAYO_SOURCE_BT_PHONE.equals(currentAudioFocus) && channelId != BT_PHONE_CHANNEL) ||
			(AdayoSource.ADAYO_SOURCE_CARPLAY_BT_PHONE.equals(currentAudioFocus) && channelId != BT_PHONE_CHANNEL) ||
			(AdayoSource.ADAYO_SOURCE_ANDROID_AUTO_BT_PHONE.equals(currentAudioFocus) && channelId != BT_PHONE_CHANNEL) ||
            (AdayoSource.ADAYO_SOURCE_CALLCENTER.equals(currentAudioFocus) && channelId != CALL_CENTER_CHANNEL))
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " switchAudioChannel currentAudioFocus = " + currentAudioFocus);
            return false;
        }

        try
        {
            //静音处理
            SrcMngSystemOperateMng.getInstance().muteChannel();

            Thread.sleep(150);

            //如果从电话workmode切走
            if (currentAudioChannel == BT_PHONE_CHANNEL || currentAudioChannel == CALL_CENTER_CHANNEL)
            {
                SrcMngChannelMng.getInstance().setWorkMode(UN_PHONE_WORK_MODE);
            }

            //切换到目标源的声道
            SrcMngChannelMng.getInstance().switchAudioChannel(channelId);

            //静音处理
            SrcMngSystemOperateMng.getInstance().muteChannel();

            //如果要切换到切换电话WorkMode
            if (channelId == BT_PHONE_CHANNEL || channelId == CALL_CENTER_CHANNEL)
            {
                SrcMngChannelMng.getInstance().setWorkMode(PHONE_WORK_MODE);
            }

            Thread.sleep(100);

        }catch (InterruptedException e)
        {
            e.printStackTrace();
        }catch (Exception e)
        {
            e.printStackTrace();
        }finally {
            //解除静音处理
            SrcMngSystemOperateMng.getInstance().unMuteChannel();
        }
        //通知mcu
        new Thread(new Runnable() {
            @Override
            public void run() {
                notifyMcu(channelId);
            }
        }).start();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " switchAudioChannel end");

        return true;
    }

    /**
     * 通知mcu
     * @param channeValue
     */
    private void notifyMcu(final int channeValue)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMcu() begin channeValue = " + channeValue);

        byte mcuCommand = 0x01; //MPU默认源
        final int MCU_RADIO = 5;

        if (channeValue == MCU_RADIO)
        {
            mcuCommand = 0X03;  //Radio源
        }

        /*通知MCU音频源发生变化*/
        McuCommCmd0x0200 mMcuCommCmd = new McuCommCmd0x0200(mcuCommand, (byte)0x00,(byte)0x00);
        try {
            byte[] buffs = JavaStruct.pack(mMcuCommCmd);
            McuCommManager.mcuCommSendcmd(buffs, (char) buffs.length);
        }catch (StructException e)
        {
            e.printStackTrace();
        }catch (Exception e)
        {
            e.printStackTrace();
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMcu() end");
    }
}