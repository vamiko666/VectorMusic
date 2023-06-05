package com.vova.musik.dataloaders;


import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.vova.musik.models.Song;

import java.util.ArrayList;
import java.util.List;


public class SongLoader {

    private static final String TAG = SongLoader.class.getName();

    public static List<Song> songList;

    public static Song findSongs(long id) {
        for (Song song: songList) {
            if (id == song.getId()) {
                return song;
            }
        }

        return null;
    }

    public static List<Song> getSongList(Context context) {

        songList = new ArrayList<>();
        ContentResolver musicResolver = context.getContentResolver();
        Uri musicUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Cursor musicCursor = musicResolver.query(musicUri, null, null, null, null);

        if (musicCursor != null && musicCursor.moveToFirst()) {
            int idCol = musicCursor.getColumnIndex(MediaStore.Audio.Media._ID);
            int titleCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int pathCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATA);
            int artistCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int artistIdCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID);
            int albumCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM);
            int albumIdCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID);
            int durationCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
            int musicCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC);
            int dateCol = musicCursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED);

            do {
                int isMusic = musicCursor.getInt(musicCol);
                if (isMusic != 0) {

                    long id = musicCursor.getLong(idCol);
                    String title = musicCursor.getString(titleCol);
                    String path = musicCursor.getString(pathCol);
                    String artist = musicCursor.getString(artistCol);
                    long artistId = musicCursor.getLong(artistIdCol);
                    String album = musicCursor.getString(albumCol);
                    long albumId = musicCursor.getLong(albumIdCol);
                    int duration = musicCursor.getInt(durationCol);
                    long date = musicCursor.getLong(dateCol);

                    if (title == null)
                        title = "Unknown";
                    if (artist == null)
                        artist = "Unknown";
                    if (path == null)
                        path = "Unknown";
                    if (album == null)
                        album = "Unknown";

                    Song song = new Song(id, title, path, artist, artistId, album, albumId, duration, date);
                    if (!songList.contains(song)) {

                        songList.add(song);
                    }
                }

            } while (musicCursor.moveToNext());

            musicCursor.close();
        }

        return songList;
    }
}
