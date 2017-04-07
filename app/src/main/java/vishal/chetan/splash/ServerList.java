package vishal.chetan.splash;

import android.util.SparseArray;

import java.util.ArrayList;
import java.util.List;


public class ServerList extends ArrayList<ServerList.SplashSource> {
    public static class SplashSource {
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


        public boolean isDisabled() {
            return disabled;
        }

        private String name;
        private String url;
        protected boolean disabled = false;
        private SparseArray<String> topics = new SparseArray<>();

        public SplashSource(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public void addTopic(int topicId, String topicName) {
            topics.append(topicId, topicName);
        }
    }

    public interface OnServerListChangeListener {
        enum SourceOperation {
            ADD, UPDATE, DELETE
        }
        void onChange(SplashSource source, SourceOperation sourceOperation);
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
        for (OnServerListChangeListener changelistener:
                changeListeners) {
            changelistener.onChange(element, OnServerListChangeListener.SourceOperation.UPDATE);
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
            changelistener.onChange(s, OnServerListChangeListener.SourceOperation.ADD);
        }
        return val;
    }

    @Override
    public SplashSource remove(int index) {
        SplashSource s = super.remove(index);
        for (OnServerListChangeListener changelistener:
                changeListeners) {
            changelistener.onChange(s, OnServerListChangeListener.SourceOperation.DELETE);
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