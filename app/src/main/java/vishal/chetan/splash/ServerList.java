package vishal.chetan.splash;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;


public class ServerList extends ArrayList<ServerList.SplashSource> {
    public static class SplashSource {

        public boolean isDisposed() {
            return disposed;
        }

        private boolean disposed = false;

        public class SplashTopic {
            public final int topicid;
            public final String name;

            public SplashTopic(int topicid, String name) {
                this.topicid = topicid;
                this.name = name;
            }
        }

        public enum SessionState {
            ALIVE,
            DEAD,
            UNKNOWN
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @Nullable
        public String getTopic(int topicid) {
            for (SplashTopic topic : this.topics) {
                if (topic.topicid == topicid) {
                    return topic.name;
                }
            }
            return null;
        }

        @NonNull
        public ArrayList<String> getTopics() {
            ArrayList<String> topics = new ArrayList<>();
            for (SplashTopic topic : this.topics) {
                topics.add(topic.name);
            }
            return topics;
        }

        public boolean isEnabled() {
            return !disabled;
        }

        private String name;
        private String url;
        boolean disabled = false;
        public final ArrayList<SplashTopic> topics = new ArrayList<>();
        public UserIdentity identity = null;
        public SessionState session = SessionState.UNKNOWN;
        public Bitmap banner = null;

        public SplashSource(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public int getTopicIndex(int topicid) {
            for (int index = 0; index < topics.size(); ++index) {
                if (topics.get(index).topicid == topicid) {
                    return index;
                }
            }
            return -1;
        }

        public void addTopic(int topicId, String topicName) {
            topics.add(new SplashTopic(topicId, topicName));
        }

        public void clearTopics() {
            topics.clear();
        }

        public void addTopic(SplashTopic topic) {
            topics.add(topic);
        }

        public void dispose() {
            disabled = true;
            disposed = true;
        }
    }

    public interface OnServerListChangeListener {
        void onAdd(SplashSource source);
        void onRemove(SplashSource source);
        void onUpdate(SplashSource previousSource, SplashSource updatedSource);
    }

    public interface OnServerEnabledListener {
        void onEnabledChanged(int serverIndex, boolean enabled);
    }

    private static ServerList instance;
    private final List<OnServerListChangeListener> changeListeners = new ArrayList<>();
    private final List<OnServerEnabledListener> enabledListeners = new ArrayList<>();

    private ServerList() {}


    @NonNull
    public ArrayList<SplashSource> getEnabled() {
        ArrayList<SplashSource> enabledSources = new ArrayList<>();
        for(SplashSource source : enabledSources) {
            if (source.isEnabled()) {
                enabledSources.add(source);
            }
        }
        return enabledSources;
    }

    public SplashSource set(int index, SplashSource element) {
        SplashSource previousElement = super.set(index, element);
        for (OnServerListChangeListener changelistener : changeListeners) {
            changelistener.onUpdate(previousElement, element);
        }
        return previousElement;
    }

    public static ServerList getInstance() {
        if (instance == null) {
            instance = new ServerList();
        }
        return instance;
    }

    public void addListener(OnServerListChangeListener changeListener) {
        changeListeners.add(changeListener);
    }

    public void addListener(OnServerEnabledListener enabledListener) {
        enabledListeners.add(enabledListener);
    }

    @Override
    public boolean add(SplashSource s) {
        boolean success = super.add(s);
        for (OnServerListChangeListener changelistener:
             changeListeners) {
            changelistener.onAdd(s);
        }
        return success;
    }

    @Override
    public SplashSource remove(int index) {
        SplashSource s = instance.get(index);
        setDisabled(index, true);
        instance.get(index).dispose();
        for (OnServerListChangeListener changelistener:
                changeListeners) {
            changelistener.onRemove(s);
        }
        return s;
    }

    public void setDisabled(int index, boolean disabled) {
        if (instance.get(index).disabled != disabled) {
            instance.get(index).disabled = disabled;
            for (OnServerEnabledListener listener : enabledListeners) {
                listener.onEnabledChanged(index, !disabled);
            }
        }
    }
}