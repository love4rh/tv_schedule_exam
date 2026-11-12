# TV Schedule REST API 명세서

TV 편성표 서비스의 REST API 명세입니다. 채널·프로그램·편성표 **조회**와 사용자별 즐겨찾기 채널 관리,
그리고 즐겨찾기 기반의 사용자 편성표·현재/다음 방송 조회 기능을 제공합니다.

> 본 교육 과정에서는 채널·프로그램·편성표의 **생성/수정/삭제(CUD) API를 제공하지 않습니다.**
> (즐겨찾기 채널의 추가/삭제는 제공됩니다.)

- **Base URL**: `http://{host}:8282`
- **공통 경로 접두사**: `/api/v1`
- **CORS**: 모든 컨트롤러에 `@CrossOrigin(origins = "*")` 적용 (모든 Origin 허용)

---

## 1. 공통 사항

### 1.1 요청 헤더

| 헤더 | 값 | 필요 시점 |
|------|-----|-----------|
| `Content-Type` | `application/json` | 요청 본문(Body)이 있는 경우 (POST, PUT) |

> 조회(GET)·삭제(DELETE) 등 본문이 없는 요청에는 별도 헤더가 필요하지 않습니다.
> 인증/인가 체계는 적용되어 있지 않습니다.

### 1.2 공통 응답 형식

모든 API는 아래의 통일된 래퍼(`ApiResponse`) 형식으로 응답합니다.

```json
{
  "success": true,
  "data": "...",
  "message": "작업 설명",
  "error": null,
  "timestamp": "2026-11-10T12:00:00.000"
}
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `success` | boolean | 성공 여부 |
| `data` | any | 응답 데이터 (성공 시). 객체/배열/문자열 등 API별로 상이 |
| `message` | string | 처리 결과 메시지 |
| `error` | object \| null | 실패 시 오류 정보. `{ "code": "...", "message": "..." }` |
| `timestamp` | string | 응답 생성 시각 (서버 로컬 시각) |

실패 응답 예시:

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": { "code": "CHANNEL_NOT_FOUND", "message": "채널을 찾을 수 없습니다" },
  "timestamp": "2026-11-10T12:00:00.000"
}
```

### 1.3 시간대(Timezone) 규칙

- 서버는 모든 시각 데이터를 **GMT(UTC) 기준**으로 저장·조회·반환합니다.
- 응답의 `startTime` / `endTime` 등은 타임존 표기가 없는 ISO LocalDateTime 문자열(`YYYY-MM-DDTHH:mm:ss`)이며 **GMT 기준**입니다.
- 표시용 시간대(예: KST) 변환은 **클라이언트 책임**입니다.
- 사용자 편성표의 범위 조회 API(2.3.6)는 클라이언트가 표시용 시간대의 하루를 GMT 범위로 변환하여 전달합니다.

### 1.4 주요 데이터 모델

**Channel**
| 필드 | 타입 | 설명 |
|------|------|------|
| `channelId` | string | 채널 ID (PK) |
| `channelName` | string | 채널명 |
| `channelGroup` | string | 채널 그룹 |

**Program**
| 필드 | 타입 | 설명 |
|------|------|------|
| `programId` | string | 프로그램 ID (PK) |
| `programName` | string | 프로그램 제목 (DB `title`) |
| `genre` | string | 장르 (여러 개면 `|` 구분) |
| `description` | string | 요약 설명 (DB `summary`) |

**Schedule**
| 필드 | 타입 | 설명 |
|------|------|------|
| `scheduleId` | string | 편성표 ID (PK) |
| `channelId` | string | 채널 ID (FK) |
| `programId` | string | 프로그램 ID (FK) |
| `startTime` | string | 시작 시각 (GMT, ISO LocalDateTime) |
| `endTime` | string | 종료 시각 (GMT, ISO LocalDateTime) |

**ScheduleDto** (채널명·프로그램명 포함 상세)
| 필드 | 타입 | 설명 |
|------|------|------|
| `scheduleId` | string | 편성표 ID |
| `channelId` | string | 채널 ID |
| `channelName` | string | 채널명 |
| `programId` | string | 프로그램 ID |
| `programName` | string | 프로그램 제목 |
| `genre` | string | 장르 |
| `summary` | string | 요약 설명 |
| `startTime` | string | 시작 시각 (GMT) |
| `endTime` | string | 종료 시각 (GMT) |

