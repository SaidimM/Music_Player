package com.hq.hqmusic.UI;

import android.media.MediaPlayer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.hq.hqmusic.Lyric.LrcHandle;
import com.hq.hqmusic.Lyric.WordView;
import com.hq.hqmusic.R;
import com.hq.hqmusic.Utils.MusicUtils;
import com.hq.hqmusic.neteasy_lyc.kugou_lyric;

import java.util.List;
import java.util.Random;

import static com.hq.hqmusic.UI.MainActivity.Button_play;
import static com.hq.hqmusic.UI.MainActivity.currentposition;
import static com.hq.hqmusic.UI.MainActivity.list;
import static com.hq.hqmusic.UI.MainActivity.mplayer;
import static com.hq.hqmusic.UI.MainActivity.play_style;
import static com.hq.hqmusic.UI.MainActivity.seekBar;
import static com.hq.hqmusic.UI.MainActivity.textView1;
import static com.hq.hqmusic.UI.MainActivity.textView2;

public class Main2Activity extends AppCompatActivity {
    boolean ischanging = false;
    private Thread thread;
    SeekBar SeekBar;
    private Button last,play,next;
    private TextView artist,name,album,time,full_time;
    ImageView albumTag;
    WordView mWordView;
    List<Integer> mTimeList;
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
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
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
        full_time.setText(MusicUtils.formatTime(duration));
        if(mplayer.isPlaying()){
            SeekBar.setProgress(mplayer.getCurrentPosition());
            thread = new Thread(new SeekBarThread());
            thread.start();
        }
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
                    setLyric();
                } else {
                    frontMusic();
                    setLyric();
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
                    setLyric();
                } else {
                    nextMusic();
                    setLyric();
                }
            }
        });
        //sdcard/Download/lyric/Good Time.lrc
        setLyric();
        albumTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                albumTag.setVisibility(View.INVISIBLE);
                mWordView.setVisibility(View.VISIBLE);
            }
        });
        mWordView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mWordView.setVisibility(View.INVISIBLE);
                albumTag.setVisibility(View.VISIBLE);
            }
        });


    }


    private void musicplay(int position) {
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
    private String cut_song_name(String name) {
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
    class SeekBarThread implements Runnable {

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
    }

    // 上一曲
    private void frontMusic() {
        currentposition--;
        if (currentposition < 0) {
            currentposition = list.size() - 1;
        }
        musicplay(currentposition);
    }
    // 随机播放下一曲
    private void random_nextMusic() {
        currentposition = currentposition + random.nextInt(list.size() - 1);
        currentposition %= list.size();
        musicplay(currentposition);
    }
    private void setLyric(){
        Name=cut_song_name(list.get(currentposition).getSong()).trim();
        sPath ="sdcard/Download/lyric/"+Name+".lrc";
        final LrcHandle lrcHandler = new LrcHandle();
        kugou_lyric.searchLyric(cut_song_name(list.get(currentposition).getSong()).trim(),list.get(currentposition).getDuration()+"");
        mWordView = (WordView) findViewById(R.id.text);
        mWordView.invalidate();
        System.out.println("##############################################"+sPath);
        try {
            lrcHandler.readLRC(sPath);
            mTimeList = lrcHandler.getTime();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        final Handler handler = new Handler();
        mplayer.start();
        new Thread(new Runnable() {
            int i = 0;

            @Override
            public void run() {
                while (mplayer.isPlaying()) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            mWordView.invalidate();
                        }
                    });
                    try {
                        Thread.sleep(mTimeList.get(i + 1) - mTimeList.get(i));
                    } catch (InterruptedException e) {
                    }
                    i++;
                    if (i == mTimeList.size() - 1) {
                        mplayer.stop();
                        break;
                    }
                }
            }
        }).start();

    }


}
