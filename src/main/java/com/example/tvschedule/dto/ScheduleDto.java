package com.example.tvschedule.dto;

import java.time.LocalDateTime;

public class ScheduleDto {
    private String scheduleId;
    private String channelId;
    private String channelName;
    private String programId;
    private String programName;
    private String genre;
    private String summary;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public ScheduleDto() {}

    public ScheduleDto(String scheduleId, String channelId, String channelName, 
    				String programId, String programName, String genre, String summary,
                    LocalDateTime startTime, LocalDateTime endTime) {
        this.scheduleId = scheduleId;
        this.channelId = channelId;
        this.channelName = channelName;
        this.programId = programId;
        this.programName = programName;
        this.genre = genre;
        this.summary = summary;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public String getScheduleId() {
        return scheduleId;
    }

    public void setScheduleId(String scheduleId) {
        this.scheduleId = scheduleId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public void setChannelName(String channelName) {
        this.channelName = channelName;
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

   public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}