**FavoriteChannelDto** (즐겨찾기 상세)
| 필드 | 타입 | 설명 |
|------|------|------|
| `id` | number | 즐겨찾기 레코드 ID |
| `channelId` | string | 채널 ID |
| `channelName` | string | 채널명 |
| `channelGroup` | string | 채널 그룹 |
| `addedAt` | string | 즐겨찾기 추가 시각 |

**CurrentNextDto** (현재/다음 방송)
| 필드 | 타입 | 설명 |
|------|------|------|
| `channelId` | string | 채널 ID |
| `channelName` | string | 채널명 |
| `channelGroup` | string | 채널 그룹 |
| `current` | ScheduleDto \| null | 현재 방송 중 프로그램 (없으면 null) |
| `next` | ScheduleDto \| null | 다음 방송 프로그램 (없으면 null) |

---

## 2. API 목록

### 2.1 채널 관리 — `/api/v1/channels`

#### 2.1.1 전체 채널 목록 조회
- **API 명**: 채널 목록 조회
- **경로**: `/api/v1/channels`
- **메소드**: `GET`
- **기능 설명**: 등록된 전체 채널 목록을 조회합니다.
- **헤더**: 없음
- **파라미터**: 없음
- **응답 `data`**: `Channel[]`

#### 2.1.2 채널 검색
- **API 명**: 채널 검색
- **경로**: `/api/v1/channels/search`
- **메소드**: `GET`
- **기능 설명**: 채널 ID 또는 채널명에 검색어가 포함된 채널을 조회합니다. (부분 일치, 채널 ID 순 정렬)
- **헤더**: 없음
- **파라미터 (Query)**:

  | 이름 | 타입 | 필수 | 설명 |
  |------|------|------|------|
  | `keyword` | string | 예 | 검색어 (채널명 또는 채널 ID 부분 문자열) |

- **응답 `data`**: `Channel[]`

#### 2.1.3 특정 채널 조회
- **API 명**: 채널 단건 조회
- **경로**: `/api/v1/channels/{channelId}`
- **메소드**: `GET`
- **기능 설명**: 특정 채널의 상세 정보를 조회합니다.
- **헤더**: 없음
- **파라미터 (Path)**:

  | 이름 | 타입 | 필수 | 설명 |
  |------|------|------|------|
  | `channelId` | string | 예 | 채널 ID |

- **응답 `data`**: `Channel`
- **오류**: `404 CHANNEL_NOT_FOUND`

#### 2.1.4 채널별 편성표 조회
- **API 명**: 채널 편성표 조회
- **경로**: `/api/v1/channels/{channelId}/schedules`
- **메소드**: `GET`
- **기능 설명**: 특정 채널의 편성표를 조회합니다. 날짜(`date`) 지정 시 해당 날짜(GMT 기준)만 조회합니다. 채널명·프로그램명이 포함된 상세(ScheduleDto)로 반환됩니다.
- **헤더**: 없음
- **파라미터 (Path / Query)**:

  | 이름 | 위치 | 타입 | 필수 | 설명 |
  |------|------|------|------|------|
  | `channelId` | Path | string | 예 | 채널 ID |
  | `date` | Query | string | 아니오 | 조회 날짜 `YYYY-MM-DD` (GMT 기준). 생략 시 전체 |

- **응답 `data`**: `ScheduleDto[]`
- **오류**: `404 CHANNEL_NOT_FOUND`, `400 INVALID_DATE_FORMAT`

> 채널 생성/수정/삭제 API는 본 교육 과정에서 제공하지 않습니다. (조회·검색만 제공)

---

### 2.2 프로그램 관리 — `/api/v1/programs`

#### 2.2.1 전체 프로그램 목록 조회
- **API 명**: 프로그램 목록 조회
- **경로**: `/api/v1/programs`
- **메소드**: `GET`
- **기능 설명**: 전체 프로그램 목록을 조회합니다.
- **헤더**: 없음
- **파라미터**: 없음
- **응답 `data`**: `Program[]`

