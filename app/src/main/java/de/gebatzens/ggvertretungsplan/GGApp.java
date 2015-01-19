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
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
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
    public Properties mSettings;

    public static GGApp GG_APP;

    @Override
    public void onCreate() {
        super.onCreate();
        GG_APP = this;
        GGBroadcast.createAlarm(this);
        create();

    }

    private void create() {
        created = true;

        loadSettings();

        createProvider(getDefaultSelection());
        //mVPToday = mProvider.getVP(mProvider.getTodayURL());
        //mVPTomorrow = mProvider.getVP(mProvider.getTomorrowURL());

        refreshAsync(null, false);

        Log.w("ggvp", "GGApp created (" + getVPClass() + " " + getDefaultSelection() + ")");

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

    public String getVPClass() {
        String str = mSettings.getProperty("gg_class", "");
        return str;
    }

    public void setVPClass(String cl) {
        mSettings.put("gg_class", cl);
    }

    public void setDefaultSelection(int s) {
        mSettings.put("gg_prev_selection", ""+s);
    }

    public int getDefaultSelection() {
        return Integer.parseInt(mSettings.getProperty("gg_prev_selection", "0"));
    }

    public boolean getNotificationsEnabled() {
        return Boolean.parseBoolean(mSettings.getProperty("gg_notifications", "true"));
    }

    public void setNotificationsEnabled(boolean b) {
        mSettings.setProperty("gg_notifications", "" + b);
    }

    public void loadSettings() {
        mSettings = new Properties();
        try {
            InputStream in = getApplicationContext().openFileInput("ggsettings");
            mSettings.load(in);
            in.close();
        } catch (IOException e) {
            mSettings.put("gg_prev_selection", "0");
            mSettings.put("gg_class", "");
            saveSettings();

        }
    }

    public void saveSettings() {
        try {
            OutputStream out = getApplicationContext().openFileOutput("ggsettings", Context.MODE_PRIVATE);
            mSettings.store(out, "GGSettings");
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
            showToast(e.getClass().getName() + ": " + e.getMessage());
        }
    }

    public void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_LONG).show();
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

    public void refreshAsync(final Runnable finished, final boolean updateFragments) {
        new AsyncTask<Object, Void, Void>() {

            @Override
            protected Void doInBackground(Object... params) {

                mVPToday = mProvider.getVPSync(mProvider.getTodayURL());
                mVPTomorrow = mProvider.getVPSync(mProvider.getTomorrowURL());

                if(updateFragments)
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
