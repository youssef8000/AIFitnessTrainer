package com.example.aifitnesstrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class Forgotten_password extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgotten_password);
        EditText email=findViewById(R.id.email);
        Button resetbtn=findViewById(R.id.Reset_Password);
        DatabaseHelper db =new DatabaseHelper(this);
        resetbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String user=email.getText().toString();
                Boolean checkuser=db.checkEmail(user);
                if(checkuser==true){
                    Intent intent=new Intent(getApplicationContext(),ResetActivity.class);
                    intent.putExtra("email",user);
                    startActivity(intent);
                }else {
                    Toast.makeText(Forgotten_password.this, "user doesn't exists", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}