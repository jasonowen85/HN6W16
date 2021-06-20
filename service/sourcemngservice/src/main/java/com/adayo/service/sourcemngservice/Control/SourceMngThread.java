package com.adayo.service.sourcemngservice.Control;

import android.content.Context;

import com.adayo.service.sourcemngservice.Control.Interface.ISrcMngEngineer;
import com.adayo.service.sourcemngservice.Module.AppSwitchInfo;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;

/**
 * Created by admin on 2018/6/22.
 */

public class SourceMngThread implements Runnable {
    private static final String  TAG  = SourceMngThread.class.getSimpleName();
    private SrcMngServiceMng     m_SrcMngServiceMng     = null;
    private ISrcMngEngineer      m_SrcMngEngineer       = null;
    private SrcMngAppQueue       m_SrcMngAppQueue       = null;
    private SrcMngRequestRltInfo m_resultInfo           = null;
    private AppSwitchInfo        m_reqAppSwitchInfo     = null;
    private Context              mContext;

    /**
     * Msg线程构造函数
     */
    public SourceMngThread()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SourceMngThread() begin");

        m_SrcMngEngineer    = SrcMngEngineer.getInstance();
        m_SrcMngAppQueue    = SrcMngAppQueue.getInstance();
        m_SrcMngServiceMng  = SrcMngServiceMng.getInstance();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SourceMngThread() end");
    }

    /**
     * 初始化Context
     * @param context
     */
    public void init(Context context)
    {
        mContext = context;
    }

    /**
     * 线程运行函数
     */
    public void run()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " run() begin");

        while (true)
        {
            dealWithQueueMsg();
        }
    }

    /**
     * Idle状态下对消息队列中消息的处理
     */
    private void dealWithQueueMsg()
    {
        int queueSize = m_SrcMngAppQueue.size();

        //消息队列内没有请求切源的消息，线程休眠
        if (queueSize <= 0)
        {
            threadSleep(50);
        }
        else
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " dealWithQueueMsg() queueSize = " + queueSize);

            //reqAppSwitchInfo为NULL,或者reqAppSwitchInfo不为NULL，并且当前已经被处理完，再从消息队列中取出新的消息
            if ((m_reqAppSwitchInfo == null ||
                    (m_reqAppSwitchInfo != null) && m_reqAppSwitchInfo.getM_curMsgStatus() != AppSwitchInfo.MSG_STATUS.MSG_STATUS_RUNNING))
            {
                //从消息队列中取出请求的Msg
                m_reqAppSwitchInfo = m_SrcMngAppQueue.pollElement();
                if (m_reqAppSwitchInfo != null)
                {
                    //将请求APP对象的状态设置为IDLE状态
                    m_reqAppSwitchInfo.setM_curMsgStatus(AppSwitchInfo.MSG_STATUS.MSG_STATUS_IDLE);

                    //对消息队列中消息的竞合判断
                    runningStateDealWithQueueMsg();

                    //对消息进行处理
                    doneStateDealWithQueueMsg();
                }
                else
                {
                    threadSleep(50);
                }
            }
            else
            {
                threadSleep(50);   //休眠100ms
            }
        }
    }

    /**
     * 对消息队列中消息的竞合判断
     */
    private void runningStateDealWithQueueMsg()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " runningStateDealWithQueueMsg() begin");

        //对请求源与当前源进行竞合判断，判断是否可以进行执行相应请求
        m_resultInfo = m_reqAppSwitchInfo == null ? null : m_SrcMngEngineer.requestChange(m_reqAppSwitchInfo.getM_AppInfo());

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " runningStateDealWithQueueMsg() end");
    }

    /**
     * 对消息进行处理
     */
    private void doneStateDealWithQueueMsg()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " doneStateDealWithQueueMsg() begin");

        //如果竞合结果为true
        if (m_resultInfo.ismReqRlt() == true)
        {
            //将请求APP对象的状态设置为Running
            m_reqAppSwitchInfo.setM_curMsgStatus(AppSwitchInfo.MSG_STATUS.MSG_STATUS_RUNNING);

            //将请求源信息传入到切替函数中，进行相应操作
            try {
                m_SrcMngServiceMng.switchSource(m_reqAppSwitchInfo);
                String blueCallOn = "com.adayo.bluetooth.action.BT_PHONE";
                if (blueCallOn.equals(m_reqAppSwitchInfo.getM_actionName()))
                    m_SrcMngEngineer.setBluePhoneCallOn(true);
            }
            catch (Exception e)
            {
                LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " doneStateDealWithQueueMsg() RemoteException");
                e.printStackTrace();
            }

            //设定该APP请求的请求状态
            m_reqAppSwitchInfo.setM_curMsgStatus(AppSwitchInfo.MSG_STATUS.MSG_STATUS_DONE);
        }
        else
        {
            //m_resultInfo.showDiagInfo();
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " doneStateDealWithQueueMsg() end");
    }

    /**
     * 休眠处理函数
     * @param mills
     */
    private void threadSleep(long mills)
    {
        try
        {
            Thread.sleep(mills);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}
