
package com.vova.musik.adapters;


import android.app.Activity;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
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


public class QueueAdapter extends BaseAdapter {

    private static final String TAG = QueueAdapter.class.getName();

    private List<Song> songList;
    private LayoutInflater layoutInflater;
    private Activity activity;

    public QueueAdapter(Activity activity, List<Song> songList) {
        this.songList = songList;
        this.layoutInflater = LayoutInflater.from(activity);
        this.activity = activity;
        if (this.songList == null) {
            this.songList = new ArrayList<>();
        }
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
        return songList.get(arg0).getId();
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parentView) {

        ViewHolder viewHolder;

        if (convertView == null) {

            convertView = layoutInflater.inflate(R.layout.item_queue, parentView, false);
            viewHolder = new ViewHolder();
            viewHolder.vuMeterView = (VuMeterView) convertView.findViewById(R.id.vumeter);
            if (position % 2 == 0) {
                convertView.setBackgroundResource(R.color.gray_darker);
            } else {
                convertView.setBackgroundResource(R.color.gray);
            }

            viewHolder.cover = (ImageView) convertView.findViewById(R.id.album_cover);
            viewHolder.name = (TextView) convertView.findViewById(R.id.song_name);
            viewHolder.artist = (TextView) convertView.findViewById(R.id.song_artist);

            viewHolder.albumId = songList.get(position).getAlbumId();


            convertView.setTag(viewHolder);

        } else {

            viewHolder = (ViewHolder) convertView.getTag();
            viewHolder.albumId = songList.get(position).getAlbumId();
        }

        if (songList.get(position).getId() == MediaService.getCurrentSong()) {
            viewHolder.vuMeterView.resume(true);
        } else {
            viewHolder.vuMeterView.stop(false);
            viewHolder.vuMeterView.pause();
        }
        viewHolder.name.setText(songList.get(position).getName());
        String songDuration = String.format(
                Locale.US, "%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(songList.get(position).getDuration()),
                TimeUnit.MILLISECONDS.toSeconds(songList.get(position).getDuration()) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songList.get(position).getDuration()))
        );
        viewHolder.artist.setText(songList.get(position).getArtist() + " \u2022 " + songDuration);
        Uri uri = MediaDataUtils.reciveAlbumArt(viewHolder.albumId);
        Picasso.with(activity)
                .load(uri)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .resize(128, 128)
                .transform(new CropCircleTransformation())
                .error(R.drawable.ic_album)
                .into(viewHolder.cover);

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

    public void getContent() {
        songList = MediaService.getSongsList();
        notifyDataSetChanged();
    }

    private class ViewHolder {
        private VuMeterView vuMeterView;
        private ImageView cover;
        private TextView name;
        private TextView artist;
        private long albumId;
    }
}
