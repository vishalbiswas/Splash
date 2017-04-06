package vishal.chetan.splash;

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

        String name;
        String url;
        boolean disabled = false;

        public SplashSource(String name, String url) {
            this.name = name;
            this.url = url;
        }

        public void setDisabled(boolean disabled) {
            this.disabled = disabled;
        }

        public boolean isDisabled() {
            return disabled;
        }
    }

    public interface ServerListChangeListener {
        enum SourceOperation {
            ADD, UPDATE, DELETE
        }
        void onChange(String name, String url, SourceOperation sourceOperation);
    }

    private static ServerList instance;
    private final List<ServerListChangeListener> changeListeners = new ArrayList<>();

    private ServerList() {}

    @Override
    public SplashSource set(int index, SplashSource element) {
        SplashSource previousElement = super.set(index, element);
        for (ServerListChangeListener changelistener:
                changeListeners) {
            changelistener.onChange(element.getName(), element.getUrl(), ServerListChangeListener.SourceOperation.UPDATE);
        }
        return previousElement;
    }

    public static ServerList getInstance() {
        if (instance == null) {
            instance = new ServerList();
        }
        return instance;
    }

    public void addChangeListener(ServerListChangeListener changeListener) {
        changeListeners.add(changeListener);
    }

    @Override
    public boolean add(SplashSource s) {
        boolean val = super.add(s);
        for (ServerListChangeListener changelistener:
             changeListeners) {
            changelistener.onChange(s.getName(), s.getUrl(), ServerListChangeListener.SourceOperation.ADD);
        }
        return val;
    }

    @Override
    public SplashSource remove(int index) {
        SplashSource s = super.remove(index);
        for (ServerListChangeListener changelistener:
                changeListeners) {
            changelistener.onChange(s.getName(), s.getUrl(), ServerListChangeListener.SourceOperation.DELETE);
        }
        return s;
    }
}