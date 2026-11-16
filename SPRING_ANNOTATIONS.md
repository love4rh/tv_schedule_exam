# 이 프로젝트에서 사용된 Spring 애노테이션 설명

TV 편성표 REST API 프로젝트(Spring Boot 3.1, Java 21, Spring JDBC)에서 실제로 사용한
애노테이션을 계층별로 정리한 교육 자료입니다. 각 항목은 **역할 → 이 프로젝트에서의 실제 사용 예시**
순서로 설명합니다.

> 애노테이션(Annotation)이란? 코드(클래스/메서드/필드/파라미터)에 붙이는 "표식"으로,
> 프레임워크가 그 표식을 읽어 자동으로 설정·처리를 해 줍니다. 즉, 개발자가 일일이 설정 코드를
> 작성하는 대신 "이건 이런 용도야"라고 선언만 하면 Spring 이 알아서 동작합니다.

## 이 프로젝트에서 사용한 애노테이션 한눈에 보기

| 분류 | 애노테이션 | 한 줄 요약 |
|------|-----------|-----------|
| 애플리케이션 | `@SpringBootApplication` | Spring Boot 앱의 시작점 선언 |
| 빈 등록(스테레오타입) | `@RestController` | REST API 컨트롤러로 등록 |
| | `@Service` | 비즈니스 로직 계층 빈으로 등록 |
| | `@Repository` | 데이터 접근 계층 빈으로 등록 |
| 의존성 주입 | `@Autowired` | 필요한 빈을 자동으로 주입 |
| 요청 경로 매핑 | `@RequestMapping` | 클래스/메서드의 공통 경로 지정 |
| | `@GetMapping` | HTTP GET 요청 매핑 |
| | `@PostMapping` | HTTP POST 요청 매핑 |
| | `@DeleteMapping` | HTTP DELETE 요청 매핑 |
| 요청 데이터 바인딩 | `@PathVariable` | 경로 변수(`{...}`) → 메서드 파라미터 |
| | `@RequestParam` | 쿼리 파라미터(`?a=b`) → 메서드 파라미터 |
| | `@RequestBody` | 요청 본문(JSON) → 객체 |
| 기타 | `@CrossOrigin` | CORS(교차 출처 요청) 허용 |

> 아래 본문에는 코드에 직접 작성하지는 않지만, 위 애노테이션을 설명할 때 함께 언급되는
> **관련(내부/상위) 애노테이션**도 별도 섹션(7장)에서 보충 설명합니다.

---

## 1. 애플리케이션 시작점

### `@SpringBootApplication`
- **역할**: Spring Boot 애플리케이션의 진입점(main 클래스)에 붙입니다. 내부적으로 세 가지를 합쳐 놓은 애노테이션입니다.
  - `@SpringBootConfiguration` : 설정 클래스
  - `@EnableAutoConfiguration` : 자동 설정 활성화 (DataSource, 내장 Tomcat 등을 자동 구성)
  - `@ComponentScan` : 하위 패키지의 `@Component`/`@Service`/`@Repository`/`@RestController` 등을 자동 탐색·등록
- **이 프로젝트 예시** (`TvScheduleApplication.java`):

  ```java
  @SpringBootApplication
  public class TvScheduleApplication {
      public static void main(String[] args) {
          SpringApplication.run(TvScheduleApplication.class, args);
      }
  }
  ```

  이 한 줄 덕분에 `com.example.tvschedule` 하위의 모든 컨트롤러/서비스/리포지토리가
  자동으로 스캔되어 빈으로 등록됩니다.

---

## 2. 빈(Bean) 등록 — 스테레오타입 애노테이션

Spring 은 애플리케이션을 "빈(Bean)"이라는 객체들의 모음으로 관리합니다.
아래 애노테이션이 붙은 클래스는 Spring 컨테이너가 생성·관리하는 빈이 됩니다.
(계층을 구분하는 이름이 다를 뿐, 모두 `@Component` 의 특수화된 형태입니다.)

### 잠깐 — "스테레오타입(stereotype)"이란?

**한 문장 정의**: "이 클래스를 **빈으로 등록해줘**" + "이 클래스의 **역할(계층)은 이거야**"
를 한 번에 알려 주는 표식.

**사전적 의미 / 어원** (용어가 어디서 왔는지 알면 이해가 쉽습니다)
- 그리스어 **stereo-**(στερεός, "단단한·고체의·입체의") + **-type**(τύπος, "틀·주형·활자")
  의 합성어 → 어원 그대로는 **"단단하게 굳어진 틀"**.
