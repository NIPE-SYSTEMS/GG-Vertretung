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

public class SWSProvider implements VPProvider {

    @Override
    public GGPlan getVP(String url) {
        GGPlan plan = new GGPlan();
        plan.date = url;
        plan.entries.add(new String[]{"SWS 1", "SWS", "SWS", "SWS", "SWS"});
        plan.loaded = true;
        return plan;
    }

    @Override
    public GGPlan getVPSync(String url) {
        GGPlan plan = new GGPlan();
        plan.date = "Sync " + url;
        return plan;
    }

    @Override
    public String getTodayURL() {
        return "HEUTE!";
    }

    @Override
    public String getTomorrowURL() {
        return "MORGEN!";
    }
}
