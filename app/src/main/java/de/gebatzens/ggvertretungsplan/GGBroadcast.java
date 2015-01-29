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

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class GGBroadcast extends BroadcastReceiver {

    public void checkForUpdates(final GGApp gg, boolean notification) {
        if(!gg.notificationsEnabled() && notification)
            return;
        if(gg.getUpdateType() == GGApp.UPDATE_DISABLE) {
            Log.w("ggvp", "update disabled");
            return;
        }
        boolean w = isWlanConnected(gg);
        if(!w && gg.getUpdateType() == GGApp.UPDATE_WLAN ) {
            Log.w("ggvp", "wlan not conected");
            return;
        }
        VPProvider prov = gg.mProvider;
        GGPlan today = prov.getVPSync(prov.getTodayURL());
        GGPlan tomo = prov.getVPSync(prov.getTomorrowURL());

        if(today.throwable != null || tomo.throwable != null)
            return;

        gg.mVPToday = today;
        gg.mVPTomorrow = tomo;
        if(gg.mActivity != null)
            gg.mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    gg.mActivity.mContent.mGGFrag.updateFragments();
                }
            });

        Properties p = new Properties();
        try {
            InputStream in;
            p.load(in = gg.openFileInput("ggsavedstate"));
            in.close();
        } catch(Exception e) {
            p.setProperty("todaydate", today.date);
            p.setProperty("tomdate", tomo.date);
            p.setProperty("todayles", "");
            p.setProperty("tomoles", "");
        }

        boolean b = false;

        String[] td = p.getProperty("todayles").split(";");
        String[] tdn = new String[today.getAllForClass(gg.getSelectedGrade()).size()];
        int i = 0;
        for(String[] ss : today.getAllForClass(gg.getSelectedGrade())) {
            tdn[i] = ss[1];
            i++;
        }
        if(td[0].isEmpty())
            td = new String[0];
        if(tdn.length != td.length) { //Heute fällt mehr/(weniger) aus
            b = true;
            Log.d("ggvp", "Heutestd nicht gleich " + tdn.length + " " + td.length);

        }

        String[] tm = p.getProperty("tomoles").split(";");
        String[] tmn = new String[tomo.getAllForClass(gg.getSelectedGrade()).size()];
        i = 0;
        for(String[] ss : tomo.getAllForClass(gg.getSelectedGrade())) {
            tmn[i] = ss[1];
            i++;
        }
        if(tm[0].isEmpty())
            tm = new String[0];
        if(tmn.length != tm.length) { //Morgen fällt mehr/(weniger) aus
            b = true;
            Log.d("ggvp", "Morgenstd nicht gleich " + tmn.length + " " + tm.length);
        }

        if(!today.date.equals(p.getProperty("todaydate"))) {
            b = true;
            Log.d("ggvp", "Datum anders: " + today.date + " " + p.getProperty("todaydate"));
        }

        p.setProperty("todaydate", today.date);
        p.setProperty("tomdate", tomo.date);
        String tdnstr = "";
        for(String s : tdn)
            tdnstr += s + ";";
        if(!tdnstr.isEmpty())
            tdnstr = tdnstr.substring(0, tdnstr.length() - 1);
        p.setProperty("todayles", tdnstr);
        String tmnstr = "";
        for(String s : tmn)
            tmnstr += s + ";";
        if(!tmnstr.isEmpty())
            tmnstr = tmnstr.substring(0, tmnstr.length() - 1);
        p.setProperty("tomoles", tmnstr);

        if(b) {
            String stdt = "";
            for(String s : tdn)
                stdt += s + ", ";
            if(!stdt.isEmpty())
                stdt = stdt.substring(0, stdt.length() - 2);
            else
                stdt = "Nichts";
            String stdtm = "";
            for(String s : tmn)
                stdtm += s + ", ";
            if(!stdtm.isEmpty())
                stdtm = stdtm.substring(0, stdtm.length() - 2);
            else
                stdtm = "Nichts";
            gg.createNotification("Vertretungsplanänderung", "Der Vertretungsplan hat sich geändert", 123, "Betroffene Stunden:", "Heute: " + stdt, "Morgen: " + stdtm);
        } else
            Log.d("ggvp", "Up to date!");

        try {
            OutputStream out;
            p.store(out = gg.openFileOutput("ggsavedstate", Context.MODE_PRIVATE), "GGSavedState");
            out.close();
        } catch(Exception e) {
            e.printStackTrace();
            gg.showToast(e.getClass().getName() + " " + e.getMessage());
        }

    }

    public static void createAlarm(Context context) {
        Intent i = new Intent(context, GGBroadcast.class);
        i.setAction("de.gebatzens.ACTION_ALARM");
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pi);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 60000, AlarmManager.INTERVAL_HALF_HOUR, pi);
    }

    public static boolean isWlanConnected(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("ggvp", "onReceive " + intent.getAction());
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent i = new Intent(context, GGBroadcast.class);
            i.setAction("de.gebatzens.ACTION_ALARM");
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, AlarmManager.INTERVAL_HALF_HOUR, pi);
        } else if (intent.getAction().equals("de.gebatzens.ACTION_ALARM")) {
            new AsyncTask<GGApp, Void, Void>() {

                @Override
                protected Void doInBackground(GGApp... params) {
                    checkForUpdates(params[0], true);
                    return null;
                }
            }.execute((GGApp) context.getApplicationContext());

        } else if (intent.getAction().equals(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION)) {
                new AsyncTask<GGApp, Void, Void>() {

                    @Override
                    protected Void doInBackground(final GGApp... params) {
                        int s = 0;
                        while(!isWlanConnected(params[0])) {
                            try {
                                Thread.sleep(300);
                            } catch (InterruptedException e) {

                            }
                            s++;
                            if(s > 10)
                                return null;
                        }
                        if(params[0].mActivity != null) {
                            params[0].mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    params[0].mActivity.mContent.mGGFrag.setFragmentsLoading();
                                }
                            });

                            params[0].refreshAsync(null, true);
                        } else {
                            checkForUpdates(params[0], false);
                        }
                        return null;
                    }
                }.execute((GGApp) context.getApplicationContext());

        }
    }

}
