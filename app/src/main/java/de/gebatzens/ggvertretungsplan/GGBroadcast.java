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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Properties;

public class GGBroadcast extends BroadcastReceiver {

    public void checkForUpdates(GGApp gg) {
        VPProvider prov = gg.mProvider;
        GGPlan today = prov.getVP(prov.getTodayURL());
        GGPlan tomo = prov.getVP(prov.getTomorrowURL());

        if(today.throwable != null || tomo.throwable != null)
            return;

        Properties p = new Properties();
        try {
            p.load(gg.openFileInput("ggsavedstate"));
        } catch(Exception e) {
            p.setProperty("todaydate", today.date);
            p.setProperty("tomdate", tomo.date);
            p.setProperty("todayles", "");
            p.setProperty("tomoles", "");
        }

        boolean b = false;

        String[] td = p.getProperty("todayles").split(";");
        String[] tdn = new String[today.getAllForClass(gg.getVPClass()).size()];
        int i = 0;
        for(String[] ss : today.getAllForClass(gg.getVPClass())) {
            tdn[i] = ss[1];
            i++;
        }
        if(tdn.length != td.length) { //Heute fällt mehr/(weniger) aus
            b = true;
        }

        String[] tm = p.getProperty("tomoles").split(";");
        String[] tmn = new String[tomo.getAllForClass(gg.getVPClass()).size()];
        i = 0;
        for(String[] ss : tomo.getAllForClass(gg.getVPClass())) {
            tmn[i] = ss[1];
            i++;
        }
        if(tmn.length != tm.length) { //Morgen fällt mehr/(weniger) aus
            b = true;
        }

        if(!today.date.equals(p.getProperty("todaydate")))
            b = true;

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
            String stdtm = "";
            for(String s : tmn)
                stdtm += s + ", ";
            if(!stdtm.isEmpty())
                stdtm = stdtm.substring(0, stdtm.length() - 2);
            gg.createNotification("Vertretungsplanänderung", "Vertretungsplanänderung für deine Klasse", 123, "Betroffene Stunden:", "Heute: " + stdt, "Morgen: " + stdtm);
        }

        try {
            p.store(gg.openFileOutput("ggsavedstate", Context.MODE_PRIVATE), "GGSavedState");
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
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 20000, AlarmManager.INTERVAL_HALF_HOUR, pi);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent i = new Intent(context, GGBroadcast.class);
            i.setAction("de.gebatzens.ACTION_ALARM");
            PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, PendingIntent.FLAG_CANCEL_CURRENT);

            am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, 1000, AlarmManager.INTERVAL_HALF_HOUR, pi);
        } else if (intent.getAction().equals("de.gebatzens.ACTION_ALARM")) {
            checkForUpdates((GGApp) context.getApplicationContext());
        }
    }

}
