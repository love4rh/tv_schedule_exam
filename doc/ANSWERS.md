# TV Schedule 서버 API 실습 문제집

Spring Boot 기반 TV 편성표 서버 API를 직접 구현해 보는 실습입니다.
각 문항은 실제 동작 중인 서버 코드의 일부를 **빈칸으로 비워 둔 것**이며, 여러분이 채워 넣어
완성하면 됩니다.

## 실습 방법

1. 실습용 파일은 다음과 같습니다.
   ```
   com/example/tvschedule/
   ├── controller/ChannelController.java          (Q1, Q3)
   ├── controller/FavoriteChannelController.java  (Q2, Q8)
   ├── controller/ScheduleController.java         (Q4, Q5)
   └── repository/ScheduleRepository.java         (Q6, Q7, Q9)
   ```
2. 코드에서 아래 마킹을 찾으면 그 자리가 빈칸입니다.
   - 설명 주석:  `▶▶▶ __BLANK_Q#__ ◀◀◀`
   - 채울 위치:  `/*__BLANK_Q#__*/`
3. 문항 설명(아래)과 **API 명세서(`API_SPEC.md`)**, **DB 스키마(`tv_schedule_erd.sql`)**를 참고하여 빈칸을 채웁니다.
4. 완성한 파일로 원본(`src/main/java/...`의 같은 이름 파일)을 교체한 뒤 빌드/실행하여 동작을 확인합니다.
   ```bash
   mvn clean compile
   mvn spring-boot:run
   ```
5. 막히면 각 문항의 **정답 펼치기**를 참고하세요.

## 공통 배경 지식

- 모든 응답은 `ApiResponse<T>` 래퍼로 감싸집니다.
- 컨트롤러 클래스에는 공통 경로가 `@RequestMapping` 으로 선언되어 있고, 각 메서드의 매핑 경로는 그 뒤에 이어 붙습니다.
- 자주 쓰는 Spring Web 애노테이션:
  - `@GetMapping("경로")` : HTTP GET 매핑
  - `@PathVariable` : 경로 변수(`{...}`) 바인딩
  - `@RequestParam` : 쿼리 파라미터 바인딩 (옵션: `required`, `defaultValue`)
- DB 접근은 `JdbcTemplate` + SQL 문자열로 수행합니다. 파라미터는 `?` 플레이스홀더로 바인딩합니다.

### DB 스키마 요약 (`tv_schedule_erd.sql`)

| 테이블 | 주요 컬럼 |
|--------|-----------|
| `Channel` | `channel_id`, `channel_name`, `channel_group` |
| `Program` | `program_id`, `title`, `genre`, `rating`, `summary`, `score` |
| `Schedule` | `schedule_id`, `channel_id`, `program_id`, `start_time`, `end_time`, `seq_num` |
| `favorite_channels` | `id`, `user_id`, `channel_id`, `added_at` |

> `Schedule.seq_num` : 채널별 방송 순번(1, 2, 3 …). `start_time` 순서와 일치하며 연속적입니다.
> 따라서 "현재 방송"의 `seq_num` 에 1을 더하면 "다음 방송"이 됩니다.

## 문항 요약 (총 9문제)

| 문항 | 주제 | 유형 | 난이도 |
|------|------|------|--------|
| Q1 | 채널 검색 | 경로 매핑 + RequestParam(필수/기본값) | ★ |
| Q2 | 즐겨찾기 상세 조회 | 경로 매핑 + PathVariable | ★ |
| Q3 | 채널별 편성표 | PathVariable + 선택 RequestParam | ★★ |
| Q4 | 사용자 편성표 범위 조회 | 경로 + 다중 RequestParam | ★★ |
| Q5 | 현재/다음 방송 | 경로 + PathVariable | ★★ |
| Q6 | 현재 방송 SQL | DB 쿼리 작성 (시간 조건) | ★★★ |
| Q7 | 다음 방송 SQL | DB 쿼리 작성 (seq_num) | ★★★ |
| Q8 | 즐겨찾기 삭제 | DELETE + 다중 PathVariable | ★★ |
| Q9 | 사용자 편성표 범위 SQL | DB 쿼리 작성 (범위/경계 걸침) | ★★★ |

---

## [Q1] 채널 검색 엔드포인트 — 경로와 파라미터 (난이도 ★)

**파일**: `practice/controller/ChannelController.java`

**설명**: 채널 ID 또는 채널명(옵션으로 그룹명까지)에 검색어가 포함된 채널을 조회하는 API입니다.

**요구사항**
- 전체 경로: `GET /api/v1/channels/search` (클래스 경로 `/api/v1/channels` + `/search`)
- 쿼리 파라미터:
  - `keyword` : 필수 문자열
  - `includeGroup` : 선택, 기본값 `false` (boolean)
