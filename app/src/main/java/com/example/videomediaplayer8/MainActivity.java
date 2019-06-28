package com.example.videomediaplayer8;

import android.app.Activity;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.PlaybackParams;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;



public class MainActivity extends Activity implements SurfaceHolder.Callback {

    public static final int SEEKBARUPDATE = 1;
    public static final int SEEKVIDEO = 2;


    Handler mHandler = new MyHandler();
    private MediaPlayer mediaPlayer;
    private PlaybackParams playbackParams;
    //视频播放视图
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private TextView maxTime, curTime, infoText;
    private SeekBar seekBar;
    private RelativeLayout rlControl;
    private Format format = new SimpleDateFormat("mm:ss");
    //定时器，统计运行时间
    private Timer timer;
    private MyTimeTask myTimeTask;

    private int currentPosition = 0;
    private int duration = 0;
    private float volume =0.3f;

    float speeds[]={0.5f,1.0f,1.5f,2.0f,4.0f};
    int speedIndex = 1;

    Point fixSize;//存储原始屏幕大小
    private boolean fullScreen = false;
    private boolean controlsHide = false;

    DisplayMetrics disPlay = new DisplayMetrics();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindowManager().getDefaultDisplay().getMetrics(disPlay);
        bindViews();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        fixSize = new Point(surfaceView.getWidth(),surfaceView.getHeight());

        //设置媒体资源
        mediaPlayer = MediaPlayer.create(MainActivity.this, R.raw.test);

        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setDisplay(surfaceHolder);
        mediaPlayer.setVolume(volume,volume);

        mediaPlayer.setLooping(true);

        //设置倍速
        playbackParams = mediaPlayer.getPlaybackParams();
        playbackParams.setSpeed(speeds[speedIndex]);
        mediaPlayer.setPlaybackParams(playbackParams);

        seekBar.setMax(100);
        duration = mediaPlayer.getDuration();

