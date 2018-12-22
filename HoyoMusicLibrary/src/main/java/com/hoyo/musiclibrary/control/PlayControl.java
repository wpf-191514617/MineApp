package com.hoyo.musiclibrary.control;

import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.support.v4.media.session.MediaSessionCompat;

import com.hoyo.musiclibrary.MusicService;
import com.hoyo.musiclibrary.aidl.listener.NotifyContract;
import com.hoyo.musiclibrary.aidl.model.SongInfo;
import com.hoyo.musiclibrary.aidl.source.IOnPlayerEventListener;
import com.hoyo.musiclibrary.aidl.source.IOnTimerTaskListener;
import com.hoyo.musiclibrary.bus.Subscriber;
import com.hoyo.musiclibrary.bus.tags.BusTags;
import com.hoyo.musiclibrary.bus.tags.QueueIndexUpdated;
import com.hoyo.musiclibrary.cache.CacheConfig;
import com.hoyo.musiclibrary.constans.State;
import com.hoyo.musiclibrary.helper.QueueHelper;
import com.hoyo.musiclibrary.notification.NotificationCreater;
import com.hoyo.musiclibrary.playback.player.ExoPlayback;
import com.hoyo.musiclibrary.playback.player.MediaPlayback;
import com.hoyo.musiclibrary.playback.player.Playback;

import java.util.List;

/**
 * Binder
 * Created by xian on 2018/1/28.
 */

public class PlayControl extends BasePlayControl {

    private RemoteCallbackList<IOnPlayerEventListener> mRemoteCallbackList;
    private RemoteCallbackList<IOnTimerTaskListener> mOnTimerTaskListenerList;
    private boolean isRegisterOnPlayerEventListener = false;
    private boolean isRegisterOnTimerTaskListener = false;

    private boolean isAsyncLoading = false;

    private PlayControl(Builder builder) {
        super();
        mMusicService = builder.mMusicService;
        isAutoPlayNext = builder.isAutoPlayNext;
        notificationCreater = builder.notificationCreater;
        mNotifyStatusChanged = new NotifyStatusChange();
        mNotifyMusicSwitch = new NotifyMusicSwitch();
        mNotifyTimerTask = new NotifyTimerTask();
        mRemoteCallbackList = new RemoteCallbackList<>();
        mOnTimerTaskListenerList = new RemoteCallbackList<>();
        mPlayback = builder.isUseMediaPlayer
                ? new MediaPlayback(mMusicService.getApplicationContext(), builder.cacheConfig, builder.isGiveUpAudioFocusManager)
                : new ExoPlayback(mMusicService.getApplicationContext(), builder.cacheConfig, builder.isGiveUpAudioFocusManager);
        init();
    }

    public PlayControl getController() {
        return this;
    }

    public static class Builder {
        private MusicService mMusicService;
        private boolean isUseMediaPlayer = false;
        private boolean isAutoPlayNext = true;
        private boolean isGiveUpAudioFocusManager = false;
        private NotificationCreater notificationCreater;
        private CacheConfig cacheConfig;

        public Builder(MusicService mService) {
            mMusicService = mService;
        }

        public Builder setUseMediaPlayer(boolean useMediaPlayer) {
            isUseMediaPlayer = useMediaPlayer;
            return this;
        }

        public Builder setAutoPlayNext(boolean autoPlayNext) {
            isAutoPlayNext = autoPlayNext;
            return this;
        }

        public Builder setNotificationCreater(NotificationCreater notificationCreater) {
            this.notificationCreater = notificationCreater;
            return this;
        }

        public Builder setCacheConfig(CacheConfig cacheConfig) {
            this.cacheConfig = cacheConfig;
            return this;
        }

        public Builder setGiveUpAudioFocusManager(boolean giveUpAudioFocusManager) {
            isGiveUpAudioFocusManager = giveUpAudioFocusManager;
            return this;
        }

        public PlayControl build() {
            return new PlayControl(this);
        }
    }

    private class NotifyStatusChange implements NotifyContract.NotifyStatusChanged {

