package com.ucast.jnidiaoyongdemo.xutilEvents;

/**
 * Created by pj on 2018/11/28.
 */
public class UdiskEvent {
    String path = "";

    public UdiskEvent(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
