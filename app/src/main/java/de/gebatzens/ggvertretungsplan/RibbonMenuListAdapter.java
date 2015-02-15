/* Copyright (C) Fabian Schultis - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Fabian Schultis <fabian@microbotik.de>, May 2014
 */

package de.gebatzens.ggvertretungsplan;
 
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
        ImageView imgIcon = (ImageView) itemView.findViewById(R.id.menuIcon);
        //txtTitle.setTextColor(Color.rgb(0,0,0));
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