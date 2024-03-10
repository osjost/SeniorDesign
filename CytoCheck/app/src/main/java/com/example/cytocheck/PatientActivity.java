package com.example.cytocheck;


import android.content.Intent;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cytocheck.DateAxisValueFormatter;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import api.*;

public class PatientActivity extends AppCompatActivity {
    private LineChart userDataChart;
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
        setContentView(R.layout.activity_patient); // Create a new XML layout for this activity if needed
        Intent intent = getIntent();
        linkString = intent.getStringExtra("linkString");
        token = intent.getStringExtra("token");
        userID = intent.getStringExtra("userID");
        firstName = intent.getStringExtra("firstName");

        // Initialize Variables for view elements
        welcomeLabel = findViewById(R.id.welcomeLabel);
        welcomeLabel.setText("Welcome, " + firstName);

        userScore = findViewById(R.id.userScoreField);
        healthSlideBar = findViewById(R.id.healthSlider);

        likertImage = findViewById(R.id.likert);
        ImageView myImageView = findViewById(R.id.likert);
        ImageView myImageView2 = findViewById(R.id.likert2);
        ImageView myImageView3 = findViewById(R.id.likert3);

        // Set the image resource
        myImageView.setImageResource(R.drawable.likert);

        painSlideBar = findViewById(R.id.healthSlider2);
        userPain = findViewById(R.id.userScoreField2);

        nauseaSlideBar = findViewById(R.id.healthSlider3);
        userNausea = findViewById(R.id.userScoreField3);

        userDataChart = findViewById(R.id.userData);

        api global = api.getInstance();
        String patientAddress = linkString + "qualitative/" + userID;
        global.sendGetRequestWithHandlerWithToken(patientAddress, token, new HandlerResponse() {
            @Override
            public void handleResponse(String response) {
                try {
                    JSONArray jsonArray = new JSONArray(response);
                    ArrayList<Entry> painEntries = new ArrayList<>();
                    ArrayList<Entry> fatigueEntries = new ArrayList<>();
                    ArrayList<Entry> nauseaEntries = new ArrayList<>();
                    ArrayList<String> timestamps = new ArrayList<>();

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());

                    // Filter data for all options (daily, weekly, and monthly)
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(System.currentTimeMillis());

                    // Daily data
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Date date = sdf.parse(jsonObject.getString("time_stamp"));
                        Calendar calData = Calendar.getInstance();
                        calData.setTime(date);
                        Calendar calToday = Calendar.getInstance();
                        if (calData.get(Calendar.YEAR) == calToday.get(Calendar.YEAR) &&
                                calData.get(Calendar.MONTH) == calToday.get(Calendar.MONTH) &&
                                calData.get(Calendar.DAY_OF_MONTH) == calToday.get(Calendar.DAY_OF_MONTH)) {
                            long millis = date.getTime();
                            timestamps.add(sdf.format(date));
                            painEntries.add(new Entry(millis, jsonObject.getInt("pain")));
                            fatigueEntries.add(new Entry(millis, jsonObject.getInt("fatigue")));
                            nauseaEntries.add(new Entry(millis, jsonObject.getInt("nausea")));
                        }
                    }

// Weekly data
                    Calendar calStartOfWeek = Calendar.getInstance();
                    calStartOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                    calStartOfWeek.set(Calendar.HOUR_OF_DAY, 0);
                    calStartOfWeek.set(Calendar.MINUTE, 0);
                    calStartOfWeek.set(Calendar.SECOND, 0);
                    calStartOfWeek.set(Calendar.MILLISECOND, 0);
                    Calendar calEndOfWeek = (Calendar) calStartOfWeek.clone();
                    calEndOfWeek.add(Calendar.DAY_OF_MONTH, 7);
                    calEndOfWeek.set(Calendar.HOUR_OF_DAY, 23);
                    calEndOfWeek.set(Calendar.MINUTE, 59);
                    calEndOfWeek.set(Calendar.SECOND, 59);
                    calEndOfWeek.set(Calendar.MILLISECOND, 999);
                    ArrayList<Entry> painEntriesWeekly = new ArrayList<>();
                    ArrayList<Entry> fatigueEntriesWeekly = new ArrayList<>();
                    ArrayList<Entry> nauseaEntriesWeekly = new ArrayList<>();
                    ArrayList<String> timestampsWeekly = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Date date = sdf.parse(jsonObject.getString("time_stamp"));
                        if (date.after(calStartOfWeek.getTime()) && date.before(calEndOfWeek.getTime())) {
                            long millis = date.getTime();
                            timestampsWeekly.add(sdf.format(date));
                            painEntriesWeekly.add(new Entry(millis, jsonObject.getInt("pain")));
                            fatigueEntriesWeekly.add(new Entry(millis, jsonObject.getInt("fatigue")));
                            nauseaEntriesWeekly.add(new Entry(millis, jsonObject.getInt("nausea")));
                        }
                    }

// Monthly data
                    Calendar calStartOfMonth = Calendar.getInstance();
                    calStartOfMonth.set(Calendar.DAY_OF_MONTH, 1);
                    calStartOfMonth.set(Calendar.HOUR_OF_DAY, 0);
                    calStartOfMonth.set(Calendar.MINUTE, 0);
                    calStartOfMonth.set(Calendar.SECOND, 0);
                    calStartOfMonth.set(Calendar.MILLISECOND, 0);
                    Calendar calEndOfMonth = (Calendar) calStartOfMonth.clone();
                    calEndOfMonth.add(Calendar.MONTH, 1);
                    calEndOfMonth.add(Calendar.DAY_OF_MONTH, -1);
                    calEndOfMonth.set(Calendar.HOUR_OF_DAY, 23);
                    calEndOfMonth.set(Calendar.MINUTE, 59);
                    calEndOfMonth.set(Calendar.SECOND, 59);
                    calEndOfMonth.set(Calendar.MILLISECOND, 999);
                    ArrayList<Entry> painEntriesMonthly = new ArrayList<>();
                    ArrayList<Entry> fatigueEntriesMonthly = new ArrayList<>();
                    ArrayList<Entry> nauseaEntriesMonthly = new ArrayList<>();
                    ArrayList<String> timestampsMonthly = new ArrayList<>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Date date = sdf.parse(jsonObject.getString("time_stamp"));
                        if (date.after(calStartOfMonth.getTime()) && date.before(calEndOfMonth.getTime())) {
                            long millis = date.getTime();
                            timestampsMonthly.add(sdf.format(date));
                            painEntriesMonthly.add(new Entry(millis, jsonObject.getInt("pain")));
                            fatigueEntriesMonthly.add(new Entry(millis, jsonObject.getInt("fatigue")));
                            nauseaEntriesMonthly.add(new Entry(millis, jsonObject.getInt("nausea")));
                        }
                    }


