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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SWSProvider implements VPProvider {

    String mTDUrl, mTMUrl;
    GGApp ggapp;

    public SWSProvider(GGApp gg) {
        ggapp = gg;
        mTDUrl = gg.urlProps == null ? null : gg.urlProps.getProperty("swsurltd");
        mTMUrl = gg.urlProps == null ? null : gg.urlProps.getProperty("swsurltm");
    }

    @Override
    public GGPlan getVPSync(String url) {
        GGPlan plan = new GGPlan();

        try {
            if(url == null || url.isEmpty())
                throw new VPUrlFileException();
            URLConnection con = new URL(url).openConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream(), "ISO-8859-1"));

            Pattern date = Pattern.compile("<div class=\"mon_title\">(.*)</div>");
            Pattern tables = Pattern.compile("<table class=\"mon_list\">");
            Pattern tdata = Pattern.compile("<td .*?>(.*?)</td>");
            int h = 0;
            String lastClass = "Bug";
            int ln = 0;

            String line;
            while ((line = GGProvider.decode(reader.readLine())) != null) {
                ln++;

                if(plan.date.isEmpty()) {
                    Matcher md = date.matcher(line);
                    if (md.find())
                        plan.date = md.group(1).trim();
                } else { //find table start
                    Matcher mt = tables.matcher(line);
                    if(mt.find()) {
                        reader.readLine(); //ignore header
                        ln++;
                        String tline;
                        String current = null;
                        while(!(tline = GGProvider.decode(reader.readLine())).equals("</table>")) {
                            ln++;
                            if(tline.contains("colspan=\"6\"")) {
                                Matcher m = tdata.matcher(tline);
                                if(m.find()) {
                                    current = m.group(1).trim();
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

                                String[] rdata = new String[5];
                                rdata[0] = current;
                                rdata[1] = data[0]; //Stunde
                                rdata[2] = data[2]; //Vertr.
                                rdata[3] = data[1]; //Fach
                                rdata[4] = data[5];
                                if(!data[3].equals(""))
                                    rdata[4] += "; Raum " + data[3];
                                if(!data[4].equals(""))
                                    rdata[4] += "; " + data[4];

                                plan.entries.add(rdata);
                            }
                        }
                    }
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
            plan.throwable = e;
        }


        return plan;
    }

    @Override
    public String getDay(String s) {
        if(s.isEmpty())
            return "Bug";
        String[] strs = s.split(" ");
        if(strs.length < 2)
            return "Bug";
        return strs[1];
    }

    @Override
    public String getTodayURL() {
        return mTDUrl;
    }

    @Override
    public String getTomorrowURL() {
        return mTMUrl;
    }
}
