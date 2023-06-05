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
package com.vova.musik.activities;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.Handler;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.vova.musik.R;
import com.vova.musik.services.MediaService;
import com.vova.musik.utils.Constants;
import com.vova.musik.utils.MediaControlUtils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public abstract class ListPlayerActivity extends BasePlayerActivity {

    private static final String TAG = ListPlayerActivity.class.getName();
    private Activity activity = this;

    protected TextView listName;
    protected ListView songListView;
    protected ImageView containerBackground;
    private ImageView listController;
    private SeekBar progressBar;
    private ImageSwitcher playButton;
    private Handler handler;

    private boolean isChanging;
    private boolean musicBound;

    public ListPlayerActivity() {
        super(R.layout.activity_list_player);
    }

    @Override
    @SuppressLint("NewApi")
    protected void startMainView() {

        listName = (TextView) findViewById(R.id.name);
        songListView = (ListView) findViewById(R.id.song_list);
        containerBackground = (ImageView) findViewById(R.id.background);
        listController = (ImageView) findViewById(R.id.controller);
        progressBar = (SeekBar) findViewById(R.id.songbar_progress);
        playButton = (ImageSwitcher) findViewById(R.id.songbar_play);

        ListPlayerActivity.this.setTitle("");
        progressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
        progressBar.getThumb().setColorFilter(getResources().getColor(R.color.red), PorterDuff.Mode.SRC_IN);
    }

    protected void startListener() {


        if (((AudioManager) activity.getSystemService(Context.AUDIO_SERVICE)).isMusicActive()) {
            playButton.setBackgroundResource(R.drawable.ic_pause);
        }
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (musicBound) {
                    MediaControlUtils.OnPause(activity);
                    playButton.setBackgroundResource(R.drawable.ic_play);
                } else {
                    MediaControlUtils.Onstart(activity);
                    playButton.setBackgroundResource(R.drawable.ic_pause);
                }
            }
        });
        listController.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MediaControlUtils.OnstartQueueShuffled(activity, songList);
                playButton.setBackgroundResource(R.drawable.ic_pause);
                Intent intent = new Intent(activity, MusicPlayerActivity.class);
                startActivity(intent);
            }
        });
        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                MediaControlUtils.OnstartQueueRepeatingSpecific(activity, songList, position);
                playButton.setBackgroundResource(R.drawable.ic_pause);
                Intent intent = new Intent(activity, MusicPlayerActivity.class);
                startActivity(intent);
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
                            playButton.setBackgroundResource(R.drawable.ic_pause);
                        } else {
                            playButton.setBackgroundResource(R.drawable.ic_play);
                        }
                    }
                    musicBound = status;


                    long tempSong = MediaService.getCurrentSong();
                    if (tempSong != currSong) {
                        currSong = tempSong;
                        UpdateDisplayData();
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

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onDestroy() {
        PicassoTools.clearCache(Picasso.with(this));
        System.gc();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.overflow_menu_list_player, menu);
        return true;
    }
}
