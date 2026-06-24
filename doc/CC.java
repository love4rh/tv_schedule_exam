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

    @GetMapping("/{channelId}/schedules")
    public ResponseEntity<ApiResponse<List<ScheduleDto>>> getChannelSchedules(
            @PathVariable String channelId,
            @RequestParam(required = false) String date) {
        
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

    @PostMapping
    public ResponseEntity<ApiResponse<Channel>> createChannel(@RequestBody Channel channel) {
        if (channelService.existsById(channel.getChannelId())) {
            return ResponseEntity.status(409)
                    .body(ApiResponse.error("DUPLICATE_CHANNEL", "이미 존재하는 채널 ID입니다"));
        }
        
        Channel savedChannel = channelService.save(channel);
        return ResponseEntity.status(201)
                .body(ApiResponse.success(savedChannel, "채널이 생성되었습니다"));
    }

    @PutMapping("/{channelId}")
    public ResponseEntity<ApiResponse<Channel>> updateChannel(@PathVariable String channelId, @RequestBody Channel channel) {
        if (!channelService.existsById(channelId)) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("CHANNEL_NOT_FOUND", "채널을 찾을 수 없습니다"));
        }
        
        channel.setChannelId(channelId);
        Channel updatedChannel = channelService.save(channel);
        return ResponseEntity.ok(ApiResponse.success(updatedChannel, "채널이 수정되었습니다"));
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<ApiResponse<String>> deleteChannel(@PathVariable String channelId) {
        if (!channelService.existsById(channelId)) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("CHANNEL_NOT_FOUND", "채널을 찾을 수 없습니다"));
        }
        
        channelService.deleteById(channelId);
        return ResponseEntity.ok(ApiResponse.success("SUCCESS", "채널이 삭제되었습니다"));
    }
}
