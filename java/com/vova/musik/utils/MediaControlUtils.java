package com.vova.musik.utils;


import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

import com.vova.musik.models.Song;
import com.vova.musik.services.MediaService;

import java.util.ArrayList;
import java.util.List;


public class MediaControlUtils {

    private static final String TAG = MediaControlUtils.class.getName();

    public static int audioId;

    public static Song findSong(long id) {
        if (MediaService.getSongsList() == null)
            return null;
        for (Song song: MediaService.getSongsList()) {
            if (song.getId() == id) {
                return song;
            }
        }

        Log.e(TAG, "No song found.");
        return null;
    }

    public static int findIDx(long id) {
        List<Song> songList = MediaService.getSongsList();
        for (int i = 0; i < songList.size(); i++) {
            if (songList.get(i).getId() == id) {
                return i;
            }
        }

        Log.e(TAG, "No song found.");
        return -1;
    }

    public static void startController(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (Build.VERSION.SDK_INT >= 21) {
            audioId = audioManager.generateAudioSessionId();
            if (audioId == AudioManager.ERROR) {
                Log.e(TAG, "Error in audio id.");
            } else {
                Intent intent = new Intent(context, MediaService.class);
                intent.setAction(Constants.MUSIC_INIT);
                intent.putExtra(Constants.MUSIC_ID, audioId);
                context.startService(intent);
            }
        }
    }

    public static void Start(Context context, List<Song> songs, int pos) {

        Intent musicCreateIntent = new Intent(context, MediaService.class);
        musicCreateIntent.setAction(Constants.MUSIC_CREATE);
        musicCreateIntent.putParcelableArrayListExtra(Constants.MUSIC_LIST, (ArrayList<Song>) songs);
        musicCreateIntent.putExtra(Constants.MUSIC_LIST_POS, pos);
        context.startService(musicCreateIntent);
    }

    public static void startRepeating(Context context, List<Song> songs, int pos) {

        Intent musicCreateIntent = new Intent(context, MediaService.class);
        musicCreateIntent.setAction(Constants.MUSIC_CREATE_REPEATING);
        musicCreateIntent.putParcelableArrayListExtra(Constants.MUSIC_LIST, (ArrayList<Song>) songs);
        musicCreateIntent.putExtra(Constants.MUSIC_LIST_POS, pos);
        context.startService(musicCreateIntent);
    }

    public static void Onstart(Context context) {
        Log.e(TAG, "Sending play request.");
        Intent musicStartIntent = new Intent( context, MediaService.class);
        musicStartIntent.setAction(Constants.MUSIC_PLAY);
        context.startService(musicStartIntent);
    }

    public static void OnPause(Context context) {
        Log.e(TAG, "Sending pause request.");
        Intent musicPauseIntent = new Intent(context, MediaService.class);
        musicPauseIntent.setAction(Constants.MUSIC_PAUSE);
        context.startService(musicPauseIntent);
    }

    public static void Onnext(Context context) {
        Log.e(TAG, "Sending next request.");
        Intent intent = new Intent(context, MediaService.class);
        intent.setAction(Constants.MUSIC_NEXT);
        context.startService(intent);
    }

    public static void Onprev(Context context) {
        Log.e(TAG, "Sending prev request.");
        Intent intent = new Intent(context, MediaService.class);
        intent.setAction(Constants.MUSIC_PREV);
        context.startService(intent);
    }

    public static void Onseek(Context context, int position) {
        Log.e(TAG, "Sending seek request.");
        Intent musicChangeIntent = new Intent(context, MediaService.class);
        musicChangeIntent.setAction(Constants.MUSIC_CHANGE);
        musicChangeIntent.putExtra(Constants.MUSIC_CHANGE, position);
        context.startService(musicChangeIntent);
    }

    public static void Onrepeat(Context context) {
        Log.e(TAG, "Sending repeat request.");
        Intent intent = new Intent(context, MediaService.class);
        intent.setAction(Constants.MUSIC_REPEAT);
        context.startService(intent);
    }

