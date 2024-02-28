package com.example.aifitnesstrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ResetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);
        TextView reset_user=findViewById(R.id.user_name_reset);
        EditText pass=findViewById(R.id.passwordtype);
        Button confirmbtn=findViewById(R.id.confirm_Password);
        DatabaseHelper db =new DatabaseHelper(this);
        Intent intent =getIntent();
        reset_user.setText(intent.getStringExtra("email"));
        confirmbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = reset_user.getText().toString();
                String password = pass.getText().toString();
                String hashedPassword = hashPassword(password);
                if (password.isEmpty()) {
                    Toast.makeText(ResetActivity.this, "Please enter password fields", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseHelper db = new DatabaseHelper(ResetActivity.this);
                    Boolean checkUserUpdate = db.updatePassword(user, hashedPassword);
                    if (checkUserUpdate) {
                        Intent intent = new Intent(getApplicationContext(), Login.class);
                        startActivity(intent);
                        Toast.makeText(ResetActivity.this, "Password updated", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ResetActivity.this, "Password not updated", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }
    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte hashByte : hashBytes) {
                String hex = Integer.toHexString(0xff & hashByte);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}