- 빈칸:
  - `__BLANK_Q1_A__` : 메서드 매핑 애노테이션
  - `__BLANK_Q1_B__` : 두 파라미터 선언

**힌트**
- 특정 하위 경로 GET → `@GetMapping("/search")`
- 선택적 파라미터는 `@RequestParam(required = false, defaultValue = "false")`.

<details>
<summary>정답 펼치기</summary>

```java
// __BLANK_Q1_A__
@GetMapping("/search")

// __BLANK_Q1_B__
@RequestParam String keyword,
@RequestParam(required = false, defaultValue = "false") boolean includeGroup
```
</details>

---

## [Q2] 즐겨찾기 상세 목록 조회 — 경로와 PathVariable (난이도 ★)

**파일**: `practice/controller/FavoriteChannelController.java`

**설명**: 특정 사용자의 즐겨찾기 채널을 채널명·그룹 포함 상세(`FavoriteChannelDto`)로 조회합니다.

**요구사항**
- 전체 경로: `GET /api/v1/users/{userId}/favorite-channels/with-details`
  (클래스 경로 `/api/v1/users/{userId}/favorite-channels` + `/with-details`)
- Path 변수: `userId`
- 빈칸:
  - `__BLANK_Q2_A__` : 메서드 매핑 애노테이션
  - `__BLANK_Q2_B__` : `userId` 바인딩

**힌트**
- 클래스 경로에 이미 `{userId}` 가 포함되어 있으므로 메서드에는 `/with-details` 만 추가합니다.
- 경로 변수 바인딩은 `@PathVariable`.

<details>
<summary>정답 펼치기</summary>

```java
// __BLANK_Q2_A__
@GetMapping("/with-details")

// __BLANK_Q2_B__
@PathVariable
```
위치상 `@PathVariable String userId` 가 됩니다.
</details>

---

## [Q3] 채널별 편성표 조회 — PathVariable + 선택 RequestParam (난이도 ★★)

**파일**: `practice/controller/ChannelController.java`

**설명**: 특정 채널의 편성표를 조회합니다. `date` 지정 시 해당 날짜만 조회합니다.

**요구사항**
- 전체 경로: `GET /api/v1/channels/{channelId}/schedules`
- Path 변수: `channelId`
- 쿼리 파라미터: `date` (선택, `YYYY-MM-DD`)
- 빈칸:
  - `__BLANK_Q3_A__` : 메서드 매핑 애노테이션
  - `__BLANK_Q3_B__` : `channelId` 바인딩
  - `__BLANK_Q3_C__` : `date` 바인딩(선택)

**힌트**
- 경로에 경로 변수가 들어갈 때: `@GetMapping("/{channelId}/schedules")`
- 선택 파라미터: `@RequestParam(required = false)`

<details>
<summary>정답 펼치기</summary>

```java
// __BLANK_Q3_A__
@GetMapping("/{channelId}/schedules")

// __BLANK_Q3_B__
@PathVariable

// __BLANK_Q3_C__
@RequestParam(required = false)
```
각각 `@PathVariable String channelId`, `@RequestParam(required = false) String date` 가 됩니다.
</details>

---

## [Q4] 사용자 편성표 "시각 범위" 조회 — 경로 + 다중 RequestParam (난이도 ★★)

**파일**: `practice/controller/ScheduleController.java`

**설명**: 사용자 즐겨찾기 채널 편성표를 `[start, end)` 시각 범위(GMT 기준)로 조회합니다.
클라이언트가 표시용 시간대(KST 등)의 하루를 GMT 범위로 변환해 전달하는 용도입니다.

**요구사항**
- 전체 경로: `GET /api/v1/schedules/users/{userId}/schedules/with-details/range`
- Path 변수: `userId`
- 쿼리 파라미터: `start`(필수), `end`(필수) — ISO LocalDateTime 문자열
- 빈칸:
  - `__BLANK_Q4_A__` : 메서드 매핑 애노테이션
  - `__BLANK_Q4_B__` : `userId` 바인딩
  - `__BLANK_Q4_C__` : `start` 바인딩
  - `__BLANK_Q4_D__` : `end` 바인딩

**힌트**
- 메서드 경로는 클래스 경로(`/api/v1/schedules`) 뒤에 이어지는 부분만 적습니다.
- 필수 쿼리 파라미터는 그냥 `@RequestParam` (기본 `required = true`).

<details>
<summary>정답 펼치기</summary>

```java
// __BLANK_Q4_A__
@GetMapping("/users/{userId}/schedules/with-details/range")

// __BLANK_Q4_B__
@PathVariable

// __BLANK_Q4_C__
@RequestParam

// __BLANK_Q4_D__
@RequestParam
```
각각 `@PathVariable String userId`, `@RequestParam String start`, `@RequestParam String end`.
</details>