    public static void Onshuffle(Context context) {
        Log.e(TAG, "Sending shuffle request.");
        Intent intent = new Intent(context, MediaService.class);
        intent.setAction(Constants.MUSIC_SHUFFLE);
        context.startService(intent);
    }

    public static void OnstartQueue(Context context, List<Song> songs) {
        Intent playlistCreateIntent = new Intent(context, MediaService.class);

        playlistCreateIntent.setAction(Constants.MUSIC_DEFAULT);
        playlistCreateIntent.putParcelableArrayListExtra(Constants.MUSIC_DEFAULT, (ArrayList<Song>) songs);
        context.startService(playlistCreateIntent);
    }

    public static void OnstartQueueRepeating(Context context, List<Song> songs) {
        Intent playlistCreateIntent = new Intent(context, MediaService.class);


        playlistCreateIntent.setAction(Constants.MUSIC_DEFAULT_REPEATING);
        playlistCreateIntent.putParcelableArrayListExtra(Constants.MUSIC_DEFAULT_REPEATING, (ArrayList<Song>) songs);
        context.startService(playlistCreateIntent);
    }

    public static void OnstartQueueRepeatingSpecific(Context context, List<Song> songs, int position) {
        Intent playlistCreateIntent = new Intent(context, MediaService.class);
        playlistCreateIntent.setAction(Constants.MUSIC_DEFAULT_REPEATING_SPECIFIC);
        playlistCreateIntent.putParcelableArrayListExtra(Constants.MUSIC_DEFAULT_REPEATING, (ArrayList<Song>) songs);
        playlistCreateIntent.putExtra(Constants.MUSIC_DEFAULT_REPEATING_SPECIFIC, position);
        context.startService(playlistCreateIntent);
    }

    public static void OnstartQueueShuffled(Context context, List<Song> songs) {
        Intent playlistCreateIntent = new Intent(context, MediaService.class);

        playlistCreateIntent.setAction(Constants.MUSIC_DEFAULT_SHUFFLED);
        playlistCreateIntent.putParcelableArrayListExtra(Constants.MUSIC_DEFAULT_SHUFFLED, (ArrayList<Song>) songs);
        context.startService(playlistCreateIntent);
    }

    public static void OnstartQueueRepeatingShuffled(Context context, List<Song> songs) {
        Intent playlistCreateIntent = new Intent(context, MediaService.class);

        playlistCreateIntent.setAction(Constants.MUSIC_DEFAULT_REPEATING_SHUFFLED);
        playlistCreateIntent.putParcelableArrayListExtra(Constants.MUSIC_DEFAULT_REPEATING_SHUFFLED, (ArrayList<Song>) songs);
        context.startService(playlistCreateIntent);
    }

    public static void OnaddToQueue(Context context, List<Song> songs) {
        Intent intent = new Intent(context, MediaService.class);
        intent.setAction(Constants.MUSIC_ADD_TO_QUEUE);
        intent.putParcelableArrayListExtra(Constants.MUSIC_ADD_TO_QUEUE, (ArrayList<Song>) songs);
        context.startService(intent);
    }

    public static void OnaddToQueue(Context context, Song song) {
        List<Song> temp = new ArrayList<>();
        temp.add(song);
        Intent intent = new Intent(context, MediaService.class);
        intent.setAction(Constants.MUSIC_ADD_TO_QUEUE);
        intent.putParcelableArrayListExtra(Constants.MUSIC_ADD_TO_QUEUE, (ArrayList<Song>) temp);
        context.startService(intent);
    }

    public static void OnremoveFromQueue(Context context, List<Song> selected) {
        Intent intent = new Intent(context, MediaService.class);
        intent.setAction(Constants.MUSIC_REMOVE_FROM_QUEUE);
        intent.putParcelableArrayListExtra(Constants.MUSIC_REMOVE_FROM_QUEUE, (ArrayList<Song>) selected);
        context.startService(intent);
    }
}
