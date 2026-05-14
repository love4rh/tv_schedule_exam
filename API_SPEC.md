---
title: TV 편성표 서비스 API 연동 규격서
version: 1.0.0
status: RELEASED
created: 2026-05-11
updated: 2026-05-11
---

# TV 편성표 서비스 API 연동 규격서

---

## 목차

1. [개요](#1-개요)
2. [비기능적 요구 사항](#2-비기능적-요구-사항)
3. [공통 정보](#3-공통-정보)
4. [공통 응답 구조](#4-공통-응답-구조)
5. [API 상세 명세](#5-api-상세-명세)
6. [공통 오류 코드](#6-공통-오류-코드)
7. [변경 이력](#7-변경-이력)

---

## 1. 개요

### 목적

본 문서는 TV 편성표 서비스의 HTTP REST API 사용 방법을 정의한다.  
프로그램 및 채널 정보를 조회·생성·수정·삭제하는 엔드포인트를 다룬다.

### 범위

- 프로그램(Program) CRUD
- 채널(Channel) CRUD
- 채널 편성표(Schedule) 조회

### 연동 방식 (인터페이스 요구 사항)

| 항목 | 내용 |
|------|------|
| 프로토콜 | HTTP |
| 데이터 형식 | JSON (UTF-8) |
| 문자 인코딩 | UTF-8 |
| 인증 방식 | 없음 (현재 버전은 인증 불필요) |
| API 버전 | v1 |
| Base URL | `http://localhost:8080/api/v1` |

---

## 2. 비기능적 요구 사항

### 성능

| 항목 | 기준 |
|------|------|
| 단건 조회 응답 시간 | 1초 이내 |
| 목록 조회 응답 시간 | 3초 이내 |
| 편성표 날짜 필터 조회 | 2초 이내 |

### 보안 정책

| 항목 | 내용 |
|------|------|
| 인증/인가 | 없음 — 현재 버전은 인증 없이 모든 요청 허용 |
| CORS | 전체 오리진 허용 (`@CrossOrigin(origins = "*")`) — 개발·학습 환경 전용, 실운영 시 허용 도메인 명시 필요 |
| 데이터 암호화 | 없음 (HTTP 평문 통신, 개발 환경 기준) |
| 접근 제어 | 없음 — IP 또는 Role 기반 접근 제어 미적용 |

> **참고:** 위 보안 설정은 학습용 환경 기준이다. 실제 서비스 배포 시에는 HTTPS 적용, CORS 도메인 제한, 인증 토큰(JWT 등) 도입이 필요하다.

### 사용성

| 항목 | 내용 |
|------|------|
| 응답 구조 일관성 | 모든 API가 동일한 `ApiResponse<T>` 래퍼 구조 사용 |
| 오류 응답 | 모든 오류에 `error.code` + `error.message` 포함, 원인 파악 용이 |

### 확장성

| 항목 | 내용 |
|------|------|
| URL 버전 관리 | `/api/v1` prefix 적용 — 향후 v2 도입 시 기존 클라이언트 영향 없이 병행 운영 가능 |
| 모듈 독립성 | 프로그램·채널·편성표 API가 독립 구성 — 기능별 독립 확장 가능 |

---

## 3. 공통 정보

### 공통 요청 헤더

| 헤더 | 필수 | 값 |
|------|------|----|
| `Content-Type` | Y (요청 Body가 있는 경우) | `application/json` |

### 날짜/시간 형식

| 항목 | 형식 | 예시 |
|------|------|------|
| 날짜 | `YYYY-MM-DD` | `2026-05-11` |
| 날짜+시간 | ISO 8601 | `2026-05-11T09:00:00` |

---

## 4. 공통 응답 구조

모든 API는 아래의 공통 JSON 구조로 응답한다.

### 성공 응답

```json
{
  "success": true,
  "data": { ... },
  "message": "처리 결과 메시지",
  "error": null,
  "timestamp": "2026-05-11T09:00:00"
}
```

### 오류 응답

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": {
    "code": "에러코드",
    "message": "오류 설명 메시지"
  },
  "timestamp": "2026-05-11T09:00:00"
}
```

### 공통 응답 필드

| 필드 | 타입 | 설명 |
|------|------|------|
| `success` | Boolean | 요청 성공 여부 (`true` / `false`) |
| `data` | Object \| Array \| null | 응답 데이터 (실패 시 `null`) |
| `message` | String \| null | 성공 메시지 (실패 시 `null`) |
| `error` | Object \| null | 오류 정보 (성공 시 `null`) |
| `error.code` | String | 오류 코드 |
| `error.message` | String | 오류 설명 |
| `timestamp` | String | 응답 생성 시각 (ISO 8601) |

---

## 5. API 상세 명세

### 전체 엔드포인트 요약

| 컨트롤러 | 메서드 | 경로 | 설명 |
|----------|--------|------|------|
| ProgramController | `GET` | `/api/v1/programs` | 전체 프로그램 목록 조회 |
| ProgramController | `GET` | `/api/v1/programs/{programId}` | 특정 프로그램 조회 |
| ProgramController | `POST` | `/api/v1/programs` | 프로그램 생성 |
| ProgramController | `PUT` | `/api/v1/programs/{programId}` | 프로그램 수정 |
| ProgramController | `DELETE` | `/api/v1/programs/{programId}` | 프로그램 삭제 |
| ChannelController | `GET` | `/api/v1/channels` | 전체 채널 목록 조회 |
| ChannelController | `GET` | `/api/v1/channels/{channelId}` | 특정 채널 조회 |
| ChannelController | `GET` | `/api/v1/channels/{channelId}/schedules` | 채널 편성표 조회 |
| ChannelController | `POST` | `/api/v1/channels` | 채널 생성 |
| ChannelController | `PUT` | `/api/v1/channels/{channelId}` | 채널 수정 |
| ChannelController | `DELETE` | `/api/v1/channels/{channelId}` | 채널 삭제 |

---

### ProgramController

Base Path: `/api/v1/programs`

---

#### GET /api/v1/programs — 전체 프로그램 목록 조회

**설명:** 등록된 모든 프로그램의 목록을 반환한다.

**처리 흐름:**
1. DB에서 전체 프로그램 목록을 조회한다.
2. 조회 결과(0건 포함)를 배열로 반환한다.

**Request**

요청 파라미터 없음.

**Response**

| 필드 | 타입 | 설명 |
|------|------|------|
| `data` | Array | 프로그램 객체 배열 |
| `data[].programId` | String | 프로그램 고유 ID |
| `data[].programName` | String | 프로그램 이름 |
| `data[].genre` | String | 장르 |
| `data[].description` | String | 프로그램 설명 |

**Response Body 예시 (성공)**

```json
{
  "success": true,
  "data": [
    {
      "programId": "1",
      "programName": "뉴스데스크",
      "genre": "뉴스",
      "description": "저녁 종합 뉴스 프로그램"
    },
    {
      "programId": "2",
      "programName": "무한도전",
      "genre": "예능",
      "description": "주말 버라이어티 예능"
    }
  ],
  "message": "프로그램 목록 조회 성공",
  "error": null,
  "timestamp": "2026-05-11T09:00:00"
}
```

**HTTP 상태코드**

| 코드 | 의미 |
|------|------|
| `200` | 성공 (프로그램이 없을 경우 빈 배열 `[]` 반환) |

---

#### GET /api/v1/programs/{programId} — 특정 프로그램 조회

**설명:** programId에 해당하는 프로그램 정보를 반환한다.

**처리 흐름:**
1. Path에서 `programId`(Long)를 추출한다.
2. DB에서 해당 ID의 프로그램을 조회한다.
3. 존재하면 프로그램 정보를 반환한다.
4. 존재하지 않으면 `PROGRAM_NOT_FOUND` 오류와 함께 404를 반환한다.

> **주의:** 현재 코드에서 `GET` 요청의 `programId`는 **숫자(Long)** 만 허용한다.  
> 같은 경로의 `PUT` · `DELETE`는 `String`으로 선언되어 있어 타입이 불일치한다.  
> 실제 프로그램 ID는 DB 자동 생성 숫자 키 (`1`, `2`, …) 이므로 숫자 값을 사용해야 한다.

**Path Parameter**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `programId` | Long | Y | 조회할 프로그램의 ID (숫자만 허용) |

**Response**

| 필드 | 타입 | 설명 |
|------|------|------|
| `data.programId` | String | 프로그램 고유 ID |
| `data.programName` | String | 프로그램 이름 |
| `data.genre` | String | 장르 |
| `data.description` | String | 프로그램 설명 |

**Response Body 예시 (성공)**

```json
{
  "success": true,
  "data": {
    "programId": "1",
    "programName": "뉴스데스크",
    "genre": "뉴스",
    "description": "저녁 종합 뉴스 프로그램"
  },
  "message": "프로그램 조회 성공",
  "error": null,
  "timestamp": "2026-05-11T09:00:00"
}
```

**Response Body 예시 (실패 — 존재하지 않는 ID)**

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": {
    "code": "PROGRAM_NOT_FOUND",
    "message": "프로그램을 찾을 수 없습니다"
  },
  "timestamp": "2026-05-11T09:00:00"
}
```

**HTTP 상태코드**

| 코드 | 의미 |
|------|------|
| `200` | 성공 |
| `404` | 해당 programId의 프로그램이 존재하지 않음 |

---

#### POST /api/v1/programs — 프로그램 생성

**설명:** 새로운 프로그램을 등록한다.

**처리 흐름:**
1. 요청 Body에서 프로그램 정보를 받는다.
2. DB에 INSERT한다. programId는 DB가 자동 생성(숫자 증가)한다.
3. 생성된 ID를 포함한 프로그램 정보를 201로 반환한다.

**Request Body**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `programName` | String | Y | 프로그램 이름 |
| `genre` | String | Y | 장르 |
| `description` | String | N | 프로그램 설명 |

**Request Body 예시**

```json
{
  "programName": "주말 드라마",
  "genre": "드라마",
  "description": "가족 모두가 함께 보는 주말 드라마"
}
```

**Response**

| 필드 | 타입 | 설명 |
|------|------|------|
| `data.programId` | String | 생성된 프로그램의 고유 ID (DB 자동 생성 숫자, 예: `"3"`) |
| `data.programName` | String | 프로그램 이름 |
| `data.genre` | String | 장르 |
| `data.description` | String | 프로그램 설명 |

**Response Body 예시 (성공)**

```json
{
  "success": true,
  "data": {
    "programId": "3",
    "programName": "주말 드라마",
    "genre": "드라마",
    "description": "가족 모두가 함께 보는 주말 드라마"
  },
  "message": "프로그램이 생성되었습니다",
  "error": null,
  "timestamp": "2026-05-11T09:00:00"
}
```

**HTTP 상태코드**

| 코드 | 의미 |
|------|------|
| `201` | 생성 성공 |
| `400` | 요청 Body가 유효한 JSON이 아닌 경우 (Spring 기본 처리) |

---

#### PUT /api/v1/programs/{programId} — 프로그램 수정

**설명:** 기존 프로그램의 정보를 수정한다. 요청 Body에 포함된 모든 필드가 덮어씌워진다.

**처리 흐름:**
1. Path에서 `programId`(String)를 추출한다.
2. DB에서 해당 ID 존재 여부를 확인한다.
3. 존재하지 않으면 `PROGRAM_NOT_FOUND` 오류와 함께 404를 반환한다.
4. 존재하면 요청 Body의 전체 필드로 기존 데이터를 덮어쓰기(전체 교체) 후 수정된 프로그램을 반환한다.

**Path Parameter**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `programId` | String | Y | 수정할 프로그램의 ID |

**Request Body**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `programName` | String | Y | 변경할 프로그램 이름 |
| `genre` | String | Y | 변경할 장르 |
| `description` | String | N | 변경할 프로그램 설명 |

**Request Body 예시**

```json
{
  "programName": "주말 드라마 시즌2",
  "genre": "드라마",
  "description": "시즌1의 뒤를 잇는 새로운 이야기"
}
```

**Response Body 예시 (성공)**

```json
{
  "success": true,
  "data": {
    "programId": "3",
    "programName": "주말 드라마 시즌2",
    "genre": "드라마",
    "description": "시즌1의 뒤를 잇는 새로운 이야기"
  },
  "message": "프로그램이 수정되었습니다",
  "error": null,
  "timestamp": "2026-05-11T09:00:00"
}
```

**Response Body 예시 (실패 — 존재하지 않는 ID)**

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": {
    "code": "PROGRAM_NOT_FOUND",
    "message": "프로그램을 찾을 수 없습니다"
  },
  "timestamp": "2026-05-11T09:00:00"
}
```

**HTTP 상태코드**

| 코드 | 의미 |
|------|------|
| `200` | 수정 성공 |
| `404` | 해당 programId의 프로그램이 존재하지 않음 |

---

#### DELETE /api/v1/programs/{programId} — 프로그램 삭제

**설명:** 지정한 프로그램을 삭제한다.

**처리 흐름:**
1. Path에서 `programId`(String)를 추출한다.
2. DB에서 해당 ID 존재 여부를 확인한다.
3. 존재하지 않으면 `PROGRAM_NOT_FOUND` 오류와 함께 404를 반환한다.
4. 존재하면 DB에서 삭제 후 `"SUCCESS"` 메시지를 반환한다.

**Path Parameter**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `programId` | String | Y | 삭제할 프로그램의 ID |

**Response Body 예시 (성공)**

```json
{
  "success": true,
  "data": "SUCCESS",
  "message": "프로그램이 삭제되었습니다",
  "error": null,
  "timestamp": "2026-05-11T09:00:00"
}
```

**Response Body 예시 (실패 — 존재하지 않는 ID)**

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": {
    "code": "PROGRAM_NOT_FOUND",
    "message": "프로그램을 찾을 수 없습니다"
  },
  "timestamp": "2026-05-11T09:00:00"
}
```

**HTTP 상태코드**

| 코드 | 의미 |
|------|------|
| `200` | 삭제 성공 |
| `404` | 해당 programId의 프로그램이 존재하지 않음 |

---

### ChannelController

Base Path: `/api/v1/channels`

---

#### GET /api/v1/channels — 전체 채널 목록 조회

**설명:** 등록된 모든 채널의 목록을 반환한다.

**처리 흐름:**
1. DB에서 전체 채널 목록을 조회한다.
2. 조회 결과(0건 포함)를 배열로 반환한다.

**Request**

요청 파라미터 없음.

**Response**

| 필드 | 타입 | 설명 |
|------|------|------|
| `data` | Array | 채널 객체 배열 |
| `data[].channelId` | String | 채널 고유 ID |
| `data[].channelName` | String | 채널 이름 |
| `data[].channelGroup` | String | 채널 그룹 (예: 지상파, 케이블) |

**Response Body 예시 (성공)**

```json
{
  "success": true,
  "data": [
    {
      "channelId": "CH-001",
      "channelName": "MBC",
      "channelGroup": "지상파"
    },
    {
      "channelId": "CH-002",
      "channelName": "tvN",
      "channelGroup": "케이블"
    }
  ],
  "message": "채널 목록 조회 성공",
  "error": null,
  "timestamp": "2026-05-11T09:00:00"
}
```

**HTTP 상태코드**

| 코드 | 의미 |
|------|------|
| `200` | 성공 (채널이 없을 경우 빈 배열 `[]` 반환) |

---

#### GET /api/v1/channels/{channelId} — 특정 채널 조회

**설명:** channelId에 해당하는 채널 정보를 반환한다.

**처리 흐름:**
1. Path에서 `channelId`를 추출한다.
2. DB에서 해당 ID의 채널을 조회한다.
3. 존재하면 채널 정보를 반환한다.
4. 존재하지 않으면 `CHANNEL_NOT_FOUND` 오류와 함께 404를 반환한다.

**Path Parameter**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `channelId` | String | Y | 조회할 채널의 ID |

**Response**

| 필드 | 타입 | 설명 |
|------|------|------|
| `data.channelId` | String | 채널 고유 ID |
| `data.channelName` | String | 채널 이름 |
| `data.channelGroup` | String | 채널 그룹 |

**Response Body 예시 (성공)**

```json
{
  "success": true,
  "data": {
    "channelId": "CH-001",
    "channelName": "MBC",
    "channelGroup": "지상파"
  },
  "message": "채널 조회 성공",
  "error": null,
  "timestamp": "2026-05-11T09:00:00"
}
```

**Response Body 예시 (실패 — 존재하지 않는 ID)**

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": {
    "code": "CHANNEL_NOT_FOUND",
    "message": "채널을 찾을 수 없습니다"
  },
  "timestamp": "2026-05-11T09:00:00"
}
```

**HTTP 상태코드**

| 코드 | 의미 |
|------|------|
| `200` | 성공 |
| `404` | 해당 channelId의 채널이 존재하지 않음 |

---

#### GET /api/v1/channels/{channelId}/schedules — 채널 편성표 조회

**설명:** 특정 채널의 방송 편성표를 반환한다. `date` 파라미터를 전달하면 해당 날짜의 편성표만 필터링한다.

**처리 흐름:**
1. Path에서 `channelId`를 추출한다.
2. DB에서 해당 ID의 채널 존재 여부를 확인한다. 없으면 `CHANNEL_NOT_FOUND` + 404 반환.
3. `date` 파라미터가 있으면 `YYYY-MM-DD` 형식으로 파싱한다. 파싱 실패 시 `INVALID_DATE_FORMAT` + 400 반환.
4. `date`가 있으면 해당 날짜 편성표만, 없으면 전체 편성표를 조회하여 반환한다.

**Path Parameter**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `channelId` | String | Y | 편성표를 조회할 채널의 ID |

**Query Parameter**

| 파라미터 | 타입 | 필수 | 형식 | 설명 |
|----------|------|------|------|------|
| `date` | String | N | `YYYY-MM-DD` | 조회 날짜. 생략 시 전체 편성표 반환 |

**요청 예시**

- 전체 편성표: `GET /api/v1/channels/CH-001/schedules`
- 특정 날짜: `GET /api/v1/channels/CH-001/schedules?date=2026-05-11`

**Response**

| 필드 | 타입 | 설명 |
|------|------|------|
| `data` | Array | 편성표 객체 배열 |
| `data[].scheduleId` | String | 편성 고유 ID |
| `data[].channelId` | String | 채널 ID |
| `data[].channelName` | String | 채널 이름 |
| `data[].programId` | String | 프로그램 ID |
| `data[].programName` | String | 프로그램 이름 |
| `data[].genre` | String | 장르 |
| `data[].summary` | String | 방송 요약 |
| `data[].startTime` | String | 방송 시작 시각 (ISO 8601) |
| `data[].endTime` | String | 방송 종료 시각 (ISO 8601) |

**Response Body 예시 (성공)**

```json
{
  "success": true,
  "data": [
    {
      "scheduleId": "SCH-001",
      "channelId": "CH-001",
      "channelName": "MBC",
      "programId": "PROG-001",
      "programName": "뉴스데스크",
      "genre": "뉴스",
      "summary": "오늘의 주요 뉴스를 전달합니다.",
      "startTime": "2026-05-11T21:00:00",
      "endTime": "2026-05-11T22:00:00"
    }
  ],
  "message": "채널 편성표 조회 성공",
  "error": null,
  "timestamp": "2026-05-11T09:00:00"
}
```

**Response Body 예시 (실패 — 채널 없음)**

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": {
    "code": "CHANNEL_NOT_FOUND",
    "message": "채널을 찾을 수 없습니다"
  },
  "timestamp": "2026-05-11T09:00:00"
}
```

**Response Body 예시 (실패 — 날짜 형식 오류)**

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": {
    "code": "INVALID_DATE_FORMAT",
    "message": "날짜 형식이 올바르지 않습니다 (YYYY-MM-DD)"
  },
  "timestamp": "2026-05-11T09:00:00"
}
```

**HTTP 상태코드**

| 코드 | 의미 |
|------|------|
| `200` | 성공 (편성표가 없으면 빈 배열 `[]` 반환) |
| `400` | `date` 파라미터 형식이 올바르지 않음 |
| `404` | 해당 channelId의 채널이 존재하지 않음 |

---

#### POST /api/v1/channels — 채널 생성

**설명:** 새로운 채널을 등록한다. channelId가 이미 존재하면 409 오류를 반환한다.

**처리 흐름:**
1. 요청 Body에서 채널 정보를 받는다.
2. 요청의 `channelId`가 DB에 이미 존재하는지 확인한다. 이미 존재하면 `DUPLICATE_CHANNEL` + 409 반환.
3. 중복이 없으면 DB에 저장 후 생성된 채널 정보를 201로 반환한다.

**Request Body**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `channelId` | String | Y | 채널 고유 ID (직접 지정) |
| `channelName` | String | Y | 채널 이름 |
| `channelGroup` | String | N | 채널 그룹 (예: 지상파, 케이블, 종편) |

**Request Body 예시**

```json
{
  "channelId": "CH-003",
  "channelName": "JTBC",
  "channelGroup": "종편"
}
```

**Response Body 예시 (성공)**

```json
{
  "success": true,
  "data": {
    "channelId": "CH-003",
    "channelName": "JTBC",
    "channelGroup": "종편"
  },
  "message": "채널이 생성되었습니다",
  "error": null,
  "timestamp": "2026-05-11T09:00:00"
}
```

**Response Body 예시 (실패 — 중복 ID)**

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": {
    "code": "DUPLICATE_CHANNEL",
    "message": "이미 존재하는 채널 ID입니다"
  },
  "timestamp": "2026-05-11T09:00:00"
}
```

