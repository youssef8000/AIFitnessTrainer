package com.example.aifitnesstrainer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class squat_goal extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_squat_goal, container, false);
        DatabaseHelper databaseHelper = new DatabaseHelper(requireContext());
        TextView name=view.findViewById(R.id.squat_name);
        EditText goal=view.findViewById(R.id.set_goal_squat);
        Button squat_camera_start = view.findViewById(R.id.squat_camera_start);
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
                        Intent intent = new Intent(requireContext(), view_camera.class);
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