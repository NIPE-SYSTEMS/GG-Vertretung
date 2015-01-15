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

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentManager;

public class GGFragmentAdapter extends FragmentPagerAdapter {

    GGFragment heute, morgen, overview;

    public GGFragmentAdapter(FragmentManager m) {
        super(m);
        refresh();
    }

    public void refresh() {
        heute = new GGFragment();
        heute.setParams("http://gymglinde.de/typo40/fileadmin/vertretungsplan/VertretungAktuell/PH_heute.htm", GGFragment.TYPE_TODAY);
        morgen = new GGFragment();
        morgen.setParams("http://gymglinde.de/typo40/fileadmin/vertretungsplan/VertretungAktuell/PH_morgen.htm", GGFragment.TYPE_TOMORROW);
        overview = new GGFragment();
        overview.setParams(null, GGFragment.TYPE_OVERVIEW);
        notifyDataSetChanged();
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
    public CharSequence getPageTitle(int p) {
        switch(p) {
            case 0:
                return "Übersicht";
            case 1:
                return "Heute";
            case 2:
                return "Morgen";
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
