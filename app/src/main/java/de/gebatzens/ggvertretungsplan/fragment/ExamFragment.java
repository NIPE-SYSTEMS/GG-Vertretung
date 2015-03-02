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

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.gebatzens.ggvertretungsplan.GGApp;
import de.gebatzens.ggvertretungsplan.R;
import de.gebatzens.ggvertretungsplan.view.TouchImageView;

public class ExamFragment extends RemoteDataFragment {

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
    public void createView(LayoutInflater inflater, ViewGroup vg) {
        TouchImageView tiv = new TouchImageView(getActivity());
        Bitmap bitmap = GGApp.GG_APP.exams.bitmap;
        if(bitmap != null)
            tiv.setImageBitmap(bitmap);
        ((ViewGroup) vg.findViewById(R.id.exam_content)).addView(tiv);
    }

    @Override
    public ViewGroup getContentView() {
        return (ViewGroup) getView().findViewById(R.id.exam_content);
    }



}
