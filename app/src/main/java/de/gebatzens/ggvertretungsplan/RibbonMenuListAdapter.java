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
 
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
 
public class RibbonMenuListAdapter extends BaseAdapter {
    private Context context;
    private String[] mTitle;
    private int[] mIcon;
    private LayoutInflater inflater;
 
    public RibbonMenuListAdapter(Context pContext, String[] pTitle, int[] pIcon) {
        context = pContext;
        mTitle = pTitle;
        mIcon = pIcon;
    }
 
    @SuppressLint("ViewHolder")
	public View getView(int position, View convertView, ViewGroup parent) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.ribbon_drawer_list_item, parent, false);
        TextView txtTitle = (TextView) itemView.findViewById(R.id.menuTitle);
        ArrayAdapter a;
        ImageView imgIcon = (ImageView) itemView.findViewById(R.id.menuIcon);
        txtTitle.setText(mTitle[position]);
        imgIcon.setImageResource(mIcon[position]);
        return itemView;
    }
 
    @Override
    public int getCount() {
        return mTitle.length;
    }
 
    @Override
    public Object getItem(int position) {
        return mTitle[position];
    }
 
    @Override
    public long getItemId(int position) {
        return position;
    }
}