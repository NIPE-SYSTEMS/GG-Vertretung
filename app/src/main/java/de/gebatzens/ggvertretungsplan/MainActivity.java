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
import android.os.AsyncTask;
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
    Toolbar mToolbar;
    int selected = 0;
    public GGContentFragment mContent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GGApp.GG_APP.mActivity = this;

        selected = Integer.parseInt(GGApp.GG_APP.mSettings.getProperty("gg_prev_selection", "0"));

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

                if(menuItem.getItemId() == R.id.action_refresh) {
                    mContent.mGGFrag.setFragmentsLoading();
                    GGApp.GG_APP.refreshAsync(null, true);
                } else if(menuItem.getItemId() == R.id.action_settings) {
                    Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                    startActivityForResult(i, 1);
                }

                return false;
            }
        });

        mStrings = new String[] {"Gymnasium Glinde", "Sachsenwaldschule", "Einstellungen"};

        mToolbar.setTitle(mStrings[selected]);
        mToolbar.inflateMenu(R.menu.toolbar_menu);
        mToolbar.setTitleTextColor(Color.WHITE);
        //toolbar.setNavigationIcon(R.drawable.ic_menu_white);

        //wait for vps
        new AsyncTask<Object, Void, Void>() {

            @Override
            protected Void doInBackground(Object... params) {
                while(GGApp.GG_APP.mVPToday == null || GGApp.GG_APP.mVPTomorrow == null);
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mContent.mGGFrag.updateFragments();
                    }
                });
                return null;
            }
        }.execute();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.w("ggvp", "result=" + resultCode + " should be " + RESULT_OK + " req=" + requestCode);

        if(requestCode == 1 && resultCode == RESULT_OK) { //Settings changed
            mContent.mGGFrag.setFragmentsLoading();
            selected = GGApp.GG_APP.getDefaultSelection();
            GGApp.GG_APP.createProvider(selected);
            mToolbar.setTitle(mStrings[selected]);
            GGApp.GG_APP.saveSettings();
            GGApp.GG_APP.refreshAsync(null, true);
            Log.w("ggvp", selected + " " + GGApp.GG_APP.getVPClass());
        }

    }

}
