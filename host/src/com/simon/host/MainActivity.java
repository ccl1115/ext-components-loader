package com.simon.host;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity implements View.OnClickListener {

    private Class mExternalActivityClass;
    private Class mExternalServiceClass;

    /**
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        findViewById(android.R.id.button1).setOnClickListener(this);
        findViewById(android.R.id.button2).setOnClickListener(this);

        try {
            mExternalActivityClass = getClassLoader().loadClass("com.simon.ExternalActivity");
            mExternalServiceClass = getClassLoader().loadClass("com.simon.ExternalService");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onClick(View v) {
        final int id = v.getId();
        switch (id) {
            case android.R.id.button1:
                if (mExternalActivityClass != null) {
                    Intent i = new Intent(this, mExternalActivityClass);
                    startActivity(i);
                }
                break;
            case android.R.id.button2:
                if (mExternalServiceClass != null) {
                    Intent i = new Intent(this, mExternalServiceClass);
                    startService(i);
                }
                break;
        }
    }
}
