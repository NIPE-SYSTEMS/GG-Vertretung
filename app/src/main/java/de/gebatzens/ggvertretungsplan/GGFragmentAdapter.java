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

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

public class GGFragmentAdapter extends FragmentPagerAdapter {

    GGFragment heute, morgen, overview;
    MainActivity mActivity;

    public GGFragmentAdapter(FragmentManager m, Bundle savedState, MainActivity ma) {
        super(m);
        createFragments();
        mActivity = ma;

    }

    private void createFragments() {
        heute = new GGFragment();
        heute.setParams(GGFragment.TYPE_TODAY);
        morgen = new GGFragment();
        morgen.setParams(GGFragment.TYPE_TOMORROW);
        overview = new GGFragment();
        overview.setParams(GGFragment.TYPE_OVERVIEW);

    }

    public void updateFragments() {
        heute.setParams(GGFragment.TYPE_TODAY);
        morgen.setParams(GGFragment.TYPE_TOMORROW);
        overview.setParams(GGFragment.TYPE_OVERVIEW);
        heute.recreate();
        morgen.recreate();
        overview.recreate();
        ((GGContentFragment)mActivity.mContent).mSlidingTabLayout.setViewPager(((GGContentFragment)mActivity.mContent).mViewPager);
    }

    public void setFragmentsLoading() {
        heute.createLoadingFragment();
        morgen.createLoadingFragment();
        overview.createLoadingFragment();
        ((GGContentFragment)mActivity.mContent).mSlidingTabLayout.setViewPager(((GGContentFragment)mActivity.mContent).mViewPager);
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0:
                return overview;
            case 1:
                return heute;
            case 2:
                return morgen;
            default:
                return null;
        }
    }

    @Override
    public Object instantiateItem(ViewGroup view, int pos) {
        Object o = super.instantiateItem(view, pos);
        ((GGFragment)o).setParams(pos == 0 ? GGFragment.TYPE_OVERVIEW : pos == 1 ? GGFragment.TYPE_TODAY : GGFragment.TYPE_TOMORROW);
        return o;
    }

    @Override
    public CharSequence getPageTitle(int p) {
        switch(p) {
            case 0:
                return "Übersicht";
            case 1:
                return GGApp.GG_APP.plans == null ? "Heute"  : GGApp.GG_APP.provider.getDay(GGApp.GG_APP.plans[0].date);
            case 2:
                return GGApp.GG_APP.plans == null ? "Morgen" : GGApp.GG_APP.provider.getDay(GGApp.GG_APP.plans[1].date);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
