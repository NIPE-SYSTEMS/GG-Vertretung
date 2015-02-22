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
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NewsFragmentListAdapter extends BaseAdapter {
    private Context context;
    private NewsFragment.News mArrayList;
    private LayoutInflater inflater;

    /*public NewsFragmentListAdapter(Context pContext, String[] pTitle, String[] pContent, int[] pIcon) {*/
    public NewsFragmentListAdapter(Context pContext, NewsFragment.News pArrayList) {
        context = pContext;
        mArrayList = pArrayList;
    }
 
    @SuppressLint("ViewHolder")
	public View getView(int position, View convertView, ViewGroup parent) {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View itemView = inflater.inflate(R.layout.news_fragment_list_item, parent, false);
        TextView txtDate = (TextView) itemView.findViewById(R.id.newsDate);
        TextView txtTitle = (TextView) itemView.findViewById(R.id.newsTitle);
        TextView txtContent = (TextView) itemView.findViewById(R.id.newsContent);
        ImageView imgIcon = (ImageView) itemView.findViewById(R.id.newsIcon);
        txtDate.setText(mArrayList.get(position)[1]);
        txtTitle.setText(mArrayList.get(position)[4]);
        txtContent.setText(Html.fromHtml(mArrayList.get(position)[5]));
        imgIcon.setImageResource(R.drawable.news_icon);
        //imgIcon.setImageResource(mIcnewson[position]);
        return itemView;
    }

    @Override
    public int getCount() {
        return mArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mArrayList.get(position)[4];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}