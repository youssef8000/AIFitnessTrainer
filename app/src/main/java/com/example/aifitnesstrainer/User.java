package com.example.aifitnesstrainer;

public class User {
    private int id;
    private String email;
    private String name;
    private String phone;
    private String date;
    public User(int id, String email,String name,String phone, String date) {
        this.id = id;
        this.email = email;
        this.name=name;
        this.phone=phone;
        this.date = date;
    }

    public int getId() {
        return id;
    }
    public String getEmail() {
        return email;
    }
    public String getname() {return name;}
    public String getPhone() {
        return phone;
    }
    public String getdate() {
        return date;
    }
}
