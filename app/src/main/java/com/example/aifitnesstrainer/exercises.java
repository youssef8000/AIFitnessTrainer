package com.example.aifitnesstrainer;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.aifitnesstrainer.exersices.Dumbbell_Tricep.dumbbell__tricep;
import com.example.aifitnesstrainer.exersices.Lateral_raise.lateral_raise;
import com.example.aifitnesstrainer.exersices.Lunge.lunge_exercise;
import com.example.aifitnesstrainer.exersices.PushUp.pushUp;
import com.example.aifitnesstrainer.exersices.ShoulderPress.shoulderPress_exercise;
import com.example.aifitnesstrainer.exersices.squat.squat_exercise;


public class exercises extends Fragment {

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
    public exercises() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exercises, container, false);
        TextView squat_exercise=view.findViewById(R.id.Squat);
        TextView lunge_exercise=view.findViewById(R.id.lunge);
        TextView lateral_raises=view.findViewById(R.id.lateral_raises);
        TextView ShoulderPress=view.findViewById(R.id.Shoulder_press);
        TextView dumbbell_tricep=view.findViewById(R.id.dumbbell);
        TextView pushup=view.findViewById(R.id.Push_up);

        squat_exercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(new squat_exercise());
            }
        });
        lunge_exercise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(new lunge_exercise());
            }
        });
        lateral_raises.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(new lateral_raise());
            }
        });
        ShoulderPress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(new shoulderPress_exercise());
            }
        });
        dumbbell_tricep.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(new dumbbell__tricep());
            }
        });
        pushup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(new pushUp());
            }
        });
        return view;
    }
}