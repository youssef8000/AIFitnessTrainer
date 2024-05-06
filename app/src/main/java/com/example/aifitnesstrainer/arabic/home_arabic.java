package com.example.aifitnesstrainer.arabic;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.aifitnesstrainer.DatabaseHelper;
import com.example.aifitnesstrainer.R;
import com.example.aifitnesstrainer.User;
import com.example.aifitnesstrainer.exercises;


public class home_arabic extends Fragment {

    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
    public home_arabic() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_arabic, container, false);
        TextView exercises=view.findViewById(R.id.exercise);
        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
        SharedPreferences preferences = requireContext().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String userEmail = preferences.getString("user_email", "");
        TextView trainerNameTextView = view.findViewById(R.id.trainer_name);
        User user = databaseHelper.getUserByEmail(userEmail);
        trainerNameTextView.setText("مرحبا, " + user.getname());
        exercises.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFragment(new exercises_arabic());
            }
        });

        return view;
    }
}