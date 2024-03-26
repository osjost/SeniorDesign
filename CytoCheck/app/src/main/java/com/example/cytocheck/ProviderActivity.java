package com.example.cytocheck;

import static com.example.cytocheck.DataProcessor.processData;
import static com.example.cytocheck.DataProcessor.processQuanData;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import api.*;

public class ProviderActivity extends AppCompatActivity {
    private BarChart userData;
    private BarChart userHRData;
    private BarChart userTempData;
    private LineChart userHRLine;
    private  LineChart userTempLine;
    private String userQualResponse;
    private String userHRResponse;
    private String userTempResponse;
    private Spinner mSpinner;
    private String linkString;
    private Handler inactivityHandler;
    private Runnable inactivityRunnable;
    private static final long INACTIVITY_TIMEOUT = 60000; // 60 seconds in milliseconds
    private TextView referralCode;
    private String token;
    private String userID;
    private String notifToken;
    private String qualitativeDetails;
    private List<Integer> patientIds = new ArrayList<>();
    private List<Integer> inboxIds = new ArrayList<>();
    private List<String> patientNames = new ArrayList<>();
    private List<PatientInfo> patientList = new ArrayList<>();
    private List<RequestInfo> requestList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        linkString = intent.getStringExtra("linkString");
        token = intent.getStringExtra("token");
        userID = intent.getStringExtra("userID");
        notifToken = intent.getStringExtra("notificationToken");
        setContentView(R.layout.activity_provider);

        api global = api.getInstance();

        String notifAddress = linkString + "fcc";
        JSONObject userObject = new JSONObject();
        try {
            userObject.put("user_id", userID);
            userObject.put("fcc", notifToken);
        } catch (JSONException e) {

        }
        global.sendPostRequestWithHandlerWithToken(notifAddress, userObject, token, new HandlerResponse() {
            @Override
            public void handleResponse(String response) {

            }
        });


