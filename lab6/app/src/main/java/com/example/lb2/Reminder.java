package com.example.lb2;

public class Reminder {
    private int id;
    private String title;
    private String message;
    private String date;

    // Конструктор с четырьмя параметрами
    public Reminder(int id, String title, String message, String date) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.date = date;
    }

    // Конструктор с двумя параметрами (если он вам все еще нужен)
    public Reminder(String title, String message) {
        this.title = title;
        this.message = message;
    }

    // Геттеры и сеттеры
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}