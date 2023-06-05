package com.vova.musik.adapters;


import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.vova.musik.R;
import com.vova.musik.models.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class SongSelectionAdapter extends ArrayAdapter<Song> {

    private static final String TAG = SongSelectionAdapter.class.getName();

    private List<Song> songList;
    private LayoutInflater songListInflater;
    private Context activity;
    private List<Integer> selectedSongs;

    public SongSelectionAdapter(Activity activity, int resourceId, List<Song> songList) {
        super(activity, resourceId, songList);
        this.songList = songList;
        this.songListInflater = LayoutInflater.from(activity);
        this.activity = activity;
        this.selectedSongs = new ArrayList<>();
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parentView) {

        boolean selected = selectedSongs.contains(position);
        if (selected) {
            convertView = songListInflater.inflate(R.layout.item_song_selected, parentView, false);
        } else {
            convertView = songListInflater.inflate(R.layout.item_song, parentView, false);
            if (position % 2 == 0) {
                convertView.setBackgroundResource(R.color.gray_darker);
            } else {
                convertView.setBackgroundResource(R.color.gray);
            }
        }

        TextView name = (TextView) convertView.findViewById(R.id.song_name);
        TextView artist = (TextView) convertView.findViewById(R.id.song_artist);

        Song songItem = songList.get(position);
        name.setText(songItem.getName());
        String songDuration = String.format(
                Locale.US, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(songItem.getDuration()),
                TimeUnit.MILLISECONDS.toSeconds(songItem.getDuration()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songItem.getDuration()))
        );
        artist.setText(songItem.getArtist() + " \u2022 " + songDuration);

        return convertView;
    }

    @Override
    public void remove(Song songData) {
        songList.remove(songData);
        notifyDataSetChanged();
    }

    public void changeSelection(int pos) {
        // !contains returns false if it already exists
        chooseSong(pos, !selectedSongs.contains(pos));
    }

    public void restartSelection() {
        selectedSongs = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void chooseSong(Integer pos, boolean checked) {

        if (checked) {
            selectedSongs.add(pos);
        } else {
            selectedSongs.remove(pos);
        }
        notifyDataSetChanged();
    }


    public List<Integer> getChosenSongs() {
        return selectedSongs;
    }
}
