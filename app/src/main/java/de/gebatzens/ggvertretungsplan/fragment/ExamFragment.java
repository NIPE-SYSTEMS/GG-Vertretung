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

package de.gebatzens.ggvertretungsplan.fragment;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.gebatzens.ggvertretungsplan.GGApp;
import de.gebatzens.ggvertretungsplan.R;

public class ExamFragment extends RemoteDataFragment {

    SwipeRefreshLayout swipeContainer;
    int cardColorIndex = 0;

    public ExamFragment() {
        type = GGApp.FragmentType.EXAMS;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle b) {
        ViewGroup v =  (ViewGroup) inflater.inflate(R.layout.fragment_exam, vg, false);
        if(GGApp.GG_APP.exams != null)
            createRootView(inflater, v);
        return v;
    }

    @Override
    public void onViewCreated(View v, Bundle b) {
        super.onViewCreated(v, b);

        swipeContainer = (SwipeRefreshLayout) v.findViewById(R.id.exam_refresh);
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
                }, true, GGApp.FragmentType.EXAMS);
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
        ScrollView sv = new ScrollView(getActivity());
        sv.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        sv.setTag("exam_scroll");
        ((LinearLayout) view.findViewById(R.id.exam_content)).addView(sv);
        LinearLayout l = new LinearLayout(getActivity());
        l.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        l.setOrientation(LinearLayout.VERTICAL);
        int p = toPixels(6);
        l.setPadding(p, p, p, p);
        sv.addView(l);
        createTextView(getResources().getString(R.string.my_exams),toPixels(12),inflater,l);
        for(int i = 0; i < GGApp.GG_APP.exams.size(); i++) {
            if(GGApp.GG_APP.provider.getGroup().equals("lehrer")&&(GGApp.GG_APP.exams.get(i)[6].equals(GGApp.GG_APP.provider.getUsername()))) {
                ExamItem exam_item = new ExamItem();
                exam_item.id = GGApp.GG_APP.exams.get(i)[0];
                exam_item.date = GGApp.GG_APP.exams.get(i)[1];
                exam_item.schoolclass = GGApp.GG_APP.exams.get(i)[2];
                exam_item.lesson = GGApp.GG_APP.exams.get(i)[3];
                exam_item.length = GGApp.GG_APP.exams.get(i)[4];
                exam_item.subject = GGApp.GG_APP.exams.get(i)[5];
                exam_item.teacher = GGApp.GG_APP.exams.get(i)[6];
                CardView cv = createCardItem(exam_item, inflater);
                l.addView(cv);
            } else if(GGApp.GG_APP.exams.get(i)[2].equals(GGApp.GG_APP.provider.getGroup())) {
                ExamItem exam_item = new ExamItem();
                exam_item.id = GGApp.GG_APP.exams.get(i)[0];
                exam_item.date = GGApp.GG_APP.exams.get(i)[1];
                exam_item.schoolclass = GGApp.GG_APP.exams.get(i)[2];
                exam_item.lesson = GGApp.GG_APP.exams.get(i)[3];
                exam_item.length = GGApp.GG_APP.exams.get(i)[4];
                exam_item.subject = GGApp.GG_APP.exams.get(i)[5];
                exam_item.teacher = GGApp.GG_APP.exams.get(i)[6];
                CardView cv = createCardItem(exam_item, inflater);
                l.addView(cv);
            } else {
                createTextView("No entries for you.",toPixels(12),inflater,view);
                break;
            }
        }
        createTextView(getResources().getString(R.string.all_exams),toPixels(12),inflater,l);
        for(int i = 0; i < GGApp.GG_APP.exams.size(); i++) {
            ExamItem exam_item = new ExamItem();
            exam_item.id = GGApp.GG_APP.exams.get(i)[0];
            exam_item.date = GGApp.GG_APP.exams.get(i)[1];
            exam_item.schoolclass = GGApp.GG_APP.exams.get(i)[2];
            exam_item.lesson = GGApp.GG_APP.exams.get(i)[3];
            exam_item.length = GGApp.GG_APP.exams.get(i)[4];
            exam_item.subject = GGApp.GG_APP.exams.get(i)[5];
            exam_item.teacher = GGApp.GG_APP.exams.get(i)[6];
            CardView cv = createCardItem(exam_item, inflater);
            l.addView(cv);
        }
        cardColorIndex = 0;
    }

    @Override
    public ViewGroup getContentView() {
        return (ViewGroup) getView().findViewById(R.id.exam_content);
    }

    private CardView createCardItem(ExamItem exam_item, LayoutInflater i) {
        CardView ecv = createCardView();
        String[] colors = getActivity().getResources().getStringArray(GGApp.GG_APP.provider.getColorArray());
        ecv.setCardBackgroundColor(Color.parseColor(colors[cardColorIndex]));
        cardColorIndex++;
        if(cardColorIndex == colors.length)
            cardColorIndex = 0;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, toPixels(6));
        ecv.setLayoutParams(params);
        i.inflate(R.layout.exam_cardview_entry, ecv, true);
        /*Date d = new Date();
        Calendar c = Calendar.getInstance();
        c.setTime(d);
        c.add(Calendar.DAY_OF_YEAR, -1);
        Date dt = c.getTime();
        try {
            if(getDate(exam_item.date).before(dt)) {
                ecv.setAlpha(0.65f);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }*/
        ((TextView) ecv.findViewById(R.id.ecv_date)).setText(getFormatedDate(exam_item.date));
        ((TextView) ecv.findViewById(R.id.ecv_lesson)).setText(exam_item.lesson + ".");
        ((TextView) ecv.findViewById(R.id.ecv_subject_teacher)).setText(exam_item.subject + " [" + exam_item.teacher + "]");
        ((TextView) ecv.findViewById(R.id.ecv_schoolclass)).setText(exam_item.schoolclass);
        return ecv;
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

    private Date getDate(String date) throws ParseException {
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.parse(date);
    }

    public class ExamItem {
        String id;
        String date;
        String schoolclass;
        String lesson;
        String length;
        String subject;
        String teacher;
    }
}
