package com.example.aifitnesstrainer;

public class user_feedback {
    private int id;
    private String email;
    private String ex_name;
    private int goal;
    private int correct_score;
    private int incorrect_score;
    private double accuracy;
    private String workoutfeedback;

    public user_feedback(int id, String email,String ex_name, int goal, int correct_score, int incorrect_score
            , double accuracy, String workoutfeedback) {
        this.id = id;
        this.email = email;
        this.ex_name=ex_name;
        this.goal = goal;
        this.correct_score = correct_score;
        this.incorrect_score = incorrect_score;
        this.accuracy = accuracy;
        this.workoutfeedback = workoutfeedback;

    }

    public int getId() {
        return id;
    }
    public String getEmail() {
        return email;
    }
    public String getEx_name() {return ex_name;}
    public int getGoal() {
        return goal;
    }
    public int getCorrect_score() {
        return correct_score;
    }
    public int getIncorrect_score() {
        return incorrect_score;
    }
    public double getAccuracy() {
        return accuracy;
    }
    public String getWorkoutFeedback() {
        return workoutfeedback;
    }
}