---

## [Q5] 현재/다음 방송 조회 — 경로 + PathVariable (난이도 ★★)

**파일**: `practice/controller/ScheduleController.java`

**설명**: 사용자의 즐겨찾기 채널마다 현재 방송 중인 프로그램과 다음 방송 프로그램을 조회합니다.

**요구사항**
- 전체 경로: `GET /api/v1/schedules/users/{userId}/current-next`
- Path 변수: `userId`
- 빈칸:
  - `__BLANK_Q5_A__` : 메서드 매핑 애노테이션
  - `__BLANK_Q5_B__` : `userId` 바인딩

<details>
<summary>정답 펼치기</summary>

```java
// __BLANK_Q5_A__
@GetMapping("/users/{userId}/current-next")

// __BLANK_Q5_B__
@PathVariable
```
`@PathVariable String userId` 가 됩니다.
</details>

---

## [Q6] "현재 방송 중" 프로그램 조회 SQL 작성 (난이도 ★★★)

**파일**: `practice/repository/ScheduleRepository.java` → `findCurrentProgram(String channelId)`

**설명**: 특정 채널에서 **지금 이 순간 방송 중인** 프로그램 1건을 조회하는 SQL을 작성합니다.

**요구사항**
- 파라미터(`?`)는 `channelId` 1개.
- 방송 중 조건: `start_time <= 현재시각 AND end_time > 현재시각`
- 테이블 조인: `Schedule s` ⨝ `Channel c` ⨝ `Program p`
- `ScheduleDto` RowMapper 가 읽는 컬럼 별칭을 SELECT 해야 함:
  `s.schedule_id, s.channel_id, c.channel_name, s.program_id, p.title, p.genre, p.summary, s.start_time, s.end_time`
- `seq_num` 오름차순 정렬 후 `LIMIT 1`.

**힌트**
- MySQL 현재 시각 함수: `NOW()`
- 조인 키: `s.channel_id = c.channel_id`, `s.program_id = p.program_id`

<details>
<summary>정답 펼치기</summary>

```java
String sql = "SELECT s.schedule_id, s.channel_id, c.channel_name, " +
            "s.program_id, p.title, p.genre, p.summary, s.start_time, s.end_time " +
            "FROM Schedule s " +
            "INNER JOIN Channel c ON s.channel_id = c.channel_id " +
            "INNER JOIN Program p ON s.program_id = p.program_id " +
            "WHERE s.channel_id = ? AND s.start_time <= NOW() AND s.end_time > NOW() " +
            "ORDER BY s.seq_num LIMIT 1";
```
</details>

---

## [Q7] seq_num 으로 "다음 방송" 조회 SQL 작성 (난이도 ★★★)

**파일**: `practice/repository/ScheduleRepository.java` → `findBySeqNum(String channelId, int seqNum)`

**설명**: 채널과 `seq_num` 으로 특정 스케줄 1건을 조회하는 SQL을 작성합니다.
호출부에서 (현재 방송의 `seq_num` + 1)을 넘기므로, 이 메서드가 곧 "다음 방송" 조회가 됩니다.

**요구사항**
- 파라미터(`?`)는 순서대로 `channelId`, `seqNum` (2개).
- 조건: 해당 채널에서 `seq_num` 이 주어진 값과 일치.
- 조인/컬럼 별칭은 Q6 과 동일 규칙.
- `LIMIT 1`.

**힌트**
- `WHERE s.channel_id = ? AND s.seq_num = ?` — 파라미터 순서가 메서드 인자 순서와 같아야 합니다.

<details>
<summary>정답 펼치기</summary>

```java
String sql = "SELECT s.schedule_id, s.channel_id, c.channel_name, " +
            "s.program_id, p.title, p.genre, p.summary, s.start_time, s.end_time " +
            "FROM Schedule s " +
            "INNER JOIN Channel c ON s.channel_id = c.channel_id " +
            "INNER JOIN Program p ON s.program_id = p.program_id " +
            "WHERE s.channel_id = ? AND s.seq_num = ? " +
            "LIMIT 1";
```
</details>

---

## [Q8] 즐겨찾기 삭제 엔드포인트 — DELETE + 다중 PathVariable (난이도 ★★)

**파일**: `practice/controller/FavoriteChannelController.java`

**설명**: 특정 사용자의 즐겨찾기에서 특정 채널을 삭제합니다.

**요구사항**
- 전체 경로: `DELETE /api/v1/users/{userId}/favorite-channels/{channelId}`
  (클래스 경로 + `/{channelId}`)
