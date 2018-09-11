package com.ucast.jnidiaoyongdemo.xutilEvents;

/**
 * Created by pj on 2018/8/30.
 */
public class MediapalyEvent {
    int source;
    String path;

    public MediapalyEvent(int source) {
        this.source = source;
    }

    public int getSource() {
        return source;
    }

    public void setSource(int source) {
        this.source = source;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
