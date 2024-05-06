package com.example.aifitnesstrainer.arabic.exersices_arabic.ShoulderPress;

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
import com.example.aifitnesstrainer.exersices.ShoulderPress.shoulderPress_goal;


public class shoulderPress_exercise_arabic extends Fragment {

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    public shoulderPress_exercise_arabic() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_shoulder_press_exercise_arabic, container, false);
        WebView gifWebView = view.findViewById(R.id.giflungeView);
        gifWebView.loadUrl("file:///android_res/drawable/press_ezgif.gif");
        Button lunge_exercise=view.findViewById(R.id.lunge_camera);
        lunge_exercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(new shoulderPress_goal_arabic());
            }
        });
        return view;
    }
}