package com.adayo.service.sourcemngservice.Control;

import com.adayo.service.sourcemngservice.Utils.LogUtils;

import com.adayo.service.sourcemngservice.Module.AppSwitchInfo;
import com.adayo.service.sourcemngservice.Utils.SrcMngLog;

import java.util.Comparator;

/**
 * Created by admin on 2018/4/6.
 */

public class SrcAppComparator implements Comparator<AppSwitchInfo> {
    private static final String  TAG  = SrcAppComparator.class.getSimpleName();

    @Override
    public int compare(AppSwitchInfo o1, AppSwitchInfo o2) {
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " compare o2 level = " + o2.getM_AppInfo().getM_PriorLevel());
        LogUtils.dL(SrcMngLog.LOG_TAG, TAG + " compare o1 level = " + o1.getM_AppInfo().getM_PriorLevel());

        return o1.getM_AppInfo().getM_PriorLevel() - o2.getM_AppInfo().getM_PriorLevel();
    }
}
