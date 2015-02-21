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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class FilterActivity extends Activity {

    Toolbar mToolBar;
    FilterListAdapter adapter;
    ListView listView;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_filter);

        listView = (ListView) findViewById(R.id.filter_list);
        adapter = new FilterListAdapter(this, GGApp.GG_APP.filters);
        listView.setAdapter(adapter);

        setTheme(GGApp.GG_APP.provider.getTheme());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            GGApp.GG_APP.setStatusBarColor(getWindow());
        }

        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mToolBar.setBackgroundColor(GGApp.GG_APP.provider.getColor());
        mToolBar.setTitleTextColor(Color.WHITE);
        mToolBar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mToolBar.setTitle(getTitle());

        mToolBar.inflateMenu(R.menu.filter_menu);
        mToolBar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FilterActivity.this);
                builder.setTitle("Filter Hinzufügen");
                builder.setView(getLayoutInflater().inflate(R.layout.filter_dialog, null));
                builder.setPositiveButton("Hinzufügen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Spinner spinner = (Spinner) ((Dialog) dialog).findViewById(R.id.filter_spinner);
                        EditText text = (EditText) ((Dialog) dialog).findViewById(R.id.filter_text);
                        Filter f = new Filter();
                        f.type = Filter.getTypeFromString((String) spinner.getSelectedItem());
                        f.filter = text.getText().toString().trim();
                        if(f.filter.isEmpty())
                            Toast.makeText(((Dialog) dialog).getContext(), "Ungültige Eingabe", Toast.LENGTH_SHORT).show();
                        else {
                            GGApp.GG_APP.filters.add(f);
                            saveFilter(GGApp.GG_APP.filters);
                            adapter.notifyDataSetChanged();
                        }
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });//
                AlertDialog d = builder.create();
                d.show();
                Spinner s = (Spinner) d.findViewById(R.id.filter_spinner);
                ArrayAdapter<String> a = new ArrayAdapter<String>(FilterActivity.this,
                        android.R.layout.simple_spinner_item, new String[] { "Klasse", "Lehrer" });
                a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                s.setAdapter(a);
                s.setSelection(0);
                return false;
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public static class Filter {
        FilterType type;
        String filter;

        public static String getTypeString(FilterType type) {
            String s;
            switch(type) {
                case CLASS:
                    s = "Klasse";
                    break;
                case TEACHER:
                    s = "Lehrer";
                    break;
                default:
                    s = "";
            }
            return s;
        }

        public static FilterType getTypeFromString(String s) {
            if(s.equals("Lehrer"))
                return FilterType.TEACHER;
            else if(s.equals("Klasse"))
                return FilterType.CLASS;
            else
                return null;
        }

        @Override
        public String toString() {
            return getTypeString(type) + " " + filter;
        }
    }

    public static enum FilterType {
        CLASS, TEACHER
    }

    public static ArrayList<Filter> loadFilter() {
        ArrayList<Filter> list = new ArrayList<Filter>();

        try {
            InputStream in = GGApp.GG_APP.openFileInput("ggfilter");
            JsonReader reader = new JsonReader(new InputStreamReader(in));
            reader.beginArray();
            while(reader.hasNext()) {
                reader.beginObject();
                Filter f = new Filter();
                list.add(f);
                while(reader.hasNext()) {
                    String name = reader.nextName();
                    if(name.equals("type"))
                        f.type = FilterType.valueOf(reader.nextString());
                    else if(name.equals("filter"))
                        f.filter = reader.nextString();
                    else
                        reader.skipValue();

                }
                reader.endObject();
            }
            reader.endArray();
            reader.close();
        } catch(Exception e) {
            list.clear();
        }

        return list;
    }

    public static void saveFilter(ArrayList<Filter> list) {
        try {
            OutputStream out = GGApp.GG_APP.openFileOutput("ggfilter", Context.MODE_PRIVATE);
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(out));
            writer.setIndent("  ");
            writer.beginArray();
            for(Filter f : list) {
                writer.beginObject();
                writer.name("type").value(f.type.toString());
                writer.name("filter").value(f.filter);
                writer.endObject();
            }
            writer.endArray();
            writer.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
