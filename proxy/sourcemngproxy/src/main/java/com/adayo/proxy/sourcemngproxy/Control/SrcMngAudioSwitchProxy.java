package com.adayo.proxy.sourcemngproxy.Control;

import android.app.Service;
import android.content.Context;
import android.media.AudioManager;


import com.adayo.proxy.sourcemngproxy.Beans.AdayoAudioFocus;
import com.adayo.proxy.sourcemngproxy.Interface.IAdayoFocusChange;
import com.adayo.proxy.sourcemngproxy.Utils.LogUtils;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;

import static android.media.AudioManager.AUDIOFOCUS_REQUEST_DELAYED;
import static android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
import static com.adayo.adayosource.AdayoSource.ADAYO_SOURCE_BT_AUDIO;
import static com.adayo.adayosource.AdayoSource.ADAYO_SOURCE_BT_PHONE;
import static com.adayo.adayosource.AdayoSource.ADAYO_SOURCE_CALLCENTER;
import static com.adayo.adayosource.AdayoSource.ADAYO_SOURCE_CARPLAY_PHONE;
import static com.adayo.adayosource.AdayoSource.ADAYO_SOURCE_CLOCK;
import static com.adayo.adayosource.AdayoSource.ADAYO_SOURCE_RADIO;
import static com.adayo.adayosource.AdayoSource.ADAYO_SOURCE_RADIO_AM;
import static com.adayo.adayosource.AdayoSource.ADAYO_SOURCE_RADIO_FM;
import static com.adayo.proxy.sourcemngproxy.Beans.AppConfigType.MuteStatus.MUTE_OFF;
import static com.adayo.proxy.sourcemngproxy.Utils.SourceMngLog.LOG_TAG;

/**
 * Created by admin on 2018/4/10.
 */
public class SrcMngAudioSwitchProxy {
    private static final String TAG = SrcMngAudioSwitchProxy.class.getSimpleName();
    private volatile static SrcMngAudioSwitchProxy m_SrcMngAudioSwitchProxy = null;
    private SrcMngSwitchProxy m_SrcMngSwitchProxy = null;

    private String  m_SourceType = null;                        //当前源类型
    private IAdayoFocusChange m_AdayoFocusChange = null;
    private Context m_Context = null;                           //上下文信息
    private Map<String, AdayoAudioFocus> map = new HashMap<>();

    /**
     *
     */
    private SrcMngAudioSwitchProxy()
    {
        LogUtils.dL(LOG_TAG, TAG + " SrcMngAudioSwitchProxy() begin");

        this.m_SrcMngSwitchProxy = SrcMngSwitchProxy.getInstance();

        LogUtils.dL(LOG_TAG, TAG + " SrcMngAudioSwitchProxy() end");
    }

    /**
     * 单例模式(线程安全，双重同步锁)
     * @return:对象实例
     */
    public static SrcMngAudioSwitchProxy getInstance()
    {
        LogUtils.dL(LOG_TAG, TAG + " getInstance() begin");

        if (m_SrcMngAudioSwitchProxy == null)
        {
            synchronized (SrcMngAudioSwitchProxy.class)
            {
                if (m_SrcMngAudioSwitchProxy == null)
                {
                    m_SrcMngAudioSwitchProxy = new SrcMngAudioSwitchProxy();
                }
            }
        }

        LogUtils.dL(LOG_TAG, TAG + " getInstance() end");

        return m_SrcMngAudioSwitchProxy;
    }

    /**
     * 设置AudioSwitchInfo
     * @param sourceType:源类型
     * @param adayoFocusChange：接口
     * @param context：上下文
     */
    public void setAudioSwitchInfo(final String sourceType, final IAdayoFocusChange adayoFocusChange, final Context context)
    {
        LogUtils.dL(LOG_TAG, TAG + " setAudioSwitchInfo() begin sourceType = " + sourceType);

        //设置源
        this.m_SourceType = sourceType;

        //设置接口类
        this.m_AdayoFocusChange = adayoFocusChange;

        //设置上下文信息
        this.m_Context = context;

        LogUtils.dL(LOG_TAG, TAG + " setAudioSwitchInfo() end");
    }

