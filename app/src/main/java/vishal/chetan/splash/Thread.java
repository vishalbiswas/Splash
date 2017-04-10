package vishal.chetan.splash;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

public class Thread {
    public int getTopicId() {
        return topicId;
    }

    //TODO: is final required?
    final static class ModificationTimeComparator implements Comparator<Thread> {
        @Override
        public int compare(Thread o1, Thread o2) {
            return o1.getMtime().compareTo(o2.getMtime());
        }
    }

    public long getThreadId() {
        return threadId;
    }

    private final long threadId;
    private final String title;
    private final String content;
    private final long creator_id;
    private final Date ctime;
    private final Date mtime;
    private final int serverIndex;
    private final int topicId;

    /**
     * Create a new thread
     * @param threadId ID of the thread
     * @param serverIndex Index of GlobalFunctions.servers
     * @param title Title of thread
     * @param content Text content of the thread
     * @param creator_id UID of the poster
     * @param ctime Thread creation time
     * @param mtime Thread modification time
     * @param topicId Sub forum id
     */
    Thread (long threadId, int serverIndex, String title, String content, long creator_id, Date ctime, Date mtime, int topicId) {
        this.threadId = threadId;
        this.serverIndex = serverIndex;
        this.title = title;
        this.content = content;
        this.creator_id = creator_id;
        this.ctime = ctime;
        this.mtime = mtime;
        this.topicId = topicId;
    }

    public ArrayList<String> getComments() {
        return comments;
    }

    private ArrayList<String> comments = new ArrayList<>();

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public long getCreatorID() {
        return creator_id;
    }

    public Date getCtime() {
        return ctime;
    }

    public Date getMtime() {
        return mtime;
    }

    public int getServerIndex() {
        return serverIndex;
    }
}
