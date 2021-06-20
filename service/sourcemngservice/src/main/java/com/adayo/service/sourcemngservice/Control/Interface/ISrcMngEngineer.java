package com.adayo.service.sourcemngservice.Control.Interface;

import com.adayo.service.sourcemngservice.Control.SrcMngRequestRltInfo;
import com.adayo.service.sourcemngservice.Module.AppInfo;

/**
 * Created by admin on 2018/4/8.
 */

public interface ISrcMngEngineer {
    SrcMngRequestRltInfo requestChange(AppInfo reqInfo);

    void setBluePhoneCallOn(boolean isPhoneCallOn) ;
}