- Path 변수 2개: `userId`, `channelId`
- 빈칸:
  - `__BLANK_Q9_A__` : 메서드 매핑 애노테이션
  - `__BLANK_Q9_B__` : `userId` 바인딩
  - `__BLANK_Q9_C__` : `channelId` 바인딩

**힌트**
- 하위 경로 DELETE → `@DeleteMapping("/{channelId}")`
- `{userId}` 는 클래스 경로에 이미 선언되어 있지만, 메서드에서 값을 쓰려면 `@PathVariable` 로 받아야 합니다.

<details>
<summary>정답 펼치기</summary>

```java
// __BLANK_Q9_A__
@DeleteMapping("/{channelId}")

// __BLANK_Q9_B__
@PathVariable

// __BLANK_Q9_C__
@PathVariable
```
각각 `@PathVariable String userId`, `@PathVariable String channelId`.
</details>

---

## [Q9] 사용자 편성표 "시각 범위" 조회 SQL 작성 (난이도 ★★★)

**파일**: `practice/repository/ScheduleRepository.java` → `findUserFavoriteSchedulesWithNamesByRange(...)`

**설명**: 사용자의 즐겨찾기 채널 편성표를 `[start, end)` 시각 범위로 조회하는 SQL을 작성합니다.
범위 경계를 **걸치는** 프로그램(범위 시작 전부터 시작해 범위 안까지 방송되는 등)도 포함해야 합니다.

**요구사항**
- 바인딩되는 파라미터(`?`) 순서는 `userId`, `end`, `start` (코드의 query 호출 순서와 동일).
- 사용자의 즐겨찾기 채널만 대상: `Schedule s` ⨝ `favorite_channels fc` (`fc.user_id = ?`)
- 범위 조건(경계 걸침 포함): `s.start_time < end AND s.end_time > start`
- 채널명·프로그램명 포함: `Schedule` ⨝ `Channel` ⨝ `Program`, `ScheduleDto` 컬럼 별칭 유지.
- 정렬: 채널 ID, 시작 시각 순.

**힌트**
- `WHERE fc.user_id = ? AND s.start_time < ? AND s.end_time > ?` — `?` 순서는 `userId, end, start`.
- 조인 키: `s.channel_id = fc.channel_id`, `s.channel_id = c.channel_id`, `s.program_id = p.program_id`

<details>
<summary>정답 펼치기</summary>

```java
String sql = "SELECT s.schedule_id, s.channel_id, c.channel_name, " +
            "s.program_id, p.title, p.genre, p.summary, s.start_time, s.end_time " +
            "FROM Schedule s " +
            "INNER JOIN favorite_channels fc ON s.channel_id = fc.channel_id " +
            "INNER JOIN Channel c ON s.channel_id = c.channel_id " +
            "INNER JOIN Program p ON s.program_id = p.program_id " +
            "WHERE fc.user_id = ? AND s.start_time < ? AND s.end_time > ? " +
            "ORDER BY c.channel_id, s.start_time";
```
</details>

---

## 동작 확인 체크리스트

완성 후 서버를 실행하고 아래 요청으로 확인해 보세요. (호스트/포트는 환경에 맞게)

```bash
# Q1 — 채널 검색 (그룹 포함)
curl "http://localhost:8282/api/v1/channels/search?keyword=스포츠&includeGroup=true"

# Q2 — 즐겨찾기 상세 목록
curl "http://localhost:8282/api/v1/users/user123/favorite-channels/with-details"

# Q3 — 채널별 편성표 (날짜 지정)
curl "http://localhost:8282/api/v1/channels/100/schedules?date=2026-11-19"

# Q4, Q9 — 사용자 편성표 범위 조회 (Q4 엔드포인트 + Q9 쿼리가 함께 동작)
curl "http://localhost:8282/api/v1/schedules/users/user123/schedules/with-details/range?start=2026-11-09T15:00:00&end=2026-11-10T15:00:00"

# Q5~Q7 — 현재/다음 방송 (Q6, Q7 쿼리가 올바라야 current/next 가 채워짐)
curl "http://localhost:8282/api/v1/schedules/users/user123/current-next"

# Q8 — 즐겨찾기 삭제 (먼저 추가되어 있어야 함)
curl -X DELETE "http://localhost:8282/api/v1/users/user123/favorite-channels/100"
```

- 응답이 `"success": true` 이고 데이터가 기대한 형태로 나오면 통과입니다.
- Q5 의 결과에서 각 채널의 `current`(현재 방송)와 `next`(다음 방송)가 올바르게 채워지는지 확인하세요.
  (현재 방송의 `endTime` 과 다음 방송의 `startTime` 이 이어지면 정상입니다.)