                    // Create LineDataSets for each option
                    LineDataSet painDataSet = new LineDataSet(painEntries, "Pain (Daily)");
                    LineDataSet fatigueDataSet = new LineDataSet(fatigueEntries, "Fatigue (Daily)");
                    LineDataSet nauseaDataSet = new LineDataSet(nauseaEntries, "Nausea (Daily)");

                    LineDataSet painDataSetWeekly = new LineDataSet(painEntriesWeekly, "Pain (Weekly)");
                    LineDataSet fatigueDataSetWeekly = new LineDataSet(fatigueEntriesWeekly, "Fatigue (Weekly)");
                    LineDataSet nauseaDataSetWeekly = new LineDataSet(nauseaEntriesWeekly, "Nausea (Weekly)");

                    LineDataSet painDataSetMonthly = new LineDataSet(painEntriesMonthly, "Pain (Monthly)");
                    LineDataSet fatigueDataSetMonthly = new LineDataSet(fatigueEntriesMonthly, "Fatigue (Monthly)");
                    LineDataSet nauseaDataSetMonthly = new LineDataSet(nauseaEntriesMonthly, "Nausea (Monthly)");

                    // Set colors for the data sets
                    painDataSet.setColor(Color.RED);
                    fatigueDataSet.setColor(Color.BLUE);
                    nauseaDataSet.setColor(Color.GREEN);

                    painDataSetWeekly.setColor(Color.RED);
                    fatigueDataSetWeekly.setColor(Color.BLUE);
                    nauseaDataSetWeekly.setColor(Color.GREEN);

