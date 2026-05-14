package com.example.tvschedule.entity;

import java.time.LocalDateTime;

public class FavoriteChannel {
    private Long id;
    private String userId;
    private String channelId;
    private LocalDateTime addedAt;

    public FavoriteChannel() {}

    public FavoriteChannel(String userId, String channelId) {
        this.userId = userId;
        this.channelId = channelId;
        this.addedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
}
