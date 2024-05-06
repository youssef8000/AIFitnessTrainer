package com.example.aifitnesstrainer.arabic.exersices_arabic.squat;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;

import com.example.aifitnesstrainer.R;
import com.example.aifitnesstrainer.exersices.squat.squatGoal;


public class squat_exercise_arabic extends Fragment {
    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
    public squat_exercise_arabic() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_squat_exercise_arabic, container, false);
        WebView gifWebView = view.findViewById(R.id.gifWebView);
        gifWebView.loadUrl("file:///android_res/drawable/squat_ezgif.gif");
        Button squat_exercise=view.findViewById(R.id.squat_camera);
        squat_exercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(new squatGoal_arabic());
            }
        });
        return view;
    }
}