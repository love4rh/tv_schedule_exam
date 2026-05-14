package com.example.tvschedule.controller;

import com.example.tvschedule.dto.ApiResponse;
import com.example.tvschedule.entity.FavoriteChannel;
import com.example.tvschedule.repository.ChannelRepository;
import com.example.tvschedule.repository.FavoriteChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users/{userId}/favorite-channels")
@CrossOrigin(origins = "*")
public class FavoriteChannelController {

    @Autowired
    private FavoriteChannelRepository favoriteChannelRepository;

    @Autowired
    private ChannelRepository channelRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FavoriteChannel>>> getFavoriteChannels(@PathVariable String userId) {
        List<FavoriteChannel> favorites = favoriteChannelRepository.findByUserId(userId);
        return ResponseEntity.ok(ApiResponse.success(favorites, "즐겨찾기 채널 목록 조회 성공"));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<String>> addFavoriteChannel(@PathVariable String userId, @RequestBody Map<String, String> request) {
        String channelId = request.get("channelId");
        
        if (channelId == null || channelId.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("INVALID_CHANNEL_ID", "채널 ID가 필요합니다"));
        }

        if (!channelRepository.existsById(channelId)) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("CHANNEL_NOT_FOUND", "채널을 찾을 수 없습니다"));
        }

        if (favoriteChannelRepository.existsByUserIdAndChannelId(userId, channelId)) {
            return ResponseEntity.status(409)
                    .body(ApiResponse.error("DUPLICATE_FAVORITE", "이미 즐겨찾기에 추가된 채널입니다"));
        }

        FavoriteChannel favorite = new FavoriteChannel(userId, channelId);
        favoriteChannelRepository.save(favorite);

        return ResponseEntity.status(201)
                .body(ApiResponse.success("SUCCESS", "즐겨찾기 채널이 추가되었습니다"));
    }

    @DeleteMapping("/{channelId}")
    public ResponseEntity<ApiResponse<String>> removeFavoriteChannel(@PathVariable String userId, @PathVariable String channelId) {
        Optional<FavoriteChannel> favorite = favoriteChannelRepository.findByUserIdAndChannelId(userId, channelId);
        
        if (!favorite.isPresent()) {
            return ResponseEntity.status(404)
                    .body(ApiResponse.error("FAVORITE_NOT_FOUND", "즐겨찾기에서 해당 채널을 찾을 수 없습니다"));
        }

        favoriteChannelRepository.deleteByUserIdAndChannelId(userId, channelId);
        
        return ResponseEntity.ok(ApiResponse.success("SUCCESS", "즐겨찾기 채널이 삭제되었습니다"));
    }

    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getFavoriteChannelCount(@PathVariable String userId) {
        long count = favoriteChannelRepository.countByUserId(userId);
        Map<String, Long> result = Map.of("count", count);
        
        return ResponseEntity.ok(ApiResponse.success(result, "즐겨찾기 채널 개수 조회 성공"));
    }

    @GetMapping("/check/{channelId}")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> checkFavoriteChannel(@PathVariable String userId, @PathVariable String channelId) {
        boolean isFavorite = favoriteChannelRepository.existsByUserIdAndChannelId(userId, channelId);
        Map<String, Boolean> result = Map.of("isFavorite", isFavorite);

        return ResponseEntity.ok(ApiResponse.success(result, "즐겨찾기 여부 확인 완료"));
    }
}