    /**
     * 利用封装接口向AudioManager请求AudioFocus
     * @param ：streamType：音量和远程播放控制
     * @param ：dorationHint：抢占方式
     * @return ：true(申请成功) / false(申请失败)
     */
    public synchronized boolean requestAdayoAudioFocus(final int streamType, final int dorationHint)
    {
        LogUtils.dL(LOG_TAG, TAG + " requestAdayoAudioFocus() begin streamType = " + streamType + " dorationHint = " + dorationHint);

        //非法请求，返回请求失败
        if (dorationHint < 0 || m_Context == null)
        {
            return false;
        }

        boolean ret = requestAdayoAudioFocus(streamType, dorationHint, m_SourceType, m_AdayoFocusChange, m_Context);

        LogUtils.dL(LOG_TAG, TAG + " requestAdayoAudioFocus() end ret = " + ret);

        return ret;
    }

    /**
     * 利用封装接口向AudioManager请求AudioFocus
     * @param streamType：音量和远程播放控制
     * @param dorationHint：抢占方式
     * @param sourceType:源类型
     * @param adayoFocusChange：接口
     * @param context：上下文
     * @return true(申请成功) / false(申请失败)
     */
    public synchronized boolean requestAdayoAudioFocus(final int streamType, final int dorationHint, final String sourceType, final IAdayoFocusChange adayoFocusChange, final Context context)
    {
        LogUtils.dL(LOG_TAG, TAG + " requestAdayoAudioFocus() begin streamType = " + streamType + " dorationHint = " + dorationHint + " sourceType = " + sourceType);

        boolean ret = requestAdayoAudioFocus(streamType, dorationHint, sourceType, adayoFocusChange, context, -2);

        LogUtils.dL(LOG_TAG, TAG + " requestAdayoAudioFocus() end ret = " + ret);

        return ret;
    }

    /**
     * 利用封装接口向AudioManager请求AudioFocus
     * @param streamType：音量和远程播放控制
     * @param dorationHint：抢占方式
     * @param sourceType:源类型
     * @param adayoFocusChange：接口
     * @param context：上下文
     * @param muteStatus：静音状态（1：静音，0：解除静音，-2：维持状态）
     * @return true(申请成功) / false(申请失败)
     */
    public synchronized boolean requestAdayoAudioFocus(final int streamType, final int dorationHint, final String sourceType, final IAdayoFocusChange adayoFocusChange, final Context context, final int muteStatus)
    {
        LogUtils.dL(LOG_TAG, TAG + " requestAdayoAudioFocus() begin streamType = " + streamType + " dorationHint = " + dorationHint + " sourceType = " + sourceType + " muteStatus = " + muteStatus);

        //非法请求，返回请求失败
        if (dorationHint < 0 || sourceType == null || adayoFocusChange == null)
        {
            return false;
        }

        //能否申请AudioFocus成功
        boolean canRlt = SrcMngSwitchProxy.getInstance().canRequestAudioFocus(sourceType);
        if (!canRlt)
        {
            LogUtils.dL(LOG_TAG, TAG + " requestAdayoAudioFocus() canRequestAudioFocus() Failed");
            return false;
        }

        AdayoAudioFocus focus = map.get(sourceType);
        if (focus == null)
        {
            LogUtils.dL(LOG_TAG, TAG + " requestAdayoAudioFocus() focus is null = "+adayoFocusChange.hashCode());
            focus = new AdayoAudioFocus(sourceType, adayoFocusChange, context);
            focus.setmMuteFlag(muteStatus);
        }
        else
        {
            LogUtils.dL(LOG_TAG, TAG + " requestAdayoAudioFocus setmAdayoFocusChg= "+adayoFocusChange.hashCode());
            IAdayoFocusChange afc = focus.getmAdayoFocusChg();
            afc = null;

            focus.setmAdayoFocusChg(adayoFocusChange);
            focus.setmMuteFlag(muteStatus);
        }

        int chgStreamType = onChangeStreamType(sourceType, streamType);
        int requestRet = requestAndroidSystemAudioFocus(focus, chgStreamType, dorationHint);
        boolean ret = (requestRet == AUDIOFOCUS_REQUEST_GRANTED ? true : false);
        int audioChannel = SrcMngSwitchProxy.getInstance().getAudioChannelBySourceId(sourceType);

        //申请成功，存放到map中，通知源变换，切换声音通道
        if (AUDIOFOCUS_REQUEST_GRANTED == requestRet)
        {
            //将源信息保存至源管理中
            SrcMngSwitchProxy.getInstance().notifyRequestAudioFocus(sourceType);
            map.put(sourceType, focus);
            if (audioChannel >= 0)  //判断是否需要切换声音通道
            {
                SrcMngSwitchProxy.getInstance().onSwitchChannel(audioChannel);
            }

            //非Clock源申请焦点，则进行解除Mute
            if (!ADAYO_SOURCE_CLOCK.equals(sourceType))
            {
                SrcMngSwitchProxy.getInstance().setSystemMuteStatus(streamType, muteStatus);
            }
        }
        else if (AUDIOFOCUS_REQUEST_DELAYED == requestRet)
        {
            //申请延迟，则只放在map中
            map.put(sourceType, focus);
        }
        else
        {
            //AUDIOFOCUS_REQUEST_FAILED,do nothing
        }

        LogUtils.dL(LOG_TAG, TAG + " requestAdayoAudioFocus() end ret = " + ret);

        return ret;
    }

