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

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.LinearLayout;

public class ExamFragment extends RemoteDataFragment {

    public ExamFragment() {
        type = GGApp.FragmentType.EXAMS;
    }

    @Override
    public void createView(LayoutInflater inflater, ViewGroup vg) {
        vg.addView(new LinearLayout(getActivity()));
    }

    @Override
    public ViewGroup getContentView() {
        return (ViewGroup) getView();
    }

}