**HTTP 상태코드**

| 코드 | 의미 |
|------|------|
| `201` | 생성 성공 |
| `409` | 동일한 channelId가 이미 존재함 |

---

#### PUT /api/v1/channels/{channelId} — 채널 수정

**설명:** 기존 채널의 정보를 수정한다. 요청 Body에 포함된 모든 필드가 덮어씌워진다.

**처리 흐름:**
1. Path에서 `channelId`를 추출한다.
2. DB에서 해당 ID 존재 여부를 확인한다. 없으면 `CHANNEL_NOT_FOUND` + 404 반환.
3. 존재하면 요청 Body의 전체 필드로 기존 데이터를 덮어쓰기(전체 교체) 후 수정된 채널을 반환한다.

**Path Parameter**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `channelId` | String | Y | 수정할 채널의 ID |

**Request Body**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `channelName` | String | Y | 변경할 채널 이름 |
| `channelGroup` | String | N | 변경할 채널 그룹 |

**Request Body 예시**

```json
{
  "channelName": "JTBC2",
  "channelGroup": "케이블"
}
```

**Response Body 예시 (성공)**

```json
{
  "success": true,
  "data": {
    "channelId": "CH-003",
    "channelName": "JTBC2",
    "channelGroup": "케이블"
  },
  "message": "채널이 수정되었습니다",
  "error": null,
  "timestamp": "2026-05-11T09:00:00"
}
```

