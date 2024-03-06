package com.example.aifitnesstrainer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;


public class UserFeedback extends Fragment {
    public UserFeedback() {
    }
    private void openFragment(Fragment fragment) {
        FragmentManager fragmentManager = requireActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }
    @SuppressLint("Range")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_user_feedback, container, false);
        DatabaseHelper databaseHelper = new DatabaseHelper(requireContext());
        SharedPreferences preferences = requireContext().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String userEmail = preferences.getString("user_email", "");
        TextView trainer_name = view.findViewById(R.id.trainer_name);
        User user = databaseHelper.getUserByEmail(userEmail);
        trainer_name.setText("Hello,"+user.getname());
        Cursor cursor = databaseHelper.getAllFeedbackByEmail(userEmail);
        TableLayout tableLayout = view.findViewById(R.id.table);
        if (cursor.moveToFirst()) {
            do {
                TableRow row = new TableRow(requireContext());

                TextView textView1 = new TextView(requireContext());
                textView1.setText(cursor.getString(cursor.getColumnIndex("ex_name")));
                textView1.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                textView1.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                TextView textView2 = new TextView(requireContext());
                textView2.setText(String.valueOf(cursor.getInt(cursor.getColumnIndex("goal"))));
                textView2.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                textView2.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                TextView textView3 = new TextView(requireContext());
                double accuracy = cursor.getDouble(cursor.getColumnIndex("accuracy"));
                textView3.setText(String.format("%.2f%%", accuracy * 100));
                textView3.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                textView3.setLayoutParams(new TableRow.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                Button button = new Button(requireContext());
                button.setText("See Feedback");
                int feedbackId = cursor.getInt(cursor.getColumnIndex("id"));
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openFragment(new feedback_details(feedbackId));
                    }
                });
                button.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

                row.addView(textView1);
                row.addView(textView2);
                row.addView(textView3);
                row.addView(button);

                tableLayout.addView(row);
                View line = new View(requireContext());
                line.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 2));
                line.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.black));
                tableLayout.addView(line);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return view;
    }
}