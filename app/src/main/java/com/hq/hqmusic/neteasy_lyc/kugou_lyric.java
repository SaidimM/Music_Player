package com.hq.hqmusic.neteasy_lyc;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hq.hqmusic.UI.Main2Activity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
public class kugou_lyric {
    public static void searchLyric(final String name, final String duration){
        String lyric;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //建立连接 -- 查找歌曲
                    String urlStr = "http://lyrics.kugou.com/search?ver=1&man=yes&client=pc&keyword=" + name + "&duration=" + duration + "&hash=";
                    System.out.println(URLEncoder.encode(urlStr,"UTF-8"));
                    System.out.println((URLDecoder.decode(urlStr, "UTF-8")));
                    URL url = new URL(URLDecoder.decode(urlStr, "UTF-8"));  //字符串进行URL编码
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    //读取流 -- JSON歌曲列表
                    InputStream input = conn.getInputStream();
                    String res = FileUtil.streamToString(input);  //流转字符串
                    System.out.println(res);
                    JsonObject json1 = new JsonParser().parse(res).getAsJsonObject();
                    JsonArray json2 = json1.getAsJsonArray("candidates");
                    JsonObject json3 = json2.get(0).getAsJsonObject();
                    System.out.println(json3.get("id").getAsString());
                    //建立连接 -- 查找歌词
                    urlStr = "http://lyrics.kugou.com/download?ver=1&client=pc&id=" + json3.get("id").getAsString() + "&accesskey=" + json3.get("accesskey").getAsString() + "&fmt=lrc&charset=utf8";
                    url = new URL(URLDecoder.decode(urlStr, "UTF-8"));
                    System.out.println(url);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.connect();
                    //读取流 -- 歌词
                    input = conn.getInputStream();
                    res = FileUtil.streamToString(input);
                    System.out.println(res);
                    JsonObject json4 = new JsonParser().parse(res).getAsJsonObject();
                    //获取歌词base64，并进行解码
                    String base64 = json4.get("content").getAsString();
                    System.out.println(base64);
                    String lyric = Base64.getFromBase64(base64);
                    System.out.println(lyric);
                    System.out.println("sdcard/Download/lyric/"+name+".lrc");
                    File file = new File("sdcard/Download/lyric/"+name+".lrc");
                    if(!file.exists()){
                        try {
                            FileOutputStream fos = new FileOutputStream(file);
                            fos.write((lyric).getBytes());
                            fos.close();
                        } catch (Exception e) {
                            // TODO: handle exception
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public static void main(String[]args){
        searchLyric("Sorry","201064");
    }
}