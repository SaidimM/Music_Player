package com.hq.hqmusic.wakeUp;


import com.baidu.speech.EventListener;
import com.baidu.speech.asr.SpeechConstant;

import static com.hq.hqmusic.UI.MainActivity.Button_play;
import static com.hq.hqmusic.UI.MainActivity.frontMusic;
import static com.hq.hqmusic.UI.MainActivity.mplayer;
import static com.hq.hqmusic.UI.MainActivity.nextMusic;

/**
 * Created by fujiayi on 2017/6/20.
 */

public class WakeupEventAdapter implements EventListener {
    private IWakeupListener listener;

    public WakeupEventAdapter(IWakeupListener listener) {
        this.listener = listener;
    }

    private static final String TAG = "WakeupEventAdapter";
  // 基于DEMO唤醒3.1 开始回调事件
    @Override
    public void onEvent(String name, String params, byte[] data, int offset, int length) {
        // android studio日志Monitor 中搜索 WakeupEventAdapter即可看见下面一行的日志
        MyLogger.info(TAG, "wakeup name:" + name + "; params:" + params);
        if (SpeechConstant.CALLBACK_EVENT_WAKEUP_SUCCESS.equals(name)) { // 识别唤醒词成功
            WakeUpResult result = WakeUpResult.parseJson(name, params);
            int errorCode = result.getErrorCode();
            if (result.hasError()) { // error不为0依旧有可能是异常情况
                listener.onError(errorCode, "", result);
            } else {
                String word = result.getWord();
                listener.onSuccess(word, result);
                switch (word){
                    case "暂停":{
                        mplayer.pause();
                        Button_play.setText("▶");
                        break;
                    }
                    case "播放":{
                        mplayer.start();
                        Button_play.setText("‖");
                        break;
                    }
                    case "下一首":nextMusic();break;
                    case "上一首":frontMusic();break;
                }
            }
        } else if (SpeechConstant.CALLBACK_EVENT_WAKEUP_ERROR.equals(name)) { // 识别唤醒词报错
            WakeUpResult result = WakeUpResult.parseJson(name, params);
            int errorCode = result.getErrorCode();
            if (result.hasError()) {
                listener.onError(errorCode, "", result);
            }
        } else if (SpeechConstant.CALLBACK_EVENT_WAKEUP_STOPED.equals(name)) { // 关闭唤醒词
            listener.onStop();
        } else if (SpeechConstant.CALLBACK_EVENT_WAKEUP_AUDIO.equals(name)) { // 音频回调
            listener.onASrAudio(data, offset, length);
        }
    }
}