        //线程刷新UI
        timer = new Timer();
        myTimeTask = new MyTimeTask();
        myTimeTask.setSpeed(duration/100);
        timer.schedule(myTimeTask,50,50);
        maxTime.setText(format.format(duration) + "");
    }


    private void bindViews() {
        Log.d("MyVideo", "View Size: ");
        surfaceView = findViewById(R.id.Suf_id);
        seekBar = findViewById(R.id.SeekBar_id);
        seekBar.setFocusable(false);
        curTime = findViewById(R.id.curTime_id);
        maxTime = findViewById(R.id.maxTime_id);
        infoText = findViewById(R.id.info_id);
        rlControl = findViewById(R.id.control_id);

        //surfaceHolder类可认为是surface类的控制器
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){

        Log.d("MyVideo","keyDown: "+keyCode);
        switch (keyCode){
            case KeyEvent.KEYCODE_5:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if(mediaPlayer.isPlaying()){
                    mediaPlayer.pause();
                    infoText.setText("暂停");
                }else{
                    mediaPlayer.start();
                    infoText.setText("播放");
                }
                break;
                //向右快进
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                Log.d("MyVideo","parse down right");
                myTimeTask.setSpeedDirection(1);
                infoText.setText("快进");
                break;
                //向左快退
            case KeyEvent.KEYCODE_DPAD_LEFT:
                myTimeTask.setSpeedDirection(-1);
                infoText.setText("快退");
                break;

            case KeyEvent.KEYCODE_1:
                try {
                    if(++speedIndex >= 5){
                        speedIndex = 0;
                    }
                    if(mediaPlayer == null){
                        return false;
                    }
                    Log.d("MyVideo","the speedIndex is: "+speedIndex);
                    playbackParams.setSpeed(speeds[speedIndex]);
                    mediaPlayer.setPlaybackParams(playbackParams);
                    infoText.setText("X"+speeds[speedIndex]);
                } catch (Exception e) {
                    // TODO: handle exception
                    Log.d("MyVideo","Exception in MessageQueue callback: handleReceiveCallback"+e);
                }
                break;

            case KeyEvent.KEYCODE_2:
                Log.d("MyVideo","parse 2 ");
                if(!fullScreen){
                    //全屏
                    RelativeLayout.LayoutParams layoutParams=
                            new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.MATCH_PARENT);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
                    layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                    surfaceView.setLayoutParams(layoutParams);
                    fullScreen = true;
                    infoText.setText("全屏");
                }else {
                    RelativeLayout.LayoutParams lp=new  RelativeLayout.LayoutParams(fixSize.x, fixSize.y);
                    lp.addRule(RelativeLayout.CENTER_IN_PARENT);
                    surfaceView.setLayoutParams(lp);
                    fullScreen = false;//改变全屏/窗口的标记
                    infoText.setText("小屏");
                }
                break;
            case KeyEvent.KEYCODE_3:
                if(!controlsHide){
                    rlControl.setVisibility(View.GONE);
                    infoText.setVisibility(View.GONE);
                    controlsHide = true;
                }else{
                    rlControl.setVisibility(View.VISIBLE);
                    infoText.setVisibility(View.VISIBLE);
                    controlsHide = false;
                }
                break;
            case KeyEvent.KEYCODE_4:
                volume+=0.01f;
                if(volume > 1.0f){
                    volume = 1.0f;
                }
                mediaPlayer.setVolume(volume,volume);
                infoText.setText("音量+："+(int)(volume*100));
                break;
            case KeyEvent.KEYCODE_7:
                volume-=0.01f;
                if(volume <= 0.0f){
                    volume = 0.0f;
                }
                mediaPlayer.setVolume(volume,volume);
                infoText.setText("音量+："+(int)(volume*100));
                break;
            case KeyEvent.KEYCODE_0:
                volume = 0.0f;
                mediaPlayer.setVolume(volume,volume);
                infoText.setText("音量：X");
//            case KeyEvent.KEYCODE_BACK:
//                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event){

        Log.d("MyVideo","keyUp: "+keyCode);
        switch (keyCode){

            //向右快进
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                myTimeTask.setSpeedDirection(0);
                break;
            //向左快退
            case KeyEvent.KEYCODE_DPAD_LEFT:
                Log.d("MyVideo","keyDown: "+keyCode);
                myTimeTask.setSpeedDirection(0);
                break;
            case KeyEvent.KEYCODE_BACK:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }


    public void onSeekBarChanged(int currentPosition, int duration,boolean needSeek){
        if(needSeek){
            mediaPlayer.seekTo(currentPosition);
        }
            Log.d("MyVideo","currentPosition: "+currentPosition);
            seekBar.setProgress(currentPosition*100/duration);

        curTime.setText(format.format(currentPosition));
    }

    private class MyTimeTask extends TimerTask{

        private int speed = 1;
        private int speedDirection = 0;

        public void setSpeedDirection(int speedDirection1){
            speedDirection = speedDirection1;
        }
        public void setSpeed(int speed1){
            speed = speed1;
        }

        @Override
        public void run(){
            if(!mediaPlayer.isPlaying()){
                return;
            }
            Log.d("MyVideo","speed is: "+speed+ " speedDir: "+ speedDirection);
            currentPosition = mediaPlayer.getCurrentPosition() + speed*speedDirection;
            if(currentPosition < 0) currentPosition = 0;
            if(currentPosition > duration) currentPosition = duration;

            Message seekMsg = new Message();
            if(speedDirection == 0){
                seekMsg.what = SEEKBARUPDATE;
                seekMsg.arg1 = currentPosition;
                seekMsg.arg2 = duration;
            }else{
                seekMsg.what = SEEKVIDEO;
                seekMsg.arg1 = currentPosition;
                seekMsg.arg2 = duration;
            }
            mHandler.sendMessage(seekMsg);
        }
    }

    private class MyHandler extends Handler{
        int currentPosition = 0;
        int duration = 0;
        @Override
        public void handleMessage(Message msg){
         switch (msg.what){
             case SEEKBARUPDATE:
                 currentPosition = msg.arg1;
                 duration = msg.arg2;
                 onSeekBarChanged(currentPosition, duration,false);
                 break;
             case SEEKVIDEO:
                 currentPosition = msg.arg1;
                 duration = msg.arg2;
                 onSeekBarChanged(currentPosition,duration,true);
                 break;
         }
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
        myTimeTask.cancel();
        if (mediaPlayer.isPlaying() && mediaPlayer != null) {
            mediaPlayer.stop();
        }
        mediaPlayer.release();
        super.onDestroy();
    }
}
