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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ExamFragment extends RemoteDataFragment {

    public ExamFragment() {
        type = GGApp.FragmentType.EXAMS;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup vg, Bundle b) {
        ViewGroup v =  (ViewGroup) inflater.inflate(R.layout.fragment_exam, vg, false);
        if(GGApp.GG_APP.exams != null)
            createView(inflater, v);
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

    public static class Exams implements RemoteData {

        Throwable throwable;
        Bitmap bitmap;

        @Override
        public Throwable getThrowable() {
            return throwable;
        }

        @Override
        public void save(String file) {
            try {
                FileOutputStream fos = GGApp.GG_APP.openFileOutput(file, Context.MODE_PRIVATE);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean load(String file) {
            try {
                bitmap = BitmapFactory.decodeStream(GGApp.GG_APP.openFileInput(file));
            } catch(Exception e) {
                e.printStackTrace();
                return false;
            }
            return bitmap != null;
        }


    }

}
