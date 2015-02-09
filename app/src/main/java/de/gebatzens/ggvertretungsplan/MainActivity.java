/*
 * Copyright (C) 2015 Hauke Oldsen
 *
 * This file is part of GGVertretungsplan.
 *
 * GGVertretungsplan is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GGVertretungsplan is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GGVertretungsplan.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.gebatzens.ggvertretungsplan;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;


public class MainActivity extends FragmentActivity {

    public RemoteDataFragment mContent;
    Toolbar mToolbar;
    ListView mDrawerList;
    TextView mDraverSettings;
    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mToggle;
    String[] mStrings = new String[] {"Vertretungsplan", "News", "Mensa"};

    public RemoteDataFragment createFragment() {
        return new GGContentFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GGApp.GG_APP.mActivity = this;

        setContentView(getLayoutInflater().inflate(R.layout.activity_main, null));

        setTheme(GGApp.GG_APP.mProvider.getTheme());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            GGApp.GG_APP.setStatusBarColor(getWindow());
        }

        List<Fragment> frags = getSupportFragmentManager().getFragments();
        if(frags != null)
            for(Fragment frag : frags) {
                getSupportFragmentManager().beginTransaction().remove(frag).commit();
            }

        //if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            mContent = createFragment();
            transaction.replace(R.id.content_fragment, mContent, "gg_content_fragment");
            transaction.commit();
        //} else
        //    mContent = (GGContentFragment) getSupportFragmentManager().findFragmentByTag("gg_content_fragment");

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mToolbar.setBackgroundColor(GGApp.GG_APP.mProvider.getColor());
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                if(menuItem.getItemId() == R.id.action_refresh) {
                    mContent.setFragmentLoading();
                    GGApp.GG_APP.refreshAsync(null, true);
                } else if(menuItem.getItemId() == R.id.action_settings) {
                    Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivityForResult(i, 1);
                }

                return false;
            }
        });

        mToolbar.setTitle(GGApp.GG_APP.mProvider.getFullName());
        mToolbar.inflateMenu(R.menu.toolbar_menu);
        mToolbar.setTitleTextColor(Color.WHITE);
        //toolbar.setNavigationIcon(R.drawable.ic_menu_white);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                mToolbar, R.string.drawer_open, R.string.drawer_close) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };

        mDrawerLayout.setDrawerListener(mToggle);

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        ArrayAdapter<String> aa = new ArrayAdapter<String>(this, R.layout.drawer_list_item, mStrings);
        mDrawerList.setAdapter(aa);
        mDrawerList.setItemChecked(0, true);
        mDrawerList.setSelection(savedInstanceState == null ? 0 : fragTypeToInt(GGApp.GG_APP.getFragmentType()));

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mDrawerLayout.closeDrawers();
                GGApp.GG_APP.setFragmentType(GGApp.FragmentType.values()[position]);
            }
        });

        mDraverSettings = (TextView) findViewById(R.id.left_draver_settings);
        mDraverSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                mDrawerLayout.closeDrawers();
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivityForResult(i, 1);
            }
        });


        //wait for vps
        new AsyncTask<Object, Void, Void>() {

            @Override
            protected Void doInBackground(Object... params) {
                boolean b = true;
                while(b) {
                    switch(GGApp.GG_APP.getFragmentType()) {
                        case PLAN:
                            b = GGApp.GG_APP.mVPToday == null || GGApp.GG_APP.mVPTomorrow == null;
                            break;
                        case NEWS:
                            //TODO
                            b = false;
                            break;
                        case MENSA:
                            //TODO
                            b = false;
                            break;
                    }
                }
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mContent.updateFragment();
                    }
                });
                return null;
            }
        }.execute();

    }

    public int fragTypeToInt(GGApp.FragmentType type) {
        switch(type) {
            case PLAN:
                return 0;
            case NEWS:
                return 1;
            case MENSA:
                return 2;
            default:
                return -1;
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mToggle.syncState();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        GGApp.GG_APP.mActivity = null;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 1 && resultCode == RESULT_OK) { //Settings changed
            GGApp.GG_APP.recreateProvider();
            setTheme(GGApp.GG_APP.mProvider.getTheme());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                GGApp.GG_APP.setStatusBarColor(getWindow());
            }
            mToolbar.setBackgroundColor(GGApp.GG_APP.mProvider.getColor());
            mToolbar.setTitle(GGApp.GG_APP.mProvider.getFullName());


            if(GGApp.GG_APP.getFragmentType() == GGApp.FragmentType.PLAN) {
                ((GGContentFragment)mContent).mSlidingTabLayout.setBackgroundColor(GGApp.GG_APP.mProvider.getColor());
                mContent.setFragmentLoading();
            }
            GGApp.GG_APP.refreshAsync(null, true);
        }

    }

}
