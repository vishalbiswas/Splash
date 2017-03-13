package net.ddns.vishalbiswas.splash;

import java.util.ArrayList;
import java.util.Date;

class Thread {
    private String title;
    private String content;
    private int creator_id;
    private Date ctime;
    private Date mtime;

    Thread (String title, String content, int creater_id, Date ctime, Date mtime) {
        this.title = title;
        this.content = content;
        this.creator_id = creater_id;
        this.ctime = ctime;
        this.mtime = mtime;
    }

    private ArrayList comments = null;

    String getTitle() {
        return title;
    }

    String getContent() {
        return content;
    }

    int getCreatorID() {
        return creator_id;
    }

    Date getCtime() {
        return ctime;
    }

    Date getMtime() {
        return mtime;
    }
}
