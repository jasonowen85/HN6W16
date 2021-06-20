// ISrcMngProxyClient.aidl
package com.adayo.proxy.sourcemngproxy;

import com.adayo.proxy.sourcemngproxy.ISourceMngMicCallBack;
import com.adayo.proxy.sourcemngproxy.ISourceActionCallBack;
import com.adayo.proxy.sourcemngproxy.Beans.SourceInfo;


// Declare any non-default types here with import statements

interface ISrcMngProxyClient {
    boolean onRequest(in SourceInfo scrinf);

    boolean hasAudioFocus(String sourceType);

    boolean notifyAppFinished(String sourceType);

    boolean notifyAbandonAudioFocus(String sourceType);

    boolean notifyRequestAudioFocus(String sourceType);

    boolean notifyServiceAudioChange(String sourceType, int dorationHint);

    boolean notifyServiceUIChange(String sourceType);

    boolean requestMicFocus(ISourceMngMicCallBack Callback);

    boolean abandonMicFocus(ISourceMngMicCallBack Callback);

    void notifyLauncherFinished();

    boolean getSourceAvailable(String packageName);

    String getCurrentAudioFocus();

    String getCurrentUID();

    boolean registeSourceActionCallBackFunc(String sourceType, ISourceActionCallBack callback);

    boolean unRegisteSourceActionCallBackFunc(String sourceType);

    boolean pauseSwitchSource();

    int getAudioChannelBySourceId(String sourceId);

    int onGetCurrentChannel();

    boolean onIsAudioFocusOccupy();

    int onSwitchChannel(int channelId);

    int getCurrentStreamType();

    boolean duckAudioStream(int streamType);

    boolean unDuckAudioStream(int streamType);

    boolean setSystemMuteStatus(int streamType, int muteStatus);

    boolean canRequestAudioFocus(String audioType);

    List<String> getUIList();

    List<String> getAudioList();
}
