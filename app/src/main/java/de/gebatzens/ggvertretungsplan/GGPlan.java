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
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class GGPlan {

    public ArrayList<String[]> entries = new ArrayList<String[]>();
    public String date = "";
    public List<String> special = new ArrayList<String>();
    public Throwable throwable = null;
    public String loadDate = "";

    public GGPlan() {

    }

    public boolean load(Context c, String file) {
        Log.w("ggvp", "Lade " + file);
        entries.clear();
        special.clear();
        date = "";
        loadDate = "";

        try {
            InputStream in = c.openFileInput(file);
            JsonReader reader = new JsonReader(new InputStreamReader(in));
            reader.beginObject();
            while(reader.hasNext()) {
                String name = reader.nextName();
                if(name.equals("loadDate"))
                    loadDate = reader.nextString();
                else if(name.equals("date"))
                    date = reader.nextString();
                else if(name.equals("messages")) {
                    reader.beginArray();
                    while(reader.hasNext()) {
                        special.add(reader.nextString());
                    }
                    reader.endArray();
                } else if(name.equals("entries")) {
                    reader.beginArray();
                    while(reader.hasNext()) {
                        reader.beginObject();
                        String[] d = new String[5];
                        while(reader.hasNext()) {
                            String name2 = reader.nextName();
                            if(name2.equals("klasse"))
                                d[0] = reader.nextString();
                            else if(name2.equals("stunde"))
                                d[1] = reader.nextString();
                            else if(name2.equals("vertr"))
                                d[2] = reader.nextString();
                            else if(name2.equals("fach"))
                                d[3] = reader.nextString();
                            else if(name2.equals("bemerk"))
                                d[4] = reader.nextString();
                            else
                                reader.skipValue();

                        }
                        entries.add(d);
                        reader.endObject();
                    }
                    reader.endArray();
                } else
                    reader.skipValue();
            }
            reader.endObject();

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
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(out, "UTF-8"));

            writer.setIndent("  ");
            writer.beginObject();
            writer.name("loadDate").value(loadDate);
            writer.name("date").value(date);
            writer.name("messages");
            writer.beginArray();
            for(String s : special)
                writer.value(s);
            writer.endArray();

            writer.name("entries");
            writer.beginArray();
            for(String[] ss : entries){
                writer.beginObject();
                writer.name("klasse").value(ss[0]);
                writer.name("stunde").value(ss[1]);
                writer.name("vertr").value(ss[2]);
                writer.name("fach").value(ss[3]);
                writer.name("bemerk").value(ss[4]);
                writer.endObject();
            }
            writer.endArray();
            writer.endObject();
            writer.close();
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
