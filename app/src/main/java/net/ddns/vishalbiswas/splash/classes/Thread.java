package net.ddns.vishalbiswas.splash.classes;

import java.util.ArrayList;
import java.util.Date;

class Thread {
    private String title;
    private String content;
    private int creator_id;
    private Date ctime;
    private Date mtime;
    private int serverIndex;

    /**
     * Create a new thread
     * @param serverIndex Index of GlobalFunctions.servers
     * @param title Title of thread
     * @param content Text content of the thread
     * @param creater_id UID of the poster
     * @param ctime Thread creation time
     * @param mtime Thread modification time
     */
    Thread (int serverIndex, String title, String content, int creater_id, Date ctime, Date mtime) {
        this.serverIndex = serverIndex;
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

    public int getServerIndex() {
        return serverIndex;
    }
}
