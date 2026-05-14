# TV Schedule REST API - Spring Boot 실습

## 프로젝트 개요
Spring Boot를 사용한 TV 편성표 REST API 실습 프로젝트입니다.

## 기술 스택
- **Java 21**
- **Spring Boot 3.1.0**
- **Spring JDBC** (JdbcTemplate)
- **MySQL 8.0**
- **Maven**

## 프로젝트 구조
```
tv_schedule_exam/
├── src/main/java/com/example/tvschedule/
│   ├── TvScheduleApplication.java          # 메인 애플리케이션
│   ├── entity/                             # 엔티티 (POJO)
│   │   ├── Channel.java
│   │   ├── Program.java
│   │   ├── Schedule.java
│   │   └── FavoriteChannel.java
│   ├── repository/                         # Repository (JdbcTemplate)
│   │   ├── ChannelRepository.java
│   │   ├── ProgramRepository.java
│   │   ├── ScheduleRepository.java
│   │   └── FavoriteChannelRepository.java
│   ├── service/                            # Service
│   │   └── ChannelService.java
│   ├── controller/                         # REST Controller
│   │   ├── ChannelController.java
│   │   ├── ProgramController.java
│   │   ├── ScheduleController.java
│   │   └── FavoriteChannelController.java
│   └── dto/                                # 응답 DTO
│       ├── ApiResponse.java
│       ├── ScheduleDto.java
│       └── FavoriteChannelDto.java
├── src/main/resources/
│   ├── static/                             # 프론트엔드
│   │   ├── index.html
│   │   ├── script.js
│   │   └── style.css
│   ├── application.yml                     # 설정 파일
│   └── schema.sql                          # DDL + 샘플 데이터
├── pom.xml                                 # Maven 설정
└── README.md
```

## 실행 방법

### 1. 프로젝트 빌드 및 실행
```bash
cd tv_schedule_exam

mvn clean compile

mvn spring-boot:run
```

### 2. 서버 확인
- **애플리케이션**: http://localhost:8282
- **프론트엔드 뷰어**: http://localhost:8282/index.html

## API 엔드포인트

모든 API는 통일된 응답 형식을 사용합니다:
```json
{
  "success": true,
  "data": "...",
  "message": "작업 완료",
  "error": null,
  "timestamp": "2024-11-11T10:30:00"
}
```

### 채널 관리 — `/api/v1/channels`

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/v1/channels` | 전체 채널 목록 조회 |
| GET | `/api/v1/channels/{channelId}` | 특정 채널 조회 |
| GET | `/api/v1/channels/{channelId}/schedules?date=` | 채널별 편성표 조회 (날짜 선택) |
| POST | `/api/v1/channels` | 채널 생성 |
| PUT | `/api/v1/channels/{channelId}` | 채널 수정 |
| DELETE | `/api/v1/channels/{channelId}` | 채널 삭제 |

### 프로그램 관리 — `/api/v1/programs`

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/v1/programs` | 전체 프로그램 목록 조회 |
| GET | `/api/v1/programs/{programId}` | 특정 프로그램 조회 |
| POST | `/api/v1/programs` | 프로그램 생성 |
| PUT | `/api/v1/programs/{programId}` | 프로그램 수정 |
| DELETE | `/api/v1/programs/{programId}` | 프로그램 삭제 |

