package com.vova.musik.utils;


import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Log;

import com.vova.musik.R;
import com.vova.musik.databases.RecentlyPlayedPair;
import com.vova.musik.databases.RecentlyPlayedStore;
import com.vova.musik.databases.TopTracksPair;
import com.vova.musik.databases.TopTracksStore;
import com.vova.musik.dataloaders.SongLoader;
import com.vova.musik.models.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class MediaDataUtils {

    private static final String TAG = MediaDataUtils.class.getName();

    private static final int NUM_OF_DAYS = 7;

    /*
    SONG UTILITIES:
     */
    public static void removeSong(long id, String path, Context context) {

        // Delete the file
        File file = new File(path);
        if (!file.delete()) {
            Log.e(TAG, "Failed to delete song?");
        }

        context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "_id=" + id, null);
    }

    public static void removeSongList(List<Song> songList, Context context) {

        for (Song song: songList) {

 
            File file = new File(song.getPath());
            if (!file.delete()) {
                Log.e(TAG, "Failed to delete song?");
            }

            context.getContentResolver().delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, "_id=" + song.getId(), null);
        }
    }

    public static void changeSOngName(long id, String name, Context context) {

  
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.TITLE, name);

   
        context.getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, "_id=" + id, null);
    }

    public static void changeAlbum(long id, String album, Context context) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.ALBUM, album);

        context.getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, "_id=" + id, null);
    }

    public static void changeArtist(long id, String artist, Context context) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.ARTIST, artist);
        
        context.getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, "_id=" + id, null);
    }

 
    
 
    public static long MakePlaylist(String name, Context context) {

        ContentResolver resolver = context.getContentResolver();
        String[] projection = new String[]{ MediaStore.Audio.PlaylistsColumns.NAME };
        String selection = MediaStore.Audio.PlaylistsColumns.NAME + " = '" + name + "'";
        Cursor cursor = resolver.query(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                projection, selection, null, null);

        if (cursor != null) {
            if (cursor.getCount() <= 0) {
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Audio.PlaylistsColumns.NAME, name);
                Uri uri = resolver.insert(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
                        values);
                if (uri != null) {
                    cursor.close();
                    return Long.parseLong(uri.getLastPathSegment());
                }
            }
            cursor.close();
        }

        return 0;
    }

    public static void removePlaylist(long id, Context context) {

        context.getContentResolver().delete(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, "_id=" + id, null);
    }

    public static void removePlaylistSongs(long playlistId, List<Long> selected, Context context) {

        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId);
        ContentResolver resolver = context.getContentResolver();
        for (Long id: selected) {
            String[] loc = { Long.toString(id) };
            resolver.delete(uri, MediaStore.Audio.Playlists.Members.AUDIO_ID + "=?", loc);
        }
    }

    public static void changePlaylist(long id, String name, Context context) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Playlists.NAME, name);

        context.getContentResolver().update(MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI, values, "_id=" + id, null);
    }

    public static List<Song> reciveSongsFromPlaylist(long playlistId, Context context) {

        List<Song> songList;

        if (playlistId == Constants.LAST_ADDED_ID)
            songList = recieveSongsFromLastAddedPlaylist();
        else if (playlistId == Constants.RECENTLY_PLAYED_ID)
            songList = recieveSongsFromRecentlyPlayedPlaylist(context);
        else if (playlistId == Constants.TOP_TRACKS_ID)
            songList = reciveSongsFromTopTracksPlaylist(context);
        else
            songList = reciveSongsFromDefaultPlaylist(playlistId, context);

        return songList;
    }

    private static List<Song> recieveSongsFromLastAddedPlaylist() {

        List<Song> songList = new ArrayList<>();
        for (Song song: SongLoader.songList) {
            if (song.getDate() > (System.currentTimeMillis()/1000 - NUM_OF_DAYS*3600*24))
                songList.add(song);
        }

        return songList;
    }

    private static List<Song> recieveSongsFromRecentlyPlayedPlaylist(Context context) {

        RecentlyPlayedStore store = new RecentlyPlayedStore(context);
        List<RecentlyPlayedPair> pairList = store.getPairsList();
        Collections.sort(pairList, new Comparator<RecentlyPlayedPair>() {
            public int compare(RecentlyPlayedPair pair1, RecentlyPlayedPair pair2) {
                return pair2.getValues() - pair1.getValues();
            }
        });


        List<Song> songList = new ArrayList<>();
        for (int i = 0; i < pairList.size(); i++) {
            songList.add(SongLoader.findSongs(pairList.get(i).getID()));
        }

        return songList;
    }

    private static List<Song> reciveSongsFromTopTracksPlaylist(Context context) {

        TopTracksStore store = new TopTracksStore(context);
        List<TopTracksPair> pairList = store.getPairsList();
        Collections.sort(pairList, new Comparator<TopTracksPair>() {
            public int compare(TopTracksPair pair1, TopTracksPair pair2) {
                return pair2.getValues() - pair1.getValues();
            }
        });

        int num = (int) (pairList.size()*0.3);
        if (num < 3)
            num = pairList.size();
        else if (num > 6)
            num = 6;

        List<Song> songList = new ArrayList<>();
        for (int i = 0; i < num; i++) {
            songList.add(SongLoader.findSongs(pairList.get(i).getID()));
        }

        return songList;
    }
    private static List<Song> reciveSongsFromDefaultPlaylist(long playlistId, Context context) {

        List<Song> songList = new ArrayList<>();

        String[] projection = {
                MediaStore.Audio.Playlists.Members.AUDIO_ID,
                MediaStore.Audio.Playlists.Members.TITLE,
                MediaStore.Audio.Playlists.Members.DATA,
                MediaStore.Audio.Playlists.Members.ARTIST,
                MediaStore.Audio.Playlists.Members.ARTIST_ID,
                MediaStore.Audio.Playlists.Members.ALBUM,
                MediaStore.Audio.Playlists.Members.ALBUM_ID,
                MediaStore.Audio.Playlists.Members.DURATION,
                MediaStore.Audio.Playlists.Members.DATE_ADDED
        };
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId), projection, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {

                long id = cursor.getLong(0);
                String title = cursor.getString(1);
                String path = cursor.getString(2);
                String artist = cursor.getString(3);
                long artistId = cursor.getLong(4);
                String album = cursor.getString(5);
                long albumId = cursor.getLong(6);
                int duration = cursor.getInt(7);
                long date = cursor.getLong(8);

                MediaPlayer mp = MediaPlayer.create(context, Uri.parse(path));
                if (mp != null) {

                    Song song = new Song(id, title, path, artist, artistId, album, albumId, duration, date);
                    songList.add(song);
                }

            } while (cursor.moveToNext());
            cursor.close();
        }

        return songList;
    }

    public static void addToPlaylist(long id, List<Song> songList, Context context) {

        if (songList == null || songList.size() == 0)
            return;

        int count = getCountPlaylistId(context, id);
        ContentValues[] values = new ContentValues[songList.size()];
        for (int i = 0; i < songList.size(); i++) {
            values[i] = new ContentValues();
            values[i].put(MediaStore.Audio.Playlists.Members.PLAY_ORDER, i+count+1);
            values[i].put(MediaStore.Audio.Playlists.Members.AUDIO_ID, songList.get(i).getId());
        }

        Uri uri = MediaStore.Audio.Playlists.Members.getContentUri("external", id);
        ContentResolver resolver = context.getContentResolver();
        resolver.bulkInsert(uri, values);
        resolver.notifyChange(Uri.parse("content://media"), null);
    }

    private static int getCountPlaylistId(final Context context, final long playlistId) {

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Audio.Playlists.Members.getContentUri("external", playlistId),
                new String[]{BaseColumns._ID}, MediaStore.Audio.AudioColumns.IS_MUSIC + "=1"
                        + " AND " + MediaStore.Audio.AudioColumns.TITLE + " != ''", null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                return cursor.getCount();
            }
            cursor.close();
        }

        return 0;
    }


  
    public static void changeAlbumName(long inputId, String name, Context context) {

        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.ALBUM, name);
        context.getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, MediaStore.Audio.Media.ALBUM_ID + "=" + inputId, null);
    }

    public static List<Song> getSongsFromAlbum(long inputAlbumId, Context context) {

        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATE_ADDED
        };
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
                "is_music != 0 and album_id = " + inputAlbumId, null, null);

        List<Song> songList = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {

                long id = cursor.getLong(0);
                String title = cursor.getString(1);
                String path = cursor.getString(2);
                String artist = cursor.getString(3);
                long artistId = cursor.getLong(4);
                String album = cursor.getString(5);
                long albumId = cursor.getLong(6);
                int duration = cursor.getInt(7);
                int date = cursor.getInt(8);

                MediaPlayer mp = MediaPlayer.create(context, Uri.parse(path));
                if (mp != null) {

                    Song song = new Song(id, title, path, artist, artistId, album, albumId, duration, date);
                    songList.add(song);
                }

            } while (cursor.moveToNext());
            cursor.close();
        }

        return songList;
    }

    public static Uri reciveAlbumArt(long albumId) {
        Uri uri =  ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), albumId);
        if (uri != null) {
         
            return uri;
        } else {
            return Uri.parse("android.resource://com.lunchareas.echo/" + R.drawable.ic_album);
        }
    }

    public static void changeAlbumArt(String path, long albumId, Context context) {

        Uri artworkUri = Uri.parse("content://media/external/audio/albumart");
        try {
            context.getContentResolver().delete(ContentUris.withAppendedId(artworkUri, albumId), null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Reinsert data
        if (new File(path).exists()) {
            ContentValues values = new ContentValues();
            values.put("album_id", albumId);
            values.put("_data", path);
            context.getContentResolver().insert(artworkUri, values);
        }
    }

   



    public static void ChangeArtist(long inputId, String name, Context context) {

        // Update all names of albums in songs
        ContentValues values = new ContentValues();
        values.put(MediaStore.Audio.Media.ARTIST, name);
        context.getContentResolver().update(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values, MediaStore.Audio.Media.ARTIST_ID + "=" + inputId, null);
    }

    public static List<Song> getArtist(long inputArtistId, Context context) {

        // Get data
        String[] projection = {
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ARTIST_ID,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.DATE_ADDED
        };
        Cursor cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,
                "is_music != 0 and artist_id = " + inputArtistId, null, null);


        List<Song> songList = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {

                // Get the data
                long id = cursor.getLong(0);
                String title = cursor.getString(1);
                String path = cursor.getString(2);
                String artist = cursor.getString(3);
                long artistId = cursor.getLong(4);
                String album = cursor.getString(5);
                long albumId = cursor.getLong(6);
                int duration = cursor.getInt(7);
                int date = cursor.getInt(8);

                MediaPlayer mp = MediaPlayer.create(context, Uri.parse(path));
                if (mp != null) {

                    Song song = new Song(id, title, path, artist, artistId, album, albumId, duration, date);
                    songList.add(song);
                }

            } while (cursor.moveToNext());
            cursor.close();
        }

        return songList;
    }
}
