package com.hoyo.musiclibrary.playback;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import com.hoyo.musiclibrary.aidl.model.SongInfo;
import com.hoyo.musiclibrary.constans.PlayMode;
import com.hoyo.musiclibrary.constans.State;
import com.hoyo.musiclibrary.playback.player.Playback;
import com.hoyo.musiclibrary.queue.PlayQueueManager;


/**
 * Created by xian on 2018/1/20.
 */

public class PlaybackManager implements Playback.Callback {

    private Playback mPlayback;
    private PlayQueueManager mQueueManager;
    private PlaybackServiceCallback mServiceCallback;
    private MediaSessionCallback mMediaSessionCallback;

    //是否自动播放下一首
    private boolean isAutoPlayNext;
    private String mCurrentMediaId;

    public PlaybackManager(Playback playback, PlayQueueManager queueManager, boolean isAutoPlayNext) {
        mPlayback = playback;
        mPlayback.setCallback(this);
        mQueueManager = queueManager;
        this.isAutoPlayNext = isAutoPlayNext;
        mMediaSessionCallback = new MediaSessionCallback();
    }

    public void setServiceCallback(PlaybackServiceCallback serviceCallback) {
        mServiceCallback = serviceCallback;
    }

    public Playback getPlayback() {
        return mPlayback;
    }


    public MediaSessionCompat.Callback getMediaSessionCallback() {
        return mMediaSessionCallback;
    }

    /**
     * 播放
     */
    public void handlePlayRequest() {
        SongInfo currentMusic = mQueueManager.getCurrentSongInfo();
        if (currentMusic != null && mPlayback.getState() != State.STATE_ASYNC_LOADING) {
            String mediaId = currentMusic.getSongId();
            boolean mediaHasChanged = !TextUtils.equals(mediaId, mCurrentMediaId);
            if (mediaHasChanged) {
                mCurrentMediaId = mediaId;
                notifyPlaybackSwitch(currentMusic);
            }
            //播放
            mPlayback.play(currentMusic);
            //更新媒体信息
            mQueueManager.updateMetadata();
            //updatePlaybackState(null);
        }
    }

    /**
     * 暂停
     */
    public void handlePauseRequest() {
        if (mPlayback.isPlaying()) {
            mPlayback.pause();
            //updatePlaybackState(null);
        }
    }

    /**
     * 停止
     *
     * @param withError
     */
    public void handleStopRequest(String withError, boolean isResetPlayer) {
        mPlayback.stop(true, isResetPlayer);
        updatePlaybackState(withError, isResetPlayer);
    }

    /**
     * 播放/暂停/切歌
     *
     * @param isJustPlay
     * @param isSwitchMusic
     */
    public void handlePlayPauseRequest(boolean isJustPlay, boolean isSwitchMusic) {
        if (isJustPlay) {
            mPlayback.setCurrentMediaId("");
            handlePlayRequest();
        } else {
            int state = mPlayback.getState();
            if (state == State.STATE_IDLE) {
                handlePlayRequest();
            } else if (state == State.STATE_PLAYING) {
                if (!isSwitchMusic) {
                    handlePauseRequest();
                } else {
                    handlePlayRequest();
                }
            } else if (state == State.STATE_PAUSED) {
                handlePlayRequest();
            } else if (state == State.STATE_STOP) {
                handlePlayRequest();
            }
        }
    }

    /**
     * 获取当前进度
     *
     * @return long
     */
    public long getCurrentPosition() {
        long position = 0;
        if (mPlayback != null && mPlayback.isConnected()) {
            position = mPlayback.getCurrentStreamPosition();
        }
        return position;
    }

