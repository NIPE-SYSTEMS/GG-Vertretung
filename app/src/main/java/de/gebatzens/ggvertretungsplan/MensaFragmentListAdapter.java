/*
 * Copyright (C) 2015 Fabian Schultis
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MensaFragmentListAdapter extends BaseAdapter {
    private Context context;
    private MensaFragment.Mensa mArrayList;
    private LayoutInflater inflater;
    private String formattedDate;

    public MensaFragmentListAdapter(Context pContext, MensaFragment.Mensa pArrayList) {
        context = pContext;
        mArrayList = pArrayList;
    }
 
    @SuppressLint("ViewHolder")
	public View getView(int position, View convertView, ViewGroup parent) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.mensa_fragment_list_item, parent, false);
        TextView txtDate = (TextView) itemView.findViewById(R.id.mensaDate);
        TextView txtTitle = (TextView) itemView.findViewById(R.id.mensaTitle);
        TextView txtContent = (TextView) itemView.findViewById(R.id.mensaContent);

        DateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat dateFormatter = new SimpleDateFormat("d. MMM yy");
        try
        {
            String startDate = mArrayList.get(position)[1];
            Date parsedDate = parser.parse(startDate);
            formattedDate = dateFormatter.format(parsedDate);
        }
        catch (ParseException e)
        {
            formattedDate = mArrayList.get(position)[1];
            e.printStackTrace();
        }

        txtDate.setText(formattedDate);
        txtDate.setTextColor(Color.parseColor("#727272"));
        txtTitle.setText(mArrayList.get(position)[2]);
        txtContent.setText(mArrayList.get(position)[3].replace("mit ","").replace("mit",""));

        return itemView;
    }

    public String[] getMensaMeal(int position) {
        return mArrayList.get(position);
    }

    @Override
    public int getCount() {
        return mArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mArrayList.get(position)[2];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}