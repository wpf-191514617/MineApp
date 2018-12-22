package com.example.administrator.mineapp;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

import hoyoMusic.greendao.db.DaoMaster;
import hoyoMusic.greendao.db.DaoSession;
import hoyoMusic.greendao.db.SongEntityDao;

public class SongDBHelper {

    private static SongDBHelper mHelper;

    private SongEntityDao mSongEntityDao;

    private SongDBHelper() {
    }

    public static SongDBHelper getHelper() {
        if (mHelper == null) {
            synchronized (SongDBHelper.class) {
                if (mHelper == null) {
                    mHelper = new SongDBHelper();
                }
            }
        }
        return mHelper;
    }

    public void init(Application application) {
        DaoMaster.DevOpenHelper helper = new DaoMaster.DevOpenHelper(application, "hoyomusic.db", null);
        SQLiteDatabase database = helper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        DaoSession daoSession = daoMaster.newSession();
        mSongEntityDao = daoSession.getSongEntityDao();
    }

    // 添加数据，如果有   替换
    public void insertSong(SongEntity songEntity) {
        mSongEntityDao.insertOrReplace(songEntity);
    }

    public void insertSongs(List<SongEntity> songEntityList) {
        mSongEntityDao.insertInTx(songEntityList);
    }

    // 删除一条歌曲
    public void removeSong(SongEntity songEntity) {
        mSongEntityDao.delete(songEntity);
    }

    // 根据id 删除一条歌曲
    public void removeSongByKey(long id) {
        mSongEntityDao.deleteByKey(id);
    }

    // 清空表数据
    public void removeAll() {
        mSongEntityDao.deleteAll();
    }


    public void updateSong(SongEntity songEntity) {
        mSongEntityDao.update(songEntity);
    }


    public SongEntity query(long id) {
        return mSongEntityDao.load(id);
    }

    /**
     * 根据播放链接，查询歌曲
     *
     * @param url
     * @return
     */
    public SongEntity query(String url) {
        List<SongEntity> list = mSongEntityDao.queryBuilder().where(SongEntityDao.Properties.Url.eq(url)).list();
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }


    public List<SongEntity> queryAll() {
        return mSongEntityDao.loadAll();
    }


}
