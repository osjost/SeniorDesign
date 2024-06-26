package com.example.antemulator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Text Initialization
        TextView devicePairing = (TextView)findViewById(R.id.textView);
        devicePairing.setText("Pair Wireless Sensor");

        // Button Initialization
        connectButton = (Button) findViewById(R.id.button);

        // Button Listener
        connectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Begin Pairing Mode
                Toast.makeText(MainActivity.this, "Pairing", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(MainActivity.this, Activity_AsyncScanHeartRateSampler.class));
            }
        });
    }


}