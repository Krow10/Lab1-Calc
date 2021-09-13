package com.example.lab1_calc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class CalcSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calc_settings);

        // Attach the settings fragment to the view for display
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings_container, new CalcSettingsFragment())
                .commit();

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_settings_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() { // Setup back arrow action
            @Override
            public void onClick(View v) {
                // Send result back to MainActivity for triggering input/output clearing
                Intent result = new Intent();
                result.putExtra("result", 0x1337);
                setResult(Activity.RESULT_OK, result);

                finish();
            }
        });
    }
}