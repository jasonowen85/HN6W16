package com.adayo.proxy.sourcemngproxy.Beans;

/**
 * Created by admin on 2018/4/2.
 */

public class AppConfigType
{
    public enum SourceSwitch
    {
        APP_ON(0),
        APP_OFF(1),
        APP_NULL(2);

        private int mValue = 0;

        SourceSwitch(int value)
        {
            this.mValue = value;
        }

        public int getValue()
        {
            return this.mValue;
        }

        public static SourceSwitch getSourceSwitch(int value)
        {
            for (SourceSwitch type : SourceSwitch.values())
            {
                if (type.getValue() == value)
                {
                    return type;
                }
            }
            return APP_NULL;
        }
    }

    public enum SourceType
    {
        IDLE(0),
        UI(1),
        UI_AUDIO(2);

        private int mValue = 0;

        SourceType(int value)
        {
            this.mValue = value;
        }

        public int getValue()
        {
            return this.mValue;
        }

        public static SourceType getSourceType(int value)
        {
            for (SourceType type : SourceType.values())
            {
                if (type.getValue() == value)
                {
                    return type;
                }
            }
            return IDLE;
        }
    }

    /**
     * 系统静音状态
     */
    public enum MuteStatus
    {
        MUTE_ON(1),
        MUTE_OFF(0),
        DEFAULT(-1);

        private int mValue = 0;

        MuteStatus(int value)
        {
            this.mValue = value;
        }

        public int getValue()
        {
            return this.mValue;
        }

        public static MuteStatus getMuteStatus(int value)
        {
            for (MuteStatus status : MuteStatus.values())
            {
                if (status.getValue() == value)
                {
                    return status;
                }
            }
            return DEFAULT;
        }
    }

    //APP ????
    public enum SourceAvaliable
    {
        ENABLE,
        DISABLE
    }
}

