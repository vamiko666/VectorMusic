
package com.vova.musik.adapters;


import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.support.v7.widget.PopupMenu;
import android.widget.TextView;

import com.vova.musik.R;
import com.vova.musik.activities.MainActivity;
import com.vova.musik.dialogs.PlaylistAddSongsDialog;
import com.vova.musik.dialogs.SongChangeAlbumDialog;
import com.vova.musik.dialogs.SongChangeArtistDialog;
import com.vova.musik.dialogs.SongRenameDialog;
import com.vova.musik.models.Song;
import com.vova.musik.utils.Constants;
import com.vova.musik.utils.MediaControlUtils;
import com.vova.musik.utils.MediaDataUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class SongAdapter extends BaseAdapter {

    private static final String TAG = SongAdapter.class.getName();

    private List<Song> songList;
    private LayoutInflater layoutInflater;
    private Activity activity;

    public SongAdapter(Activity activity, List<Song> songList) {
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

            convertView = layoutInflater.inflate(R.layout.item_song, parentView, false);
            if (position % 2 == 0) {
                convertView.setBackgroundResource(R.color.gray_darker);
            } else {
                convertView.setBackgroundResource(R.color.gray);
            }

            TextView name = (TextView) convertView.findViewById(R.id.song_name);
            TextView artist = (TextView) convertView.findViewById(R.id.song_artist);
            ImageView overflow = (ImageView) convertView.findViewById(R.id.song_duration);

            Song song = songList.get(position);
            name.setText(song.getName());
            String songDuration = String.format(
                    Locale.US, "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(song.getDuration()),
                    TimeUnit.MILLISECONDS.toSeconds(song.getDuration()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(song.getDuration()))
            );
            artist.setText(song.getArtist() + " \u2022 " + songDuration);
            overflow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showMenu(v, position);
                }
            });
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

    private void showMenu(View view, final int pos) {
        final PopupMenu popupMenu = new PopupMenu(activity, view, Gravity.END);
        final Song song = songList.get(pos);

        // Handle individual clicks
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.song_play: {
                        MediaControlUtils.Start(activity, songList, pos);
                        ((MainActivity) activity).makeMusicBounds(true);
                        return true;
                    }
                    case R.id.song_rename: {
                        Bundle bundle = new Bundle();
                        bundle.putLong(Constants.SONG_ID, song.getId());
                        DialogFragment dialogFragment = new SongRenameDialog();
                        dialogFragment.setArguments(bundle);
                        dialogFragment.show(activity.getFragmentManager(), "RenameSong");
                        return true;
                    }
                    case R.id.song_delete: {
                        MediaDataUtils.removeSong(song.getId(), song.getPath(), activity);
                        ((MainActivity) activity).getAll();
                        return true;
                    }
                    case R.id.song_album: {
                        Bundle bundle = new Bundle();
                        bundle.putLong(Constants.SONG_ID, song.getId());
                        DialogFragment dialogFragment = new SongChangeAlbumDialog();
                        dialogFragment.setArguments(bundle);
                        dialogFragment.show(activity.getFragmentManager(), "ChangeSongAlbum");
                        return true;
                    }
                    case R.id.song_artist: {
                        Bundle bundle = new Bundle();
                        bundle.putLong(Constants.SONG_ID, song.getId());
                        DialogFragment dialogFragment = new SongChangeArtistDialog();
                        dialogFragment.setArguments(bundle);
                        dialogFragment.show(activity.getFragmentManager(), "ChangeSongArtist");
                        return true;
                    }
                    case R.id.song_playlist: {
                        Bundle bundle = new Bundle();
                        ArrayList<Song> tempList = new ArrayList<>();
                        tempList.add(song);
                        bundle.putParcelableArrayList(Constants.SONG_LIST, tempList);
                        DialogFragment dialogFragment = new PlaylistAddSongsDialog();
                        dialogFragment.setArguments(bundle);
                        dialogFragment.show(activity.getFragmentManager(), "AddToPlaylist");
                        return true;
                    }
                    case R.id.add_to_queue: {
                        MediaControlUtils.OnaddToQueue(activity, song);
                        return true;
                    }
                }

                return false;
            }
        });

        MenuInflater menuInflater = popupMenu.getMenuInflater();
        menuInflater.inflate(R.menu.choice_menu_song, popupMenu.getMenu());
        popupMenu.show();
    }
}
