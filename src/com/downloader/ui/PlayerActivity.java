
package com.downloader.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;

import java.io.File;

/**
 * <title></title>
 * 
 * @author XiangYuan
 * @version 0.1
 * @time 2011-12-5
 * @mailto liyj2@wondershare.cn
 */
public class PlayerActivity extends Activity {
//
//    /**
//     * 播放实例
//     */
//    private MediaPlayer mMediaPlayer = null;
//
//    /**
//     * 
//     */
//    private SurfaceView surfaceView = null;
//
//    /**
//     * 
//     */
//    private SurfaceHolder surfaceHolder = null;

    // private MediaController controller = null;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vedio);
        // surfaceView = (SurfaceView) findViewById(R.id.surface);
        // surfaceHolder = surfaceView.getHolder();
        // surfaceHolder.addCallback(this);
        // // 缓存模式
        // surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // Intent i = new Intent(Intent.ACTION_VIEW,Uri.parse(getFilePath()));
        // i.setType("video/*");
        // startActivity(i);
    }

    public void btnOclick(View v) {
        Log.d("liyajie", "sljldsjflds");
        if (v.getId() == R.id.btn_onclick) {
            Uri uri = Uri.fromFile(new File(getFilePath()));
            Intent i = new Intent(Intent.ACTION_VIEW,uri );
            i.setDataAndType(uri, "video/*"); 
            startActivity(i);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (mMediaPlayer != null) {
//            mMediaPlayer.release();
//            mMediaPlayer = null;
//        }
        finish();
    }

    /**
     * sdcard 文件路径
     * 
     * @return
     */
    public String getFilePath() {
        String path = Environment.getExternalStorageDirectory().getPath();
        return (path + File.separator + "beijinhuanyingni.mp4");
    }
    // @Override
    // public void surfaceChanged(SurfaceHolder holder, int format, int width,
    // int height) {
    //
    // }
    //
    // @Override
    // public void surfaceCreated(SurfaceHolder holder) {
    // try {
    // playVideo();
    // } catch (IllegalArgumentException e) {
    // e.printStackTrace();
    // } catch (IllegalStateException e) {
    // e.printStackTrace();
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // }
    //
    // @Override
    // public void surfaceDestroyed(SurfaceHolder holder) {
    //
    // }

    // private void playVideo() throws IllegalArgumentException,
    // IllegalStateException, IOException {
    // mMediaPlayer = new MediaPlayer();
    // mMediaPlayer.setDataSource(getFilePath());
    // mMediaPlayer.setDisplay(this.surfaceHolder);
    // mMediaPlayer.prepare();
    // mMediaPlayer.setOnBufferingUpdateListener(this);
    // mMediaPlayer.setOnPreparedListener(this);
    // mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    // Log.v("mplayer", ">>>play video");
    // }
    //
    // private int videoWidth, videoHeight;
    //
    // // 播放视频部分
    // @Override
    // public void onPrepared(MediaPlayer mp) {
    // this.videoWidth = mMediaPlayer.getVideoWidth();
    // this.videoHeight = mMediaPlayer.getVideoHeight();
    //
    // if (this.videoHeight != 0 && this.videoWidth != 0) {
    // this.surfaceHolder.setFixedSize(this.videoHeight,this.videoWidth);
    // mMediaPlayer.start();
    // }
    // }
    //
    // @Override
    // public void onCompletion(MediaPlayer mp) {
    //
    // }
    //
    // @Override
    // public void onBufferingUpdate(MediaPlayer mp, int percent) {
    //
    // }
}
