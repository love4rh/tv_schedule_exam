package com.example.tvschedule.repository;

import com.example.tvschedule.dto.CurrentNextDto;
import com.example.tvschedule.dto.FavoriteChannelDto;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/*
 * ============================================================================
 *  [실습용 파일]  ScheduleRepository (DB 쿼리 작성 실습)
 *  - 빈칸 위치는  ▶▶▶ __BLANK_Q#__ ◀◀◀  주석으로 표시되어 있습니다.
 *  - 아래 Q6, Q7 의 SQL 문자열을 직접 작성하세요.
 *  - 문제 설명은 PRACTICE.md 를 참고하세요.
 *
 *  [DB 스키마 참고 — tv_schedule_erd.sql]
 *   Channel (channel_id, channel_name, channel_group)
 *   Program (program_id, title, genre, rating, summary, score)
 *   Schedule(schedule_id, channel_id, program_id, start_time, end_time, seq_num)
 *     · seq_num : 채널별 방송 순번(1,2,3,...). start_time 순서와 일치하며 연속적이다.
 *   favorite_channels (id, user_id, channel_id, added_at)
 *
 *  [ScheduleDto RowMapper 가 기대하는 컬럼 별칭]
 *   schedule_id, channel_id, channel_name, program_id, title, genre, summary,
 *   start_time, end_time
 * ============================================================================
 */
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

    /* ======================================================================
     * [Q10] 사용자 즐겨찾기 편성표 "시각 범위" 조회 SQL 작성
     *  파일: ScheduleRepository.findUserFavoriteSchedulesWithNamesByRange(...)
     *  요구사항:
     *   - 파라미터(?)는 호출 순서상 userId, end, start (3개) 로 바인딩된다.
     *     (주의: 마지막 query 호출에서 ... userId, end, start 순서로 넘긴다)
     *   - 사용자(userId)의 즐겨찾기 채널 편성표만 대상으로 한다.
     *     → Schedule ⨝ favorite_channels(fc) 조인, fc.user_id = ?
     *   - [start, end) 범위에 걸치는 프로그램까지 포함:
     *       s.start_time < end  AND  s.end_time > start
     *   - 채널명/프로그램명 포함: Schedule ⨝ Channel ⨝ Program 조인,
     *     ScheduleDto 컬럼 별칭(channel_name, title, genre, summary 등) 유지.
     *   - 정렬: 채널 ID, 시작 시각 순.
     *  힌트: WHERE fc.user_id = ? AND s.start_time < ? AND s.end_time > ?
     *        (? 순서 = userId, end, start)
     * ====================================================================== */
    public List<ScheduleDto> findUserFavoriteSchedulesWithNamesByRange(
            String userId, LocalDateTime start, LocalDateTime end) {
        /* ▶▶▶ __BLANK_Q10__ : 아래 sql 문자열을 완성하세요 ◀◀◀ */
        String sql = /*__BLANK_Q10__*/ "";
        return jdbcTemplate.query(sql, scheduleDtoRowMapper, userId, end, start);
    }

    /**
     * 사용자의 Favorite 채널마다 "현재 방송 중" 프로그램과 "다음 방송" 프로그램을 조회한다.
     * - 현재 방송: start_time <= now < end_time 인 스케줄 (그 채널의 seq_num = N)
     * - 다음 방송: 같은 채널의 seq_num = N + 1 인 스케줄
     */
    public List<CurrentNextDto> findUserFavoriteCurrentNext(String userId) {
        String favSql = "SELECT fc.channel_id, c.channel_name, c.channel_group " +
                       "FROM favorite_channels fc " +
                       "INNER JOIN Channel c ON fc.channel_id = c.channel_id " +
                       "WHERE fc.user_id = ? " +
                       "ORDER BY fc.added_at";
        List<FavoriteChannelDto> favorites = jdbcTemplate.query(favSql, (rs, rowNum) ->
            new FavoriteChannelDto(null, rs.getString("channel_id"),
                rs.getString("channel_name"), rs.getString("channel_group"), null), userId);

        List<CurrentNextDto> result = new ArrayList<>();
        for (FavoriteChannelDto fav : favorites) {
            ScheduleDto current = findCurrentProgram(fav.getChannelId());
            ScheduleDto next;
            if (current != null) {
                Integer curSeq = findSeqNum(current.getScheduleId());
                next = (curSeq != null) ? findBySeqNum(fav.getChannelId(), curSeq + 1) : null;
            } else {
                next = findUpcomingProgram(fav.getChannelId());
            }
            result.add(new CurrentNextDto(fav.getChannelId(), fav.getChannelName(),
                fav.getChannelGroup(), current, next));
        }
        return result;
    }

    /* ======================================================================
     * [Q6] "현재 방송 중" 프로그램 조회 SQL
     *  요구사항:
     *   - 파라미터(?)는 channelId 1개.
     *   - 해당 채널에서 "현재 시각 기준 방송 중"인 스케줄 1건을 반환.
     *     (방송 중 = start_time <= 현재시각 AND end_time > 현재시각)
     *   - Schedule(s) ⨝ Channel(c) ⨝ Program(p) 조인.
     *   - ScheduleDto RowMapper 가 읽는 컬럼 별칭에 맞춰 SELECT 할 것
     *     (channel_name, title, genre, summary 등).
     *   - seq_num 순으로 정렬하여 1건만(LIMIT 1).
     *  힌트: MySQL 현재 시각 함수는 NOW().
     * ====================================================================== */
    private ScheduleDto findCurrentProgram(String channelId) {
        /* ▶▶▶ __BLANK_Q6__ : 아래 sql 문자열을 완성하세요 ◀◀◀ */
        String sql = /*__BLANK_Q6__*/ "";
        List<ScheduleDto> list = jdbcTemplate.query(sql, scheduleDtoRowMapper, channelId);
        return list.isEmpty() ? null : list.get(0);
    }

    // 특정 스케줄의 seq_num 조회 (참고용 — 완성되어 있음)
    private Integer findSeqNum(String scheduleId) {
        String sql = "SELECT seq_num FROM Schedule WHERE schedule_id = ?";
        List<Integer> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            int v = rs.getInt("seq_num");
            return rs.wasNull() ? null : v;
        }, scheduleId);
        return list.isEmpty() ? null : list.get(0);
    }

    /* ======================================================================
     * [Q7] 채널 + seq_num 으로 "다음 방송" 스케줄 조회 SQL
     *  요구사항:
     *   - 파라미터(?)는 순서대로 channelId, seqNum (2개).
     *   - 해당 채널에서 seq_num 이 주어진 값과 일치하는 스케줄 1건 반환.
     *   - Schedule ⨝ Channel ⨝ Program 조인, ScheduleDto 컬럼 별칭 유지.
     *   - LIMIT 1.
     *  참고: 호출부에서 (현재 방송 seq_num + 1) 을 seqNum 으로 넘긴다.
     * ====================================================================== */
    private ScheduleDto findBySeqNum(String channelId, int seqNum) {
        /* ▶▶▶ __BLANK_Q7__ : 아래 sql 문자열을 완성하세요 ◀◀◀ */
        String sql = /*__BLANK_Q7__*/ "";
        List<ScheduleDto> list = jdbcTemplate.query(sql, scheduleDtoRowMapper, channelId, seqNum);
        return list.isEmpty() ? null : list.get(0);
    }

    // 현재 방송이 없을 때, 앞으로 시작하는 가장 이른 프로그램 (참고용 — 완성되어 있음)
    private ScheduleDto findUpcomingProgram(String channelId) {
        String sql = "SELECT s.schedule_id, s.channel_id, c.channel_name, " +
                    "s.program_id, p.title, p.genre, p.summary, s.start_time, s.end_time " +
                    "FROM Schedule s " +
                    "INNER JOIN Channel c ON s.channel_id = c.channel_id " +
                    "INNER JOIN Program p ON s.program_id = p.program_id " +
                    "WHERE s.channel_id = ? AND s.start_time > NOW() " +
                    "ORDER BY s.seq_num LIMIT 1";
        List<ScheduleDto> list = jdbcTemplate.query(sql, scheduleDtoRowMapper, channelId);
        return list.isEmpty() ? null : list.get(0);
    }
}
