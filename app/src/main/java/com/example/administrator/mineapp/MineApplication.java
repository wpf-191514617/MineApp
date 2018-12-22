package com.example.administrator.mineapp;

import android.app.Application;

public class MineApplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        SongDBHelper.getHelper().init(this);
        PlayManager.getPlayManager().init(this);
    }


}
