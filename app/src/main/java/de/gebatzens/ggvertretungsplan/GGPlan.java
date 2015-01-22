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

import java.util.ArrayList;
import java.util.List;

public class GGPlan {

    public ArrayList<String[]> entries = new ArrayList<String[]>();
    public String date = "";
    public String special = "";
    public Throwable throwable = null;

    public GGPlan() {

    }

    public List<String[]> getAllForClass(String c) {
        c = c.toLowerCase();
        c = c.replaceAll(" ", "");
        ArrayList<String[]> list = new ArrayList<String[]>();
        for(String[] ss : entries) {
            String sc = ss[0].toLowerCase().replaceAll(" ", "");
            if (sc.equals(c))
                list.add(ss);
        }
        return list;
    }

}