#### 2.2.2 특정 프로그램 조회
- **API 명**: 프로그램 단건 조회
- **경로**: `/api/v1/programs/{programId}`
- **메소드**: `GET`
- **기능 설명**: 특정 프로그램의 상세 정보를 조회합니다.
- **헤더**: 없음
- **파라미터 (Path)**:

  | 이름 | 타입 | 필수 | 설명 |
  |------|------|------|------|
  | `programId` | number | 예 | 프로그램 ID |

- **응답 `data`**: `Program`
- **오류**: `404 PROGRAM_NOT_FOUND`

> 프로그램 생성/수정/삭제 API는 본 교육 과정에서 제공하지 않습니다. (조회만 제공)

---

### 2.3 편성표 관리 — `/api/v1/schedules`

#### 2.3.1 편성표 목록 조회 (필터)
- **API 명**: 편성표 조회
- **경로**: `/api/v1/schedules`
- **메소드**: `GET`
- **기능 설명**: 편성표를 조회합니다. 파라미터 조합에 따라 필터링합니다.
  - `date` + `channelId` → 특정 채널의 특정 날짜
  - `date`만 → 전체 채널의 해당 날짜
  - 파라미터 없음 → 전체
- **헤더**: 없음
- **파라미터 (Query)**:

  | 이름 | 타입 | 필수 | 설명 |
  |------|------|------|------|
  | `date` | string | 아니오 | 조회 날짜 `YYYY-MM-DD` (GMT 기준) |
  | `channelId` | string | 아니오 | 채널 ID |

- **응답 `data`**: `Schedule[]`
- **오류**: `400 INVALID_DATE_FORMAT`

#### 2.3.2 특정 편성표 조회
- **API 명**: 편성표 단건 조회
- **경로**: `/api/v1/schedules/{scheduleId}`
- **메소드**: `GET`
- **기능 설명**: 특정 편성표를 조회합니다.
- **헤더**: 없음
- **파라미터 (Path)**: `scheduleId` (string, 필수)
- **응답 `data`**: `Schedule`
- **오류**: `404 SCHEDULE_NOT_FOUND`

> 편성표 생성/수정/삭제 API는 본 교육 과정에서 제공하지 않습니다. (조회류만 제공)

#### 2.3.6 사용자 즐겨찾기 편성표 조회 (날짜)
- **API 명**: 사용자 편성표 조회
- **경로**: `/api/v1/schedules/users/{userId}/schedules`
- **메소드**: `GET`
- **기능 설명**: 사용자의 즐겨찾기 채널들의 편성표를 특정 날짜(GMT 기준)로 조회합니다. (기본 Schedule 형식)
- **헤더**: 없음
- **파라미터 (Path / Query)**:

  | 이름 | 위치 | 타입 | 필수 | 설명 |
  |------|------|------|------|------|
  | `userId` | Path | string | 예 | 사용자 ID |
  | `date` | Query | string | 예 | 조회 날짜 `YYYY-MM-DD` (GMT 기준) |

- **응답 `data`**: `Schedule[]`
- **오류**: `400 INVALID_DATE_FORMAT`

#### 2.3.7 사용자 즐겨찾기 편성표 상세 조회 (날짜)
- **API 명**: 사용자 편성표 상세 조회
- **경로**: `/api/v1/schedules/users/{userId}/schedules/with-details`
- **메소드**: `GET`
- **기능 설명**: 사용자의 즐겨찾기 채널 편성표를 특정 날짜(GMT 기준)로 조회합니다. 채널명·프로그램명 포함 상세(ScheduleDto)로 반환하며 채널 ID·시작 시각 순으로 정렬됩니다.
- **헤더**: 없음
- **파라미터 (Path / Query)**:

  | 이름 | 위치 | 타입 | 필수 | 설명 |
  |------|------|------|------|------|
  | `userId` | Path | string | 예 | 사용자 ID |
  | `date` | Query | string | 예 | 조회 날짜 `YYYY-MM-DD` (GMT 기준) |

- **응답 `data`**: `ScheduleDto[]`
- **오류**: `400 INVALID_DATE_FORMAT`

