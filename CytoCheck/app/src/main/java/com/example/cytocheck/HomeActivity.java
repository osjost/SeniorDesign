package com.example.cytocheck;
import android.content.Intent;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {
    private SeekBar healthSlideBar;
    private ImageView likertImage;
    private TextView userScore;
    private SeekBar painSlideBar;
    private TextView userPain;
    private SeekBar nauseaSlideBar;
    private TextView userNausea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Create a new XML layout for this activity if needed

        userScore = findViewById(R.id.userScoreField);
        healthSlideBar = findViewById(R.id.healthSlider);
        likertImage = findViewById(R.id.likert);

        painSlideBar = findViewById(R.id.healthSlider2);
        userPain = findViewById(R.id.userScoreField2);

        nauseaSlideBar = findViewById(R.id.healthSlider3);
        userNausea = findViewById(R.id.userScoreField3);
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
        painSlideBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar painSlideBar, int progress, boolean fromUser) {
                // 'progress' contains the new value of the SeekBar
                // 'fromUser' is true if the change was initiated by the user, false if it was programmatically set
                userPain.setText(progress + "/10");
            }
            @Override
            public void onStartTrackingTouch(SeekBar painSlideBar) { //Called when user Starts touch
            }
            @Override
            public void onStopTrackingTouch(SeekBar painSlideBar) { //Called when user is done
            }
        });
        nauseaSlideBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar nauseaSlideBar, int progress, boolean fromUser) {
                // 'progress' contains the new value of the SeekBar
                // 'fromUser' is true if the change was initiated by the user, false if it was programmatically set
                userNausea.setText(progress + "/10");
            }
            @Override
            public void onStartTrackingTouch(SeekBar nauseaSlideBar) { //Called when user Starts touch
            }
            @Override
            public void onStopTrackingTouch(SeekBar nauseaSlideBar) { //Called when user is done
            }
        });

        ImageView myImageView = findViewById(R.id.likert);

        // Set the image resource programmatically (optional if you've set it in XML)
         myImageView.setImageResource(R.drawable.likert);
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
                likertImage.setVisibility(View.GONE);
                if (userScore.getText().equals("")) {
                    userScore.setText("Score Submitted: 0/10");
                }
                else {
                    userScore.setText("Score Submitted: " + userScore.getText());
                }

            }
        });

        Button painProcess = findViewById(R.id.surveySubmit2); //Signup functionality
        painProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                painSlideBar.setVisibility(View.GONE);
                painProcess.setVisibility(View.GONE);

                if (userPain.getText().equals("")) {
                    userPain.setText("Score Submitted: 0/10");
                }
                else {
                    userPain.setText("Score Submitted: " + userPain.getText());
                }

            }
        });
        Button nauseaProcess = findViewById(R.id.surveySubmit3); //Signup functionality
        nauseaProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nauseaSlideBar.setVisibility(View.GONE);
                nauseaProcess.setVisibility(View.GONE);

                if (userNausea.getText().equals("")) {
                    userNausea.setText("Score Submitted: 0/10");
                }
                else {
                    userNausea.setText("Score Submitted: " + userNausea.getText());
                }

            }
        });
        Button extraSymptoms = findViewById(R.id.symptomExtra);
        extraSymptoms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {}

        });
        Button sendWarning = findViewById(R.id.warningButton);
        sendWarning.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {}

        });
        Button sensorButton = findViewById(R.id.sensorButton);
        sensorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {}

        });

        Button rashButton = findViewById(R.id.rashButton);
        rashButton.setOnClickListener(v -> showCustomDialog());


    }

    public void showCustomDialog() {
        // Create a LayoutInflater instance
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.custom_dialog, null);

        // Get references to views in the custom dialog layout
        EditText editText = dialogView.findViewById(R.id.editText);
        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);

        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Custom Dialog")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Handle the OK button click
                    String userInput = editText.getText().toString();
                    int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();
                    RadioButton selectedRadioButton = dialogView.findViewById(selectedRadioButtonId);

                    if (selectedRadioButton != null) {
                        String radioButtonValue = selectedRadioButton.getText().toString();
                        // Handle the user input and radio button selection
                        // ...
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Handle the Cancel button click
                });

        // Show the AlertDialog
        builder.create().show();
    }

}
