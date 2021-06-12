package com.example.myrun;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class GamingRunStopActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gaming_run_stop);

        Button btnRestart = findViewById(R.id.btnRestart);
        Button btnComplete = findViewById(R.id.btnComplete);

        btnRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GamingRunActivity.class);
                startActivity(intent);
                finish();
            }
        });
        btnComplete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), GamingEnd.class);
                startActivity(intent);
                finish();
            }
        });
    }
}