#### 2.3.8 사용자 즐겨찾기 편성표 상세 조회 (시각 범위) — 시간대 대응
- **API 명**: 사용자 편성표 범위 조회
- **경로**: `/api/v1/schedules/users/{userId}/schedules/with-details/range`
- **메소드**: `GET`
- **기능 설명**: 사용자 즐겨찾기 채널 편성표를 `[start, end)` 시각 범위(**GMT 기준**)로 조회합니다. 범위에 걸쳐 있는(경계를 넘나드는) 프로그램도 포함합니다(`start_time < end AND end_time > start`). 클라이언트가 표시용 시간대(예: KST)의 하루를 GMT 범위로 변환해 전달하는 용도입니다.
- **헤더**: 없음
- **파라미터 (Path / Query)**:

  | 이름 | 위치 | 타입 | 필수 | 설명 |
  |------|------|------|------|------|
  | `userId` | Path | string | 예 | 사용자 ID |
  | `start` | Query | string | 예 | 시작 시각 (포함), ISO LocalDateTime `YYYY-MM-DDTHH:mm:ss` (GMT) |
  | `end` | Query | string | 예 | 종료 시각 (미포함), ISO LocalDateTime `YYYY-MM-DDTHH:mm:ss` (GMT) |

- **예시**: KST `2026-11-10` 하루 → `start=2026-11-09T15:00:00&end=2026-11-10T15:00:00`
- **응답 `data`**: `ScheduleDto[]` (채널 ID·시작 시각 순 정렬)
- **오류**: `400 INVALID_DATETIME_FORMAT`

#### 2.3.9 사용자 현재/다음 방송 조회
- **API 명**: 현재/다음 방송 조회
- **경로**: `/api/v1/schedules/users/{userId}/current-next`
- **메소드**: `GET`
- **기능 설명**: 사용자의 즐겨찾기 채널마다 현재 방송 중인 프로그램과 다음 방송 프로그램을 조회합니다. 현재 방송은 서버 현재 시각(GMT, `NOW()`)이 `start_time <= NOW < end_time`인 스케줄이며, 다음 방송은 같은 채널의 `seq_num + 1` 스케줄입니다. 현재 방송이 없으면 앞으로 가장 이른 방송을 `next`로 제공합니다.
- **헤더**: 없음
- **파라미터 (Path)**: `userId` (string, 필수)
- **응답 `data`**: `CurrentNextDto[]`

---

### 2.4 즐겨찾기 채널 — `/api/v1/users/{userId}/favorite-channels`

> 공통 Path 파라미터: `userId` (string, 필수) — 사용자 ID

#### 2.4.1 즐겨찾기 목록 조회
- **API 명**: 즐겨찾기 목록 조회
- **경로**: `/api/v1/users/{userId}/favorite-channels`
- **메소드**: `GET`
- **기능 설명**: 사용자의 즐겨찾기 채널 목록을 조회합니다. (채널 ID 중심의 기본 형식)
- **헤더**: 없음
- **파라미터 (Path)**: `userId`
- **응답 `data`**: `FavoriteChannel[]` (`id`, `userId`, `channelId`, `addedAt`)

#### 2.4.2 즐겨찾기 목록 상세 조회
- **API 명**: 즐겨찾기 상세 목록 조회
- **경로**: `/api/v1/users/{userId}/favorite-channels/with-details`
- **메소드**: `GET`
- **기능 설명**: 사용자의 즐겨찾기 채널을 채널명·그룹 포함 상세(FavoriteChannelDto)로 조회합니다. 추가 시각 순 정렬.
- **헤더**: 없음
- **파라미터 (Path)**: `userId`
- **응답 `data`**: `FavoriteChannelDto[]`

#### 2.4.3 즐겨찾기 추가
- **API 명**: 즐겨찾기 추가
- **경로**: `/api/v1/users/{userId}/favorite-channels`
- **메소드**: `POST`
- **기능 설명**: 사용자 즐겨찾기에 채널을 추가합니다.
- **헤더**: `Content-Type: application/json`
- **파라미터 (Path)**: `userId`
- **요청 본문 (Body)**:

  ```json
  { "channelId": "MBC" }
  ```

  | 필드 | 타입 | 필수 | 설명 |
  |------|------|------|------|
  | `channelId` | string | 예 | 추가할 채널 ID |

- **응답 `data`**: `"SUCCESS"` (추가됨, 201)
- **오류**: `400 INVALID_CHANNEL_ID`, `404 CHANNEL_NOT_FOUND`, `409 DUPLICATE_FAVORITE`

