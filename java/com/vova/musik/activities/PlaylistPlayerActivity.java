package com.vova.musik.activities;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ListView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vova.musik.R;
import com.vova.musik.adapters.PlaylistSelectionAdapter;
import com.vova.musik.adapters.SongPlainAdapter;
import com.vova.musik.dataloaders.PlaylistLoader;
import com.vova.musik.models.Playlist;
import com.vova.musik.models.Song;
import com.vova.musik.utils.Constants;
import com.vova.musik.utils.MediaControlUtils;
import com.vova.musik.utils.MediaDataUtils;
import com.vova.musik.utils.NavUtils;
import com.vova.musik.utils.ShareUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;

import java.util.ArrayList;
import java.util.List;


public class PlaylistPlayerActivity extends ListPlayerActivity {

    private static final String TAG = PlaylistPlayerActivity.class.getName();
    private final Activity activity = this;

    private Playlist playlist;
    private PlaylistSelectionAdapter selectionAdapter;

    private boolean isUpdated;

    @Override
    protected void onCreate(Bundle savedInstanceSate) {
        super.onCreate(savedInstanceSate);

        startMultiChoice();
    }

    private void startMultiChoice() {

        selectionAdapter = new PlaylistSelectionAdapter(activity, R.layout.item_playlist_song_selected, songList);
        if (playlist.getId() > 0) {
            songListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
            songListView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {

                    int numChecked = songListView.getCheckedItemCount();
                    mode.setTitle(numChecked + " Selected");
                    selectionAdapter.chageSelection(position);
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {

                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.selection_menu_playlist, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

                    int index = songListView.getFirstVisiblePosition();
                    View v = songListView.getChildAt(0);
                    int top = (v == null) ? 0 : (v.getTop() - songListView.getPaddingTop());
                    songListView.setAdapter(selectionAdapter);
                    songListView.setSelectionFromTop(index, top);
                    return true;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.delete: {
                            List<Integer> temp = selectionAdapter.getChosenSongs();
                            List<Long> selected = new ArrayList<>();
                            for (Integer idx : temp) {
                                selected.add(songList.get(idx).getId());
                            }
                            MediaDataUtils.removePlaylistSongs(playlist.getId(), selected, activity);
                            UpdateDisplayData();
                            mode.finish();
                            return true;
                        }
                        case R.id.add_to_queue: {
                            List<Integer> temp = selectionAdapter.getChosenSongs();
                            List<Song> selected = new ArrayList<>();
                            for (Integer idx : temp) {
                                selected.add(songList.get(idx));
                            }
                            MediaControlUtils.OnaddToQueue(activity, selected);
                            return true;
                        }
                        case R.id.play_all: {

                            List<Integer> selected = selectionAdapter.getChosenSongs();
                            List<Song> tempList = new ArrayList<>();
                            for (Integer integer : selected) {
                                tempList.add(songList.get(integer));
                            }
                            MediaControlUtils.OnstartQueueShuffled(activity, tempList);
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    selectionAdapter.restartSelection();
                    int index = songListView.getFirstVisiblePosition();
                    View v = songListView.getChildAt(0);
                    int top = (v == null) ? 0 : (v.getTop() - songListView.getPaddingTop());
                    songListView.setAdapter(new SongPlainAdapter(activity, songList));
                    songListView.setSelectionFromTop(index, top);
                    ((PlaylistPlayerActivity) activity).getSupportActionBar().show();
                }
            });
        }
    }

    @Override
    protected void getDisplayData() {

        long id = getIntent().getLongExtra(Constants.PLAYLIST_ID, 0);
        playlist = PlaylistLoader.findPlaylists(id);
    }

    @Override
    protected void showDisplayData() {

        if (playlist != null) {

            listName.setText(playlist.getName());
            songList = MediaDataUtils.reciveSongsFromPlaylist(playlist.getId(), activity);

            if (songList.size() > 0) {

                Song song = songList.get((int) (Math.random() * songList.size()));
                if (song != null) {
                    Glide.with(activity)
                            .load(MediaDataUtils.reciveAlbumArt(song.getAlbumId()))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(containerBackground);
                }

                songListView.setAdapter(new SongPlainAdapter(activity, songList));
                if (songList.size() % 2 == 0) {
                    containerBackground.setBackgroundResource(R.color.gray_darker);
                } else {
                    containerBackground.setBackgroundResource(R.color.gray);
                }
            } else {
                Glide.with(activity)
                        .load(R.drawable.nav_background)
                        .into(containerBackground);
            }
        }
    }

    @Override
    protected void UpdateDisplayData() {
        isUpdated = true;
        List<Playlist> playlists = PlaylistLoader.getPlaylistsList(activity);
        for (Playlist p: playlists) {
            if (p.getId() == playlist.getId()) {
                playlist = p;
                songList = MediaDataUtils.reciveSongsFromPlaylist(playlist.getId(), activity);
            }
        }

        if (songList.size() > 0) {

            Song song = songList.get((int) (Math.random() * songList.size()));
            Glide.with(activity)
                    .load(MediaDataUtils.reciveAlbumArt(song.getAlbumId()))
                    .into(containerBackground);

            songListView.setAdapter(new SongPlainAdapter(activity, songList));
            if (songList.size() % 2 == 0) {
                containerBackground.setBackgroundResource(R.color.gray_darker);
            } else {
                containerBackground.setBackgroundResource(R.color.gray);
            }

        } else {
            Glide.with(activity)
                    .load(R.drawable.nav_background)
                    .into(containerBackground);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                if (isUpdated) {
                    Intent intent = new Intent();
                    intent.putExtra(Constants.RESULT_DEFAULT, true);
                    setResult(RESULT_OK, intent);
                } else {
                    Intent intent = new Intent();
                    intent.putExtra(Constants.RESULT_DEFAULT, false);
                    setResult(RESULT_OK, intent);
                }
                finish();
                break;
            }
            case R.id.share: {
                ShareUtils.shareTrackList(activity, songList);
                break;
            }
            case R.id.equalizer: {
                NavUtils.goToEqualizer(activity);
                return true;
            }
            case R.id.search: {
                NavUtils.goToSearch(activity);
                return true;
            }
        }

        return false;
    }

    @Override
    public void onDestroy() {
        PicassoTools.clearCache(Picasso.with(this));
        System.gc();
        super.onDestroy();
    }
}
