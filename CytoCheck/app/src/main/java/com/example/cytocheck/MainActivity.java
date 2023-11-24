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
import api.*;



public class MainActivity extends AppCompatActivity {
    private EditText userTextField, passTextField;
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
//                grab the shared api class
                api global = api.getInstance();



                String username = userTextField.getText().toString();
                String password = passTextField.getText().toString();
//                global.sendGetRequest(username, new HandlerResponse() {
//                    @Override
//                    public void handleResponse(String response) {
//                        Log.d("response", response);
//                        usernameResponse = response;
//
//                    }
//                });
//                Toast.makeText(MainActivity.this, usernameResponse, Toast.LENGTH_SHORT).show();
                JSONObject loginSend = new JSONObject();
                try {
                    loginSend.put("username", username);
                    loginSend.put("password", password);
                }
                catch (JSONException e){
                    e.printStackTrace();
                }
                global.sendPostRequestWithHandler("https://10.0.2.2:443/login", loginSend, new HandlerResponse() {
                    @Override
                    public void handleResponse(String response) {

                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            token = jsonObject.getString("jwt");
                            userID = jsonObject.getString("user_id");
                            String httpsAddress = "https://10.0.2.2:443/users/" + userID;
                            global.sendGetRequestWithHandlerWithToken(httpsAddress, token, new HandlerResponse() {
                                @Override
                                public void handleResponse(String response) {
                                    String firstName = "";
                                    String role = "";
                                    try {
                                        JSONObject userProfile = new JSONObject(response);
                                        firstName = userProfile.getString("first_name");
                                        role = userProfile.getString("role");
                                        if (role.equals("patient")) {
                                            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                            intent.putExtra("token", token);
                                            intent.putExtra("userID", userID);
                                            intent.putExtra("firstName", firstName);
                                            startActivity(intent);
                                        }
                                        else if (role.equals("provider")) {
                                            Intent intent = new Intent(MainActivity.this, ProviderActivity.class);
                                            intent.putExtra("token", token);
                                            intent.putExtra("userID", userID);

                                            startActivity(intent);
                                        }
                                    }
                                    catch (JSONException e) {

                                    }
                                }
                            });
                        }
                        catch (JSONException e) {

                        }



                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                        intent.putExtra("token", token);
                        intent.putExtra("userID", userID);
                        startActivity(intent);
//                        System.out.print(response);
//                        Log.d("response", response);
                    }
                });




            }
        });

        Button submitButton = findViewById(R.id.signUp); //Signup functionality
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, SignupActivity.class);

                startActivity(intent);
            }
        });
    }


}


//make our JSON object we want to pass in
//                JSONObject jsonInput = new JSONObject();
//                try {
//                    jsonInput.put("role", "Patient");
//                    jsonInput.put("username", "user2");
//                    jsonInput.put("first_name", "first");
//                    jsonInput.put("last_name", "last");
//                    jsonInput.put("date_of_birth", "1991-02-02");
//                    jsonInput.put("email", "a@gmail.com");
//                    jsonInput.put("phone_number", "12345678");
//                    jsonInput.put("num_measures", "twelve");
////                    jsonInput.put("user_id", "1");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                JSONObject user = new JSONObject();
//                try {
//                    user.put("user_id", "4");
//                } catch (JSONException e) {
//                }
//                global.sendDeleteRequest("https://10.0.2.2:443/users", user);
//                global.sendPostRequest("https://10.0.2.2:443/users", jsonInput);
//                global.sendGetRequest("https://10.0.2.2:443/users/5", new HandlerResponse() {
//                    @Override
//                    public void handleResponse(String response) {
//                        Log.d("response", response);
//                    }
//                });





//                global.sendDeleteRequest("https://10.0.2.2:443/users", jsonInput);