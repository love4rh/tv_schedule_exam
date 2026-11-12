package com.example.tvschedule.controller;

import com.example.tvschedule.dto.ApiResponse;
import com.example.tvschedule.dto.FavoriteChannelDto;
import com.example.tvschedule.entity.FavoriteChannel;
import com.example.tvschedule.repository.ChannelRepository;
import com.example.tvschedule.repository.FavoriteChannelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
 * ============================================================================
 *  [실습용 파일]  FavoriteChannelController
 *  - 빈칸 위치는  ▶▶▶ __BLANK_Q#__ ◀◀◀  주석으로 표시되어 있습니다.
 *  - 문제 설명은 PRACTICE.md 를 참고하세요.
 *  - 이 컨트롤러의 기본 경로는 /api/v1/users/{userId}/favorite-channels 입니다.
 * ============================================================================
 */
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

    /* ======================================================================
     * [Q2] 즐겨찾기 상세 목록 조회 엔드포인트
     *  - HTTP GET
     *  - 전체 경로: /api/v1/users/{userId}/favorite-channels/with-details
     *    (클래스 @RequestMapping 뒤에 "/with-details" 가 붙는다)
     *  - Path 변수: userId
     *  아래 두 빈칸을 채우세요.
     * ====================================================================== */
    /* ▶▶▶ __BLANK_Q2_A__ : "/with-details" 경로의 GET 매핑 애노테이션 ◀◀◀ */
    /*__BLANK_Q2_A__*/
    public ResponseEntity<ApiResponse<List<FavoriteChannelDto>>> getFavoriteChannelsWithDetails(
            /* ▶▶▶ __BLANK_Q2_B__ : userId 를 경로 변수로 바인딩 ◀◀◀ */
            /*__BLANK_Q2_B__*/ String userId) {
        List<FavoriteChannelDto> favorites = favoriteChannelRepository.findByUserIdWithDetails(userId);
        return ResponseEntity.ok(ApiResponse.success(favorites, "즐겨찾기 채널 상세 목록 조회 성공"));
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

    /* ======================================================================
     * [Q9] 즐겨찾기 삭제 엔드포인트
     *  - HTTP DELETE
     *  - 전체 경로: /api/v1/users/{userId}/favorite-channels/{channelId}
     *    (클래스 경로 + "/{channelId}")
     *  - Path 변수 2개: userId, channelId
     *  아래 세 빈칸을 채우세요.
     * ====================================================================== */
    /* ▶▶▶ __BLANK_Q9_A__ : "/{channelId}" 경로의 DELETE 매핑 애노테이션 ◀◀◀ */
    /*__BLANK_Q9_A__*/
    public ResponseEntity<ApiResponse<String>> removeFavoriteChannel(
            /* ▶▶▶ __BLANK_Q9_B__ : userId 경로 변수 바인딩 ◀◀◀ */
            /*__BLANK_Q9_B__*/ String userId,
            /* ▶▶▶ __BLANK_Q9_C__ : channelId 경로 변수 바인딩 ◀◀◀ */
            /*__BLANK_Q9_C__*/ String channelId) {
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
