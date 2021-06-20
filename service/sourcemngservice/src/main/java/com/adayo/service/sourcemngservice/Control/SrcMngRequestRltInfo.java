package com.adayo.service.sourcemngservice.Control;

import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;

public class SrcMngRequestRltInfo {
    private final static String TAG = SrcMngRequestRltInfo.class.getSimpleName();
    private boolean mReqRlt;            //请求结果（true / false）
    private String  mReqRltInfo;        //请求结果为false时，显示内容
    private boolean mIsDiagShow;        //请求结果为false时，是否要弹出提示框

    /**
     * 构造函数
     * @param result
     * @param reqRltInfo
     */
    public SrcMngRequestRltInfo(final boolean result, final String reqRltInfo, final boolean isDiagShow)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngRequestRltInfo() begin result = " + result + " reqRltInf = " + reqRltInfo + " isDiagShow = " + isDiagShow);

        this.mReqRlt = result;
        this.mReqRltInfo = reqRltInfo;
        this.mIsDiagShow = isDiagShow;

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " SrcMngRequestRltInfo() end");
    }

    /**
     * 获取请求结果
     * @return : false(禁止切替) / true(允许切替)
     */
    public boolean ismReqRlt() {
        return mReqRlt;
    }

    public void setReqRlt(boolean mReqRlt) {
        this.mReqRlt = mReqRlt;
    }

    /**
     * 将提示内容显示到Diag画面
     */
    public void showDiagInfo()
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " showDiagInfo() begin mIsDiagShow = " + mIsDiagShow);
/*
        //若允许提示用户，则提示内容
        if (!mIsDiagShow)
        {
            //信息属性及值设定
            DialogInfo inf = new DialogInfo();
            inf.setContent(mReqRltInfo);
            inf.setToastTime(3000); //设置显示时间为3s
            inf.setViewStyle(DialogInfo.VIEWSTYLE_WAINING);

            DialogManager mDialogMng = DialogManager.getDialogManager();

            if (mDialogMng != null)
            {
                mDialogMng.showToast(inf, new OnClickListener() {
                    @Override
                    public void firstClick() {
                        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " showDiagInfo() firstClick");
                    }

                    @Override
                    public void secondClick() {
                        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " showDiagInfo() secondClick");
                    }

                    @Override
                    public void toastClick() {
                        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " showDiagInfo() secondClick");
                    }
                });
            }
        }
*/
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " showDiagInfo() end");
    }
}
