package com.vova.musik.activities;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DialogFragment;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import com.vova.musik.R;
import com.vova.musik.dataloaders.SongLoader;
import com.vova.musik.dialogs.PlaylistAddSongsDialog;
import com.vova.musik.fragments.MusicPlayerFragment;
import com.vova.musik.models.Song;
import com.vova.musik.services.MediaService;
import com.vova.musik.utils.Constants;
import com.vova.musik.utils.ImageUtils;
import com.vova.musik.utils.MediaControlUtils;
import com.vova.musik.utils.MediaDataUtils;
import com.vova.musik.utils.NavUtils;
import com.vova.musik.utils.ShareUtils;
import com.ohoussein.playpause.PlayPauseView;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class MusicPlayerActivity extends BasePlayerActivity {

    private static final String TAG = MusicPlayerActivity.class.getName();
    private final Activity activity = this;

    private PlayPauseView ctrlButton;
    private SeekBar progressBar;
    private GestureDetector gestureDetector;
    private ImageView shuffleButton;
    private ImageView prevButton;
    private ImageView nextButton;
    private ImageView repeatButton;
    private Handler handler;

    private boolean musicBound;
    private boolean isChanging;
    private boolean isShuffled;
    private boolean isRepeating;
    private boolean isPlay;
    private long albumId;
    private int prevIdx;

    public MusicPlayerActivity() {
        super(R.layout.activity_music_player);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MusicPlayerActivity.this.setTitle("");
    }

    @Override
    @SuppressLint("NewApi")
    protected void startMainView() {

        ctrlButton = (PlayPauseView) findViewById(R.id.play_button);
        shuffleButton = (ImageView) findViewById(R.id.shuffle);
        prevButton = (ImageView) findViewById(R.id.prev);
        nextButton = (ImageView) findViewById(R.id.next);
        repeatButton = (ImageView) findViewById(R.id.repeat);
        isShuffled = false;
        isPlay = false;

        shuffleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isShuffled) {
                    shuffleButton.setImageResource(R.drawable.ic_shuffle_red);
                    MediaControlUtils.Onshuffle(activity);
                    isShuffled = true;
                } else {
                    shuffleButton.setImageResource(R.drawable.ic_shuffle);
                    MediaControlUtils.Onshuffle(activity);
                    isShuffled = false;
                }
            }
        });
        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaControlUtils.Onprev(activity);
                if (isPlay) {
                    ctrlButton.toggle();
                    isPlay = false;
                }
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaControlUtils.Onnext(activity);
                if (isPlay) {
                    ctrlButton.toggle();
                    isPlay = false;
                }
            }
        });
        repeatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRepeating) {
                    repeatButton.setImageResource(R.drawable.ic_repeat_red);
                    MediaControlUtils.Onrepeat(activity);
                    isRepeating = true;
                } else {
                    repeatButton.setImageResource(R.drawable.ic_repeat);
                    MediaControlUtils.Onrepeat(activity);
                    isRepeating = false;
                }
            }
        });

        progressBar = (SeekBar) findViewById(R.id.progress_bar);
        progressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        progressBar.getThumb().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        gestureDetector = new GestureDetector(activity, new GestureDetector.OnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public void onShowPress(MotionEvent e) {

            }

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                return false;
            }

            @Override
            public void onLongPress(MotionEvent e) {

            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1.getX() < e2.getX() && Math.abs(e1.getX()-e2.getX()) > 150) {

                    MediaControlUtils.Onprev(activity);
                    return true;
                }

                if (e1.getX() > e2.getX() && Math.abs(e1.getX()-e2.getX()) > 150) {

                    MediaControlUtils.Onnext(activity);
                    return true;
                }

                return false;
            }
        });

    }

    @Override
    protected void getDisplayData() {

        songList = MediaService.getSongsList();
        if (getIntent().getExtras() != null) {
            if (getIntent().getExtras().containsKey(Constants.MUSIC_ID)) {
                currSong = getIntent().getLongExtra(Constants.MUSIC_ID, 0);
            } else {
                currSong = -1;
            }
        } else {
            currSong = -1;
        }
    }

    @Override
    @SuppressLint("NewApi")
    protected void showDisplayData() {

        if (currSong != -1 && songList != null) {
            for (final Song song: songList) {
                if (song.getId() == currSong) {

                    Bundle bundle = new Bundle();
                    bundle.putString(Constants.SONG_NAME, song.getName());
                    bundle.putString(Constants.SONG_ARTIST, song.getArtist());
                    bundle.putString(Constants.SONG_ALBUM, song.getAlbum());
                    bundle.putLong(Constants.ALBUM_ID, song.getAlbumId());

                    Fragment fragment = new MusicPlayerFragment();
                    fragment.setArguments(bundle);
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.main_content, fragment);
                    fragmentTransaction.commit();

                    LinearLayout linearLayout = (LinearLayout) findViewById(R.id.background);
                    try {
                        Bitmap bitmap = ImageUtils.blurBitmap(ImageUtils.getResizedBitmap(MediaStore.Images.Media.getBitmap(
                                getContentResolver(), MediaDataUtils.reciveAlbumArt(song.getAlbumId())), 400, 400), activity);
                        linearLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
                        linearLayout.getBackground().setAlpha(200);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    prevIdx = songList.indexOf(song);
                }
            }
        }
    }

    @Override
    @SuppressLint("NewApi")
    protected void UpdateDisplayData() {

        songList = MediaService.getSongsList();

        if (currSong != -1 && songList != null) {

            final Song song = MediaControlUtils.findSong(currSong);
            if (song != null) {

                Bundle bundle = new Bundle();
                bundle.putString(Constants.SONG_NAME, song.getName());
                bundle.putString(Constants.SONG_ARTIST, song.getArtist());
                bundle.putString(Constants.SONG_ALBUM, song.getAlbum());
                bundle.putLong(Constants.ALBUM_ID, song.getAlbumId());

                Fragment fragment = new MusicPlayerFragment();
                fragment.setArguments(bundle);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

                if (prevIdx < songList.indexOf(song))
                    fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                else
                    fragmentTransaction.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
                fragmentTransaction.replace(R.id.main_content, fragment);
                fragmentTransaction.commit();

                LinearLayout linearLayout = (LinearLayout) findViewById(R.id.background);
                try {
                    Bitmap bitmap = ImageUtils.blurBitmap(ImageUtils.getResizedBitmap(MediaStore.Images.Media.getBitmap(
                            getContentResolver(), MediaDataUtils.reciveAlbumArt(song.getAlbumId())), 400, 400), activity);
                    linearLayout.setBackground(new BitmapDrawable(getResources(), bitmap));
                    linearLayout.getBackground().setAlpha(200);

                } catch (Exception e) {
                    e.printStackTrace();
                }

                prevIdx = songList.indexOf(song);
            }
        }
    }

    @Override
    protected void startListener() {
        ctrlButton.toggle(false);
        ctrlButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicBound) {
                    MediaControlUtils.OnPause(activity);
                    if (!isPlay) {
                        ctrlButton.toggle();
                        isPlay = true;
                    }
                } else {
                    MediaControlUtils.Onstart(activity);
                    if (isPlay) {
                        ctrlButton.toggle();
                        isPlay = false;
                    }
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
                if (isPlay) {
                    ctrlButton.toggle();
                    isPlay = false;
                }
                isChanging = false;
            }
        });

        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                try {
                    musicBound = MediaService.resiceStatus();

                    boolean currShuffled = MediaService.reciveShuffleMode();
                    if (currShuffled != isShuffled) {
                        if (currShuffled) {
                            shuffleButton.setImageResource(R.drawable.ic_shuffle_red);
                            isShuffled = true;
                        } else {
                            shuffleButton.setImageResource(R.drawable.ic_shuffle);
                            isShuffled = false;
                        }
                    }

                    boolean currRepeated = MediaService.recieveRepeatMode();
                    if (currRepeated != isRepeating) {
                        if (currRepeated) {
                            repeatButton.setImageResource(R.drawable.ic_repeat_red);
                            isShuffled = true;
                        } else {
                            repeatButton.setImageResource(R.drawable.ic_repeat);
                            isShuffled = false;
                        }
                    }


                    long tempSong = MediaService.getCurrentSong();
                    if (tempSong != currSong) {
                        currSong = tempSong;
                        UpdateDisplayData();
                    }


                    Song song = MediaControlUtils.findSong(currSong);
                    assert song != null;
                    int songPosition = MediaService.getSongPosition();

                    int songDuration = song.getDuration();
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

                } catch (Exception ignored) { ignored.printStackTrace(); }

                handler.postDelayed(this, Constants.HANDLER_DELAY);

            }
        }, Constants.HANDLER_DELAY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        UpdateDisplayData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow_menu_music_player, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
                return true;
            }
            case R.id.share: {
                ShareUtils.shareTrack(activity, currSong);
                return true;
            }
            case R.id.equalizer: {
                NavUtils.goToEqualizer(activity);
                return true;
            }
            case R.id.change_cover: {
                Song song = MediaControlUtils.findSong(currSong);
                if (song != null)
                    selectPhoto(song.getAlbumId());
                return true;
            }
            case R.id.add_to_playlist: {
                Song temp = MediaControlUtils.findSong(currSong);
                if (temp != null) {
                    Bundle bundle = new Bundle();
                    ArrayList<Song> tempList = new ArrayList<>();
                    tempList.add(temp);
                    bundle.putParcelableArrayList(Constants.SONG_LIST, tempList);
                    DialogFragment dialogFragment = new PlaylistAddSongsDialog();
                    dialogFragment.setArguments(bundle);
                    dialogFragment.show(activity.getFragmentManager(), "AddToPlaylist");
                }
                return true;
            }
            case R.id.go_to_artist: {
                Song temp = SongLoader.findSongs(currSong);
                if (temp != null)
                    NavUtils.goToArtist(activity, temp.getArtistId());
                return true;
            }
            case R.id.go_to_album: {
                Song temp = SongLoader.findSongs(currSong);
                if (temp != null)
                    NavUtils.goToAlbum(activity, temp.getAlbumId());
                return true;
            }
        }

        return false;
    }

    public void selectPhoto(long id) {
        albumId = id;
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), Constants.RESULT_CHOOSE_ALBUM_COVER);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Change photo request
        if (resultCode == RESULT_OK && requestCode == Constants.RESULT_CHOOSE_ALBUM_COVER) {
            if (data != null) {
                Uri uri = data.getData();
                String[] file = {MediaStore.Images.Media.DATA};
                Cursor cursor = getContentResolver().query(uri, file, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    String path = cursor.getString(cursor.getColumnIndex(file[0]));
                    cursor.close();
                    MediaDataUtils.changeAlbumArt(path, albumId, getApplicationContext());
                    UpdateDisplayData();
                }
            } else {
                Log.e(TAG, "Error, no data.");
            }
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event){
        this.gestureDetector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    @Override
    public void onDestroy() {
        PicassoTools.clearCache(Picasso.with(this));
        super.onDestroy();
    }
}
