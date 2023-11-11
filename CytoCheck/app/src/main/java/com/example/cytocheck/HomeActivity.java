package com.example.cytocheck;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    private SeekBar healthSlideBar;
    private TextView userScore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Create a new XML layout for this activity if needed

        userScore = findViewById(R.id.userScoreField);
        healthSlideBar = findViewById(R.id.healthSlider);
        healthSlideBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar healthSlideBar, int progress, boolean fromUser) {
                // 'progress' contains the new value of the SeekBar
                // 'fromUser' is true if the change was initiated by the user, false if it was programmatically set
                userScore.setText(progress + "/10");
            }
            @Override
            public void onStartTrackingTouch(SeekBar healthSlideBar) { //Called when user Starts touch
            }
            @Override
            public void onStopTrackingTouch(SeekBar healthSlideBar) { //Called when user is done
            }
        });
        Button loginButton = findViewById(R.id.logOut); //Login Functionality
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Button surveyProcess = findViewById(R.id.surveySubmit); //Signup functionality
        surveyProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                healthSlideBar.setVisibility(View.GONE);
                surveyProcess.setVisibility(View.GONE);
                userScore.setText("Score Submitted: " + userScore.getText());
            }
        });


    }

}
