package com.simon;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 */
public class ExternalView extends TextView {
    public ExternalView(Context context) {
        this(context, null, 0);
    }

    public ExternalView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ExternalView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setText("I'm a custom view in an external dex");
    }
}
