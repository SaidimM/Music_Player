package com.hq.hqmusic.UI;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import com.hq.hqmusic.LYRICS.DefaultLrcParser;
import com.hq.hqmusic.LYRICS.LrcRow;
import com.hq.hqmusic.LYRICS.LrcView;
import com.hq.hqmusic.R;
import com.hq.hqmusic.Utils.MusicUtils;
import com.hq.hqmusic.neteasy_lyc.kugou_lyric;
import com.hq.hqmusic.wakeUp.IWakeupListener;
import com.hq.hqmusic.wakeUp.MyWakeup;
import com.hq.hqmusic.wakeUp.SimpleWakeupListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;
import static com.hq.hqmusic.UI.MainActivity.Button_play;
import static com.hq.hqmusic.UI.MainActivity.currentposition;
import static com.hq.hqmusic.UI.MainActivity.list;
import static com.hq.hqmusic.UI.MainActivity.mplayer;
import static com.hq.hqmusic.UI.MainActivity.play_style;
import static com.hq.hqmusic.UI.MainActivity.seekBar;
import static com.hq.hqmusic.UI.MainActivity.start;
import static com.hq.hqmusic.UI.MainActivity.textView1;
import static com.hq.hqmusic.UI.MainActivity.textView2;

public class Main2Activity extends AppCompatActivity {
    /**    控制播放的SeekBar***/

    /**控制歌词字体大小的SeekBar***/