    /**
     * 播放完成
     */
    @Override
    public void onPlayCompletion(SongInfo songInfo) {

        if (mServiceCallback != null) {
            mServiceCallback.onPlaybackCompletion(songInfo);
        }
        if (isAutoPlayNext) {
            int playMode = mQueueManager.getPlayMode();
            if (playMode == PlayMode.PLAY_IN_SINGLE_LOOP) {
                mCurrentMediaId = "";
                mPlayback.setCurrentMediaId("");
                handlePlayRequest();
            } else if (playMode == PlayMode.PLAY_IN_RANDOM || playMode == PlayMode.PLAY_IN_LIST_LOOP) {
                if (mQueueManager.skipQueuePosition(1, true)) {
                    handlePlayRequest();
                }
            } else if (playMode == PlayMode.PLAY_IN_FLASHBACK && mQueueManager.skipQueuePosition(-1, true)) {
                handlePlayRequest();
            } else if (playMode == PlayMode.PLAY_IN_ORDER && hasNextSong() && mQueueManager.skipQueuePosition(1, true)) {
                handlePlayRequest();
            } else {
                handleStopRequest(null, true);
            }
        }
    }

    private void notifyPlaybackSwitch(SongInfo info) {
        if (mServiceCallback != null) {
            mServiceCallback.onPlaybackSwitch(info);
        }
    }

    public boolean hasNextSong() {
        return mQueueManager.hasNextSong();
    }

    public boolean hasPreSong() {
        return mQueueManager.hasPreSong();
    }

    /**
     * 播放状态更新回调
     *
     * @param state
     */
    @Override
    public void onPlaybackStatusChanged(int state) {
        updatePlaybackState(null, false);
    }

    @Override
    public void onError(String error) {
        updatePlaybackState(error, false);
    }

    /**
     * 设置正在播放的id
     *
     * @param mediaId being currently played
     */
    @Override
    public void setCurrentMediaId(String mediaId) {

    }

    /**
     * 获取当前媒体id
     *
     * @return id
     */
    public String getCurrentMediaId() {
        return mPlayback.getCurrentMediaId();
    }

    public void updatePlaybackState(String error, boolean isResetPlayer) {
        long position = PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN;
        if (mPlayback != null && mPlayback.isConnected()) {
            position = mPlayback.getCurrentStreamPosition();
        }

        PlaybackStateCompat.Builder stateBuilder = new PlaybackStateCompat.Builder()
                .setActions(getAvailableActions());

        //获取播放状态
        int state = mPlayback.getState();
        if (isResetPlayer) {
            state = State.STATE_STOP;
        }
        //如果是播放失败
        if (error != null) {
            //设置错误信息
            stateBuilder.setErrorMessage(error);
            state = State.STATE_ERROR;
            if (mServiceCallback != null) {
                mServiceCallback.onPlaybackError(error);
            }
        }
        //设置播放状态
        stateBuilder.setState(state == State.STATE_PLAYING ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED,
                position, 1.0f, SystemClock.elapsedRealtime());
        // Set the activeQueueItemId if the current index is valid.
        SongInfo currentMusic = mQueueManager.getCurrentSongInfo();
        if (currentMusic != null) {
            stateBuilder.setActiveQueueItemId(currentMusic.getTrackNumber());
        }
        if (mServiceCallback != null) {
            //回调状态更新
            mServiceCallback.onPlaybackStateUpdated(state, stateBuilder.build());
            //播放/暂停状态就通知通知栏更新
            if (state == State.STATE_PLAYING || state == State.STATE_PAUSED) {
                mServiceCallback.onNotificationRequired();
            }
        }
    }

    private long getAvailableActions() {
        long actions =
                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                        PlaybackStateCompat.ACTION_PLAY_FROM_MEDIA_ID |
                        PlaybackStateCompat.ACTION_PLAY_FROM_SEARCH |
                        PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS |
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT;
        if (mPlayback.isPlaying()) {
            actions |= PlaybackStateCompat.ACTION_PAUSE;
        } else {
            actions |= PlaybackStateCompat.ACTION_PLAY;
        }
        return actions;
    }

    public int getAudioSessionId() {
        return mPlayback.getAudioSessionId();
    }

