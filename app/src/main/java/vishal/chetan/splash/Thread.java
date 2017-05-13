package vishal.chetan.splash;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;

import java.util.Comparator;
import java.util.Date;

public class Thread {
    final static class ModificationTimeComparator implements Comparator<Thread> {
        @Override
        public int compare(@NonNull Thread o1, @NonNull Thread o2) {
            //descending order
            return o2.getMtime().compareTo(o1.getMtime());
        }
    }

    public int getTopicId() {
        return topicId;
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
    @Nullable
    private final Date ctime;

    public void setMtime(@Nullable Date mtime) {
        this.mtime = mtime;
    }

    @Nullable
    private Date mtime;
    private final int serverIndex;
    private int topicId;

    public long getAttachId() {
        return attachId;
    }

    public void setAttachId(long attachId) {
        this.attachId = attachId;
    }

    private long attachId = -1;

    public Thread(int serverIndex, String title, String content, int topicId) {
        this.threadId = -1;
        this.serverIndex = serverIndex;
        this.title = title;
        this.rawContent = content;
        this.content = GlobalFunctions.parseMarkdown(content);
        this.creator_id = GlobalFunctions.identities.get(serverIndex).getUid();
        this.ctime = null;
        this.mtime = null;
        this.topicId = topicId;
    }

    public Thread(long threadId, int serverIndex, String title, String content, long creator_id, long ctime, long mtime, int topicId, long attachId) {
        this.threadId = threadId;
        this.serverIndex = serverIndex;
        this.title = title;
        this.rawContent = content;
        this.content = GlobalFunctions.parseMarkdown(content);
        this.creator_id = creator_id;
        this.ctime = new Date(ctime);
        this.mtime = new Date(mtime);
        this.topicId = topicId;
        this.attachId = attachId;
    }
    public Thread(long threadId, int serverIndex, String title, String content, long creator_id, long ctime, long mtime, int topicId) {
        this.threadId = threadId;
        this.serverIndex = serverIndex;
        this.title = title;
        this.rawContent = content;
        this.content = GlobalFunctions.parseMarkdown(content);
        this.creator_id = creator_id;
        this.ctime = new Date(ctime);
        this.mtime = new Date(mtime);
        this.topicId = topicId;
        this.attachId = -1;
    }

    @NonNull
    public LongSparseArray<Comment> getComments() {
        return comments;
    }

    public Comment getComment(long commentId) {
        return comments.get(commentId);
    }

    public void setComment(@NonNull Comment comment) {
        if (comment.getServerIndex() == serverIndex && comment.getThreadId() == threadId) {
            comments.put(comment.getCommentId(), comment);
        }
    }

    public void addComment(@NonNull Comment comment) {
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

    @Nullable
    public Date getCtime() {
        return ctime;
    }

    @Nullable
    public Date getMtime() {
        return mtime;
    }

    public int getServerIndex() {
        return serverIndex;
    }

    public static class Comment {
        final static class ModificationTimeComparator implements Comparator<Comment> {
            @Override
            public int compare(@NonNull Comment o1, @NonNull Comment o2) {
                //ascending order
                return o1.getMtime().compareTo(o2.getMtime());
            }
        }

        public String getText() {
            return text;
        }

        public long getCreatorID() {
            return creator_id;
        }

        public long getCommentId() {
            return commentId;
        }

        @Nullable
        public Date getCtime() {
            return ctime;
        }

        @Nullable
        public Date getMtime() {
            return mtime;
        }

        long getThreadId() {
            return threadId;
        }

        public int getServerIndex() {
            return serverIndex;
        }

        private String text;
        final private long creator_id;
        final private long commentId;
        @Nullable
        final private Date ctime;

        public void setText(String text) {
            this.text = text;
        }

        public void setMtime(@Nullable Date mtime) {
            this.mtime = mtime;
        }

        @Nullable
        private Date mtime = null;
        final private long threadId;
        final private int serverIndex;

        public Comment(int serverIndex, long threadId, long creator_id, String text, long commentId, long ctime, long mtime) {
            this.text = text;
            this.creator_id = creator_id;
            this.commentId = commentId;
            this.ctime = new Date(ctime);
            this.mtime = new Date(mtime);
            this.threadId = threadId;
            this.serverIndex = serverIndex;
        }

        public Comment(int serverIndex, long threadId, String text) {
            this.text = text;
            this.creator_id = GlobalFunctions.identities.get(serverIndex).getUid();
            this.commentId = -1;
            this.ctime = null;
            this.mtime = null;
            this.threadId = threadId;
            this.serverIndex = serverIndex;
        }
    }
}
