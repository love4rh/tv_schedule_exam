package com.example.tvschedule.dto;

import java.time.LocalDateTime;

public class FavoriteChannelDto {
    private Long id;
    private String channelId;
    private String channelName;
    private String channelGroup;
    private LocalDateTime addedAt;

    public FavoriteChannelDto() {}

    public FavoriteChannelDto(Long id, String channelId, String channelName, String channelGroup, LocalDateTime addedAt) {
        this.id = id;
        this.channelId = channelId;
        this.channelName = channelName;
        this.channelGroup = channelGroup;
        this.addedAt = addedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }

    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }

    public String getChannelGroup() { return channelGroup; }
    public void setChannelGroup(String channelGroup) { this.channelGroup = channelGroup; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}

// 즐겨찾기 추가 요청 DTO
class AddFavoriteRequest {
    private String channelId;

    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }
}
