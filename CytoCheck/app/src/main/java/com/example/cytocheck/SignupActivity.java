package com.example.cytocheck;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import fragments.DatePickerFragment;



public class SignupActivity extends AppCompatActivity {
    private EditText phoneField;
    private EditText dobField;
    private EditText passText;
    private EditText firstName;
    private EditText lastName;
    private EditText usernameText;
    private EditText confirmText;
    private EditText emailText;
    private TextView referralText;
    private boolean userGood = false;
    private boolean passGood = false;
    private boolean confirmGood = false;
    private boolean firstGood = false;
    private boolean lastGood = false;
    private boolean emailGood = false;
    private boolean dobGood = false;
    private boolean phoneGood = false;
    private boolean referGood = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        passText = findViewById(R.id.passwordCreate);
        passText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                }
                else {
                    if (String.valueOf(passText.getText()).equals("")) {
                        Toast.makeText(SignupActivity.this, "Must enter a password", Toast.LENGTH_SHORT).show();
                        Drawable customDrawable = getResources().getDrawable(R.drawable.edittext_border);
                        passText.setBackground(customDrawable);
                        passGood = false;
                    }
                    else {
                        passGood = true;
                        passText.setBackgroundResource(android.R.drawable.edit_text);
                    }
                }
            }
        });
        usernameText = findViewById(R.id.createUsername);
        usernameText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                }
                else {
                    if (String.valueOf(usernameText.getText()).equals("")) { //Also need to check if username is same as any in database
                        Toast.makeText(SignupActivity.this, "Must enter a username", Toast.LENGTH_SHORT).show();
                        Drawable customDrawable = getResources().getDrawable(R.drawable.edittext_border);
                        usernameText.setBackground(customDrawable);
                        userGood = false;
                    }
                    else {
                        userGood = true;
                        usernameText.setBackgroundResource(android.R.drawable.edit_text);
                    }
                }
            }
        });
        firstName = findViewById(R.id.firstInput);
        firstName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                }
                else {
                    if (String.valueOf(firstName.getText()).equals("")) {
                        Toast.makeText(SignupActivity.this, "Must enter a First name", Toast.LENGTH_SHORT).show();
                        Drawable customDrawable = getResources().getDrawable(R.drawable.edittext_border);
                        firstName.setBackground(customDrawable);
                        firstGood = false;
                    }
                    else {
                        firstGood = true;
                        firstName.setBackgroundResource(android.R.drawable.edit_text);
                    }
                }
            }
        });
        lastName = findViewById(R.id.lastInput);
        lastName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                }
                else {
                    if (String.valueOf(lastName.getText()).equals("")) {
                        Toast.makeText(SignupActivity.this, "Must enter a Last name", Toast.LENGTH_SHORT).show();
                        Drawable customDrawable = getResources().getDrawable(R.drawable.edittext_border);
                        lastName.setBackground(customDrawable);
                        lastGood = false;
                    }
                    else {
                        lastGood = true;
                        lastName.setBackgroundResource(android.R.drawable.edit_text);
                    }
                }
            }
        });



        confirmText = findViewById(R.id.passwordConfirm);
        confirmText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                }
                else {
                    if (String.valueOf(confirmText.getText()).equals(String.valueOf(passText.getText()))) {
                        confirmText.setBackgroundResource(android.R.drawable.edit_text);
                        confirmGood = true;
                    }
                    else {

                        Toast.makeText(SignupActivity.this, "Passwords must match", Toast.LENGTH_SHORT).show();
                        Drawable customDrawable = getResources().getDrawable(R.drawable.edittext_border);
                        confirmText.setBackground(customDrawable);
                        confirmGood = false;
                    }
                }
            }
        });
        emailText = findViewById(R.id.enterEmailField);
        emailText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                }
                else {
                    if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailText.getText().toString()).matches()) {
                        emailText.setBackgroundResource(android.R.drawable.edit_text);
                        emailGood = true;
                    }
                    else {

                        Toast.makeText(SignupActivity.this, "Must enter valid email", Toast.LENGTH_SHORT).show();
                        Drawable customDrawable = getResources().getDrawable(R.drawable.edittext_border);
                        emailText.setBackground(customDrawable);
                        emailGood = false;
                    }
                }
            }
        });


        dobField = findViewById(R.id.dobField);
        dobField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    showDatePickerDialog();
                }
                else {
                    updateBirthday(String.valueOf((dobField.getText())));
                }
            }
        });

        Button homeButton = findViewById(R.id.returnButton); //Back to Log in screen
        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Button createButton = findViewById(R.id.createAccount); //Create account functionality
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (checkAll()) {
                    Intent intent = new Intent(SignupActivity.this, MainActivity.class);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(SignupActivity.this, "Check Red-Highlighted Input Fields", Toast.LENGTH_SHORT).show();
                }

            }
        });

        RadioGroup radioHolder = findViewById(R.id.radioGroup);
        TextView referralLabel = findViewById(R.id.referralCodeLabel);
        referralText = findViewById(R.id.referralCodeText);

        radioHolder.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // Check which radio button was selected
                if (checkedId == R.id.providerSelector) {
                    // If the specific radio button is selected, hide the TextView
                    referralLabel.setVisibility(View.GONE);
                    referralText.setVisibility(View.GONE);
                    referGood = true;
                } else {
                    // If other radio buttons are selected, make the TextView visible
                    referralLabel.setVisibility(View.VISIBLE);
                    referralText.setVisibility(View.VISIBLE);
                }
            }
        });

        referralText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                }
                else {
                    if (!(String.valueOf(referralText.getText()).equals(""))) {
                        referralText.setBackgroundResource(android.R.drawable.edit_text);
                        referGood = true;
                    }
                    else {

                        //Toast.makeText(SignupActivity.this, "Must enter referral code", Toast.LENGTH_SHORT).show();
                        Drawable customDrawable = getResources().getDrawable(R.drawable.edittext_border);
                        referralText.setBackground(customDrawable);
                        referGood = false;
                    }
                }
            }
        });

        phoneField = findViewById(R.id.phoneField);

        // Add a TextWatcher to format the phone number as the user types
        phoneField.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        phoneField.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {

                }
                else {
                    if (String.valueOf(phoneField.getText()).length() == 12 && !(String.valueOf(phoneField.getText()).equals(""))) {
                        phoneField.setBackgroundResource(android.R.drawable.edit_text);
                        phoneGood = true;
                    }
                    else {

                        Toast.makeText(SignupActivity.this, "Phone number is formatted incorrectly", Toast.LENGTH_SHORT).show();
                        Drawable customDrawable = getResources().getDrawable(R.drawable.edittext_border);
                        phoneField.setBackground(customDrawable);
                        phoneGood = false;
                    }
                }
            }
        });
    }
    private boolean checkAll() {
        if (!(String.valueOf(referralText.getText()).equals(""))) {
            referralText.setBackgroundResource(android.R.drawable.edit_text);
            referGood = true;
        }
        else {
            //Toast.makeText(SignupActivity.this, "Must enter referral code", Toast.LENGTH_SHORT).show();
            Drawable customDrawable = getResources().getDrawable(R.drawable.edittext_border);
            referralText.setBackground(customDrawable);
            referGood = false;
        }
        updateBirthday(String.valueOf((dobField.getText())));

        if (String.valueOf(phoneField.getText()).length() == 12 && !(String.valueOf(phoneField.getText()).equals(""))) {
            phoneField.setBackgroundResource(android.R.drawable.edit_text);
            phoneGood = true;
        }
        else {
            //Toast.makeText(SignupActivity.this, "Phone number is formatted incorrectly", Toast.LENGTH_SHORT).show();
            Drawable customDrawable = getResources().getDrawable(R.drawable.edittext_border);
            phoneField.setBackground(customDrawable);
            phoneGood = false;
        }
        if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailText.getText().toString()).matches()) {
            emailText.setBackgroundResource(android.R.drawable.edit_text);
            emailGood = true;
        }
        else {
            //Toast.makeText(SignupActivity.this, "Must enter valid email", Toast.LENGTH_SHORT).show();
            Drawable customDrawable = getResources().getDrawable(R.drawable.edittext_border);
            emailText.setBackground(customDrawable);
            emailGood = false;
        }
        if (String.valueOf(passText.getText()).equals("")) {
            //Toast.makeText(SignupActivity.this, "Must enter a password", Toast.LENGTH_SHORT).show();
            Drawable customDrawable = getResources().getDrawable(R.drawable.edittext_border);
            passText.setBackground(customDrawable);
            passGood = false;
        }
        else {
            passGood = true;
            passText.setBackgroundResource(android.R.drawable.edit_text);
        }
        if (String.valueOf(usernameText.getText()).equals("")) { //Also need to check if username is same as any in database
            //Toast.makeText(SignupActivity.this, "Must enter a username", Toast.LENGTH_SHORT).show();
            Drawable customDrawable = getResources().getDrawable(R.drawable.edittext_border);
            usernameText.setBackground(customDrawable);
            userGood = false;
        }
        else {
            userGood = true;
            usernameText.setBackgroundResource(android.R.drawable.edit_text);
        }
        if (String.valueOf(firstName.getText()).equals("")) {
           // Toast.makeText(SignupActivity.this, "Must enter a First name", Toast.LENGTH_SHORT).show();
            Drawable customDrawable = getResources().getDrawable(R.drawable.edittext_border);
            firstName.setBackground(customDrawable);
            firstGood = false;
        }
        else {
            firstGood = true;
            firstName.setBackgroundResource(android.R.drawable.edit_text);
        }
        if (String.valueOf(lastName.getText()).equals("")) {
            //Toast.makeText(SignupActivity.this, "Must enter a Last name", Toast.LENGTH_SHORT).show();
            Drawable customDrawable = getResources().getDrawable(R.drawable.edittext_border);
            lastName.setBackground(customDrawable);
            lastGood = false;
        }
        else {
            lastGood = true;
            lastName.setBackgroundResource(android.R.drawable.edit_text);
        }
        if (String.valueOf(confirmText.getText()).equals(String.valueOf(passText.getText()))) {
            confirmText.setBackgroundResource(android.R.drawable.edit_text);
            confirmGood = true;
        }
        else {
            //Toast.makeText(SignupActivity.this, "Passwords must match", Toast.LENGTH_SHORT).show();
            Drawable customDrawable = getResources().getDrawable(R.drawable.edittext_border);
            confirmText.setBackground(customDrawable);
            confirmGood = false;
        }
        if (userGood && firstGood && lastGood && passGood && confirmGood && emailGood && phoneGood && dobGood && referGood) {
            return true;
        }
        else {
            return false;
        }

    }

    private void showDatePickerDialog() {
        // Create an instance of the DatePickerFragment
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }

    // Update the TextView with the selected birthday
    public void updateBirthday(String birthday) {
        if (isValidDate(birthday)) {
            dobField.setBackgroundResource(android.R.drawable.edit_text);
            dobField.setText(birthday);
            dobGood = true;
        }
        else {
            dobGood = false;
            Drawable customDrawable = getResources().getDrawable(R.drawable.edittext_border);
            dobField.setBackground(customDrawable);
        }

    }
    private boolean isValidDate(String date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        dateFormat.setLenient(false);

        try {
            // Attempt to parse the date
            Date parsedDate = dateFormat.parse(date);
            return parsedDate != null;
        } catch (ParseException e) {
            // ParseException will be thrown if the date is in an incorrect format
            return false;
        }
    }
    private class PhoneNumberFormattingTextWatcher implements TextWatcher {

        private boolean isFormatting;
        private int previousLength;

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            if (isFormatting) {
                return;
            }
            previousLength = s.length();
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (isFormatting) {
                return;
            }

            // Save cursor position before formatting
            int cursorPosition = phoneField.getSelectionStart();

            String formattedNumber = formatPhoneNumber(s.toString());
            isFormatting = true;
            phoneField.setText(formattedNumber);

            // Calculate the cursor position after formatting
            int newCursorPosition = calculateNewCursorPosition(cursorPosition, s.length(), previousLength, formattedNumber);
            phoneField.setSelection(newCursorPosition);

            isFormatting = false;
        }

        @Override
        public void afterTextChanged(Editable s) {
            // No need to implement anything here
        }

        private int calculateNewCursorPosition(int cursorPosition, int currentLength, int previousLength, String formattedNumber) {
            // Ensure the cursor is always to the right
            if (previousLength < currentLength) {
                return Math.max(formattedNumber.length(), cursorPosition);
            }
            else {
                return Math.min(formattedNumber.length(), cursorPosition);
            }

        }

        private String formatPhoneNumber(String phoneNumber) {
            StringBuilder formatted = new StringBuilder();
            int digitCount = 0;

            for (int i = 0; i < phoneNumber.length(); i++) {
                char c = phoneNumber.charAt(i);
                if (Character.isDigit(c)) {
                    formatted.append(c);
                    digitCount++;
                    if (digitCount == 3 || digitCount == 6) {
                        formatted.append('-');
                    }
                }
            }

            return formatted.toString();
        }
    }



}

