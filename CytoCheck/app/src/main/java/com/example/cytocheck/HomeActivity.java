package com.example.cytocheck;


import android.content.Intent;
import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import api.*;

public class HomeActivity extends AppCompatActivity {
    private String linkString;
    private SeekBar healthSlideBar;
    private ImageView likertImage;
    private TextView userScore;
    private SeekBar painSlideBar;
    private TextView userPain;
    private SeekBar nauseaSlideBar;
    private TextView userNausea;
    private TextView welcomeLabel;
    private String token;
    private String userID;
    private String providerID;
    private String firstName;
    private boolean fatigueReady = false;
    private String fatigueFinal = "10";
    private String painFinal = "10";
    private String nauseaFinal = "10";
    private String rashFinal = "N/A";
    private String extraFinal= "N/A";
    private boolean painReady = false;
    private boolean nauseaReady = false;
    private boolean rashReady = false;
    private boolean extraReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home); // Create a new XML layout for this activity if needed
        Intent intent = getIntent();
        linkString = intent.getStringExtra("linkString");
        token = intent.getStringExtra("token");
        userID = intent.getStringExtra("userID");
        firstName = intent.getStringExtra("firstName");
        welcomeLabel = findViewById(R.id.welcomeLabel);
        welcomeLabel.setText("Welcome, " + firstName);
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
                userScore.setText((10-progress) + "/10");
                fatigueFinal = String.valueOf(10-progress);
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
                userPain.setText((10-progress) + "/10");
                painFinal = String.valueOf(10-progress);
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
                userNausea.setText((10-progress) + "/10");
                nauseaFinal = String.valueOf(10-progress);
            }
            @Override
            public void onStartTrackingTouch(SeekBar nauseaSlideBar) { //Called when user Starts touch
            }
            @Override
            public void onStopTrackingTouch(SeekBar nauseaSlideBar) { //Called when user is done
            }
        });

        ImageView myImageView = findViewById(R.id.likert);
        ImageView myImageView2 = findViewById(R.id.likert2);
        ImageView myImageView3 = findViewById(R.id.likert3);

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
                fatigueReady = true;
                checkAll();
            }
        });

        Button painProcess = findViewById(R.id.surveySubmit2); //Signup functionality
        painProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                painSlideBar.setVisibility(View.GONE);
                painProcess.setVisibility(View.GONE);
                myImageView2.setVisibility(View.GONE);
                if (userPain.getText().equals("")) {
                    userPain.setText("Score Submitted: 0/10");
                }
                else {
                    userPain.setText("Score Submitted: " + userPain.getText());
                }
                painReady = true;
                checkAll();
            }
        });
        Button nauseaProcess = findViewById(R.id.surveySubmit3); //Signup functionality
        nauseaProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nauseaSlideBar.setVisibility(View.GONE);
                nauseaProcess.setVisibility(View.GONE);
                myImageView3.setVisibility(View.GONE);
                if (userNausea.getText().equals("")) {
                    userNausea.setText("Score Submitted: 0/10");
                }
                else {
                    userNausea.setText("Score Submitted: " + userNausea.getText());
                }
                nauseaReady = true;
                checkAll();

            }
        });

        Button associationView = findViewById(R.id.association_view);
        associationView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setContentView(R.layout.association_layout);

                Button deleteAssociation = findViewById(R.id.deleteCurrent);
                Button createAssociation = findViewById(R.id.submitNewID);
                Button patientReturn = findViewById(R.id.patientReturn);

                TextView currentLabel = findViewById(R.id.currentProvider);
                TextView newLabel = findViewById(R.id.newProvider);

                EditText newProviderID = findViewById(R.id.newProviderID);

                // Make the Text views and buttons initially invisible
                currentLabel.setVisibility(View.INVISIBLE);
                deleteAssociation.setVisibility(View.INVISIBLE);

                createAssociation.setVisibility(View.INVISIBLE);
                newLabel.setVisibility(View.INVISIBLE);
                newProviderID.setVisibility(View.INVISIBLE);
                // Get any associations
                api global = api.getInstance();
                String patientCurrent = linkString + "associations/" + userID;
                global.sendGetRequestWithHandlerWithToken(patientCurrent, token, new HandlerResponse() {
                    @Override
                    public void handleResponse(String response) {
                        Log.d("associations", response);
                        try {
                            JSONArray responseInfo = new JSONArray(response);
                            JSONObject providerInfo = responseInfo.getJSONObject(0);
                            Log.d("providerInfo", providerInfo.toString());
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (providerInfo.has("provider_id")) {
                                            providerID = providerInfo.getString("provider_id");
                                            currentLabel.setText(currentLabel.getText() + " " + providerInfo.getString("provider_id"));
                                            currentLabel.setVisibility(View.VISIBLE);
                                            deleteAssociation.setVisibility(View.VISIBLE);
                                        } else {
                                            createAssociation.setVisibility(View.VISIBLE);
                                            newLabel.setVisibility(View.VISIBLE);
                                            newProviderID.setVisibility(View.VISIBLE);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                        } catch (JSONException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    createAssociation.setVisibility(View.VISIBLE);
                                    newLabel.setVisibility(View.VISIBLE);
                                    newProviderID.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }
                });

                deleteAssociation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        api global = api.getInstance();
                        String deleteCurrent = linkString + "associations/" + userID;
                        global.sendDeleteRequestWithTokenWithHandler(deleteCurrent,token, new HandlerResponse(){
                            @Override
                            public void handleResponse(String response) {
                                Intent homeIntent = new Intent(HomeActivity.this, HomeActivity.class);
                                homeIntent.putExtra("linkString", linkString);
                                homeIntent.putExtra("token", token);
                                homeIntent.putExtra("userID", userID);
                                homeIntent.putExtra("firstName", firstName);

                                // Start the HomeActivity
                                startActivity(homeIntent);
                            }
                        });


                    }
                });

                createAssociation.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        api global = api.getInstance();
                        JSONObject requestHolder = new JSONObject();
                        try {
                            requestHolder.put("provider_id",String.valueOf(newProviderID.getText()));
                            requestHolder.put("message",firstName);
                            requestHolder.put("message_type","association_request");
                            requestHolder.put("sender_id",userID);
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String associationString = linkString + "inbox/associations";
                        global.sendPostRequestWithHandlerWithToken(associationString, requestHolder, token, new HandlerResponse() {
                            @Override
                            public void handleResponse(String response) {

                            }
                        });
                        Toast.makeText(HomeActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();
                    }
                });

                patientReturn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Create an intent to start the HomeActivity
                        Intent homeIntent = new Intent(HomeActivity.this, HomeActivity.class);
                        homeIntent.putExtra("linkString", linkString);
                        homeIntent.putExtra("token", token);
                        homeIntent.putExtra("userID", userID);
                        homeIntent.putExtra("firstName", firstName);

                        // Start the HomeActivity
                        startActivity(homeIntent);
                    }

                });

            }
        });


        //MVP2 Warning
