package com.example.administrator.mineapp;

import com.hoyo.musiclibrary.aidl.model.SongInfo;

import java.util.ArrayList;
import java.util.List;

public class SongInfoMapper {


    public static SongInfo transform(SongEntity songEntity){
        if (songEntity == null){
            throw new IllegalArgumentException("Cannot transform a null value");
        }
        SongInfo songInfo = null;
        try {
            songInfo = new SongInfo();
            songInfo.setSongUrl(songEntity.getUrl());
            songInfo.setSongId(String.valueOf(songEntity.getSongId()));
            songInfo.setSongName(songEntity.getMusicTitle());
        } catch (Exception e){
            e.printStackTrace();
        }
        return songInfo;
    }


    public static List<SongInfo> transform(List<SongEntity> entityList){
        List<SongInfo> songInfoList = null;
        if (entityList != null && !entityList.isEmpty()){
            songInfoList = new ArrayList<>();
            for (SongEntity songEntity : entityList){
                songInfoList.add(transform(songEntity));
            }
        }
        return songInfoList;
    }

}
