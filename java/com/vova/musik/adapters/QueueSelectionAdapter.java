
package com.vova.musik.adapters;


import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.vova.musik.R;
import com.vova.musik.models.Song;
import com.vova.musik.services.MediaService;
import com.vova.musik.utils.MediaDataUtils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.gresse.hugo.vumeterlibrary.VuMeterView;
import jp.wasabeef.picasso.transformations.CropCircleTransformation;


public class QueueSelectionAdapter extends ArrayAdapter<Song> {

    private static final String TAG = QueueSelectionAdapter.class.getName();

    private List<Song> songList;
    private LayoutInflater layoutInflater;
    private Context activity;
    private List<Integer> selectedSongs;

    public QueueSelectionAdapter(Activity activity, int resourceId, List<Song> songList) {
        super(activity, resourceId, songList);
        this.songList = songList;
        this.layoutInflater = LayoutInflater.from(activity);
        this.activity = activity;
        this.selectedSongs = new ArrayList<>();
    }

    @NonNull
    @Override
    public View getView(final int position, View convertView, @NonNull ViewGroup parentView) {

        if (convertView == null) {


            convertView = layoutInflater.inflate(R.layout.item_queue, parentView, false);
            boolean selected = selectedSongs.contains(position);
            if (selected) {
                convertView.setBackgroundResource(R.color.blue);
                convertView.setAlpha(0.7f);
            } else {
                if (position % 2 == 0) {
                    convertView.setBackgroundResource(R.color.gray_darker);
                } else {
                    convertView.setBackgroundResource(R.color.gray);
                }
            }


            ImageView cover = (ImageView) convertView.findViewById(R.id.album_cover);
            TextView name = (TextView) convertView.findViewById(R.id.song_name);
            TextView artist = (TextView) convertView.findViewById(R.id.song_artist);
            VuMeterView vumeter = (VuMeterView) convertView.findViewById(R.id.vumeter);

            Uri uri = MediaDataUtils.reciveAlbumArt(songList.get(position).getAlbumId());
            if (cover.getDrawable() == null) {
                Picasso.with(activity)
                        .load(uri)
                        .memoryPolicy(MemoryPolicy.NO_CACHE)
                        .resize(128, 128)
                        .transform(new CropCircleTransformation())
                        .error(R.drawable.ic_album)
                        .into(cover);
            }

            name.setText(songList.get(position).getName());
            String songDuration = String.format(
                    Locale.US, "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(songList.get(position).getDuration()),
                    TimeUnit.MILLISECONDS.toSeconds(songList.get(position).getDuration()) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songList.get(position).getDuration()))
            );
            artist.setText(songList.get(position).getArtist() + " \u2022 " + songDuration);

            if (songList.get(position).getId() == MediaService.getCurrentSong()) {
                vumeter.resume(true);
            } else {
                vumeter.stop(false);
                vumeter.pause();
            }

        } else {

            boolean selected = selectedSongs.contains(position);

            if (selected) {
                convertView.setBackgroundResource(R.color.blue);
                convertView.setAlpha(0.7f);
            } else {
                if (position % 2 == 0) {
                    convertView.setBackgroundResource(R.color.gray_darker);
                } else {
                    convertView.setBackgroundResource(R.color.gray);
                }
            }
        }

        return convertView;
    }

    @Override
    public void remove(Song songData) {
        songList.remove(songData);
        notifyDataSetChanged();
    }

    public void changeSelection(int pos) {

        chooseSongs(pos, !selectedSongs.contains(pos));
    }

    public void restartSelection() {
        selectedSongs = new ArrayList<>();
        notifyDataSetChanged();
    }

    public void chooseSongs(Integer pos, boolean checked) {

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