- 원래는 인쇄 용어로, 조판한 활자를 통째로 떠서 만든 **고정된 금속 인쇄판(연판)** 을 뜻했습니다.
  같은 페이지를 반복해 찍기 위한 "고정 틀"이죠.
- 여기서 비유가 확장되어 일상어로는 **"판에 박힌 고정관념"**, 소프트웨어에서는
  **"미리 정해진 전형적 역할의 틀/범주"** 라는 의미로 쓰입니다.

**비유로 이해하기**: 회사 **사원증**과 같습니다.
- 모든 직원은 사원증(= `@Component`)이 있어야 사내 시스템(= Spring 컨테이너)에 등록됩니다.
- 그런데 사원증에는 **부서명**이 찍혀 있습니다: `@RestController`(요청 창구 부서),
  `@Service`(업무 처리 부서), `@Repository`(자료/DB 부서).
- **셋 다 "등록된 직원"이라는 본질은 같고, 부서(역할) 이름만 다릅니다.**

**핵심 3가지**
1. **공통 효과**: 네 가지(`@Component`·`@RestController`·`@Service`·`@Repository`) 모두
   "Spring 이 생성·관리하는 빈이 된다"는 점은 동일합니다.
   (`@RestController`/`@Service`/`@Repository` 는 내부에 `@Component` 를 품고 있음)
2. **차이는 '의미'**: 그런데도 나눠 쓰는 이유는, **코드만 봐도 역할(계층)이 드러나게** 하기
   위해서입니다. 애노테이션 이름이 곧 문서 역할을 합니다.
3. **역할별 부가 기능**: `@Repository` 는 DB 예외를 Spring 표준 예외로 변환,
   `@RestController` 는 `@ResponseBody` 포함(JSON 직렬화) 같은 기능이 추가로 딸려옵니다.

> 한 줄 요약: **스테레오타입 = "빈으로 등록" + "내 역할은 이거야" 를 한 번에 선언하는 애노테이션.**

### `@RestController`
- **역할**: 이 클래스가 REST API 요청을 처리하는 컨트롤러임을 선언합니다.
  `@Controller` + `@ResponseBody` 가 합쳐진 것으로, 메서드 반환값이 뷰(HTML)가 아니라
  **데이터(JSON) 그 자체로 응답 본문에 직렬화**됩니다.
- **이 프로젝트 예시** (`ChannelController.java`):

  ```java
  @RestController
  @RequestMapping("/api/v1/channels")
  public class ChannelController { ... }
  ```

  메서드가 `ResponseEntity<ApiResponse<...>>` 를 반환하면 Spring 이 자동으로 JSON 으로 변환합니다.

### `@Service`
- **역할**: 비즈니스 로직을 담당하는 서비스 계층 빈으로 등록합니다.
  동작은 `@Component` 와 같지만, "이 클래스는 서비스 계층"이라는 의미를 코드에 드러냅니다.
- **이 프로젝트 예시** (`ChannelService.java`):

  ```java
  @Service
  public class ChannelService {
      @Autowired
      private ChannelRepository channelRepository;

      public List<Channel> search(String keyword) {
          return channelRepository.search(keyword);
      }
  }
  ```

### `@Repository`
- **역할**: 데이터 접근(DB) 계층 빈으로 등록합니다. 추가로 **데이터 접근 예외를
  Spring 의 표준 예외(`DataAccessException`)로 변환**해 주는 기능이 있습니다.
- **이 프로젝트 예시** (`ChannelRepository.java`):

  ```java
  @Repository
  public class ChannelRepository {
      @Autowired
      private JdbcTemplate jdbcTemplate;

      public List<Channel> findAll() {
          String sql = "SELECT channel_id, channel_name, channel_group FROM Channel";
          return jdbcTemplate.query(sql, channelRowMapper);
      }
  }
  ```

> 계층 구조: **Controller(요청/응답) → Service(비즈니스 로직) → Repository(DB 접근)**
> 이 프로젝트도 이 3계층 구조를 따릅니다.

---

## 3. 의존성 주입(DI)

### `@Autowired`
- **역할**: Spring 컨테이너가 관리하는 빈을 **자동으로 찾아서 주입**합니다.
  개발자가 `new` 로 직접 객체를 만들지 않아도, 타입에 맞는 빈을 Spring 이 넣어 줍니다.
