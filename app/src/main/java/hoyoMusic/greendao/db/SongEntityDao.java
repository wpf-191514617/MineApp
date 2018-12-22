package hoyoMusic.greendao.db;

import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import org.greenrobot.greendao.AbstractDao;
import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.internal.DaoConfig;
import org.greenrobot.greendao.database.Database;
import org.greenrobot.greendao.database.DatabaseStatement;

import com.example.administrator.mineapp.SongEntity;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table "SONG_ENTITY".
*/
public class SongEntityDao extends AbstractDao<SongEntity, Long> {

    public static final String TABLENAME = "SONG_ENTITY";

    /**
     * Properties of entity SongEntity.<br/>
     * Can be used for QueryBuilder and for referencing column names.
     */
    public static class Properties {
        public final static Property Id = new Property(0, long.class, "id", true, "_id");
        public final static Property MusicTitle = new Property(1, String.class, "musicTitle", false, "MUSIC_TITLE");
        public final static Property Url = new Property(2, String.class, "url", false, "URL");
        public final static Property Singer = new Property(3, String.class, "singer", false, "SINGER");
        public final static Property SingerPath = new Property(4, String.class, "singerPath", false, "SINGER_PATH");
        public final static Property Album = new Property(5, String.class, "album", false, "ALBUM");
        public final static Property AlbumPath = new Property(6, String.class, "albumPath", false, "ALBUM_PATH");
        public final static Property Duration = new Property(7, long.class, "duration", false, "DURATION");
        public final static Property SongId = new Property(8, String.class, "songId", false, "SONG_ID");
    }


    public SongEntityDao(DaoConfig config) {
        super(config);
    }
    
    public SongEntityDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
    }

    /** Creates the underlying database table. */
    public static void createTable(Database db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "\"SONG_ENTITY\" (" + //
                "\"_id\" INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL ," + // 0: id
                "\"MUSIC_TITLE\" TEXT," + // 1: musicTitle
                "\"URL\" TEXT," + // 2: url
                "\"SINGER\" TEXT," + // 3: singer
                "\"SINGER_PATH\" TEXT," + // 4: singerPath
                "\"ALBUM\" TEXT," + // 5: album
                "\"ALBUM_PATH\" TEXT," + // 6: albumPath
                "\"DURATION\" INTEGER NOT NULL ," + // 7: duration
                "\"SONG_ID\" TEXT);"); // 8: songId
    }

    /** Drops the underlying database table. */
    public static void dropTable(Database db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "\"SONG_ENTITY\"";
        db.execSQL(sql);
    }

    @Override
    protected final void bindValues(DatabaseStatement stmt, SongEntity entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
 
        String musicTitle = entity.getMusicTitle();
        if (musicTitle != null) {
            stmt.bindString(2, musicTitle);
        }
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(3, url);
        }
 
        String singer = entity.getSinger();
        if (singer != null) {
            stmt.bindString(4, singer);
        }
 
        String singerPath = entity.getSingerPath();
        if (singerPath != null) {
            stmt.bindString(5, singerPath);
        }
 
        String album = entity.getAlbum();
        if (album != null) {
            stmt.bindString(6, album);
        }
 
        String albumPath = entity.getAlbumPath();
        if (albumPath != null) {
            stmt.bindString(7, albumPath);
        }
        stmt.bindLong(8, entity.getDuration());
 
        String songId = entity.getSongId();
        if (songId != null) {
            stmt.bindString(9, songId);
        }
    }

    @Override
    protected final void bindValues(SQLiteStatement stmt, SongEntity entity) {
        stmt.clearBindings();
        stmt.bindLong(1, entity.getId());
 
        String musicTitle = entity.getMusicTitle();
        if (musicTitle != null) {
            stmt.bindString(2, musicTitle);
        }
 
        String url = entity.getUrl();
        if (url != null) {
            stmt.bindString(3, url);
        }
 
        String singer = entity.getSinger();
        if (singer != null) {
            stmt.bindString(4, singer);
        }
 
        String singerPath = entity.getSingerPath();
        if (singerPath != null) {
            stmt.bindString(5, singerPath);
        }
 
        String album = entity.getAlbum();
        if (album != null) {
            stmt.bindString(6, album);
        }
 
        String albumPath = entity.getAlbumPath();
        if (albumPath != null) {
            stmt.bindString(7, albumPath);
        }
        stmt.bindLong(8, entity.getDuration());
 
        String songId = entity.getSongId();
        if (songId != null) {
            stmt.bindString(9, songId);
        }
    }

    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.getLong(offset + 0);
    }    

    @Override
    public SongEntity readEntity(Cursor cursor, int offset) {
        SongEntity entity = new SongEntity( //
            cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // musicTitle
            cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2), // url
            cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3), // singer
            cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4), // singerPath
            cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5), // album
            cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6), // albumPath
            cursor.getLong(offset + 7), // duration
            cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8) // songId
        );
        return entity;
    }
     
    @Override
    public void readEntity(Cursor cursor, SongEntity entity, int offset) {
        entity.setId(cursor.getLong(offset + 0));
        entity.setMusicTitle(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setUrl(cursor.isNull(offset + 2) ? null : cursor.getString(offset + 2));
        entity.setSinger(cursor.isNull(offset + 3) ? null : cursor.getString(offset + 3));
        entity.setSingerPath(cursor.isNull(offset + 4) ? null : cursor.getString(offset + 4));
        entity.setAlbum(cursor.isNull(offset + 5) ? null : cursor.getString(offset + 5));
        entity.setAlbumPath(cursor.isNull(offset + 6) ? null : cursor.getString(offset + 6));
        entity.setDuration(cursor.getLong(offset + 7));
        entity.setSongId(cursor.isNull(offset + 8) ? null : cursor.getString(offset + 8));
     }
    
    @Override
    protected final Long updateKeyAfterInsert(SongEntity entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    @Override
    public Long getKey(SongEntity entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    @Override
    public boolean hasKey(SongEntity entity) {
        throw new UnsupportedOperationException("Unsupported for entities with a non-null key");
    }

    @Override
    protected final boolean isEntityUpdateable() {
        return true;
    }
    
}