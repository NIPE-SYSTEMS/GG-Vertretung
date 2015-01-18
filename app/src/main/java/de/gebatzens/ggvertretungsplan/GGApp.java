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

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class GGApp extends Application {

    public GGPlan mVPToday, mVPTomorrow;
    public MainActivity mActivity;
    public boolean created = false;
    public VPProvider mProvider;
    private Properties mSettings;

    public static GGApp GG_APP;

    @Override
    public void onCreate() {
        super.onCreate();
        GG_APP = this;

    }

    public void create() {
        created = true;

        loadSettings();
        mActivity.selected = Integer.parseInt(mSettings.getProperty("gg_prev_selection", "0"));

        createProvider(mActivity.selected);
        //mVPToday = mProvider.getVP(mProvider.getTodayURL());
        //mVPTomorrow = mProvider.getVP(mProvider.getTomorrowURL());

        refreshAsync(null);

    }

    public String getVPClass(int s) {
        String str = mSettings.getProperty("gg_class" + s, "");
        return str;
    }

    public void setVPClass(int s, String cl) {
        mSettings.put("gg_class" + s, cl);
    }

    public void setDefaultSelection(int s) {
        mSettings.put("gg_prev_selection", ""+s);
    }

    public int getDefaultSelection() {
        return Integer.parseInt(mSettings.getProperty("gg_prev_selection", "0"));
    }

    public void loadSettings() {
        mSettings = new Properties();
        try {
            InputStream in = mActivity.openFileInput("ggsettings");
            mSettings.load(in);
        } catch (IOException e) {
            mSettings.put("gg_prev_selection", "0");
            mSettings.put("gg_class0", "");
            mSettings.put("gg_class1", "");
            saveSettings();

        }
    }

    public void saveSettings() {
        try {
            OutputStream out = mActivity.openFileOutput("ggsettings", Context.MODE_PRIVATE);
            mSettings.store(out, "GGSettings");
        } catch (IOException e) {
            e.printStackTrace();
            showToast(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public void showToast(String s) {
        Toast.makeText(mActivity, s, Toast.LENGTH_LONG).show();
    }

    public void createProvider(int selected) {
        switch(selected) {
            case 0:
                mProvider = new GGProvider();
                break;
            case 1:
                mProvider = new SWSProvider();
                break;
        }
    }

    public void updateVP() {
        mVPToday = mProvider.getVP(mProvider.getTodayURL());
        mVPTomorrow = mProvider.getVP(mProvider.getTomorrowURL());
        mActivity.mContent.mGGFrag.updateFragments();
    }

    public void refreshAsync(final Runnable finished) {
        new AsyncTask<Object, Void, Void>() {

            @Override
            protected Void doInBackground(Object... params) {

                mVPToday = mProvider.getVPSync(mProvider.getTodayURL());
                mVPTomorrow = mProvider.getVPSync(mProvider.getTomorrowURL());

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mActivity.mContent.mGGFrag.updateFragments();
                    }
                });

                if(finished != null)
                    mActivity.runOnUiThread(finished);
                return null;
            }
        }.execute();
    }


}
