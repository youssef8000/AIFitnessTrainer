package com.example.aifitnesstrainer;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(@Nullable Context context) {
        super(context, "FitnessDB.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase MyDatabase) {
        try {
            MyDatabase.execSQL("CREATE TABLE users(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "email TEXT UNIQUE," +
                    "name TEXT," +
                    "phone TEXT," +
                    "password TEXT," +
                    "date TEXT)");
            MyDatabase.execSQL("CREATE TABLE set_goals(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "email TEXT," +
                    "ex_name TEXT," +
                    "goal INTEGER)");
            MyDatabase.execSQL("CREATE TABLE feedback(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "email TEXT," +
                    "ex_name TEXT," +
                    "goal INTEGER ," +
                    "correct_score INTEGER," +
                    "incorrect_score INTEGER," +
                    "accuracy DECIMAL," +
                    "workoutfeedback TEXT)");
            MyDatabase.execSQL("CREATE TABLE calories(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "email TEXT," +
                    "weight DECIMAL," +
                    "height DECIMAL ," +
                    "age INTEGER," +
                    "gender TEXT," +
                    "no_exercise TEXT," +
                    "exercise_1_3 TEXT," +
                    "exercise_4_5 TEXT," +
                    "intense_exercise_3_4 TEXT," +
                    "intense_exercise_6_7 TEXT," +
                    "very_intense_exercise TEXT)");

        }catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onUpgrade(SQLiteDatabase MyDatabase, int oldVersion, int newVersion) {
        try {
            MyDatabase.execSQL("DROP TABLE IF EXISTS users");
            MyDatabase.execSQL("DROP TABLE IF EXISTS set_goals");
            onCreate(MyDatabase);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public Boolean insertuser(String email,String name,String phone, String password,String birthdate){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("email", email);
        contentValues.put("name", name);
        contentValues.put("phone", phone);
        contentValues.put("password", password);
        contentValues.put("date", birthdate);
        long result = MyDatabase.insert("users", null, contentValues);
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }
    public Boolean insertusergoal(String email,String ex_name,int goal){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("email", email);
        contentValues.put("ex_name", ex_name);
        contentValues.put("goal", goal);
        long result = MyDatabase.insert("set_goals", null, contentValues);
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }
    public Boolean insertuserfeedback(String email, String ex_name, int goal, int correct_score, int incorrect_score,
                                      double accuracy, String workoutfeedback) {
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("email", email);
        contentValues.put("ex_name", ex_name);
        contentValues.put("goal", goal);
        contentValues.put("correct_score", correct_score);
        contentValues.put("incorrect_score", incorrect_score);
        contentValues.put("accuracy", accuracy);
        contentValues.put("workoutfeedback", workoutfeedback);

        long result = MyDatabase.insert("feedback", null, contentValues);
        return result != -1;
    }
    public boolean insertCalorieData(String email, double weight, double height, int age,
                                     String gender, double noExercise, double exercise1_3, double exercise4_5,
                                     double intenseExercise3_4, double intenseExercise6_7, double veryIntenseExercise) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("email", email);
        values.put("weight", weight);
        values.put("height", height);
        values.put("age", age);
        values.put("gender", gender);
        values.put("no_exercise", noExercise);
        values.put("exercise_1_3", exercise1_3);
        values.put("exercise_4_5", exercise4_5);
        values.put("intense_exercise_3_4", intenseExercise3_4);
        values.put("intense_exercise_6_7", intenseExercise6_7);
        values.put("very_intense_exercise", veryIntenseExercise);

        long result = db.insert("calories", null, values);
        db.close();

        return result != -1;
    }
    public Cursor getCalorieDataByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {
                "email",
                "weight",
                "height",
                "age",
                "gender",
                "no_exercise",
                "exercise_1_3",
                "exercise_4_5",
                "intense_exercise_3_4",
                "intense_exercise_6_7",
                "very_intense_exercise"
        };

        String selection = "email = ?";
        String[] selectionArgs = {email};

        Cursor cursor = db.query(
                "calories",  // The table to query
                projection,  // The array of columns to return (null to get all)
                selection,   // The columns for the WHERE clause
                selectionArgs,  // The values for the WHERE clause
                null,        // don't group the rows
                null,        // don't filter by row groups
                null         // don't sort the rows
        );

        // Make sure to close the database after using the cursor in your activity or fragment
        return cursor;
    }
    public boolean updateCalorieData(String email, double weight, double height, int age,
                                     String gender, double noExercise, double exercise1_3, double exercise4_5,
                                     double intenseExercise3_4, double intenseExercise6_7, double veryIntenseExercise) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("weight", weight);
        values.put("height", height);
        values.put("age", age);
        values.put("gender", gender);
        values.put("no_exercise", noExercise);
        values.put("exercise_1_3", exercise1_3);
        values.put("exercise_4_5", exercise4_5);
        values.put("intense_exercise_3_4", intenseExercise3_4);
        values.put("intense_exercise_6_7", intenseExercise6_7);
        values.put("very_intense_exercise", veryIntenseExercise);

        int rowsAffected = db.update("calories", values, "email = ?", new String[]{email});
        db.close();

        return rowsAffected > 0;
    }
    public User getUserByEmail(String email) {
        SQLiteDatabase MyDatabase = this.getReadableDatabase();
        Cursor cursor = MyDatabase.rawQuery("SELECT * FROM users WHERE email = ?", new String[]{email});
        User user = null;
        try {
            if (cursor != null && cursor.moveToFirst()) {
                int idColumnIndex = cursor.getColumnIndex("id");
                int emailColumnIndex = cursor.getColumnIndex("email");
                int nameColumnIndex = cursor.getColumnIndex("name");
                int phoneColumnIndex = cursor.getColumnIndex("phone");
                int dateColumnIndex = cursor.getColumnIndex("date");
                int userId = cursor.getInt(idColumnIndex);
                String userEmail = cursor.getString(emailColumnIndex);
                String name = cursor.getString(nameColumnIndex);
                String phone = cursor.getString(phoneColumnIndex);
                String userbirthdate = cursor.getString(dateColumnIndex);
                user = new User(userId, userEmail,name,phone,userbirthdate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return user;
    }
    public user_goal getUsergoalByEmail(String email) {
        SQLiteDatabase MyDatabase = this.getReadableDatabase();
        String[] projection = {"id", "email", "ex_name", "goal"};
        String selection = "email = ?"; // WHERE clause
        String[] selectionArgs = {email}; // Values for the WHERE clause
        String sortOrder = "id DESC"; // Order by ID in descending order
        Cursor cursor = MyDatabase.query("set_goals", projection, selection, selectionArgs, null, null, sortOrder);
        if (cursor != null && cursor.moveToFirst()) {
            // Extract user data from the cursor
            int idIndex = cursor.getColumnIndex("id");
            int emailIndex = cursor.getColumnIndex("email");
            int nameIndex = cursor.getColumnIndex("ex_name");
            int goalIndex = cursor.getColumnIndex("goal");

            int id = cursor.getInt(idIndex);
            String userEmail = cursor.getString(emailIndex);
            String userName = cursor.getString(nameIndex);
            int userGoal = cursor.getInt(goalIndex);

            // Create and return a user_goal object
            return new user_goal(id, userEmail, userName, userGoal);
        } else {
            // User not found or database error
            return null;
        }
    }
    public Cursor getAllFeedbackByEmail(String email) {
        SQLiteDatabase MyDatabase = this.getReadableDatabase();
        String[] columns = {"id","ex_name", "goal", "correct_score", "incorrect_score", "accuracy", "workoutfeedback"};
        String selection = "email=?";
        String[] selectionArgs = {email};

        // Query the database
        Cursor cursor = MyDatabase.query("feedback", columns, selection, selectionArgs, null, null, null);

        return cursor;
    }
    public user_feedback getFeedbackById(int feedbackId) {
        SQLiteDatabase db = this.getReadableDatabase();
        user_feedback feedback = null;

        Cursor cursor = db.rawQuery("SELECT * FROM feedback WHERE id = ?", new String[]{String.valueOf(feedbackId)});
        try {
            if (cursor != null && cursor.moveToFirst()) {
                int idColumnIndex = cursor.getColumnIndex("id");
                int emailColumnIndex = cursor.getColumnIndex("email");
                int exNameColumnIndex = cursor.getColumnIndex("ex_name");
                int goalColumnIndex = cursor.getColumnIndex("goal");
                int correctScoreColumnIndex = cursor.getColumnIndex("correct_score");
                int incorrectScoreColumnIndex = cursor.getColumnIndex("incorrect_score");
                int accuracyColumnIndex = cursor.getColumnIndex("accuracy");
                int workoutFeedbackColumnIndex = cursor.getColumnIndex("workoutfeedback");

                int fetchedId = cursor.getInt(idColumnIndex);
                String email = cursor.getString(emailColumnIndex);
                String exName = cursor.getString(exNameColumnIndex);
                int goal = cursor.getInt(goalColumnIndex);
                int correctScore = cursor.getInt(correctScoreColumnIndex);
                int incorrectScore = cursor.getInt(incorrectScoreColumnIndex);
                double accuracy = cursor.getDouble(accuracyColumnIndex);
                String workoutFeedback = cursor.getString(workoutFeedbackColumnIndex);

                feedback = new user_feedback(fetchedId, email, exName, goal, correctScore, incorrectScore, accuracy, workoutFeedback);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return feedback;
    }
    public Boolean updatePassword(String email, String password){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("password", password);
        long result = MyDatabase.update("users", contentValues, "email=?",new String[]{email});
        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }
    public Boolean checkEmail(String email){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        Cursor cursor = MyDatabase.rawQuery("Select * from users where email = ?", new String[]{email});
        if(cursor.getCount() > 0) {
            return true;
        }else {
            return false;
        }
    }
    public Boolean checkEmailPassword(String email, String password){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        Cursor cursor = MyDatabase.rawQuery("Select * from users where email = ? and password = ?", new String[]{email, password});
        if (cursor.getCount() > 0) {
            return true;
        }else {
            return false;
        }
    }

}
