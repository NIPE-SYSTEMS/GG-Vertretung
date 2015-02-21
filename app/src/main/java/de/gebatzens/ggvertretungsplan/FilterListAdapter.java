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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class FilterListAdapter extends BaseAdapter {

    ArrayList<FilterActivity.Filter> list;
    Context c;

    public FilterListAdapter(Context c, ArrayList<FilterActivity.Filter> filters) {
        this.c = c;
        list = filters;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        FilterActivity.Filter filter = list.get(position);
        ViewGroup vg = (ViewGroup) ((LayoutInflater) c.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.filter_item, parent, false);
        ((TextView) vg.findViewById(R.id.filter_main_text)).setText(filter.toString());
        ((ImageButton) vg.findViewById(R.id.filter_edit)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        ((ImageButton)vg.findViewById(R.id.filter_delete)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.remove(getItem(position));
                notifyDataSetChanged();
            }
        });
        return vg;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


}
