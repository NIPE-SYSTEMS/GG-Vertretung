/*
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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;

/**
 * Created by hauke on 11.12.14.
 */
public class GGFragment extends Fragment {

    public static final int TYPE_OVERVIEW = 0, TYPE_TODAY = 1, TYPE_TOMORROW = 2;

    String url;
    static GGPlan planh, planm;
    GGPlan plan;
    int type;

    public void setParams(String u, int type) {
        url = u;
        this.type = type;

        if(url != null)
            plan = MainActivity.mProvider.getVP(url);
        if(type == TYPE_TODAY)
            planh = plan;
        else if(type == TYPE_TOMORROW)
            planm = plan;
    }

    private int toPixels(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private TextView createTextView(String text, int size, LayoutInflater inflater, ViewGroup group) {
       // TextView t = (TextView) inflater.inflate(R.layout.plan_text, group, true).findViewById(R.id.plan_entry);
        TextView t = new TextView(getActivity());
        t.setText(text);
        t.setPadding(0, 0, toPixels(20), 0);
        t.setTextSize(size);
        group.addView(t);
        return t;
    }

    public View createTable(List<String[]> list, boolean clas, LayoutInflater inflater, ViewGroup group) {
        TableLayout table = (TableLayout) inflater.inflate(R.layout.overview_table, group, true).findViewById(R.id.plan_table);

        for(String[] s : list) {
            TableRow row = new TableRow(getActivity());
            if(clas)
                createTextView(s[0], 10, inflater, row);
            createTextView(s[1], 10, inflater, row);
            createTextView(s[2], 10, inflater, row);
            createTextView(s[3], 10, inflater, row);
            createTextView(s[4], 10, inflater, row);
            table.addView(row);
        }
        return table;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle bundle) {

        LinearLayout l = new LinearLayout(getActivity());
        l.setPadding(10, 10, 10, 10);
        if(type == TYPE_OVERVIEW) {
            List<String[]> list = planh.getAllForClass("Eb");
            createTable(list, false, inflater, l);

        } else if(plan.loaded) {
            TextView text = new TextView(getActivity());
            if(plan.throwable != null) {
                text.setTextSize(40);
                text.setText(plan.throwable.toString());
            } else {
                text.setTextSize(20);
                text.setText(plan.date);

                for(String[] s : plan.entries) {
                    TextView tv = new TextView(getActivity());
                    tv.setTextSize(10);
                    tv.setText(s[0] + " " + s[1] + " " + s[2] + " " + s[3] + " " + s[4]);
                    l.addView(tv);
                }

            }
            l.addView(text);
        } else {
            TextView text = new TextView(getActivity());
            text.setTextSize(20);
            text.setText("Lade...");
            l.addView(text);
        }

        return l;

    }
}
