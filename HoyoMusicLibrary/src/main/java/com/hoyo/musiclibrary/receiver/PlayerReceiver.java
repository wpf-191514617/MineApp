package com.hoyo.musiclibrary.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.hoyo.musiclibrary.MusicService;
import com.hoyo.musiclibrary.constans.State;
import com.hoyo.musiclibrary.control.BasePlayControl;
import com.hoyo.musiclibrary.control.PlayControl;
import com.hoyo.musiclibrary.notification.IMediaNotification;

/**
 * Created by xian on 2018/2/18.
 */

public class PlayerReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (TextUtils.isEmpty(action)) {
            return;
        }
        MusicService musicService = MusicService.getService();
        if (musicService == null) {
            return;
        }
        PlayControl binder = musicService.getBinder();
        if (binder == null) {
            return;
        }
        BasePlayControl controller = binder.getController();
        if (controller == null) {
            return;
        }
        switch (action) {
            case IMediaNotification.ACTION_CLOSE:
                controller.stopMusic();
                controller.stopNotification();
                break;
            case IMediaNotification.ACTION_PLAY_PAUSE:
                if (controller.getStatus() == State.STATE_PLAYING) {
                    controller.pauseMusic();
                } else if (controller.getStatus() == State.STATE_PAUSED) {
                    controller.resumeMusic();
                }
                break;
            case IMediaNotification.ACTION_PREV:
                controller.playPre();
                break;
            case IMediaNotification.ACTION_NEXT:
                controller.playNext();
                break;
            default:
                break;
        }
    }
}
