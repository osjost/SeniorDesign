package com.example.cytocheck;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import android.widget.Button;
import android.widget.TextView;

public class SensorActivity extends AppCompatActivity{
    String linkString;
    private Button connectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor);
        Intent intent = getIntent();
        linkString = intent.getStringExtra("linkString");
        String token = intent.getStringExtra("token");
        String userID = intent.getStringExtra("userID");
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
                Toast.makeText(SensorActivity.this, "Pairing", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(SensorActivity.this, Activity_AsyncScanHeartRateSampler.class);
                intent.putExtra("linkString", linkString);
                intent.putExtra("token", token);
                intent.putExtra("userID", userID);
                startActivity(intent);
                //startActivity(new Intent(SensorActivity.this, Activity_AsyncScanHeartRateSampler.class));
            }
        });
    }
}