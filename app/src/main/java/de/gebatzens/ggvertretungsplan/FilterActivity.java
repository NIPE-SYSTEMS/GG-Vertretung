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
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class FilterActivity extends Activity {

    Toolbar mToolBar;
    FilterListAdapter adapter;
    ListView listView;

    public String[] main_filterStrings;
    public String[] filterStrings;
    TextView mainFilterCategory;
    TextView mainFilterContent;
    ImageButton mAddFilterButton;

    @Override
    public void onCreate(Bundle bundle) {
        setTheme(GGApp.GG_APP.provider.getTheme());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            GGApp.GG_APP.setStatusBarColor(getWindow());
        }
        super.onCreate(bundle);
        setContentView(R.layout.activity_filter);

        main_filterStrings = new String[] { getApplication().getString(R.string.schoolclass), getApplication().getString(R.string.teacher)};

        filterStrings = new String[] { getApplication().getString(R.string.subject_course), getApplication().getString(R.string.schoolclass),
                                        getApplication().getString(R.string.teacher)};

        listView = (ListView) findViewById(R.id.filter_list);
        adapter = new FilterListAdapter(this, GGApp.GG_APP.filters);
        listView.setAdapter(adapter);
        listView.setDrawSelectorOnTop(true);
        setListViewHeightBasedOnChildren(listView);

        TextView tv = (TextView) findViewById(R.id.filter_sep_1);
        tv.setTextColor(GGApp.GG_APP.provider.getColor());
        TextView tv2 = (TextView) findViewById(R.id.filter_sep_2);
        tv2.setTextColor(GGApp.GG_APP.provider.getColor());

        FilterList list = GGApp.GG_APP.filters;
        mainFilterCategory = (TextView) findViewById(R.id.filter_main_category);
        mainFilterCategory.setText(list.mainFilter.type == FilterActivity.FilterType.CLASS ? getApplication().getString(R.string.schoolclass) : getApplication().getString(R.string.teacher));
        mainFilterContent = (TextView) findViewById(R.id.filter_main_content);
        mainFilterContent.setText(list.mainFilter.filter);

        LinearLayout l = (LinearLayout) findViewById(R.id.mainfilter_layout);
        l.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FilterActivity.this);
                builder.setTitle(getApplication().getString(R.string.set_main_filter));
                builder.setView(getLayoutInflater().inflate(R.layout.filter_dialog, null));
                builder.setPositiveButton(getApplication().getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Spinner spinner = (Spinner) ((Dialog) dialog).findViewById(R.id.filter_spinner);
                        EditText text = (EditText) ((Dialog) dialog).findViewById(R.id.filter_text);
                        String text2 = text.getText().toString().trim();
                        if (text2.isEmpty())
                            Toast.makeText(((Dialog) dialog).getContext(), getApplication().getString(R.string.invalid_filter), Toast.LENGTH_SHORT).show();
                        else {
                            FilterList list = GGApp.GG_APP.filters;
                            list.mainFilter.filter = text2;
                            mainFilterContent.setText(list.mainFilter.filter);
                            list.mainFilter.type = FilterType.values()[spinner.getSelectedItemPosition()];
                            mainFilterCategory.setText(list.mainFilter.type == FilterActivity.FilterType.CLASS ? getApplication().getString(R.string.schoolclass) : getApplication().getString(R.string.teacher));
                            FilterActivity.saveFilter(GGApp.GG_APP.filters);
                        }
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(getApplication().getString(R.string.abort), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });//
                AlertDialog d = builder.create();
                d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                d.show();
                Spinner s = (Spinner) d.findViewById(R.id.filter_spinner);
                ArrayAdapter<String> a = new ArrayAdapter<String>(FilterActivity.this,
                        android.R.layout.simple_spinner_item, main_filterStrings);
                a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                s.setAdapter(a);
                FilterList list = GGApp.GG_APP.filters;
                s.setSelection(list.mainFilter.type == FilterType.CLASS ? 0 : 1);
                EditText mainEdit = (EditText) d.findViewById(R.id.filter_text);
                mainEdit.setText(list.mainFilter.filter);
            }
        });

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

        mAddFilterButton = (ImageButton) findViewById(R.id.addfilter_button);
        mAddFilterButton.setBackgroundResource(R.drawable.floating_action_circle);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            StateListDrawable drawable = (StateListDrawable) mAddFilterButton.getBackground();
            drawable.setColorFilter(GGApp.GG_APP.provider.getColor(), PorterDuff.Mode.SRC_ATOP);
        }
        mAddFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                AlertDialog.Builder builder = new AlertDialog.Builder(FilterActivity.this);
                builder.setTitle(getApplication().getString(R.string.add_filter));
                builder.setView(getLayoutInflater().inflate(R.layout.filter_dialog, null));
                builder.setPositiveButton(getApplication().getString(R.string.add), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Spinner spinner = (Spinner) ((Dialog) dialog).findViewById(R.id.filter_spinner);
                        EditText text = (EditText) ((Dialog) dialog).findViewById(R.id.filter_text);
                        Filter f = new Filter();
                        f.type = Filter.getTypeFromString((String) spinner.getSelectedItem());
                        f.filter = text.getText().toString().trim();
                        if (f.filter.isEmpty())
                            Toast.makeText(((Dialog) dialog).getContext(), getApplication().getString(R.string.invalid_filter), Toast.LENGTH_SHORT).show();
                        else {
                            GGApp.GG_APP.filters.add(f);
                            adapter.notifyDataSetChanged();
                            FilterActivity.saveFilter(GGApp.GG_APP.filters);
                            setListViewHeightBasedOnChildren(listView);
                        }
                        dialog.dismiss();
                    }
                });
                builder.setNegativeButton(getApplication().getString(R.string.abort), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });//
                AlertDialog d = builder.create();
                d.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                d.show();
                Spinner s = (Spinner) d.findViewById(R.id.filter_spinner);
                ArrayAdapter<String> a = new ArrayAdapter<String>(FilterActivity.this,
                        android.R.layout.simple_spinner_item, filterStrings);
                a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                s.setAdapter(a);
                s.setSelection(0);
            }
        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }

    public static class FilterList extends ArrayList<Filter> {
        public Filter mainFilter;
    }

    public static class Filter {
        FilterType type;
        String filter;

        public boolean matches(GGPlan.Entry e) {
            if(filter.isEmpty())
                return false;
            switch(type) {
                case CLASS:
                    return e.clazz.toLowerCase().equals(filter.toLowerCase());
                case TEACHER:
                    return e.subst.toLowerCase().equals(filter.toLowerCase());
                case SUBJECT:
                    return e.subject.toLowerCase().replace(" ", "").equals(filter.toLowerCase().replace(" ", ""));
            }
            return false;
        }

        public static String getTypeString(FilterType type) {
            String s;
            switch(type) {
                case CLASS:
                    s = GGApp.GG_APP.getString(R.string.schoolclass);
                    break;
                case TEACHER:
                    s = GGApp.GG_APP.getString(R.string.teacher);
                    break;
                case SUBJECT:
                    s = GGApp.GG_APP.getString(R.string.subject_course);
                    break;
                default:
                    s = "";
            }
            return s;
        }

        public static FilterType getTypeFromString(String s) {
            if(s.equals(GGApp.GG_APP.getString(R.string.teacher)))
                return FilterType.TEACHER;
            else if(s.equals(GGApp.GG_APP.getString(R.string.schoolclass)))
                return FilterType.CLASS;
            else if(s.equals(GGApp.GG_APP.getString(R.string.subject_course)))
                return FilterType.SUBJECT;
            else
                return null;
        }

        @Override
        public String toString() {
            return getTypeString(type) + " " + filter;
        }
    }

    public static enum FilterType {
        CLASS, TEACHER, SUBJECT
    }

    public static FilterList loadFilter() {
        FilterList list = new FilterList();
        list.mainFilter = null;

        try {
            InputStream in = GGApp.GG_APP.openFileInput("ggfilter");
            JsonReader reader = new JsonReader(new InputStreamReader(in));
            reader.beginArray();
            while(reader.hasNext()) {
                reader.beginObject();
                Filter f = new Filter();
                if(list.mainFilter == null)
                    list.mainFilter = f;
                else
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

        if(list.mainFilter == null) {
            Filter f = new Filter();
            list.mainFilter = f;
            f.type = FilterType.CLASS;
            f.filter = "";
        }

        return list;
    }

    @Override
    public void finish() {
        setResult(RESULT_OK);
        saveFilter(GGApp.GG_APP.filters);
        super.finish();
    }

    public static void saveFilter(FilterList list) {
        try {
            OutputStream out = GGApp.GG_APP.openFileOutput("ggfilter", Context.MODE_PRIVATE);
            JsonWriter writer = new JsonWriter(new OutputStreamWriter(out));
            writer.setIndent("  ");
            writer.beginArray();
            writer.beginObject();
            writer.name("type").value(list.mainFilter.type.toString());
            writer.name("filter").value(list.mainFilter.filter);
            writer.endObject();
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

    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ViewGroup.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}
