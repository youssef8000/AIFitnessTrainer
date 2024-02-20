package com.example.aifitnesstrainer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class Feedback extends AppCompatActivity {
    private ImageButton logoutbtn;
    private ImageButton feedback;
    private Toolbar toolbar;
    private FragmentManager fragmentManager;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);
        logoutbtn = findViewById(R.id.btn_logout);
        feedback = findViewById(R.id.btn_camera);
        toolbar = findViewById(R.id.toolbar_nav);
        bottomNavigationView = findViewById(R.id.bottom_nav);
        setSupportActionBar(toolbar);
        bottomNavigationView.setBackground(null);
        fragmentManager = getSupportFragmentManager();
        logoutbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences preferences = getSharedPreferences("checkbox", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("remember", "false");
                editor.apply();
                finish();
            }
        });
        feedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openUserFeedbackFragment();
            }
        });
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.home) {
                    openFragment(new homamainFragment());
                    return true;
                } else if (itemId == R.id.category) {
                    openFragment(new exercises());
                    return true;
                }
                return false;
            }
        });
        openFragment(new UserFeedback());
    }
    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }
    private void openUserFeedbackFragment() {
        // Create a new instance of the UserFeedback fragment
        UserFeedback userFeedbackFragment = new UserFeedback();

        // Begin a fragment transaction
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, userFeedbackFragment) // Replace fragment_container with the ID of your fragment container layout
                .addToBackStack(null) // Add the transaction to the back stack
                .commit();
    }
}