package com.example.TrainingsExercise.models;

public class TrainingStatus {
    private String trainingName;
    private String status;

    public TrainingStatus(String trainingName, String status) {
        this.trainingName = trainingName;
        this.status = status;
    }

    public String getTrainingName() {
        return trainingName;
    }

    public void setTrainingName(String trainingName) {
        this.trainingName = trainingName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
