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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GGProvider extends VPProvider {

    GGApp ggapp;

    public GGProvider(GGApp gg) {
        super(gg);
    }


    @Override
    public GGPlan getVPSync(String url, boolean toast) {
        final GGPlan p = new GGPlan();
        try {
            if(url == null || url.isEmpty())
                throw new VPUrlFileException();
            URLConnection con = new URL(url).openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "ISO-8859-1"));

            Pattern title = Pattern.compile("<title>(.*?)</title>");
            Pattern tr = Pattern.compile("<tr .*>");
            Pattern tdata = Pattern.compile(">(.*)</td>");
            Pattern special = Pattern.compile("<h2>.*?</h2>");
            Pattern send = Pattern.compile("</div>");
            Pattern extr = Pattern.compile("<p>(.*?)</p>");
            int h = 0;
            String lastClass = "Bug";

            String line;
            while((line = decode(reader.readLine())) != null) {

                Matcher m = title.matcher(line);
                if(m.find()) {
                    p.date = m.group(1).substring(21).replaceAll("den", "der");
                } else {
                    Matcher row = tr.matcher(line);
                    if(row.find()) {
                        h++;
                        if(h > 1) {
                            String[] values = new String[5];
                            for(int i = 0; i < 5; i++) {
                                String l = decode(reader.readLine());
                                Matcher data = tdata.matcher(l);
                                if(data.find())
                                    values[i] = data.group(1).trim();
                                else
                                    values[i] = "#error";
                            }
                            if(values[0].equals(""))
                                values[0] = lastClass;
                            lastClass = values[0];

                            p.entries.add(values);
                        }
                    } else {
                        Matcher sm = special.matcher(line);
                        if(sm.find()) {
                            String spv = "";
                            String sp = "";
                            while(!send.matcher(sp = reader.readLine()).find()) {
                                Matcher mm = extr.matcher(sp);
                                if(mm.find())
                                    spv += mm.group(1) + " ";

                            }
                            p.special = spv;
                        }
                    }
                }
            }

            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
            p.loadDate = "Stand: " + sdf.format(new Date());
        } catch (Exception e) {
            e.printStackTrace();
            p.throwable = e;
        }

        boolean b = url != null && url.equals(getTodayURL());

        if(p.throwable != null) {
            if (p.load(GGApp.GG_APP, "ggvp" + (b ? "td" : "tm"))) {
                final String message = p.throwable.getMessage();
                p.loadDate = "Keine Internetverbindung\n" + p.loadDate;
                p.throwable = null;
                if(toast)
                    GGApp.GG_APP.mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            GGApp.GG_APP.showToast(p.throwable instanceof UnknownHostException ? "Konnte gymglinde.de nicht auflösen" :
                                    p.throwable instanceof GGInvalidSourceException ? "Ungültige Antwort vom Server" : "Konnte keine Verbindung zu http://gymglinde.de aufbauen");
                        }
                    });
            }
        } else
            p.save(GGApp.GG_APP, "ggvp" + (b ? "td" : "tm"));
        return p;
    }

    @Override
    public String getTodayURL() {
        return gg.urlProps == null ? null : gg.urlProps.getProperty("ggurltd");
    }

    @Override
    public String getTomorrowURL() {
        return gg.urlProps == null ? null : gg.urlProps.getProperty("ggurltm");
    }

    @Override
    public String getDay(String date) {
        if(date.isEmpty() || date.length() < 20)
            return "(Bug)";
        return date.substring(0, date.length() - 17);
    }

    @Override
    public int getColor() {
        return GGApp.GG_APP.getResources().getColor(R.color.main_orange);
    }

    @Override
    public int getDarkColor() {
        return GGApp.GG_APP.getResources().getColor(R.color.main_orange_dark);
    }

    @Override
    public int getTheme() {
        return R.style.AppThemeOrange;
    }

    @Override
    public String getFullName() {
        return "Gynmasium Glinde";
    }

}
