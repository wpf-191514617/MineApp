package com.hoyo.musiclibrary.constans;

import android.content.Context;

import com.hoyo.musiclibrary.bus.Bus;
import com.hoyo.musiclibrary.bus.tags.BusTags;
import com.hoyo.musiclibrary.utils.SPUtils;

import static com.hoyo.musiclibrary.constans.Constans.music_key_play_model;

/**
 * Created by xian on 2018/1/28.
 */

public class PlayMode {

    //单曲循环
    public static final int PLAY_IN_SINGLE_LOOP = 1;

    //随机播放
    public static final int PLAY_IN_RANDOM = 2;

    //列表循环
    public static final int PLAY_IN_LIST_LOOP = 3;

    //顺序播放
    public static final int PLAY_IN_ORDER = 4;

    //倒叙播放
    public static final int PLAY_IN_FLASHBACK = 5;

    private int currPlayMode = PLAY_IN_LIST_LOOP;

    private PlayMode() {
    }

    public static PlayMode getInstance() {
        return SingletonHolder.sInstance;
    }

    private static class SingletonHolder {
        private static final PlayMode sInstance = new PlayMode();
    }

    public int getCurrPlayMode(Context context) {
        currPlayMode = (int) SPUtils.get(context, music_key_play_model, PLAY_IN_LIST_LOOP);
        return currPlayMode;
    }

    public void setCurrPlayMode(Context context, int currPlayMode) {
        this.currPlayMode = currPlayMode;
        SPUtils.put(context, music_key_play_model, currPlayMode);
        Bus.getInstance().post(currPlayMode, BusTags.onPlayModeChange); //通知播放模式变化
    }

}
