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
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

public class NewsFragment extends RemoteDataFragment {

    private ListView lv;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_news, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        lv = (ListView) view.findViewById(R.id.news_listview);
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
                ad.setNeutralButton("Schließen", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                ad.show();
            }
        });
        updateNews();
    }

    private void updateNews() {

        new AsyncTask<Object, Void, Void>() {

            @Override
            protected Void doInBackground(Object... params) {
                News n = GGApp.GG_APP.provider.getNews();
                updateUI(n);

                return null;
            }

        }.execute();
    }

    private void updateUI(final News mNewsList) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                NewsFragmentListAdapter nfla = new NewsFragmentListAdapter(getActivity(),mNewsList);
                lv.setAdapter(nfla);
            }
        });
    }

    @Override
    public void setFragmentLoading() {

    }

    @Override
    public void updateFragment() {

    }

    public static class News extends ArrayList<String[]> {

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
