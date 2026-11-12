package com.example.tvschedule.dto;

/**
 * 특정 채널의 "현재 방송 중" 프로그램과 "다음 방송" 프로그램을 함께 담는 DTO.
 * current/next 는 Schedule.seq_num 을 기준으로 선정한다.
 */
public class CurrentNextDto {
    private String channelId;
    private String channelName;
    private String channelGroup;
    private ScheduleDto current; // 현재 방송 중 (없으면 null)
    private ScheduleDto next;    // 다음 방송 (없으면 null)

    public CurrentNextDto() {}

    public CurrentNextDto(String channelId, String channelName, String channelGroup,
                          ScheduleDto current, ScheduleDto next) {
        this.channelId = channelId;
        this.channelName = channelName;
        this.channelGroup = channelGroup;
        this.current = current;
        this.next = next;
    }

    public String getChannelId() { return channelId; }
    public void setChannelId(String channelId) { this.channelId = channelId; }

    public String getChannelName() { return channelName; }
    public void setChannelName(String channelName) { this.channelName = channelName; }

    public String getChannelGroup() { return channelGroup; }
    public void setChannelGroup(String channelGroup) { this.channelGroup = channelGroup; }

    public ScheduleDto getCurrent() { return current; }
    public void setCurrent(ScheduleDto current) { this.current = current; }

    public ScheduleDto getNext() { return next; }
    public void setNext(ScheduleDto next) { this.next = next; }
}
