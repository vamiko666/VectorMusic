package com.vova.musik.activities;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageSwitcher;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.vova.musik.R;
import com.vova.musik.adapters.QueueAdapter;
import com.vova.musik.adapters.QueueSelectionAdapter;
import com.vova.musik.dataloaders.SongLoader;
import com.vova.musik.dialogs.PlaylistAddSongsDialog;
import com.vova.musik.dialogs.PlaylistFromSongsDialog;
import com.vova.musik.models.Song;
import com.vova.musik.services.MediaService;
import com.vova.musik.utils.Constants;
import com.vova.musik.utils.MediaControlUtils;
import com.vova.musik.utils.NavUtils;
import com.vova.musik.utils.ShareUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class QueueActivity extends BaseActivity {

    private static final String TAG = QueueActivity.class.getName();
    private final Activity activity = this;

    private SeekBar progressBar;
    private ImageSwitcher songPlayButton;
    private ListView listView;
    private List<Song> songList;
    private Handler handler;
    private QueueAdapter queueAdapter;
    private QueueSelectionAdapter selectionAdapter;

    private boolean musicBound;
    private boolean isChanging;
    private int currSize;

    public QueueActivity() {
        super(R.layout.activity_queue);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getDisplayData();
        initSongbar();
        startMainView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateDisplayData();
    }

    @SuppressLint("NewApi")
    private void initSongbar() {

        progressBar = (SeekBar) findViewById(R.id.songbar_progress);
        progressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        progressBar.getThumb().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        songPlayButton = (ImageSwitcher) findViewById(R.id.songbar_play);
        songPlayButton.setBackgroundResource(R.drawable.ic_pause);
        musicBound = true;

        songPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicBound) {
                    MediaControlUtils.OnPause(activity);
                    songPlayButton.setBackgroundResource(R.drawable.ic_play);
                } else {
                    MediaControlUtils.Onstart(activity);
                    songPlayButton.setBackgroundResource(R.drawable.ic_pause);
                }
            }
        });


        progressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar sb, int position, boolean userPressed) {
                if (userPressed) {
                    MediaControlUtils.Onseek(activity, position);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isChanging = true;
                MediaControlUtils.OnPause(activity);
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                MediaControlUtils.Onstart(activity);
                songPlayButton.setBackgroundResource(R.drawable.ic_pause);
                isChanging = false;
            }
        });

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                try {

                    boolean status = MediaService.resiceStatus();
                    if (musicBound != status && !isChanging) {
                        if (status) {
                            songPlayButton.setBackgroundResource(R.drawable.ic_pause);
                        } else {
                            songPlayButton.setBackgroundResource(R.drawable.ic_play);
                        }
                    }
                    musicBound = status;

                    long tempSong = MediaService.getCurrentSong();
                    int size = MediaService.getSongsList().size();
                    if (tempSong != currSong) {
                        currSong = tempSong;
                        updateDisplayData();
                    } else if (size != currSize) {
                        currSize = size;
                        queueAdapter = new QueueAdapter(activity, songList);
                        listView.setAdapter(queueAdapter);
                    }

                    int songPosition = MediaService.getSongPosition();
                    int songDuration = MediaService.getSongDuration();
                    if (!isChanging && songPosition != Constants.MEDIA_ERROR && songDuration != Constants.MEDIA_ERROR) {
                        progressBar.setMax(songDuration);
                        progressBar.setProgress(songPosition);

                        String songPositionTime = String.format(
                                Locale.US, "%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(songPosition),
                                TimeUnit.MILLISECONDS.toSeconds(songPosition) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songPosition))
                        );

                        String songDurationTime = String.format(
                                Locale.US, "%02d:%02d",
                                TimeUnit.MILLISECONDS.toMinutes(songDuration),
                                TimeUnit.MILLISECONDS.toSeconds(songDuration) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(songDuration))
                        );

                        String totalSongTime = songPositionTime + "/" + songDurationTime;
                        TextView songTimeView = (TextView) findViewById(R.id.songbar_time);
                        songTimeView.setText(totalSongTime);
                    }

                } catch (Exception ignored) { ignored.printStackTrace(); }

                handler.postDelayed(this, Constants.HANDLER_DELAY);

            }
        }, Constants.HANDLER_DELAY);
    }

    private void startMainView() {

        QueueActivity.this.setTitle("Playing Queue");

        listView = (ListView) findViewById(R.id.songlist);
        if (songList != null) {

            queueAdapter = new QueueAdapter(activity, songList);
            listView.setAdapter(queueAdapter);

            selectionAdapter = new QueueSelectionAdapter(activity, R.layout.fragment_song, songList);
            listView.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
            listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
                @Override
                public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                    int numChecked = listView.getCheckedItemCount();
                    mode.setTitle(numChecked + " Selected");
                    selectionAdapter.changeSelection(position);
                }

                @Override
                public boolean onCreateActionMode(ActionMode mode, Menu menu) {

                    MenuInflater inflater = mode.getMenuInflater();
                    inflater.inflate(R.menu.selection_menu_queue, menu);
                    return true;
                }

                @Override
                public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

                    int index = listView.getFirstVisiblePosition();
                    View v = listView.getChildAt(0);
                    int top = (v == null) ? 0 : (v.getTop() - listView.getPaddingTop());
                    listView.setAdapter(selectionAdapter);
                    listView.setSelectionFromTop(index, top);
                    return true;
                }

                @Override
                public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.remove_from_queue: {
                            List<Integer> temp = selectionAdapter.getChosenSongs();
                            List<Song> selected = new ArrayList<>();
                            for (Integer idx: temp) {
                                selected.add(songList.get(idx));
                            }
                            MediaControlUtils.OnremoveFromQueue(activity, selected);
                            mode.finish();
                            return true;
                        }
                        case R.id.add_to_playlist: {
                            List<Integer> temp = selectionAdapter.getChosenSongs();
                            List<Song> selected = new ArrayList<>();
                            for (Integer idx: temp) {
                                selected.add(songList.get(idx));
                            }
                            Bundle bundle = new Bundle();
                            bundle.putParcelableArrayList(Constants.SONG_LIST, (ArrayList<Song>) selected);
                            DialogFragment dialogFragment = new PlaylistAddSongsDialog();
                            dialogFragment.setArguments(bundle);
                            dialogFragment.show(activity.getFragmentManager(), "AddToPlaylist");
                            mode.finish();
                            return true;
                        }
                        case R.id.create_playlist: {
                            List<Integer> selected = selectionAdapter.getChosenSongs();
                            List<Song> tempList = new ArrayList<>();
                            for (Integer integer: selected) {
                                tempList.add(songList.get(integer));
                            }
                            Bundle bundle = new Bundle();
                            bundle.putParcelableArrayList(Constants.SONG_LIST, (ArrayList<Song>) tempList);
                            DialogFragment dialogFragment = new PlaylistFromSongsDialog();
                            dialogFragment.setArguments(bundle);
                            dialogFragment.show(activity.getFragmentManager(), "PlaylistFromSongs");
                            mode.finish();
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public void onDestroyActionMode(ActionMode mode) {
                    selectionAdapter.restartSelection();
                    int index = listView.getFirstVisiblePosition();
                    View v = listView.getChildAt(0);
                    int top = (v == null) ? 0 : (v.getTop() - listView.getPaddingTop());
                    listView.setAdapter(new QueueAdapter(activity, songList));
                    listView.setSelectionFromTop(index, top);
                    ((QueueActivity) activity).getSupportActionBar().show();
                }
            });

            if (songList.size()%2 == 0) {
                listView.setBackgroundResource(R.color.gray_darker);
            } else {
                listView.setBackgroundResource(R.color.gray);
            }
        }
    }

    private void getDisplayData() {
        songList = MediaService.getSongsList();
        if (songList != null)
            currSize = songList.size();
        else
            currSize = 0;
    }

    private void updateDisplayData() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (listView != null && listView.getAdapter() != null)
                    ((QueueAdapter) listView.getAdapter()).getContent();
            }
        });
    }

    @Override
    protected void selectItem(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.library: {
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            case R.id.queue: {
                Intent intent = new Intent(this, QueueActivity.class);
                startActivity(intent);
                finish();
                break;
            }
            case R.id.now_playing: {
                Intent intent = new Intent(this, MusicPlayerActivity.class);
                intent.putExtra(Constants.MUSIC_ID, currSong);
                startActivity(intent);
                finish();
                break;
            }
            case R.id.equalizer: {
                NavUtils.goToEqualizer(activity);
                finish();
                break;
            }
            case R.id.info: {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("vector music");
                builder.setMessage("\nmusic player\n");
                builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow_menu_queue, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.play_all: {
                List<Song> temp = new ArrayList<>(songList);
                MediaControlUtils.OnstartQueueRepeatingShuffled(activity, temp);
                return true;
            }
            case R.id.a_to_z: {
                Collections.sort(songList, new AlphabeticalOrganazier());
                MediaService.setSongsList(songList);
                MediaService.setIDx(MediaControlUtils.findIDx(currSong));
                updateDisplayData();
                return true;
            }
            case R.id.z_to_a: {
                Collections.sort(songList, new ReverseAlphabeticalorganizer());
                MediaService.setSongsList(songList);
                MediaService.setIDx(MediaControlUtils.findIDx(currSong));
                updateDisplayData();
                return true;
            }
            case R.id.album_name: {
                Collections.sort(songList, new AlbumNameOrganizer());
                MediaService.setSongsList(songList);
                MediaService.setIDx(MediaControlUtils.findIDx(currSong));
                updateDisplayData();
                return true;
            }
            case R.id.artist_name: {
                Collections.sort(songList, new ArtistNameOrganizer());
                MediaService.setSongsList(songList);
                MediaService.setIDx(MediaControlUtils.findIDx(currSong));
                updateDisplayData();
                return true;
            }
            case R.id.duration: {
                Collections.sort(songList, new DurationOrganizer());
                MediaService.setSongsList(songList);
                MediaService.setIDx(MediaControlUtils.findIDx(currSong));
                updateDisplayData();
                return true;
            }
            case R.id.share: {
                ShareUtils.shareTrackList(activity, songList );
                return true;
            }
            case R.id.equalizer: {
                NavUtils.goToEqualizer(activity);
                return true;
            }
            case R.id.go_to_artist: {
                Song temp = SongLoader.findSongs(currSong);
                assert temp != null;
                NavUtils.goToArtist(activity, temp.getArtistId());
                finish();
                return true;
            }
            case R.id.go_to_album: {
                Song temp = SongLoader.findSongs(currSong);
                assert temp != null;
                NavUtils.goToAlbum(activity, temp.getAlbumId());
                finish();
                return true;
            }
        }

        return false;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStart() {
        super.onStart();
        menuDrawer.closeDrawers();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private class AlphabeticalOrganazier implements Comparator<Song> {
        @Override
        public int compare(Song song1, Song song2) {
            String name1 = song1.getName().replaceAll("[^\\p{L}\\p{Nd}]+", "");
            String name2 = song2.getName().replaceAll("[^\\p{L}\\p{Nd}]+", "");
            return name1.compareTo(name2);
        }
    }

    private class ReverseAlphabeticalorganizer implements Comparator<Song> {
        @Override
        public int compare(Song song1, Song song2) {
            String name1 = song1.getName().replaceAll("[^\\p{L}\\p{Nd}]+", "");
            String name2 = song2.getName().replaceAll("[^\\p{L}\\p{Nd}]+", "");
            return name2.compareTo(name1);
        }
    }

    private class AlbumNameOrganizer implements Comparator<Song> {
        @Override
        public int compare(Song song1, Song song2) {
            String name1 = song1.getAlbum().replaceAll("[^\\p{L}\\p{Nd}]+", "");
            String name2 = song2.getAlbum().replaceAll("[^\\p{L}\\p{Nd}]+", "");
            return name1.compareTo(name2);
        }
    }

    private class ArtistNameOrganizer implements Comparator<Song> {
        @Override
        public int compare(Song song1, Song song2) {
            String name1 = song1.getArtist().replaceAll("[^\\p{L}\\p{Nd}]+", "");
            String name2 = song2.getArtist().replaceAll("[^\\p{L}\\p{Nd}]+", "");
            return name1.compareTo(name2);
        }
    }

    private class DurationOrganizer implements Comparator<Song> {
        @Override
        public int compare(Song song1, Song song2) {
            return song1.getDuration() - song2.getDuration();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
