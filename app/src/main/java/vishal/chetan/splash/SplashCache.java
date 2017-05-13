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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

import vishal.chetan.splash.asyncs.ThreadHelper;

import static android.content.ContentValues.TAG;

public class SplashCache {
    public static class UsersCache {
        public interface OnGetUserListener {
            void onGetUser(UserIdentity user);
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
                                    jsonObject.getString("username"), jsonObject.getString("fname"),
                                    jsonObject.getString("lname"), jsonObject.getString("email"));
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
                    returnList.add(threadList.valueAt(i));
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
            String postMessage = String.format("title=%s&content=%s&author=%s&topicid=%s", thread.getTitle(), thread.getRawContent(), thread.getCreatorID(), thread.getTopicId());
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
                                    thread.getTopicId(), thread.getAttachId());
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
            String postMessage = String.format("title=%s&content=%s&author=%s&topicid=%s", thread.getTitle(), thread.getRawContent(), thread.getCreatorID(), thread.getTopicId());
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
                            newThread.setMtime(GlobalFunctions.parseDate(jsonObject.getString("mtime")));
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

    public static class ImageCache {
        public interface OnUploadCompleteListener {
            void onUpload(long attachId);
        }

        public interface OnGetImageListener {
            void onGetImage(final Bitmap image);
        }

        private final static SparseArray<LongSparseArray<Bitmap>> images = new SparseArray<>();

        public static void get(int serverIndex, long attachId, @NonNull OnGetImageListener listener) {
            if (attachId < 0) {
                return;
            }
            LongSparseArray<Bitmap> serverArray = images.get(serverIndex);
            Bitmap image = null;
            if (serverArray == null) {
                serverArray = new LongSparseArray<>();
                images.append(serverIndex, serverArray);
            } else {
                image = serverArray.get(attachId);
            }
            if (image == null) {
                loadImage(serverIndex, attachId, listener);
            } else {
                listener.onGetImage(image);
            }
        }

        static private void loadImage(final int serverIndex, final long attachId, @NonNull final OnGetImageListener listener) {
            Runnable loader = new ThreadHelper(serverIndex, "attachment/" + attachId) {
                @Nullable
                Bitmap image = null;

                @Override
                protected void doWork(@Nullable JSONObject jsonObject) {
                    if (jsonObject != null) {
                        try {
                            if (jsonObject.getInt("status") != 0) {
                                Log.e(TAG, "Fetch image failed");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e(TAG, "Unknown error");
                    }
                    listener.onGetImage(image);
                }

                @Override
                protected JSONObject workInput(InputStream rawInputStream) throws JSONException {
                    image = BitmapFactory.decodeStream(rawInputStream);
                    setImage(serverIndex, attachId, image);
                    return new JSONObject("{status:0}");
                }
            };
            GlobalFunctions.executor.execute(loader);
        }

        static void setImage(int serverIndex, long attachId, Bitmap image) {
            if (attachId < 0) {
                return;
            }
            LongSparseArray<Bitmap> serverArray = images.get(serverIndex);
            if (serverArray == null) {
                serverArray = new LongSparseArray<>();
                images.append(serverIndex, serverArray);
            }
            serverArray.append(attachId, image);
        }


        public static void upload(final int serverIndex, @NonNull final Bitmap image, @NonNull final OnUploadCompleteListener listener) {
            ThreadHelper uploader = new ThreadHelper(serverIndex, "upload", true) {
                @Override
                protected void workOutput(@NonNull OutputStream rawOutputStream) throws IOException {
                    rawOutputStream.write("Content-Disposition: form-data; name=\"attach\"; filename=\"attach.png\"\r\n".getBytes());
                    rawOutputStream.write("Content-Type: image/png\r\n".getBytes());
                    rawOutputStream.write("Content-Transfer-Encoding: binary\r\n\r\n".getBytes());
                    image.compress(Bitmap.CompressFormat.PNG, 100, rawOutputStream);
                    rawOutputStream.write("\r\n".getBytes());
                    rawOutputStream.flush();
                }

                @Override
                protected void doWork(@Nullable JSONObject jsonObject) {
                    long attachId = -1;
                    if (jsonObject != null) {
                        try {
                            attachId = jsonObject.getLong("attachid");
                            setImage(serverIndex, attachId, image);
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