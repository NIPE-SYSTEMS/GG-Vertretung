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

import android.content.res.Configuration;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

public class GGContentFragment extends Fragment {

    Toolbar mToolbar;
    ViewPager mViewPager;
    SlidingTabLayout mSlidingTabLayout;
    GGFragmentAdapter mGGFrag;
    SwipeRefreshLayout swipeContainer;

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
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mGGFrag = new GGFragmentAdapter(getActivity().getSupportFragmentManager(), savedInstanceState, (MainActivity) getActivity());
        mViewPager.setAdapter(mGGFrag);

        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.

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

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.swipeContainer);
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
                }, true);
            }
        });
        // Configure the refreshing colors
        swipeContainer.setColorSchemeResources(R.color.custom_material_green,
                R.color.custom_material_red,
                R.color.custom_material_blue,
                R.color.custom_material_orange);

    }

}
