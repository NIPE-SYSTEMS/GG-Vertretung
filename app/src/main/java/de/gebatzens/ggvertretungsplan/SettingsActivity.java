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

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.Properties;

public class SettingsActivity extends Activity {

    Toolbar mToolBar;
    private static boolean changed;
    static String version;

    public static class GGPFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle s) {
            super.onCreate(s);
            GGApp gg = GGApp.GG_APP;
            Properties props = new Properties();
            String sessId;
            try {
                props.load(GGApp.GG_APP.openFileInput("gguserinfo"));
                sessId = props.getProperty("sessid");
            } catch(Exception e) {
                sessId = null;
                e.printStackTrace();
            }
            addPreferencesFromResource(R.xml.preferences);
            SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
            sp.registerOnSharedPreferenceChangeListener(this);
            String pref_schule_content = gg.provider.getFullName();
            String pref_klasse_content = gg.getSelectedClass();
            if(pref_klasse_content.equals(""))
                pref_klasse_content = "Keine ausgewählt";

            Preference pref_schule = findPreference("schule");
            pref_schule.setSummary(pref_schule_content);

            Preference pref_klasse = findPreference("klasse");
            pref_klasse.setSummary(pref_klasse_content);

            Preference update = findPreference("appupdates");
            update.setSummary(gg.translateUpdateType(gg.getUpdateType()));

            Preference pref_buildversion = findPreference("buildversion");
            String versionName = BuildConfig.VERSION_NAME;
            pref_buildversion.setSummary("Version: " + versionName + " (" + BuildConfig.BUILD_TYPE + ")");

            Preference pref_githublink = findPreference("githublink");
            pref_githublink.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Intent linkIntent = new Intent(Intent.ACTION_VIEW);
                    linkIntent.setData(Uri.parse("https://github.com/Gebatzens/GG-Vertretung"));
                    startActivity(linkIntent);
                    return true;
                }
            });

            Preference pref_username = findPreference("authentication_username");
            pref_username.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    GGApp.GG_APP.provider.logout();
                    preference.setSummary("Sie sind nicht angemeldet");
                    return false;
                }
            });
            if(sessId!=null) {
                String username = props.getProperty("username");
                if(username!=null) {
                    pref_username.setSummary(username + " (zum abmelden berühren)");
                }
            }

        }

        @Override
         public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);

            changed = true;


            if (key.equals("schule")) {
                ListPreference listPref = (ListPreference) pref;
                pref.setSummary(listPref.getEntry());
            } else if(key.equals("klasse")) {
                EditTextPreference editTextPref = (EditTextPreference) pref;
                if(editTextPref.getText().equals("")){ //Klasse
                    pref.setSummary("Keine ausgewählt");
                } else{
                    pref.setSummary(editTextPref.getText());
                }
            } else if(key.equals("appupdates")) {
                ListPreference listPreference = (ListPreference) pref;
                listPreference.setSummary(listPreference.getEntry());
            }


        }

        @Override
        public void onDestroy() {
            super.onDestroy();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        changed = false;
        Fragment f = getFragmentManager().findFragmentByTag("gg_settings_frag");
        if(f != null) {
            getFragmentManager().beginTransaction().remove(f).commit();
        }

        super.onCreate(savedInstanceState);

        ViewGroup contentView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.settings_activity, new LinearLayout(this), false);

        if(savedInstanceState != null) {
            changed = savedInstanceState.getBoolean("ggs_changed");

        }

        setTheme(GGApp.GG_APP.provider.getTheme());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            GGApp.GG_APP.setStatusBarColor(getWindow());
        }

        mToolBar = (Toolbar) contentView.findViewById(R.id.toolbar);
        mToolBar.setBackgroundColor(GGApp.GG_APP.provider.getColor());
        mToolBar.setTitleTextColor(Color.WHITE);
        mToolBar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mToolBar.setTitle(getTitle());

        getFragmentManager().beginTransaction().replace(R.id.content_wrapper, new GGPFragment(), "gg_settings_frag").commit();

        setContentView(contentView);
    }


    @Override
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        b.putBoolean("ggs_changed", changed);
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void finish() {
        Intent i = new Intent();
        setResult(changed ? RESULT_OK : RESULT_CANCELED, i);
        super.finish();
    }

}