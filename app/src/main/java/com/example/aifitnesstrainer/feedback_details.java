package com.example.aifitnesstrainer;

import android.database.Cursor;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class feedback_details extends Fragment {
    private int id;
    public feedback_details() {
    }
    public feedback_details(int id) {
       this.id=id;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view= inflater.inflate(R.layout.fragment_feedback_details, container, false);
        TextView exercise_name = view.findViewById(R.id.exercise_name);
        TextView exercise_goal = view.findViewById(R.id.exercise_goal);
        TextView exercise_correct = view.findViewById(R.id.exercise_correct);
        TextView exercise_incorrect = view.findViewById(R.id.exercise_incorrect);
        TextView exercise_accuracy = view.findViewById(R.id.exercise_accuracy);
        TextView exercise_feedback = view.findViewById(R.id.exercise_feedback);
        DatabaseHelper databaseHelper = new DatabaseHelper(requireContext());
        user_feedback feedback=databaseHelper.getFeedbackById(id);

        exercise_name.setText("Exercise Name: "+feedback.getEx_name());
        exercise_goal.setText("Your Goal: "+feedback.getGoal());
        exercise_correct.setText("Your Correct repetition: "+feedback.getCorrect_score());
        exercise_incorrect.setText("Your InCorrect repetition: "+feedback.getIncorrect_score());
        exercise_accuracy.setText("Your Accuracy: "+feedback.getAccuracy()*100+"%");
        exercise_feedback.setText("Our Feedback: "+feedback.getWorkoutFeedback());

        return view;
    }
}