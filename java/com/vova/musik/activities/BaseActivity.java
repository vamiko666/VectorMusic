package com.vova.musik.activities;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.vova.musik.R;
import com.vova.musik.utils.Constants;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;


public abstract class BaseActivity extends AppCompatActivity {

    private static final String TAG = BaseActivity.class.getName();

    protected int id;
    protected long currSong;
    protected Toolbar toolbar;
    protected DrawerLayout menuDrawer;
    protected NavigationView navView;
    protected ActionBarDrawerToggle toggle;

    public BaseActivity(int id) {
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


        startToolbar();
    }

    protected void startToolbar() {


        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);


        menuDrawer = (DrawerLayout) findViewById(R.id.menu_drawer);
        toggle = new ActionBarDrawerToggle(this, menuDrawer, toolbar, R.string.confirm, R.string.cancel);
        menuDrawer.setDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();
        navView = (NavigationView) findViewById(R.id.menu_drawer_list);
        navView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectItem(item);
                return true;
            }
        });
    }

    protected abstract void selectItem(MenuItem item);
}