**Response Body 예시 (실패 — 존재하지 않는 ID)**

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": {
    "code": "CHANNEL_NOT_FOUND",
    "message": "채널을 찾을 수 없습니다"
  },
  "timestamp": "2026-05-11T09:00:00"
}
```

**HTTP 상태코드**

| 코드 | 의미 |
|------|------|
| `200` | 수정 성공 |
| `404` | 해당 channelId의 채널이 존재하지 않음 |

---

#### DELETE /api/v1/channels/{channelId} — 채널 삭제

**설명:** 지정한 채널을 삭제한다.

**처리 흐름:**
1. Path에서 `channelId`를 추출한다.
2. DB에서 해당 ID 존재 여부를 확인한다. 없으면 `CHANNEL_NOT_FOUND` + 404 반환.
3. 존재하면 DB에서 삭제 후 `"SUCCESS"` 메시지를 반환한다.

**Path Parameter**

| 파라미터 | 타입 | 필수 | 설명 |
|----------|------|------|------|
| `channelId` | String | Y | 삭제할 채널의 ID |

**Response Body 예시 (성공)**

```json
{
  "success": true,
  "data": "SUCCESS",
  "message": "채널이 삭제되었습니다",
  "error": null,
  "timestamp": "2026-05-11T09:00:00"
}
```

**Response Body 예시 (실패 — 존재하지 않는 ID)**

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": {
    "code": "CHANNEL_NOT_FOUND",
    "message": "채널을 찾을 수 없습니다"
  },
  "timestamp": "2026-05-11T09:00:00"
}
```

