package com.adayo.service.sourcemngservice.Control.SrcMngHardKeyManage;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.adayo.proxy.share.ShareDataManager;
import com.adayo.service.sourcemngservice.Control.SrcMngServiceMng;
import com.adayo.service.sourcemngservice.Utils.LogUtils;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by admin on 2018/4/18.
 */
public class SrcMngHomeKey implements ISrcMngHardKeyImp {
    private static final String  TAG  = SrcMngHomeKey.class.getSimpleName();

    @Override
    public void notifyMsg( final String event) {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() begin event = " + event);

        final int SHARE_INFO_NUM = 16;
        final String s = ShareDataManager.getShareDataManager().getShareData(SHARE_INFO_NUM);
        boolean rvsStatus = getRvcStatus(s);
        if (rvsStatus)
        {
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() end backCar");
            return;
        }


        startAppOn();

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " notifyMsg() end");
    }

    private void startAppOn()
    {
        Context context = SrcMngServiceMng.getInstance().getM_Context();
        //555323  当前的Home界面 按Menu 导致home被重新onResume
        final String mediaActivityName = "com.adayo.ui.launcher.main.Launcher_MainActivity";
        boolean isHomeTop = mediaActivityName.equals(getTopActivity(context));
        if (isHomeTop) return;
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " startAppOn() begin");

        Intent i = new Intent(Intent.ACTION_MAIN);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addCategory(Intent.CATEGORY_HOME);
        if (context != null)
        {
            context.startActivity(i);
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " startAppOn() end");
    }

    private boolean getRvcStatus(final String s)
    {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getRvcStatus() begin s = " + s);

        if (s == null)
        {
            return false;
        }

        boolean status = false;
        JSONObject obj = null;
        try
        {
            obj = new JSONObject(s);
            if (obj != null && obj.has("backCarState"))
            {
                status = obj.getBoolean("backCarState");
            }
        }catch (JSONException e)
        {
            e.printStackTrace();
        }

        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getRvcStatus() end status = " + status);

        return status;
    }

    /**
     * 获得栈中最顶层的Activity
     *
     * @param
     * @return
     */
    public String getTopActivity(Context mContext) {
        if (mContext ==null ) return null;
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getTopActivity(() begin");
        android.app.ActivityManager manager = (android.app.ActivityManager) mContext.getSystemService(mContext.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> activityStackList = manager.getRunningTasks(1);
        if (activityStackList != null) {
            String result = activityStackList.get(0).topActivity.getClassName();
            LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " getTopActivity(() end =" + result);
            return result;
        } else
            return null;
    }
}
