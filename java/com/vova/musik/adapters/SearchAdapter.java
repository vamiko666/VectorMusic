
package com.vova.musik.adapters;


import android.app.Activity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.vova.musik.R;
import com.vova.musik.dataloaders.AlbumLoader;
import com.vova.musik.dataloaders.ArtistLoader;
import com.vova.musik.dataloaders.PlaylistLoader;
import com.vova.musik.dataloaders.SongLoader;
import com.vova.musik.models.Album;
import com.vova.musik.models.Artist;
import com.vova.musik.models.Playlist;
import com.vova.musik.models.Song;
import com.vova.musik.utils.Constants;
import com.vova.musik.utils.NavUtils;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = SearchAdapter.class.getName();

    private List<Object> dataList;
    private Activity activity;

    public SearchAdapter(Activity activity, List<Object> dataList) {
        this.dataList = dataList;
        this.activity = activity;
    }

    @Override
    public int getItemViewType(int position) {
        if (dataList.get(position) instanceof String)
            return Constants.SEARCH_TITLE;
        else if (dataList.get(position) instanceof Song)
            return Constants.SEARCH_SONG;
        else if (dataList.get(position) instanceof Playlist)
            return Constants.SEARCH_PLAYLIST;
        else if (dataList.get(position) instanceof Album)
            return Constants.SEARCH_ALBUM;
        else if (dataList.get(position) instanceof Artist)
            return Constants.SEARCH_ARTIST;

        Log.e(TAG, "Item type not found.");
        return -1;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int type) {
        switch (type) {
            case Constants.SEARCH_TITLE:
                return new TitleHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_search_title, viewGroup, false));
            case Constants.SEARCH_SONG:
                return new Songs(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_search_song, viewGroup, false),
                        dataList, activity);
            case Constants.SEARCH_PLAYLIST:
                return new PlayLists(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_search_playlist, viewGroup, false),
                        dataList, activity);
            case Constants.SEARCH_ALBUM:
                return new Albums(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_search_album, viewGroup, false),
                        dataList, activity);
            case Constants.SEARCH_ARTIST:
                return new Artists(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_search_artist, viewGroup, false),
                        dataList, activity);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int pos) {
        switch (holder.getItemViewType()) {
            case Constants.SEARCH_TITLE:
                TitleHolder titleHolder = (TitleHolder) holder;
                titleHolder.title.setText((String) dataList.get(pos));
                break;
            case Constants.SEARCH_SONG:
                Song song = (Song) dataList.get(pos);
                String songDuration = String.format(
                        Locale.US, "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(song.getDuration()),
                        TimeUnit.MILLISECONDS.toSeconds(song.getDuration()) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(song.getDuration()))
                );
                Songs songHolder = (Songs) holder;
                songHolder.songName.setText(song.getName());
                songHolder.songArtist.setText(song.getArtist());
                songHolder.songDuration.setText(songDuration);
                break;
            case Constants.SEARCH_PLAYLIST:
                Playlist playlist = (Playlist) dataList.get(pos);
                String playlistText = playlist.getCount() + " Tracks";
                if (playlist.getCount() == 1) {
                    playlistText = playlist.getCount() + " Track";
                }
                PlayLists playLists = (PlayLists) holder;
                playLists.playlistName.setText(playlist.getName());
                playLists.playlistCount.setText(playlistText);
                break;
            case Constants.SEARCH_ALBUM:
                Album album = (Album) dataList.get(pos);
                String albumText = album.getCount() + " Tracks";
                if (album.getCount() == 1) {
                    albumText = album.getCount() + " Track";
                }
                Albums albums = (Albums) holder;
                albums.albumName.setText(album.getName());
                albums.albumArtist.setText(album.getArtist());
                albums.albumCount.setText(albumText);
                break;
            case Constants.SEARCH_ARTIST:
                Artist artist = (Artist) dataList.get(pos);
                String artistText = artist.getAlbumCount() + " Albums";
                String artistTrackText = artist.getTrackCount() + " Tracks";
                if (artist.getAlbumCount() == 1) {
                    artistText = artist.getAlbumCount() + " Album";
                }
                if (artist.getTrackCount() == 1) {
                    artistTrackText = artist.getTrackCount() + " Track";
                }
                Artists artists = (Artists) holder;
                artists.artistName.setText(artist.getName());
                artists.artistCount.setText(artistText);
                artists.artistTrackCount.setText(artistTrackText);
                break;
        }
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }

    private static class TitleHolder extends RecyclerView.ViewHolder {
        private TextView title;
        private TitleHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
        }
    }

    private static class Songs extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView songName;
        private TextView songArtist;
        private TextView songDuration;
        private List<Object> objectList;
        private Activity activity;

        private Songs(View view, List<Object> objects, Activity act) {
            super(view);
            view.setOnClickListener(this);
            songName = (TextView) view.findViewById(R.id.song_name);
            songArtist = (TextView) view.findViewById(R.id.song_artist);
            songDuration = (TextView) view.findViewById(R.id.song_duration);
            objectList = objects;
            activity = act;
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            NavUtils.goToObject(activity, objectList.get(position));
        }
    }

    private static class PlayLists extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView playlistName;
        private TextView playlistCount;
        private List<Object> objectList;
        private Activity activity;

        private PlayLists(View view, List<Object> objects, Activity act) {
            super(view);
            playlistName = (TextView) view.findViewById(R.id.playlist_name);
            playlistCount = (TextView) view.findViewById(R.id.playlist_count);
            objectList = objects;
            activity = act;
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            NavUtils.goToObject(activity, objectList.get(position));
        }
    }

    private static class Albums extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView albumName;
        private TextView albumArtist;
        private TextView albumCount;
        private List<Object> objectList;
        private Activity activity;

        private Albums(View view, List<Object> objects, Activity act) {
            super(view);
            albumName = (TextView) view.findViewById(R.id.album_name);
            albumArtist = (TextView) view.findViewById(R.id.album_artist);
            albumCount = (TextView) view.findViewById(R.id.album_count);
            objectList = objects;
            activity = act;
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            NavUtils.goToObject(activity, objectList.get(position));
        }
    }

    private static class Artists extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView artistName;
        private TextView artistCount;
        private TextView artistTrackCount;
        private List<Object> objectList;
        private Activity activity;

        private Artists(View view, List<Object> objects, Activity act) {
            super(view);
            artistName = (TextView) view.findViewById(R.id.artist_name);
            artistCount = (TextView) view.findViewById(R.id.artist_count);
            artistTrackCount = (TextView) view.findViewById(R.id.artist_track_count);
            objectList = objects;
            activity = act;
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            NavUtils.goToObject(activity, objectList.get(position));
        }
    }

    public void Mediasearcher(String query) {

        dataList.clear();
        Log.d(TAG, "Searching media...");

        boolean songExists = false;
        for (Song song: SongLoader.songList) {
            if (song.getName().toLowerCase().contains(query) ||
                    song.getArtist().toLowerCase().contains(query)) {
                if (!songExists) {
                    dataList.add("Tracks");
                    songExists = true;
                }
                dataList.add(song);
            }
        }

        boolean playlistExists = false;
        for (Playlist playlist: PlaylistLoader.playlistList) {
            if (playlist.getName().toLowerCase().contains(query) && playlist.getId() > 0) {
                if (!playlistExists) {
                    dataList.add("Playlists");
                    playlistExists = true;
                }
                dataList.add(playlist);
            }
        }

        boolean albumExists = false;
        for (Album album: AlbumLoader.albumList) {
            if (album.getName().toLowerCase().contains(query)) {
                if (!albumExists) {
                    dataList.add("Albums");
                    albumExists = true;
                }
                dataList.add(album);
            }
        }

        boolean artistExists = false;
        for (Artist artist: ArtistLoader.artistList) {
            if (artist.getName().toLowerCase().contains(query)) {
                if (!artistExists) {
                    dataList.add("Artists");
                    artistExists = true;
                }
                dataList.add(artist);
            }
        }

        notifyDataSetChanged();
    }
}