        @Override
        public void notify(SongInfo info, int index, int status, String errorMsg) {
            synchronized (PlayControl.class) {
                final int N = mRemoteCallbackList.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    IOnPlayerEventListener listener = mRemoteCallbackList.getBroadcastItem(i);
                    if (listener != null) {
                        try {
                            switch (status) {
                                case State.STATE_IDLE:
                                    listener.onPlayCompletion(info);
                                    break;
                                case State.STATE_ASYNC_LOADING:
                                    isAsyncLoading = true;
                                    listener.onAsyncLoading(false);
                                    break;
                                case State.STATE_PLAYING:
                                    isAsyncLoading = false;
                                    listener.onAsyncLoading(true);
                                    listener.onPlayerStart();
                                    break;
                                case State.STATE_PAUSED:
                                    if (isAsyncLoading) {
                                        listener.onAsyncLoading(true);
                                        isAsyncLoading = false;
                                    }
                                    listener.onPlayerPause();
                                    break;
                                case State.STATE_STOP:
                                    listener.onPlayerStop();
                                    break;
                                case State.STATE_ERROR:
                                    listener.onError(errorMsg);
                                    break;
                            }
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mRemoteCallbackList.finishBroadcast();
            }
        }
    }

    private class NotifyMusicSwitch implements NotifyContract.NotifyMusicSwitch {

        @Override
        public void notify(SongInfo info) {
            synchronized (PlayControl.class) {
                final int N = mRemoteCallbackList.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    IOnPlayerEventListener listener = mRemoteCallbackList.getBroadcastItem(i);
                    if (listener != null) {
                        try {
                            listener.onMusicSwitch(info);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mRemoteCallbackList.finishBroadcast();
            }
        }
    }

    private class NotifyTimerTask implements NotifyContract.NotifyTimerTask {

        @Override
        public void notifyTimerTasFinish() {
            synchronized (PlayControl.class) {
                final int N = mOnTimerTaskListenerList.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    IOnTimerTaskListener listener = mOnTimerTaskListenerList.getBroadcastItem(i);
                    if (listener != null) {
                        try {
                            listener.onTimerFinish();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mOnTimerTaskListenerList.finishBroadcast();
            }
        }

        @Override
        public void onTimerTick(long millisUntilFinished, long totalTime) {
            synchronized (PlayControl.class) {
                final int N = mOnTimerTaskListenerList.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    IOnTimerTaskListener listener = mOnTimerTaskListenerList.getBroadcastItem(i);
                    if (listener != null) {
                        try {
                            listener.onTimerTick(millisUntilFinished, totalTime);
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mOnTimerTaskListenerList.finishBroadcast();
            }
        }
    }

    public Playback getPlayback() {
        return mPlayback;
    }

    public void releaseMediaSession() {
        mMediaSessionManager.release();
    }

    @Override
    public void registerPlayerEventListener(IOnPlayerEventListener listener) {
        super.registerPlayerEventListener(listener);
        mRemoteCallbackList.register(listener);
        isRegisterOnPlayerEventListener = true;
    }

    @Override
    public void unregisterPlayerEventListener(IOnPlayerEventListener listener) {
        super.unregisterPlayerEventListener(listener);
        if (isRegisterOnPlayerEventListener) {
            mRemoteCallbackList.unregister(listener);
            isRegisterOnPlayerEventListener = false;
        }
    }

    @Override
    public void registerTimerTaskListener(IOnTimerTaskListener listener) {
        super.registerTimerTaskListener(listener);
        mOnTimerTaskListenerList.register(listener);
        isRegisterOnTimerTaskListener = true;
    }

    @Override
    public void unregisterTimerTaskListener(IOnTimerTaskListener listener) {
        super.unregisterTimerTaskListener(listener);
        if (isRegisterOnTimerTaskListener) {
            mOnTimerTaskListenerList.unregister(listener);
            isRegisterOnTimerTaskListener = false;
        }
    }

    @Subscriber(tag = BusTags.onMetadataChanged)
    public void onMetadataChanged(SongInfo songInfo) {
        mMediaSessionManager.updateMetaData(QueueHelper.fetchInfoWithMediaMetadata(songInfo));
    }

    @Subscriber(tag = BusTags.onMetadataRetrieveError)
    public void onMetadataRetrieveError() {
        mPlaybackManager.updatePlaybackState("Unable to retrieve metadata.",false);
    }

    @Subscriber(tag = BusTags.onCurrentQueueIndexUpdated)
    public void onCurrentQueueIndexUpdated(QueueIndexUpdated updated) {
        mPlaybackManager.handlePlayPauseRequest(updated.isJustPlay, updated.isSwitchMusic); //播放
    }

    @Subscriber(tag = BusTags.onQueueUpdated)
    public void onQueueUpdated(List<MediaSessionCompat.QueueItem> newQueue) {
        mMediaSessionManager.setQueue(newQueue);
    }

    @Subscriber(tag = BusTags.onPlayModeChange)
    public void onPlayModeChange(int playMode) {
        mPlayQueueManager.checkIndexForPlayMode(mPlayback.getCurrentMediaId());
    }
}
