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

import android.content.Context;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.ScrollView;

public class GGSwipeLayout extends SwipeRefreshLayout {

    private int mTouchSlop;
    private float mPrevX;

    public GGSwipeLayout(Context context, AttributeSet attrs) {
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
                int i = ((MainActivity) getContext()).mContent.mViewPager.getCurrentItem();
                GGFragment frag = (GGFragment) ((FragmentPagerAdapter) ((MainActivity) getContext()).mContent.mViewPager.getAdapter()).getItem(i);
                ScrollView sv = (ScrollView) frag.getView().findViewWithTag("ggfrag_scrollview");

                if(sv.getScrollY() != 0)
                    return false;
                break;

            case MotionEvent.ACTION_MOVE:
                float xd = Math.abs(event.getX() - mPrevX);
                if (xd > mTouchSlop)
                    return false;

        }

        return super.onInterceptTouchEvent(event);
    }

}