package com.example.cytocheck;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

//Import API stuff
import org.json.JSONObject;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Date;

import api.*;



public class MainActivity extends AppCompatActivity {
    // For push notifications
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                }
            });

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    private EditText userTextField, passTextField;
    private String linkString = "https://ec2-54-193-162-215.us-west-1.compute.amazonaws.com:443/"; //This is the link to the server holding the database
    private String usernameResponse = "";
    private String token = "";
    private String userID = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button loginButton = findViewById(R.id.loginSubmit); //Login Functionality
        userTextField = findViewById(R.id.userTextField);
        passTextField = findViewById(R.id.passTextField);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                api global = api.getInstance(); //get the shared api instance
                String username = userTextField.getText().toString();
                String password = passTextField.getText().toString();

                JSONObject loginSend = new JSONObject();
                try {
                    loginSend.put("username", username);
                    loginSend.put("password", password);
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
                String loginString = linkString + "login";
                global.sendPostRequestWithHandler(loginString, loginSend, new HandlerResponse() {
                    @Override
                    public void handleResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            token = jsonObject.getString("jwt");
                            userID = jsonObject.getString("user_id");
                            Log.d("eep", jsonObject.toString());
                            String httpsAddress = linkString + "users/" + userID;
                            global.sendGetRequestWithHandlerWithToken(httpsAddress, token, new HandlerResponse() {
                                @Override
                                public void handleResponse(String response) {
                                    String firstName = "";
                                    String role = "";
                                    try {
                                        JSONObject userProfile = new JSONObject(response);
                                        firstName = userProfile.getString("first_name");
                                        role = userProfile.getString("role");
                                        if (role.equals("patient")) { //user is a patient so they can be taken to their respective interface
                                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                            intent.putExtra("token", token);
                                            intent.putExtra("userID", userID);
                                            intent.putExtra("firstName", firstName);
                                            intent.putExtra("linkString", linkString); //globalize linkString between activities

                                            startActivity(intent);
                                        }
                                        else if (role.equals("provider")) { //if the user is a provider, take them to the provider interface
                                            Intent intent = new Intent(MainActivity.this, ProviderActivity.class);
                                            intent.putExtra("token", token);
                                            intent.putExtra("userID", userID);
                                            intent.putExtra("linkString", linkString); //globalize linkString between activities

                                            startActivity(intent);
                                        }
                                    }
                                    catch (JSONException e) {
                                        Log.d("loginfail", e.getMessage());
                                    }
                                }
                            });
                        }
                        catch (JSONException e) {

                        }
                    }
                });
            }
        });

        Button submitButton = findViewById(R.id.signUp); //Signup functionality
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Get current timestamp in milliseconds
                long currentTimeMillis = System.currentTimeMillis();

                // Convert the timestamp to a readable format (optional)
                String timestampString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
                        .format(new Date(currentTimeMillis));

                // Display the timestamp in a Toast
                Toast.makeText(MainActivity.this, "Timestamp: " + timestampString, Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(MainActivity.this, SignupActivity.class); //send user to signup activity
                intent.putExtra("linkString", linkString); //globalize linkString between activities

                startActivity(intent);
            }
        }); //end of submit on click listener

    } //end of onCreate

//    @Override
//    protected void onResume() {
//        super.onResume();
//        Log.w("messaging", "here");
        // Your condition to ensure you don't ask for permission unnecessarily
//        if (true) {
//            askNotificationPermission();
//        }


//        this actually gets the users token, assuming that they enter yes. we have to add more UI elements to account for this:
//        FirebaseMessaging.getInstance().getToken()
//                .addOnCompleteListener(new OnCompleteListener<String>() {
//                    @Override
//                    public void onComplete(@NonNull Task<String> task) {
//                        if (!task.isSuccessful()) {
//                            Log.w("messaging", "Fetching FCM registration token failed", task.getException());
//                            return;
//                        }
//
//                        // Get new FCM registration token
//                        String token = task.getResult();
//
//                        // Log and toast
//                        Log.d("messaging", token);
//                        // Send the token to your server to keep it updated
//                    }
//                });
//    }
} //end of main activity