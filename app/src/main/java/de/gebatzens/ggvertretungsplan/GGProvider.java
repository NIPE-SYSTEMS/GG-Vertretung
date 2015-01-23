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

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GGProvider implements VPProvider {

    public static void load(GGPlan target, String s) {
        try {
            URLConnection con = new URL(s).openConnection();
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
                    target.date = m.group(1).substring(21).replaceAll("den", "der");
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

                            target.entries.add(values);
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
                            target.special = spv;
                        }
                    }
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
            target.throwable = e;
        }
    }

    public static String decode(String html) {
        if(html == null)
            return null;

        html = html.replaceAll("&uuml;", "ü");
        html = html.replaceAll("&auml;", "ä");
        html = html.replaceAll("&ouml;", "ö");

        html = html.replaceAll("&Uuml;", "Ü");
        html = html.replaceAll("&Auml;", "Ä");
        html = html.replaceAll("&Ouml;", "Ö");

        html = html.replaceAll("&nbsp;", "");
        html = html.replaceAll("---", ""); //SWS '---'

        return html;
    }


    @Override
    public GGPlan getVPSync(String url) {
        GGPlan p = new GGPlan();
        load(p, url);
        return p;
    }

    @Override
    public String getTodayURL() {
        return "http://gymglinde.de/typo40/fileadmin/vertretungsplan/VertretungAktuell/PH_heute.htm";
    }

    @Override
    public String getTomorrowURL() {
        return "http://gymglinde.de/typo40/fileadmin/vertretungsplan/VertretungAktuell/PH_morgen.htm";
    }

    @Override
    public String getDay(String date) {
        if(date.isEmpty() || date.length() < 20)
            return "(Bug)";
        return date.substring(0, date.length() - 17);
    }

}
