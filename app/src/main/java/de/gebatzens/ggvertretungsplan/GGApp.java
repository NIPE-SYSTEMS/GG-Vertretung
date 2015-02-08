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

import android.annotation.TargetApi;
import android.app.Application;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

public class GGApp extends Application {

    public GGPlan mVPToday, mVPTomorrow;
    public MainActivity mActivity;
    public VPProvider mProvider;
    public static final int UPDATE_DISABLE = 0, UPDATE_WLAN = 1, UPDATE_ALL = 2;
    private SharedPreferences preferences;
    public static GGApp GG_APP;
    public HashMap<String, Class<? extends VPProvider>> mProviderList = new HashMap<String, Class<? extends VPProvider>>();

    @Override
    public void onCreate() {
        super.onCreate();
        GG_APP = this;
        registerProviders();
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        GGBroadcast.createAlarm(this);
        recreateProvider();
        refreshAsync(null, false);

    }

    public void registerProviders() {
        mProviderList.clear();
        mProviderList.put("gg", GGProvider.class);
        mProviderList.put("sws", SWSProvider.class);
    }

    public void createNotification(String title, String message, int id, String... strings) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.stern)
                        .setContentTitle(title)
                        .setContentText(message);
        if(strings.length > 1) {
            NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
            inboxStyle.setBigContentTitle(strings[0]);

            boolean b = true;
            for(String s : strings) {
                if(!b) {
                    inboxStyle.addLine(s);
                }
                b = false;
            }

            mBuilder.setStyle(inboxStyle);
        }
        mBuilder.setColor(GGApp.GG_APP.mProvider.getDarkColor());
        // Creates an explicit intent for an Activity in your app
        Intent resultIntent = new Intent(this, MainActivity.class);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // mId allows you to update the notification later on.
        mNotificationManager.notify(id, mBuilder.build());
    }

    public String getSelectedGrade() {
        return preferences.getString("klasse", "");
    }

    public String getSelectedProvider() {
        return preferences.getString("schule", "gg");
    }

    public boolean notificationsEnabled() {
        return preferences.getBoolean("benachrichtigungen", false);
    }

    public void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
    }

    public void recreateProvider() {
        createProvider(getSelectedProvider());
    }

    public void createProvider(String id) {
        Class<? extends VPProvider> clas = mProviderList.get(id);
        if(clas == null)
            throw new RuntimeException("Provider for " + id + " not found");

        try {
            mProvider = (VPProvider) clas.getConstructors()[0].newInstance(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mProvider.loadLogin();
    }

    public int translateUpdateType(String s) {
        if(s.equals("disable"))
            return UPDATE_DISABLE;
        else if(s.equals("wifi"))
            return UPDATE_WLAN;
        else if(s.equals("all"))
            return UPDATE_ALL;
        return UPDATE_DISABLE;
    }

    public String translateUpdateType(int i) {
        String[] s = getResources().getStringArray(R.array.appupdatesArray);
        return s[i];
    }

    public int getUpdateType() {
        return translateUpdateType(preferences.getString("appupdates", "wifi"));
    }

    public FragmentType getFragmentType() {
        return FragmentType.valueOf(preferences.getString("fragtype", "PLAN"));
    }

    public void setFragmentType(FragmentType type) {
        preferences.edit().putString("fragtype", type.toString()).commit();
    }

    public void refreshAsync(final Runnable finished, final boolean updateFragments) {
        new AsyncTask<Object, Void, Void>() {

            @Override
            protected Void doInBackground(Object... params) {

                mVPToday = mProvider.getVPSync(mProvider.getTodayURL(), updateFragments);
                mVPTomorrow = mProvider.getVPSync(mProvider.getTomorrowURL(), updateFragments);

                //TODO news und mensa

                if(updateFragments)
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        mActivity.mContent.updateFragment();
                        }
                    });

                if(finished != null)
                    mActivity.runOnUiThread(finished);
                return null;
            }
        }.execute();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setStatusBarColor(Window w) {
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        w.setStatusBarColor(GGApp.GG_APP.mProvider.getDarkColor());
    }

    public static enum FragmentType {
        PLAN, NEWS, MENSA
    }


}
