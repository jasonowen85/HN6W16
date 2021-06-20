package com.adayo.service.sourcemngservice.Control;

import android.content.Context;
import android.os.SystemProperties;

import com.adayo.adayosource.AdayoSource;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;

/**
 * Created by admin on 2018/4/17.
 */

public class SrcMngSharePreference {
    private static final String TAG                 = SrcMngSharePreference.class.getSimpleName();
    private static final String FILENAME            = "SourceMngStore";
    private static final String CURRENT_UID         = "persist.CURRENT_UID";
    private static final String CURRENT_SOURCEID    = "persist.CURRENT_SOURCEID";

    private             Context                         m_Context   = null;
    private volatile static      SrcMngSharePreference           m_SrcMngSp  = null;

    /**
     * 构造方法
     */
    private SrcMngSharePreference()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngSharePreference() Constructor");
    }

    /**
     * 获取单例
     * @return
     */
    public static SrcMngSharePreference getInstance()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() begin");

        if (m_SrcMngSp == null)
        {
            synchronized (SrcMngSharePreference.class)
            {
                if (m_SrcMngSp == null)
                {
                    m_SrcMngSp = new SrcMngSharePreference();
                }
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() end");

        return m_SrcMngSp;
    }

    /**
     * 设置上下文
     */
    public void setM_Context(Context context)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setM_Context() begin");

        m_Context = context;

        if (m_Context == null)
        {
            LogUtils.dL(TAG, " m_Context is null");
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setM_Context() end");
    }

    /**
     * 获取到相应key的Value值
     * @param key : 键值
     * @return 默认值（Radio） / 相应的返回值
     */
    public synchronized String getKeyValue(final String key)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getKeyValue() begin key = " + key);

        String value = AdayoSource.ADAYO_SOURCE_NULL;
        if (CURRENT_UID.equals(key) || CURRENT_SOURCEID.equals(key))
        {
            value = SystemProperties.get(key);
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getKeyValue() end value = " + value);

        return value;
    }

    /**
     * 设置键值
     * @param key ：键
     * @param value ： 值
     * @return 无需动作（true） / 提交成功（true） / 提交失败（false）
     */
    public synchronized boolean setKeyValue(final String key, final String value)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setKeyValue() begin key = " + key + " value = " + value);

        boolean ret = false;

        if (CURRENT_UID.equals(key) || CURRENT_SOURCEID.equals(key))
        {
            SystemProperties.set(key, value);
            ret = true;
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " setKeyValue() end ret = " + ret);

        return ret;
    }
}
