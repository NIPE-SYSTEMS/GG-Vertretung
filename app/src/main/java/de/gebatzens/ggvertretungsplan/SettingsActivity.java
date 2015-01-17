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
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

public class SettingsActivity extends Activity {

    public static interface DialogInputListener {
        public void onInput(String in);
    }

    String[] mStrings = new String[] { "Klasse (GG): " + GGApp.GG_APP.getVPClass(0), "Klasse (SWS): " + GGApp.GG_APP.getVPClass(1), "Test"};
    ListView mList;
    boolean changed = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        if(savedInstanceState != null)
            changed = savedInstanceState.getBoolean("ggs_changed");

        Toolbar t = (Toolbar) findViewById(R.id.toolbar);
        t.setTitle("Einstellungen");
        t.setTitleTextColor(Color.WHITE);

        mList = (ListView) findViewById(R.id.settings_list);
        mList.setAdapter(new ArrayAdapter<String>(this, R.layout.drawer_list_item, mStrings));
        mList.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch(position) {
                    case 0:
                        showDialog(new DialogInputListener() {
                            @Override
                            public void onInput(String in) {
                                GGApp.GG_APP.setVPClass(0, in);
                                mStrings[0] = "Klasse (GG): " + in;
                                mList.setAdapter(new ArrayAdapter<String>(SettingsActivity.this, R.layout.drawer_list_item, mStrings));
                                mList.requestLayout();
                                changed = true;
                            }
                        });
                        break;
                    case 1:
                        showDialog(new DialogInputListener() {
                            @Override
                            public void onInput(String in) {
                                GGApp.GG_APP.setVPClass(1, in);
                                mStrings[1] = "Klasse (SWS): " + in;
                                mList.setAdapter(new ArrayAdapter<String>(SettingsActivity.this, R.layout.drawer_list_item, mStrings));
                                mList.requestLayout();
                                changed = true;
                            }
                        });
                        break;
                    case 2:
                        GGApp.GG_APP.showToast("Das hier ist ein TEST!");
                        mList.setItemChecked(2, false);
                }
            }
        });

    }

    public void showDialog(final DialogInputListener dil) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Klasse eingeben");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dil.onInput(input.getText().toString());
                mList.setItemChecked(0, false);
                mList.setItemChecked(1, false);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                mList.setItemChecked(0, false);
                mList.setItemChecked(1, false);
            }
        });

        builder.show();
    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        b.putBoolean("ggs_changed", changed);
    }

    public void buttonBack(View view) {
        Intent i = new Intent();
        setResult(changed ? RESULT_OK : RESULT_CANCELED, i);
        this.finish();
    }
}
