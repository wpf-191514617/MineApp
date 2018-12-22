package com.hoyo.musiclibrary.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;

import com.hoyo.musiclibrary.MusicService;
import com.hoyo.musiclibrary.aidl.source.IPlayControl;
import com.hoyo.musiclibrary.cache.CacheConfig;
import com.hoyo.musiclibrary.notification.NotificationCreater;


/**
 * Created by xian on 2018/5/15.
 */

public class MusicLibrary {

    public static String ACTION_MUSICLIBRARY_INIT_FINISH = "ACTION_MUSICLIBRARY_INIT_FINISH";//服务初始化成功action
    private Context mContext;
    private boolean isUseMediaPlayer;
    private boolean isAutoPlayNext;
    private boolean isGiveUpAudioFocusManager;

    private NotificationCreater mNotificationCreater;
    private CacheConfig mCacheConfig;
    private Builder mBuilder;

    private MusicLibrary(Builder builder) {
        mBuilder = builder;
        mContext = builder.context;
        isUseMediaPlayer = builder.isUseMediaPlayer;
        isAutoPlayNext = builder.isAutoPlayNext;
        isGiveUpAudioFocusManager = builder.isGiveUpAudioFocusManager;
        mNotificationCreater = builder.mNotificationCreater;
        mCacheConfig = builder.mCacheConfig;
    }

    public static class Builder {
        private Context context;
        private boolean isUseMediaPlayer = false;
        private boolean isAutoPlayNext = true;
        private boolean isGiveUpAudioFocusManager = false;
        private NotificationCreater mNotificationCreater;
        private CacheConfig mCacheConfig;

        public Builder(Context context) {
            this.context = context.getApplicationContext();
        }

//        public Builder setUseMediaPlayer(boolean isUseMediaPlayer) {
//            this.isUseMediaPlayer = isUseMediaPlayer;
//            return this;
//        }

        public Builder setAutoPlayNext(boolean autoPlayNext) {
            isAutoPlayNext = autoPlayNext;
            return this;
        }

        public Builder setNotificationCreater(NotificationCreater creater) {
            mNotificationCreater = creater;
            return this;
        }

        public Builder giveUpAudioFocusManager() {
            isGiveUpAudioFocusManager = true;
            return this;
        }

        public Builder setCacheConfig(CacheConfig cacheConfig) {
            if (cacheConfig != null) {
                mCacheConfig = cacheConfig;
            }
            return this;
        }

        boolean isUseMediaPlayer() {
            return isUseMediaPlayer;
        }

        boolean isAutoPlayNext() {
            return isAutoPlayNext;
        }

        boolean isGiveUpAudioFocusManager() {
            return isGiveUpAudioFocusManager;
        }

        NotificationCreater getNotificationCreater() {
            return mNotificationCreater;
        }

        CacheConfig getCacheConfig() {
            return mCacheConfig;
        }

        public MusicLibrary build() {
            return new MusicLibrary(this);
        }
    }

    public void startMusicService() {
        init(true);
    }

    public void bindMusicService() {
        init(false);
    }

    public void stopService() {
        MusicManager.get().stopService();
        Intent intent = new Intent(mContext, MusicService.class);
        mContext.unbindService(mServiceConnection);
        mContext.stopService(intent);
    }

    private void init(boolean isStartService) {
        Intent intent = new Intent(mContext, MusicService.class);
        intent.putExtra("isUseMediaPlayer", isUseMediaPlayer);
        intent.putExtra("isAutoPlayNext", isAutoPlayNext);
        intent.putExtra("isGiveUpAudioFocusManager", isGiveUpAudioFocusManager);
        intent.putExtra("notificationCreater", mNotificationCreater);
        intent.putExtra("cacheConfig", mCacheConfig);
        if (isStartService) {
            startService(intent);
        }
        mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    private void startService(Intent intent) {
        try {
            mContext.startService(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            IPlayControl control = IPlayControl.Stub.asInterface(iBinder);
            MusicManager.get().attachPlayControl(mContext, control);
            MusicManager.get().attachMusicLibraryBuilder(mBuilder);
            //发送一个广播，可通过接受它来知道服务初始化成功
            mContext.sendBroadcast(new Intent(ACTION_MUSICLIBRARY_INIT_FINISH));
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
        }
    };
}
