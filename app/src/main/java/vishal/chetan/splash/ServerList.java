package vishal.chetan.splash;

import java.util.ArrayList;
import java.util.List;


public class ServerList extends ArrayList<ServerList.SplashSource> {
    public static class SplashSource {
        public class SplashTopic {
            public final int topicid;
            public final String name;

            public SplashTopic(int topicid, String name) {
                this.topicid = topicid;
                this.name = name;
            }
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

        public String getTopic(int topicid) {
            for (SplashTopic topic : this.topics) {
                if (topic.topicid == topicid) {
                    return topic.name;
                }
            }
            return null;
        }

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

        public SplashSource(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public void addTopic(int topicId, String topicName) {
            topics.add(new SplashTopic(topicId, topicName));
        }

        public void addTopic(SplashTopic topic) {
            topics.add(topic);
        }
    }

    public interface OnServerListChangeListener {
        void onAdd(SplashSource source);
        void onRemove(SplashSource source);
        void onUpdate(SplashSource previousSource, SplashSource updatedSource);
    }

    public interface OnServerDisabledListener {
        void onDisabled();
    }

    private static ServerList instance;
    private final List<OnServerListChangeListener> changeListeners = new ArrayList<>();
    private final List<OnServerDisabledListener> disabledListeners = new ArrayList<>();

    private ServerList() {}

    @Override
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

    public void addListener(OnServerDisabledListener disabledListener) {
        disabledListeners.add(disabledListener);
    }

    @Override
    public boolean add(SplashSource s) {
        boolean val = super.add(s);
        for (OnServerListChangeListener changelistener:
             changeListeners) {
            changelistener.onAdd(s);
        }
        return val;
    }

    @Override
    public SplashSource remove(int index) {
        SplashSource s = super.remove(index);
        for (OnServerListChangeListener changelistener:
                changeListeners) {
            changelistener.onRemove(s);
        }
        return s;
    }


    public void setDisabled(int index, boolean disabled) {
        if (super.get(index).disabled != disabled) {
            super.get(index).disabled = disabled;
            for (OnServerDisabledListener listener : disabledListeners) {
                listener.onDisabled();
            }
        }
    }
}