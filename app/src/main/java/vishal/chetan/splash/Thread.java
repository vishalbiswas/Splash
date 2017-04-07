package vishal.chetan.splash;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

class Thread {
    //TODO: is final required?
    final static class ModificationTimeComparator implements Comparator<Thread> {
        @Override
        public int compare(Thread o1, Thread o2) {
            return o1.getMtime().compareTo(o2.getMtime());
        }
    }

    private final String title;
    private final String content;
    private final int creator_id;
    private final Date ctime;
    private final Date mtime;
    private final int serverIndex;
    private final int topicId;

    /**
     * Create a new thread
     * @param serverIndex Index of GlobalFunctions.servers
     * @param title Title of thread
     * @param content Text content of the thread
     * @param creater_id UID of the poster
     * @param ctime Thread creation time
     * @param mtime Thread modification time
     */
    Thread (int serverIndex, String title, String content, int creater_id, Date ctime, Date mtime, int topicId) {
        this.serverIndex = serverIndex;
        this.title = title;
        this.content = content;
        this.creator_id = creater_id;
        this.ctime = ctime;
        this.mtime = mtime;
        this.topicId = topicId;
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
