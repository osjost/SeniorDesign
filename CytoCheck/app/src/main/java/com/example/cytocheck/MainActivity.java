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

//                //make our JSON object we want to pass in
//                JSONObject jsonInput = new JSONObject();
//                try {
////                    jsonInput.put("role", "Patient");
////                    jsonInput.put("username", "poo69");
////                    jsonInput.put("first_name", "poo2");
////                    jsonInput.put("last_name", "poo3");
////                    jsonInput.put("date_of_birth", "1991-02-02");
////                    jsonInput.put("email", "poo@gmail.com");
////                    jsonInput.put("phone_number", "12345678");
////                    jsonInput.put("num_measures", "twelve");
//                    jsonInput.put("user_id", "1");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
                global.sendGetRequest("https://10.0.2.2:443/users/4", new ResponseHandler() {
                    @Override
                    public void handleResponse(String response) {
                        Log.d("response", response);
                    }
                });





//                global.sendDeleteRequest("https://10.0.2.2:443/users", jsonInput);

//                String username = userTextField.getText().toString();
//                String password = passTextField.getText().toString();
//                if (username.equals("user") && password.equals("pass")) {
//                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
//                    startActivity(intent);
//                }
//                else if (username.equals("prov") && password.equals("pass")) {
//                    Intent intent2 = new Intent(MainActivity.this, ProviderActivity.class);
//                    startActivity(intent2);
//                }
//                else {
//                    Toast.makeText(MainActivity.this, "Login Failed. Invalid credentials.", Toast.LENGTH_SHORT).show();
//                }

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