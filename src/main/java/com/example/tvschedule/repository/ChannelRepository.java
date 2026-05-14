package com.example.tvschedule.repository;

import com.example.tvschedule.entity.Channel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class ChannelRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Channel> channelRowMapper = new RowMapper<Channel>() {
        @Override
        public Channel mapRow(ResultSet rs, int rowNum) throws SQLException {
            Channel channel = new Channel();
            channel.setChannelId(rs.getString("channel_id"));
            channel.setChannelName(rs.getString("channel_name"));
            channel.setChannelGroup(rs.getString("channel_group"));
            return channel;
        }
    };

    public List<Channel> findAll() {
        String sql = "SELECT channel_id, channel_name, channel_group FROM Channel";
        return jdbcTemplate.query(sql, channelRowMapper);
    }

    public Optional<Channel> findById(String channelId) {
        String sql = "SELECT channel_id, channel_name, channel_group FROM Channel WHERE channel_id = ?";
        List<Channel> channels = jdbcTemplate.query(sql, channelRowMapper, channelId);
        return channels.isEmpty() ? Optional.empty() : Optional.of(channels.get(0));
    }

    public Channel save(Channel channel) {
        if (existsById(channel.getChannelId())) {
            update(channel);
        } else {
            insert(channel);
        }
        return channel;
    }

    private void insert(Channel channel) {
        String sql = "INSERT INTO Channel (channel_id, channel_name, channel_group) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, channel.getChannelId(), channel.getChannelName(), channel.getChannelGroup());
    }

    private void update(Channel channel) {
        String sql = "UPDATE Channel SET channel_name = ?, channel_group = ? WHERE channel_id = ?";
        jdbcTemplate.update(sql, channel.getChannelName(), channel.getChannelGroup(), channel.getChannelId());
    }

    public void deleteById(String channelId) {
        String sql = "DELETE FROM Channel WHERE channel_id = ?";
        jdbcTemplate.update(sql, channelId);
    }

    public boolean existsById(String channelId) {
        String sql = "SELECT COUNT(*) FROM Channel WHERE channel_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, channelId);
        return count != null && count > 0;
    }

    public List<Channel> findByChannelGroup(String channelGroup) {
        String sql = "SELECT channel_id, channel_name, channel_group FROM Channel WHERE channel_group = ?";
        return jdbcTemplate.query(sql, channelRowMapper, channelGroup);
    }
}