//        Button sendWarning = findViewById(R.id.warningButton);
//        sendWarning.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {}
//
//        });
        Button sensorButton = findViewById(R.id.sensorButton);
        sensorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(HomeActivity.this, SensorActivity.class);
                intent.putExtra("linkString", linkString);
                intent.putExtra("token", token);
                intent.putExtra("userID", userID);
                startActivity(intent);
            }

        });

        Button rashButton = findViewById(R.id.rashButton);
        rashButton.setOnClickListener(v -> showCustomDialog());

        Button otherButton = findViewById(R.id.symptomExtra);
        otherButton.setOnClickListener(v -> showCustomOtherDialog());


    }
    public void checkAll() {
        if (fatigueReady && painReady && nauseaReady && rashReady && extraReady) {
            api global = api.getInstance();
            JSONObject qualData = new JSONObject();
            try {
                qualData.put("fatigue", fatigueFinal);
                qualData.put("user_id", userID);
                qualData.put("pain", painFinal);
                qualData.put("nausea", nauseaFinal);
                qualData.put("rash", rashFinal);
                qualData.put("other", extraFinal);
            }
            catch (JSONException e) {

            }
            String qualDataString = linkString + "qualitative";
            global.sendPostRequestWithHandlerWithToken(qualDataString, qualData, token, new HandlerResponse() {
                @Override
                public void handleResponse(String response) {

                }
            });
        }


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
                .setTitle("Rash Survey")
                .setPositiveButton("Submit", (dialog, which) -> {
                    // Handle the OK button click
                    rashReady = true;
                    String userInput = editText.getText().toString();
                    int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();
                    RadioButton selectedRadioButton = dialogView.findViewById(selectedRadioButtonId);

                    if (selectedRadioButton != null) {
                        String radioButtonValue = selectedRadioButton.getText().toString();
                        if (radioButtonValue.equals("Yes")) {
                            rashFinal = "Yes: " + userInput;
                        }
                        else {
                            rashFinal = "No: " + userInput;
                        }
                    }
                    checkAll();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Handle the Cancel button click
                });

        // Show the AlertDialog
        builder.create().show();
    }
    public void showCustomOtherDialog() {
        // Create a LayoutInflater instance
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.custom_dialog, null);

        // Get references to views in the custom dialog layout
        EditText editText = dialogView.findViewById(R.id.editText);
        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);
        //editText.setHint("Symptom Description");
        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView)
                .setTitle("Other Symptoms Survey")
                .setPositiveButton("Submit", (dialog, which) -> {
                    // Handle the OK button click
                    extraReady = true;
                    String userInput = editText.getText().toString();
                    int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();
                    RadioButton selectedRadioButton = dialogView.findViewById(selectedRadioButtonId);

                    if (selectedRadioButton != null) {
                        String radioButtonValue = selectedRadioButton.getText().toString();
                        if (radioButtonValue.equals("Yes")) {
                            extraFinal = "Yes: " + userInput;
                        }
                        else {
                            extraFinal = "No: " + userInput;
                        }
                    }
                    checkAll();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Handle the Cancel button click
                });

        // Show the AlertDialog
        builder.create().show();
    }

}
