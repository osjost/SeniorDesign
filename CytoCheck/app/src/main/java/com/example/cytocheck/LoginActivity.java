package com.example.cytocheck;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

//Import API stuff
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
    private String userID = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
                global.sendPostRequestWithHandler(loginString, loginSend, new HandlerResponse() {
                    @Override
                    public void handleResponse(String response) {
                        //Log.d("response", response);
                        // Run response handling on a UI thread to make toasts
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    JSONObject jsonObject = new JSONObject(response);
                                    token = jsonObject.getString("jwt");
                                    userID = jsonObject.getString("user_id");
                                    String httpsAddress = linkString + "users/" + userID;
                                    global.sendGetRequestWithHandlerWithToken(httpsAddress, token, new HandlerResponse() {

                                        @Override
                                        public void handleResponse(String response) {
                                            // Run response handling on a UI thread to make toasts
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
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

                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                        else if (role.equals("provider")) { //if the user is a provider, take them to the provider interface
                                                            Intent intent = new Intent(LoginActivity.this, ProviderActivity.class);
                                                            intent.putExtra("token", token);
                                                            intent.putExtra("userID", userID);
                                                            intent.putExtra("linkString", linkString); //globalize linkString between activities

                                                            startActivity(intent);
                                                            finish();
                                                        }
                                                    }
                                                    catch (JSONException e) {
                                                        Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });

                                        }
                                    });
                                }
                                catch (JSONException e) {
                                    Toast.makeText(LoginActivity.this, "Login failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });
            }
        });

        Button submitButton = findViewById(R.id.signUp); //Signup functionality
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(LoginActivity.this, SignupActivity.class); //send user to signup activity
                intent.putExtra("linkString", linkString); //globalize linkString between activities

                startActivity(intent);
            }
        }); //end of submit on click listener
    } //end of onCreate
} //end of main activity