- **이 프로젝트 예시** (`ChannelController.java`):

  ```java
  @Autowired
  private ChannelService channelService;

  @Autowired
  private ScheduleRepository scheduleRepository;
  ```

  → `channelService`, `scheduleRepository` 는 Spring 이 등록해 둔 빈으로 자동 채워집니다.

> 참고: 생성자가 하나뿐이면 `@Autowired` 를 생략해도 생성자 주입이 동작합니다.
> 이 프로젝트는 필드 주입 방식(`@Autowired` 를 필드에 붙임)을 사용합니다.

---

## 4. 요청 경로 매핑

### `@RequestMapping`
- **역할**: 클래스나 메서드에 **공통 요청 경로(URL prefix)** 를 지정합니다.
  보통 클래스 레벨에 붙여 해당 컨트롤러의 기본 경로를 정합니다.
- **이 프로젝트 예시**:

  ```java
  @RequestMapping("/api/v1/channels")                        // ChannelController
  @RequestMapping("/api/v1/schedules")                       // ScheduleController
  @RequestMapping("/api/v1/users/{userId}/favorite-channels") // FavoriteChannelController
  ```

  클래스에 선언한 경로 뒤에, 각 메서드의 매핑 경로가 이어 붙습니다.

### `@GetMapping`
- **역할**: HTTP **GET** 요청(조회)을 메서드에 매핑합니다. `@RequestMapping(method = GET)` 의 축약형.
- **이 프로젝트 예시** (`ChannelController.java`):

  ```java
  @GetMapping                      // GET /api/v1/channels
  public ResponseEntity<...> getAllChannels() { ... }

  @GetMapping("/search")           // GET /api/v1/channels/search
  public ResponseEntity<...> searchChannels(...) { ... }

  @GetMapping("/{channelId}")      // GET /api/v1/channels/{channelId}
  public ResponseEntity<...> getChannelById(...) { ... }
  ```

### `@PostMapping`
- **역할**: HTTP **POST** 요청(생성)을 매핑합니다.
- **이 프로젝트 예시** (`FavoriteChannelController.java` — 즐겨찾기 추가):

  ```java
  @PostMapping                     // POST /api/v1/users/{userId}/favorite-channels
  public ResponseEntity<ApiResponse<String>> addFavoriteChannel(
          @PathVariable String userId, @RequestBody Map<String, String> request) { ... }
  ```

### `@DeleteMapping`
- **역할**: HTTP **DELETE** 요청(삭제)을 매핑합니다.
- **이 프로젝트 예시** (`FavoriteChannelController.java` — 즐겨찾기 삭제):

  ```java
  @DeleteMapping("/{channelId}")   // DELETE /api/v1/users/{userId}/favorite-channels/{channelId}
  public ResponseEntity<ApiResponse<String>> removeFavoriteChannel(
          @PathVariable String userId, @PathVariable String channelId) { ... }
  ```

> HTTP 메서드별 매핑 애노테이션: `@GetMapping`(조회), `@PostMapping`(생성),
> `@PutMapping`(전체 수정), `@PatchMapping`(부분 수정), `@DeleteMapping`(삭제).
> 이 프로젝트는 교육용으로 조회(GET)와 즐겨찾기 추가/삭제(POST/DELETE)만 제공합니다.

---

## 5. 요청 데이터 바인딩

클라이언트가 보낸 값을 메서드 파라미터로 받아 오는 애노테이션입니다.

### `@PathVariable`
- **역할**: URL 경로 안의 변수(`{...}`) 값을 메서드 파라미터로 바인딩합니다.
- **이 프로젝트 예시** (`ChannelController.java`):

  ```java
  @GetMapping("/{channelId}")
  public ResponseEntity<...> getChannelById(@PathVariable String channelId) { ... }
  ```

  요청이 `GET /api/v1/channels/KBS1` 이면 `channelId = "KBS1"` 로 들어옵니다.

- **경로 변수 2개 예시** (`FavoriteChannelController.java`):

  ```java
  @DeleteMapping("/{channelId}")
  public ResponseEntity<...> removeFavoriteChannel(
          @PathVariable String userId,     // 클래스 경로의 {userId}
          @PathVariable String channelId) { ... }  // 메서드 경로의 {channelId}
  ```

