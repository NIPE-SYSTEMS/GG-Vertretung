/*
 * Copyright (C) 2015 Fabian Schultis
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

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextClock;
import android.widget.TextView;
import android.widget.Toast;

import javax.net.ssl.HttpsURLConnection;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class HelpdeskActivity extends Activity {

    Toolbar mToolBar;

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_helpdesk);

        setTheme(GGApp.GG_APP.provider.getTheme());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            GGApp.GG_APP.setStatusBarColor(getWindow());
        }

        mToolBar = (Toolbar) findViewById(R.id.toolbar);
        mToolBar.setBackgroundColor(GGApp.GG_APP.provider.getColor());
        mToolBar.setTitleTextColor(Color.WHITE);
        mToolBar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        mToolBar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mToolBar.setTitle(getTitle());

        final TextView mTextViewName = (TextView) findViewById(R.id.reportName);
        final TextView mTextViewEmail = (TextView) findViewById(R.id.reportEmail);
        final TextView mTextViewSubject = (TextView) findViewById(R.id.reportSubject);
        final TextView mTextViewMessage = (TextView) findViewById(R.id.reportMessage);

        if(!GGApp.GG_APP.provider.getUsername().equals("")) {
            mTextViewName.setText(GGApp.GG_APP.provider.getUsername());
            mTextViewName.setEnabled(false);
        }

        Button mButtonSubmit = (Button) findViewById(R.id.reportSubmit);
        mButtonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mTextViewName.getText().toString();
                String email = mTextViewEmail.getText().toString();
                String subject = mTextViewSubject.getText().toString();
                String message = mTextViewMessage.getText().toString();

                new AsyncTask<String, Integer, Integer>() {
                    @Override
                    protected Integer doInBackground(String... params) {
                        try {
                            if((params[0]!=null)&&(params[1]!=null)&&(params[2]!=null)&&(params[3]!=null)&&!params[0].equals("")&&!params[1].equals("")&&!params[2].equals("")&&!params[3].equals("")) {
                                HttpsURLConnection con = (HttpsURLConnection) new URL("https://gymnasium-glinde.logoip.de/infoapp/infoapp_helpdesk.php").openConnection();

                                con.setRequestMethod("POST");
                                con.setSSLSocketFactory(GGProvider.sslSocketFactory);

                                con.setDoOutput(true);
                                DataOutputStream wr = new DataOutputStream(con.getOutputStream());
                                String urlParams = "name=" + URLEncoder.encode(params[0],"UTF-8") + "&email=" + URLEncoder.encode(params[1],"UTF-8") + "&subject=" + URLEncoder.encode(params[2],"UTF-8") + "&message=" + URLEncoder.encode(params[3],"UTF-8");
                                Log.d("urlParams", urlParams);
                                wr.writeBytes(urlParams);
                                wr.flush ();
                                wr.close ();

                                if(con.getResponseCode()==200) {
                                    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
                                    String line;
                                    StringBuilder sb = new StringBuilder();
                                    while((line = br.readLine()) != null) {
                                        sb.append(line);
                                    }
                                    br.close();
                                    if(sb.toString().contains("<state>true</state>")) {
                                        return 0;
                                    } else {
                                        return 2;
                                    }
                                } else {
                                    return 2;
                                }

                            } else {
                                return 1;
                            }
                        } catch(MalformedURLException e) {
                            e.printStackTrace();
                            return 2;
                        } catch(IOException e) {
                            e.printStackTrace();
                            return 2;
                        }
                    }

                    @Override
                    protected void onPostExecute(Integer result) {
                        if(result==0) {
                            Toast.makeText(getApplicationContext(),"Message sent successfully",Toast.LENGTH_LONG).show();
                            finish();
                        } else if(result==1) {
                            Toast.makeText(getApplicationContext(),"Please fill out all inputs",Toast.LENGTH_LONG).show();
                        } else if(result==2) {
                            Toast.makeText(getApplicationContext(),"Error while sending message",Toast.LENGTH_LONG).show();
                        }
                    }
                }.execute(name,email,subject,message);
            }
        });
    }

    @Override
    public void onBackPressed() {
        finish();
    }
}
