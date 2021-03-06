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
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

import de.gebatzens.ggvertretungsplan.GGApp;
import de.gebatzens.ggvertretungsplan.MainActivity;
import de.gebatzens.ggvertretungsplan.fragment.SubstFragment;
import de.gebatzens.ggvertretungsplan.fragment.SubstPagerFragment;

public class SubstSwipeLayout extends SwipeRefreshLayout {

    private int mTouchSlop;
    private float mPrevX;

    public SubstSwipeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                MotionEvent m = MotionEvent.obtain(event);
                mPrevX = m.getX();
                m.recycle();

                break;

            case MotionEvent.ACTION_MOVE:
                float xd = Math.abs(event.getX() - mPrevX);
                if (xd > mTouchSlop)
                    return false;

                if(GGApp.GG_APP.getFragmentType() == GGApp.FragmentType.PLAN) {
                    int i = ((SubstFragment) ((MainActivity) getContext()).mContent).mViewPager.getCurrentItem();
                    SubstPagerFragment frag = (SubstPagerFragment) ((FragmentPagerAdapter) ((SubstFragment) ((MainActivity) getContext()).mContent).mViewPager.getAdapter()).getItem(i);
                    ScrollView sv = (ScrollView) frag.getView().findViewWithTag("ggfrag_scrollview");

                    if (sv != null && sv.getScrollY() != 0)
                        return false;
                }
        }

        return super.onInterceptTouchEvent(event);
    }

}