/*
 * Copyright (C) 2015 Fabian Schultis, Hauke Oldsen
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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class NewsFragment extends RemoteDataFragment {

    ListView lv;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle bundle) {
        ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.fragment_news, group, false);
        if(GGApp.GG_APP.news != null)
            createView(inflater, vg);
        return vg;
    }

    @Override
    public void onViewCreated(View v, Bundle b) {
        super.onViewCreated(v, b);

        /*final SwipeRefreshLayout swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.news_refresh);
        // Setup refresh listener which triggers new data loading
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                GGApp.GG_APP.refreshAsync(new Runnable() {
                    @Override
                    public void run() {
                        swipeContainer.post(new Runnable() {
                            @Override
                            public void run() {
                                swipeContainer.setRefreshing(false);
                            }
                        });

                    }
                }, true, GGApp.FragmentType.PLAN);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.custom_material_green,
                R.color.custom_material_red,
                R.color.custom_material_blue,
                R.color.custom_material_orange);
        */

        if(GGApp.GG_APP.plans == null) {
            ((ViewGroup) getView().findViewById(R.id.news_content)).addView(createLoadingView());
        }
    }

    private void createView(LayoutInflater inflater, ViewGroup view) {
        lv = new ListView(getActivity());
        lv.setDrawSelectorOnTop(true);
        ((LinearLayout) view.findViewById(R.id.news_content)).addView(lv);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView txtTitle = (TextView) view.findViewById(R.id.newsTitle);
                TextView txtContent =  (TextView) view.findViewById(R.id.newsContent);
                String mTitle = txtTitle.getText().toString();
                String mContent = txtContent.getText().toString();
                AlertDialog.Builder ad = new AlertDialog.Builder(view.getContext());
                ad.setTitle(mTitle);
                ad.setMessage(mContent);
                ad.setNegativeButton("Schließen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                ad.show();
            }
        });
        NewsFragmentListAdapter nfla = new NewsFragmentListAdapter(getActivity(), GGApp.GG_APP.news);
        lv.setAdapter(nfla);
    }

    private View createLoadingView() {
        LinearLayout l = new LinearLayout(getActivity());
        l.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        l.setGravity(Gravity.CENTER);

        ProgressBar pb = new ProgressBar(getActivity());
        pb.getIndeterminateDrawable().setColorFilter(GGApp.GG_APP.provider.getColor(), PorterDuff.Mode.SRC_IN);
        pb.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        pb.setVisibility(ProgressBar.VISIBLE);

        l.addView(pb);
        return l;
    }

    @Override
    public void setFragmentLoading() {
        if(getView() == null)
            return;

        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.news_content);
        if(vg == null)
            return;
        vg.removeAllViews();

        vg.addView(createLoadingView());
    }

    @Override
    public void updateFragment() {
        if(getView() == null)
            return;

        ViewGroup vg = (ViewGroup) getView().findViewById(R.id.news_content);
        if(vg == null)
            return;
        vg.removeAllViews();

        createView(getActivity().getLayoutInflater(), vg);
    }

    public static class News extends ArrayList<String[]> {

        Throwable throwable;

        public void save(String file) {
            try {
                OutputStream out = GGApp.GG_APP.openFileOutput(file, Context.MODE_PRIVATE);
                JsonWriter writer = new JsonWriter(new OutputStreamWriter(out));

                writer.setIndent("  ");
                writer.beginArray();
                for(String[] s : this) {
                    writer.beginObject();

                    writer.name("id").value(s[0]);
                    writer.name("date").value(s[1]);
                    writer.name("topic").value(s[2]);
                    writer.name("source").value(s[3]);
                    writer.name("title").value(s[4]);
                    writer.name("text").value(s[5]);

                    writer.endObject();
                }
                writer.endArray();
                writer.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
        }

        public boolean load(String file) {
            clear();
            try {
                InputStream in = GGApp.GG_APP.openFileInput(file);
                JsonReader reader = new JsonReader(new InputStreamReader(in));
                reader.beginArray();
                while(reader.hasNext()) {
                    reader.beginObject();
                    String[] s = new String[6];

                    while(reader.hasNext()) {
                        String name = reader.nextName();
                        if(name.equals("id"))
                            s[0] = reader.nextString();
                        else if(name.equals("date"))
                            s[1] = reader.nextString();
                        else if(name.equals("topic"))
                            s[2] = reader.nextString();
                        else if(name.equals("source"))
                            s[3] = reader.nextString();
                        else if(name.equals("title"))
                            s[4] = reader.nextString();
                        else if(name.equals("text"))
                            s[5] = reader.nextString();
                        else
                            reader.skipValue();
                    }
                    reader.endObject();
                    add(s);
                }
                reader.endArray();
                reader.close();
            } catch(Exception e) {
                e.printStackTrace();
                return false;
            }

            return true;
        }
    }
}
