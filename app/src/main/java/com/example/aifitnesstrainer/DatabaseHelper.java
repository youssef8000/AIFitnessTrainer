package com.example.aifitnesstrainer;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    public DatabaseHelper(@Nullable Context context) {
        super(context, "newDB.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase MyDatabase) {
        try {
            MyDatabase.execSQL("CREATE TABLE users(" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "email TEXT UNIQUE," +
                    "name TEXT," +
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
    public Boolean insertuser(String email,String name, String password,String birthdate){
        SQLiteDatabase MyDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put("email", email);
        contentValues.put("name", name);
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

    public User getUserByEmail(String email) {
        SQLiteDatabase MyDatabase = this.getReadableDatabase();
        Cursor cursor = MyDatabase.rawQuery("SELECT * FROM users WHERE email = ?", new String[]{email});
        User user = null;
        try {
            if (cursor != null && cursor.moveToFirst()) {
                int idColumnIndex = cursor.getColumnIndex("id");
                int emailColumnIndex = cursor.getColumnIndex("email");
                int nameColumnIndex = cursor.getColumnIndex("name");
                int dateColumnIndex = cursor.getColumnIndex("date");
                int userId = cursor.getInt(idColumnIndex);
                String userEmail = cursor.getString(emailColumnIndex);
                String name = cursor.getString(nameColumnIndex);
                String userbirthdate = cursor.getString(dateColumnIndex);
                user = new User(userId, userEmail,name,userbirthdate);
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
        String[] columns = {"ex_name", "goal", "correct_score", "incorrect_score", "accuracy", "workoutfeedback"};
        String selection = "email=?";
        String[] selectionArgs = {email};

        // Query the database
        Cursor cursor = MyDatabase.query("feedback", columns, selection, selectionArgs, null, null, null);

        return cursor;
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
