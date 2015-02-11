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

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.List;


public class MainActivity extends FragmentActivity {

    public RemoteDataFragment mContent;
    Toolbar mToolbar;
    ListView mDrawerList;
    TextView mDrawerSettings;
    DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mToggle;
    String[] mStrings = new String[] {"Vertretungsplan", "News", "Mensa"};
    ScrollView mDrawerContent;
    ImageView navigation_schoolpicture;

    private int toPixels(float dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public RemoteDataFragment createFragment() {
        switch(GGApp.GG_APP.getFragmentType()) {
            case PLAN:
                return new GGContentFragment();
            case NEWS:
                return new NewsFragment();
            case MENSA:
                return new MensaFragment();
            default:
                return null;
        }

    }

    public void removeAllFragments() {
        List<Fragment> frags = getSupportFragmentManager().getFragments();
        if(frags != null)
            for(Fragment frag : frags) {
                if(frag != null && !frag.getTag().equals("gg_content_fragment"))
                    getSupportFragmentManager().beginTransaction().remove(frag).commit();
            }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GGApp.GG_APP.mActivity = this;

        setContentView(getLayoutInflater().inflate(R.layout.activity_main, null));

        setTheme(GGApp.GG_APP.mProvider.getTheme());

        removeAllFragments();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        mContent = createFragment();
        transaction.replace(R.id.content_fragment, mContent, "gg_content_fragment");
        transaction.commit();

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
        mToolbar.setSubtitle(mStrings[fragTypeToInt(GGApp.GG_APP.getFragmentType())]);
        mToolbar.inflateMenu(R.menu.toolbar_menu);
        mToolbar.setTitleTextColor(Color.WHITE);
        mToolbar.setSubtitleTextColor(Color.WHITE);
        //toolbar.setNavigationIcon(R.drawable.ic_menu_white);


        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            GGApp.GG_APP.setStatusBarColorTransparent(getWindow());
            mDrawerLayout.setStatusBarBackgroundColor(GGApp.GG_APP.mProvider.getDarkColor());
        }

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

        navigation_schoolpicture = (ImageView) findViewById(R.id.navigation_schoolpicture);
        navigation_schoolpicture.setImageResource(GGApp.GG_APP.mProvider.getImage());

        mDrawerLayout.setDrawerListener(mToggle);

        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        ArrayAdapter<String> aa = new ArrayAdapter<String>(this, R.layout.drawer_list_item, mStrings);
        mDrawerList.setAdapter(aa);
        mDrawerList.setItemChecked(fragTypeToInt(GGApp.GG_APP.getFragmentType()), true);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GGApp.GG_APP.setFragmentType(GGApp.FragmentType.values()[position]);
                mDrawerLayout.closeDrawers();
                mToolbar.setSubtitle(mStrings[position]);

                removeAllFragments();

                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                mContent = createFragment();
                transaction.replace(R.id.content_fragment, mContent, "gg_content_fragment");
                transaction.commit();

            }
        });
        ListviewHelper.getListViewSize(mDrawerList);

        mDrawerSettings = (TextView) findViewById(R.id.left_drawer_settings);
        mDrawerSettings.setOnClickListener(new View.OnClickListener() {
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
            navigation_schoolpicture = (ImageView) findViewById(R.id.navigation_schoolpicture);
            navigation_schoolpicture.setImageResource(GGApp.GG_APP.mProvider.getImage());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                GGApp.GG_APP.setStatusBarColorTransparent(getWindow());
                mDrawerLayout.setStatusBarBackgroundColor(GGApp.GG_APP.mProvider.getDarkColor());
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
