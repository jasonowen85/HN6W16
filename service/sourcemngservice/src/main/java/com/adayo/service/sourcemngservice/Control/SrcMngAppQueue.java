package com.adayo.service.sourcemngservice.Control;

import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Module.AppSwitchInfo;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;

import java.util.concurrent.PriorityBlockingQueue;

/**
 * Created by admin on 2018/4/6.
 */

public class SrcMngAppQueue {
    private static final String                                 TAG              = SrcMngAppQueue.class.getSimpleName();
    private volatile static       SrcMngAppQueue                         m_srcMngAppQueue = null;
    private              PriorityBlockingQueue<AppSwitchInfo>   m_questQueue     = null;
    private final  int                                          APP_QUEUE_MAX    = 10;  //queue max size

    /**
     * 构造函数
     */
    private SrcMngAppQueue()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngAppQueue() begin");

        this.m_questQueue = new PriorityBlockingQueue<>(APP_QUEUE_MAX, new SrcAppComparator());

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngAppQueue() end");
    }

    /**
     * 获取消息队列的实例
     * @return
     */
    public static SrcMngAppQueue getInstance()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() begin");

        if (m_srcMngAppQueue == null)
        {
            synchronized (SrcMngAppQueue.class)
            {
                if (m_srcMngAppQueue == null)
                {
                    m_srcMngAppQueue = new SrcMngAppQueue();
                }
            }
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getInstance() end");

        return m_srcMngAppQueue;
    }

    /**
     * 将请求的APP情报，添加到相应消息队列中
     * @param app
     */
    public synchronized void addElement(final AppSwitchInfo app)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " addElement() begin");

        this.m_questQueue.put(app);

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " addElement() end");
    }

    /**
     * 从消息队列中取出队头的消息
     * @return
     */
    public synchronized AppSwitchInfo pollElement()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " pollElement() begin");

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " pollElement() end");

        return this.m_questQueue.poll();
    }

    /**
     * 获取到消息队列中消息的个数
     * @return
     */
    public synchronized int size()
    {
        return this.m_questQueue.size();
    }
}
