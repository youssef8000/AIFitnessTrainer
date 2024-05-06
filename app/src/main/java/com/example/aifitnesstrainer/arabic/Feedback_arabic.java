package com.example.aifitnesstrainer.arabic;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.aifitnesstrainer.Feedback;
import com.example.aifitnesstrainer.Login;
import com.example.aifitnesstrainer.MainActivity;
import com.example.aifitnesstrainer.R;
import com.example.aifitnesstrainer.userProfile;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class Feedback_arabic extends AppCompatActivity {
    private ImageButton logoutbtn;
    private Toolbar toolbar;
    ImageView english;
    private FragmentManager fragmentManager;
    private BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback_arabic);
        logoutbtn = findViewById(R.id.btn_logout);
        toolbar = findViewById(R.id.toolbar_nav);
        english =findViewById(R.id.btn_arabic);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        setSupportActionBar(toolbar);
        bottomNavigationView.setBackground(null);
        fragmentManager = getSupportFragmentManager();
        english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Feedback_arabic.this, MainActivity.class);
                startActivity(intent);
            }
        });
        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("remember", "false");
                editor.apply();
                Intent intent = new Intent(Feedback_arabic.this, Login.class);
                startActivity(intent);
                finish();
            }
        });

        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.home_ar) {
                    openFragment(new home_arabic());
                    return true;
                } else if (itemId == R.id.category_ar) {
                    openFragment(new exercises_arabic());
                    return true;
                }
                else if (itemId == R.id.profile_ar) {
                    openFragment(new userProfile_arabic());
                    return true;
                }
                else if (itemId == R.id.my_feedback_ar) {
                    openFragment(new UserFeedback_arabic());
                    return true;
                }
                return false;
            }
        });
        openFragment(new UserFeedback_arabic());
    }
    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

}