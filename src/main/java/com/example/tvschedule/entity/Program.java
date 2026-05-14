package com.example.tvschedule.entity;

public class Program {
    private String programId;
    private String programName;
    private String genre;
    private String description;

    public Program() {}

    public Program(String programName, String genre, String description) {
        this.programName = programName;
        this.genre = genre;
        this.description = description;
    }

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
    }

    public String getProgramName() {
        return programName;
    }

    public void setProgramName(String programName) {
        this.programName = programName;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
