package com.example.TrainingsExercise.models;

import java.util.List;

public class Person {
    private String name;
    private List<Training> completions;

    public Person(String name, List<Training> completions) {
        this.name = name;
        this.completions = completions;
    }

    public String getName() {
        return name;
    }

    public List<Training> getCompletions() {
        return completions;
    }

    public void setCompletions(List<Training> completions) {
        this.completions = completions;
    }

    @Override
    public String toString() {
        return "name: " + name + ", completions: " + completions;
    }

}
