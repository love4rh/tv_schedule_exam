package com.example.tvschedule.service;

import com.example.tvschedule.entity.Channel;
import com.example.tvschedule.repository.ChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ChannelService {

    @Autowired
    private ChannelRepository channelRepository;

    public List<Channel> findAll() {
        return channelRepository.findAll();
    }

    public Optional<Channel> findById(String channelId) {
        return channelRepository.findById(channelId);
    }

    public Channel save(Channel channel) {
        return channelRepository.save(channel);
    }

    public void deleteById(String channelId) {
        channelRepository.deleteById(channelId);
    }

    public List<Channel> findByChannelGroup(String channelGroup) {
        return channelRepository.findByChannelGroup(channelGroup);
    }

    public boolean existsById(String channelId) {
        return channelRepository.existsById(channelId);
    }
}
