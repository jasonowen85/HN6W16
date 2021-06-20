package com.adayo.proxy.sourcemngproxy.Beans;

import android.content.Context;
import android.media.AudioManager.OnAudioFocusChangeListener;

import com.adayo.proxy.sourcemngproxy.Control.SrcMngSwitchProxy;
import com.adayo.proxy.sourcemngproxy.Interface.IAdayoFocusChange;
import com.adayo.proxy.sourcemngproxy.Utils.LogUtils;

import static android.media.AudioManager.AUDIOFOCUS_GAIN;
import static android.media.AudioManager.AUDIOFOCUS_LOSS;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
import static com.adayo.proxy.sourcemngproxy.Utils.SourceMngLog.LOG_TAG;


public class AdayoAudioFocus
{
    private static final String TAG = AdayoAudioFocus.class.getSimpleName();
    private String mSrcType;                            //源ID
    private IAdayoFocusChange mAdayoFocusChg;           //客户端回调
    private Context mContext;                           //上下文信息
    private int     mMuteFlag;                          //静音状态

    //AudioListener
    private OnAudioFocusChangeListener mAudioListener = new OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            LogUtils.dL(LOG_TAG, TAG + " onAudioFocusChange() begin focusChange = " + focusChange);

            switch (focusChange) {
                case AUDIOFOCUS_GAIN:
                    onGain();
                    break;

                case AUDIOFOCUS_LOSS:
                    onLoss();
                    break;

                case AUDIOFOCUS_LOSS_TRANSIENT:
                    onLossTransient();
                    break;

                case AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    onLossTransientCanDuck();
                    break;

                default:
                    break;
            }

            LogUtils.dL(LOG_TAG, TAG + " onAudioFocusChange() end");
        }
    };

    /**
     * 获得焦点后处理
     */
    private void onGain()
    {
        LogUtils.dL(LOG_TAG, TAG + " onGain begin ");

        if (mAdayoFocusChg != null)
        {
            LogUtils.dL(LOG_TAG, TAG + " onGain begin IAdayoFocusCode= "+mAdayoFocusChg.hashCode());

            mAdayoFocusChg.onGainBeforeSwitchChannel();

            //向服务端发送获取音频焦点通知
            SrcMngSwitchProxy.getInstance().notifyServiceAudioChange(mSrcType, AUDIOFOCUS_GAIN);

            mAdayoFocusChg.onGainAfterSwitchChannel();
        }

        LogUtils.dL(LOG_TAG, TAG + " onGain() end");
    }

    /**
     * 失去焦点后处理
     */
    private void onLoss()
    {
        LogUtils.dL(LOG_TAG, TAG + " onLoss() begin");

        if (mAdayoFocusChg != null)
        {
            mAdayoFocusChg.onLossBeforeSwitchChannel();

            //向服务端发送失去音频焦点通知
            SrcMngSwitchProxy.getInstance().notifyServiceAudioChange(mSrcType, AUDIOFOCUS_LOSS);

            mAdayoFocusChg.onLossAfterSwitchChannel();
        }

        LogUtils.dL(LOG_TAG, TAG + " onLoss() end");
    }

    /**
     * 一时失去焦点后处理
     */
    private void onLossTransient()
    {
        LogUtils.dL(LOG_TAG, TAG + " onLossTransient() begin");

        if (mAdayoFocusChg != null)
        {
            mAdayoFocusChg.onLossTransientBeforeSwitchChannel();

            //向服务端发送一时失去音频焦点通知
            SrcMngSwitchProxy.getInstance().notifyServiceAudioChange(mSrcType, AUDIOFOCUS_LOSS_TRANSIENT);

            mAdayoFocusChg.onLossTransientAfterSwitchChannel();
        }

        LogUtils.dL(LOG_TAG, TAG + " onLossTransient() end");
    }

    /**
     * 一时失去焦点 & 降低音量后处理
     */
    private void onLossTransientCanDuck()
    {
        LogUtils.dL(LOG_TAG, TAG + " onLossTransientCanDuck() begin");

        if (mAdayoFocusChg != null)
        {
            mAdayoFocusChg.onLossTransientCanDuckBeforeSwitchChannel();

            //向服务端发送一时失去音频焦点通知
            SrcMngSwitchProxy.getInstance().notifyServiceAudioChange(mSrcType, AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK);

            mAdayoFocusChg.onLossTransientCanDuckAfterSwitchChannel();
        }

        LogUtils.dL(LOG_TAG, TAG + " onLossTransientCanDuck() end");
    }


    /**
     * 构造函数
     * @param srcType
     * @param adayoFocusChange
     * @param context
     */
    public AdayoAudioFocus(String srcType, IAdayoFocusChange adayoFocusChange, Context context)
    {
        mSrcType = srcType;
        mAdayoFocusChg = adayoFocusChange;
        mContext = context;
        mMuteFlag = 0;      //默认解除静音
    }

    public String getmSrcType() {
        return mSrcType;
    }

    public IAdayoFocusChange getmAdayoFocusChg() {
        return mAdayoFocusChg;
    }

    public void setmAdayoFocusChg(IAdayoFocusChange adayoFocusChange)
    {
        mAdayoFocusChg = adayoFocusChange;
    }

    public Context getmContext() {
        return mContext;
    }

    /**
     * 获取AudioListener
     * @return
     */
    public OnAudioFocusChangeListener getAudioFocusChangerListener()
    {
        return mAudioListener;
    }

    public int getmMuteFlag() {
        return mMuteFlag;
    }

    public void setmMuteFlag(int mMuteFlag) {
        this.mMuteFlag = mMuteFlag;
    }
}
