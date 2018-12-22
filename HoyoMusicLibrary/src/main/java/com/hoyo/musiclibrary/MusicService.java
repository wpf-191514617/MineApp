package com.hoyo.musiclibrary;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.hoyo.musiclibrary.cache.CacheConfig;
import com.hoyo.musiclibrary.control.PlayControl;
import com.hoyo.musiclibrary.notification.NotificationCreater;
import com.hoyo.musiclibrary.playback.player.ExoPlayerHelper;
import com.hoyo.musiclibrary.utils.LogUtil;


/**
 * Created by xian on 2018/1/20.
 */

public class MusicService extends Service {

    private PlayControl mBinder;

    private static MusicService mService;
    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mService = this;
        this.mNotificationManager = (NotificationManager) getSystemService(Service.NOTIFICATION_SERVICE);
        stopForeground(true);

        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel("com.lzx.musiclibrary", "播放通知栏", NotificationManager.IMPORTANCE_HIGH);
            channel.enableLights(false);
            channel.setShowBadge(false);
            channel.setSound(null, null);
            channel.enableVibration(false);
            this.mNotificationManager.createNotificationChannel(channel);
            //    Notification notification = new Notification.Builder(getApplicationContext(),channel.getId()).build();
            //    startForeground(1, notification);
        }

        ExoPlayerHelper.getInstance().init(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        boolean isUseMediaPlayer = intent.getBooleanExtra("isUseMediaPlayer", false);
        boolean isAutoPlayNext = intent.getBooleanExtra("isAutoPlayNext", true);
        boolean isGiveUpAudioFocusManager = intent.getBooleanExtra("isGiveUpAudioFocusManager", false);
        NotificationCreater notificationCreater = intent.getParcelableExtra("notificationCreater");
        CacheConfig cacheConfig = intent.getParcelableExtra("cacheConfig");
        mBinder = new PlayControl
                .Builder(this)
                .setAutoPlayNext(isAutoPlayNext)
                .setUseMediaPlayer(isUseMediaPlayer)
                .setGiveUpAudioFocusManager(isGiveUpAudioFocusManager)
                .setNotificationCreater(notificationCreater)
                .setCacheConfig(cacheConfig)
                .build();
        return mBinder;
    }

    public PlayControl getBinder() {
        return mBinder;
    }

    public static MusicService getService() {
        return mService;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
        LogUtil.i("服务关闭了。。。");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtil.i("服务关闭了。。。");
        if (mBinder != null) {
            //mBinder.stopMusic();
            mBinder.releaseMediaSession();
        }
    }
}
