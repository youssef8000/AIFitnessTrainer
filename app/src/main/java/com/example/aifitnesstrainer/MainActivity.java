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
import android.widget.ImageView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.aifitnesstrainer.arabic.MainActivity_arabic;
import com.example.aifitnesstrainer.databinding.ActivityMainBinding;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private ImageButton logoutbtn;
    private Toolbar toolbar;
    ImageView arabic;
    private FragmentManager fragmentManager;
    private BottomNavigationView bottomNavigationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        logoutbtn = findViewById(R.id.btn_logout);
        toolbar = findViewById(R.id.toolbar_nav);
        arabic =findViewById(R.id.btn_arabic);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        setSupportActionBar(toolbar);
        bottomNavigationView.setBackground(null);
        fragmentManager = getSupportFragmentManager();
        arabic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MainActivity_arabic.class);
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
                finish();
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
                else if (itemId == R.id.profile) {
                    openFragment(new userProfile());
                return true;
                }
                else if (itemId == R.id.my_feedback) {
                    openFragment(new UserFeedback());
                    return true;
                }
                return false;
            }
        });
        openFragment(new homamainFragment());
    }
    private void openFragment(Fragment fragment) {
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

}