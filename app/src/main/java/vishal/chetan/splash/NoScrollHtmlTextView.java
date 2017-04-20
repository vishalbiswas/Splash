package vishal.chetan.splash;

import android.content.Context;
import android.util.AttributeSet;

import org.sufficientlysecure.htmltextview.HtmlTextView;

public class NoScrollHtmlTextView extends HtmlTextView {
    public NoScrollHtmlTextView(Context context) {
        super(context);
    }

    public NoScrollHtmlTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoScrollHtmlTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void scrollTo(int x, int y) {
        //do nothing
    }
}
