package com.nathankun.buyonsbspricescalculator;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 * 说明页面
 */
public class AboutActivity extends AppCompatActivity {

    /**
     * 进入时将ContentView设为对应的View
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }
}
