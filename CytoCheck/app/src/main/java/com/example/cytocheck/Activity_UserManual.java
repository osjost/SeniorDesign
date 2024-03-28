package com.example.cytocheck;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

public class Activity_UserManual extends AppCompatActivity {
    String linkString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_manual);
        Intent intent = getIntent();
        linkString = intent.getStringExtra("linkString");
        String token = intent.getStringExtra("token");
        String userID = intent.getStringExtra("userID");
    }

}