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

import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import de.gebatzens.ggvertretungsplan.GGApp;
import de.gebatzens.ggvertretungsplan.MainActivity;
import de.gebatzens.ggvertretungsplan.R;
import de.gebatzens.ggvertretungsplan.view.SlidingTabLayout;

public class GGContentFragment extends RemoteDataFragment {

    public Toolbar mToolbar;
    public ViewPager mViewPager;
    public SlidingTabLayout mSlidingTabLayout;
    public GGFragmentAdapter mGGFrag;
    public SwipeRefreshLayout swipeContainer;
    public Bundle bundle;

    public GGContentFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment, container, false);
    }

    private int toPixels(int dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        bundle = ((MainActivity) getActivity()).savedState;
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mGGFrag = new GGFragmentAdapter(this, savedInstanceState, (MainActivity) getActivity());
        if(bundle != null) {
            mGGFrag.heute.spinnerPos = bundle.getInt("ggvp_frag_today_spinner");
            mGGFrag.morgen.spinnerPos = bundle.getInt("ggvp_frag_tomorrow_spinner");
        }
        mViewPager.setAdapter(mGGFrag);
        mViewPager.setOffscreenPageLimit(3);
        if(bundle != null)
            mViewPager.setCurrentItem(bundle.getInt("ggvp_tab"));

        mToolbar = (Toolbar) ((MainActivity) this.getActivity()).mToolbar;
        ColorDrawable mToolbarColor = (ColorDrawable) mToolbar.getBackground();
        int mToolbarColorId = mToolbarColor.getColor();

        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        mSlidingTabLayout.setBackgroundColor(mToolbarColorId);
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE){
            mSlidingTabLayout.setPadding(toPixels(48),0,toPixels(48),0);
        }
        else if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            mSlidingTabLayout.setPadding(toPixels(8),0,toPixels(8),0);
        }
        mSlidingTabLayout.setViewPager(mViewPager);

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
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
                }, true, GGApp.FragmentType.PLAN);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.custom_material_green,
                R.color.custom_material_red,
                R.color.custom_material_blue,
                R.color.custom_material_orange);

    }

    @Override
    public void saveInstanceState(Bundle b) {

        b.putInt("ggvp_frag_today_spinner", mGGFrag.heute.spinnerPos);
        b.putInt("ggvp_frag_tomorrow_spinner", mGGFrag.morgen.spinnerPos);
        b.putInt("ggvp_tab", mViewPager.getCurrentItem());
    }

    @Override
    public void createView(LayoutInflater inflater, ViewGroup vg) {

    }

    @Override
    public ViewGroup getContentView() {
        return null;
    }

    @Override
    public void setFragmentLoading() {
        mGGFrag.setFragmentsLoading();
    }

    @Override
    public void updateFragment() {
        mGGFrag.morgen.spinnerPos = 0;
        mGGFrag.heute.spinnerPos = 0;
        mGGFrag.updateFragments();
    }
}
