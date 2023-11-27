package com.example.cytocheck;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.Button;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import api.*;

public class ProviderActivity extends AppCompatActivity {

    private Handler inactivityHandler;
    private PopupWindow currentPopup;
    private Runnable inactivityRunnable;
    private static final long INACTIVITY_TIMEOUT = 60000; // 60 seconds in milliseconds
    private TextView referralCode;
    private String token;
    private List<Integer> patientIds = new ArrayList<>();
    private List<String> patientNames = new ArrayList<>();
    private List<PatientInfo> patientList = new ArrayList<>();
    private int index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        token = intent.getStringExtra("token");
        String userID = intent.getStringExtra("userID");
        setContentView(R.layout.activity_provider);

        api global = api.getInstance();
        String associationAddress = "https://10.0.2.2:443/associations/" + userID;
        global.sendGetRequestWithHandlerWithToken(associationAddress, token, new HandlerResponse() {
            @Override
            public void handleResponse(String response) {
                Log.d("response", response);
                Pattern pattern = Pattern.compile("\"patient_id\":(\\d+)");
                Matcher matcher = pattern.matcher(response);

                // Find all matches
                while (matcher.find()) {
                    // Extract the value
                    int patientId = Integer.parseInt(matcher.group(1));

                    // Add the patient ID to the ArrayList
                    patientIds.add(patientId);
                }

                // Now initiate the second set of requests for patient details
                fetchPatientDetails(token);
            }
        });

        // Other UI setup code can go here...

        inactivityHandler = new Handler(Looper.getMainLooper());
        inactivityRunnable = new Runnable() {
            @Override
            public void run() {
                // Log out the user or perform any other action on inactivity timeout
                // For example, you can navigate to the login screen or finish the activity
                // For now, let's simply finish the activity
                Intent intent = new Intent(ProviderActivity.this, MainActivity.class);
                startActivity(intent);
            }
        };
        startInactivityTimer();

        Button homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProviderActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        referralCode = findViewById(R.id.referralCode);
        referralCode.setText("Referral Code: " + userID);
    }
    private void fetchPatientDetails(String token) {
        // Second set of asynchronous requests for patient details
        for (int i = 0; i < patientIds.size(); i++) {
            String httpsAddress = "https://10.0.2.2:443/users/" + patientIds.get(i);

            api global = api.getInstance();
            global.sendGetRequestWithHandlerWithToken(httpsAddress, token, new HandlerResponse() {
                @Override
                public void handleResponse(String response) {
                    try {
                        JSONObject userProfile = new JSONObject(response);
                        patientNames.add(userProfile.getString("first_name"));
                        patientList.add(new PatientInfo(userProfile.getInt("user_id"), userProfile.getString("first_name")));
                        // Check if all responses have been received
                        if (patientNames.size() == patientIds.size()) {
                            // All responses received, set up the UI
                            setupUI();
                        }
                    } catch (JSONException e) {

                    }
                }
            });
        }
    }

    private void setupUI() {


        // Populate patientList with PatientInfo objects (ID and Name)
//        for (int i = 0; i < patientIds.size(); i++) {
//            String patientName = patientNames.get(i);
//            int patientId = patientIds.get(i);
//            patientList.add(new PatientInfo(patientId, patientName));
//        }

        // Create the adapter and set it to the ListView
        PatientAdapter adapter = new PatientAdapter(this, patientList);
        ListView listViewPatients = findViewById(R.id.listViewPatients);
        listViewPatients.setAdapter(adapter);

        // Set item click listener
        listViewPatients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Handle item click here
                PatientInfo selectedPatient = (PatientInfo) parent.getItemAtPosition(position);
                int selectedPatientId = selectedPatient.getId();
                String selectedPatientName = selectedPatient.getName();

                // Do something with the selected patient ID and name
                // For example, show a popup with additional information
                showPopup(selectedPatientId, selectedPatientName);
            }
        });
    }

    private void showPopup(int patientId, String patientName) {
        PopupWindow popupWindow = new PopupWindow(this);

        // Inflate the layout for the popup window
        View popupView = getLayoutInflater().inflate(R.layout.popup_layout, null);

        // Set the content view of the popup window
        popupWindow.setContentView(popupView);

        // Set the width and height of the popup window
        popupWindow.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        popupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);

        // Find TextView and Button in the popup layout
        TextView popupText = popupView.findViewById(R.id.popupText);
        Button closeButton = popupView.findViewById(R.id.closeButton);

        api global = api.getInstance();

        global.sendGetRequestWithHandlerWithToken("https://10.0.2.2:443/qualatative/" + patientId, token, new HandlerResponse() {
            @Override
            public void handleResponse(String response) {

                try {
                    // Parse the response as a JSONArray
                    JSONArray patientsArray = new JSONArray(response);

                    // Iterate through the array to process each patient
                    for (int i = 0; i < patientsArray.length(); i++) {
                        JSONObject patientObtained = patientsArray.getJSONObject(i);

                        // Extract information for each patient
                        String fatigueString = "Fatigue: " + patientObtained.getInt("fatigue") + "/10" + "\n";
                        String painString = "Pain: " + patientObtained.getInt("pain") + "/10" + "\n";
                        String nauseaString = "Nausea: " + patientObtained.getInt("nausea") + "/10" + "\n";
                        String rashString = "Rash: " + patientObtained.getString("rash") + "\n";
                        String otherString = "Other: " + patientObtained.getString("other") + "\n";

                        // Combine the information for the current patient
                        String popupMessage = fatigueString + painString + nauseaString + rashString + otherString;

                        // Update UI with the information for the current patient
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                popupText.setText(popupMessage);
                            }
                        });
                    }
                } catch (JSONException e) {
                    Log.e("ERROR", "Error parsing JSON: " + e.getMessage());
                }


            }
        });
        // Set the text with patient information
        //String popupMessage = "Patient ID: " + patientId + "\nPatient Name: " + patientName;


        // Set close button click listener
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.dismiss(); // Close the popup when the close button is clicked
            }
        });

        // Show the popup window at a specific location on the screen
        popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Touch event detected anywhere on the screen, reset inactivity timer
        resetInactivityTimer();
        return super.onTouchEvent(event);
    }

    private void startInactivityTimer() {
        inactivityHandler.postDelayed(inactivityRunnable, INACTIVITY_TIMEOUT);
    }

    private void resetInactivityTimer() {
        // Remove the existing callbacks from the handler and start the timer again
        inactivityHandler.removeCallbacks(inactivityRunnable);
        startInactivityTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Start the inactivity timer again when the activity is resumed
        startInactivityTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Remove the callbacks to stop the inactivity timer when the activity is paused
        inactivityHandler.removeCallbacks(inactivityRunnable);
    }
}