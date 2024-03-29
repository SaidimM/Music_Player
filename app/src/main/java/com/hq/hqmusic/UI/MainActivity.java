package com.hq.hqmusic.UI;

import android.Manifest;
import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.speech.asr.SpeechConstant;
import com.hq.hqmusic.Adapter.MyAdapter;
import com.hq.hqmusic.CustomView.CustomDialog;
import com.hq.hqmusic.CustomView.MyDialog;
import com.hq.hqmusic.Entity.Song;
import com.hq.hqmusic.R;
import com.hq.hqmusic.StatusBar.BaseActivity;
import com.hq.hqmusic.StatusBar.SystemBarTintManager;
import com.hq.hqmusic.Utils.ImageCacheUtil;
import com.hq.hqmusic.Utils.MusicUtils;
import com.hq.hqmusic.wakeUp.IWakeupListener;
import com.hq.hqmusic.wakeUp.MyWakeup;
import com.hq.hqmusic.wakeUp.RecogWakeupListener;
import com.hq.hqmusic.wakeUp.SimpleWakeupListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import kr.co.namee.permissiongen.PermissionFail;
import kr.co.namee.permissiongen.PermissionGen;
import kr.co.namee.permissiongen.PermissionSuccess;

public class MainActivity extends BaseActivity  {

    public static MyWakeup myWakeup;
    private SharedPreferences sharedPreferences;
    private ListView listview,listView1,listView2;
    public static List<Song> list,list1,list2,mainList;
    private static MyAdapter adapter;
    private MyAdapter adapter1;
    private MyAdapter adapter2;
    private PopupWindow popupWindow;
    public static MediaPlayer mplayer = new MediaPlayer();
    private MyDialog myDialog, myDialog_bestlove;
    public static SeekBar seekBar;
    public static TextView textView1, textView2;
    public static Button Button_play;
    public static Button Button_front;
    public static Button Button_next;
    private ImageView imageview_playstyle;
    private LinearLayout to_second;
    private Button popUpMenu;
    private int screen_width;
    private Random random = new Random();
    // 用于判断当前的播放顺序，0->单曲循环,1->顺序播放,2->随机播放
    public static int play_style = 0;
    // 判断seekbar是否正在滑动
    private static boolean ischanging = false;
    private static Thread thread;
    // 当前音乐播放位置,从0开始
    public static int currentposition;
    // 屏幕显示的最大listview条数
    private int max_item;
    // 该字符串用于判断主题
    private String string_theme;
    // 修改顶部状态栏颜色使用
    private SystemBarTintManager mTintManager;
    protected Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //获取权限
        getAuthority();

        sharedPreferences = getSharedPreferences("location", MODE_PRIVATE);
        // 主题设置
        string_theme = sharedPreferences.getString("theme_select", "blue");
        if (string_theme.equals("blue")) {
            setTheme(R.style.Theme_blue);
        } else if (string_theme.equals("purple")) {
            setTheme(R.style.Theme_purple);
        } else if (string_theme.equals("green")) {
            setTheme(R.style.Theme_green);
        } else {
            setTheme(R.style.Theme_red);
        }
        setContentView(R.layout.activity_main);
        to_second=(LinearLayout)findViewById(R.id.touchable_LL);
        // 顶部状态栏颜色设置
        mTintManager = new SystemBarTintManager(MainActivity.this);
        popUpMenu=(Button)findViewById(R.id.popUpMenu);
        mTintManager.setStatusBarTintEnabled(true);
        if (string_theme.equals("blue")) {
            mTintManager.setStatusBarTintResource(R.color.gray);
        } else if (string_theme.equals("purple")) {
            mTintManager.setStatusBarTintResource(R.color.purple);
        } else if (string_theme.equals("green")) {
            mTintManager.setStatusBarTintResource(R.color.green);
        } else {
            mTintManager.setStatusBarTintResource(R.color.red);
        }

