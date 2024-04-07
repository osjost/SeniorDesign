package com.example.cytocheck;

import static com.example.cytocheck.DataProcessor.setAllToFalse;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;

//Import API stuff
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;
import org.json.JSONException;

import java.text.SimpleDateFormat;
import java.util.Date;

import api.*;



public class LoginActivity extends AppCompatActivity {
    private EditText userTextField, passTextField;
    private String linkString = "https://ec2-54-193-162-215.us-west-1.compute.amazonaws.com:443/"; //This is the link to the server holding the database
    private String usernameResponse = "";
    private String token = "";
    private String notificationToken = "";
    private String userID = "";

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setAllToFalse();
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
                //Log.d("Login Sent", loginSend.toString());
                String loginString = linkString + "login";
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                global.sendPostRequestWithHandler(loginString, loginSend, new HandlerResponse() {

                    @Override
                    public void handleResponse(String response) {
                        //Log.d("response", response);
                        // Run response handling on a UI thread to make toasts

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            token = jsonObject.getString("jwt");
                            userID = jsonObject.getString("user_id");
                            String httpsAddress = linkString + "users/" + userID;
                            global.sendGetRequestWithHandlerWithToken(httpsAddress, token, new HandlerResponse() {
                                @Override
                                public void handleResponse(String response) {
                                    // Run response handling on a UI thread to make toasts
                                    String firstName = "";
                                    String role = "";
                                    //Log.d("login result", response);
                                    try {
                                        JSONObject userProfile = new JSONObject(response);
                                        firstName = userProfile.getString("first_name");
                                        role = userProfile.getString("role");
                                        if (role.equals("patient")) { //user is a patient so they can be taken to their respective interface
                                            Intent intent = new Intent(LoginActivity.this, PatientActivity.class);
                                            intent.putExtra("token", token);
                                            intent.putExtra("userID", userID);
                                            intent.putExtra("firstName", firstName);
                                            intent.putExtra("linkString", linkString); //globalize linkString between activities
                                            intent.putExtra("notificationToken", notificationToken);
                                            startActivity(intent);
                                            finish();
                                        }
                                        else if (role.equals("provider")) { //if the user is a provider, take them to the provider interface
                                            Intent intent = new Intent(LoginActivity.this, ProviderActivity.class);
                                            intent.putExtra("token", token);
                                            intent.putExtra("userID", userID);
                                            intent.putExtra("linkString", linkString); //globalize linkString between activities
                                            intent.putExtra("notificationToken", notificationToken);

                                            startActivity(intent);
                                            finish();
                                        }
                                    }
                                    catch (JSONException e) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    }
                                }
                            });

                        }
                        catch (JSONException e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            }
        });
        }
        });

        TextView signupTextView = findViewById(R.id.signUpLabel);
        signupTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class); //send user to signup activity
                intent.putExtra("linkString", linkString); //globalize linkString between activities
                startActivity(intent);
            }
        });
    } //end of onCreate

    @Override
        protected void onResume() {
            super.onResume();
            Log.w("messaging", "here");
    // Your condition to ensure you don't ask for permission unnecessarily
            if (true) {
                askNotificationPermission();
            }


    //        this actually gets the users token, assuming that they enter yes. we have to add more UI elements to account for this:
            FirebaseMessaging.getInstance().getToken()
                    .addOnCompleteListener(new OnCompleteListener<String>() {
                        @Override
                        public void onComplete(@NonNull Task<String> task) {
                            if (!task.isSuccessful()) {
                                Log.w("messaging", "Fetching FCM registration token failed", task.getException());
                                return;
                            }

                            // Get new FCM registration token

                            notificationToken = task.getResult();

                            // Send the token to your server to keep it updated
                        }
                    });
        }
} //end of main activity