### 편성표 관리 — `/api/v1/schedules`

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/v1/schedules?date=&channelId=` | 편성표 조회 (필터 선택) |
| GET | `/api/v1/schedules/{scheduleId}` | 특정 편성표 조회 |
| POST | `/api/v1/schedules` | 편성표 생성 |
| PUT | `/api/v1/schedules/{scheduleId}` | 편성표 수정 |
| DELETE | `/api/v1/schedules/{scheduleId}` | 편성표 삭제 |
| GET | `/api/v1/schedules/users/{userId}/schedules?date=` | 사용자 즐겨찾기 채널 편성표 |

편성표 목록 조회 시 파라미터 조합에 따라 필터링됩니다:
- `date` + `channelId` → 특정 채널의 특정 날짜 편성표
- `date`만 → 전체 채널의 해당 날짜 편성표
- 파라미터 없음 → 전체 편성표

### 즐겨찾기 채널 — `/api/v1/users/{userId}/favorite-channels`

| Method | URL | 설명 |
|--------|-----|------|
| GET | `/api/v1/users/{userId}/favorite-channels` | 즐겨찾기 목록 조회 |
| POST | `/api/v1/users/{userId}/favorite-channels` | 즐겨찾기 추가 |
| DELETE | `/api/v1/users/{userId}/favorite-channels/{channelId}` | 즐겨찾기 삭제 |
| GET | `/api/v1/users/{userId}/favorite-channels/count` | 즐겨찾기 개수 조회 |
| GET | `/api/v1/users/{userId}/favorite-channels/check/{channelId}` | 즐겨찾기 여부 확인 |

## cURL 테스트 예시

```bash
# 채널 목록 조회
curl -X GET "http://localhost:8282/api/v1/channels"

# 특정 채널 조회
curl -X GET "http://localhost:8282/api/v1/channels/KBS1"

# 채널 생성
curl -X POST "http://localhost:8282/api/v1/channels" \
  -H "Content-Type: application/json" \
  -d '{"channelId": "EBS", "channelName": "EBS", "channelGroup": "지상파"}'

# 채널별 편성표 조회 (날짜 지정)
curl -X GET "http://localhost:8282/api/v1/channels/KBS1/schedules?date=2024-11-25"

# 전체 편성표 조회
curl -X GET "http://localhost:8282/api/v1/schedules"

# 즐겨찾기 채널 목록 조회
curl -X GET "http://localhost:8282/api/v1/users/user123/favorite-channels"

# 즐겨찾기 채널 추가
curl -X POST "http://localhost:8282/api/v1/users/user123/favorite-channels" \
  -H "Content-Type: application/json" \
  -d '{"channelId": "MBC"}'

# 즐겨찾기 채널 삭제
curl -X DELETE "http://localhost:8282/api/v1/users/user123/favorite-channels/MBC"

# 즐겨찾기 여부 확인
curl -X GET "http://localhost:8282/api/v1/users/user123/favorite-channels/check/KBS1"

# 즐겨찾기 개수 조회
curl -X GET "http://localhost:8282/api/v1/users/user123/favorite-channels/count"

# 사용자 즐겨찾기 채널 편성표
curl -X GET "http://localhost:8282/api/v1/schedules/users/user123/schedules?date=2024-11-25"
```

## DB 테이블 구조

```sql
-- 채널
Channel (channel_id PK, channel_name, channel_group)

-- 프로그램
Program (program_id PK, program_name, genre, description)

-- 편성표
Schedule (schedule_id PK, channel_id FK, program_id FK, start_time, end_time)

-- 즐겨찾기 채널
favorite_channels (id PK AUTO_INCREMENT, user_id, channel_id FK, added_at)
                  UNIQUE(user_id, channel_id)
```

## 샘플 데이터

### 채널 (5개)
- KBS1, KBS2, MBC, SBS (지상파)
- TVN (케이블)

### 프로그램 (5개)
- 뉴스데스크, 무한도전, 드라마 스페셜, 아침마당, 스포츠 뉴스

### 편성표
- 2024-11-25 기준 KBS1, KBS2, MBC, SBS 편성표

## 문제 해결

### 포트 충돌 시
`application.yml`에서 포트 변경:
```yaml
server:
  port: 8081
```

### DB 연결 실패 시
`application.yml`에서 MySQL 접속 정보 확인:
```yaml
spring:
  datasource:
    url: jdbc:mysql://<host>:<port>/<database>
    username: <username>
    password: <password>
```
