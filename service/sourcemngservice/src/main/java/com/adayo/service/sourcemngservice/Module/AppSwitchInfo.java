package com.adayo.service.sourcemngservice.Module;

import com.adayo.proxy.sourcemngproxy.Beans.AppConfigType.SourceSwitch;
import android.os.Bundle;

import java.util.Map;

/**
 * Created by admin on 2018/4/9.
 */

public class  AppSwitchInfo {
    private static final String  TAG  = AppSwitchInfo.class.getSimpleName();
    private AppInfo m_AppInfo = null;
    private SourceSwitch m_SrcSwitchType = SourceSwitch.APP_OFF;
    private MSG_STATUS m_curMsgStatus = MSG_STATUS.MSG_STATUS_IDLE;
    private Map<String, String> m_hashMap = null;
    private String m_actionName;

	private Bundle mOption = null;
	
    //消息状态
    public enum MSG_STATUS
    {
        MSG_STATUS_IDLE,
        MSG_STATUS_RUNNING,
        MSG_STATUS_DONE
    }

    public AppSwitchInfo(AppInfo appInfo, SourceSwitch srcSwitchType, Map<String, String> map, String actionName)
    {
        this.m_AppInfo = appInfo;
        this.m_SrcSwitchType = srcSwitchType;
        this.m_hashMap = map;
        this.m_actionName = actionName;
    }

	public Bundle getOption(){
		return mOption;
		
	}
	
	public void setOption(Bundle option){
		mOption = option;
	}
	
    public AppInfo getM_AppInfo()
    {
        return m_AppInfo;
    }

    public SourceSwitch getM_SrcSwitchType()
    {
        return m_SrcSwitchType;
    }

    public void setM_curMsgStatus(MSG_STATUS msgStatus)
    {
        m_curMsgStatus = msgStatus;
    }

    public MSG_STATUS getM_curMsgStatus()
    {
        return m_curMsgStatus;
    }

    public Map<String, String> getM_hashMap()
    {
        return m_hashMap;
    }

    public String getM_actionName()
    {
        return m_actionName;
    }
}
