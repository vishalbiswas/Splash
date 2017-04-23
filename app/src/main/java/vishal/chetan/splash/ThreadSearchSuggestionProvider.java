package vishal.chetan.splash;

import android.content.SearchRecentSuggestionsProvider;


public class ThreadSearchSuggestionProvider extends SearchRecentSuggestionsProvider {
    public final static String AUTHORITY = "vishal.chetan.splash.ThreadSearchSuggestionProvider";
    public final static int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;

    public ThreadSearchSuggestionProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }
}
