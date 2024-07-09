package com.example.aifitnesstrainer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.opencv.android.OpenCVLoader;

public class homamainFragment extends Fragment {

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
    public homamainFragment() {
        // Required empty public constructor
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view= inflater.inflate(R.layout.fragment_homamain, container, false);
        TextView exercises=view.findViewById(R.id.exercise);
        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
        SharedPreferences preferences = requireContext().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String userEmail = preferences.getString("user_email", "");
        TextView trainerNameTextView = view.findViewById(R.id.trainer_name);
        User user = databaseHelper.getUserByEmail(userEmail);
        trainerNameTextView.setText("Welcome, " + user.getname());
        exercises.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(new exercises());
            }
        });
    return view;
    }
}