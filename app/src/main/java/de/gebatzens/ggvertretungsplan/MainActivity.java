/*
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
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;


public class MainActivity extends FragmentActivity {

    String[] mStrings;
    DrawerLayout mDrawerLayout;
    ListView mDrawerList;
    ActionBarDrawerToggle mToggle;
    Toolbar mToolbar;
    int selected = 0;
    public GGContentFragment mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GGApp.GG_APP.mActivity = this;

        if(!GGApp.GG_APP.created)
            GGApp.GG_APP.create();
        if(savedInstanceState != null)
            selected = savedInstanceState.getInt("gg_selection");

        setContentView(getLayoutInflater().inflate(R.layout.activity_main, null));

        List<Fragment> frags = getSupportFragmentManager().getFragments();
        if(frags != null)
            for(Fragment frag : frags) {
                if(frag != null && !frag.getTag().equals("gg_content_fragment"))
                    getSupportFragmentManager().beginTransaction().remove(frag).commit();
            }

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            mContent = new GGContentFragment();
            transaction.replace(R.id.content_fragment, mContent, "gg_content_fragment");
            transaction.commit();
        } else
            mContent = (GGContentFragment) getSupportFragmentManager().findFragmentByTag("gg_content_fragment");

        mToolbar = (Toolbar) findViewById(R.id.toolbar);

        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                mContent.mGGFrag.setFragmentsLoading();
                GGApp.GG_APP.refreshAsync(null);

                return false;
            }
        });

        mStrings = new String[] {"Gymnasium Glinde", "Sachsenwaldschule", "Einstellungen"};

        mToolbar.setTitle(mStrings[selected]);
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
        mDrawerList.setItemChecked(selected, true);

        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(position < 2) {
                    selected = position;
                    mToolbar.setTitle(mStrings[position]);
                    mDrawerLayout.closeDrawers();
                    mContent.mGGFrag.setFragmentsLoading();
                    GGApp.GG_APP.setDefaultSelection(selected);
                    GGApp.GG_APP.saveSettings();
                    GGApp.GG_APP.createProvider(position);
                    GGApp.GG_APP.refreshAsync(null);

                } else if(position == 2) {
                    //ignore settings selection
                    mDrawerList.setItemChecked(position, false);
                    mDrawerList.setItemChecked(selected, true);
                    mDrawerLayout.closeDrawers();
                    Intent intent = new Intent(GGApp.GG_APP.mActivity, SettingsActivity.class);

                    GGApp.GG_APP.mActivity.startActivityForResult(intent, 1);

                }


            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle s) {
        super.onSaveInstanceState(s);
        s.putInt("gg_selection", selected);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mToggle.syncState();

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.w("ggvp", "result=" + resultCode + " should be " + RESULT_OK + " req=" + requestCode);

        if(requestCode == 1 && resultCode == RESULT_OK) { //Settings changed
            mContent.mGGFrag.setFragmentsLoading();
            selected = GGApp.GG_APP.getDefaultSelection();
            mDrawerList.setItemChecked(0, false);
            mDrawerList.setItemChecked(1, false);
            mDrawerList.setItemChecked(selected, true);
            GGApp.GG_APP.createProvider(selected);
            mToolbar.setTitle(mStrings[selected]);
            GGApp.GG_APP.saveSettings();
            GGApp.GG_APP.refreshAsync(null);
            Log.w("ggvp", selected + " " + GGApp.GG_APP.getVPClass());
        }

    }

}