    MyWakeup myWakeup;
    private static final String TAG="Main2Activity";
    private LrcView mLrcView;
    private Toast mPlayerToast;
    private Toast mLrcToast;
    static boolean ischanging = false;
    private static Thread thread;
    static SeekBar SeekBar;
    private Button last,play,next;
    private static TextView artist;
    private static TextView name;
    private static TextView album;
    private TextView time;
    private TextView full_time;
    ImageView albumTag;
    LinearLayout lrcContainer;
    public static String Name, sPath;
    private Random random = new Random();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        last=(Button)findViewById(R.id.play_last_Button);
        play=(Button)findViewById(R.id.play_Button);
        next=(Button)findViewById(R.id.play_next_Button);
        artist=(TextView)findViewById(R.id.MusicArtistText);
        album=(TextView)findViewById(R.id.MusicAlbumText);
        name=(TextView)findViewById(R.id.MusicNameText);
        SeekBar=(SeekBar)findViewById(R.id.SeekBar);
        time=(TextView)findViewById(R.id.music_time);
        full_time=(TextView)findViewById(R.id.music_all_time);
        albumTag=(ImageView)findViewById(R.id.AlbumTag);
        lrcContainer=(LinearLayout)findViewById(R.id.lrcContainer);
        SeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStopTrackingTouch(SeekBar seekbar) {
                // TODO Auto-generated method stub
                ischanging = false;
                mplayer.seekTo(seekbar.getProgress());
                thread = new Thread(new SeekBarThread());
                thread.start();
                seekBar.setProgress(mplayer.getCurrentPosition());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                ischanging = true;
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // TODO Auto-generated method stub
                // 可以用来写拖动的时候实时显示时间
                String t = MusicUtils.formatTime(progress);
                time.setText(t);
            }
        });
        if(mplayer.isPlaying()){
            play.setText("‖");
        }
        else{
            play.setText("▶");
        }
        name.setText(cut_song_name(list.get(currentposition).getSong()).trim());
        artist.setText(list.get(currentposition).getSinger().trim());
        album.setText(list.get(currentposition).getAlbum().trim());
        SeekBar.setMax(list.get(currentposition).getDuration());
        int duration = list.get(currentposition).getDuration();
        String t = MusicUtils.formatTime(duration);
        time.setText(t);
        full_time.setText(MusicUtils.formatTime(duration));
        if(mplayer.isPlaying()){
            SeekBar.setProgress(mplayer.getCurrentPosition());
            thread = new Thread(new SeekBarThread());
            thread.start();
        }
        setMediaPlayerListener();

        play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mplayer.isPlaying()) {
                    mplayer.pause();
                    play.setText("▶");
                    Button_play.setText("▶");
                } else {
                    mplayer.start();
                    play.setText("‖");
                    Button_play.setText("‖");
                    thread = new Thread(new SeekBarThread());
                    thread.start();
                }
            }
        });
        last.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                play.setText("‖");
                if (play_style == 2) {
                    random_nextMusic();
                } else {
                    frontMusic();
                }
            }
        });
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO Auto-generated method stub
                play.setText("‖");
                if (play_style == 2) {
                    random_nextMusic();
                } else {
                    nextMusic();
                }
            }
        });

        albumTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lrcContainer.setVisibility(View.VISIBLE);
                albumTag.setVisibility(View.INVISIBLE);
            }
        });
        lrcContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lrcContainer.setVisibility(View.INVISIBLE);
                albumTag.setVisibility(View.VISIBLE);
            }
        });
        initViews();
    }
    public static void musicplay(int position) {
        name.setText(cut_song_name(list.get(position).getSong()).trim());
        artist.setText(list.get(position).getSinger().trim());
        album.setText(list.get(position).getAlbum().trim());
        textView1.setText(cut_song_name(list.get(position).getSong()).trim());
        textView2.setText(list.get(position).getSinger().trim());
        SeekBar.setMax(list.get(position).getDuration());
        try {
            mplayer.reset();
            mplayer.setDataSource(list.get(position).getPath());
            mplayer.prepare();
            mplayer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        thread = new Thread(new SeekBarThread());
        thread.start();
    }
    private static String cut_song_name(String name) {
        if (name.length() >= 5
                && name.substring(name.length() - 4, name.length()).equals(
                ".mp3")) {
            return name.substring(0, name.length() - 4);
        }
        return name;
    }
    private void setMediaPlayerListener() {
        // 监听mediaplayer播放完毕时调用
        mplayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                switch (play_style) {
                    case 0:
                        musicplay(currentposition);
                        break;
                    case 1:
                        // 这里会引发初次进入时直接点击播放按钮时，播放的是下一首音乐的问题
                        nextMusic();
                        break;
                    case 2:
                        random_nextMusic();
                        break;
                    default:
                        break;
                }
            }
        });
        // 设置发生错误时调用
        mplayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                // TODO Auto-generated method stub
                mp.reset();
                // Toast.makeText(MainActivity.this, "未发现音乐", 1500).show();
                return false;
            }
        });
    }

    // 自定义的线程,用于下方seekbar的刷新
    static class SeekBarThread implements Runnable {

        @Override
        public void run() {
            while (!ischanging && mplayer.isPlaying()) {
                // 将SeekBar位置设置到当前播放位置
                SeekBar.setProgress(mplayer.getCurrentPosition());
                try {
                    // 每500毫秒更新一次位置
                    Thread.sleep(500);
                    // 播放进度

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 下一曲
    private void nextMusic() {
        currentposition++;
        if (currentposition > list.size() - 1) {
            currentposition = 0;
        }
        musicplay(currentposition);
        mLrcView.setLrcRows(getLrcRows());
    }

    // 上一曲
    private void frontMusic() {
        currentposition--;
        if (currentposition < 0) {
            currentposition = list.size() - 1;
        }
        musicplay(currentposition);
        mLrcView.setLrcRows(getLrcRows());
    }
    // 随机播放下一曲
    private void random_nextMusic() {
        currentposition = currentposition + random.nextInt(list.size() - 1);
        currentposition %= list.size();
        musicplay(currentposition);
        mLrcView.setLrcRows(getLrcRows());
    }

    private void initViews() {
        mLrcView = (LrcView) findViewById(R.id.lrcView);
        mLrcView.setOnSeekToListener(onSeekToListener);
        mLrcView.setOnLrcClickListener(onLrcClickListener);
        SeekBar.setOnSeekBarChangeListener(onSeekBarChangeListener);
        mLrcView.setLrcRows(getLrcRows());
    }
    LrcView.OnLrcClickListener onLrcClickListener = new LrcView.OnLrcClickListener() {

        @Override
        public void onClick() {
            Toast.makeText(getApplicationContext(), "歌词被点击啦", Toast.LENGTH_SHORT).show();
        }
    };
    LrcView.OnSeekToListener onSeekToListener = new LrcView.OnSeekToListener() {
        @Override
        public void onSeekTo(int progress) {
            mplayer.seekTo(progress);
        }
    };
    @SuppressLint("HandlerLeak")
    Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            SeekBar.setMax(mplayer.getDuration());
            SeekBar.setProgress(mplayer.getCurrentPosition());
            handler.sendEmptyMessageDelayed(0, 100);
        };
    };

    android.widget.SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new android.widget.SeekBar.OnSeekBarChangeListener() {

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            if(seekBar == SeekBar){
                mplayer.seekTo(seekBar.getProgress());
                handler.sendEmptyMessageDelayed(0, 100);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            if(seekBar == SeekBar){
                handler.removeMessages(0);
            }
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress,
                                      boolean fromUser) {
            mLrcView.seekTo(progress, true,fromUser);
            if(fromUser){
                showPlayerToast(formatTimeFromProgress(progress));
            }
        }
    };
    /**
     * 将播放进度的毫米数转换成时间格式
     * 如 3000 --> 00:03
     * @param progress
     * @return
     */
    private String formatTimeFromProgress(int progress){
        //总的秒数
        int msecTotal = progress/1000;
        int min = msecTotal/60;
        int msec = msecTotal%60;
        String minStr = min < 10 ? "0"+min:""+min;
        String msecStr = msec < 10 ? "0"+msec:""+msec;
        return minStr+":"+msecStr;
    }
    /**
     * 获取歌词List集合
     * @return
     */
    private List<LrcRow> getLrcRows(){
        List<LrcRow> rows = null;
        Name=cut_song_name(list.get(currentposition).getSong()).trim();
        System.out.println(Environment.getExternalStoragePublicDirectory(""));
        String path= Environment.getExternalStoragePublicDirectory("").getPath();
        sPath =path+"/MyPlayer/"+Name+".lrc";
        File file = new File(sPath);
        if(!file.exists()){
            kugou_lyric.searchLyric(Name,mplayer.getDuration()+"");
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamReader inputStreamReader = new InputStreamReader(
                    fileInputStream, "utf-8");
            BufferedReader br = new BufferedReader(inputStreamReader);
            String line ;
            StringBuffer sb = new StringBuffer();
            while((line = br.readLine()) != null){
                sb.append(line+"\n");
            }
            System.out.println(sb.toString());
            rows = DefaultLrcParser.getIstance().getLrcRows(sb.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rows;
    }

    private TextView mPlayerToastTv;
    private void showPlayerToast(String text){
        if(mPlayerToast == null){
            mPlayerToast = new Toast(this);
            mPlayerToastTv = (TextView) LayoutInflater.from(this).inflate(R.layout.toast, null);
            mPlayerToast.setView(mPlayerToastTv);
            mPlayerToast.setDuration(Toast.LENGTH_SHORT);
        }
        mPlayerToastTv.setText(text);
        mPlayerToast.show();
    }
    private TextView mLrcToastTv;
    private void showLrcToast(String text){
        if(mLrcToast == null){
            mLrcToast = new Toast(this);
            mLrcToastTv = (TextView) LayoutInflater.from(this).inflate(R.layout.toast, null);
            mLrcToast.setView(mLrcToastTv);
            mLrcToast.setDuration(Toast.LENGTH_SHORT);
        }
        mLrcToastTv.setText(text);
        mLrcToast.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeMessages(0);
        mplayer.stop();
        mplayer.release();
        mplayer = null;
        mLrcView.reset();
    }
}
