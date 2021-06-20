package com.adayo.service.sourcemngservice.Control.Interface;

/**
 * Created by admin on 2018/4/11.
 */

public interface ISrcMngChannelChange {
    int switchAudioChannel(int channeValue);
    int getCurrentAudioChannel();
    int setWorkMode(int modeValue);
}
