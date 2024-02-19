package com.example.aifitnesstrainer;

public class user_goal {
    private int id;
    private String email;
    private String name;
    private int goal;
    public user_goal(int id, String email,String name, int goal) {
        this.id = id;
        this.email = email;
        this.name=name;
        this.goal = goal;
    }

    public int getId() {
        return id;
    }
    public String getEmail() {
        return email;
    }
    public String getname() {return name;}
    public int getgoal() {
        return goal;
    }
}
