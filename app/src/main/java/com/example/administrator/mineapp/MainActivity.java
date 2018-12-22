package com.example.administrator.mineapp;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hoyo.musiclibrary.aidl.listener.OnPlayerEventListener;
import com.hoyo.musiclibrary.aidl.model.SongInfo;
import com.hoyo.musiclibrary.constans.PlayMode;
import com.hoyo.musiclibrary.constans.State;
import com.hoyo.musiclibrary.db.SongHistoryManager;
import com.hoyo.musiclibrary.manager.MusicManager;
import com.hoyo.musiclibrary.manager.TimerTaskManager;
import com.hoyo.musiclibrary.utils.LogUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import hoyoMusic.greendao.db.DaoSession;
import hoyoMusic.greendao.db.SongEntityDao;

public class MainActivity extends AppCompatActivity implements OnPlayerEventListener {


    Button play_mode, btn_speed;
    TextView curr_info;
    SeekBar mSeekBar;
    TimerTaskManager manager;
    TextView mCurrProgress;
    int viewWidth = 0;
    int seekBarWidth = 0;
    float speed = 1;

    private List<SongInfo> mSongList; // 歌曲名称


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        manager = new TimerTaskManager();



        List<SongEntity> entityList = SongDBHelper.getHelper().queryAll();
        if (entityList == null || entityList.isEmpty()){
            entityList = initData();
            SongDBHelper.getHelper().insertSongs(entityList);
        }

        List<SongInfo> list = SongInfoMapper.transform(entityList);

        SharedPreferences sharedPreferences = getSharedPreferences("setting",0);
        String id = sharedPreferences.getString("musicId" ,"0000");

        int progress = SongHistoryManager.getImpl(this).findSongProgressById(id);

        Toast.makeText(this, "id = " + id + " , progress = " + progress, Toast.LENGTH_SHORT).show();

        /**/

