package com.example.musicbox;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;
import java.util.ArrayList;

public class DB extends SQLiteOpenHelper {

    private static SongAdapter songAdapter;
    private static Playlist playlist;
    private static int DEFAULT_INT = -1627752;

    public DB(@Nullable Context context) {
        super(context, "musicbox.db", null, 1);
    }

    @SuppressLint("SQLiteString")
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String createTableStatement = "CREATE TABLE PLAYLIST (ID INTEGER PRIMARY KEY " +
                "AUTOINCREMENT, POSITION INTEGER, SONG_NAME STRING, SINGER STRING," +
                "LINK STRING, UUID STRING, IMAGE STRING, NOW_PLAYING STRING)";
        sqLiteDatabase.execSQL(createTableStatement);

        createTableStatement = "CREATE TABLE NOW_PLAYING (ID INTEGER PRIMARY KEY " +
                "AUTOINCREMENT, NOW_POS INTEGER)";
        sqLiteDatabase.execSQL(createTableStatement);
        initDB(sqLiteDatabase);
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {}

    private void initDB(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        String table = "PLAYLIST";
        ContentValues cv2 = new ContentValues();
        String table2 = "NOW_PLAYING";
        ArrayList<Song> playlist = new ArrayList<>();

        playlist.add(new Song(
                "Too close",
                "Alex Clare",
                "https://docs.google.com/uc?export=download&id=1Qiz50Qh3dronPopwgYI3a-X8YBxOrKDF",
                "musicbox_def_too_close",
                null, null));
        playlist.add(new Song(
                "From Souvenirs To Souvenirs",
                "Demis Roussos",
                "https://drive.google.com/uc?export=download&id=1skY2YH93boraShDBEjfWtxANvzSREZ-j",
                "musicbox_def_default_image_song",
                null, null));
        playlist.add(new Song(
                "We Are The Champions",
                "Queen",
                "https://drive.google.com/uc?export=download&id=1gnkqZdN7OB2Ca7OqVyRmr2IZhAUudAzW",
                "musicbox_def_default_image_song",
                null, null));
        playlist.add(new Song(
                "It's My Life",
                "Dr. Alban",
                "https://drive.google.com/uc?export=download&id=1CWtwQOn_HZzB933i6Wi3W73NH_-Y3HJg",
                "musicbox_def_default_image_song",
                null, null));
        playlist.add(new Song(
                "All Summer Long",
                "Kid Rock",
                "https://drive.google.com/uc?export=download&id=1HpPb_CPzmafPOoG1N9EryigZcPdRBpSX",
                "musicbox_def_default_image_song",
                null, null));
        playlist.add(new Song(
                "The Show Must Go On",
                "Queen",
                "https://drive.google.com/uc?export=download&id=1EOg93RI4fmSRTBSQo1sz5MRj0DPv9Wh5",
                "musicbox_def_default_image_song",
                null, null));
        playlist.add(new Song(
                "One more cup of coffee",
                "Bob Dylan",
                "http://www.syntax.org.il/xtra/bob.m4a",
                "musicbox_def_one_more_cup",
                null, null));
        playlist.add(new Song(
                "Sara",
                "Bob Dylan",
                "http://www.syntax.org.il/xtra/bob1.m4a",
                "musicbox_def_sara",
                null, null));
        playlist.add(new Song(
                "The man in me",
                "Bob Dylan",
                "http://www.syntax.org.il/xtra/bob2.mp3",
                "musicbox_def_the_man_in_me",
                null, null));

        int position;
        for (Song song : playlist) {
            position = playlist.indexOf(song);
            cv.put("POSITION", position);
            cv.put("SONG_NAME", playlist.get(position).getSongName());
            cv.put("SINGER", playlist.get(position).getSinger());
            cv.put("LINK", playlist.get(position).getLink());
            cv.put("UUID", playlist.get(position).getUUID());
            cv.put("IMAGE", playlist.get(position).getImage());
            cv.put("NOW_PLAYING", playlist.get(position).song().get("visible"));
            db.insert(table, null, cv);
        }
        cv2.put("NOW_POS", 0);
        db.insert(table2, null, cv2);
    }

    public void updateNowPlaying(int now, int prev) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        ContentValues cv2 = new ContentValues();
        String table = "NOW_PLAYING";
        String table2 = "PLAYLIST";
        cv.put("NOW_POS", now);

