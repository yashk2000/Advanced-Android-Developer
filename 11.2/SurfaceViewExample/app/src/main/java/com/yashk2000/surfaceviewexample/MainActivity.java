package com.yashk2000.surfaceviewexample;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import static android.view.View.SYSTEM_UI_FLAG_FULLSCREEN;

public class MainActivity extends AppCompatActivity {

    private GameView mGameView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mGameView = new GameView(this);
        mGameView.setSystemUiVisibility(SYSTEM_UI_FLAG_FULLSCREEN);
        setContentView(mGameView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGameView.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGameView.resume();
    }
}