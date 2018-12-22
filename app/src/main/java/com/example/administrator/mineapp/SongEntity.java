package com.example.administrator.mineapp;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class SongEntity {

    @Id(autoincrement = true)
    private long id; // ID

    private String musicTitle; // 歌曲名称

    private String url; // 歌曲链接

    private String singer;  //歌手

    private String singerPath; // 歌手图片

    private String album;   //专辑

    private String albumPath; //专辑图片路径

    private long duration; // 时常

    private String songId;

    @Generated(hash = 968946692)
    public SongEntity(long id, String musicTitle, String url, String singer,
            String singerPath, String album, String albumPath, long duration,
            String songId) {
        this.id = id;
        this.musicTitle = musicTitle;
        this.url = url;
        this.singer = singer;
        this.singerPath = singerPath;
        this.album = album;
        this.albumPath = albumPath;
        this.duration = duration;
        this.songId = songId;
    }

    @Generated(hash = 274420887)
    public SongEntity() {
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMusicTitle() {
        return this.musicTitle;
    }

    public void setMusicTitle(String musicTitle) {
        this.musicTitle = musicTitle;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSinger() {
        return this.singer;
    }

    public void setSinger(String singer) {
        this.singer = singer;
    }

    public String getSingerPath() {
        return this.singerPath;
    }

    public void setSingerPath(String singerPath) {
        this.singerPath = singerPath;
    }

    public String getAlbum() {
        return this.album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getAlbumPath() {
        return this.albumPath;
    }

    public void setAlbumPath(String albumPath) {
        this.albumPath = albumPath;
    }

    public long getDuration() {
        return this.duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getSongId() {
        return this.songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    

}
