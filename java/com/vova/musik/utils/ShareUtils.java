package com.vova.musik.utils;


import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.vova.musik.dataloaders.SongLoader;
import com.vova.musik.models.Song;

import java.util.ArrayList;
import java.util.List;


public class ShareUtils {

    public static void shareTrack(Activity activity, long id) {
        Song song = SongLoader.findSongs(id);
        if (song != null) {
            Uri uri = Uri.parse("file:///" + song.getPath());
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.putExtra(Intent.EXTRA_SUBJECT, "Track from Echo Music!");
            intent.setType("audio/*");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            activity.startActivity(Intent.createChooser(intent, "Share from Echo Music using"));
        }
    }

    public static void shareTrackList(Activity activity, List<Song> songList) {


        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_SEND_MULTIPLE);
        intent.putExtra(Intent.EXTRA_SUBJECT, "Tracks from Echo Music!");
        intent.setType("audio/*");


        ArrayList<Uri> files = new ArrayList<>();
        for(Song song: songList) {
            Uri uri = Uri.parse("file:///" + song.getPath());
            files.add(uri);
        }

        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
        activity.startActivity(Intent.createChooser(intent, "Share from Echo Music using"));
    }
}