    /**
     * 申请Android系统AudioFocus
     * @param focus : focus类对象，保存了context，OnAudioFocusChangeListener信息
     * @param streamType ： 流类型
     * @param dorationHint ： 强占方式
     * @return : AUDIOFOCUS_REQUEST_FAILED(0) ： 申请失败
     *           AUDIOFOCUS_REQUEST_GRANTED(1)： 申请成功
     */
    private int requestAndroidSystemAudioFocus(@NonNull final AdayoAudioFocus focus, final int streamType, final int dorationHint)
    {
        LogUtils.dL(LOG_TAG, TAG + " requestAndroidSystemAudioFocus() begin");

        Context context = focus.getmContext();
        AudioManager.OnAudioFocusChangeListener listener = focus.getAudioFocusChangerListener();
        AudioManager am = (AudioManager)context.getSystemService(Service.AUDIO_SERVICE);
        int requestRlt = am.requestAudioFocus(listener, streamType, dorationHint);

        LogUtils.dL(LOG_TAG, TAG + " requestAndroidSystemAudioFocus() end requestRlt = " + requestRlt);

        return requestRlt;
    }

    /**
     * 利用封装接口向AudioManager请求放弃AudioFocus
     * @return true(申请成功) / false(申请失败)
     */
    public synchronized boolean abandonAdayoAudioFocus()
    {
        LogUtils.dL(LOG_TAG, TAG + " abandonAdayoAudioFocus() begin");

        //请求放弃AudioFocus
        boolean ret = abandonAdayoAudioFocus(m_AdayoFocusChange);

        LogUtils.dL(LOG_TAG, TAG + " abandonAdayoAudioFocus() end ret = " + ret);

        return ret;
    }

