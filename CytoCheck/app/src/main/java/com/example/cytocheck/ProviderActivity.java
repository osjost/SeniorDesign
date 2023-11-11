package com.example.cytocheck;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ProviderActivity extends AppCompatActivity {


    private Handler inactivityHandler;
    private Runnable inactivityRunnable;
    private static final long INACTIVITY_TIMEOUT = 5000; // 5 seconds in milliseconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider); // Create a new XML layout for this activity if needed
        // Additional initialization code for the WelcomeActivity

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

        Button continueButton = findViewById(R.id.continueButton); //Back to Log in screen
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resetInactivityTimer();

            }
        });
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