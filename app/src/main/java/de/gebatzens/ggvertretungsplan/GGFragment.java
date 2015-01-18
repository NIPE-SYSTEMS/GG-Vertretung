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

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.List;

public class GGFragment extends Fragment {

    public static final int TYPE_OVERVIEW = 0, TYPE_TODAY = 1, TYPE_TOMORROW = 2;

    String url;
    GGPlan plan, planh, planm;
    int type = -1;

    public void setParams(int type) {
        this.type = type;
        planh = GGApp.GG_APP.mVPToday;
        planm = GGApp.GG_APP.mVPTomorrow;
        if(type == TYPE_TODAY)
            plan = planh;
        else if(type == TYPE_TOMORROW)
            plan = planm;

    }

    public void recreate() {
        if(getView() == null)
            return;

        ViewGroup vg = (ViewGroup) getView();

        vg.removeAllViews();

        createView(getActivity().getLayoutInflater(), vg);

    }

    public void createLoadingFragment() {
        if(getView() == null)
            return;

        ViewGroup vg = (ViewGroup) getView();
        vg.removeAllViews();


        LinearLayout l = new LinearLayout(getActivity());
        l.setGravity(Gravity.CENTER);

        ProgressBar pb = new ProgressBar(getActivity());
        pb.getIndeterminateDrawable().setColorFilter(getResources().getColor(R.color.main),PorterDuff.Mode.SRC_IN);
        pb.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        pb.setVisibility(ProgressBar.VISIBLE);

        l.addView(pb);

        vg.addView(l);
    }

    private int toPixels(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private LinearLayout createLinearLayoutText(String text, int size, ViewGroup g) {
        LinearLayout l = new LinearLayout(getActivity());
        createTextView(text, size, null, l);
        g.addView(l);
        return l;
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
        TableLayout table = (TableLayout) inflater.inflate(clas ? R.layout.all_table : R.layout.overview_table, group, true).findViewById(R.id.plan_table);

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

    public void createView(LayoutInflater inflater, ViewGroup group) {
        LinearLayout l = new LinearLayout(getActivity());
        l.setOrientation(LinearLayout.VERTICAL);
        group.addView(l);
        l.setPadding(10, 10, 10, 10);
        if(planh == null || planm == null) {
            TextView tv = new TextView(getActivity());
            tv.setText("Error: " + type);
            l.addView(tv);
            Log.w("ggvp", "setParams not called " + type + " " + this + " " + getParentFragment());
        } else if(type == TYPE_OVERVIEW && !GGApp.GG_APP.getVPClass(((MainActivity)getActivity()).selected).equals("")) {
            String clas = GGApp.GG_APP.getVPClass(((MainActivity)getActivity()).selected);

            List<String[]> list = planh.getAllForClass(clas);
            LinearLayout l2 = new LinearLayout(getActivity());
            l2.setOrientation(LinearLayout.VERTICAL);
            l.addView(l2);
            TextView tv1 = createTextView(planh.date + " für "+clas+":", 10, inflater, l2);
            tv1.setPadding(0, 0, 0, toPixels(5));
            tv1.setTextAppearance(getActivity(), R.style.boldText);
            if(list.size() == 0) {
                createTextView("Es fällt nichts für dich aus!", 10, inflater, l2);
            } else
                createTable(list, false, inflater, l2);


            list = planm.getAllForClass(clas);
            LinearLayout l3 = new LinearLayout(getActivity());
            l3.setOrientation(LinearLayout.VERTICAL);
            l.addView(l3);
            TextView tv2 = createTextView(planm.date + " für "+clas+":", 10, inflater, l3);
            tv2.setPadding(0, 0, 0, toPixels(5));
            tv2.setTextAppearance(getActivity(), R.style.boldText);
            if(list.size() == 0) {
                createTextView("Es fällt nichts für dich aus!", 10, inflater, l3);
            } else
                createTable(list, false, inflater, l3);

        } else if(type == TYPE_OVERVIEW) {


            //TODO center vertically
            l.setGravity(Gravity.CENTER_HORIZONTAL);
            l.setPadding(50, 50, 50, 50);

            LinearLayout l2 = new LinearLayout(getActivity());
            l2.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            TextView tv = new TextView(getActivity());
            tv.setText("Du musst eine Klasse wählen!");
            l2.addView(tv);
            l.addView(l2);

            LinearLayout l3 = new LinearLayout(getActivity());
            l3.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            Button b = new Button(getActivity());
            b.setText("Einstellungen");
            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(), SettingsActivity.class);
                    startActivityForResult(i, 1);
                }
            });
            l3.addView(b);

            l.addView(l3);

        } else if(plan.loaded) {
            TextView text = new TextView(getActivity());
            if(plan.throwable != null) {
                text.setTextSize(20);
                text.setTextColor(Color.RED);
                text.setText(plan.throwable.toString());
                l.addView(text);
            } else {
                text.setTextSize(20);
                text.setText(plan.date);
                //l.addView(text);
                createTable(plan.entries, true, inflater, l);
            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle bundle) {
        ScrollView s = new ScrollView(getActivity());
        createView(inflater, s);
        return s;
    }
}
