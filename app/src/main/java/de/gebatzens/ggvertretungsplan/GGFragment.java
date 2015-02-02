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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Scanner;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class GGFragment extends Fragment {

    public static final int TYPE_OVERVIEW = 0, TYPE_TODAY = 1, TYPE_TOMORROW = 2;

    String url;
    GGPlan plan, planh, planm;
    int type = -1;

    public void setParams(int type) {
        this.type = type;
        planh = GGApp.GG_APP.mVPToday;
        planm = GGApp.GG_APP.mVPTomorrow;
        if(type == TYPE_TODAY)
            plan = planh;
        else if(type == TYPE_TOMORROW)
            plan = planm;

    }

    public void recreate() {
        if(getView() == null)
            return;

        ViewGroup vg = (ViewGroup) getView();

        vg.removeAllViews();

        createView(getActivity().getLayoutInflater(), vg);

    }

    private View createLoadingView() {
        LinearLayout l = new LinearLayout(getActivity());
        l.setGravity(Gravity.CENTER);

        ProgressBar pb = new ProgressBar(getActivity());
        pb.getIndeterminateDrawable().setColorFilter(GGApp.GG_APP.mProvider.getColor(),PorterDuff.Mode.SRC_IN);
        pb.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        pb.setVisibility(ProgressBar.VISIBLE);

        l.addView(pb);
        return l;
    }

    public void createLoadingFragment() {
        if(getView() == null)
            return;

        ViewGroup vg = (ViewGroup) getView();
        vg.removeAllViews();

        vg.addView(createLoadingView());
    }

    private int toPixels(float dp) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private LinearLayout createLinearLayoutText(String text, int size, ViewGroup g) {
        LinearLayout l = new LinearLayout(getActivity());
        createTextView(text, size, null, l);
        g.addView(l);
        return l;
    }

    private TextView createTextView(String text, int size, LayoutInflater inflater, ViewGroup group) {
       // TextView t = (TextView) inflater.inflate(R.layout.plan_text, group, true).findViewById(R.id.plan_entry);
        TextView t = new TextView(getActivity());
        t.setText(text);
        t.setPadding(0, 0, toPixels(20), 0);
        t.setTextSize(size);
        group.addView(t);
        return t;
    }

    public View createTable(List<String[]> list, boolean clas, LayoutInflater inflater, ViewGroup group, GGPlan plan) {
        TableLayout table = (TableLayout) inflater.inflate(clas ? R.layout.all_table : R.layout.overview_table, group, true).findViewById(R.id.plan_table);

        for(String[] s : list) {
            TableRow row = new TableRow(getActivity());
            if(clas)
                createTextView(s[0], 10, inflater, row);
            createTextView(s[1], 10, inflater, row);
            createTextView(s[2], 10, inflater, row);
            createTextView(s[3], 10, inflater, row);
            createTextView(s[4], 10, inflater, row);
            table.addView(row);
        }
        return table;
    }


    private CardView createCardView() {
        CardView c2 = new CardView(getActivity());
        CardView.LayoutParams c2params = new CardView.LayoutParams(
                CardView.LayoutParams.MATCH_PARENT,
                CardView.LayoutParams.WRAP_CONTENT
        );
        c2.setLayoutParams(c2params);
        c2.setUseCompatPadding(true);
        c2.setContentPadding(toPixels(16), toPixels(16), toPixels(16), toPixels(16));
        return c2;
    }

    private void createButtonWithText(LinearLayout l, String text, String button, View.OnClickListener onclick) {
        RelativeLayout r = new RelativeLayout(getActivity());
        r.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        TextView tv = new TextView(getActivity());
        RelativeLayout.LayoutParams tvparams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        tvparams.addRule(RelativeLayout.ABOVE, R.id.reload_button);
        tvparams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        tv.setLayoutParams(tvparams);
        tv.setText(text);
        tv.setTextSize(23);
        tv.setPadding( 0, 0, 0, toPixels(15));
        tv.setGravity(Gravity.CENTER_HORIZONTAL);
        r.addView(tv);

        Button b = new Button(getActivity());
        RelativeLayout.LayoutParams bparams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        bparams.addRule(RelativeLayout.CENTER_VERTICAL);
        bparams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        b.setLayoutParams(bparams);
        b.setId(R.id.reload_button);
        b.setText(button);
        b.setTextSize(23);
        b.setAllCaps(false);
        b.setTypeface(null, Typeface.NORMAL);
        b.setOnClickListener(onclick);
        r.addView(b);

        l.addView(r);
    }

    public void createView(final LayoutInflater inflater, ViewGroup group) {
        ScrollView sv = new ScrollView(getActivity());
        sv.setLayoutParams(new ScrollView.LayoutParams(ScrollView.LayoutParams.MATCH_PARENT, ScrollView.LayoutParams.MATCH_PARENT));
        sv.setFillViewport(true);
        sv.setTag("ggfrag_scrollview");
        LinearLayout l = new LinearLayout(getActivity());
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(toPixels(4),toPixels(4),toPixels(4),toPixels(4));
        group.addView(sv);
        if(planh == null || planm == null) {
            TextView tv = new TextView(getActivity());
            tv.setText("Error: " + type);
            l.addView(tv);
            Log.w("ggvp", "setParams not called " + type + " " + this + " " + getParentFragment());
        } else if(type == TYPE_OVERVIEW && !GGApp.GG_APP.getSelectedGrade().equals("") && planh.throwable == null && planm.throwable == null) {
            //normale Übersicht
            String clas = GGApp.GG_APP.getSelectedGrade();

            List<String[]> list = planh.getAllForClass(clas);


            FrameLayout f2s = new FrameLayout(getActivity());
            f2s.setPadding(toPixels(1.3f),toPixels(0.3f),toPixels(1.3f),toPixels(0.3f));
            CardView c2s = createCardView();
            createTextView("Stand: " + planh.loadDate, 15, inflater, c2s);
            f2s.addView(c2s);
            l.addView(f2s);

            FrameLayout f2 = new FrameLayout(getActivity());
            f2.setPadding(toPixels(1.3f),toPixels(0.3f),toPixels(1.3f),toPixels(0.3f));
            CardView c2 = createCardView();
            LinearLayout l2 = new LinearLayout(getActivity());
            l2.setOrientation(LinearLayout.VERTICAL);
            c2.addView(l2);
            f2.addView(c2);
            l.addView(f2);
            TextView tv2 = createTextView(planh.date + " für "+clas+":", 20, inflater, l2);
            tv2.setPadding(0, 0, 0, toPixels(8));
            tv2.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            if(list.size() == 0) {
                createTextView("Es fällt nichts für dich aus!", 14, inflater, l2);
            } else
                createTable(list, false, inflater, l2, planh);

            if(!planh.special.isEmpty()) {
                TextView tv3 = new TextView(getActivity());
                tv3.setText(Html.fromHtml("<b>Besondere Mitteilungen</b><br>" + planh.special));
                l2.addView(tv3);
            }

            list = planm.getAllForClass(clas);
            FrameLayout f4 = new FrameLayout(getActivity());
            f4.setPadding(toPixels(1.3f),toPixels(0.3f),toPixels(1.3f),toPixels(0.3f));
            CardView c4 = createCardView();
            LinearLayout l4 = new LinearLayout(getActivity());
            l4.setOrientation(LinearLayout.VERTICAL);
            c4.addView(l4);
            f4.addView(c4);
            l.addView(f4);
            TextView tv4 = createTextView(planm.date + " für "+clas+":", 20, inflater, l4);
            tv4.setPadding(0, 0, 0, toPixels(8));
            tv4.setTextColor(getResources().getColor(android.R.color.primary_text_light));
            if(list.size() == 0) {
                createTextView("Es fällt nichts für dich aus!", 14, inflater, l4);
            } else
                createTable(list, false, inflater, l4, planm);

            if(!planm.special.isEmpty()) {
                TextView tv5 = new TextView(getActivity());
                tv5.setText(Html.fromHtml("<b>Besondere Mitteilungen</b><br>" + planm.special));
                l4.addView(tv5);
            }

        } else if(type == TYPE_OVERVIEW && planh.throwable == null && planm.throwable == null) {
            //Keine Klasse
            createButtonWithText(l, "Du musst eine Klasse wählen!", "Einstellungen", new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getActivity(), SettingsActivity.class);
                    getActivity().startActivityForResult(i, 1);
                }
            });


        } else if((type == TYPE_OVERVIEW && (planm.throwable != null || planh.throwable != null)) || (plan != null && plan.throwable != null)) {
            //Irgendein Error
            LinearLayout.LayoutParams lparams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            boolean b = planm.throwable != null && planm.throwable instanceof VPUrlFileException;
            if(!b)
                createButtonWithText(l, "Verbindung überprüfen und wiederholen", "Nochmal", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GGApp.GG_APP.refreshAsync(null, true);
                    }
                });
            else
                createButtonWithText(l, "Du musst dich anmelden!", "Anmelden", new View.OnClickListener() {
                    @Override
                    public void onClick(View c) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        AlertDialog dialog;
                        builder.setTitle("Login");
                        builder.setView(inflater.inflate(R.layout.login_dialog, null));

                        builder.setPositiveButton("Einloggen", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(final DialogInterface dialog, int which) {
                                new AsyncTask<Integer, Integer, Integer>() {

                                    @Override
                                    public void onProgressUpdate(Integer... values) {
                                        if (values.length == 0)
                                            return;
                                        if (values[0] == 10)
                                            GGApp.GG_APP.showToast("Benutzername oder Passwort falsch");
                                        else if (values[0] == 20)
                                            GGApp.GG_APP.showToast("Konnte keine Verbindung zum Anmeldeserver herstellen");
                                        else if (values[0] == 30)
                                            GGApp.GG_APP.showToast("Unbekannter Fehler bei der Anmeldung");
                                    }

                                    @Override
                                    protected Integer doInBackground(Integer... params) {
                                        String user = ((EditText) ((Dialog) dialog).findViewById(R.id.usernameInput)).getText().toString();
                                        String pass = ((EditText) ((Dialog) dialog).findViewById(R.id.passwordInput)).getText().toString();
                                        try {
                                            TrustManager[] trustAllCerts = new TrustManager[]{ new X509TrustManager() {
                                                    @Override
                                                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                                        return null;
                                                    }

                                                    @Override
                                                    public void checkClientTrusted(X509Certificate[] certs, String authType) {

                                                    }

                                                    @Override
                                                    public void checkServerTrusted(X509Certificate[] certs, String authType) {

                                                    }
                                                }
                                            };
                                            SSLContext sc = SSLContext.getInstance("TLS");
                                            sc.init(null, trustAllCerts, new java.security.SecureRandom());
                                            HostnameVerifier hv = new HostnameVerifier() {
                                                @Override
                                                public boolean verify(String hostname, SSLSession session) {
                                                    return true;
                                                }

                                                ;
                                            };

                                            HttpsURLConnection con = (HttpsURLConnection) new URL("https://gebatzens.de/api/getgg.php").openConnection();
                                            con.setRequestMethod("POST");

                                            con.setSSLSocketFactory(sc.getSocketFactory());
                                            con.setHostnameVerifier(hv);

                                            con.setDoOutput(true);
                                            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                                            wr.writeBytes("user=" + user + "&pw=" + pass);
                                            wr.flush();
                                            wr.close();

                                            int resp = con.getResponseCode();
                                            if (resp == 200) {
                                                Scanner scan = new Scanner(new BufferedInputStream(con.getInputStream()));
                                                String data = "";
                                                while (scan.hasNextLine())
                                                    data += scan.nextLine() + "\n";
                                                scan.close();

                                                Writer out = new OutputStreamWriter(getActivity().openFileOutput("ggsec.conf", Context.MODE_PRIVATE));
                                                out.write(data);
                                                out.flush();
                                                out.close();

                                                if (!GGApp.GG_APP.loadURLFile())
                                                    publishProgress(30);
                                                else {
                                                    GGApp.GG_APP.recreateProvider();
                                                    getActivity().runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            ((MainActivity) getActivity()).mContent.mGGFrag.setFragmentsLoading();
                                                        }
                                                    });
                                                    GGApp.GG_APP.refreshAsync(null, true);
                                                }


                                            } else {
                                                publishProgress(10);

                                            }


                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            if (e instanceof IOException)
                                                publishProgress(20);
                                            else
                                                publishProgress(30);

                                        }
                                        return null;
                                    }
                                }.execute();
                                dialog.dismiss();
                            }
                        });


                        builder.setNegativeButton("Abbrechen", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        dialog = builder.create();
                        dialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                        dialog.show();

                    }
                });
        } else {

            FrameLayout f6s = new FrameLayout(getActivity());
            f6s.setPadding(toPixels(1.3f),toPixels(0.3f),toPixels(1.3f),toPixels(0.3f));
            CardView c6s = createCardView();
            createTextView("Stand: " + plan.loadDate, 15, inflater, c6s);
            f6s.addView(c6s);
            l.addView(f6s);

            if(!plan.special.isEmpty()) {
                FrameLayout f6 = new FrameLayout(getActivity());
                f6.setPadding(toPixels(1.3f),toPixels(0.3f),toPixels(1.3f),toPixels(0.3f));
                CardView c6 = createCardView();
                LinearLayout l6 = new LinearLayout(getActivity());
                l6.setOrientation(LinearLayout.VERTICAL);
                c6.addView(l6);
                f6.addView(c6);
                l.addView(f6);
                TextView tv6 = new TextView(getActivity());
                tv6.setText(Html.fromHtml("<b>Besondere Mitteilungen</b><br>" + plan.special));
                l6.addView(tv6);
            }

            FrameLayout f7 = new FrameLayout(getActivity());
            f7.setPadding(toPixels(1.3f),toPixels(0.3f),toPixels(1.3f),toPixels(0.3f));
            CardView c7 = createCardView();
            LinearLayout l7 = new LinearLayout(getActivity());
            l7.setOrientation(LinearLayout.VERTICAL);
            c7.addView(l7);
            f7.addView(c7);
            l.addView(f7);

            if(plan.entries.size() == 0)
                createTextView("Keine Einträge!", 15, inflater, l7);
            else
                createTable(plan.entries, true, inflater, l7, plan);
        }
        sv.addView(l);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup group, Bundle bundle) {
        LinearLayout l = new LinearLayout(getActivity());
        l.setOrientation(LinearLayout.VERTICAL);
        if(GGApp.GG_APP.mVPToday != null && GGApp.GG_APP.mVPTomorrow != null)
            createView(inflater, l);
        return l;
    }

    @Override
    public void onViewCreated(View v, Bundle b) {
        super.onViewCreated(v, b);

        if(GGApp.GG_APP.mVPToday == null || GGApp.GG_APP.mVPTomorrow == null) {
            ((ViewGroup) getView()).addView(createLoadingView());
        }
    }

}
