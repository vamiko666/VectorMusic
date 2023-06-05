
package com.vova.musik.fragments;


import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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
import com.vova.musik.adapters.AlbumAdapter;
import com.vova.musik.models.Album;
import com.vova.musik.utils.MediaControlUtils;
import com.vova.musik.utils.NavUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class AlbumFragment extends Fragment {

    private static final String TAG = AlbumFragment.class.getName();

    private List<Album> albumList;
    private View albumView;
    private ListView listView;
    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        activity = getActivity();
        albumList = ((MainActivity) activity).receiveAlbumsList();
        albumView = inflater.inflate(R.layout.fragment_album, container, false);

        listView = (ListView) albumView.findViewById(R.id.album_list);
        listView.setAdapter(new AlbumAdapter(activity, this, albumList));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NavUtils.goToAlbum(activity, albumList.get(position).getId());
            }
        });

        if (albumList.size()%2 == 0) {
            albumView.findViewById(R.id.fragment).setBackgroundResource(R.color.gray_darker);
        } else {
            albumView.findViewById(R.id.fragment).setBackgroundResource(R.color.gray);
        }

        setHasOptionsMenu(true);

        return albumView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.overflow_menu_album, menu);
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
                Collections.sort(((MainActivity) activity).receiveAlbumsList(), new AlphabeticalOrganizer());
                ((MainActivity) activity).getContent();
                return true;
            }
            case R.id.z_to_a: {
                Collections.sort(((MainActivity) activity).receiveAlbumsList(), new ReverseAlphabeticalOrganizer());
                ((MainActivity) activity).getContent();
                return true;
            }
            case R.id.artist: {
                Collections.sort(((MainActivity) activity).receiveAlbumsList(), new ArtistOrganizer());
                ((MainActivity) activity).getContent();
            }
            case R.id.count: {
                Collections.sort(((MainActivity) activity).receiveAlbumsList(), new CountOrganizer());
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

    private class AlphabeticalOrganizer implements Comparator<Album> {
        @Override
        public int compare(Album album1, Album album2) {
            return album1.getName().compareTo(album2.getName());
        }
    }

    private class ReverseAlphabeticalOrganizer implements Comparator<Album> {
        @Override
        public int compare(Album album1, Album album2) {
            return album2.getName().compareTo(album1.getName());
        }
    }

    private class ArtistOrganizer implements Comparator<Album> {
        @Override
        public int compare(Album album1, Album album2) {
            return album1.getArtist().compareTo(album2.getArtist());
        }
    }

    private class CountOrganizer implements Comparator<Album> {
        @Override
        public int compare(Album album1, Album album2) {
            return album1.getCount() - album2.getCount();
        }
    }
}