    /**
     * 放弃音频焦点
     * @param afc ： 回调接口
     * @return ： true(申请成功) / false(申请失败)
     */
    public synchronized boolean abandonAdayoAudioFocus(final IAdayoFocusChange afc)
    {
        LogUtils.dL(LOG_TAG, TAG + " abandonAdayoAudioFocus() begin");

        AdayoAudioFocus aaf = getAudioFocusFromMap(afc);
        if (aaf == null)
        {
            return false;
        }


        final String sourceType = aaf.getmSrcType();
        SrcMngSwitchProxy.getInstance().notifyAbandonAudioFocus(sourceType);
        int abandonRlt = abandonAndroidAudioFocus(aaf);

        boolean ret = (abandonRlt == AUDIOFOCUS_REQUEST_GRANTED ? true : false);

        //放弃成功，则从map中删除
        if (ret)
        {
            LogUtils.dL(LOG_TAG, TAG + " abandonAdayoAudioFocus() sourceType = " + sourceType);

            map.remove(sourceType);
        }

        LogUtils.dL(LOG_TAG, TAG + " abandonAdayoAudioFocus() end ret = " + ret);

        return ret;
    }

    /**
     * 遍历Map获取AdayoAudioFocus
     * @param afc ：客户端回调接口
     * @return ：设置的AdayoAudioFocus
     */
    private AdayoAudioFocus getAudioFocusFromMap(@NonNull final IAdayoFocusChange afc)
    {
        if (afc == null)
        {
            return null;
        }

        for (String key : map.keySet())
        {
            AdayoAudioFocus aaf = map.get(key);
            if (aaf != null && aaf.getmAdayoFocusChg() == afc)
            {
                return aaf;
            }
        }

        return null;
    }

    /**
     * 申请放弃Android系统音频焦点
     * @param focus ： 回调接口
     * @return : AUDIOFOCUS_REQUEST_FAILED(0) ： 申请失败
     *           AUDIOFOCUS_REQUEST_GRANTED(1)： 申请成功
     */
    private synchronized int abandonAndroidAudioFocus(@NonNull final AdayoAudioFocus focus)
    {
        LogUtils.dL(LOG_TAG, TAG + " abandonAndroidAudioFocus() begin");
        Context context = focus.getmContext();
        AudioManager.OnAudioFocusChangeListener listener = focus.getAudioFocusChangerListener();
        AudioManager am = (AudioManager)context.getSystemService(Service.AUDIO_SERVICE);
        int requestRlt = am.abandonAudioFocus(listener);
        LogUtils.dL(LOG_TAG, TAG + " abandonAndroidAudioFocus() end requestRlt = " + requestRlt);
        return requestRlt;
    }

    /**
     * streamType转换
     * @param sourceId ： 源ID
     * @param streamTypeValue : streamTypeValue
     * @return
     */
    private int onChangeStreamType(final String sourceId, final int streamTypeValue)
    {
        LogUtils.dL(LOG_TAG, TAG + " onChangeStreamType() begin sourceId = " + sourceId + " streamTypeValue = " + streamTypeValue);
        int retStreamType = 3;      //MUSIC声道
        if (streamTypeValue < 0)
        {
            return retStreamType;
        }

        if (ADAYO_SOURCE_BT_PHONE.equals(sourceId) || ADAYO_SOURCE_CARPLAY_PHONE.equals(sourceId))
        {
            retStreamType = 6; //STREAM_BLUETOOTH_SCO
        }
        else if (ADAYO_SOURCE_RADIO.equals(sourceId) ||
                ADAYO_SOURCE_RADIO_FM.equals(sourceId) ||
                ADAYO_SOURCE_RADIO_AM.equals(sourceId))
        {
            retStreamType = 16; //STREAM_FM
        }
        else if (ADAYO_SOURCE_BT_AUDIO.equals(sourceId))
        {
            retStreamType = 15; //STREAM_MUSIC_BT
        }
        else if (ADAYO_SOURCE_CALLCENTER.equals(sourceId))
        {
            retStreamType = 0; //STREAM_VOICE_CALL
        }
        else
        {
            retStreamType = streamTypeValue;
        }

        LogUtils.dL(LOG_TAG, TAG + " onChangeStreamType() end retStreamType = " + retStreamType);

        return retStreamType;
    }
}
