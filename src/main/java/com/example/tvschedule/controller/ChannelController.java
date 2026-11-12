package com.example.tvschedule.controller;

import com.example.tvschedule.dto.ApiResponse;
import com.example.tvschedule.dto.ScheduleDto;
import com.example.tvschedule.entity.Channel;
import com.example.tvschedule.service.ChannelService;
import com.example.tvschedule.repository.ScheduleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;

/*
 * ============================================================================
 *  [실습용 파일]  ChannelController
 *  - 빈칸 위치는  ▶▶▶ __BLANK_Q#__ ◀◀◀  주석으로 표시되어 있습니다.
 *  - 문제 설명은 PRACTICE.md 를 참고하세요.
 *  - 이 컨트롤러의 기본 경로는 /api/v1/channels 입니다.
 * ============================================================================
 */
@RestController
@RequestMapping("/api/v1/channels")
@CrossOrigin(origins = "*")
public class ChannelController {

    @Autowired
    private ChannelService channelService;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Channel>>> getAllChannels() {
        List<Channel> channels = channelService.findAll();
        return ResponseEntity.ok(ApiResponse.success(channels, "채널 목록 조회 성공"));
    }

    /* ======================================================================
     * [Q1] 채널 검색 엔드포인트
     *  - HTTP GET, 경로: /api/v1/channels/search
     *  - 쿼리 파라미터: keyword(필수), includeGroup(선택, 기본 false)
     *  아래 두 빈칸을 채우세요.
     * ====================================================================== */
    /* ▶▶▶ __BLANK_Q1_A__ : 이 메서드를 /search 경로의 GET 으로 매핑하는 애노테이션 ◀◀◀ */
    /*__BLANK_Q1_A__*/
    public ResponseEntity<ApiResponse<List<Channel>>> searchChannels(
            /* ▶▶▶ __BLANK_Q1_B__ : keyword(필수), includeGroup(선택, 기본 "false") 파라미터 선언 ◀◀◀ */
            /*__BLANK_Q1_B__*/ ) {
        List<Channel> channels = includeGroup
                ? channelService.searchByNameOrGroup(keyword)
                : channelService.search(keyword);
        return ResponseEntity.ok(ApiResponse.success(channels, "채널 검색 성공"));
    }

    @GetMapping("/{channelId}")
    public ResponseEntity<ApiResponse<Channel>> getChannelById(@PathVariable String channelId) {
        Optional<Channel> channel = channelService.findById(channelId);
        if (channel.isPresent()) {
            return ResponseEntity.ok(ApiResponse.success(channel.get(), "채널 조회 성공"));
        } else {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("CHANNEL_NOT_FOUND", "채널을 찾을 수 없습니다"));
        }
    }

    /* ======================================================================
     * [Q3] 채널별 편성표 조회 엔드포인트
     *  - HTTP GET, 경로: /api/v1/channels/{channelId}/schedules
     *  - Path 변수: channelId
     *  - 쿼리 파라미터: date(선택, YYYY-MM-DD)
     *  아래 세 빈칸을 채우세요.
     * ====================================================================== */
    /* ▶▶▶ __BLANK_Q3_A__ : "{channelId}/schedules" 경로의 GET 매핑 애노테이션 ◀◀◀ */
    /*__BLANK_Q3_A__*/
    public ResponseEntity<ApiResponse<List<ScheduleDto>>> getChannelSchedules(
            /* ▶▶▶ __BLANK_Q3_B__ : channelId 를 경로 변수로 바인딩 ◀◀◀ */
            /*__BLANK_Q3_B__*/ String channelId,
            /* ▶▶▶ __BLANK_Q3_C__ : date 를 선택적 쿼리 파라미터로 바인딩 ◀◀◀ */
            /*__BLANK_Q3_C__*/ String date) {

        if (!channelService.existsById(channelId)) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("CHANNEL_NOT_FOUND", "채널을 찾을 수 없습니다"));
        }

        try {
            List<ScheduleDto> schedules;

            if (date != null) {
                LocalDate scheduleDate = LocalDate.parse(date);
                schedules = scheduleRepository.findChannelSchedulesWithNamesByDate(channelId, scheduleDate);
            } else {
                schedules = scheduleRepository.findChannelSchedulesWithNames(channelId);
            }

            return ResponseEntity.ok(ApiResponse.success(schedules, "채널 편성표 조회 성공"));
        } catch (DateTimeParseException e) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_DATE_FORMAT", "날짜 형식이 올바르지 않습니다 (YYYY-MM-DD)"));
        }
    }

    /* 채널 생성/수정/삭제 API는 본 교육 과정에서 제공하지 않습니다. (조회/검색만) */
}
