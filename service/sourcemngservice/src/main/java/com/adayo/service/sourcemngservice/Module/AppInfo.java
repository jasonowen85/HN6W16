package com.adayo.service.sourcemngservice.Module;

import com.adayo.proxy.sourcemngproxy.Beans.AppConfigType.SourceType;

public class AppInfo {
	private int			    		m_AppId;				//01.APP Id
	private String 					m_CaptionName;			//02.Caption Name
	private String 					m_PriorLevel;			//03.Prior Level
	private String 					m_StoreProperty;		//04.Store Property
	private String 					m_ModeSeqValue;			//05.Mode Sequence Value
	private SourceType				m_AppType;				//06.APP Type(UI/AUDIO_UI)
	private String 					m_AppPackageName;		//07.Package Name
	private String 					m_AutoProperty;			//08.AutoProperty
	private int						m_AudioChannel;			//09.AudioChannel
	private String					m_AppExists;			//10.AppExist
	private String					m_SourceType;			//11.Source Type
	private String					m_ModeSwitchCondition;	//mode键切替是否存在条件

	/*
	Method Name:AppInfo
	Function:   Construction function
	Parameters: AppId:AppId                     Type:String
	            CaptionName:Caption Name        Type:String
	            PriorLevel:Prior Level          Type:String
	            StoreProperty:Store Property    Type:String
	            ModeSeqValue:Mode SeqValue      Type:String
	            AppType:App Type                Type:String
	            AppPackageName:App PackageName  Type:String
    Return:     NULL
	 */
	public AppInfo(int AppId, String CaptionName, String PriorLevel,
			String StoreProperty,String ModeSeqValue, SourceType AppType,
			String AppPackageName, String AutoProperty, int audioChannel, String AppExist, String sourceType)
	{
		m_AppId             =       AppId;
		m_CaptionName       =       CaptionName;
		m_PriorLevel        =       PriorLevel;
		m_StoreProperty     =       StoreProperty;
		m_ModeSeqValue      =       ModeSeqValue;
		m_AppType           =       AppType;
		m_AppPackageName    =       AppPackageName;
		m_AutoProperty		=		AutoProperty;
		m_AudioChannel		= 		audioChannel;
		m_AppExists			=		AppExist;
		m_SourceType		=		sourceType;
	}

	/**
	 *
	 * @return
     */
	public int getM_AudioChannel() {
		return m_AudioChannel;
	}

	/*
        Method Name:getAppId
        Function:   get the AppId
        Parameters: NULL
        Return:     the member m_AppId's value
         */
	public int getM_AppId()
	{
		return m_AppId;
	}

    /*
    Method Name:getCaptionName
    Function:   get the CaptionName
    Parameters: NULL
    Return:     the member m_CaptionName's value
     */
	public String getM_CaptionName()
	{
		return m_CaptionName;
	}

    /*
    Method Name:getPriorLevel
    Function:   get the PriorLevel(the prior of the app)
    Parameters: NULL
    Return:     the member m_PriorLevel's value
     */
	public int getM_PriorLevel()
	{
		int retValue = 0;
		try
		{
			retValue = Integer.parseInt(m_PriorLevel);
		}
		catch (NumberFormatException e)
		{
			//put the error into diag model
		}

		return retValue;
	}

    /*
    Method Name:getStoreProperty
    Function:   get the Store Property(when ACC OFF started, App store property)
    Parameters: NULL
    Return:     the member m_StoreProperty's value
     */
	public String getM_StoreProperty()
	{
		return m_StoreProperty;
	}

    /*
    Method Name:getModeSeqValue
    Function:   get the Mode Sequence(when mode key pressed, run app's sequence)
    Parameters: NULL
    Return:     the member m_ModeSeqValue's value
     */
	public int getM_ModeSeqValue()
	{
		final String ZERO = "0";
		if (ZERO.equals(m_ModeSeqValue))
		{
			return 0;
		}

		int retValue = 0;
		boolean conditionExists = m_ModeSeqValue.indexOf('_') > -1 ? true : false;

		try
		{
			//如果有条件进行mode键操作
			if (conditionExists)
			{
				final int MODE_SEQ_VALUE = 0;	//mode键切替顺序
				final int MODE_SWITCH_CON = 1;	//mode键条件字符串

				String [] sArray = m_ModeSeqValue.split("_");
				retValue = Integer.parseInt(sArray[MODE_SEQ_VALUE]);
				m_ModeSwitchCondition = sArray[MODE_SWITCH_CON];
			}
			else
			{
				retValue = Integer.parseInt(m_ModeSeqValue);
			}
		}
		catch (NumberFormatException e)
		{
			e.printStackTrace();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}

		return retValue;
	}

	/**
	 * 获取mode键切替是否存在条件
	 * @return
	 */
	public String getM_ModeSwitchCondition()
	{
		return m_ModeSwitchCondition;
	}



    /*
    Method Name:getAppType
    Function:   get the App Type(UI / UI_AUDIO)
    Parameters: NULL
    Return:     the member m_AppType's value
     */
	public SourceType getM_AppType()
	{
		return m_AppType;
	}

    /*
    Method Name:m_AppPackageName
    Function:   get the App's PackageName
    Parameters: NULL
    Return:     the member m_AppPackageName's value
     */
	public String getM_AppPackageName()
	{
		return m_AppPackageName;
	}

	public String getM_AutoProperty()
	{
		return m_AutoProperty;
	}

	public String getM_AppExists()
	{
		return m_AppExists;
	}

	/**
	 * 获取源类型
	 * @return
	 */
	public String getM_SourceType() {
		return m_SourceType;
	}
}