        // 获得屏幕宽度并保存在screen_width中
        init_screen_width();


        // 加载currentposition的初始数据
        currentposition = sharedPreferences.getInt("currentposition", 0);

        // 顶部视图控件的绑定
        initTopView();

        // 顶部和 底部操作栏按钮点击事件
        setClick();

        // 给textView1和textView2赋初值
        initText();

        // 加载popupwindow并设置相关属性
        setpopupwindow();

        // listview的绑定,数据加载,以及相关事件的监听
        setListView();

        // 设置mediaplayer监听器
        setMediaPlayerListener();

        initPermission();

        to_second.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this,Main2Activity.class);
                startActivity(intent);
            }
        });
        popUpMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu();
            }
        });
        IWakeupListener listener= new SimpleWakeupListener();
        myWakeup = new MyWakeup(this, listener);
        start();
    }

    @Override
    protected void onDestroy() {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("song_name",
                cut_song_name(list.get(currentposition).getSong()));
        editor.putString("song_singer", list.get(currentposition).getSinger());
        editor.putInt("currentposition", currentposition);
        editor.apply();
        if (mplayer.isPlaying()) {
            mplayer.stop();
        }
        mplayer.release();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 1) {

            Bitmap bitmap = ImageCacheUtil.getResizedBitmap(null, null,
                    MainActivity.this, data.getData(), screen_width, true);
            BitmapDrawable drawable = new BitmapDrawable(null, bitmap);

            listview.setBackground(drawable);
            saveDrawable(drawable);
            myDialog.dismiss();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // 监听返回键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        final CustomDialog customDialog = new CustomDialog(MainActivity.this,
                R.layout.layout_customdialog, R.style.dialogTheme);
        customDialog.setT("系统提示");
        customDialog.setM("确定要退出播放器了吗?");
        customDialog.setButtonLeftText("确定");
        customDialog.setButtonRightText("取消");
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (myDialog.isShowing()) {
                myDialog.dismiss();
            } else if (popupWindow.isShowing()) {
                popupWindow.dismiss();
            } else {
                customDialog.show();
                customDialog.setOnClickBtnListener(new CustomDialog.OnClickBtnListener() {
                    @Override
                    public void onClickOk() {
                        // TODO Auto-generated method stub
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        startActivity(intent);
                        customDialog.dismiss();
                    }

                    @Override
                    public void onClickCancel() {
                        // TODO Auto-generated method stub
                        customDialog.cancel();
                    }
                });
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void showPopupMenu() {
        // 这里的view代表popupMenu需要依附的view
        TextView mPopupMenu=findViewById(R.id.popUpMenu);
        PopupMenu popupMenu = new PopupMenu(this,mPopupMenu);
        // 获取布局文件
        popupMenu.getMenuInflater().inflate(R.menu.menu_test, popupMenu.getMenu());
        popupMenu.show();
        // 通过上面这几行代码，就可以把控件显示出来了
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                // 控件每一个item的点击事件
                switch (item.getItemId()){
                    case R.id.main_List:{
                        listview.setVisibility(View.VISIBLE);
                        listView1.setVisibility(View.INVISIBLE);
                        listView2.setVisibility(View.INVISIBLE);
                        adapter.notifyDataSetChanged();
                        return true;
                    }
                    case R.id.list_1:{
                        adapter1.notifyDataSetChanged();
                        listView1.setVisibility(View.VISIBLE);
                        listview.setVisibility(View.INVISIBLE);
                        listView2.setVisibility(View.INVISIBLE);
                        return true;
                    }
                    case R.id.list_2:{
                        adapter2.notifyDataSetChanged();
                        listView1.setVisibility(View.INVISIBLE);
                        listview.setVisibility(View.INVISIBLE);
                        listView2.setVisibility(View.VISIBLE);
                        return true;
                    }
                }
                return true;
            }
        });
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                // 控件消失时的事件
            }
        });

    }

    // 给屏幕宽度赋值
    private void init_screen_width() {
        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        screen_width = size.x;
    }

    // 设置popupwindow相关属性及里面控件的点击事件
    /**
     * 设置左边控制栏与其点击事件
     */
    private void setpopupwindow() {
        View popup_layout = LayoutInflater.from(MainActivity.this).inflate(
                R.layout.popupwindow_setting, null);
        popupWindow = new PopupWindow(popup_layout, ActionBar.LayoutParams.WRAP_CONTENT,
                ActionBar.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new ColorDrawable(
                android.graphics.Color.TRANSPARENT));
        popupWindow.setOutsideTouchable(true);

        RelativeLayout relativeLayout_listbg = (RelativeLayout) popup_layout
                .findViewById(R.id.layout_listbg);

        RelativeLayout relativeLayout_skin = (RelativeLayout) popup_layout
                .findViewById(R.id.layout_skin);

        RelativeLayout relativeLayout_location = (RelativeLayout) popup_layout
                .findViewById(R.id.layout_location_bestlove);

        RelativeLayout relativeLayout_exit = (RelativeLayout) popup_layout
                .findViewById(R.id.layout_exit);

        // 初始化第一行点击后弹出的自定义dialog并设置相关属性
        myDialog = new MyDialog(MainActivity.this, R.style.dialogTheme,
                R.layout.popup_changelistbg);
        final Window window = myDialog.getWindow();
        window.setGravity(Gravity.BOTTOM);
        window.setWindowAnimations(R.style.enter);

        relativeLayout_listbg.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                popupWindow.dismiss();

                myDialog.show();
                WindowManager.LayoutParams params = window.getAttributes();
                params.width = screen_width;
                params.dimAmount = 0.4f;
                window.setAttributes(params);

                // 弹出dialog的点击事件
                RelativeLayout select_localimage = (RelativeLayout) myDialog
                        .findViewById(R.id.select_localimage);
                RelativeLayout use_default = (RelativeLayout) myDialog
                        .findViewById(R.id.use_default);
                RelativeLayout select_xhh = (RelativeLayout) myDialog
                        .findViewById(R.id.select_xhh);
                select_localimage.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        Intent intent = new Intent();
                        intent.setType("image/*");
                        intent.putExtra("return-data", true);

                        intent.putExtra("crop", "circle");
                        // 使用Intent.ACTION_GET_CONTENT这个Action
                        intent.setAction(Intent.ACTION_GET_CONTENT);
                        // 取得相片后返回本画面
                        startActivityForResult(intent, 1);
                    }
                });
                use_default.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        listview.setBackground(null);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("listbg", "");
                        editor.commit();
                        myDialog.dismiss();
                    }
                });
                select_xhh.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        listview.setBackground(getResources().getDrawable(
                                R.mipmap.xhh));
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("listbg", "xhh");
                        editor.commit();
                        myDialog.dismiss();
                    }
                });

            }
        });

        relativeLayout_skin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                popupWindow.dismiss();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("song_name",
                        cut_song_name(list.get(currentposition).getSong()));
                editor.putString("song_singer",
                        list.get(currentposition).getSinger());
                editor.putInt("currentposition", currentposition);

                editor.commit();
                Intent intent = new Intent(MainActivity.this,
                        ThemeSettingActivity.class);
                MainActivity.this.startActivity(intent);
                overridePendingTransition(R.anim.fade, R.anim.hold);

                if (mplayer.isPlaying()) {
                    mplayer.stop();
                    mplayer.reset();
                }
            }
        });

        // 定位至最爱歌曲
        relativeLayout_location.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                popupWindow.dismiss();
                int bestlove_position = sharedPreferences.getInt(
                        "bestlove_position", 0);
                currentposition = bestlove_position;

                adapter.setFlag(currentposition);

                adapter.notifyDataSetChanged();

                if (bestlove_position - 3 < 0) {
                    listview.setSelection(0);
                } else {
                    listview.setSelection(bestlove_position - 3);
                }
                musicplay(bestlove_position);
            }
        });

        // 初始化退出行点击后弹出的dialog并设置相关属性
        final CustomDialog customDialog = new CustomDialog(MainActivity.this,
                R.layout.layout_customdialog, R.style.dialogTheme);
        customDialog.setT("系统提示");
        customDialog.setM("确定要退出播放器了吗?");
        customDialog.setButtonLeftText("确定");
        customDialog.setButtonRightText("取消");

        relativeLayout_exit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                popupWindow.dismiss();

                customDialog.show();
                customDialog.setOnClickBtnListener(new CustomDialog.OnClickBtnListener() {

                    @Override
                    public void onClickOk() {
                        // TODO Auto-generated method stub
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        startActivity(intent);
                        customDialog.dismiss();
                    }
                    @Override
                    public void onClickCancel() {
                        // TODO Auto-generated method stub
                        customDialog.cancel();
                    }
                });
            }
        });
    }

    private void initTopView() {
        imageview_playstyle = (ImageView) this.findViewById(R.id.play_style);
    }

    private void setClick() {


        imageview_playstyle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                play_style++;
                if (play_style > 2) {
                    play_style = 0;
                }
                switch (play_style) {
                    case 0:
                        imageview_playstyle.setImageResource(R.mipmap.cicle);
                        Toast.makeText(MainActivity.this, "单曲循环",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        imageview_playstyle.setImageResource(R.mipmap.ordered);
                        Toast.makeText(MainActivity.this, "顺序播放",
                                Toast.LENGTH_SHORT).show();
                        break;
                    case 2:
                        imageview_playstyle.setImageResource(R.mipmap.unordered);
                        Toast.makeText(MainActivity.this, "随机播放",
                                Toast.LENGTH_SHORT).show();
                        break;

                }
            }
        });


        View layout_playbar = (View) findViewById(R.id.layout_playbar);
        Button_play = (Button) layout_playbar
                .findViewById(R.id.imageview_play);
        Button_next = (Button) layout_playbar
                .findViewById(R.id.imageview_next);
        Button_front = (Button) layout_playbar
                .findViewById(R.id.imageview_front);
        textView1 = (TextView) layout_playbar.findViewById(R.id.name);
        textView2 = (TextView) layout_playbar.findViewById(R.id.singer);
        seekBar = (SeekBar) layout_playbar.findViewById(R.id.seekbar);
        Button_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                play();
            }
        });

        Button_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                if (play_style == 2) {
                    random_nextMusic();
                    auto_change_listview();
                } else {
                    nextMusic();
                    auto_change_listview();
                }
            }
        });

        Button_front.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                if (play_style == 2) {
                    random_nextMusic();
                    auto_change_listview();
                } else {
                    frontMusic();
                    auto_change_listview();
                }
            }
        });
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                ischanging = false;
                mplayer.seekTo(seekBar.getProgress());
                thread = new Thread(new SeekBarThread());
                thread.start();
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
            }
        });

    }

    private void initText() {
        textView1.setText(sharedPreferences.getString("song_name", "歌曲名").trim());
        textView2.setText(sharedPreferences.getString("song_singer", "歌手").trim());
    }

    private void setListView() {
        listview = (ListView) this.findViewById(R.id.listveiw);
        listView1 = (ListView) this.findViewById(R.id.listveiw1);
        listView2 = (ListView) this.findViewById(R.id.listveiw2);
        mainList=new ArrayList<Song>();
        list = new ArrayList<Song>();
        list1=new ArrayList<Song>();
        list2=new ArrayList<Song>();
        mainList = MusicUtils.getMusicData(MainActivity.this);
        list=mainList;
        adapter = new MyAdapter(MainActivity.this, list);
        adapter1=new MyAdapter(MainActivity.this,list1);
        adapter2=new MyAdapter(MainActivity.this,list2);
        // 标记正在播放的音乐条目为主题色
        adapter.setFlag(currentposition);
        adapter.notifyDataSetChanged();
        adapter1.notifyDataSetChanged();
        adapter2.notifyDataSetChanged();
        listview.setAdapter(adapter);
        listView1.setAdapter(adapter1);
        listView2.setAdapter(adapter2);


        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                currentposition = position;
                musicplay(currentposition);
                Button_play.setText("‖");
                adapter.setFlag(currentposition);
                adapter.notifyDataSetChanged();
            }
        });
        listView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                list=list1;
                currentposition = position;
                musicplay(currentposition);
                Button_play.setText("‖");
                adapter1.setFlag(currentposition);
                adapter1.notifyDataSetChanged();
            }
        });
        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // TODO Auto-generated method stub
                list=list2;
                currentposition = position;
                musicplay(currentposition);
                Button_play.setText("‖");
                adapter2.setFlag(currentposition);
                adapter2.notifyDataSetChanged();
            }
        });

        myDialog_bestlove = new MyDialog(MainActivity.this,
                R.style.dialogTheme, R.layout.setting_best_lovesong);
        final Window window2 = myDialog_bestlove.getWindow();
        window2.setGravity(Gravity.CENTER);
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view,
                                           final int position, long id) {
                // TODO Auto-generated method stub
                myDialog_bestlove.show();
                WindowManager.LayoutParams params = window2.getAttributes();
                params.width = (int) (screen_width * 0.75);
                params.dimAmount = 0.4f;
                window2.setAttributes(params);

                RelativeLayout relativeLayout_make_delete = (RelativeLayout) myDialog_bestlove.findViewById(R.id.make_delete);
                RelativeLayout relativeLayout_add_to_list1 = (RelativeLayout) myDialog_bestlove.findViewById(R.id.add_to_list1);
                RelativeLayout relativeLayout_add_to_list2 = (RelativeLayout) myDialog_bestlove.findViewById(R.id.add_to_list2);


                final int best_love_position = position;

                relativeLayout_make_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        myDialog_bestlove.dismiss();
                        list.remove(position);
                        adapter = new MyAdapter(MainActivity.this, list);
                        adapter.notifyDataSetChanged();
                        listview.setAdapter(adapter);

                    }
                });

                relativeLayout_add_to_list1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub
                        myDialog_bestlove.dismiss();
                        list1.add(list.get(position));
                        adapter1 = new MyAdapter(MainActivity.this, list1);
                        adapter1.notifyDataSetChanged();
                        listView1.setAdapter(adapter1);
                        Toast.makeText(MainActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                    }
                });
                relativeLayout_add_to_list2
                        .setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                // TODO Auto-generated method stub
                                myDialog_bestlove.dismiss();
                                list2.add(list.get(position));
                                adapter2 = new MyAdapter(MainActivity.this, list2);
                                adapter2.notifyDataSetChanged();
                                listView2.setAdapter(adapter2);
                                Toast.makeText(MainActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                            }
                        });
                return true;
            }
        });

        listview.setOnScrollListener(new AbsListView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                // TODO Auto-generated method stub
                max_item = visibleItemCount;
            }
        });

        // 给listview设置初始背景
        if (loadDrawable() != null) {
            listview.setBackground(loadDrawable());
        } else if (sharedPreferences.getString("listbg", "").equals("xhh")) {
            listview.setBackground(getResources().getDrawable(R.mipmap.xhh));
        } else {
            listview.setBackground(null);
        }
    }


    private static void musicplay(int position) {

        textView1.setText(cut_song_name(list.get(position).getSong()).trim());
        textView2.setText(list.get(position).getSinger().trim());
        seekBar.setMax(list.get(position).getDuration());
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
                seekBar.setProgress(mplayer.getCurrentPosition());

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
    //播放
    public static void play(){
        if (mplayer.isPlaying()) {
            mplayer.pause();
            Button_play.setText("▶");
        } else {
            mplayer.start();
            // thread = new Thread(new SeekBarThread());
            // thread.start();
            Button_play.setText("‖");
        }
    }
    // 下一曲
    public static void nextMusic() {
        currentposition++;
        if (currentposition > list.size() - 1) {
            currentposition = 0;
        }
        musicplay(currentposition);
        adapter.setFlag(currentposition);
        adapter.notifyDataSetChanged();
    }

    // 上一曲
    public static void frontMusic() {
        currentposition--;
        if (currentposition < 0) {
            currentposition = list.size() - 1;
        }
        musicplay(currentposition);
        adapter.setFlag(currentposition);
        adapter.notifyDataSetChanged();
    }

    // 随机播放下一曲
    private void random_nextMusic() {
        currentposition = currentposition + random.nextInt(list.size() - 1);
        currentposition %= list.size();
        musicplay(currentposition);
        adapter.setFlag(currentposition);
        adapter.notifyDataSetChanged();
    }

    // 切掉音乐名字最后的.mp3
    private static String cut_song_name(String name) {
        if (name.length() >= 5
                && name.substring(name.length() - 4, name.length()).equals(
                ".mp3")) {
            return name.substring(0, name.length() - 4);
        }
        return name;
    }


    // 点击下一曲上一曲时自动滚动列表
    private void auto_change_listview() {
        if (currentposition <= listview.getFirstVisiblePosition()) {
            listview.setSelection(currentposition);
        }
        if (currentposition >= listview.getLastVisiblePosition()) {
            // listview.smoothScrollToPosition(currentposition);
            listview.setSelection(currentposition - max_item + 2);
        }
    }

    // 使用sharedPreferences保存listview背景图片
    private void saveDrawable(Drawable drawable) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        Bitmap bitmap = bitmapDrawable.getBitmap();
        // Bitmap bitmap = BitmapFactory.decodeResource(getResources(), id);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
        String imageBase64 = new String(Base64.encodeToString(
                baos.toByteArray(), Base64.DEFAULT));
        editor.putString("listbg", imageBase64);
        editor.commit();
    }

    // 加载用sharedPreferences保存的图片
    private Drawable loadDrawable() {
        String temp = sharedPreferences.getString("listbg", "");
        ByteArrayInputStream bais = new ByteArrayInputStream(Base64.decode(
                temp.getBytes(), Base64.DEFAULT));
        return Drawable.createFromStream(bais, "");
    }
    private void getAuthority() {
        //适配6.0以上机型请求权限
        PermissionGen.with(MainActivity.this)
                .addRequestCode(100)
                .permissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                .request();
    }

    //以下三个方法用于6.0以上权限申请适配
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionGen.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @PermissionSuccess(requestCode = 100)
    public void doSomething() {
        //Toast.makeText(this, "相关权限已允许", Toast.LENGTH_SHORT).show();
    }

    @PermissionFail(requestCode = 100)
    public void doFailSomething() {
        //Toast.makeText(this, "相关权限已拒绝", Toast.LENGTH_SHORT).show();
    }
    public static List<Song> getList(){
        return list;
    }




    private void initPermission() {
        String[] permissions = {
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                // 进入到这里代表没有权限.

            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }
    }

    public static void start() {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(SpeechConstant.WP_WORDS_FILE, "assets:///WakeUp.bin");
        // "assets:///WakeUp.bin" 表示WakeUp.bin文件定义在assets目录下
        // params.put(SpeechConstant.ACCEPT_AUDIO_DATA,true);
        // params.put(SpeechConstant.IN_FILE,"res:///com/baidu/android/voicedemo/wakeup.pcm");
        // params里 "assets:///WakeUp.bin" 表示WakeUp.bin文件定义在assets目录下
        myWakeup.start(params);
    }
}