### `@RequestParam`
- **역할**: 쿼리 스트링(`?key=value`)의 값을 파라미터로 바인딩합니다.
  옵션으로 `required`(필수 여부), `defaultValue`(기본값)를 지정할 수 있습니다.
- **이 프로젝트 예시** (`ChannelController.java` — 채널 검색):

  ```java
  @GetMapping("/search")
  public ResponseEntity<...> searchChannels(
          @RequestParam String keyword,                                  // 필수
          @RequestParam(required = false, defaultValue = "false") boolean includeGroup) { ... }
  ```

  - `GET /api/v1/channels/search?keyword=KBS` → `keyword="KBS"`, `includeGroup=false`
  - `GET /api/v1/channels/search?keyword=스포츠&includeGroup=true` → 그룹명까지 검색

- **선택 파라미터 예시** (`ChannelController.java` — 채널별 편성표):

  ```java
  @GetMapping("/{channelId}/schedules")
  public ResponseEntity<...> getChannelSchedules(
          @PathVariable String channelId,
          @RequestParam(required = false) String date) { ... }   // date 생략 가능
  ```

### `@RequestBody`
- **역할**: HTTP 요청 **본문(JSON)** 을 Java 객체(또는 Map)로 변환해 바인딩합니다.
  주로 POST/PUT 처럼 데이터를 보내는 요청에서 사용합니다.
- **이 프로젝트 예시** (`FavoriteChannelController.java` — 즐겨찾기 추가):

  ```java
  @PostMapping
  public ResponseEntity<...> addFavoriteChannel(
          @PathVariable String userId,
          @RequestBody Map<String, String> request) {
      String channelId = request.get("channelId");
      ...
  }
  ```

  요청 본문이 `{"channelId": "MBC"}` 이면 `request.get("channelId")` 가 `"MBC"` 가 됩니다.

> 정리: 값이 **어디에** 실려 오는지에 따라 애노테이션이 달라집니다.
> - 경로 `/users/user123` → `@PathVariable`
> - 쿼리 `?date=2026-11-10` → `@RequestParam`
> - 본문 `{ "channelId": "MBC" }` → `@RequestBody`

---

## 6. CORS 설정

### `@CrossOrigin`
- **역할**: 다른 출처(Origin, 즉 다른 도메인/포트)에서 오는 요청을 허용합니다(CORS).
  브라우저는 보안상 다른 출처로의 AJAX 요청을 기본 차단하는데, 이 애노테이션으로 허용합니다.
- **이 프로젝트 예시** (모든 컨트롤러):

  ```java
  @RestController
  @RequestMapping("/api/v1/channels")
  @CrossOrigin(origins = "*")   // 모든 출처 허용
  public class ChannelController { ... }
  ```

  프론트엔드(`index.html`, `script.js`)가 API 를 직접 호출할 수 있도록 모든 출처를 허용했습니다.

> 주의: `origins = "*"`(전체 허용)는 학습·개발 편의를 위한 설정입니다.
> 실제 서비스에서는 허용할 도메인을 명시적으로 제한하는 것이 안전합니다.

---

## 7. 본문 설명에 등장하는 관련 애노테이션 (코드에는 직접 없음)

앞선 설명에서 "~의 합성/특수화"라고 언급한 애노테이션들입니다. 이 프로젝트 소스에
직접 작성하지는 않지만, 우리가 쓰는 애노테이션의 **내부 구성 요소**이거나 **상위 개념**이라
이해해 두면 동작 원리가 분명해집니다.

### `@Component`
- **역할**: Spring 이 관리하는 빈으로 등록되는 **가장 기본적인** 스테레오타입 애노테이션.
- **관계**: `@RestController`, `@Service`, `@Repository` 는 모두 내부적으로 `@Component` 를
  품고 있는 **특수화(구체화)** 형태입니다. 즉 세 애노테이션 중 무엇을 붙이든 "빈으로 등록"되는
  효과는 동일하고, 계층(역할)을 이름으로 구분해 줄 뿐입니다.
- **비고**: 이 프로젝트는 계층이 명확하므로 `@Component` 대신 역할에 맞는
  `@Service`/`@Repository`/`@RestController` 를 사용했습니다.

### `@Controller`
- **역할**: 전통적인 Spring MVC 컨트롤러로 등록합니다. 기본적으로 메서드 반환값을
  **뷰 이름(HTML 템플릿)** 으로 해석합니다.