        db.update(table, cv, "ID=?", new String[]{"1"});

        if (prev != -2) {
            playlist.getPlaylist().get(now).setVisible("true");
            cv2.put("NOW_PLAYING", "true");
        }
        else {
            playlist.getPlaylist().get(now).setVisible("false");
            cv2.put("NOW_PLAYING", "false");
        }
        db.update(table2, cv2, "POSITION=?", new String[]{String.valueOf(now)});
        songAdapter.notifyItemChanged(now);
        if (prev >= 0) {
            playlist.getPlaylist().get(prev).setVisible("false");
            cv2.put("NOW_PLAYING", "false");
            db.update(table2, cv2, "POSITION=?", new String[]{String.valueOf(prev)});
            songAdapter.notifyItemChanged(prev);
        }
    }

    public void setSongAdapter(SongAdapter songAdapter) {
        DB.songAdapter = songAdapter;
    }

    public void setPlaylist(Playlist playlist) {
        DB.playlist = playlist;
    }

    public int readNowPlaying() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM NOW_PLAYING";
        Cursor cursor = db.rawQuery(query, null);
        int now = DEFAULT_INT;

        if (cursor.moveToFirst()) {
            now = Integer.parseInt(cursor.getString(1));
        }
        cursor.close();
        return now;
    }

    public ArrayList<Song> readPlaylist() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM PLAYLIST ORDER BY POSITION";
        ArrayList<Song> playlist = new ArrayList<>();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                playlist.add(new Song(
                        cursor.getString(2),
                        cursor.getString(3),
                        cursor.getString(4),
                        cursor.getString(6),
                        cursor.getString(5),
                        cursor.getString(7)
                ));
            } while (cursor.moveToNext());

            cursor.close();
        } else {
            return null;
        }
        return playlist;
    }

    public void deleteSong(String UUID, int position) {
        SQLiteDatabase db = this.getWritableDatabase();
        String table = "PLAYLIST";
        db.delete(table, "UUID=?", new String[]{UUID});
        updatePlaylist(position, -1);
    }

    public void addSong(Song song, int position) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        String table = "PLAYLIST";

        for (int i = playlist.getSize() - 1; i >= position; i--) {
            cv.put("POSITION", i + 1);
            db.update(table, cv, "POSITION=?", new String[]{String.valueOf(i)});
        }

        cv.put("POSITION", position);
        cv.put("SONG_NAME", song.getSongName());
        cv.put("SINGER", song.getSinger());
        cv.put("LINK", song.getLink());
        cv.put("UUID", song.getUUID());
        cv.put("IMAGE", song.getImage());
        cv.put("NOW_PLAYING", song.song().get("visible"));
        db.insert(table, null, cv);
    }

    public String getPosition(String UUID) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT POSITION FROM PLAYLIST WHERE UUID=?";
        Cursor cursor = db.rawQuery(query, new String[]{UUID});

        if (cursor.moveToFirst()) {
            return String.valueOf(cursor.getInt(0));
        }
        return null;
    }

    public void updatePlaylist(int from, int to) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        String table = "PLAYLIST";

        if (to == -1){
            for (int i = from + 1; i <= playlist.getSize() - 1; i++) {
                cv.put("POSITION", i - 1);
                db.update(table, cv, "POSITION=?", new String[]{String.valueOf(i)});
            }
        }
        else if (from > to){
            cv.put("POSITION", DEFAULT_INT);
            db.update(table, cv, "POSITION=?", new String[]{String.valueOf(from)});

            for (int i = from - 1; i >= to; i--) {
                cv.put("POSITION", i + 1);
                db.update(table, cv, "POSITION=?", new String[]{String.valueOf(i)});
            }

            cv.put("POSITION", to);
            db.update(table, cv, "POSITION=?", new String[]{String.valueOf(DEFAULT_INT)});
        }
        else {
            cv.put("POSITION", DEFAULT_INT);
            db.update(table, cv, "POSITION=?", new String[]{String.valueOf(from)});

            for (int i = from + 1; i <= to; i++) {
                cv.put("POSITION", i - 1);
                db.update(table, cv, "POSITION=?", new String[]{String.valueOf(i)});
            }

            cv.put("POSITION", to);
            db.update(table, cv, "POSITION=?", new String[]{String.valueOf(DEFAULT_INT)});
        }
    }
}


