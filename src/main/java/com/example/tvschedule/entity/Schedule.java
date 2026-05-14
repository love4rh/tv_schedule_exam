package com.example.tvschedule.entity;

import java.time.LocalDateTime;

public class Schedule {
    private String scheduleId;
    private String channelId;
    private String programId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public Schedule() {}

    public Schedule(String channelId, String programId, LocalDateTime startTime, LocalDateTime endTime) {
        this.channelId = channelId;
        this.programId = programId;
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

    public String getProgramId() {
        return programId;
    }

    public void setProgramId(String programId) {
        this.programId = programId;
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
