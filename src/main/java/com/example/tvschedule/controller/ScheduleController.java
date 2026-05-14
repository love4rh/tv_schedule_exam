package com.example.tvschedule.controller;

import com.example.tvschedule.dto.ApiResponse;
import com.example.tvschedule.entity.Schedule;
import com.example.tvschedule.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

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

    @PostMapping
    public ResponseEntity<ApiResponse<Schedule>> createSchedule(@RequestBody Schedule schedule) {
        Schedule savedSchedule = scheduleRepository.save(schedule);
        return ResponseEntity.status(201)
                .body(ApiResponse.success(savedSchedule, "편성표가 생성되었습니다"));
    }

    @PutMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<Schedule>> updateSchedule(@PathVariable String scheduleId, @RequestBody Schedule schedule) {
        if (!scheduleRepository.existsById(scheduleId)) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("SCHEDULE_NOT_FOUND", "편성표를 찾을 수 없습니다"));
        }
        
        schedule.setScheduleId(scheduleId);
        Schedule updatedSchedule = scheduleRepository.save(schedule);
        return ResponseEntity.ok(ApiResponse.success(updatedSchedule, "편성표가 수정되었습니다"));
    }

    @DeleteMapping("/{scheduleId}")
    public ResponseEntity<ApiResponse<String>> deleteSchedule(@PathVariable String scheduleId) {
        if (!scheduleRepository.existsById(scheduleId)) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("SCHEDULE_NOT_FOUND", "편성표를 찾을 수 없습니다"));
        }
        
        scheduleRepository.deleteById(scheduleId);
        return ResponseEntity.ok(ApiResponse.success("SUCCESS", "편성표가 삭제되었습니다"));
    }

    @GetMapping("/users/{userId}/schedules")
    public ResponseEntity<ApiResponse<List<Schedule>>> getUserSchedules(@PathVariable String userId, @RequestParam String date) {
        try {
            LocalDate scheduleDate = LocalDate.parse(date);
            List<Schedule> schedules = scheduleRepository.findByUserFavoriteChannels(userId, scheduleDate);
            
            return ResponseEntity.ok(ApiResponse.success(schedules, "사용자 편성표 조회 성공"));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_DATE_FORMAT", "날짜 형식이 올바르지 않습니다 (YYYY-MM-DD)"));
        }
    }
}
