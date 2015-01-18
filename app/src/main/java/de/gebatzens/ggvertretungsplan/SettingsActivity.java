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
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.List;

public class SettingsActivity extends Activity {

    private Toolbar mToolBar;
    private GGPFragment mFrag;
    private static boolean changed;

    public static class GGPFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle s) {
            super.onCreate(s);

            addPreferencesFromResource(R.xml.preferences);
            SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
            sp.registerOnSharedPreferenceChangeListener(this);
            GGApp gg = GGApp.GG_APP;
            String pref_schule_content = gg.getDefaultSelection() == 0 ? "Gymnasium Glinde" : "Sachsenwaldschule";
            String pref_klasse_content = gg.getVPClass(gg.getDefaultSelection());
            if(pref_klasse_content.equals(""))
                pref_klasse_content = "Keine ausgewählt";

            Preference pref_schule = findPreference("schule");
            pref_schule.setSummary(pref_schule_content);

            Preference pref_klasse = findPreference("klasse");
            pref_klasse.setSummary(pref_klasse_content);


        }

        @Override
         public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference pref = findPreference(key);

            changed = true;

            if (pref instanceof ListPreference) { //Schule
                ListPreference listPref = (ListPreference) pref;
                pref.setSummary(listPref.getEntry());
                GGApp.GG_APP.setDefaultSelection(listPref.getEntry().equals("Gymnasium Glinde") ? 0 : 1);
                Preference kl = findPreference("klasse");
                kl.setSummary(GGApp.GG_APP.getVPClass(GGApp.GG_APP.getDefaultSelection()));
                if(kl.getSummary().equals(""))
                    kl.setSummary("Keine ausgewählt");
            }
            if (pref instanceof EditTextPreference) {
                EditTextPreference editTextPref = (EditTextPreference) pref;
                if(editTextPref.getText().equals("")){ //Klasse
                    pref.setSummary("Keine ausgewählt");
                } else{
                    pref.setSummary(editTextPref.getText());
                }
                GGApp.GG_APP.setVPClass(GGApp.GG_APP.getDefaultSelection(), editTextPref.getText());
            }
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        changed = false;
        super.onCreate(savedInstanceState);

        ViewGroup contentView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.settings_activity, new LinearLayout(this), false);

        if(savedInstanceState != null)
            changed = savedInstanceState.getBoolean("ggs_changed");

        mToolBar = (Toolbar) contentView.findViewById(R.id.toolbar);
        mToolBar.setTitleTextColor(Color.WHITE);
        mToolBar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                buttonBack(null);
            }
        });

        mToolBar.setTitle(getTitle());

        if(savedInstanceState == null)
            getFragmentManager().beginTransaction().replace(R.id.content_wrapper, new GGPFragment()).commit();

        setContentView(contentView);
    }


    @Override
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        b.putBoolean("ggs_changed", changed);
    }

    @Override
    public void onBackPressed() {
        buttonBack(null);
    }

    public void buttonBack(View view) {
        Intent i = new Intent();
        setResult(changed ? RESULT_OK : RESULT_CANCELED, i);
        this.finish();
    }

}