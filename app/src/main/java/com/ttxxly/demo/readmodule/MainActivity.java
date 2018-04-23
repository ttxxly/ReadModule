package com.ttxxly.demo.readmodule;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.ttxxly.demo.readmodule.utils.ScreenUtils;

public class MainActivity extends AppCompatActivity {

    private FrameLayout ReadWidget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);


        ReadWidget = findViewById(R.id.flReadWidget);
        DrawTextView view = new DrawTextView(this, ScreenUtils.getScreenWidth(this), ScreenUtils.getRealHeight(this));
        ScreenUtils.hideVirtualButtons(this);
        ReadWidget.removeAllViews();
        ReadWidget.addView(view);
    }


}
