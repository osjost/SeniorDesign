package com.example.cytocheck;

import static com.example.cytocheck.DataProcessor.processData;
import static com.example.cytocheck.DataProcessor.processQuanData;
import static com.example.cytocheck.DataProcessor.setAllToFalse;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import api.*;

public class ProviderActivity extends AppCompatActivity {
    /* This activity showcases a bulk of the functionality of the app where a provider can log in and
    *  view all of their patients, as well as emergency notifications, threshold breach notifications,
    *  and association requests. When any of these elements are selected, a new view is used to display
    *  detailed information on the request and patient. The patient view utilizes the Patient info and
    *  Patient Adapter classes to show a patient's data, graphs, and configurable thresholds. The request
    *  view utilizes the Request info and Request Adapter classes to show the above notifications and
    *  requests and allows for dismissal of all requests and approval of association requests */
    private BarChart userData;
    private BarChart userHRData;
    private BarChart userTempData;
    private LineChart userHRLine;
    private  LineChart userTempLine;

    private Spinner mSpinner;
    private TextView blankGraphs;
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
                Log.d("inbox items", response);

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
        Button refresh = findViewById(R.id.refreshButton);
        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent providerIntent = new Intent(ProviderActivity.this, ProviderActivity.class);
                providerIntent.putExtra("linkString", linkString);
                providerIntent.putExtra("token", token);
                providerIntent.putExtra("userID", userID);
                providerIntent.putExtra("notificationToken", notifToken);

                startActivity(providerIntent);
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
                        Log.d("patient info", response);
                        patientNames.add(userProfile.getString("first_name"));
                        patientList.add(new PatientInfo(userProfile.getInt("user_id"), userProfile.getString("first_name") + " " + userProfile.getString("last_name"), userProfile.getString("phone_number"), userProfile.getString("email"), userProfile.getString("date_of_birth")));
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
                        blankGraphs = findViewById(R.id.blankGraph);
                        Button returnProvider = findViewById(R.id.providerPatientReturn);
                        Button submitThresholds = findViewById(R.id.submitAllThresholds);

                        TextView patientName = findViewById(R.id.patientLabel);
                        TextView patientContact = findViewById(R.id.patientContact);
                        TextView patientQualData = findViewById(R.id.patientQualData);


                        patientName.setText(selectedPatientName);
                        patientContact.setText("Phone:" + selectedPatient.getPhone() + "\n" + "Email:" + selectedPatient.getEmail());

                        // Charting Elements
                        userData = findViewById(R.id.userData); // Qualitative data bar chart
                        userHRData = findViewById(R.id.userHRData); // Quantitative Heart Rate Data Bar chart
                        userTempData = findViewById(R.id.userTempData); // Quantitative Temperature bar chart
                        userHRLine = findViewById(R.id.userHRLine); // Quantitative heart rate line chart
                        userTempLine = findViewById(R.id.userTempLine); // Quantitative temperature line chart
                        mSpinner = findViewById(R.id.selectorSpinner);


