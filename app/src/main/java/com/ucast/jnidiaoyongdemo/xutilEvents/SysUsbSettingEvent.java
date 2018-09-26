package com.ucast.jnidiaoyongdemo.xutilEvents;

/**
 * Created by pj on 2018/9/25.
 */
public class SysUsbSettingEvent {
    boolean isSetNone = false;
    long firstTime = 8500L;

    public SysUsbSettingEvent(boolean isSetNone) {
        this.isSetNone = isSetNone;
    }

    public SysUsbSettingEvent(boolean isSetNone, long firstTime) {
        this.isSetNone = isSetNone;
        this.firstTime = firstTime;
    }

    public boolean isSetNone() {
        return isSetNone;
    }

    public void setSetNone(boolean setNone) {
        isSetNone = setNone;
    }

    public long getFirstTime() {
        return firstTime;
    }

    public void setFirstTime(long firstTime) {
        this.firstTime = firstTime;
    }
}
