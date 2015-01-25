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
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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

    private void createButtonWithText(LinearLayout l, String text, String button, View.OnClickListener onclick) {
        //TODO center vertically
        RelativeLayout r = new RelativeLayout(getActivity());
        r.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView tv = new TextView(getActivity());
        RelativeLayout.LayoutParams tvparams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        tvparams.addRule(RelativeLayout.ABOVE, R.id.reload_button);
        tvparams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        tv.setLayoutParams(tvparams);
        tv.setText(text);
        tv.setTextSize(23);
        tv.setPadding( 0, 0, 0, toPixels(15));
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        r.addView(tv);

        Button b = new Button(getActivity());
        RelativeLayout.LayoutParams bparams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        bparams.addRule(RelativeLayout.CENTER_VERTICAL);
        bparams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        b.setLayoutParams(bparams);
        b.setId(R.id.reload_button);
        b.setText(button);
        b.setTextSize(23);
        b.setAllCaps(false);
        b.setTypeface(null, Typeface.NORMAL);
        b.setOnClickListener(onclick);
        r.addView(b);

        l.addView(r);
    }

    public void createView(LayoutInflater inflater, ViewGroup group) {
        ScrollView sv = new ScrollView(getActivity());
        sv.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.MATCH_PARENT));
        sv.setFillViewport(true);
        LinearLayout l = new LinearLayout(getActivity());
        l.setOrientation(LinearLayout.VERTICAL);
        group.addView(sv);
        if(planh == null || planm == null) {
            TextView tv = new TextView(getActivity());
            tv.setText("Error: " + type);
            l.addView(tv);
            Log.w("ggvp", "setParams not called " + type + " " + this + " " + getParentFragment());
        } else if(type == TYPE_OVERVIEW && !GGApp.GG_APP.getVPClass().equals("") && planh.throwable == null && planm.throwable == null) {
            String clas = GGApp.GG_APP.getVPClass();

            List<String[]> list = planh.getAllForClass(clas);
            FrameLayout f2 = new FrameLayout(getActivity());
            CardView c2 = new CardView(getActivity());
            CardView.LayoutParams c2params = new CardView.LayoutParams(
                    CardView.LayoutParams.MATCH_PARENT,
                    CardView.LayoutParams.WRAP_CONTENT
            );
            c2params.setMargins(toPixels(8), toPixels(8), toPixels(8), toPixels(8));
            c2.setLayoutParams(c2params);
            c2.setContentPadding(toPixels(16), toPixels(16), toPixels(16), toPixels(16));
            LinearLayout l2 = new LinearLayout(getActivity());
            l2.setOrientation(LinearLayout.VERTICAL);
            c2.addView(l2);
            f2.addView(c2);
            l.addView(f2);
            TextView tv2 = createTextView(planh.date + " für "+clas+":", 20, inflater, l2);
            tv2.setPadding(0, 0, 0, toPixels(8));
            tv2.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            if(list.size() == 0) {
                createTextView("Es fällt nichts für dich aus!", 14, inflater, l2);
            } else
                createTable(list, false, inflater, l2);

            if(!planh.special.isEmpty()) {
                TextView tv3 = new TextView(getActivity());
                tv3.setText(Html.fromHtml("<b>Besondere Mitteilungen</b><br>" + planh.special));
                l2.addView(tv3);
            }

            list = planm.getAllForClass(clas);
            FrameLayout f4 = new FrameLayout(getActivity());
            CardView c4 = new CardView(getActivity());
            CardView.LayoutParams c4params = new CardView.LayoutParams(
                    CardView.LayoutParams.MATCH_PARENT,
                    CardView.LayoutParams.WRAP_CONTENT
            );
            c4params.setMargins(toPixels(8), toPixels(8), toPixels(8), toPixels(8));
            c4.setLayoutParams(c4params);
            c4.setContentPadding(toPixels(16), toPixels(16), toPixels(16), toPixels(16));
            LinearLayout l4 = new LinearLayout(getActivity());
            l4.setOrientation(LinearLayout.VERTICAL);
            c4.addView(l4);
            f4.addView(c4);
            l.addView(f4);
            TextView tv4 = createTextView(planm.date + " für "+clas+":", 20, inflater, l4);
            tv4.setPadding(0, 0, 0, toPixels(8));
            tv4.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            if(list.size() == 0) {
                createTextView("Es fällt nichts für dich aus!", 14, inflater, l4);
            } else
                createTable(list, false, inflater, l4);

            if(!planm.special.isEmpty()) {
                TextView tv5 = new TextView(getActivity());
                tv5.setText(Html.fromHtml("<b>Besondere Mitteilungen</b><br>" + planm.special));
                l4.addView(tv5);
            }

        } else if(type == TYPE_OVERVIEW && planh.throwable == null && planm.throwable == null) {
            createButtonWithText(l, "Du musst eine Klasse wählen!", "Einstellungen", new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(), SettingsActivity.class);
                    getActivity().startActivityForResult(i, 1);
                }
            });


        } else if((type == TYPE_OVERVIEW && (planm.throwable != null || planh.throwable != null)) || (plan != null && plan.throwable != null)) {
            LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                createButtonWithText(l, "Verbindung prüfen und wiederholen", "Nochmal", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GGApp.GG_APP.refreshAsync(null, true);
                    }
                });
        } else {
            if(!plan.special.isEmpty()) {
                FrameLayout f6 = new FrameLayout(getActivity());
                CardView c6 = new CardView(getActivity());
                CardView.LayoutParams c6params = new CardView.LayoutParams(
                        CardView.LayoutParams.MATCH_PARENT,
                        CardView.LayoutParams.WRAP_CONTENT
                );
                c6params.setMargins(toPixels(8), toPixels(8), toPixels(8), toPixels(8));
                c6.setLayoutParams(c6params);
                c6.setContentPadding(toPixels(16), toPixels(16), toPixels(16), toPixels(16));
                LinearLayout l6 = new LinearLayout(getActivity());
                l6.setOrientation(LinearLayout.VERTICAL);
                c6.addView(l6);
                f6.addView(c6);
                l.addView(f6);
                TextView tv6 = new TextView(getActivity());
                tv6.setText(Html.fromHtml("<b>Besondere Mitteilungen</b><br>" + planm.special));
                l6.addView(tv6);
            }

            FrameLayout f7 = new FrameLayout(getActivity());
            FrameLayout.LayoutParams f7params = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
            );
            f7.setLayoutParams(f7params);
            CardView c7 = new CardView(getActivity());
            CardView.LayoutParams c7params = new CardView.LayoutParams(
                    CardView.LayoutParams.MATCH_PARENT,
                    CardView.LayoutParams.WRAP_CONTENT
            );
            c7params.setMargins(toPixels(8), toPixels(8), toPixels(8), toPixels(8));
            c7.setLayoutParams(c7params);
            c7.setContentPadding(toPixels(16), toPixels(16), toPixels(16), toPixels(16));
            LinearLayout l7 = new LinearLayout(getActivity());
            l7.setOrientation(LinearLayout.VERTICAL);
            c7.addView(l7);
            f7.addView(c7);
            l.addView(f7);
            createTable(plan.entries, true, inflater, l7);
        }
        sv.addView(l);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle bundle) {
        LinearLayout l = new LinearLayout(getActivity());
        l.setOrientation(LinearLayout.VERTICAL);
        createView(inflater, l);
        return l;
    }
}
