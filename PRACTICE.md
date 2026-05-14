# TV Schedule REST API 실습 가이드

## 실습 목표
Spring Boot에서 REST API Controller를 직접 구현하며 다음 개념을 익힙니다:
- HTTP Method와 Spring 어노테이션 매핑 (`@GetMapping`, `@PostMapping`, `@PutMapping`, `@DeleteMapping`)
- 경로 변수(`@PathVariable`)와 쿼리 파라미터(`@RequestParam`)
- HTTP 상태 코드 (200, 201, 404, 409)
- 응답 객체(`ResponseEntity`) 구성

## 사전 준비
- 프로젝트 빌드: `mvn clean compile`
- 서버 실행: `mvn spring-boot:run`
- 테스트 도구: Postman 또는 cURL

---

## 실습 1: PracChannelController.java

> 파일 위치: `src/main/java/com/example/tvschedule/controller/PracChannelController.java`

완성된 `ChannelController.java`를 참고하여 TODO를 채워주세요.

### TODO 1 — API 기본 경로 설정
`@RequestMapping`에 채널 API의 기본 경로를 작성하세요.

**힌트**: README의 "채널 관리" 섹션에서 URL 패턴을 확인하세요.

---

### TODO 2 — 전체 목록 조회 어노테이션
전체 채널 목록을 조회하는 HTTP Method 어노테이션을 작성하세요.

**힌트**: 데이터를 "가져오는" 행위에 해당하는 HTTP Method는?

---

### TODO 3 — 생성 어노테이션
새로운 채널을 생성하는 HTTP Method 어노테이션을 작성하세요.

**힌트**: 새로운 리소스를 "만드는" 행위에 해당하는 HTTP Method는?

---

### TODO 4 — 삭제 어노테이션 (경로 포함)
채널을 삭제하는 HTTP Method 어노테이션을 작성하세요. 경로에 `channelId`를 포함해야 합니다.

**힌트**: `@XxxMapping("/{변수명}")` 형태로 작성합니다.

---

### TODO 5 — 경로 변수 어노테이션
URL 경로에서 `channelId` 값을 받아오는 파라미터 어노테이션을 작성하세요.

**힌트**: `/api/v1/channels/9` 요청 시 `9`을 변수로 받으려면?

---

### TODO 6 — 쿼리 파라미터 어노테이션
`date` 파라미터를 선택적(필수 아님)으로 받는 어노테이션을 작성하세요.

**힌트**: `?date=2026-05-11`처럼 전달되는 값을 받으며, 필수가 아닌 경우 `required = false` 옵션을 사용합니다.

---

### TODO 7 — 수정 경로 작성
특정 채널을 수정하기 위한 경로를 작성하세요.

**힌트**: `@PathVariable String channelId`와 매핑되는 경로입니다.

---

### TODO 8 — HTTP 상태 코드
리소스를 찾을 수 없을 때 반환해야 하는 HTTP 상태 코드를 작성하세요.

**힌트**: "Not Found"에 해당하는 숫자 코드는?

---

### 검증 방법 (Postman / cURL)

```bash
# 전체 채널 조회
curl -X GET "http://localhost:8282/api/v1/channels"

# 특정 채널 조회
curl -X GET "http://localhost:8282/api/v1/channels/9"

# 채널 생성
curl -X POST "http://localhost:8282/api/v1/channels" \
  -H "Content-Type: application/json" \
  -d '{"channelId": "test", "channelName": "test_channel", "channelGroup": "음악/오락"}'

# 채널 수정
curl -X PUT "http://localhost:8282/api/v1/channels/test" \
  -H "Content-Type: application/json" \
  -d '{"channelName": "test_channel_2", "channelGroup": "스포츠/취미"}'

# 채널 삭제
curl -X DELETE "http://localhost:8282/api/v1/channels/test"

# 채널별 편성표 조회
curl -X GET "http://localhost:8282/api/v1/channels/9/schedules?date=2026-05-11"
```

---

## 실습 2: PracProgramController.java

> 파일 위치: `src/main/java/com/example/tvschedule/controller/PracProgramController.java`

이미 완성된 `createProgram`, `updateProgram` 메서드의 패턴을 참고하여 나머지 TODO를 구현하세요.

### TODO — getAllPrograms 구현
전체 프로그램 목록을 조회하여 반환하세요.

**요구사항**:
- `programRepository.findAll()`로 목록 조회
- `ApiResponse.success()`로 감싸서 반환
- 메시지: `"프로그램 목록 조회 성공"`

**참고**: 같은 파일의 `createProgram` 메서드에서 `ResponseEntity`와 `ApiResponse` 사용법을 확인하세요.

---

### TODO — getProgramById 구현
`programId`로 특정 프로그램을 조회하세요.

**요구사항**:
- `programRepository.findById(programId)`로 조회 (반환 타입: `Optional<Program>`)
- 존재하면: `200 OK` + `ApiResponse.success(program, "프로그램 조회 성공")`
- 존재하지 않으면: `404` + `ApiResponse.error("PROGRAM_NOT_FOUND", "프로그램을 찾을 수 없습니다")`

**참고**: 실습 1의 `getChannelById`에서 `Optional` 처리 패턴을 확인하세요.

---

### TODO — deleteProgram 구현
프로그램을 삭제하세요.

**요구사항**:
1. `programRepository.existsById(programId)`로 존재 여부 확인
2. 존재하지 않으면: `404` + `ApiResponse.error("PROGRAM_NOT_FOUND", "프로그램을 찾을 수 없습니다")`
3. 존재하면: `programRepository.deleteById(programId)` 호출 후 `200 OK` + `ApiResponse.success("SUCCESS", "프로그램이 삭제되었습니다")`

**참고**: 같은 파일의 `updateProgram` 메서드에서 존재 확인 → 처리 패턴을 확인하세요.

---

### 검증 방법 (Postman / cURL)

```bash
# 전체 프로그램 조회
curl -X GET "http://localhost:8282/api/v1/programs"

# 특정 프로그램 조회
curl -X GET "http://localhost:8282/api/v1/programs/1"

# 프로그램 생성
curl -X POST "http://localhost:8282/api/v1/programs" \
  -H "Content-Type: application/json" \
  -d '{"programName": "새 프로그램", "genre": "예능", "description": "테스트 프로그램"}'

# 프로그램 삭제
curl -X DELETE "http://localhost:8282/api/v1/programs/1"

# 존재하지 않는 프로그램 조회 (404 확인)
curl -X GET "http://localhost:8282/api/v1/programs/9999"
```

---

## 정답 확인
- 채널: `ChannelController.java` 참고
- 프로그램: `ProgramController.java` 참고
