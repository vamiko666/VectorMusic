package com.vova.musik.fragments;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
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
import com.vova.musik.adapters.PlaylistAdapter;
import com.vova.musik.dialogs.PlaylistCreateDialog;
import com.vova.musik.models.Playlist;
import com.vova.musik.utils.MediaControlUtils;
import com.vova.musik.utils.NavUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class PlaylistFragment extends Fragment {

    private static final String TAG = PlaylistFragment.class.getName();

    private List<Playlist> playlistList;
    private View playlistView;
    private ListView listView;
    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        activity = getActivity();
        playlistList = ((MainActivity) activity).receivePlaylistsList();
        playlistView = inflater.inflate(R.layout.fragment_playlist, container, false);

        listView = (ListView) playlistView.findViewById(R.id.playlist);
        listView.setAdapter(new PlaylistAdapter(activity, playlistList));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NavUtils.goToPlaylist(activity, playlistList.get(position).getId());
            }
        });

        if (playlistList.size()%2 == 0) {
            playlistView.findViewById(R.id.fragment).setBackgroundResource(R.color.gray_darker);
        } else {
            playlistView.findViewById(R.id.fragment).setBackgroundResource(R.color.gray);
        }

        setHasOptionsMenu(true);

        return playlistView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.overflow_menu_playlist, menu);
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
                MediaControlUtils.OnstartQueueRepeatingShuffled(activity, ((MainActivity) activity).receiveSongsList());
                return true;
            }
            case R.id.a_to_z: {
                Collections.sort(((MainActivity) activity).receivePlaylistsList(), new AlphabeticalComparator());
                ((MainActivity) activity).getContent();
                return true;
            }
            case R.id.z_to_a: {
                Collections.sort(((MainActivity) activity).receivePlaylistsList(), new ReverseAlphabeticalComparator());
                ((MainActivity) activity).getContent();
                return true;
            }
            case R.id.count: {
                Collections.sort(((MainActivity) activity).receivePlaylistsList(), new CountComparator());
                ((MainActivity) activity).getContent();
                return true;
            }
            case R.id.create_playlist: {
                DialogFragment dialogFragment = new PlaylistCreateDialog();
                dialogFragment.show(getFragmentManager(), "CreatePlaylist");
                return true;
            }
            case R.id.equalizer: {
                NavUtils.goToEqualizer(activity);
                return true;
            }
        }

        return false;
    }

    private class AlphabeticalComparator implements Comparator<Playlist> {
        @Override
        public int compare(Playlist playlist1, Playlist playlist2) {
            return playlist1.getName().compareTo(playlist2.getName());
        }
    }

    private class ReverseAlphabeticalComparator implements Comparator<Playlist> {
        @Override
        public int compare(Playlist playlist1, Playlist playlist2) {
            return playlist2.getName().compareTo(playlist1.getName());
        }
    }

    private class CountComparator implements Comparator<Playlist> {
        @Override
        public int compare(Playlist playlist1, Playlist playlist2) {
            return playlist1.getCount() - playlist2.getCount();
        }
    }
}
