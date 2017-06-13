package vishal.chetan.splash;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import vishal.chetan.splash.asyncs.ThreadHelper;

import static android.content.ContentValues.TAG;

public class SplashCache {
    public static class UsersCache {
        public interface OnGetUserListener {
            void onGetUser(final UserIdentity user);
        }

        private static final SparseArray<LongSparseArray<UserIdentity>> usernames = new SparseArray<>();

        @Nullable
        public static UserIdentity getUser(final int serverIndex, final long uid, @Nullable final OnGetUserListener listener) {
            LongSparseArray<UserIdentity> serverArray = usernames.get(serverIndex);
            UserIdentity user = null;
            if (serverArray == null) {
                serverArray = new LongSparseArray<>();
                usernames.append(serverIndex, serverArray);
            } else {
                user = serverArray.get(uid);
            }
            if (user == null) {
                loadUser(serverIndex, uid, listener);
            } else if (listener != null) {
                listener.onGetUser(user);
            }
            return user;
        }

        private static void loadUser(final int serverIndex, long uid, @Nullable final OnGetUserListener listener) {
            Runnable loader = new ThreadHelper(serverIndex, "user/" + uid) {
                @Override
                protected void doWork(@Nullable  JSONObject jsonObject) {
                    if (jsonObject != null) {
                        try {
                            UserIdentity fetcheduser = new UserIdentity(jsonObject.getLong("uid"),
                                    jsonObject.getString("username"), jsonObject.getString("email"));
                            if (jsonObject.has("fname")) {
                                fetcheduser.setFirstname(jsonObject.getString("fname"));
                            }
                            if (jsonObject.has("lname")) {
                                fetcheduser.setLastname(jsonObject.getString("lname"));
                            }
                            if (jsonObject.has("profpic")) {
                                fetcheduser.setProfpic(jsonObject.getLong("profpic"));
                            }
                            setUser(serverIndex, fetcheduser);
                            if (listener != null) {
                                listener.onGetUser(fetcheduser);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e(TAG, "Unknown error");
                    }
                }
            };
            GlobalFunctions.executor.execute(loader);
        }

        static void setUser(int serverIndex, @NonNull UserIdentity user) {
            LongSparseArray<UserIdentity> serverArray = usernames.get(serverIndex);
            if (serverArray == null) {
                serverArray = new LongSparseArray<>();
                usernames.append(serverIndex, serverArray);
            }
            serverArray.append(user.getUid(), user);
        }
    }

    public static class ThreadCache {
        public static ArrayList<Thread> getModeratable() {
            ArrayList<Thread> allThreads = new ArrayList<>();
            for (int i = 0; i < GlobalFunctions.servers.size(); ++i) {
                if (GlobalFunctions.servers.get(i).identity == null || GlobalFunctions.servers.get(i).identity.getMod() <= 0) {
                    continue;
                }
                LongSparseArray<Thread> threadList = threads.get(i, new LongSparseArray<Thread>());
                for (int j = 0; j < threadList.size(); ++j) {
                    Thread thread = threadList.valueAt(i);
                    if (thread.needmod) {
                        allThreads.add(thread);
                    }
                }
            }
            Collections.sort(allThreads, new Thread.ModificationTimeComparator());
            return allThreads;
        }

        public interface OnThreadModifiedListener {
            void onModify(Thread thread);
        }

        private static final SparseArray<LongSparseArray<Thread>> threads = new SparseArray<>();
        @Nullable
        static OnThreadModifiedListener adapterListener = null;
        @Nullable
        public static OnThreadModifiedListener postListener = null;

        @Nullable
        static ArrayList<Thread> getAllForIndex(final int filterIndex) {
            if (filterIndex == -1) {
                ArrayList<Thread> allThreads = new ArrayList<>();
                for (int i = 0; i < GlobalFunctions.servers.size(); ++i) {
                    //noinspection ConstantConditions
                    allThreads.addAll(getAllForIndex(i));
                }
                Collections.sort(allThreads, new Thread.ModificationTimeComparator());
                return allThreads;
            } else if (filterIndex < 0) {
                // index should be positive
                return null;
            } else {
                LongSparseArray<Thread> threadList = threads.get(filterIndex, new LongSparseArray<Thread>());
                ArrayList<Thread> returnList = new ArrayList<>();
                for (int i = 0; i < threadList.size(); ++i) {
                    Thread thread = threadList.valueAt(i);
                    if (thread.canShow()) {
                        returnList.add(thread);
                    }
                }
                Collections.sort(returnList, new Thread.ModificationTimeComparator());
                return returnList;
            }
        }

        public static void add(@NonNull final Thread thread) {
            LongSparseArray<Thread> threadList = threads.get(thread.getServerIndex());
            if (threadList == null) {
                threadList = new LongSparseArray<>();
                threadList.append(thread.getThreadId(), thread);
                threads.append(thread.getServerIndex(), threadList);
            } else {
                threadList.append(thread.getThreadId(), thread);
            }
        }

        public static void create(@NonNull final Thread thread) {
            String postMessage = String.format("title=%s&content=%s&sessionid=%s&topicid=%s", thread.getTitle(), thread.getRawContent(), GlobalFunctions.servers.get(thread.getServerIndex()).identity.getSessionid(), thread.getTopicId());
            if (thread.getAttachId() >= 0) {
                postMessage = String.format("%s&attachid=%s", postMessage, thread.getAttachId());
            }
            Runnable creator = new ThreadHelper(thread.getServerIndex(), "post", postMessage) {
                @Override
                protected void doWork(@Nullable JSONObject jsonObject) {
                    Thread newThread = null;
                    if (jsonObject != null) {
                        try {
                            newThread = new Thread(jsonObject.getLong("threadid"), thread.getServerIndex(),
                                    thread.getTitle(), thread.getRawContent(), thread.getCreatorID(),
                                    jsonObject.getLong("ctime"),
                                    jsonObject.getLong("mtime"),
                                    thread.getTopicId(), thread.getAttachId(),
                                    thread.getAttachType());
                            add(newThread);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "Unable to add thread");
                    }
                    if (adapterListener != null) {
                        adapterListener.onModify(newThread);
                    }
                    if (postListener != null) {
                        postListener.onModify(newThread);
                        postListener = null;
                    }
                }
            };
            GlobalFunctions.executor.execute(creator);
        }

        public static void set(@NonNull final Thread thread) {
            String postMessage = String.format("title=%s&content=%s&sessionid=%s&topicid=%s", thread.getTitle(), thread.getRawContent(), GlobalFunctions.servers.get(thread.getServerIndex()).identity.getSessionid(), thread.getTopicId());
            if (thread.getAttachId() >= 0) {
                postMessage = String.format("%s&attachid=%s", postMessage, thread.getAttachId());
            }
            Runnable setter = new ThreadHelper(thread.getServerIndex(), "editpost/" + thread.getThreadId(), postMessage) {
                @Override
                protected void doWork(@Nullable JSONObject jsonObject) {
                    Thread newThread = null;
                    if (jsonObject != null) {
                        newThread = thread;
                        try {
                            newThread.setMtime(new Date(jsonObject.getLong("mtime")));
                            LongSparseArray<Thread> threadList = threads.get(newThread.getServerIndex());
                            if (threadList == null) {
                                threadList = new LongSparseArray<>();
                                threadList.append(newThread.getThreadId(), newThread);
                                threads.append(newThread.getServerIndex(), threadList);
                            } else {
                                threadList.put(newThread.getThreadId(), newThread);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    if (adapterListener != null) {
                        adapterListener.onModify(newThread);
                    }
                    if (postListener != null) {
                        postListener.onModify(newThread);
                        postListener = null;
                    }
                }
            };
            GlobalFunctions.executor.execute(setter);
        }

        public static Thread getThread(int serverIndex, long threadId) {
            return threads.get(serverIndex).get(threadId);
        }
    }

    public static class AttachmentCache {
        public static final class SplashAttachment {
            public static final int NONE = 0;
            public static final int IMAGE = 1;
            public static final int VIDEO = 2;
            public static final int AUDIO = 3;
            public static final int OTHER = 4;

            @NonNull
            public Object data;
            public int type = OTHER;
            public String mimeType;
            public String name = "attach";

            public SplashAttachment(@NonNull Object data) {
                if (data instanceof Bitmap) {
                    this.type = IMAGE;
                    this.mimeType = "image/png";
                } else {
                    this.mimeType = "application/octet-stream";
                    this.type = OTHER;
                }
                this.data = data;
            }

            public SplashAttachment(@NonNull Object data, @NonNull String name) {
                if (data instanceof Bitmap) {
                    this.type = IMAGE;
                    this.mimeType = "image/png";
                } else {
                    this.mimeType = "application/octet-stream";
                    this.type = OTHER;
                }
                this.data = data;
                this.name = name;
            }
        }

        public interface OnUploadCompleteListener {
            void onUpload(long attachId);
        }

        public interface OnGetAttachmentListener {
            void onGetAttachment(final SplashAttachment attachment);
        }

        private final static SparseArray<LongSparseArray<SplashAttachment>> attachments = new SparseArray<>();

        public static SplashAttachment get(int serverIndex, long attachId, OnGetAttachmentListener listener) {
            if (attachId < 0) {
                return null;
            }
            LongSparseArray<SplashAttachment> serverArray = attachments.get(serverIndex);
            SplashAttachment attachment = null;
            if (serverArray == null) {
                serverArray = new LongSparseArray<>();
                attachments.append(serverIndex, serverArray);
            } else {
                attachment = serverArray.get(attachId);
            }
            if (attachment == null) {
                loadAttachment(serverIndex, attachId, listener);
            } else {
                if (listener != null) {
                    listener.onGetAttachment(attachment);
                }
            }
            return attachment;
        }

        static private void loadAttachment(final int serverIndex, final long attachId, final OnGetAttachmentListener listener) {
            Runnable loader = new ThreadHelper(serverIndex, "attachment/" + attachId) {
                @Nullable
                SplashAttachment attachment = null;

                @Override
                protected void doWork(@Nullable JSONObject jsonObject) {
                    if (jsonObject != null) {
                        try {
                            if (jsonObject.getInt("status") != 0) {
                                Log.e(TAG, "Fetch attachment failed");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e(TAG, "Unknown error");
                    }
                    if (listener != null) {
                        listener.onGetAttachment(attachment);
                    }
                }

                @Override
                protected JSONObject workInput(HttpURLConnection webservice) throws JSONException, IOException {
                    String type = webservice.getHeaderField("Content-Type");
                    InputStream stream = webservice.getInputStream();
                    if (type != null && type.startsWith("image")) {
                        attachment = new SplashAttachment(BitmapFactory.decodeStream(stream));
                    } else {
                        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                        int nRead;
                        byte[] data = new byte[16384];

                        while ((nRead = stream.read(data, 0, data.length)) != -1) {
                            buffer.write(data, 0, nRead);
                        }

                        buffer.flush();
                        attachment = new SplashAttachment(buffer.toByteArray());
                    }
                    attachment.name = webservice.getHeaderField("FileName");
                    attachment.mimeType = type;
                    setAttachment(serverIndex, attachId, attachment);
                    return new JSONObject("{status:0}");
                }
            };
            GlobalFunctions.executor.execute(loader);
        }

        static void setAttachment(int serverIndex, long attachId, SplashAttachment attachment) {
            if (attachId < 0) {
                return;
            }
            LongSparseArray<SplashAttachment> serverArray = attachments.get(serverIndex);
            if (serverArray == null) {
                serverArray = new LongSparseArray<>();
                attachments.append(serverIndex, serverArray);
            }
            serverArray.append(attachId, attachment);
        }


        public static void upload(final int serverIndex, @NonNull final SplashAttachment attachment, @NonNull final OnUploadCompleteListener listener) {
            ThreadHelper uploader = new ThreadHelper(serverIndex, "upload", true) {
                @Override
                protected void workOutput(HttpURLConnection webservice) throws IOException {
                    OutputStream rawOutputStream = webservice.getOutputStream();
                    rawOutputStream.write(String.format("Content-Disposition: form-data; name=\"attach\"; filename=\"%s\"\r\n", attachment.name).getBytes());
                    rawOutputStream.write("Content-Transfer-Encoding: binary\r\n".getBytes());
                    rawOutputStream.write(String.format("Content-Type: %s\r\n\r\n", attachment.mimeType).getBytes());
                    if (attachment.type == SplashAttachment.IMAGE) {
                        ((Bitmap) attachment.data).compress(Bitmap.CompressFormat.PNG, 100, rawOutputStream);
                    } else {
                        rawOutputStream.write((byte[]) attachment.data);
                    }
                    rawOutputStream.write("\r\n".getBytes());
                    rawOutputStream.flush();
                }

                @Override
                protected void doWork(@Nullable JSONObject jsonObject) {
                    long attachId = -1;
                    if (jsonObject != null) {
                        try {
                            attachId = jsonObject.getLong("attachid");
                            setAttachment(serverIndex, attachId, attachment);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    listener.onUpload(attachId);
                }
            };
            GlobalFunctions.executor.execute(uploader);
        }
    }
}