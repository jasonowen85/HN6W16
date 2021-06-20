package com.adayo.proxy.sourcemngproxy.Interface;

/**
 * Created by admin on 2018/4/10.
 */

public interface IAdayoFocusChange {
    public void onGainBeforeSwitchChannel();
    public void onGainAfterSwitchChannel();

    public void onLossBeforeSwitchChannel();
    public void onLossAfterSwitchChannel();

    public void onLossTransientBeforeSwitchChannel();
    public void onLossTransientAfterSwitchChannel();

    public void onLossTransientCanDuckBeforeSwitchChannel();
    public void onLossTransientCanDuckAfterSwitchChannel();
}
