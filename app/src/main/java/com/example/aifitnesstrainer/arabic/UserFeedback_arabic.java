package com.example.aifitnesstrainer.arabic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
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

import com.example.aifitnesstrainer.DatabaseHelper;
import com.example.aifitnesstrainer.R;
import com.example.aifitnesstrainer.User;
import com.example.aifitnesstrainer.feedback_details;

import java.text.NumberFormat;
import java.util.Locale;

public class UserFeedback_arabic extends Fragment {

    public UserFeedback_arabic() {
        // Required empty public constructor
    } private void openFragment(Fragment fragment) {
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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_feedback_arabic, container, false);
        DatabaseHelper databaseHelper = new DatabaseHelper(requireContext());
        SharedPreferences preferences = requireContext().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String userEmail = preferences.getString("user_email", "");
        Cursor cursor = databaseHelper.getAllFeedbackByEmail(userEmail);
        TableLayout tableLayout = view.findViewById(R.id.table);
        if (cursor.moveToFirst()) {
            do {
                TableRow row = new TableRow(requireContext());

                TextView textView1 = new TextView(requireContext());
                textView1.setText(cursor.getString(cursor.getColumnIndex("ex_name")));
                textView1.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                TableRow.LayoutParams layoutParams1 = new TableRow.LayoutParams(50, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                layoutParams1.setMargins(0, 0, 5, 0);
                textView1.setLayoutParams(layoutParams1);

                TextView textView2 = new TextView(requireContext());
                int goal = cursor.getInt(cursor.getColumnIndex("goal"));
                NumberFormat numberFormat = NumberFormat.getNumberInstance(new Locale("ar", "AE"));
                String arabicGoal = numberFormat.format(goal);
                textView2.setText(arabicGoal);
                textView2.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                textView2.setLayoutParams(new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
                TableRow.LayoutParams layoutParams2 = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                layoutParams2.setMargins(0, 0, 5, 0);
                textView2.setLayoutParams(layoutParams2);

                TextView textView3 = new TextView(requireContext());
                double accuracy = cursor.getDouble(cursor.getColumnIndex("accuracy"));
                NumberFormat numberFormat_acc = NumberFormat.getNumberInstance(new Locale("ar"));
                String arabicAccuracy = numberFormat_acc.format(accuracy * 100);
                textView3.setText(String.format("%s%%", arabicAccuracy));
                textView3.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                TableRow.LayoutParams layoutParams3 = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
                layoutParams3.setMargins(0, 0, 5, 0);
                textView3.setLayoutParams(layoutParams3);

                TextView button = new Button(requireContext());
                button.setText("انظر ردود الفعل");
                int feedbackId = cursor.getInt(cursor.getColumnIndex("id"));
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openFragment(new feedback_details_arabic(feedbackId));
                    }
                });
                button.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                button.setTextColor(Color.BLACK);
                button.setBackgroundColor(Color.parseColor("#2ffc2b"));

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