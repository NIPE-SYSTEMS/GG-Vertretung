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
import android.widget.ArrayAdapter;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GGPlan {

    //TODO java.util.Date benutzen
    public ArrayList<Entry> entries = new ArrayList<Entry>();
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
                        Entry e = new Entry();
                        while(reader.hasNext()) {
                            String name2 = reader.nextName();
                            if(name2.equals("class"))
                                e.clazz = reader.nextString();
                            else if(name2.equals("hour"))
                                e.hour = reader.nextString();
                            else if(name2.equals("subst"))
                                e.subst = reader.nextString();
                            else if(name2.equals("subject"))
                                e.subject = reader.nextString();
                            else if(name2.equals("comment"))
                                e.comment = reader.nextString();
                            else if(name2.equals("type"))
                                e.type = reader.nextString();
                            else if(name2.equals("room"))
                                e.type = reader.nextString();
                            else
                                reader.skipValue();

                        }
                        if(!e.isValid()) {
                            reader.close();
                            return false;
                        }
                        entries.add(e);
                        reader.endObject();
                    }
                    reader.endArray();
                } else
                    reader.skipValue();
            }
            reader.endObject();
            reader.close();

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
            for(Entry e : entries) {
                writer.beginObject();
                writer.name("class").value(e.clazz);
                writer.name("hour").value(e.hour);
                writer.name("subst").value(e.subst);
                writer.name("subject").value(e.subject);
                writer.name("comment").value(e.comment);
                writer.name("type").value(e.type);
                writer.name("room").value(e.room);
                writer.endObject();
            }
            writer.endArray();
            writer.endObject();
            writer.close();
        } catch(Exception e) {
            e.printStackTrace();

        }
    }

    public List<String> getAllClasses() {
        ArrayList<String> list = new ArrayList<String>();

        String last = "";
        for(Entry e : entries) {
            if(!last.equals(e.clazz)) {
                list.add(e.clazz);
                last = e.clazz;
            }
        }
        return list;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof GGPlan) {
            GGPlan plan = (GGPlan) o;
            return plan.entries.equals(entries) && plan.date.equals(date) && plan.special.equals(special);
        } else
            return false;
    }

    public List<Entry> getAllForClass(String c) {
        c = c.toLowerCase();
        c = c.replaceAll(" ", "");
        ArrayList<Entry> list = new ArrayList<Entry>();
        for(Entry e : entries) {
            String sc = e.clazz.toLowerCase().replaceAll(" ", "");
            if (sc.equals(c))
                list.add(e);
        }
        return list;
    }

    public static class Entry {
        String type;
        String clazz;
        //String missing;
        String subst;
        String subject;
        //Neues Fach
        String repsub;
        String comment;
        String hour;
        String room;

        public boolean isValid() {
            return type != null && clazz != null && subject != null
                    && subst != null && comment != null && room != null && repsub != null;
        }

        @Override
        public boolean equals(Object o) {
            if(o instanceof Entry) {
                Entry e = (Entry) o;
                return e.type.equals(type) && e.clazz.equals(clazz) && e.subject.equals(subject)
                        && e.subst.equals(subst) && e.comment.equals(comment)
                        && e.hour.equals(hour) && e.room.equals(room) && e.repsub.equals(repsub);
            } else
                return false;
        }

        /**
         * Comment hat Inhalt, type noch nicht
         *
         */
        public void unify() {
            Pattern aufg = Pattern.compile("Aufg. durch (\\w+)");
            String commentl = comment.toLowerCase();

            Matcher aufgm = aufg.matcher(comment);

            if(commentl.contains("eigenverantwortlich") || commentl.contains("eva")) {
                type = "EVA";

                if(aufgm.find()) {
                    comment = "Aufgabe durch " + aufgm.group(1);
                } else
                    comment = "";

            } else if(commentl.contains("siehe")) {
                type = "Entfall / Verlegung";

                Matcher m = Pattern.compile("[Ss]iehe (.*)").matcher(comment);
                if (m.find())
                    if (m.group(1).contains("heute")) {
                        comment = "Verlegt in " + m.group(1).replaceAll("heute,? ", "");
                    } else {
                        comment = "Verlegt nach " + m.group(1);
                    }
            } else if(commentl.contains("f.a.") || commentl.contains("fällt aus") || commentl.contains("entfall")) {
                type = "Entfall";

                if(aufgm.find())
                    comment = "Aufgabe durch " + aufgm.group(1);
                else
                    comment = "";
            } else if(commentl.contains("klausur")) {
                type = "Klausur";
                comment = "";
            } else if(commentl.contains("unterricht findet statt") || commentl.contains("absenz")) {
                type = "Unterricht";
            } else if(commentl.contains("statt")) {
                type = "Vertretung / Verlegung";

                Matcher m1 = Pattern.compile("[Ss]tatt (.*?)Std.").matcher(comment);
                Matcher m2 = Pattern.compile(":(.*)").matcher(comment);

                if(m1.find())
                    comment = "Statt " + m1.group(1).replaceAll("\\w+ \\(heute\\)", "").replaceAll("heute,? ", "") + " Stunde";

                if(m2.find())
                    repsub = m2.group(1).trim();
            } else if(commentl.contains("betreuung")) {
                type = "Betreuung";
                comment = "";
            } else {
                type = "Vertretung";

                if(aufgm.find())
                    comment = "Aufgabe durch " + aufgm.group(1);

                if(commentl.contains("mittag"))
                    comment = "Mittagspause";
            }

            if(subject.isEmpty() && !repsub.isEmpty())
                subject = "&#x2192; " + repsub;
            else if(!subject.isEmpty() && !repsub.isEmpty() && !subject.equals(repsub))
                subject += " &#x2192; " + repsub;



        }
    }

}
