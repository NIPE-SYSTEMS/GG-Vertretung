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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.Spanned;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import javax.net.ssl.HttpsURLConnection;

public class MensaFragment extends RemoteDataFragment {

    SwipeRefreshLayout swipeContainer;
    int cardColorIndex = 0;
    String cache_file_prefix = "cache_mensa_";

    public MensaFragment() {
        type = GGApp.FragmentType.MENSA;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle bundle) {
        ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.fragment_mensa, group, false);
        if(GGApp.GG_APP.mensa != null)
            createView(inflater, vg);
        return vg;
    }

    @Override
    public void onViewCreated(View v, Bundle b) {
        super.onViewCreated(v, b);

        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.mensa_refresh);
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
                }, true, GGApp.FragmentType.MENSA);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.custom_material_green,
                R.color.custom_material_red,
                R.color.custom_material_blue,
                R.color.custom_material_orange);

    }

    @Override
    public void createView(LayoutInflater inflater, ViewGroup view) {
        /*lv = new ListView(getActivity());
        lv.setDrawSelectorOnTop(true);
        lv.setDivider(getResources().getDrawable(R.drawable.listview_divider));
        ((LinearLayout) view.findViewById(R.id.mensa_content)).addView(lv);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TextView txtTitle = (TextView) view.findViewById(R.id.mensaTitle);
                String mTitle = txtTitle.getText().toString();
                String vegi = (Integer.valueOf(mfla.getMensaMeal(position)[4]) == 1) ? getResources().getString(R.string.yes) : getResources().getString(R.string.no);
                Spanned mContent = Html.fromHtml("<b>" + getResources().getString(R.string.vegi) + ":</b> " + vegi +  "<br /><b>" + getResources().getString(R.string.garnish) + ":</b> " + mfla.getMensaMeal(position)[3].replace("mit ","").replace("mit",""));
                AlertDialog.Builder ad = new AlertDialog.Builder(view.getContext());
                ad.setTitle(mTitle);
                ad.setMessage(mContent);
                ad.setNegativeButton(GGApp.GG_APP.getResources().getString(R.string.close), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                ad.show();
            }
        });
        mfla = new MensaFragmentListAdapter(getActivity(), GGApp.GG_APP.mensa);
        lv.setAdapter(mfla);*/
        for(int i = 0; i < GGApp.GG_APP.mensa.size(); i++) {
            MensaItem mi = new MensaItem();
            mi.id = GGApp.GG_APP.mensa.get(i)[0];
            mi.date = GGApp.GG_APP.mensa.get(i)[1];
            mi.garnish = GGApp.GG_APP.mensa.get(i)[3];
            mi.meal = GGApp.GG_APP.mensa.get(i)[2];
            mi.vegi = GGApp.GG_APP.mensa.get(i)[4];
            mi.image = GGApp.GG_APP.mensa.get(i)[5];
            ((LinearLayout) view.findViewById(R.id.mensa_content)).addView(createCardItem(mi,inflater));
        }
        cardColorIndex = 0;
    }

    @Override
    public ViewGroup getContentView() {
        return (ViewGroup) getView().findViewById(R.id.mensa_content);
    }

    private CardView createCardItem(MensaItem mensa_item, LayoutInflater i) {
        CardView mcv = createCardView();
        i.inflate(R.layout.mensa_cardview_entry, mcv, true);
        String[] colors = getActivity().getResources().getStringArray(GGApp.GG_APP.provider.getColorArray());
        //((ImageView) mcv.findViewById(R.id.mcv_image)).setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.block_house_steak));
        /*((LinearLayout) mcv.findViewById(R.id.mcv_header_outer)).setBackgroundColor(Color.parseColor(colors[cardColorIndex]));
        cardColorIndex++;
        if(cardColorIndex == colors.length)
            cardColorIndex = 0;*/
        ((TextView) mcv.findViewById(R.id.mcv_date)).setText(getFormatedDate(mensa_item.date));
        ((TextView) mcv.findViewById(R.id.mcv_meal)).setText(mensa_item.meal);
        ((TextView) mcv.findViewById(R.id.mcv_garnish)).setText(getResources().getString(R.string.garnish) + ": " + mensa_item.garnish.replace("mit ","").replace("mit",""));
        ((TextView) mcv.findViewById(R.id.mcv_day)).setText(getDayByDate(mensa_item.date));
        ((ImageView) mcv.findViewById(R.id.mcv_imgvegi)).setImageBitmap((Integer.valueOf(mensa_item.vegi) == 1) ? BitmapFactory.decodeResource(getResources(), R.drawable.vegi) : BitmapFactory.decodeResource(getResources(), R.drawable.meat));
        ViewHolder vh = new ViewHolder();
        vh.imgview = (ImageView) mcv.findViewById(R.id.mcv_image);
        vh.filename = mensa_item.image;
        new AsyncTask<ViewHolder, Void, ViewHolder>() {

            @Override
            protected ViewHolder doInBackground(ViewHolder... params) {
                //params[0].bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.block_house_steak);
                try {
                    Bitmap bitmap;
                    if(cacheCheckDir()) {
                        bitmap = cacheGetBitmap(params[0].filename);
                        if(bitmap!=null) {
                            params[0].bitmap = bitmap;
                        } else {
                            bitmap = GGApp.GG_APP.provider.getMensaImage(params[0].filename);
                            cacheSetBitmap(params[0].filename, bitmap);
                            params[0].bitmap = bitmap;
                        }
                    } else {
                        bitmap = GGApp.GG_APP.provider.getMensaImage(params[0].filename);
                        cacheSetBitmap(params[0].filename, bitmap);
                        params[0].bitmap = bitmap;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    params[0].bitmap = null;
                }

                return params[0];
            }

            @Override
            protected void onPostExecute(ViewHolder result) {
                try {
                    ImageView imgView = (ImageView) result.imgview;
                    imgView.setImageBitmap((Bitmap) result.bitmap);
                    imgView.setScaleType(ImageView.ScaleType.FIT_XY);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.execute(vh);
        return mcv;
    }

    private String getDayByDate(String date) {
        String formattedDate;
        DateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat dateFormatter = new SimpleDateFormat("EEE");
        try
        {
            Date parsedDate = parser.parse(date);
            formattedDate = dateFormatter.format(parsedDate);
            return formattedDate;
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            return "";
        }
    }

    private String getFormatedDate(String date) {
        String formattedDate;
        DateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat dateFormatter;
        if(Locale.getDefault().getLanguage().equals("de")) {
            dateFormatter = new SimpleDateFormat("d. MMM");
        } else if(Locale.getDefault().getLanguage().equals("en")) {
            dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
        } else {
            dateFormatter = new SimpleDateFormat("yyyy-MM-dd");
        }
        try
        {
            Date parsedDate = parser.parse(date);
            formattedDate = dateFormatter.format(parsedDate);
            return formattedDate;
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            return "";
        }
    }

    private boolean cacheCheckDir() {
        File schulinfoapp_dir = new File(Environment.getExternalStorageDirectory() + "/SchulinfoAPP");
        if(schulinfoapp_dir.isDirectory()) {
            return true;
        } else {
            schulinfoapp_dir.mkdirs();
            if(schulinfoapp_dir.isDirectory()) {
                return true;
            } else {
                return false;
            }
        }
    }

    public Bitmap cacheGetBitmap(String filename) {
        File bitmap_file = new File(Environment.getExternalStorageDirectory() + "/SchulinfoAPP/" + cache_file_prefix + filename);
        if(bitmap_file.exists()) {
            return BitmapFactory.decodeFile(Environment.getExternalStorageDirectory() + "/SchulinfoAPP/" + cache_file_prefix + filename);
        } else {
            return null;
        }
    }

    private void cacheSetBitmap(String filename, Bitmap image) {
        File bitmap_file = new File(Environment.getExternalStorageDirectory() + "/SchulinfoAPP/" + cache_file_prefix + filename);
        try {
            if(!bitmap_file.exists()) {
                bitmap_file.createNewFile();
            }
            FileOutputStream fos = new FileOutputStream(bitmap_file);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ViewHolder {
        ImageView imgview;
        Bitmap bitmap;
        String filename;
    }

    public class MensaItem {
        String id;
        String date;
        String meal;
        String garnish;
        String vegi;
        String image;
    }

    public static class Mensa extends ArrayList<String[]> {

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
                    writer.name("meal").value(s[2]);
                    writer.name("garnish").value(s[3]);
                    writer.name("vegi").value(s[4]);
                    writer.name("image").value(s[5]);

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
                        else if(name.equals("meal"))
                            s[2] = reader.nextString();
                        else if(name.equals("garnish"))
                            s[3] = reader.nextString();
                        else if(name.equals("vegi"))
                            s[4] = reader.nextString();
                        else if(name.equals("image"))
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
