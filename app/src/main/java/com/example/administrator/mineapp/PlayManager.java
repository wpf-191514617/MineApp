package com.example.administrator.mineapp;

import android.app.Application;

import com.hoyo.musiclibrary.aidl.model.SongInfo;
import com.hoyo.musiclibrary.manager.MusicLibrary;
import com.hoyo.musiclibrary.manager.MusicManager;
import com.hoyo.musiclibrary.notification.NotificationCreater;
import com.hoyo.musiclibrary.utils.BaseUtil;

import java.util.List;

public class PlayManager {

    private static PlayManager mPlayManager;

    private MusicLibrary musicLibrary;

    private PlayManager() {
    }

    public static PlayManager getPlayManager() {
        if (mPlayManager == null) {
            synchronized (PlayManager.class) {
                if (mPlayManager == null) {
                    mPlayManager = new PlayManager();
                }
            }
        }
        return mPlayManager;
    }

    public void init(Application application) {
        if (BaseUtil.getCurProcessName(application).equals("com.example.administrator.mineapp")) {
            NotificationCreater creater = new NotificationCreater.Builder()
                    .setTargetClass("com.example.administrator.mineapp.MainActivity")
                    .setCreateSystemNotification(true)
                    .build();
            musicLibrary = new MusicLibrary.Builder(application)
                    .setNotificationCreater(creater)
                    .build();
            musicLibrary.startMusicService();
        }
    }

    public void stopService() {
        musicLibrary.stopService();
    }

    /**
     * 播放音乐  列表 如专辑
     * @param songEntities
     */
    public void playList(List<SongEntity> songEntities) {
        playList(songEntities,0);
    }


    /**
     * 播放音乐  列表 如专辑
     * @param songEntities
     */
    public void playList(List<SongEntity> songEntities, int index) {
        MusicManager.get().playMusic(SongInfoMapper.transform(songEntities), index);
        SongDBHelper.getHelper().insertSongs(songEntities);
    }

    public void playSong(SongEntity songEntity){
        // 查询数据库播放列表当中是否存在这首歌
        SongEntity entity = SongDBHelper.getHelper().query(songEntity.getUrl());
        if (entity == null){
            SongDBHelper.getHelper().insertSong(songEntity);
        }
        List<SongInfo> infoList = MusicManager.get().getPlayList();
        if (!infoList.contains(SongInfoMapper.transform(songEntity))){
            infoList.add(SongInfoMapper.transform(songEntity));
        }
        MusicManager.get().playMusic(infoList , infoList.indexOf(SongInfoMapper.transform(songEntity)));
    }

}
