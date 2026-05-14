package com.example.tvschedule.entity;

public class Channel {
    private String channelId;
    private String channelName;
    private String channelGroup;

    public Channel() {}

    public Channel(String channelId, String channelName, String channelGroup) {
        this.channelId = channelId;
        this.channelName = channelName;
        this.channelGroup = channelGroup;
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

    public String getChannelGroup() {
        return channelGroup;
    }

    public void setChannelGroup(String channelGroup) {
        this.channelGroup = channelGroup;
    }
}
