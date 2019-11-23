package com.example.android.simplevideoview;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.webkit.URLUtil;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

public class MainActivity extends AppCompatActivity {
    private static final String VIDEO_SAMPLE =
            "https://developers.google.com/training/images/tacoma_narrows.mp4";

    private VideoView mVideoView;
    private TextView mBufferingTextView;

    private int mCurrentPosition = 0;

    private static final String PLAYBACK_TIME = "play_time";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVideoView = findViewById(R.id.videoview);
        mBufferingTextView = findViewById(R.id.buffering_textview);

        if (savedInstanceState != null) {
            mCurrentPosition = savedInstanceState.getInt(PLAYBACK_TIME);
        }

        MediaController controller = new MediaController(this);
        controller.setMediaPlayer(mVideoView);
        mVideoView.setMediaController(controller);
    }


    @Override
    protected void onStart() {
        super.onStart();
        initializePlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            mVideoView.pause();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(PLAYBACK_TIME, mVideoView.getCurrentPosition());
    }

    private void initializePlayer() {
        mBufferingTextView.setVisibility(VideoView.VISIBLE);

        Uri videoUri = getMedia(VIDEO_SAMPLE);
        mVideoView.setVideoURI(videoUri);

        mVideoView.setOnPreparedListener(
                new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                mBufferingTextView.setVisibility(VideoView.INVISIBLE);

                if (mCurrentPosition > 0) {
                    mVideoView.seekTo(mCurrentPosition);
                } else {
                    mVideoView.seekTo(1);
                }
                mVideoView.start();
            }
        });
        mVideoView.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                Toast.makeText(MainActivity.this,
                        R.string.toast_message,
                        Toast.LENGTH_SHORT).show();

                mVideoView.seekTo(0);
            }
        });
    }

    private void releasePlayer() {
        mVideoView.stopPlayback();
    }

    private Uri getMedia(String mediaName) {
        if (URLUtil.isValidUrl(mediaName)) {
            return Uri.parse(mediaName);
        } else {
            return Uri.parse("android.resource://" + getPackageName() +
                    "/raw/" + mediaName);
        }
    }
}
