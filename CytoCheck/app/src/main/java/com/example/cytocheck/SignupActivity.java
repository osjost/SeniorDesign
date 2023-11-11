package com.example.cytocheck;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.w3c.dom.Text;

public class SignupActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);


        Button homeButton = findViewById(R.id.returnButton); //Back to Log in screen
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Button createButton = findViewById(R.id.createAccount); //Create account functionality
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        RadioGroup radioHolder = findViewById(R.id.radioGroup);
        TextView referralLabel = findViewById(R.id.referralCodeLabel);
        TextView referralText = findViewById(R.id.referralCodeText);

        radioHolder.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Check which radio button was selected
                if (checkedId == R.id.providerSelector) {
                    // If the specific radio button is selected, hide the TextView
                    referralLabel.setVisibility(View.GONE);
                    referralText.setVisibility(View.GONE);
                } else {
                    // If other radio buttons are selected, make the TextView visible
                    referralLabel.setVisibility(View.VISIBLE);
                    referralText.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}