                    painDataSetMonthly.setColor(Color.RED);
                    fatigueDataSetMonthly.setColor(Color.BLUE);
                    nauseaDataSetMonthly.setColor(Color.GREEN);

                    // Set line widths
                    painDataSet.setLineWidth(2f);
                    fatigueDataSet.setLineWidth(2f);
                    nauseaDataSet.setLineWidth(2f);

                    painDataSetWeekly.setLineWidth(2f);
                    fatigueDataSetWeekly.setLineWidth(2f);
                    nauseaDataSetWeekly.setLineWidth(2f);

                    painDataSetMonthly.setLineWidth(2f);
                    fatigueDataSetMonthly.setLineWidth(2f);
                    nauseaDataSetMonthly.setLineWidth(2f);

                    // Add data sets to the LineData object
                    LineData lineData = new LineData(painDataSet, fatigueDataSet, nauseaDataSet);
                    LineData lineDataWeekly = new LineData(painDataSetWeekly, fatigueDataSetWeekly, nauseaDataSetWeekly);
                    LineData lineDataMonthly = new LineData(painDataSetMonthly, fatigueDataSetMonthly, nauseaDataSetMonthly);

                    // Set initial data
                    userDataChart.setData(lineData);
                    userDataChart.invalidate();

                    // Add a selector to switch between data options
                    Spinner selectorSpinner = findViewById(R.id.selectorSpinner);
                    selectorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            DateAxisValueFormatter axisFormatter;
                            switch (position) {
                                case 0:
                                    axisFormatter = new DateAxisValueFormatter(timestamps, DateAxisValueFormatter.DAILY);
                                    userDataChart.setData(lineData);
                                    break;
                                case 1:
                                    axisFormatter = new DateAxisValueFormatter(timestampsWeekly, DateAxisValueFormatter.WEEKLY);
                                    userDataChart.setData(lineDataWeekly);
                                    break;
                                case 2:
                                    axisFormatter = new DateAxisValueFormatter(timestampsMonthly, DateAxisValueFormatter.MONTHLY);
                                    userDataChart.setData(lineDataMonthly);
                                    break;
                                default:
                                    axisFormatter = new DateAxisValueFormatter(timestamps, DateAxisValueFormatter.DAILY);
                                    userDataChart.setData(lineData);
                                    break;
                            }
                            XAxis xAxis = userDataChart.getXAxis();
                            xAxis.setValueFormatter(axisFormatter);
                            userDataChart.invalidate();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                            // Handle nothing selected
                        }
                    });


                } catch (JSONException e) {
                    Log.e("ERROR", "Error parsing JSON: " + e.getMessage());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        // On click/change Listeners for elements
        healthSlideBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar healthSlideBar, int progress, boolean fromUser) {
                // 'progress' contains the new value of the SeekBar
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

        Button loginButton = findViewById(R.id.logOut); //Logout Functionality
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(PatientActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        Button surveyProcess = findViewById(R.id.surveySubmit); //Submit functionality
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

        Button painProcess = findViewById(R.id.surveySubmit2); //Submit functionality
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
        Button nauseaProcess = findViewById(R.id.surveySubmit3); //Submit functionality
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
                // This instantiates the Association view
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
                                Intent homeIntent = new Intent(PatientActivity.this, PatientActivity.class);
                                homeIntent.putExtra("linkString", linkString);
                                homeIntent.putExtra("token", token);
                                homeIntent.putExtra("userID", userID);
                                homeIntent.putExtra("firstName", firstName);

                                // Start the PatientActivity
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
                            requestHolder.put("message",userID);
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
                        Toast.makeText(PatientActivity.this, "Request Sent", Toast.LENGTH_SHORT).show();
                    }
                });

                patientReturn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Create an intent to start the PatientActivity
                        Intent homeIntent = new Intent(PatientActivity.this, PatientActivity.class);
                        homeIntent.putExtra("linkString", linkString);
                        homeIntent.putExtra("token", token);
                        homeIntent.putExtra("userID", userID);
                        homeIntent.putExtra("firstName", firstName);

                        // Start the PatientActivity
                        startActivity(homeIntent);
                        finish();
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
                Intent intent = new Intent(PatientActivity.this, SensorActivity.class);
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