**HTTP 상태코드**

| 코드 | 의미 |
|------|------|
| `200` | 삭제 성공 |
| `404` | 해당 channelId의 채널이 존재하지 않음 |

---

## 6. 공통 오류 코드

### 오류 응답 구조 예시

```json
{
  "success": false,
  "data": null,
  "message": null,
  "error": {
    "code": "CHANNEL_NOT_FOUND",
    "message": "채널을 찾을 수 없습니다"
  },
  "timestamp": "2026-05-11T09:00:00"
}
```

### 오류 코드 목록

| 코드 | HTTP | 발생 위치 | 설명 | 조치 방법 |
|------|------|-----------|------|-----------|
| `PROGRAM_NOT_FOUND` | 404 | 프로그램 API | 해당 ID의 프로그램이 존재하지 않음 | programId 값 확인 |
| `CHANNEL_NOT_FOUND` | 404 | 채널 API | 해당 ID의 채널이 존재하지 않음 | channelId 값 확인 |
| `DUPLICATE_CHANNEL` | 409 | POST /channels | 동일한 channelId가 이미 등록되어 있음 | 다른 channelId 사용 또는 기존 채널 수정 |
| `INVALID_DATE_FORMAT` | 400 | GET /channels/{id}/schedules | date 파라미터 형식이 잘못됨 | `YYYY-MM-DD` 형식으로 재요청 (예: `2026-05-11`) |

### HTTP 상태코드 요약

| 코드 | 의미 | 설명 |
|------|------|------|
| `200` | OK | 요청 성공 |
| `201` | Created | 리소스 생성 성공 |
| `400` | Bad Request | 요청 파라미터 오류 (형식 불일치 등) |
| `404` | Not Found | 요청한 리소스가 존재하지 않음 |
| `409` | Conflict | 리소스 중복 (이미 존재하는 ID) |
| `500` | Internal Server Error | 서버 내부 오류 |

---

## 7. 변경 이력

| 버전 | 일자 | 변경 내용 |
|------|------|-----------|
| 1.2.0 | 2026-05-11 | API 상세 명세를 컨트롤러별 단일 섹션으로 재구성, 전체 엔드포인트 요약 테이블 추가 |
| 1.1.0 | 2026-05-11 | 비기능적 요구사항(성능·보안·확장성) 섹션 추가, 각 API에 처리 흐름 기술, GET programId 타입 불일치 주의 문구 추가 |
| 1.0.0 | 2026-05-11 | 최초 작성 — 프로그램·채널 CRUD 및 채널 편성표 조회 API 명세 |
