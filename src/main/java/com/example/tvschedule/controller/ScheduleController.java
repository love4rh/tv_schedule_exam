package com.example.tvschedule.controller;

import com.example.tvschedule.dto.ApiResponse;
import com.example.tvschedule.dto.CurrentNextDto;
import com.example.tvschedule.dto.ScheduleDto;
import com.example.tvschedule.entity.Schedule;
import com.example.tvschedule.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/*
 * ============================================================================
 *  [실습용 파일]  ScheduleController
 *  - 빈칸 위치는  ▶▶▶ __BLANK_Q#__ ◀◀◀  주석으로 표시되어 있습니다.
 *  - 문제 설명은 PRACTICE.md 를 참고하세요.
 *  - 클래스 기본 경로는 /api/v1/schedules 입니다.
 * ============================================================================
 */
@RestController
@RequestMapping("/api/v1/schedules")
@CrossOrigin(origins = "*")
public class ScheduleController {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Schedule>>> getSchedules(@RequestParam(required = false) String date,
                                                                   @RequestParam(required = false) String channelId) {
        try {
            List<Schedule> schedules;

            if (date != null && channelId != null) {
                LocalDate scheduleDate = LocalDate.parse(date);
                schedules = scheduleRepository.findByChannelIdAndDate(channelId, scheduleDate);
            } else if (date != null) {
                LocalDate scheduleDate = LocalDate.parse(date);
                schedules = scheduleRepository.findByDate(scheduleDate);
            } else {
                schedules = scheduleRepository.findAll();
            }

            return ResponseEntity.ok(ApiResponse.success(schedules, "편성표 조회 성공"));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_DATE_FORMAT", "날짜 형식이 올바르지 않습니다 (YYYY-MM-DD)"));
        }
    }

    @GetMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<Schedule>> getScheduleById(@PathVariable String scheduleId) {
        Optional<Schedule> schedule = scheduleRepository.findById(scheduleId);
        if (schedule.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(schedule.get(), "편성표 조회 성공"));
        } else {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("SCHEDULE_NOT_FOUND", "편성표를 찾을 수 없습니다"));
        }
    }

    @GetMapping("/users/{userId}/schedules/with-details")
    public ResponseEntity<ApiResponse<List<ScheduleDto>>> getUserSchedulesWithDetails(@PathVariable String userId, @RequestParam String date) {
        try {
            LocalDate scheduleDate = LocalDate.parse(date);
            List<ScheduleDto> schedules = scheduleRepository.findUserFavoriteSchedulesWithNamesByDate(userId, scheduleDate);

            return ResponseEntity.ok(ApiResponse.success(schedules, "사용자 편성표 상세 조회 성공"));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_DATE_FORMAT", "날짜 형식이 올바르지 않습니다 (YYYY-MM-DD)"));
        }
    }

    /* ======================================================================
     * [Q4] 사용자 즐겨찾기 편성표 "시각 범위" 상세 조회 엔드포인트
     *  - HTTP GET
     *  - 전체 경로: /api/v1/schedules/users/{userId}/schedules/with-details/range
     *  - Path 변수: userId
     *  - 쿼리 파라미터: start(필수), end(필수)  (ISO LocalDateTime, GMT 기준)
     *  아래 네 빈칸을 채우세요.
     * ====================================================================== */
    /* ▶▶▶ __BLANK_Q4_A__ : "/users/{userId}/schedules/with-details/range" 의 GET 매핑 ◀◀◀ */
    /*__BLANK_Q4_A__*/
    public ResponseEntity<ApiResponse<List<ScheduleDto>>> getUserSchedulesWithDetailsByRange(
            /* ▶▶▶ __BLANK_Q4_B__ : userId 경로 변수 바인딩 ◀◀◀ */
            /*__BLANK_Q4_B__*/ String userId,
            /* ▶▶▶ __BLANK_Q4_C__ : start 필수 쿼리 파라미터 바인딩 ◀◀◀ */
            /*__BLANK_Q4_C__*/ String start,
            /* ▶▶▶ __BLANK_Q4_D__ : end 필수 쿼리 파라미터 바인딩 ◀◀◀ */
            /*__BLANK_Q4_D__*/ String end) {
        try {
            LocalDateTime startTime = LocalDateTime.parse(start);
            LocalDateTime endTime = LocalDateTime.parse(end);
            List<ScheduleDto> schedules =
                    scheduleRepository.findUserFavoriteSchedulesWithNamesByRange(userId, startTime, endTime);

            return ResponseEntity.ok(ApiResponse.success(schedules, "사용자 편성표 상세 조회 성공"));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_DATETIME_FORMAT",
                            "시각 형식이 올바르지 않습니다 (ISO-8601, 예: 2026-11-09T15:00:00)"));
        }
    }

    /* ======================================================================
     * [Q5] 사용자 현재/다음 방송 조회 엔드포인트
     *  - HTTP GET
     *  - 전체 경로: /api/v1/schedules/users/{userId}/current-next
     *  - Path 변수: userId
     *  아래 두 빈칸을 채우세요.
     * ====================================================================== */
    /* ▶▶▶ __BLANK_Q5_A__ : "/users/{userId}/current-next" 의 GET 매핑 ◀◀◀ */
    /*__BLANK_Q5_A__*/
    public ResponseEntity<ApiResponse<List<CurrentNextDto>>> getUserCurrentNext(
            /* ▶▶▶ __BLANK_Q5_B__ : userId 경로 변수 바인딩 ◀◀◀ */
            /*__BLANK_Q5_B__*/ String userId) {
        List<CurrentNextDto> result = scheduleRepository.findUserFavoriteCurrentNext(userId);
        return ResponseEntity.ok(ApiResponse.success(result, "현재/다음 방송 조회 성공"));
    }
}
