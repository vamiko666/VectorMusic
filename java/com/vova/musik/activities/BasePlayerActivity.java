
package com.vova.musik.activities;


import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.vova.musik.R;
import com.vova.musik.models.Song;
import com.vova.musik.utils.Constants;

import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


public abstract class BasePlayerActivity extends AppCompatActivity {

    protected Toolbar toolbar;
    protected List<Song> songList;

    protected int id;
    protected long currSong;

    public BasePlayerActivity(int id) {
        this.id = id;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath(Constants.FONT_PATH)
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(id);


        startReturn();


        startMainView();


        getDisplayData();


        showDisplayData();


        startListener();
    }

    private void startReturn() {


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    protected abstract void getDisplayData();

    protected abstract void showDisplayData();

    protected abstract void UpdateDisplayData();

    protected abstract void startMainView();

    protected abstract void startListener();
}
