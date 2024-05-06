package com.example.aifitnesstrainer.arabic;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import java.text.DecimalFormat;
import androidx.appcompat.widget.AppCompatButton;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.example.aifitnesstrainer.DatabaseHelper;
import com.example.aifitnesstrainer.R;
import com.example.aifitnesstrainer.User;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Pattern;

public class userProfile_arabic extends Fragment {
    private TextInputEditText editTextAge, editTextHeight, editTextWeight;
    private RadioGroup genderRadioGroup;
    private MaterialRadioButton maleRadioButton, femaleRadioButton;
    private TextView caloriesTextView, requiredTextView, textView1, textView2, textView3, textView4, textView5, textView6, textDummyTextView;
    private AppCompatButton calculateButton;
    public userProfile_arabic() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_profile_arabic, container, false);
        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
        SharedPreferences preferences = requireContext().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String userEmail = preferences.getString("user_email", "");
        Cursor cursor = databaseHelper.getCalorieDataByEmail(userEmail);
        // Initializing the variables
        editTextAge = view.findViewById(R.id.age);
        editTextHeight = view.findViewById(R.id.height);
        editTextWeight = view.findViewById(R.id.weight);
        genderRadioGroup = view.findViewById(R.id.gender);
        maleRadioButton = view.findViewById(R.id.male);
        femaleRadioButton = view.findViewById(R.id.female);
        caloriesTextView = view.findViewById(R.id.calories);
        textView1 = view.findViewById(R.id.textView1);
        textView2 = view.findViewById(R.id.textView2);
        textView3 = view.findViewById(R.id.textView3);
        textView4 = view.findViewById(R.id.textView4);
        textView5 = view.findViewById(R.id.textView5);
        textView6 = view.findViewById(R.id.textView6);
        textDummyTextView = view.findViewById(R.id.text_dummy);
        requiredTextView = view.findViewById(R.id.required);
        calculateButton = view.findViewById(R.id.calculate);

        if (cursor != null && cursor.moveToFirst()) {
            // Retrieve data from the cursor
            @SuppressLint("Range")
            double userWeight = cursor.getDouble(cursor.getColumnIndex("weight"));
            @SuppressLint("Range")
            double userHeight = cursor.getDouble(cursor.getColumnIndex("height"));
            @SuppressLint("Range")
            int userAge = (int) cursor.getDouble(cursor.getColumnIndex("age"));
            @SuppressLint("Range")
            double noExercise = cursor.getDouble(cursor.getColumnIndex("no_exercise"));
            @SuppressLint("Range")
            double exercise1_3 = cursor.getDouble(cursor.getColumnIndex("exercise_1_3"));
            @SuppressLint("Range")
            double exercise4_5 = cursor.getDouble(cursor.getColumnIndex("exercise_4_5"));
            @SuppressLint("Range")
            double intenseExercise3_4 = cursor.getDouble(cursor.getColumnIndex("intense_exercise_3_4"));
            @SuppressLint("Range")
            double intenseExercise6_7 = cursor.getDouble(cursor.getColumnIndex("intense_exercise_6_7"));
            @SuppressLint("Range")
            double veryIntenseExercise = cursor.getDouble(cursor.getColumnIndex("very_intense_exercise"));

            editTextAge.setText(String.valueOf(userAge));
            editTextWeight.setText(String.valueOf(userWeight));
            editTextHeight.setText(String.valueOf(userHeight));
            @SuppressLint("Range")
            String genderFromDB = cursor.getString(cursor.getColumnIndex("gender"));
            if ("male".equals(genderFromDB)) {
                maleRadioButton.setChecked(true);
            } else if ("female".equals(genderFromDB)) {
                femaleRadioButton.setChecked(true);
            }
            textView1.setText(String.valueOf(noExercise));
            textView2.setText(String.valueOf(exercise1_3));
            textView3.setText(String.valueOf(exercise4_5));
            textView4.setText(String.valueOf(intenseExercise3_4));
            textView5.setText(String.valueOf(intenseExercise6_7));
            textView6.setText(String.valueOf(veryIntenseExercise));
        }
        if (cursor != null) {
            cursor.close();
        }
        calculateButton.setOnClickListener(v -> {
            // Getting the values from the text fields
            String ageText = editTextAge.getText().toString();
            String heightText = editTextHeight.getText().toString();
            String weightText = editTextWeight.getText().toString();
            // This will check if the value is a number or not
            Pattern pattern = Pattern.compile("\\d+(\\.\\d+)?");
            boolean ageCheck = false;
            boolean heightCheck = false;
            boolean weightCheck = false;
            // Checking if the age text field is empty or not
            if (ageText.isEmpty()) {
                editTextAge.setError("Please enter your age");
                editTextAge.requestFocus();
                ageCheck = false;
            } else if (!pattern.matcher(ageText).matches()) {
                editTextAge.setError("Please enter your age correctly");
                editTextAge.requestFocus();
                ageCheck = false;
            } else {
                editTextAge.setError(null);
                ageCheck = true;
            }
            // Checking if the height text field is empty or not
            if (heightText.isEmpty()) {
                editTextHeight.setError("Please enter your height");
                editTextHeight.requestFocus();
                heightCheck = false;
            } else if (!pattern.matcher(heightText).matches()) {
                editTextHeight.setError("Please enter your height correctly");
                editTextHeight.requestFocus();
                heightCheck = false;
            } else {
                editTextHeight.setError(null);
                heightCheck = true;
            }
            // Checking if the weight text field is empty or not
            if (weightText.isEmpty()) {
                editTextWeight.setError("Please enter your weight");
                editTextWeight.requestFocus();
                weightCheck = false;
            } else if (!pattern.matcher(weightText).matches()) {
                editTextWeight.setError("Please enter your weight correctly");
                editTextWeight.requestFocus();
                weightCheck = false;
            } else {
                editTextWeight.setError(null);
                weightCheck = true;
            }
            // Checking if the user has selected the gender or not
            if (genderRadioGroup.getCheckedRadioButtonId() == -1) {
                requiredTextView.setText("Please Select Gender");
                requiredTextView.setVisibility(View.VISIBLE);
            } else {
                requiredTextView.setText("");
                requiredTextView.setVisibility(View.GONE);

                // Checking if all the values are not empty
                if (ageCheck && heightCheck && weightCheck) {
                    // Calling the calculateCalorie method
                    calculateCalorie();
                }
            }
        });

        return view;
    }
    public void calculateCalorie() {
        // Getting the values from the text fields
        double ageValue = Double.parseDouble(editTextAge.getText().toString());
        double heightValue = Double.parseDouble(editTextHeight.getText().toString());
        double weightValue = Double.parseDouble(editTextWeight.getText().toString());
        double totalCalories = 0;

        if (genderRadioGroup.getCheckedRadioButtonId() == maleRadioButton.getId()) {
            totalCalories = (10 * weightValue) + (6.25 * heightValue) - (5 * ageValue + 5);
            textDummyTextView.setVisibility(View.VISIBLE);
        } else {
            totalCalories = (10 * weightValue) + (6.25 * heightValue) - (5 * ageValue - 161);
            caloriesTextView.setText(String.format("%.2f", totalCalories) + "*");
            textDummyTextView.setVisibility(View.VISIBLE);
        }
        DecimalFormat arabicFormat = new DecimalFormat("#,###.##");
        arabicFormat.setDecimalFormatSymbols(new DecimalFormatSymbols(new Locale("ar", "AE")));

        double noExercise = totalCalories;
        textView1.setText(arabicFormat.format(noExercise) + "*");
        double exercise1 = totalCalories * 1.149;
        textView2.setText(arabicFormat.format(exercise1) + "*");
        double exercise2 = totalCalories * 1.220;
        textView3.setText(arabicFormat.format(exercise2) + "*");
        double exercise3 = totalCalories * 1.292;
        textView4.setText(arabicFormat.format(exercise3) + "*");
        double exercise4 = totalCalories * 1.437;
        textView5.setText(arabicFormat.format(exercise4) + "*");
        double exercise5 = totalCalories * 1.583;
        textView6.setText(arabicFormat.format(exercise5) + "*");
        requiredTextView.setTextSize(12);
        requiredTextView.setVisibility(View.VISIBLE);

        DatabaseHelper databaseHelper = new DatabaseHelper(getContext());
        SharedPreferences preferences = getContext().getSharedPreferences("user_info", Context.MODE_PRIVATE);
        String userEmail = preferences.getString("user_email", "");
        String genderValue = (genderRadioGroup.getCheckedRadioButtonId() == maleRadioButton.getId()) ? "male" : "female";
        Cursor cursor = databaseHelper.getCalorieDataByEmail(userEmail);

        if (cursor != null && cursor.moveToFirst()) {
            boolean update = databaseHelper.updateCalorieData(userEmail, weightValue, heightValue, (int) ageValue, genderValue,
                    noExercise, exercise1, exercise2, exercise3, exercise4, exercise5);
            if (update) {
                Toast.makeText(getContext(), "Update calories successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Update calories failed", Toast.LENGTH_SHORT).show();
            }
        } else {
            boolean insert = databaseHelper.insertCalorieData(userEmail, weightValue, heightValue, (int) ageValue, genderValue,
                    noExercise, exercise1, exercise2, exercise3, exercise4, exercise5);
            if (insert) {
                Toast.makeText(getContext(), "Insert calories successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Insert calories failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}