        play_mode = findViewById(R.id.play_mode);
        curr_info = findViewById(R.id.curr_info);
        mSeekBar = findViewById(R.id.seekBar);
        btn_speed = findViewById(R.id.btn_speed);
        mCurrProgress = findViewById(R.id.curr_progress);
        MusicManager.get().addPlayerEventListener(this);
        findViewById(R.id.play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicManager.get().playMusic(list , 0);
                curr_info.setText(getCurrInfo("play"));
            }
        });




        findViewById(R.id.pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicManager.get().pauseMusic();
                curr_info.setText(getCurrInfo("pause"));
            }
        });
        findViewById(R.id.resume).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicManager.get().resumeMusic();
                curr_info.setText(getCurrInfo("resume"));
            }
        });
        findViewById(R.id.pre).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicManager.get().hasPre()) {
                    MusicManager.get().playPre();
                    curr_info.setText(getCurrInfo("pre"));
                } else {
                    Toast.makeText(MainActivity.this, "没有上一首", Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MusicManager.get().hasNext()) {
                    MusicManager.get().playNext();
                    curr_info.setText(getCurrInfo("next"));
                } else {
                    Toast.makeText(MainActivity.this, "没有下一首", Toast.LENGTH_SHORT).show();
                }
            }
        });
        findViewById(R.id.close_server).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlayManager.getPlayManager().stopService();
            }
        });
        play_mode.setText(getMode());
        play_mode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int mode = MusicManager.get().getPlayMode();
                if (mode == PlayMode.PLAY_IN_FLASHBACK) {
                    MusicManager.get().setPlayMode(PlayMode.PLAY_IN_LIST_LOOP);
                } else if (mode == PlayMode.PLAY_IN_LIST_LOOP) {
                    MusicManager.get().setPlayMode(PlayMode.PLAY_IN_ORDER);
                } else if (mode == PlayMode.PLAY_IN_ORDER) {
                    MusicManager.get().setPlayMode(PlayMode.PLAY_IN_RANDOM);
                } else if (mode == PlayMode.PLAY_IN_RANDOM) {
                    MusicManager.get().setPlayMode(PlayMode.PLAY_IN_SINGLE_LOOP);
                } else if (mode == PlayMode.PLAY_IN_SINGLE_LOOP) {
                    MusicManager.get().setPlayMode(PlayMode.PLAY_IN_FLASHBACK);
                }
                play_mode.setText(getMode());
                curr_info.setText(getCurrInfo("play_mode"));
            }
        });
        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicManager.get().reset();
                PlayManager.getPlayManager().stopService();
            }
        });
        curr_info.setText(getCurrInfo("curr_info"));
        manager.setUpdateProgressTask(new Runnable() {
            @Override
            public void run() {
                long progress = MusicManager.get().getProgress();
                mSeekBar.setProgress((int) progress);
                mSeekBar.setSecondaryProgress((int) MusicManager.get().getBufferedPosition());
            }
        });

        mCurrProgress.post(new Runnable() {
            @Override
            public void run() {
                viewWidth = mCurrProgress.getWidth();
                seekBarWidth = mSeekBar.getWidth();
            }
        });


        btn_speed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speed += 0.5;
                if (speed >= 3) {
                    speed = 1;
                }
                MusicManager.get().setPlaybackParameters(speed, 1);
                btn_speed.setText("变速 当前" + speed + "倍");
            }
        });

        @SuppressLint("SimpleDateFormat") final SimpleDateFormat sDateFormat = new SimpleDateFormat("mm:ss");
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int halfWidth = viewWidth / 2;
                int max = mSeekBar.getMax();
                if (max == 0 || halfWidth == 0 || progress == 0) {
                    return;
                }
                int translationX = (seekBarWidth - viewWidth) * progress / max;
                mCurrProgress.setTranslationX(translationX);
                mCurrProgress.setText(sDateFormat.format(progress) + "/" + sDateFormat.format(max));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MusicManager.get().seekTo(seekBar.getProgress());
            }
        });
    }


    private List<SongEntity> initData(){
        List<String> strings = new ArrayList<>();
        strings.add("http://www.ytmp3.cn/down/53479.mp3");
        strings.add("http://www.ytmp3.cn/down/53219.mp3");
        strings.add("http://www.ytmp3.cn/down/39077.mp3");
        strings.add("http://www.ytmp3.cn/down/54258.mp3");
        strings.add("http://www.ytmp3.cn/down/33031.mp3");
        strings.add("http://www.ytmp3.cn/down/56049.mp3");
        List<SongEntity> list = new ArrayList<>();
        for (int i = 0; i < strings.size(); i++) {
            SongEntity songInfo = new SongEntity();
            songInfo.setSongId(String.valueOf(i));
            songInfo.setUrl(strings.get(i));
            if (i == 0) {
                songInfo.setMusicTitle("末班车");
            } else if (i == 1) {
                songInfo.setMusicTitle("only my railgun");
            } else if (i == 2) {
                songInfo.setMusicTitle("零度的亲吻");
            }else if (i == 3) {
                songInfo.setMusicTitle("光辉岁月");
            }else if (i == 4) {
                songInfo.setMusicTitle("一千年以后");
            }else if (i == 5) {
                songInfo.setMusicTitle("想你想疯了");
            }
            list.add(songInfo);
        }
        return list;
    }


    private String getCurrInfo(String from) {
        LogUtil.i("from = " + from);
        StringBuilder builder = new StringBuilder();
        List<SongInfo> songInfos = MusicManager.get().getPlayList();
        if (songInfos == null) {
            return "";
        }
        for (int i = 0; i < songInfos.size(); i++) {
            builder
                    .append(String.valueOf(i))
                    .append(" ")
                    .append(songInfos.get(i).getSongName())
                    .append("\n");
        }
        SongInfo currInfo = MusicManager.get().getCurrPlayingMusic();
        String name = currInfo == null ? "没有" : currInfo.getSongName();
        return " 当前播放：" + name + "\n 播放状态：" + getStatus() + "\n 下标：" + MusicManager.get().getCurrPlayingIndex() + " \n\n" +
                "当前播放列表：\n\n" + builder.toString();
    }

    public String getStatus() {
        int status = MusicManager.get().getStatus();
        if (status == State.STATE_IDLE) {
            return "空闲";
        } else if (status == State.STATE_PLAYING) {
            return "播放中";
        } else if (status == State.STATE_PAUSED) {
            return "暂停";
        } else if (status == State.STATE_ERROR) {
            return "错误";
        } else if (status == State.STATE_STOP) {
            return "停止";
        } else if (status == State.STATE_ASYNC_LOADING) {
            return "加载中";
        }
        return "其他";
    }

    private String getMode() {
        int mode = MusicManager.get().getPlayMode();
        if (mode == PlayMode.PLAY_IN_FLASHBACK) {
            return "倒序播放";
        } else if (mode == PlayMode.PLAY_IN_LIST_LOOP) {
            return "列表循环";
        } else if (mode == PlayMode.PLAY_IN_ORDER) {
            return "顺序播放";
        } else if (mode == PlayMode.PLAY_IN_RANDOM) {
            return "随机播放";
        } else if (mode == PlayMode.PLAY_IN_SINGLE_LOOP) {
            return "单曲循环";
        }
        return "不知道什么模式";
    }

    @Override
    public void onMusicSwitch(SongInfo music) {
        curr_info.setText(getCurrInfo("onMusicSwitch"));
        manager.stopSeekBarUpdate();
        mSeekBar.setMax((int) music.getDuration());
    }

    @Override
    public void onPlayerStart() {
        curr_info.setText(getCurrInfo("onPlayerStart"));
        manager.scheduleSeekBarUpdate();
    }

    @Override
    public void onPlayerPause() {
        curr_info.setText(getCurrInfo("onPlayerPause"));
        manager.stopSeekBarUpdate();
        SongInfo songInfo = MusicManager.get().getCurrPlayingMusic();
        SongHistoryManager.getImpl(this).saveSongHistory(songInfo.getSongId() , mSeekBar.getProgress());

        SharedPreferences sharedPreferences = getSharedPreferences("setting",0);
        sharedPreferences.edit().putString("musicId",songInfo.getSongId()).commit();
    }

    @Override
    public void onPlayCompletion(SongInfo songInfo) {
        curr_info.setText(getCurrInfo("onPlayCompletion"));
        manager.stopSeekBarUpdate();
        mSeekBar.setProgress(0);
    }

    @Override
    public void onPlayerStop() {
        curr_info.setText(getCurrInfo("onPlayerStop"));
        manager.stopSeekBarUpdate();
    }

    @Override
    public void onError(String errorMsg) {
        curr_info.setText(getCurrInfo("onError"));
        manager.stopSeekBarUpdate();
    }

    @Override
    public void onAsyncLoading(boolean isFinishLoading) {
        curr_info.setText(getCurrInfo("onAsyncLoading"));
        if (isFinishLoading) {
            mSeekBar.setMax(MusicManager.get().getDuration());
        }
    }
}
