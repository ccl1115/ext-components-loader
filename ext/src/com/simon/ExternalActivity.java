package com.simon;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 */
public class ExternalActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.addView(buildTextView());
        linearLayout.addView(buildExternalView());

        setContentView(linearLayout);
    }

    private TextView buildTextView() {
        TextView ret = new TextView(this);
        ret.setText("I'm an activity in an external dex which is loaded at runtime.");
        ret.setGravity(Gravity.CENTER);
        return ret;
    }

    private ExternalView buildExternalView() {
        return new ExternalView(this);
    }
}
