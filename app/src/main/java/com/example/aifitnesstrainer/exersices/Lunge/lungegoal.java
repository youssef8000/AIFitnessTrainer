package com.example.aifitnesstrainer.exersices.Lunge;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.aifitnesstrainer.DatabaseHelper;
import com.example.aifitnesstrainer.R;
import com.example.aifitnesstrainer.view_camera;

public class lungegoal extends Fragment {


    public lungegoal() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_lungegoal, container, false);
        DatabaseHelper databaseHelper = new DatabaseHelper(requireContext());
        TextView name=view.findViewById(R.id.lunge_name);
        EditText goal=view.findViewById(R.id.set_goal_lunge);
        Button squat_camera_start = view.findViewById(R.id.lunge_camera_start);
        SharedPreferences preferences = requireActivity().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String userEmail = preferences.getString("user_email", "");
        squat_camera_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userGoal = goal.getText().toString();
                String exname = name.getText().toString();
                if (!userGoal.isEmpty() ) {
                    boolean inserted = databaseHelper.insertusergoal(userEmail,exname , Integer.parseInt(userGoal));
                    if (inserted) {
                        Toast.makeText(requireContext(), "Start Training", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(requireContext(), view_lunge_camera.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(requireContext(), "Failed to set your goal", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(requireContext(), "Please set your goal", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return view;
    }
}