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

import java.util.ArrayList;

public abstract class VPProvider {

    GGApp gg;

    public VPProvider(GGApp app) {
        this.gg = app;
    }

    public static String decode(String html) {
        if(html == null)
            return null;

        html = html.trim();

        html = html.replaceAll("&uuml;", "ü");
        html = html.replaceAll("&auml;", "ä");
        html = html.replaceAll("&ouml;", "ö");

        html = html.replaceAll("&Uuml;", "Ü");
        html = html.replaceAll("&Auml;", "Ä");
        html = html.replaceAll("&Ouml;", "Ö");

        html = html.replaceAll("&nbsp;", "");
        html = html.replaceAll("---", ""); //SWS '---'

        return html;
    }

    /**
     *
     * @param toast
     * @return GGPlan[2], Elemente können null sein
     */
    public abstract GGPlan[] getPlans(boolean toast);
    public abstract String getFullName();
    public abstract String getDay(String date);
    public abstract int getColor();
    public abstract int getDarkColor();
    public abstract int getTheme();
    public abstract int getImage();
    public abstract String getWebsite();
    public abstract boolean loginNeeded();
    public abstract int login(String u, String p);
    public abstract void logout();
    public abstract ArrayList getNews();

}
