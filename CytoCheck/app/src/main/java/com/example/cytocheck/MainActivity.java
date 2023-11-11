package com.example.cytocheck;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
                String username = userTextField.getText().toString();
                String password = passTextField.getText().toString();
                if (username.equals("user") && password.equals("pass")) {
                    Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
                else if (username.equals("prov") && password.equals("pass")) {
                    Intent intent2 = new Intent(MainActivity.this, ProviderActivity.class);
                    startActivity(intent2);
                }
                else {
                    Toast.makeText(MainActivity.this, "Login Failed. Invalid credentials.", Toast.LENGTH_SHORT).show();
                }

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