                        api global = api.getInstance();
                        Log.d("userID", String.valueOf(selectedPatientId));
                        String patientAddress = linkString + "qualitative/" + selectedPatientId;
                        global.sendGetRequestWithHandlerWithToken(patientAddress, token, new HandlerResponse() {
                            @Override
                            public void handleResponse(String response) {
                                selectedPatient.setQualData(response);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        processData(response, userData, "Daily");
                                        Log.d("qual done", "patient qual done");
                                    }
                                }).start();

                            }
                        });
                        String patientHR = linkString + "readings/" + selectedPatientId + "/1";
                        global.sendGetRequestWithHandlerWithToken(patientHR, token, new HandlerResponse() {
                            @Override
                            public void handleResponse(String response) {
                                selectedPatient.setHRData(response);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        processQuanData(response, userHRLine, userHRData, 1, "Daily");
                                        Log.d("hr done", "patient hr done");
                                    }
                                }).start();

                            }
                        });
                        String patientTemp = linkString + "readings/" + selectedPatientId + "/2";
                        global.sendGetRequestWithHandlerWithToken(patientTemp, token, new HandlerResponse() {
                            @Override
                            public void handleResponse(String response) {
                                selectedPatient.setTempData(response);
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        processQuanData(response, userTempLine, userTempData, 2, "Daily");
                                        Log.d("temp done", "patient temp done");
                                    }
                                }).start();
                            }
                        });
                        mSpinner.setVisibility(View.GONE);
                        userData.setVisibility(View.GONE);
                        userHRData.setVisibility(View.GONE);
                        userTempData.setVisibility(View.GONE);
                        userHRLine.setVisibility(View.GONE);
                        userTempLine.setVisibility(View.GONE);

                        Button displayGraphs = findViewById(R.id.patgraph_display);
                        displayGraphs.setVisibility(View.VISIBLE);
                        displayGraphs.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                updateAllGraphs("Daily",selectedPatient.getQualData(), selectedPatient.getHRData(), selectedPatient.getTempData());
                                mSpinner.setVisibility(View.VISIBLE);

                                displayGraphs.setVisibility(View.GONE);
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
                                        String convertedTimeString = "";
                                        try {
                                            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                                            originalFormat.setTimeZone(TimeZone.getTimeZone("UTC")); // Original time zone (+0 hours)

                                            Date originalDate = originalFormat.parse(patientObtained.getString("time_stamp"));

                                            Calendar calendar = Calendar.getInstance();
                                            calendar.setTime(originalDate);

                                            SimpleDateFormat newFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                                            newFormat.setTimeZone(TimeZone.getDefault()); // Device's time zone

                                            convertedTimeString = newFormat.format(calendar.getTime());
                                        } catch (ParseException e) {
                                            e.printStackTrace();
                                        }

                                        String timeStamp = "";
                                        String rashString = "";
                                        String otherString = "";
                                        if (patientObtained.getString("rash").equals("No") && patientObtained.getString("other").equals("No")) {
                                            // Both no so no need to display anything for that day
                                        }
                                        else if (patientObtained.getString("rash").equals("No")) {
                                            // Rash is not present so don't display it
                                            timeStamp = "Date: " + convertedTimeString + "\n";
                                            otherString = "Other: " + patientObtained.getString("other") + "\n";
                                        }
                                        else if (patientObtained.getString("other").equals("No")) {
                                            // Additional symptoms not present
                                            timeStamp = "Date: " + convertedTimeString + "\n";
                                            rashString = "Rash: " + patientObtained.getString("rash") + "\n";
                                        }
                                        else {
                                            // All are present
                                            timeStamp = "Date: " + convertedTimeString + "\n";
                                            rashString = "Rash: " + patientObtained.getString("rash") + "\n";
                                            otherString = "Other: " + patientObtained.getString("other") + "\n";
                                        }

                                        // Combine the information for the current patient
                                        qualitativeDetails += timeStamp + rashString + otherString + "\n";

                                    }
                                    // Update UI with the information for the current patient
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            patientQualData.setText("Qualitative data: \n" + qualitativeDetails);
                                        }});
                                } catch (JSONException e) {
                                    Log.e("ERROR", "Error parsing JSON: " + e.getMessage());
                                }
                            }
                        });


                        //get lower and upper from GUI and check if they are doubles
                        EditText hrLowerThreshBox = findViewById(R.id.lowerBound1);
                        EditText hrUpperThreshBox = findViewById(R.id.upperBound1);
                        global.sendGetRequestWithHandlerWithToken(linkString + "threshold/" + selectedPatientId + "/1", token, new HandlerResponse() {
                            @Override
                            public void handleResponse(String response) {

                                try {
                                    JSONObject patientHRThreshold = new JSONObject(response);
                                    String hrUpper = patientHRThreshold.getString("upper");
                                    String hrLower = patientHRThreshold.getString("lower");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            hrLowerThreshBox.setHint(hrLower);
                                            hrUpperThreshBox.setHint(hrUpper);
                                        }
                                    });

                                } catch (JSONException e) {

                                }
                            }
                        });


                        EditText tempLowerThreshBox = findViewById(R.id.lowerBound2);
                        EditText tempUpperThreshBox = findViewById(R.id.upperBound2);
                        global.sendGetRequestWithHandlerWithToken(linkString + "threshold/" + selectedPatientId + "/2", token, new HandlerResponse() {
                            @Override
                            public void handleResponse(String response) {

                                try {
                                    JSONObject patientTempThreshold = new JSONObject(response);
                                    String tempUpper = patientTempThreshold.getString("upper");
                                    String tempLower = patientTempThreshold.getString("lower");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            tempLowerThreshBox.setHint(tempLower);
                                            tempUpperThreshBox.setHint(tempUpper);
                                        }
                                    });

                                } catch (JSONException e) {

                                }
                            }
                        });
                        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                String selectedTimeframe = (String) parent.getItemAtPosition(position);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateAllGraphs(selectedTimeframe, selectedPatient.getQualData(), selectedPatient.getHRData(), selectedPatient.getTempData());
                                    }
                                });
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> parent) {
                                // Do nothing
                            }
                        });

                        returnProvider.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                setAllToFalse();
                                Intent providerIntent = new Intent(ProviderActivity.this, ProviderActivity.class);
                                providerIntent.putExtra("linkString", linkString);
                                providerIntent.putExtra("token", token);
                                providerIntent.putExtra("userID", userID);
                                providerIntent.putExtra("notificationToken", notifToken);

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

                                float hrLower = 0;
                                float hrUpper = 0;
                                boolean hrValid = false;
                                if (!TextUtils.isEmpty(hrLowerThreshBox.getText()) && !TextUtils.isEmpty(hrUpperThreshBox.getText())) {
                                    try {
                                        // Parse the text as floats
                                        hrLower = Float.parseFloat(hrLowerThreshBox.getText().toString());
                                        hrUpper = Float.parseFloat(hrUpperThreshBox.getText().toString());

                                        // Check if lower threshold is lower than upper threshold
                                        if ((hrLower < hrUpper) && hrLower >= 30 && hrUpper <= 200) {
                                            // Lower and upper are valid
                                            hrValid = true;
                                        } else {
                                            // Show a toast indicating the error
                                            Toast.makeText(ProviderActivity.this, "Format Lower < Upper and Range: (30-200)bpm", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (NumberFormatException e) {
                                        // Show a toast indicating that the input is not a valid float
                                        Toast.makeText(ProviderActivity.this, "Thresholds must be numeric values", Toast.LENGTH_SHORT).show();
                                    }
                                } else if (TextUtils.isEmpty(hrLowerThreshBox.getText()) && !TextUtils.isEmpty(hrUpperThreshBox.getText())) {
                                    // If lower threshold box is empty and upper isn't so use hint to send
                                    try {
                                        // Parse the text as floats
                                        hrLower = Float.parseFloat(hrLowerThreshBox.getHint().toString());
                                        hrUpper = Float.parseFloat(hrUpperThreshBox.getText().toString());

                                        // Check if lower threshold is lower than upper threshold
                                        if ((hrLower < hrUpper) && hrLower >= 30 && hrUpper <= 200) {
                                            // Lower and upper are valid
                                            hrValid = true;
                                        } else {
                                            // Show a toast indicating the error
                                            Toast.makeText(ProviderActivity.this, "Format Lower < Upper and Range: (30-200)bpm", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (NumberFormatException e) {
                                        // Show a toast indicating that the input is not a valid float
                                        Toast.makeText(ProviderActivity.this, "Thresholds must be numeric values", Toast.LENGTH_SHORT).show();
                                    }
                                } else if (TextUtils.isEmpty(hrUpperThreshBox.getText()) && !TextUtils.isEmpty(hrLowerThreshBox.getText())) {
                                    // If upper threshold box is empty, fill it with the hint from lower threshold box
                                    try {
                                        // Parse the text as floats
                                        hrLower = Float.parseFloat(hrLowerThreshBox.getText().toString());
                                        hrUpper = Float.parseFloat(hrUpperThreshBox.getHint().toString());

                                        // Check if lower threshold is lower than upper threshold
                                        if ((hrLower < hrUpper) && hrLower >= 30 && hrUpper <= 200) {
                                            // Lower and upper are valid
                                            hrValid = true;
                                        } else {
                                            // Show a toast indicating the error
                                            Toast.makeText(ProviderActivity.this, "Format Lower < Upper and Range: (30-200)bpm", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (NumberFormatException e) {
                                        // Show a toast indicating that the input is not a valid float
                                        Toast.makeText(ProviderActivity.this, "Thresholds must be numeric values", Toast.LENGTH_SHORT).show();
                                    }
                                }
                                float tempLower = 0;
                                float tempUpper = 0;
                                boolean tempValid = false;


                                // Check if the text in the boxes is not empty
                                if (!TextUtils.isEmpty(tempLowerThreshBox.getText()) && !TextUtils.isEmpty(tempUpperThreshBox.getText())) {
                                    try {
                                        // Parse the text as floats
                                        tempLower = Float.parseFloat(tempLowerThreshBox.getText().toString());
                                        tempUpper = Float.parseFloat(tempUpperThreshBox.getText().toString());

                                        // Check if lower threshold is lower than upper threshold
                                        if ((tempLower < tempUpper) && tempLower >= 90 && tempUpper <= 115) {
                                            // Lower and upper are valid
                                            tempValid = true;
                                        } else {
                                            // Show a toast indicating the error
                                            Toast.makeText(ProviderActivity.this, "Format Lower < Upper and Range: (90-115)F", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (NumberFormatException e) {
                                        // Show a toast indicating that the input is not a valid float
                                        Toast.makeText(ProviderActivity.this, "Thresholds must be numeric values", Toast.LENGTH_SHORT).show();
                                    }
                                } else if (TextUtils.isEmpty(tempLowerThreshBox.getText()) && !TextUtils.isEmpty(tempUpperThreshBox.getText())) {
                                    // If lower threshold box is empty and upper isn't, use hint to fill lower threshold
                                    try {
                                        // Parse the text as floats
                                        tempLower = Float.parseFloat(tempLowerThreshBox.getHint().toString());
                                        tempUpper = Float.parseFloat(tempUpperThreshBox.getText().toString());

                                        // Check if lower threshold is lower than upper threshold
                                        if ((tempLower < tempUpper) && tempLower >= 90 && tempUpper <= 115) {
                                            // Lower and upper are valid
                                            tempValid = true;
                                        } else {
                                            // Show a toast indicating the error
                                            Toast.makeText(ProviderActivity.this, "Format Lower < Upper and Range: (90-115)F", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (NumberFormatException e) {
                                        // Show a toast indicating that the input is not a valid float
                                        Toast.makeText(ProviderActivity.this, "Thresholds must be numeric values", Toast.LENGTH_SHORT).show();
                                    }
                                } else if (TextUtils.isEmpty(tempUpperThreshBox.getText()) && !TextUtils.isEmpty(tempLowerThreshBox.getText())) {
                                    // If upper threshold box is empty, fill it with the hint from lower threshold box
                                    try {
                                        // Parse the text as floats
                                        tempLower = Float.parseFloat(tempLowerThreshBox.getText().toString());
                                        tempUpper = Float.parseFloat(tempUpperThreshBox.getHint().toString());

                                        // Check if lower threshold is lower than upper threshold
                                        if ((tempLower < tempUpper) && tempLower >= 90 && tempUpper <= 115) {
                                            // Lower and upper are valid
                                            tempValid = true;
                                        } else {
                                            // Show a toast indicating the error
                                            Toast.makeText(ProviderActivity.this, "Format Lower < Upper and Range: (90-115)F", Toast.LENGTH_SHORT).show();
                                        }
                                    } catch (NumberFormatException e) {
                                        // Show a toast indicating that the input is not a valid float
                                        Toast.makeText(ProviderActivity.this, "Thresholds must be numeric values", Toast.LENGTH_SHORT).show();
                                    }
                                }

                                String thresholdString = linkString + "threshold";

                                if (hrValid) {
                                    JSONObject thresholdData = new JSONObject();
                                    try {
                                        thresholdData.put("patient_id", selectedPatientId);
                                        thresholdData.put("sensor_id", "1");
                                        thresholdData.put("lower", hrLower);
                                        thresholdData.put("upper", hrUpper);
                                        Log.d("preHR", thresholdData.toString());
                                        global.sendPostRequestWithHandlerWithToken(thresholdString, thresholdData, token, new HandlerResponse() {
                                            @Override
                                            public void handleResponse(String response) {

                                                Log.d("hrsubmit", response);
                                            }
                                        });
                                    }
                                    catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (tempValid) {
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

                                                Log.d("tempsubmit", response);
                                            }
                                        });
                                    }
                                    catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if (hrValid || tempValid) {
                                    Toast.makeText(ProviderActivity.this, "Thresholds Submitted", Toast.LENGTH_SHORT).show();
                                }

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

                // Set item click listener for each request
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
                        if (selectedMessageType.equals("breach")) {
                            requestTitle.setText("Threshold Breach");
                            requestBody.setText(selectedRequestMessage);
                        }
                        else if (selectedMessageType.equals("emergency")) {
                            requestTitle.setText("Emergency Patient Notification");
                            requestBody.setText("Emergency with Patient id: " + selectedSenderId);
                        }
                        else {
                            requestTitle.setText("New Patient Association");
                            requestBody.setText("Association Request for Patient id: " + selectedSenderId);
                        }

                        providerReturn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                // Handle provider return button click
                                Intent providerIntent = new Intent(ProviderActivity.this, ProviderActivity.class);
                                providerIntent.putExtra("linkString", linkString);
                                providerIntent.putExtra("token", token);
                                providerIntent.putExtra("userID", userID);
                                providerIntent.putExtra("notificationToken", notifToken);

                                startActivity(providerIntent);
                                finish();
                            }
                        });

                        // Check messageType and adjust button visibility and text
                        if (selectedMessageType.equals("emergency") || selectedMessageType.equals("breach")) {
                            // For emergency requests, hide the approve button
                            approveRequest.setVisibility(View.GONE);
                            // Change the text of the deny button to "Dismiss"
                            denyRequest.setText("Dismiss");

                            denyRequest.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Handle dismiss button click for emergency messages
                                    String emergencyDeleteUrl = linkString + "inbox/" + selectedRequestId;
                                    api global = api.getInstance();
                                    global.sendDeleteRequestWithTokenWithHandler(emergencyDeleteUrl, token, new HandlerResponse() {
                                        @Override
                                        public void handleResponse(String response) {
                                            Intent providerIntent = new Intent(ProviderActivity.this, ProviderActivity.class);
                                            providerIntent.putExtra("linkString", linkString);
                                            providerIntent.putExtra("token", token);
                                            providerIntent.putExtra("userID", userID);
                                            providerIntent.putExtra("notificationToken", notifToken);

                                            startActivity(providerIntent);
                                            finish();
                                        }
                                    });
                                }
                            });
                        } else {
                            // For non-emergency requests, show the approve button
                            approveRequest.setVisibility(View.VISIBLE);
                            // Change the text of the deny button back to "Deny"
                            denyRequest.setText("Deny");

                            denyRequest.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    // Handle deny button click for non-emergency messages
                                    String nonEmergencyDeleteUrl = linkString + "inbox/" + selectedRequestId;
                                    api global = api.getInstance();
                                    global.sendDeleteRequestWithTokenWithHandler(nonEmergencyDeleteUrl, token, new HandlerResponse() {
                                        @Override
                                        public void handleResponse(String response) {
                                            Intent providerIntent = new Intent(ProviderActivity.this, ProviderActivity.class);
                                            providerIntent.putExtra("linkString", linkString);
                                            providerIntent.putExtra("token", token);
                                            providerIntent.putExtra("userID", userID);
                                            providerIntent.putExtra("notificationToken", notifToken);

                                            startActivity(providerIntent);
                                            finish();
                                        }
                                    });
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
                                            String inboxDelete = linkString + "inbox/" + selectedRequestId;
                                            global.sendDeleteRequestWithTokenWithHandler(inboxDelete, token, new HandlerResponse() {

                                                @Override
                                                public void handleResponse(String response) {
                                                    Intent providerIntent = new Intent(ProviderActivity.this, ProviderActivity.class);
                                                    providerIntent.putExtra("linkString", linkString);
                                                    providerIntent.putExtra("token", token);
                                                    providerIntent.putExtra("userID", userID);
                                                    providerIntent.putExtra("notificationToken", notifToken);

                                                    startActivity(providerIntent);
                                                    finish();
                                                }
                                            });

                                        }
                                    });
                                }
                            });
                        }
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

    private void updateAllGraphs(String selectedTimeframe, String userQualResponse, String userHRResponse, String userTempResponse) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                processData(userQualResponse, userData, selectedTimeframe);
                processQuanData(userHRResponse, userHRLine, userHRData, 1, selectedTimeframe);
                processQuanData(userTempResponse, userTempLine, userTempData, 2, selectedTimeframe);
                blankGraphs.setText("");
                if (userData.getVisibility() == View.GONE) {
                    blankGraphs.setText(blankGraphs.getText() + "No Qualitative data for the selected timeframe.\n");
                }
                if (userHRLine.getVisibility() == userHRData.getVisibility()) {
                    blankGraphs.setText(blankGraphs.getText() + "No Heart Rate data for the selected timeframe.\n");
                }
                if (userTempLine.getVisibility() == userTempData.getVisibility()) {
                    blankGraphs.setText(blankGraphs.getText() + "No Temperature data for the selected timeframe.\n");
                }
            }
        });
    }

}