#### 2.4.4 즐겨찾기 삭제
- **API 명**: 즐겨찾기 삭제
- **경로**: `/api/v1/users/{userId}/favorite-channels/{channelId}`
- **메소드**: `DELETE`
- **기능 설명**: 사용자 즐겨찾기에서 특정 채널을 삭제합니다.
- **헤더**: 없음
- **파라미터 (Path)**:

  | 이름 | 타입 | 필수 | 설명 |
  |------|------|------|------|
  | `userId` | string | 예 | 사용자 ID |
  | `channelId` | string | 예 | 삭제할 채널 ID |

- **응답 `data`**: `"SUCCESS"`
- **오류**: `404 FAVORITE_NOT_FOUND`

#### 2.4.5 즐겨찾기 개수 조회
- **API 명**: 즐겨찾기 개수 조회
- **경로**: `/api/v1/users/{userId}/favorite-channels/count`
- **메소드**: `GET`
- **기능 설명**: 사용자의 즐겨찾기 채널 개수를 조회합니다.
- **헤더**: 없음
- **파라미터 (Path)**: `userId`
- **응답 `data`**: `{ "count": number }`

#### 2.4.6 즐겨찾기 여부 확인
- **API 명**: 즐겨찾기 여부 확인
- **경로**: `/api/v1/users/{userId}/favorite-channels/check/{channelId}`
- **메소드**: `GET`
- **기능 설명**: 특정 채널이 사용자의 즐겨찾기에 등록되어 있는지 확인합니다.
- **헤더**: 없음
- **파라미터 (Path)**:

  | 이름 | 타입 | 필수 | 설명 |
  |------|------|------|------|
  | `userId` | string | 예 | 사용자 ID |
  | `channelId` | string | 예 | 확인할 채널 ID |

- **응답 `data`**: `{ "isFavorite": boolean }`

---

## 3. 오류 코드 정리

| HTTP | code | 발생 상황 |
|------|------|-----------|
| 400 | `INVALID_DATE_FORMAT` | 날짜 형식 오류 (`YYYY-MM-DD` 아님) |
| 400 | `INVALID_DATETIME_FORMAT` | 시각 형식 오류 (ISO LocalDateTime 아님) |
| 400 | `INVALID_CHANNEL_ID` | 즐겨찾기 추가 시 channelId 누락 |
| 404 | `CHANNEL_NOT_FOUND` | 채널 없음 |
| 404 | `PROGRAM_NOT_FOUND` | 프로그램 없음 |
| 404 | `SCHEDULE_NOT_FOUND` | 편성표 없음 |
| 404 | `FAVORITE_NOT_FOUND` | 즐겨찾기 항목 없음 |
| 409 | `DUPLICATE_FAVORITE` | 이미 등록된 즐겨찾기 |

---

## 4. cURL 예시

```bash
# 채널 검색 (채널명/ID 부분 일치)
curl -X GET "http://localhost:8282/api/v1/channels/search?keyword=KBS"

# 채널별 편성표 (날짜 지정)
curl -X GET "http://localhost:8282/api/v1/channels/100/schedules?date=2026-11-10"

# 즐겨찾기 상세 목록
curl -X GET "http://localhost:8282/api/v1/users/user123/favorite-channels/with-details"

# 즐겨찾기 추가
curl -X POST "http://localhost:8282/api/v1/users/user123/favorite-channels" \
  -H "Content-Type: application/json" \
  -d '{"channelId": "100"}'

# 즐겨찾기 삭제
curl -X DELETE "http://localhost:8282/api/v1/users/user123/favorite-channels/100"

# 사용자 편성표 상세 (날짜, GMT)
curl -X GET "http://localhost:8282/api/v1/schedules/users/user123/schedules/with-details?date=2026-11-10"

# 사용자 편성표 상세 (시각 범위, 시간대 대응: KST 2026-11-10)
curl -X GET "http://localhost:8282/api/v1/schedules/users/user123/schedules/with-details/range?start=2026-11-09T15:00:00&end=2026-11-10T15:00:00"

# 현재/다음 방송
curl -X GET "http://localhost:8282/api/v1/schedules/users/user123/current-next"
```
