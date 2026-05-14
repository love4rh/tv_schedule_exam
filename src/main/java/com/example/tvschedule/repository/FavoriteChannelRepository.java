package com.example.tvschedule.repository;

import com.example.tvschedule.entity.FavoriteChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class FavoriteChannelRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<FavoriteChannel> favoriteChannelRowMapper = new RowMapper<FavoriteChannel>() {
        @Override
        public FavoriteChannel mapRow(ResultSet rs, int rowNum) throws SQLException {
            FavoriteChannel favorite = new FavoriteChannel();
            favorite.setId(rs.getLong("id"));
            favorite.setUserId(rs.getString("user_id"));
            favorite.setChannelId(rs.getString("channel_id"));
            favorite.setAddedAt(rs.getTimestamp("added_at").toLocalDateTime());
            return favorite;
        }
    };

    public List<FavoriteChannel> findByUserId(String userId) {
        String sql = "SELECT id, user_id, channel_id, added_at FROM favorite_channels WHERE user_id = ?";
        return jdbcTemplate.query(sql, favoriteChannelRowMapper, userId);
    }

    public Optional<FavoriteChannel> findByUserIdAndChannelId(String userId, String channelId) {
        String sql = "SELECT id, user_id, channel_id, added_at FROM favorite_channels WHERE user_id = ? AND channel_id = ?";
        List<FavoriteChannel> favorites = jdbcTemplate.query(sql, favoriteChannelRowMapper, userId, channelId);
        return favorites.isEmpty() ? Optional.empty() : Optional.of(favorites.get(0));
    }

    public FavoriteChannel save(FavoriteChannel favorite) {
        String sql = "INSERT INTO favorite_channels (user_id, channel_id, added_at) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, favorite.getUserId(), favorite.getChannelId(), favorite.getAddedAt());
        return favorite;
    }

    public void deleteByUserIdAndChannelId(String userId, String channelId) {
        String sql = "DELETE FROM favorite_channels WHERE user_id = ? AND channel_id = ?";
        jdbcTemplate.update(sql, userId, channelId);
    }

    public long countByUserId(String userId) {
        String sql = "SELECT COUNT(*) FROM favorite_channels WHERE user_id = ?";
        return jdbcTemplate.queryForObject(sql, Long.class, userId);
    }

    public boolean existsByUserIdAndChannelId(String userId, String channelId) {
        String sql = "SELECT COUNT(*) FROM favorite_channels WHERE user_id = ? AND channel_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, channelId);
        return count != null && count > 0;
    }
}
