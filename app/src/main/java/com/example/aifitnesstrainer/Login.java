package com.example.aifitnesstrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Login extends AppCompatActivity {
    EditText loginEmail, loginPassword;
    CheckBox remember;
    TextView signupRedirectText, forgotPassword;
    DatabaseHelper databaseHelper;
    private static final long LOADING_DELAY_MS = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading); // Show the loading page initially
        // Delayed execution to switch to the login page after 3 seconds
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                showLoginPage(); // Switch to the login page after 3 seconds
            }
        }, LOADING_DELAY_MS);

    }
    // Method to hash the password using SHA-256
    public static String hashPassword(String password) {
        try {
            // Create a MessageDigest instance for SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            // Apply the hashing function to the password bytes
            byte[] hashBytes = digest.digest(password.getBytes());

            // Convert the byte array to a hexadecimal string representation
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            // Return the hashed password
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            // Handle the case where SHA-256 algorithm is not available
            e.printStackTrace();
            return null;
        }
    }
    private void saveUserInfo(String userEmail,int userId) {
        SharedPreferences preferences = getSharedPreferences("user_info", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("user_email", userEmail);
        editor.putInt("user_id", userId);
        editor.apply();
    }
    private void showLoginPage() {
        setContentView(R.layout.activity_login);
        databaseHelper = new DatabaseHelper(this);
        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        signupRedirectText = findViewById(R.id.signupRedirectText);
        forgotPassword = findViewById(R.id.ForgotPassword);
        remember=findViewById(R.id.rememberme);
        SharedPreferences preferences=getSharedPreferences("checkbox",MODE_PRIVATE);
        String checkbox=preferences.getString("remember","");
        if (checkbox.equals("true")){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }

        findViewById(R.id.login_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = loginEmail.getText().toString();
                String password = loginPassword.getText().toString();
                String hashedPassword = hashPassword(password);
                if (email.equals("") || password.equals("")) {
                    Toast.makeText(Login.this, "All fields are mandatory", Toast.LENGTH_SHORT).show();
                }else {
                    Boolean checkCredentials = databaseHelper.checkEmailPassword(email,hashedPassword);
                    if (checkCredentials) {
                        User loggedInUser = databaseHelper.getUserByEmail(email);
                        saveUserInfo(loggedInUser.getEmail(),loggedInUser.getId());
                        Toast.makeText(Login.this, "Login Successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(Login.this, "Invalid Credentials", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        remember.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean b) {
                if (buttonView.isChecked()){
                    SharedPreferences preferences=getSharedPreferences("checkbox",MODE_PRIVATE);
                    SharedPreferences.Editor editor=preferences.edit();
                    editor.putString("remember","true");
                    editor.apply();
                    Toast.makeText(Login.this, "Checked", Toast.LENGTH_SHORT).show();
                } else if (!buttonView.isChecked()) {
                    SharedPreferences preferences=getSharedPreferences("checkbox",MODE_PRIVATE);
                    SharedPreferences.Editor editor=preferences.edit();
                    editor.putString("remember","false");
                    editor.apply();
                    Toast.makeText(Login.this, "UnChecked", Toast.LENGTH_SHORT).show();
                }
            }
        });
        signupRedirectText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, SignUp.class);
                startActivity(intent);
            }
        });
        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Forgotten_password.class);
                startActivity(intent);
            }
        });
    }
}