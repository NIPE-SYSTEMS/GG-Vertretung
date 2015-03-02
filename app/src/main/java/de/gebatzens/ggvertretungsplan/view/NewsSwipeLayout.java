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
package de.gebatzens.ggvertretungsplan.view;

import android.content.Context;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;

import de.gebatzens.ggvertretungsplan.GGApp;
import de.gebatzens.ggvertretungsplan.MainActivity;
import de.gebatzens.ggvertretungsplan.fragment.NewsFragment;

public class NewsSwipeLayout extends SwipeRefreshLayout {

    public NewsSwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_MOVE:

                if(GGApp.GG_APP.getFragmentType() == GGApp.FragmentType.NEWS) {
                    ListView lv = ((NewsFragment) ((MainActivity) getContext()).mContent).lv;
                    if(lv.getChildCount() == 0)
                        return super.onInterceptTouchEvent(event);

                    View c = lv.getChildAt(0);
                    int i = -c.getTop() + lv.getFirstVisiblePosition() * c.getHeight();

                    if (i != 0)
                        return false;
                }
        }

        return super.onInterceptTouchEvent(event);
    }
}
