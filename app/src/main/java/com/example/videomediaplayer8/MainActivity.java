package com.example.videomediaplayer8;

import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity implements View.OnClickListener, SurfaceHolder.Callback {

    private MediaPlayer mediaPlayer;
    //视频播放视图
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Button playBtn, pauseBtn, rePlayBtn;
    private TextView maxTime, curTime;
    private SeekBar seekBar;
    private Format format = new SimpleDateFormat("mm:ss");
    //定时器，统计运行时间
    private Timer timer;
    private int currentPosition = 0;//当前播放进度
    private boolean isSeekBarChanging = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
    }

    private void bindViews() {
        surfaceView = findViewById(R.id.Suf_id);
        playBtn = findViewById(R.id.playBtn_id);
        pauseBtn = findViewById(R.id.pause_id);
        rePlayBtn = findViewById(R.id.rePlayBtn_id);
        seekBar = findViewById(R.id.SeekBar_id);
        seekBar.setOnSeekBarChangeListener(new SeekBarParse());
        curTime = findViewById(R.id.curTime_id);
        maxTime = findViewById(R.id.maxTime_id);

        playBtn.setOnClickListener(this);
        pauseBtn.setOnClickListener(this);
        rePlayBtn.setOnClickListener(this);

        //surfaceHolder类可认为是surface类的控制器
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        //surfaceHolder.setFixedSize(1080,350);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.playBtn_id:
                mediaPlayer.start();
                mediaPlayer.seekTo(currentPosition);

                timer = new Timer();
                timer.schedule(new TimerTask() {
                    Runnable updateUI = new Runnable() {
                        @Override
                        public void run() {
                            curTime.setText(format.format(mediaPlayer.getCurrentPosition()) + "");
                            currentPosition = mediaPlayer.getCurrentPosition();
                        }
                    };
                    @Override
                    public void run() {
                        if (!isSeekBarChanging) {
                            seekBar.setProgress(mediaPlayer.getCurrentPosition());
                            runOnUiThread(updateUI);
                        }
                    }
                }, 0, 3000);//时间间隔一般要求小一点
                playBtn.setEnabled(false);
                break;
            case R.id.pause_id:
                mediaPlayer.pause();
                playBtn.setEnabled(true);
                break;
            case R.id.rePlayBtn_id:
                mediaPlayer.seekTo(0);
                break;
        }

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.test);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDisplay(surfaceHolder);
        mediaPlayer.setLooping(true);
        seekBar.setMax(mediaPlayer.getDuration());
        maxTime.setText(format.format(mediaPlayer.getDuration()) + "");
    }

    public class SeekBarParse implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser){
            isSeekBarChanging = true;
            mediaPlayer.seekTo(seekBar.getProgress());
            isSeekBarChanging = false;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

            //isSeekBarChanging = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
           // mediaPlayer.seekTo(seekBar.getProgress());
           // isSeekBarChanging = false;
        }


    }
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int heigth) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer.isPlaying() && mediaPlayer != null) {
            mediaPlayer.stop();
        }
        mediaPlayer.release();
    }
}
