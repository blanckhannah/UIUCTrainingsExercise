package com.example.TrainingsExercise.models;

public class Training {
    private String trainingName;
    private String timestamp;
    private String expires;

    public Training(String name, String timestamp, String expires) {
        this.trainingName = name;
        this.timestamp = timestamp;
        this.expires = expires;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
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
        return "name: " + trainingName + ", timestamp: " + timestamp + ", expires: " + expires;
    }
}
