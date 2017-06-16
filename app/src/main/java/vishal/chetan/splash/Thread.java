package vishal.chetan.splash;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Random;

import vishal.chetan.splash.asyncs.AsyncArrayHelper;

public class Thread {

    final static class ModificationTimeComparator implements Comparator<Thread> {
        @Override
        public int compare(@NonNull Thread o1, @NonNull Thread o2) {
            //descending order
            return o2.getMtime().compareTo(o1.getMtime());
        }
    }

    public int reported;

    public boolean needmod;

    public interface LoadCommentsListener {
        void onCommentsLoaded(boolean result);
    }

    public long adapterId = new Random().nextLong();

    public int getTopicId() {
        return topicId;
    }

    public long getThreadId() {
        return threadId;
    }

    private long threadId = -1;

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
    private long creator_id = -1;
    @Nullable
    private Date ctime = null;

    public void setMtime(@Nullable Date mtime) {
        this.mtime = mtime;
    }

    @Nullable
    private Date mtime = null;
    private int serverIndex = -1;
    private int topicId;

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean canShow() {
        return !hidden ||
                (GlobalFunctions.servers.get(serverIndex).identity != null &&
                        GlobalFunctions.servers.get(serverIndex).identity.getMod() > UserIdentity.MODERATOR_NONE);
    }

    private boolean blocked = false;
    private boolean hidden = false;

    public long getAttachId() {
        return attachId;
    }

    public void setAttachId(long attachId) {
        this.attachId = attachId;
    }

    private long attachId = -1;

    public int getAttachType() {
        return attachType;
    }

    private void setAttachType(String attachType) {
        if (attachType == null) {
            this.attachType = SplashCache.AttachmentCache.SplashAttachment.NONE;
        } else if (attachType.startsWith("image")) {
            this.attachType = SplashCache.AttachmentCache.SplashAttachment.IMAGE;
        } else if (attachType.startsWith("video")) {
            this.attachType = SplashCache.AttachmentCache.SplashAttachment.VIDEO;
        } else if (attachType.startsWith("audio")) {
            this.attachType = SplashCache.AttachmentCache.SplashAttachment.AUDIO;
        } else {
            this.attachType = SplashCache.AttachmentCache.SplashAttachment.OTHER;
        }
    }

    private int attachType = SplashCache.AttachmentCache.SplashAttachment.NONE;

    public Thread(int serverIndex, String title, String content, int topicId) {
        this.serverIndex = serverIndex;
        this.title = title;
        this.rawContent = content;
        this.content = GlobalFunctions.parseMarkdown(content);
        if (GlobalFunctions.servers.get(serverIndex).identity != null) {
            this.creator_id = GlobalFunctions.servers.get(serverIndex).identity.getUid();
        }
        this.topicId = topicId;
    }

    public Thread(long threadId, int serverIndex, String title, String content, long creator_id, long ctime, long mtime, int topicId, long attachId, String attachType) {
        this(threadId, serverIndex, title, content, creator_id, ctime, mtime, topicId);
        this.attachId = attachId;
        setAttachType(attachType);
    }

    public Thread(long threadId, int serverIndex, String title, String content, long creator_id, long ctime, long mtime, int topicId, long attachId, int attachType) {
        this(threadId, serverIndex, title, content, creator_id, ctime, mtime, topicId);
        this.attachId = attachId;
        this.attachType = attachType;
    }

    public Thread(long threadId, int serverIndex, String title, String content, long creator_id, long ctime, long mtime, int topicId) {
        this(serverIndex, title, content, topicId);
        this.threadId = threadId;
        this.creator_id = creator_id;
        this.ctime = new Date(ctime);
        this.mtime = new Date(mtime);
    }

    @NonNull
    public LongSparseArray<Comment> getComments() {
        return comments;
    }

    public Comment getComment(long commentId) {
        return comments.get(commentId);
    }

    public void getCommentAsync(long commentId, LoadCommentsListener listener) {
        if (getComment(commentId) != null) {
            listener.onCommentsLoaded(true);
        } else {
            loadComments(listener);
        }
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

    public void loadComments(final LoadCommentsListener listener) {
        this.clearComments();
        new AsyncArrayHelper(serverIndex, "comments/" + threadId) {
            @Override
            protected void workInBackground(@Nullable JSONArray jsonArray) {
                boolean result = true;
                if (jsonArray != null) {
                    for (int i = 0; i < jsonArray.length(); ++i) {
                        try {
                            JSONObject commentJSON = jsonArray.getJSONObject(i);
                            Thread.Comment comment = new Thread.Comment(serverIndex, threadId, commentJSON.getLong("author"), commentJSON.getString("content"), commentJSON.getLong("commentid"), commentJSON.getLong("ctime"), commentJSON.getLong("mtime"));
                            if (commentJSON.has("locked")) {
                                comment.setBlocked(commentJSON.getBoolean("locked"));
                            }
                            if (commentJSON.has("hidden")) {
                                comment.setHidden(commentJSON.getBoolean("hidden"));
                            }
                            if (commentJSON.has("parent")) {
                                comment.setParentCommentId(commentJSON.getLong("parent"));
                            }
                            comment.reported = commentJSON.getInt("reported");
                            if (comment.canShow()) {
                                Thread.this.addComment(comment);
                            }
                        } catch (JSONException e) {
                            result = false;
                            e.printStackTrace();
                        }
                    }
                } else {
                    result = false;
                }
                if (listener != null) {
                    listener.onCommentsLoaded(result);
                }
            }
        }.execute();
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
        private long creator_id = -1;
        private long commentId = -1;
        @Nullable
        private Date ctime = null;

        public void setText(String text) {
            this.text = text;
        }

        public void setMtime(@Nullable Date mtime) {
            this.mtime = mtime;
        }

        @Nullable
        private Date mtime = null;
        private long threadId = -1;

        public long getParentCommentId() {
            return parentCommentId;
        }

        public void setParentCommentId(long parentCommentId) {
            this.parentCommentId = parentCommentId;
        }

        private long parentCommentId = -1;
        private int serverIndex = -1;
        private boolean blocked = false;
        private boolean hidden = false;
        public int reported = 0;

        public boolean isBlocked() {
            return blocked;
        }

        public void setBlocked(boolean blocked) {
            this.blocked = blocked;
        }

        public boolean isHidden() {
            return hidden;
        }

        public void setHidden(boolean hidden) {
            this.hidden = hidden;
        }

        public boolean canShow() {
            return !hidden ||
                    (GlobalFunctions.servers.get(serverIndex).identity != null &&
                            GlobalFunctions.servers.get(serverIndex).identity.getMod() > UserIdentity.MODERATOR_NONE);
        }

        public Comment(int serverIndex, long threadId, long creator_id, String text, long commentId, long ctime, long mtime) {
            this(serverIndex, threadId, text);
            this.creator_id = creator_id;
            this.commentId = commentId;
            this.ctime = new Date(ctime);
            this.mtime = new Date(mtime);
        }

        public Comment(int serverIndex, long threadId, String text) {
            this.text = text;
            if (GlobalFunctions.servers.get(serverIndex).identity != null) {
                this.creator_id = GlobalFunctions.servers.get(serverIndex).identity.getUid();
            }
            this.threadId = threadId;
            this.serverIndex = serverIndex;
        }
    }
}
