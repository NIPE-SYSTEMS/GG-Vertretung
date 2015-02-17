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
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
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
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Scanner;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class SettingsActivity extends Activity {

    Toolbar mToolBar;
    private static boolean changed;
    static String version;


    public static class GGPFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

        SharedPreferences prefs;

        @Override
        public void onCreate(Bundle s) {
            super.onCreate(s);
            GGApp gg = GGApp.GG_APP;
            prefs = getActivity().getSharedPreferences("gguser", Context.MODE_PRIVATE);
            String sessId = prefs.getString("sessid", null);

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
            pref_buildversion.setSummary("Version: " + versionName + " (" + BuildConfig.BUILD_TYPE + ") (Zum aktualisieren berühren)");
            pref_buildversion.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    new AsyncTask<Object, Void, Void>() {

                        @Override
                        protected Void doInBackground(Object... params) {
                            try {

                                HttpsURLConnection con = (HttpsURLConnection) new URL("https://gymnasium-glinde.logoip.de/infoapp/update.php?version").openConnection();
                                con.setRequestMethod("POST");
                                con.setSSLSocketFactory(GGProvider.sslSocketFactory);

                                if (con.getResponseCode() == 200) {
                                    BufferedInputStream in = new BufferedInputStream(con.getInputStream());
                                    Scanner scan = new Scanner(in);
                                    String resp = "";
                                    while (scan.hasNextLine())
                                        resp += scan.nextLine();
                                    scan.close();
                                    if (!resp.equals(BuildConfig.VERSION_NAME)) {
                                        HttpsURLConnection con_changelog = (HttpsURLConnection) new URL("https://gymnasium-glinde.logoip.de/infoapp/update.php?changelog="+resp).openConnection();
                                        con_changelog.setRequestMethod("GET");
                                        con_changelog.setSSLSocketFactory(GGProvider.sslSocketFactory);

                                        if(con_changelog.getResponseCode() == 200) {
                                            BufferedInputStream in_changelog = new BufferedInputStream(con_changelog.getInputStream());
                                            Scanner scan_changelog = new Scanner(in_changelog);
                                            String resp_changelog = "";
                                            while (scan_changelog.hasNextLine())
                                                resp_changelog += scan_changelog.nextLine();
                                            scan_changelog.close();
                                            final String final_resp_changelog = resp_changelog;
                                            getActivity().runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                                    builder.setTitle("Aktualisierung verfügbar");
                                                    builder.setMessage("Soll die SchulinfoAPP aktualisiert werden?\n\nChangelog:\n"+final_resp_changelog.replace("|","\n"));
                                                    builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            UpdateActivity ua = new UpdateActivity(getActivity(), getActivity());
                                                            ua.execute();
                                                            dialog.dismiss();
                                                        }
                                                    });
                                                    builder.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                                                        @Override
                                                        public void onClick(DialogInterface dialog, int which) {
                                                            dialog.dismiss();
                                                        }
                                                    });
                                                    builder.create().show();
                                                }
                                            });
                                        }
                                    } else {
                                        getActivity().runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(getActivity().getApplication(), "Keine neue Version verfügbar.", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(getActivity().getApplication(), "Keine Internetverindung", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                            return null;
                        }
                    }.execute();
                    return false;
                }
            });

            Preference pref_githublink = findPreference("githublink");
            pref_githublink.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Intent linkIntent = new Intent(Intent.ACTION_VIEW);
                    linkIntent.setData(Uri.parse("https://github.com/Gebatzens/GG-Vertretung"));
                    startActivity(linkIntent);
                    return true;
                }
            });

            final Preference pref_username = findPreference("authentication_username");

            pref_username.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    if(prefs.getString("sessid", null)!=null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Abmelden");
                        builder.setMessage("Wirklich abmelden?");
                        builder.setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                GGApp.GG_APP.provider.logout();
                                pref_username.setSummary("Du bist nicht angemeldet");
                                dialog.dismiss();
                            }
                        });
                        builder.setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        builder.create().show();
                    } else {
                        //Überall in der App steht "Du", also sollte das mal einheitlich werden
                        Toast.makeText(getActivity(),"Du bist nicht angemeldet", Toast.LENGTH_SHORT).show();
                    }

                    return false;
                }
            });

            if(sessId!=null) {
                String username = prefs.getString("username", null);
                if(username!=null) {
                    pref_username.setSummary(username + " (Zum Abmelden berühren)");
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
                if(!listPref.getEntry().equals("gg"))
                    findPreference("authentication_username").setSummary("Du bist nicht angemeldet");
                else
                    findPreference("authentication_username").setSummary(prefs.getString("username", null) + "(Zum Abmelden berühren)");
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