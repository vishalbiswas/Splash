package vishal.chetan.splash;

import android.support.v4.util.LongSparseArray;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;

public class Thread {
    public int getTopicId() {
        return topicId;
    }

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

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.rawContent = content;
        this.content = GlobalFunctions.parseMarkdown(content);
    }

    public void setTopicId(int topicId) {
        this.topicId = topicId;
    }

    private String title;
    private String content;

    public String getRawContent() {
        return rawContent;
    }

    private String rawContent;
    private final long creator_id;
    private final Date ctime;

    public void setMtime(Date mtime) {
        this.mtime = mtime;
    }

    private Date mtime;
    private final int serverIndex;
    private int topicId;

    public long getAttachId() {
        return attachId;
    }

    public void setAttachId(long attachId) {
        this.attachId = attachId;
    }

    private long attachId;

    public Thread(long threadId, String title, String content, long creator_id, Date ctime, Date mtime, int serverIndex, int topicId, long attachId) {
        this.threadId = threadId;
        this.serverIndex = serverIndex;
        this.title = title;
        this.rawContent = content;
        this.content = GlobalFunctions.parseMarkdown(content);
        this.creator_id = creator_id;
        this.ctime = ctime;
        this.mtime = mtime;
        this.topicId = topicId;
        this.attachId = attachId;
    }

    public Thread(String title, String content, int serverIndex, int topicId, long attachId) {
        this.threadId = -1;
        this.serverIndex = serverIndex;
        this.title = title;
        this.rawContent = content;
        this.content = GlobalFunctions.parseMarkdown(content);
        this.creator_id = GlobalFunctions.identities.get(serverIndex).getUid();
        this.ctime = null;
        this.mtime = null;
        this.topicId = topicId;
        if (attachId < 0) {
            this.attachId = -1;
        } else {
            this.attachId = attachId;
        }
    }

    public Thread(String title, String content, int serverIndex, int topicId) {
        this.threadId = -1;
        this.serverIndex = serverIndex;
        this.title = title;
        this.rawContent = content;
        this.content = GlobalFunctions.parseMarkdown(content);
        this.creator_id = GlobalFunctions.identities.get(serverIndex).getUid();
        this.ctime = null;
        this.mtime = null;
        this.topicId = topicId;
        this.attachId = -1;
    }

    public Thread(long threadId, int serverIndex, String title, String content, long creator_id, Date ctime, Date mtime, int topicId) {
        this.threadId = threadId;
        this.serverIndex = serverIndex;
        this.title = title;
        this.rawContent = content;
        this.content = GlobalFunctions.parseMarkdown(content);
        this.creator_id = creator_id;
        this.ctime = ctime;
        this.mtime = mtime;
        this.topicId = topicId;
        this.attachId = -1;
    }

    public LongSparseArray<Comment> getComments() {
        return comments;
    }

    public Comment getComment(long commentId) {
        return comments.get(commentId);
    }

    public void setComment(Comment comment) {
        if (comment.getServerIndex() == serverIndex && comment.getThreadId() == threadId) {
            comments.put(comment.getCommentId(), comment);
        }
    }

    public void addComment(Comment comment) {
        if (comment.getServerIndex() == serverIndex && comment.getThreadId() == threadId) {
            comments.append(comment.getCommentId(), comment);
        }
    }

    public void clearComments() {
        comments.clear();
    }

    private final LongSparseArray<Comment> comments = new LongSparseArray<>();

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

    public static class Comment {
        public String getText() {
            return text;
        }

        public long getCreatorID() {
            return creator_id;
        }

        public long getCommentId() {
            return commentId;
        }

        public Date getCtime() {
            return ctime;
        }

        public Date getMtime() {
            return mtime;
        }

        public long getThreadId() {
            return threadId;
        }

        public int getServerIndex() {
            return serverIndex;
        }

        private String text;
        private long creator_id;
        private long commentId;
        private Date ctime;

        public void setText(String text) {
            this.text = text;
        }

        public void setMtime(Date mtime) {
            this.mtime = mtime;
        }

        private Date mtime;
        private long threadId;
        private int serverIndex;

        public Comment(String text, long creator_id, long commentId, Date ctime, Date mtime, long threadId, int serverIndex) {
            this.text = text;
            this.creator_id = creator_id;
            this.commentId = commentId;
            this.ctime = ctime;
            this.mtime = mtime;
            this.threadId = threadId;
            this.serverIndex = serverIndex;
        }
    }
}
