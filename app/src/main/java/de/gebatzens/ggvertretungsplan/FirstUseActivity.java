package de.gebatzens.ggvertretungsplan;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class FirstUseActivity extends Activity {
    Button nextStep;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_firstuse);

        nextStep = (Button) findViewById(R.id.nextStep);
        nextStep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View viewIn) {
                //save and switch setup page
            }
        });

    }

}