package com.vova.musik.fragments;


import android.app.Activity;
import android.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.vova.musik.R;
import com.vova.musik.activities.MainActivity;
import com.vova.musik.adapters.SongAdapter;
import com.vova.musik.adapters.SongSelectionAdapter;
import com.vova.musik.dialogs.PlaylistAddSongsDialog;
import com.vova.musik.dialogs.PlaylistFromSongsDialog;
import com.vova.musik.models.Song;
import com.vova.musik.utils.Constants;
import com.vova.musik.utils.MediaControlUtils;
import com.vova.musik.utils.MediaDataUtils;
import com.vova.musik.utils.NavUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class SongFragment extends Fragment {

    private static final String TAG = SongFragment.class.getName();

    private List<Song> songList;
    private View libraryView;
    private ListView listView;
    private SongSelectionAdapter selectionAdapter;
    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        activity = getActivity();
        songList = ((MainActivity) activity).receiveSongsList();
        libraryView = inflater.inflate(R.layout.fragment_song, container, false);

        listView = (ListView) libraryView.findViewById(R.id.songlist);
        listView.setAdapter(new SongAdapter(activity, songList));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaControlUtils.startRepeating(activity, songList, position);
                ((MainActivity) activity).makeMusicBounds(true);
                NavUtils.goToSong(activity, songList.get(position).getId());
            }
        });

        selectionAdapter = new SongSelectionAdapter(activity, R.layout.item_song, songList);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new ListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                // Change the title to num of clicked items
                int numChecked = listView.getCheckedItemCount();
                mode.setTitle(numChecked + " Selected");
                selectionAdapter.changeSelection(position);
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {

                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.selection_menu_song, menu);
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
                    case R.id.delete: {

                        List<Integer> selected = selectionAdapter.getChosenSongs();
                        List<Song> tempList = new ArrayList<>();
                        for (Integer integer: selected) {
                            tempList.add(songList.get(integer));
                        }
                        MediaDataUtils.removeSongList(tempList, activity);
                        ((MainActivity) activity).getAll();
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
                    case R.id.add_to_playlist: {

                        List<Integer> selected = selectionAdapter.getChosenSongs();
                        List<Song> tempList = new ArrayList<>();
                        for (Integer integer: selected) {
                            tempList.add(songList.get(integer));
                        }
                        Bundle bundle = new Bundle();
                        bundle.putParcelableArrayList(Constants.SONG_LIST, (ArrayList<Song>) tempList);
                        DialogFragment dialogFragment = new PlaylistAddSongsDialog();
                        dialogFragment.setArguments(bundle);
                        dialogFragment.show(activity.getFragmentManager(), "AddToPlaylist");
                        mode.finish();
                        return true;
                    }
                    case R.id.add_to_queue: {
                        List<Integer> selected = selectionAdapter.getChosenSongs();
                        List<Song> tempList = new ArrayList<>();
                        for (Integer integer: selected) {
                            tempList.add(songList.get(integer));
                        }
                        MediaControlUtils.OnaddToQueue(activity, tempList);
                        mode.finish();
                        return true;
                    }
                    case R.id.play_all: {

                        List<Integer> selected = selectionAdapter.getChosenSongs();
                        List<Song> tempList = new ArrayList<>();
                        for (Integer integer: selected) {
                            tempList.add(songList.get(integer));
                        }
                        MediaControlUtils.OnstartQueueShuffled(activity, tempList);
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
                listView.setAdapter(new SongAdapter(activity, songList));
                listView.setSelectionFromTop(index, top);
                ((MainActivity) activity).getSupportActionBar().show();
            }
        });


        if (songList.size()%2 == 0) {
            libraryView.findViewById(R.id.fragment).setBackgroundResource(R.color.gray_darker);
        } else {
            libraryView.findViewById(R.id.fragment).setBackgroundResource(R.color.gray);
        }


        setHasOptionsMenu(true);

        return libraryView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.overflow_menu_song, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search: {
                NavUtils.goToSearch(activity);
                return true;
            }
            case R.id.play_all: {
                List<Song> temp = new ArrayList<>(songList);
                MediaControlUtils.OnstartQueueRepeatingShuffled(activity, temp);
                return true;
            }
            case R.id.a_to_z: {
                Collections.sort(((MainActivity) activity).receiveSongsList(), new AlphabeticalOrganizer());
                ((MainActivity) activity).getContent();
                return true;
            }
            case R.id.z_to_a: {
                Collections.sort(((MainActivity) activity).receiveSongsList(), new ReverseAlphabeticalOrganizer());
                ((MainActivity) activity).getContent();
                return true;
            }
            case R.id.album_name: {
                Collections.sort(((MainActivity) activity).receiveSongsList(), new AlbumNameOrganizer());
                ((MainActivity) activity).getContent();
                return true;
            }
            case R.id.artist_name: {
                Collections.sort(((MainActivity) activity).receiveSongsList(), new ArtistNameOrganizer());
                ((MainActivity) activity).getContent();
                return true;
            }
            case R.id.duration: {
                Collections.sort(((MainActivity) activity).receiveSongsList(), new DurationOrganizer());
                ((MainActivity) activity).getContent();
                return true;
            }


            case R.id.equalizer: {
                NavUtils.goToEqualizer(activity);
                return true;
            }
        }

        return false;
    }

    private class AlphabeticalOrganizer implements Comparator<Song> {
        @Override
        public int compare(Song song1, Song song2) {
            String name1 = song1.getName().replaceAll("[^\\p{L}\\p{Nd}]+", "");
            String name2 = song2.getName().replaceAll("[^\\p{L}\\p{Nd}]+", "");
            return name1.compareTo(name2);
        }
    }

    private class ReverseAlphabeticalOrganizer implements Comparator<Song> {
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
}
