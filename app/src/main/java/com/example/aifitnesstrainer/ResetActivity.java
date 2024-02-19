package com.example.aifitnesstrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ResetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset);
        TextView reset_user=findViewById(R.id.user_name_reset);
        EditText pass=findViewById(R.id.passwordtype);
        EditText repass=findViewById(R.id.repassword);
        Button confirmbtn=findViewById(R.id.confirm_Password);
        DatabaseHelper db =new DatabaseHelper(this);
        Intent intent =getIntent();
        reset_user.setText(intent.getStringExtra("email"));
        confirmbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user = reset_user.getText().toString();
                String password = pass.getText().toString();
                String repassword = repass.getText().toString();
                if (password.isEmpty() || repassword.isEmpty()) {
                    Toast.makeText(ResetActivity.this, "Please enter both password fields", Toast.LENGTH_SHORT).show();
                } else if (!password.equals(repassword)) {
                    Toast.makeText(ResetActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                } else {
                    DatabaseHelper db = new DatabaseHelper(ResetActivity.this);
                    Boolean checkUserUpdate = db.updatePassword(user, password);
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
}