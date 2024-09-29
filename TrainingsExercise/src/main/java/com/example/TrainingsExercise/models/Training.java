package com.example.TrainingsExercise.models;

public class Training {
    private String name;
    private String timestamp;
    private String expires;

    public Training(String name, String timestamp, String expires) {
        this.name = name;
        this.timestamp = timestamp;
        this.expires = expires;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getExpires() {
        return expires;
    }

    public void setExpires(String expires) {
        this.expires = expires;
    }

    @Override
    public String toString() {
        return "name: " + name + ", timestamp: " + timestamp + ", expires: " + expires;
    }
}