        String associationAddress = linkString + "associations/" + userID;
        global.sendGetRequestWithHandlerWithToken(associationAddress, token, new HandlerResponse() {
            @Override
            public void handleResponse(String response) {
                Log.d("response", response);
                try {
                    JSONArray jsonArray = new JSONArray(response);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        int patientId = jsonObject.getInt("patient_id");
                        patientIds.add(patientId);
                    }

                    // Now initiate the second set of requests for patient details
                    fetchPatientDetails(token);
                } catch (JSONException e) {
                    Log.e("Error", "Error parsing JSON: " + e.getMessage());
                }
            }
        });

        // prototype for handling populating of inbox list
        String inboxAddress = linkString + "inbox/" + userID;
        global.sendGetRequestWithHandlerWithToken(inboxAddress, token, new HandlerResponse() {
            @Override
            public void handleResponse(String response) {
                Log.d("response", response);

                try {
                    JSONArray jsonArray = new JSONArray(response);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        int messageId = jsonObject.getInt("message_id");
                        String message = jsonObject.getString("message");
                        String messageType = jsonObject.getString("message_type");
                        int senderId = jsonObject.getInt("sender_id");

                        // Create a RequestInfo object and add it to the requestList
                        requestList.add(new RequestInfo(messageId, message, messageType, senderId));
                    }

                    setupInboxUI();
                } catch (JSONException e) {
                    Log.e("Error", "Error parsing JSON: " + e.getMessage());
                }
            }
        });


        inactivityHandler = new Handler(Looper.getMainLooper());
        inactivityRunnable = new Runnable() {
            @Override
            public void run() {
                // Log out the user on inactivity timeout
                Intent intent = new Intent(ProviderActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        };
        startInactivityTimer();

        Button homeButton = findViewById(R.id.homeButton);
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ProviderActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });

        referralCode = findViewById(R.id.referralCode);
        referralCode.setText("Referral Code: " + userID);
    }
    private void fetchPatientDetails(String token) {
        // Second set of asynchronous requests for patient details

        for (int i = 0; i < patientIds.size(); i++) {
            String httpsAddress = linkString + "users/" + patientIds.get(i);

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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Create the adapter and set it to the ListView
                PatientAdapter adapter = new PatientAdapter(ProviderActivity.this, patientList);
                ListView listViewPatients = findViewById(R.id.listViewPatients);
                listViewPatients.setAdapter(adapter);

                // Set item click listener for each patient
                listViewPatients.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        PatientInfo selectedPatient = (PatientInfo) parent.getItemAtPosition(position);
                        int selectedPatientId = selectedPatient.getId();
                        String selectedPatientName = selectedPatient.getName();

                        setContentView(R.layout.patient_view);

                        Button returnProvider = findViewById(R.id.providerPatientReturn);
                        Button submitThresholds = findViewById(R.id.submitAllThresholds);

                        TextView patientName = findViewById(R.id.patientLabel);
                        TextView patientQualData = findViewById(R.id.patientQualData);

                        patientName.setText(selectedPatientName);

                        // Charting Elements
                        userData = findViewById(R.id.userData); // Qualitative data bar chart
                        userHRData = findViewById(R.id.userHRData); // Quantitative Heart Rate Data Bar chart
                        userTempData = findViewById(R.id.userTempData); // Quantitative Temperature bar chart
                        userHRLine = findViewById(R.id.userHRLine); // Quantitative heart rate line chart
                        userTempLine = findViewById(R.id.userTempLine); // Quantitative temperature line chart
                        mSpinner = findViewById(R.id.selectorSpinner);
                        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                String selectedTimeframe = (String) parent.getItemAtPosition(position);
                                updateAllGraphs(selectedTimeframe);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                // Do nothing
                            }
                        });

                        api global = api.getInstance();

                        String patientAddress = linkString + "qualitative/" + userID;
                        global.sendGetRequestWithHandlerWithToken(patientAddress, token, new HandlerResponse() {
                            @Override
                            public void handleResponse(String response) {
                                Log.d("userData", response);
                                userQualResponse = response;

                            }
                        });

                        String patientHR = linkString + "readings/" + userID + "/1";
                        global.sendGetRequestWithHandlerWithToken(patientHR, token, new HandlerResponse() {
                            @Override
                            public void handleResponse(String response) {
                                userHRResponse = response;
                                Log.d("patientHRVals", response);
                            }
                        });

                        String patientTemp = linkString + "readings/" + userID + "/2";
                        global.sendGetRequestWithHandlerWithToken(patientTemp, token, new HandlerResponse() {
                            @Override
                            public void handleResponse(String response) {
                                Log.d("patientTempVals", response);
                                userTempResponse = response;
                                updateAllGraphs("Daily");
                            }
                        });

                        String patientQualAddress = linkString + "qualitative/" + selectedPatientId;
                        global.sendGetRequestWithHandlerWithToken(patientQualAddress, token, new HandlerResponse() {
                            @Override
                            public void handleResponse(String response) {
                                try {
                                    // Parse the response as a JSONArray
                                    JSONArray patientsArray = new JSONArray(response);
                                    qualitativeDetails = "";
                                    // Iterate through the array to process each patient
                                    for (int i = 0; i < patientsArray.length(); i++) {
                                        JSONObject patientObtained = patientsArray.getJSONObject(i);

                                        // Extract information for each patient
                                        String timeStamp = "Date: " + patientObtained.getString("time_stamp") + "\n";
                                        String rashString = "Rash: " + patientObtained.getString("rash") + "\n";
                                        String otherString = "Other: " + patientObtained.getString("other") + "\n";

                                        // Combine the information for the current patient
                                        qualitativeDetails += timeStamp + rashString + otherString;

                                    }
                                    // Update UI with the information for the current patient
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            patientQualData.setText("Qualitative data: \n" + qualitativeDetails);
                                        }
                                    });
                                } catch (JSONException e) {
                                    Log.e("ERROR", "Error parsing JSON: " + e.getMessage());
                                }
                            }
                        });

                        returnProvider.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent providerIntent = new Intent(ProviderActivity.this, ProviderActivity.class);
                                providerIntent.putExtra("linkString", linkString);
                                providerIntent.putExtra("token", token);
                                providerIntent.putExtra("userID", userID);

                                startActivity(providerIntent);
                                finish();
                            }
                        });
                        submitThresholds.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                api global = api.getInstance();

                                //get lower and upper from GUI and check if they are doubles
                                EditText hrLowerThreshBox = findViewById(R.id.lowerBound1);
                                EditText hrUpperThreshBox = findViewById(R.id.upperBound1);

                                EditText tempLowerThreshBox = findViewById(R.id.lowerBound2);
                                EditText tempUpperThreshBox = findViewById(R.id.upperBound2);

                                //TODO if they are doubles and not null, do next, else toast
                                
                                String.valueOf(hrLowerThreshBox.getText());

                                String thresholdString = linkString + "threshold";
                                JSONObject thresholdData = new JSONObject();
                                try {
                                    thresholdData.put("patient_id", selectedPatientId);
                                    thresholdData.put("sensor_id", "1");
                                    thresholdData.put("lower", String.valueOf(hrLowerThreshBox.getText()));
                                    thresholdData.put("upper", String.valueOf(hrUpperThreshBox.getText()));
                                    Log.d("preHR", thresholdData.toString());
                                    global.sendPostRequestWithHandlerWithToken(thresholdString, thresholdData, token, new HandlerResponse() {
                                        @Override
                                        public void handleResponse(String response) {
                                            //TODO Toast to say submitted
                                            Log.d("hrsubmit", response);
                                        }
                                    });
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                JSONObject tempThresholdData = new JSONObject();
                                try {
                                    tempThresholdData.put("patient_id", selectedPatientId);
                                    tempThresholdData.put("sensor_id", "2");
                                    tempThresholdData.put("lower", String.valueOf(tempLowerThreshBox.getText()));
                                    tempThresholdData.put("upper", String.valueOf(tempUpperThreshBox.getText()));

                                    Log.d("tempdata", tempThresholdData.toString());
                                    global.sendPostRequestWithHandlerWithToken(thresholdString, tempThresholdData, token, new HandlerResponse() {
                                        @Override
                                        public void handleResponse(String response) {
                                            //TODO Toast to say submitted
                                            Log.d("tempsubmit", response);
                                        }
                                    });
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Toast.makeText(ProviderActivity.this, "Thresholds Submitted", Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
            }
        });
    }
    private void setupInboxUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Create the adapter and set it to the ListView
                RequestAdapter adapter = new RequestAdapter(ProviderActivity.this, requestList);
                ListView listViewRequests = findViewById(R.id.inboxListView);
                listViewRequests.setAdapter(adapter);

                // Set item click listener for each patient
                listViewRequests.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        RequestInfo selectedRequest = (RequestInfo) parent.getItemAtPosition(position);
                        int selectedRequestId = selectedRequest.getId();
                        String selectedRequestMessage = selectedRequest.getMessage();
                        String selectedMessageType = selectedRequest.getMessageType();
                        int selectedSenderId = selectedRequest.getSenderID();

                        setContentView(R.layout.request_view);
                        Button providerReturn = findViewById(R.id.providerRequestReturn);
                        Button approveRequest = findViewById(R.id.approveRequest);
                        Button denyRequest = findViewById(R.id.denyRequest);

                        TextView requestTitle = findViewById(R.id.requestLabel);
                        TextView requestBody = findViewById(R.id.requestMessage);

                        requestTitle.setText(selectedRequestMessage);
                        requestBody.setText(selectedMessageType + " for id: " + selectedSenderId);

                        providerReturn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent providerIntent = new Intent(ProviderActivity.this, ProviderActivity.class);
                                providerIntent.putExtra("linkString", linkString);
                                providerIntent.putExtra("token", token);
                                providerIntent.putExtra("userID", userID);

                                startActivity(providerIntent);
                            }
                        });

                        approveRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                String associationConfirm = linkString + "associations";
                                api global = api.getInstance();
                                JSONObject patientProviderConnect = new JSONObject();
                                try {
                                    patientProviderConnect.put("patient_id", selectedSenderId);
                                    patientProviderConnect.put("provider_id", userID);
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                global.sendPostRequestWithHandlerWithToken(associationConfirm, patientProviderConnect, token, new HandlerResponse() {
                                    @Override
                                    public void handleResponse(String response) {
                                        //TODO DELETE REQUEST and SEND BACK TO PROVIDER ACTIVITY
                                        String inboxDelete = linkString + "inbox/" + selectedRequestId;
                                        global.sendDeleteRequestWithTokenWithHandler(inboxDelete, token, new HandlerResponse() {

                                            @Override
                                            public void handleResponse(String response) {
                                                Intent providerIntent = new Intent(ProviderActivity.this, ProviderActivity.class);
                                                providerIntent.putExtra("linkString", linkString);
                                                providerIntent.putExtra("token", token);
                                                providerIntent.putExtra("userID", userID);

                                                startActivity(providerIntent);
                                            }
                                        });

                                    }
                                });
                            }
                        });
                        denyRequest.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //TODO DELETE REQUEST and SEND BACK TO PROVIDER ACTIVITY
                                String inboxDelete = linkString + "inbox/" + selectedRequestId;
                                api global = api.getInstance();
                                global.sendDeleteRequestWithTokenWithHandler(inboxDelete, token, new HandlerResponse() {

                                    @Override
                                    public void handleResponse(String response) {
                                        Intent providerIntent = new Intent(ProviderActivity.this, ProviderActivity.class);
                                        providerIntent.putExtra("linkString", linkString);
                                        providerIntent.putExtra("token", token);
                                        providerIntent.putExtra("userID", userID);

                                        startActivity(providerIntent);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
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

    private void updateAllGraphs(String selectedTimeframe) {
        processData(userQualResponse, userData, selectedTimeframe);
        processQuanData(userHRResponse, userHRLine, userHRData, 1, selectedTimeframe);
        processQuanData(userTempResponse, userTempLine, userTempData, 2, selectedTimeframe);
    }

}