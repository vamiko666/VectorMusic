package com.vova.musik.activities;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.SeekBar;
import android.widget.TextView;

import com.vova.musik.R;
import com.vova.musik.services.MediaService;
import com.vova.musik.utils.NavUtils;
import com.vova.musik.widgets.FontTabLayout;
import com.vova.musik.adapters.SlidePagerAdapter;
import com.vova.musik.dataloaders.AlbumLoader;
import com.vova.musik.dataloaders.ArtistLoader;
import com.vova.musik.models.Album;
import com.vova.musik.models.Artist;
import com.vova.musik.models.Playlist;
import com.vova.musik.models.Song;
import com.vova.musik.dataloaders.PlaylistLoader;
import com.vova.musik.dataloaders.SongLoader;
import com.vova.musik.utils.Constants;
import com.vova.musik.utils.MediaControlUtils;
import com.vova.musik.utils.MediaDataUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getName();
    private final Activity activity = this;

    private SeekBar progressBar;
    private ImageSwitcher songPlayButton;
    private ViewPager viewPager;
    private Thread getSongs;
    private Thread getPlaylists;
    private Thread getAlbums;
    private Thread getArtists;
    private List<Song> songList;
    private List<Playlist> playlistList;
    private List<Album> albumList;
    private List<Artist> artistList;
    private Handler handler;

    private boolean isChanging;
    private boolean musicBound;
    private long albumId;

    public MainActivity() {
        super(R.layout.activity_main);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getPermission();

        getData();

        startPlayer();

        waitData();
        startMainView();
    }

    @Override
    protected void onStart() {
        super.onStart();

        menuDrawer.closeDrawers();

        startSongbar();
    }

    private void getPermission() {
        if (Build.VERSION.SDK_INT < 23) {
            Log.d(TAG, "Don't need permissions.");
        } else {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Log.d(TAG, "PERMISSIONS: App needs permissions to read external storage.");
                }
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            }
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Log.d(TAG, "PERMISSIONS: App needs permissions to read external storage.");
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    private void getData() {

        // Multi-thread for performance
        getSongs = new Thread(new Runnable() {
            @Override
            public void run() {
                songList = SongLoader.getSongList(activity);
            }
        });
        getPlaylists = new Thread(new Runnable() {
            @Override
            public void run() {
                playlistList = PlaylistLoader.getPlaylistsList(activity);
            }
        });
        getAlbums = new Thread(new Runnable() {
            @Override
            public void run() {
                albumList = AlbumLoader.getAlbumsList(activity);
            }
        });
        getArtists = new Thread(new Runnable() {
            @Override
            public void run() {
                artistList = ArtistLoader.getArtistsList(activity);
            }
        });
        getSongs.start();
        getPlaylists.start();
        getAlbums.start();
        getArtists.start();
    }

    private void startPlayer() {
        MediaControlUtils.startController(this);
    }

    @SuppressLint("NewApi")
    private void startSongbar() {


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

        // Get receiver
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                try {

                    // Get music status
                    boolean status = MediaService.resiceStatus();
                    if (musicBound != status && !isChanging) {
                        if (status) {
                            songPlayButton.setBackgroundResource(R.drawable.ic_pause);
                        } else {
                            songPlayButton.setBackgroundResource(R.drawable.ic_play);
                        }
                    }
                    musicBound = status;

                    // Update if different song
                    long tempSong = MediaService.getCurrentSong();
                    if (tempSong != currSong) {
                        currSong = tempSong;
                    }

                    // Set location based on position/duration
                    int songPosition = MediaService.getSongPosition();
                    int songDuration = MediaService.getSongDuration();
                    if (!isChanging && songPosition != Constants.MEDIA_ERROR && songDuration != Constants.MEDIA_ERROR) {
                        progressBar.setMax(songDuration);
                        progressBar.setProgress(songPosition);

                        // Set new text in time
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

                } catch (Exception ignored) {}

                handler.postDelayed(this, Constants.HANDLER_DELAY);
            }
        }, Constants.HANDLER_DELAY);
    }

    private void waitData() {

        try {
            getSongs.join();
            getPlaylists.join();
            getAlbums.join();
            getArtists.join();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startMainView() {


        viewPager = (ViewPager) findViewById(R.id.headers);
        PagerAdapter pagerAdapter = new SlidePagerAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(pagerAdapter);

        FontTabLayout tabLayout = (FontTabLayout) findViewById(R.id.slider);
        tabLayout.setupWithViewPager(viewPager);

        MainActivity.this.setTitle("Vector Music");
    }

    public void getContent() {
        Log.d(TAG, "Getting content.");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewPager.getAdapter().notifyDataSetChanged();
            }
        });
    }

    public void getAll() {
        Log.d(TAG, "Receiving all.");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getData();
                waitData();

                viewPager.getAdapter().notifyDataSetChanged();
            }
        });
    }

    public void getNew(final String path, final String url) {
        Log.d(TAG, "getting new.");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {


                Log.e(TAG, "getting on UI thread.");
                getData();
                waitData();



                getData();
                waitData();
                viewPager.getAdapter().notifyDataSetChanged();
            }
        });
    }

    public void getPhoto(long id) {
        albumId = id;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.RESULT_CHOOSE_ALBUM_COVER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.RESULT_UPDATE_ACTIVITY && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                boolean change = data.getBooleanExtra(Constants.RESULT_DEFAULT, false);
                if (change) {
                    getAll();
                }
            }
        }
        if (requestCode == Constants.RESULT_CHOOSE_ALBUM_COVER && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                String[] file = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(uri, file, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    String path = cursor.getString(cursor.getColumnIndex(file[0]));
                    cursor.close();
                    MediaDataUtils.changeAlbumArt(path, albumId, getApplicationContext());
                    getAll();
                }
            } else {
                Log.e(TAG, "Error, no data.");
            }
        }
    }

    @Override
    protected void selectItem(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.library: {
                break;
            }
            case R.id.queue: {
                Intent intent = new Intent(this, QueueActivity.class);
                startActivity(intent);
                break;
            }
            case R.id.now_playing: {
                Intent intent = new Intent(this, MusicPlayerActivity.class);
                intent.putExtra(Constants.MUSIC_ID, currSong);
                startActivity(intent);
                break;
            }
            case R.id.equalizer: {
                NavUtils.goToEqualizer(activity);
                break;
            }
            case R.id.info: {
                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setTitle("Vector music");
                builder.setMessage("\nvector Music Player\n");
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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public List<Song> receiveSongsList() {
        return songList;
    }

    public List<Playlist> receivePlaylistsList() {
        return playlistList;
    }

    public List<Album> receiveAlbumsList() {
        return albumList;
    }

    public List<Artist> receiveArtistsList() {
        return artistList;
    }

    public void makeMusicBounds(boolean bound) {
        this.musicBound = bound;
    }

    @Override
    public void onDestroy() {
        PicassoTools.clearCache(Picasso.with(this));
        super.onDestroy();
    }
}
