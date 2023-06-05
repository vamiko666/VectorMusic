package com.vova.musik.adapters;


import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.vova.musik.R;
import com.vova.musik.models.Song;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class SongPlainAdapter extends BaseAdapter {

    private static final String TAG = SongPlainAdapter.class.getName();

    private List<Song> songList;
    private LayoutInflater layoutInflater;
    private Activity activity;

    public SongPlainAdapter(Activity activity, List<Song> songList) {
        this.songList = songList;
        this.layoutInflater = LayoutInflater.from(activity);
        this.activity = activity;
    }

    @Override
    public int getCount() {
        return songList.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parentView) {

        if (convertView == null) {

            convertView = layoutInflater.inflate(R.layout.item_song_plain, parentView, false);
            if (position % 2 == 0) {
                convertView.setBackgroundResource(R.color.gray_darker);
            } else {
                convertView.setBackgroundResource(R.color.gray);
            }

            TextView name = (TextView) convertView.findViewById(R.id.song_name);
            TextView artist = (TextView) convertView.findViewById(R.id.song_artist);

            Song song = songList.get(position);
            if (song != null) {
                name.setText(song.getName());
                String songDuration = String.format(
                        Locale.US, "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(song.getDuration()),
                        TimeUnit.MILLISECONDS.toSeconds(song.getDuration()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(song.getDuration()))
                );
                artist.setText(song.getArtist() + " \u2022 " + songDuration);
            }
        }

        return convertView;
    }

    @Override
    public int getViewTypeCount() {
        if (getCount() == 0) {
            return 1;
        }

        return getCount();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
