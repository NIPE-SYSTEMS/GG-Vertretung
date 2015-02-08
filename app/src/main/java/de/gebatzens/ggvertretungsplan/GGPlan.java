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

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class GGPlan {

    public ArrayList<String[]> entries = new ArrayList<String[]>();
    public String date = "";
    public String special = "";
    public Throwable throwable = null;
    public String loadDate = "";

    public GGPlan() {

    }

    public boolean load(Context c, String file) {
        Log.w("ggvp", "Lade " + file);
        entries.clear();
        try {
            InputStream in = c.openFileInput(file);
            Scanner scan = new Scanner(new BufferedInputStream(in));

            loadDate = scan.nextLine();
            date = scan.nextLine();
            special = scan.nextLine();

            while(scan.hasNextLine()) {
                String[] sr = new String[5];

                for(int i = 0; i < 5; i++)
                    sr[i] = scan.nextLine();

                entries.add(sr);
            }

            scan.close();

            if(date == null || date.isEmpty())
                return false;

        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void save(Context c, String file) {
        Log.w("ggvp", "Speichere " + file);
        try {
            OutputStream out = c.openFileOutput(file, Context.MODE_PRIVATE);
            PrintStream wr = new PrintStream(out);
            wr.println(loadDate);
            wr.println(date);
            wr.println(special);
            for(String[] s : entries) {
                for(int i = 0; i < 5; i++)
                    wr.println(s[i]);
            }
            wr.close();
        } catch(Exception e) {
            e.printStackTrace();

        }
    }

    public List<String[]> getAllForClass(String c) {
        c = c.toLowerCase();
        c = c.replaceAll(" ", "");
        ArrayList<String[]> list = new ArrayList<String[]>();
        for(String[] ss : entries) {
            String sc = ss[0].toLowerCase().replaceAll(" ", "");
            if (sc.equals(c))
                list.add(ss);
        }
        return list;
    }

}
