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

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SWSProvider extends VPProvider {

    GGApp ggapp;

    public SWSProvider(GGApp gg) {
        super(gg);
    }

    public GGPlan[] getPlans(boolean toast) {
        Log.w("ggvp", "Lade SWS PLäne");
        GGPlan[] plans = new GGPlan[2];

        plans[0] = getPlan("http://contao.sachsenwaldschule.org/files/dateiablage_extern/vertretungsplanung/schueler_online/subst_001.htm", toast);
        plans[1] = getPlan("http://contao.sachsenwaldschule.org/files/dateiablage_extern/vertretungsplanung/schueler_online/subst_002.htm", false);

        return plans;
    }

    public GGPlan getPlan(String url, boolean toast) {
        final GGPlan plan = new GGPlan();

        try {
            URLConnection con = new URL(url).openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "ISO-8859-1"));

            Pattern date = Pattern.compile("<div class=\"mon_title\">(.*)</div>");
            Pattern tables = Pattern.compile("<table class=\"mon_list\" >");
            Pattern tdata = Pattern.compile("<td .*?>(.*?)</td>");
            String specialBegin = "<tr class=\"info\"><th class=\"info\" align=\"center\" colspan=\"2\">Nachrichten zum Tag</th></tr>";
            Pattern specialCont = Pattern.compile("<tr class='info'><td class='info' colspan=\"2\">(.*?)</td></tr>");
            int h = 0;
            String lastClass = "Bug";
            int ln = 0;

            String line;
            while ((line = decode(reader.readLine())) != null) {
                ln++;

                if(plan.date == null) {
                    Matcher md = date.matcher(line);
                    if (md.find())
                        plan.date = new SimpleDateFormat("dd.M.yyyy").parse(md.group(1).trim().split(" ")[0]);
                } else if(line.equals(specialBegin)) { //Nachrichten
                    String line2 = "";
                    while(!(line = decode(reader.readLine())).equals("</table>")) {
                        Matcher mc = specialCont.matcher(line);
                        if(mc.find()) {
                            plan.special.add("&#8226;  " + mc.group(1));
                        }
                    }
                } else {
                    Matcher mt = tables.matcher(line);
                    if(mt.find()) {
                        reader.readLine(); //ignore header
                        ln++;
                        String tline;
                        String current = null;
                        while(!(tline = decode(reader.readLine())).equals("</table>")) {
                            ln++;
                            if(tline.contains("colspan=\"6\"")) {
                                Matcher m = tdata.matcher(tline);
                                if(m.find()) {
                                    current = m.group(1).trim();
                                    if(current.equals("--")) // decode löscht schon zwei
                                        current = "Sonstiges";
                                } else
                                    throw new GGInvalidSourceException("Malformed class row (" + ln + "): " + tline);

                                if(current == "---") //AG (?) ignorieren
                                    current = null;
                            } else {
                                if(current == null)
                                    continue;
                                Matcher m = tdata.matcher(tline);
                                String[] data = new String[6];
                                int i = 0;
                                while(m.find()) {
                                    data[i] = m.group(1).trim();
                                    i++;
                                }
                                if(i != 6)
                                    throw new GGInvalidSourceException("Not enough data in line " + ln + ": " + tline);

                                //Die SWS hat keine Lehrereinträge
                                GGPlan.Entry e = new GGPlan.Entry();
                                e.subst = "";
                                e.clazz = current;
                                e.hour = data[0]; //Stunde
                                e.repsub = data[2]; //Vertr.
                                e.subject = data[1]; //Fach
                                e.comment = data[5]; //Typ
                                e.room = data[3];
                                e.comment += " " + data[4];

                                e.unify();
                                plan.entries.add(e);
                            }
                        }
                    }
                }

            }
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            plan.loadDate = "Stand: " + sdf.format(new Date());
        } catch (Exception e) {
            e.printStackTrace();
            plan.throwable = e;
        }

        boolean b = url.contains("001.htm");

        if(plan.throwable == null)
            plan.save(GGApp.GG_APP, b ? "swstd" : "swstm");
        else {
            if(plan.load(GGApp.GG_APP, b ? "swstd" : "swstm")) {
                final String message = plan.throwable.getMessage();
                plan.loadDate = GGApp.GG_APP.getResources().getString(R.string.no_internet_connection) + "\n" + plan.loadDate;
                plan.throwable = null;
                if(toast)
                    GGApp.GG_APP.activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            GGApp.GG_APP.showToast(plan.throwable instanceof UnknownHostException ? "Konnte contao.sachsenwaldschule.org nicht auflösen" :
                                    plan.throwable instanceof GGInvalidSourceException ? "Ungültige Antwort vom Server" : "Konnte keine Verbindung zu http://contao.sachsenwaldschule.org aufbauen");
                        }
                    });
            } else {
                plan.date = new Date();
            }
        }

        return plan;
    }

    @Override
    public int getColorArray() {
        return R.array.blueColors;
    }

    public NewsFragment.News getNews() {
        return new NewsFragment.News();
    }

    @Override
    public int getColor() {
        return GGApp.GG_APP.getResources().getColor(R.color.main_blue);
    }

    @Override
    public int getDarkColor() {
        return GGApp.GG_APP.getResources().getColor(R.color.main_blue_dark);
    }

    @Override
    public void logout(Boolean logout_local_only, Boolean delete_token) {

    }

    @Override
    public int getTheme() {
        return R.style.AppThemeBlue;
    }

    @Override
    public int getImage() {
        return R.drawable.sws_logo;
    }

    @Override
    public String getWebsite() {
        return "http://www.sachsenwaldschule.de/";
    }

    @Override
    public boolean loginNeeded() {
        return false;
    }

    @Override
    public int login(String u, String p) {
        return 0;
    }

    @Override
    public String getFullName() {
        return "Sachsenwaldschule";
    }

    @Override
    public String getUsername() {
        return null;
    }
}
