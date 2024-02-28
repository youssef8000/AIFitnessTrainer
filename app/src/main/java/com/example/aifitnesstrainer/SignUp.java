package com.example.aifitnesstrainer;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class SignUp extends AppCompatActivity {
    EditText signupName,signupEmail, signupPhone,signupPassword, signupConfirm,birthday;
    DatabaseHelper databaseHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        databaseHelper = new DatabaseHelper(this);
        birthday=findViewById(R.id.signup_date);
        signupName = findViewById(R.id.signup_name);
        signupEmail = findViewById(R.id.signup_email);
        signupPhone = findViewById(R.id.signup_phone);
//        signupPassword = findViewById(R.id.signup_password);
//        signupConfirm = findViewById(R.id.signup_confirm);
        Calendar calendar1=Calendar.getInstance();
        DatePickerDialog.OnDateSetListener date=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

                calendar1.set(Calendar.YEAR,year);
                calendar1.set(Calendar.MONTH,month);
                calendar1.set(Calendar.DAY_OF_MONTH,dayOfMonth);
                updatecalendar();
            }
            private void updatecalendar(){
                String formate="MM/dd/yy";
                SimpleDateFormat sdf=new SimpleDateFormat(formate, Locale.US);
                birthday.setText(sdf.format(calendar1.getTime()));
            }
        };
        birthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(SignUp.this,
                        R.style.MyDatePickerDialogStyle,date,calendar1.get(Calendar.YEAR),calendar1.get(Calendar.MONTH),
                        calendar1.get(Calendar.DAY_OF_MONTH)).show();
            }
        });
        findViewById(R.id.signup_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = signupName.getText().toString();
                String email = signupEmail.getText().toString();
                String phone =signupPhone.getText().toString();
                String randomPassword = generateRandomPassword();
                String hashedPassword = hashPassword(randomPassword);
                SendMail(email,name,phone,randomPassword);
                String birthdate = birthday.getText().toString();
                if (name.equals("") ||birthdate.equals("") ||email.equals("") || phone.equals(""))
                    Toast.makeText(SignUp.this, "All fields are mandatory", Toast.LENGTH_SHORT).show();
                else {
                    if (email.contains("@gmail.com")) {
                        Boolean checkUserEmail = databaseHelper.checkEmail(email);
                        if (!checkUserEmail) {
                            Boolean insert = databaseHelper.insertuser(email,name,phone,hashedPassword,birthdate);
                            if (insert) {
                                Toast.makeText(SignUp.this, "Signup Successfully, please check your email to know your password", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(getApplicationContext(), Login.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(SignUp.this, "Signup Failed!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(SignUp.this, "User already exists! Please login", Toast.LENGTH_SHORT).show();
                        }
                }
                    else {
                        Toast.makeText(SignUp.this, "Please enter a valid Gmail address", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        findViewById(R.id.loginRedirectText).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignUp.this, Login.class);
                startActivity(intent);
            }
        });
    }
    private String generateRandomPassword() {
        String allowedChars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890#@$!&*";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) { // Generate an 8-character random password
            int index = (int) (Math.random() * allowedChars.length());
            sb.append(allowedChars.charAt(index));
        }
        return sb.toString();
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
    private void SendMail(String email, String name,String phone,String password) {
        String subject = "Your password to log in to the application";
        String message = "Dear, " + name
                + "\nYou SignUp successfully and this your data"
                + "\nYour Name: " + name
                + "\nYour email: " + email
                + "\nYour Phone number: " + phone
                + "\nYour password: " + password;
        JavaMailAPI javaMailAPI = new JavaMailAPI(this, email, subject, message);
        javaMailAPI.execute();
    }
}