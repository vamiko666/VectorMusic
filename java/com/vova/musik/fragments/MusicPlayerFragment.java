/*
Echo Music Player
Copyright (C) 2019 David Zhang

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.vova.musik.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.vova.musik.R;
import com.vova.musik.utils.Constants;
import com.vova.musik.utils.MediaDataUtils;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;


public class MusicPlayerFragment extends Fragment {

    private static final String TAG = MusicPlayerFragment.class.getName();

    private View view;
    private ImageView coverView;
    private TextView songName;
    private TextView songArtist;
    private TextView songAlbum;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_music_player, container, false);
        coverView = (ImageView) view.findViewById(R.id.song_cover);
        songName = (TextView) view.findViewById(R.id.song_name);
        songArtist = (TextView) view.findViewById(R.id.song_artist);
        songAlbum = (TextView) view.findViewById(R.id.album_name);

        Bundle bundle = getArguments();
        long albumId = bundle.getLong(Constants.ALBUM_ID);
        String name = bundle.getString(Constants.SONG_NAME);
        String artist = bundle.getString(Constants.SONG_ARTIST);
        String album = bundle.getString(Constants.SONG_ALBUM);

        Picasso.with(getActivity())
                .load(MediaDataUtils.reciveAlbumArt(albumId))
                .resize(512, 512)
                .onlyScaleDown()
                .centerInside()
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .error(R.drawable.ic_album)
                .into(coverView);
        songName.setText(name);
        songArtist.setText(artist);
        songAlbum.setText(album);

        return view;
    }

    @Override
    public void onDestroy() {
        PicassoTools.clearCache(Picasso.with(getActivity()));
        super.onDestroy();
    }
}
