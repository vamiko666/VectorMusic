package com.vova.musik.activities;


import android.app.Activity;
import android.view.MenuItem;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.vova.musik.R;
import com.vova.musik.adapters.SongPlainAdapter;
import com.vova.musik.dataloaders.ArtistLoader;
import com.vova.musik.models.Artist;
import com.vova.musik.models.Song;
import com.vova.musik.utils.Constants;
import com.vova.musik.utils.MediaDataUtils;
import com.vova.musik.utils.NavUtils;
import com.vova.musik.utils.ShareUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;


public class ArtistPlayerActivity extends ListPlayerActivity {

    private static final String TAG = ArtistPlayerActivity.class.getName();
    private final Activity activity = this;

    private Artist artist;

    @Override
    protected void getDisplayData() {

        long id = getIntent().getLongExtra(Constants.ARTIST_ID, 0);
        artist = ArtistLoader.findArtistsById(id);
    }

    @Override
    protected void showDisplayData() {

        if (artist != null) {


            listName.setText(artist.getName());
            songList = MediaDataUtils.getArtist(artist.getId(), activity);


            Song song = songList.get((int)(Math.random()*songList.size()));
            Glide.with(activity)
                    .load(MediaDataUtils.reciveAlbumArt(song.getAlbumId()))
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .into(containerBackground);


            songListView.setAdapter(new SongPlainAdapter(activity, songList));
            if (songList.size()%2 == 0) {
                containerBackground.setBackgroundResource(R.color.gray_darker);
            } else {
                containerBackground.setBackgroundResource(R.color.gray);
            }
        }
    }

    @Override
    protected void UpdateDisplayData() {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
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