    public void switchToPlayback(Playback playback, boolean resumePlaying) {
        if (playback == null) {
            throw new IllegalArgumentException("Playback cannot be null");
        }
        // Suspends current state.
        int oldState = mPlayback.getState();
        long pos = mPlayback.getCurrentStreamPosition();
        String currentMediaId = mPlayback.getCurrentMediaId();
        mPlayback.stop(false, false);
        playback.setCallback(this);
        playback.setCurrentMediaId(currentMediaId);
        playback.seekTo(pos < 0 ? 0 : pos);
        playback.start();
        // Swaps instance.
        mPlayback = playback;
        switch (oldState) {
            case PlaybackStateCompat.STATE_BUFFERING:
            case PlaybackStateCompat.STATE_CONNECTING:
            case PlaybackStateCompat.STATE_PAUSED:
                mPlayback.pause();
                break;
            case PlaybackStateCompat.STATE_PLAYING:
                SongInfo currentMusic = mQueueManager.getCurrentSongInfo();
                if (resumePlaying && currentMusic != null) {
                    mPlayback.play(currentMusic);
                } else if (!resumePlaying) {
                    mPlayback.pause();
                } else {
                    mPlayback.stop(true, false);
                }
                break;
            case PlaybackStateCompat.STATE_NONE:
                break;
            default:
                break;
        }
    }

    public long getBufferedPosition() {
        long position = 0;
        if (mPlayback != null && mPlayback.isConnected()) {
            position = mPlayback.getBufferedPosition();
        }
        return position;
    }

    /**
     * 媒体操作
     */
    private class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlay() {

            handlePlayRequest();
        }

        @Override
        public void onSkipToQueueItem(long queueId) {
            mQueueManager.setCurrentQueueItem(String.valueOf(queueId), true, true);
            mQueueManager.updateMetadata();
        }

        @Override
        public void onSeekTo(long position) {
            mPlayback.seekTo((int) position);
        }

        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
//            mQueueManager.setQueueFromMusic(mediaId);
//            handlePlayRequest();
        }

        @Override
        public void onPause() {
            handlePauseRequest();
        }

        @Override
        public void onStop() {
            handleStopRequest(null, false);
        }

        @Override
        public void onSkipToNext() {
            if (mQueueManager.skipQueuePosition(1, true)) {
                handlePlayRequest();
            } else {
                handleStopRequest("Cannot skip", false);
            }
            mQueueManager.updateMetadata();
        }

        @Override
        public void onSkipToPrevious() {
            if (mQueueManager.skipQueuePosition(-1, true)) {
                handlePlayRequest();
            } else {
                handleStopRequest("Cannot skip", false);
            }
            mQueueManager.updateMetadata();
        }

        @Override
        public void onCustomAction(@NonNull String action, Bundle extras) {
            //媒体库点击
        }

        /**
         * 搜索
         * Handle free and contextual searches.
         * <p/>
         * All voice searches on Android Auto are sent to this method through a connected
         * {@link android.support.v4.media.session.MediaControllerCompat}.
         * <p/>
         * Threads and async handling:
         * Search, as a potentially slow operation, should run in another thread.
         * <p/>
         * Since this method runs on the main thread, most apps with non-trivial metadata
         * should defer the actual search to another thread (for example, by using
         * an {@link AsyncTask} as we do here).
         *
         * @param extras
         * @param query
         **/
        @Override
        public void onPlayFromSearch(final String query, final Bundle extras) {
            mPlayback.setState(PlaybackStateCompat.STATE_CONNECTING);
        }
    }


    public interface PlaybackServiceCallback {

        void onPlaybackSwitch(SongInfo info);

        void onPlaybackError(String errorMsg);

        void onPlaybackCompletion(SongInfo songInfo);

        void onNotificationRequired();

        void onPlaybackStateUpdated(int state, PlaybackStateCompat newState);
    }
}
