package vishal.chetan.splash;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.LongSparseArray;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Stack;

import vishal.chetan.splash.asyncs.AsyncHelper;

import static android.content.ContentValues.TAG;

public class SplashCache {
    public static class UsersCache {
        public interface OnGetUserListener {
            void onGetUser(UserIdentity user);
        }

        private static final SparseArray<LongSparseArray<UserIdentity>> usernames = new SparseArray<>();

        @Nullable
        public static UserIdentity getUser(final int serverIndex, final long uid, @Nullable final OnGetUserListener listener) {
            Log.e(TAG, String.format("getting uid:%s server=%s", uid, serverIndex));
            LongSparseArray<UserIdentity> serverArray = usernames.get(serverIndex);
            UserIdentity user = null;
            if (serverArray == null) {
                serverArray = new LongSparseArray<>();
                usernames.append(serverIndex, serverArray);
            } else {
                user = serverArray.get(uid);
            }
            if (user == null) {
                new AsyncHelper(serverIndex, "getuser/" + uid) {
                    @Override
                    protected void onPostExecute(@Nullable JSONObject jsonObject) {
                        if (jsonObject != null) {
                            try {
                                byte[] blob = Base64.decode(jsonObject.getString("profpic"), Base64.DEFAULT);
                                Bitmap profpic = BitmapFactory.decodeByteArray(blob, 0, blob.length);
                                UserIdentity fetcheduser = new UserIdentity(jsonObject.getLong("uid"),
                                        jsonObject.getString("username"), jsonObject.getString("firstname"),
                                        jsonObject.getString("lastname"), jsonObject.getString("email"), profpic);
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
                }.execute();
            } else if (listener != null) {
                listener.onGetUser(user);
            }
            return user;
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
        private static OnThreadModifiedListener modifyListener;

        @Nullable
        static ArrayList<Thread> getAllForIndex(final int filterIndex) {
            //// FIXME: 3/13/17 demo code to be removed
            add(new Thread(0, 0, "Hello", "Welcome to `Splash app`! Visit https://github.com/vishalbiswas/splash to know more.\n\n Have fun!", 1, new Date(), new Date(), 0));
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
            String postMessage = String.format("title=%s&content=%s&author=%s&topicid=%s", thread.getTitle(), thread.getRawContent(), thread.getCreatorID(), thread.getTopicId());
            if (thread.getAttachId() >= 0) {
                postMessage = String.format("%s&attachid=%s", postMessage, thread.getAttachId());
            }
            new AsyncHelper(thread.getServerIndex(), "post", postMessage) {
                @Override
                protected void onPostExecute(@Nullable JSONObject jsonObject) {
                    Thread newThread = null;
                    if (jsonObject != null) {
                        try {
                            newThread = new Thread(jsonObject.getLong("threadid"), thread.getTitle(),
                                    thread.getRawContent(), thread.getCreatorID(), new Date(jsonObject.getString("ctime")),
                                    new Date(jsonObject.getString("mtime")), thread.getServerIndex(),
                                    thread.getTopicId(), thread.getAttachId());
                            LongSparseArray<Thread> threadList = threads.get(thread.getServerIndex());
                            if (threadList == null) {
                                threadList = new LongSparseArray<>();
                                threadList.append(newThread.getThreadId(), newThread);
                                threads.append(newThread.getServerIndex(), threadList);
                            } else {
                                threadList.append(newThread.getThreadId(), newThread);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d(TAG, "Unable to add thread");
                    }
                    if (modifyListener != null) {
                        modifyListener.onModify(newThread);
                        modifyListener = null;
                    }
                }
            }.execute();
        }

        public static void set(@NonNull final Thread thread) {
            String postMessage = String.format("threadid=%s&title=%s&content=%s&author=%s&topicid=%s", thread.getThreadId(), thread.getTitle(), thread.getRawContent(), thread.getCreatorID(), thread.getTopicId());
            if (thread.getAttachId() >= 0) {
                postMessage = String.format("%s&attachid=%s", postMessage, thread.getAttachId());
            }
            new AsyncHelper(thread.getServerIndex(), "editpost", postMessage) {
                @Override
                protected void onPostExecute(@Nullable JSONObject jsonObject) {
                    Thread newThread = null;
                    if (jsonObject != null) {
                        newThread = thread;
                        try {
                            newThread.setMtime(new Date(jsonObject.getString("mtime")));
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
                    if (modifyListener != null) {
                        modifyListener.onModify(newThread);
                        modifyListener = null;
                    }
                }
            }.execute();
        }

        public static Thread getThread(int serverIndex, long threadId) {
            return threads.get(serverIndex).get(threadId);
        }

        public static void setOnThreadModifyListener(OnThreadModifiedListener listener) {
            modifyListener = listener;
        }
    }

    public static class ImageCache {
        public interface OnUploadCompleteListener {
            void onUpload(long attachId);
        }

        private final static SparseArray<LongSparseArray<Bitmap>> images = new SparseArray<>();

        public static Bitmap get(int serverIndex, long attachId) {
            if (attachId < 0) {
                return null;
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
                loadImage(serverIndex, attachId);
            }
            return image;
        }

        static private void loadImage(final int serverIndex, final long attachId) {
            new AsyncHelper(serverIndex, "attachment/" + attachId) {
                @Override
                protected void onPostExecute(@Nullable JSONObject jsonObject) {
                    if (jsonObject != null) {
                        try {
                            byte[] blob = Base64.decode(jsonObject.getString("image"), Base64.DEFAULT);
                            Bitmap image = BitmapFactory.decodeByteArray(blob, 0, blob.length);
                            setImage(serverIndex, attachId, image);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.e(TAG, "Unknown error");
                    }
                }
            }.execute();
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
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.PNG, 90, out);
            new AsyncHelper(serverIndex, "upload", "attach=" + Base64.encodeToString(out.toByteArray(), Base64.DEFAULT)) {
                @Override
                protected void onPostExecute(@Nullable JSONObject jsonObject) {
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
            }.execute();
        }
    }
}