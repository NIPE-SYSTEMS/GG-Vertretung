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

import android.app.Activity;
import android.app.Application;

public class GGApp extends Application {

    public GGPlan mVPToday, mVPTomorrow;
    public MainActivity mActivity;
    public boolean created = false;
    public VPProvider mProvider;

    public static GGApp GG_APP;

    @Override
    public void onCreate() {
        super.onCreate();
        GG_APP = this;

    }

    public void create() {
        created = true;

        createProvider(mActivity.selected);
        mVPToday = mProvider.getVP(mProvider.getTodayURL());
        mVPTomorrow = mProvider.getVP(mProvider.getTomorrowURL());

    }

    public void createProvider(int selected) {
        switch(selected) {
            case 0:
                mProvider = new GGProvider();
                break;
            case 1:
                mProvider = new SWSProvider();
                break;
        }
    }

    public void updateVP() {
        mVPToday = mProvider.getVP(mProvider.getTodayURL());
        mVPTomorrow = mProvider.getVP(mProvider.getTomorrowURL());
        mActivity.mContent.mGGFrag.updateFragments();
    }


}
