package com.example.tvschedule.repository;

import com.example.tvschedule.dto.ScheduleDto;
import com.example.tvschedule.entity.Schedule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class ScheduleRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private final RowMapper<Schedule> scheduleRowMapper = new RowMapper<Schedule>() {
        @Override
        public Schedule mapRow(ResultSet rs, int rowNum) throws SQLException {
            Schedule schedule = new Schedule();
            schedule.setScheduleId(rs.getString("schedule_id"));
            schedule.setChannelId(rs.getString("channel_id"));
            schedule.setProgramId(rs.getString("program_id"));
            schedule.setStartTime(rs.getTimestamp("start_time").toLocalDateTime());
            schedule.setEndTime(rs.getTimestamp("end_time").toLocalDateTime());
            return schedule;
        }
    };

    public List<Schedule> findAll() {
        String sql = "SELECT schedule_id, channel_id, program_id, start_time, end_time FROM Schedule";
        return jdbcTemplate.query(sql, scheduleRowMapper);
    }

    public List<Schedule> findByDate(LocalDate date) {
        String sql = "SELECT schedule_id, channel_id, program_id, start_time, end_time FROM Schedule " +
                    "WHERE DATE(start_time) = ?";
        return jdbcTemplate.query(sql, scheduleRowMapper, date);
    }

   private final RowMapper<ScheduleDto> scheduleDtoRowMapper = new RowMapper<ScheduleDto>() {
       @Override
       public ScheduleDto mapRow(ResultSet rs, int rowNum) throws SQLException {
           return new ScheduleDto(
               rs.getString("schedule_id"),
               rs.getString("channel_id"),
               rs.getString("channel_name"),
               rs.getString("program_id"),
               rs.getString("title"),
               rs.getString("genre"),
               rs.getString("summary"),
               rs.getTimestamp("start_time").toLocalDateTime(),
               rs.getTimestamp("end_time").toLocalDateTime()
            );
        }
    };

    public List<ScheduleDto> findChannelSchedulesWithNames(String channelId) {
        String sql = "SELECT s.schedule_id, s.channel_id, c.channel_name, " +
                    "s.program_id, p.title, p.genre, p.summary, s.start_time, s.end_time " +
                    "FROM Schedule s " +
                    "INNER JOIN Channel c ON s.channel_id = c.channel_id " +
                    "INNER JOIN Program p ON s.program_id = p.program_id " +
                    "WHERE s.channel_id = ? " +
                    "ORDER BY s.start_time";
        return jdbcTemplate.query(sql, scheduleDtoRowMapper, channelId);
    }

    public List<ScheduleDto> findChannelSchedulesWithNamesByDate(String channelId, LocalDate date) {
        String sql = "SELECT s.schedule_id, s.channel_id, c.channel_name, " +
                    "s.program_id, p.title, p.genre, p.summary, s.start_time, s.end_time " +
                    "FROM Schedule s " +
                    "INNER JOIN Channel c ON s.channel_id = c.channel_id " +
                    "INNER JOIN Program p ON s.program_id = p.program_id " +
                    "WHERE s.channel_id = ? AND DATE(s.start_time) = ? " +
                    "ORDER BY s.start_time";
        return jdbcTemplate.query(sql, scheduleDtoRowMapper, channelId, date);
    }

    public List<Schedule> findByChannelId(String channelId) {
        String sql = "SELECT schedule_id, channel_id, program_id, start_time, end_time FROM Schedule WHERE channel_id = ?";
        return jdbcTemplate.query(sql, scheduleRowMapper, channelId);
    }

    public List<Schedule> findByChannelIdAndDate(String channelId, LocalDate date) {
        String sql = "SELECT schedule_id, channel_id, program_id, start_time, end_time FROM Schedule " +
                    "WHERE channel_id = ? AND DATE(start_time) = ?";
        return jdbcTemplate.query(sql, scheduleRowMapper, channelId, date);
    }

    public List<Schedule> findByUserFavoriteChannels(String userId, LocalDate date) {
        String sql = "SELECT s.schedule_id, s.channel_id, s.program_id, s.start_time, s.end_time " +
                    "FROM Schedule s " +
                    "INNER JOIN favorite_channels fc ON s.channel_id = fc.channel_id " +
                    "WHERE fc.user_id = ? AND DATE(s.start_time) = ?";
        return jdbcTemplate.query(sql, scheduleRowMapper, userId, date);
    }

    public Optional<Schedule> findById(String scheduleId) {
        String sql = "SELECT schedule_id, channel_id, program_id, start_time, end_time FROM Schedule WHERE schedule_id = ?";
        List<Schedule> schedules = jdbcTemplate.query(sql, scheduleRowMapper, scheduleId);
        return schedules.isEmpty() ? Optional.empty() : Optional.of(schedules.get(0));
    }

    public Schedule save(Schedule schedule) {
        if (schedule.getScheduleId() == null) {
            return insert(schedule);
        } else {
            update(schedule);
            return schedule;
        }
    }

    private Schedule insert(Schedule schedule) {
        java.util.Random random = new java.util.Random();
        String scheduleId = String.format("%04d", random.nextInt(10000)) + "T" + String.format("%09d", random.nextInt(1_000_000_000));
        schedule.setScheduleId(scheduleId);

        String sql = "INSERT INTO Schedule (schedule_id, channel_id, program_id, start_time, end_time) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, schedule.getScheduleId(), schedule.getChannelId(), schedule.getProgramId(),
                java.sql.Timestamp.valueOf(schedule.getStartTime()), java.sql.Timestamp.valueOf(schedule.getEndTime()));
        return schedule;
    }

    private void update(Schedule schedule) {
        String sql = "UPDATE Schedule SET channel_id = ?, program_id = ?, start_time = ?, end_time = ? WHERE schedule_id = ?";
        jdbcTemplate.update(sql, schedule.getChannelId(), schedule.getProgramId(), 
                           schedule.getStartTime(), schedule.getEndTime(), schedule.getScheduleId());
    }

    public void deleteById(String scheduleId) {
        String sql = "DELETE FROM Schedule WHERE schedule_id = ?";
        jdbcTemplate.update(sql, scheduleId);
    }

    public boolean existsById(String scheduleId) {
        String sql = "SELECT COUNT(*) FROM Schedule WHERE schedule_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, scheduleId);
        return count != null && count > 0;
    }
}
