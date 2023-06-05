package com.vova.musik.fragments;


import android.app.Activity;
import android.os.Bundle;
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
import com.vova.musik.adapters.ArtistAdapter;
import com.vova.musik.models.Artist;
import com.vova.musik.utils.MediaControlUtils;
import com.vova.musik.utils.NavUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;


public class ArtistFragment extends Fragment {

    private List<Artist> artistList;
    private View artistView;
    private ListView listView;
    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {


        activity = getActivity();
        artistList = ((MainActivity) activity).receiveArtistsList();
        artistView = inflater.inflate(R.layout.fragment_artist, container, false);

        listView = (ListView) artistView.findViewById(R.id.artist_list);
        listView.setAdapter(new ArtistAdapter(activity, artistList));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NavUtils.goToArtist(activity, artistList.get(position).getId());
            }
        });

   
        if (artistList.size()%2 == 0) {
            artistView.findViewById(R.id.fragment).setBackgroundResource(R.color.gray_darker);
        } else {
            artistView.findViewById(R.id.fragment).setBackgroundResource(R.color.gray);
        }


        setHasOptionsMenu(true);

        return artistView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.overflow_menu_artist, menu);
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
                Collections.sort(((MainActivity) activity).receiveArtistsList(), new AlphabeticalOrganizer());
                ((MainActivity) activity).getContent();
                return true;
            }
            case R.id.z_to_a: {
                Collections.sort(((MainActivity) activity).receiveArtistsList(), new ReverseAlphabeticalOrganizer());
                ((MainActivity) activity).getContent();
                return true;
            }
            case R.id.count: {
                Collections.sort(((MainActivity) activity).receiveArtistsList(), new CountOrganizer());
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

    private class AlphabeticalOrganizer implements Comparator<Artist> {

        @Override
        public int compare(Artist artist1, Artist artist2) {
            return artist1.getName().compareTo(artist2.getName());
        }
    }

    private class ReverseAlphabeticalOrganizer implements Comparator<Artist> {
        @Override
        public int compare(Artist artist1, Artist artist2) {
            return artist2.getName().compareTo(artist1.getName());
        }
    }

    private class CountOrganizer implements Comparator<Artist> {
        @Override
        public int compare(Artist artist1, Artist artist2) {
            return artist1.getAlbumCount() - artist2.getAlbumCount();
        }
    }
}