- **관계**: `@RestController` = `@Controller` + `@ResponseBody`.
  이 프로젝트는 HTML 뷰가 아니라 JSON 데이터를 반환하므로 `@Controller` 대신
  `@RestController` 를 사용합니다.

### `@ResponseBody`
- **역할**: 메서드 반환값을 뷰가 아니라 **HTTP 응답 본문(JSON 등)으로 직접 직렬화**합니다.
- **관계**: `@RestController` 를 쓰면 클래스의 모든 메서드에 `@ResponseBody` 가 적용된 것과
  같습니다. 그래서 이 프로젝트의 컨트롤러 메서드는 반환한 `ResponseEntity<ApiResponse<...>>`
  가 자동으로 JSON 으로 변환됩니다.

### `@SpringBootApplication` 을 구성하는 3종
`@SpringBootApplication` 은 아래 세 애노테이션을 하나로 합친 것입니다(1장 참고).

- **`@SpringBootConfiguration`**
  - 이 클래스가 Spring 설정(Configuration) 클래스임을 나타냅니다.
    (일반 Spring 의 `@Configuration` 을 Boot 전용으로 특수화한 것)
- **`@EnableAutoConfiguration`**
  - 클래스패스(의존성)와 설정을 보고 **필요한 빈을 자동 구성**합니다.
    예: `spring-boot-starter-web` 이 있으면 내장 Tomcat·JSON 변환기를, JDBC 의존성과
    `application.yml` 의 datasource 설정이 있으면 `DataSource`·`JdbcTemplate` 을 자동 구성.
- **`@ComponentScan`**
  - 현재 클래스가 위치한 패키지(`com.example.tvschedule`)와 그 하위를 스캔하여
    `@Component`(및 `@Service`/`@Repository`/`@RestController`)가 붙은 클래스를 찾아
    빈으로 등록합니다. 이 프로젝트의 모든 컨트롤러/서비스/리포지토리가 자동 등록되는 이유입니다.

### HTTP 메서드 매핑 중 이 프로젝트에 없는 것
요청 매핑(4장)에서 언급했지만 현재 교육용 범위에서는 쓰지 않는 매핑입니다.

- **`@PutMapping`**: HTTP **PUT**(리소스 전체 수정) 매핑. `@RequestMapping(method = PUT)` 축약형.
- **`@PatchMapping`**: HTTP **PATCH**(리소스 부분 수정) 매핑.

> 이 프로젝트는 조회(GET)와 즐겨찾기 추가/삭제(POST/DELETE)만 제공하므로
> `@PutMapping`/`@PatchMapping` 은 사용하지 않습니다. 생성·수정·삭제 전반이 필요할 때
> 참고용으로 알아 두면 됩니다.

> 요약: 우리가 실제로 타이핑하는 애노테이션(`@RestController`, `@Service`, `@Repository`,
> `@SpringBootApplication`)은 위의 기본 애노테이션들을 **조합·특수화**한 "편의 애노테이션"입니다.
> 그래서 한 줄만 붙여도 여러 동작이 한꺼번에 적용됩니다.

---

## 8. 전체 흐름으로 이해하기

채널 검색 요청 `GET /api/v1/channels/search?keyword=KBS` 하나가 처리되는 과정:

```
[브라우저]  GET /api/v1/channels/search?keyword=KBS
     │
     ▼
@RestController + @RequestMapping("/api/v1/channels")  ← ChannelController 로 라우팅
@CrossOrigin(origins = "*")                            ← 교차 출처 요청 허용
     │
     ▼
@GetMapping("/search")                                 ← searchChannels() 메서드 선택
@RequestParam String keyword                           ← keyword="KBS" 바인딩
     │
     ▼
@Autowired ChannelService (@Service)                   ← 비즈니스 로직 호출
     │
     ▼
@Autowired ChannelRepository (@Repository)             ← DB 조회 (JdbcTemplate)
     │                                                    조회 결과 → Channel 객체로 매핑
     ▼
ResponseEntity<ApiResponse<List<Channel>>> 반환          ← @RestController 가 JSON 으로 직렬화
     │
     ▼
[브라우저]  { "success": true, "data": [ ... ] }
```

이처럼 애노테이션들이 각 계층에서 역할을 나눠 맡아, 개발자는 **비즈니스 로직과 쿼리에만 집중**하고
라우팅·직렬화·DI·CORS 같은 공통 처리는 Spring 이 자동으로 담당합니다.
