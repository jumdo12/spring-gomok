# Spring Gomok - 실시간 멀티플레이어 오목 게임

> Spring Boot 기반 실시간 웹 오목 게임 플랫폼.
> 두 플레이어가 18x18 보드에서 실시간으로 대국하며, SSE 기반 실시간 통신과 블루/그린 무중단 배포를 적용한 프로덕션 수준의 서비스입니다.

**[gomoku.jumdo12.cloud](https://gomoku.jumdo12.cloud)**

---

## 목차

- [프로젝트 소개](#프로젝트-소개)
- [기술 스택](#기술-스택)
- [시스템 아키텍처](#시스템-아키텍처)
- [주요 기능](#주요-기능)
- [API 명세](#api-명세)
- [데이터 흐름](#데이터-흐름)
- [패키지 구조](#패키지-구조)
- [CI/CD 파이프라인](#cicd-파이프라인)
- [인프라 구성](#인프라-구성)
- [설계 결정](#설계-결정)

---

## 프로젝트 소개

두 명이 실시간으로 오목을 즐길 수 있는 웹 게임 서비스입니다. 회원가입/로그인 후 게임방을 생성하거나 입장하여 대국을 진행하고, 대국이 끝나면 기보(replay)를 다시 볼 수 있습니다.

**핵심 구현 포인트**
- WebSocket 대신 **SSE(Server-Sent Events)** 를 활용한 단방향 실시간 이벤트 스트림
- 게임 세션을 **Redis**에 캐싱하여 빠른 상태 조회와 DB 부하 분리
- **블루/그린 배포**로 배포 중에도 서비스 중단 없이 운영
- **Prometheus + Grafana + Loki** 기반 모니터링 & 로그 수집 파이프라인 구축

---

## 기술 스택

| 분류 | 기술 |
|------|------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 3.5.7, Spring Data JPA, Spring Security |
| **Template Engine** | Thymeleaf |
| **Database** | PostgreSQL (Supabase), H2 (로컬 개발) |
| **Cache** | Redis 7 (TTL 3시간) |
| **Reverse Proxy** | Caddy (자동 TLS/SSL) |
| **Infra** | AWS EC2, Docker, Docker Compose |
| **CI/CD** | GitHub Actions (자체 호스팅 runner) |
| **Monitoring** | Prometheus, Grafana, Grafana Loki, Promtail |
| **API Docs** | SpringDoc OpenAPI (Swagger UI) |
| **Build Tool** | Gradle |

---

## 시스템 아키텍처

```
┌─────────────────────────────────────────────────────────────────┐
│                          Client (Browser)                        │
└───────────────────────────────┬─────────────────────────────────┘
                                │ HTTPS (443)
                                ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Caddy (Reverse Proxy)                      │
│  - 자동 TLS/SSL 인증서 발급 & 갱신                                │
│  - /actuator/* 차단 (403)                                        │
│  - Blue/Green upstream 파일 기반 동적 라우팅                       │
└──────────────┬────────────────────────────────────────┬─────────┘
               │                                        │
               ▼                                        ▼
    ┌─────────────────┐                      ┌─────────────────┐
    │  App (Blue)     │   Blue/Green 전환      │  App (Green)    │
    │  :8080          │ ◄──────────────────► │  :8081          │
    │  Spring Boot    │                      │  Spring Boot    │
    └────────┬────────┘                      └────────┬────────┘
             │                                        │
             └──────────────────┬─────────────────────┘
                                │
               ┌────────────────┼────────────────┐
               │                │                │
               ▼                ▼                ▼
    ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐
    │  Redis 7     │  │  Supabase    │  │  Prometheus          │
    │  게임 세션 캐싱  │  │  PostgreSQL  │  │  + Grafana           │
    │  TTL: 3시간  │  │  유저/기보 저장 │  │  + Loki + Promtail   │
    └──────────────┘  └──────────────┘  └──────────────────────┘
```

### 레이어드 아키텍처 (패키지 구조 기반)

```
presentation (Controller, DTO, Resolver)
      │
      ▼
application (Service, Application DTO)
      │
      ▼
domain (Entity, Domain Logic, Repository Interface)
      │
      ▼
infra (Redis, SSE, JPA Repository 구현)
```

---

## 주요 기능

### 유저 인증
- 회원가입 / 로그인 / 로그아웃
- HTTP 세션 기반 인증 (`SessionProvider`)
- BCrypt 비밀번호 암호화
- 커스텀 `@AuthUser` 어노테이션 + `ArgumentResolver`로 컨트롤러에서 인증 유저 주입

### 게임방 관리
- 게임방 생성 / 목록 조회 / 입장 / 퇴장
- 방 상태 머신: `WAITING → READY → PLAYING → FINISHED`
- 호스트 전용 돌 색깔 변경 (`READY` 상태에서만)
- 채팅 (SSE 이벤트로 실시간 전달)

### 오목 게임 로직
- 18x18 보드
- 착수 유효성 검증 (범위, 이미 놓인 칸, 차례)
- 4방향(가로/세로/대각선 2개) 5연속 승리 판정
- 흑/백 교대 진행

### 실시간 통신 (SSE)
- `GET /api/rooms/{roomId}/subscribe` 구독
- 이벤트 종류: `move` (착수), `room-update` (방 상태/채팅 변경)
- `ConcurrentHashMap` 기반 `SseEmitters`로 멀티 스레드 안전하게 관리

### 기보 & 리플레이
- 게임 결과와 착수 순서 전체를 PostgreSQL에 영구 저장
- 유저별 게임 목록 조회
- 특정 게임의 착수 순서 전체 조회 (리플레이용)
- `@Async` 비동기 처리로 게임 응답에 영향 없이 기록

---

## API 명세

### 유저

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/users/signup` | 회원가입 |
| POST | `/api/users/login` | 로그인 |
| POST | `/api/users/logout` | 로그아웃 |
| GET | `/api/users/me` | 현재 유저 정보 조회 |

### 게임방

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/rooms` | 방 생성 |
| GET | `/api/rooms/{roomId}` | 방 정보 조회 |
| GET | `/api/rooms/waitings` | 대기 중인 방 목록 |
| GET | `/api/rooms/{roomId}/subscribe` | SSE 구독 |
| POST | `/api/rooms/{roomId}/join` | 방 입장 |
| POST | `/api/rooms/{roomId}/leave` | 방 퇴장 |
| POST | `/api/rooms/{roomId}/start` | 게임 시작 |
| POST | `/api/rooms/{roomId}/switch-stone` | 돌 색 변경 (호스트 전용) |
| POST | `/api/rooms/{roomId}/chat` | 채팅 전송 |

### 게임 / 기보

| Method | URL | 설명 |
|--------|-----|------|
| POST | `/api/game/{roomId}/place` | 착수 |
| GET | `/api/games` | 내 게임 목록 |
| GET | `/api/games/{id}` | 게임 기보 (착수 순서 전체) |

---

## 데이터 흐름

### 착수 요청 흐름

```
Client
  │ POST /api/game/{roomId}/place { row: 8, col: 9 }
  ▼
GomokController (@AuthUser 검증)
  │
  ▼
GomokService
  ├─ Redis에서 GomokRoom 조회
  ├─ 착수 유효성 검증 (차례, 범위, 이미 놓인 칸)
  ├─ 돌 배치 & 5연속 승리 판정
  ├─ Redis 갱신
  └─ GomokHistoryService.placeGomokHistory() @Async ──► PostgreSQL 저장
  │
  ▼
SseEmitters.send("move", event)
  │
  ▼
상대 Client (SSE 이벤트 수신)
```

### 게임방 상태 머신

```
[방 생성]
    │
    ▼
 WAITING ──► (2번째 플레이어 입장) ──► READY
    │                                    │
    │ (호스트 퇴장 시 방 삭제)            │ (게임 시작)
    ▼                                    ▼
 (방 삭제)                            PLAYING
                                         │
                                         │ (5연속 완성)
                                         ▼
                                      FINISHED
```

---

## 패키지 구조

```
src/main/java/jumdo12/springgomok/
├── application/                  # 비즈니스 로직
│   ├── GomokService              # 착수 처리, 승리 판정
│   ├── GomokRoomService          # 방 생성/입장/퇴장/채팅
│   ├── GomokHistoryService       # 기보 저장 (@Async)
│   ├── UserService               # 회원가입/로그인
│   └── dto/                      # Application 계층 DTO
│
├── domain/                       # 도메인 모델
│   ├── User                      # 유저 JPA 엔티티
│   ├── GomokRoom                 # 게임방 (Redis 저장)
│   ├── Gomok                     # 게임 보드 & 승리 로직
│   ├── GomokHistory              # 기보 JPA 엔티티
│   ├── Participant               # 참가자 (돌 색 포함)
│   ├── PlaceResult               # 개별 착수 기록 JPA 엔티티
│   ├── Stone                     # Enum: BLACK, WHITE, EMPTY
│   ├── GomokRoomStatus           # Enum: WAITING, READY, PLAYING, FINISHED
│   ├── UserRepository            # JPA Repository
│   └── GomokHistoryRepository    # JPA Repository
│
├── infra/                        # 인프라 연동
│   ├── config/
│   │   ├── RedisConfig           # RedisTemplate 설정
│   │   └── SecurityConfig        # BCrypt 빈 설정
│   ├── redis/
│   │   └── GomokRoomRedisRepository  # Redis CRUD + TTL 관리
│   └── sse/
│       └── SseEmitters           # SSE 연결 관리 (ConcurrentHashMap)
│
├── presentation/                 # 프레젠테이션 계층
│   ├── GomokController           # 착수 API
│   ├── GomokRoomController       # 게임방 API
│   ├── GomokResultController     # 기보 API
│   ├── UserController            # 유저 API
│   ├── PageController            # Thymeleaf 페이지 라우팅
│   ├── GlobalExceptionHandler    # 전역 예외 처리
│   ├── resolver/
│   │   ├── LoginUserArgumentResolver  # @AuthUser 처리
│   │   └── AuthUser              # 커스텀 어노테이션
│   └── session/
│       └── SessionProvider       # HTTP 세션 관리
│
└── common/
    └── exception/
        ├── ErrorCode             # 에러 코드 Enum (HTTP 상태 + 메시지)
        ├── BusinessException     # 커스텀 예외
        └── ErrorResponse         # 에러 응답 DTO
```

---

## CI/CD 파이프라인

```
개발자 Push (master)
        │
        ▼
┌───────────────────────────────┐
│    CI (GitHub Actions)         │
│  1. Java 21 설정               │
│  2. Gradle 빌드 (테스트 스킵)   │
│  3. Docker 이미지 빌드          │
│  4. Docker Hub Push            │
│     jumdo12/server:gomok       │
└───────────────┬───────────────┘
                │ CI 완료 시 CD 트리거
                ▼
┌───────────────────────────────┐
│    CD (Self-hosted Runner)     │
│  1. Docker Hub Pull            │
│  2. 신규 컨테이너 기동          │
│     (Blue ↔ Green 교대)        │
│  3. Health Check 대기          │
│     /actuator/health 폴링      │
│     (최대 120회, 5초 간격)      │
│  4. Caddy upstream 파일 교체   │
│  5. Caddy 리로드               │
│  6. 구 컨테이너 종료            │
└───────────────────────────────┘
         Health Check 실패 시 자동 롤백
```

---

## 인프라 구성

### Docker Compose 서비스 구성

| 서비스 | 이미지 | 역할 |
|--------|--------|------|
| app-blue | jumdo12/server:gomok | Spring Boot 앱 (8080) |
| app-green | jumdo12/server:gomok | Spring Boot 앱 (8081) |
| caddy | caddy:latest | 리버스 프록시 + TLS |
| redis | redis:7-alpine | 게임 세션 캐시 |
| prometheus | prom/prometheus | 메트릭 수집 (7일 보존) |
| grafana | grafana/grafana | 대시보드 & 알림 |
| loki | grafana/loki | 로그 집계 |
| promtail | grafana/promtail | 로그 수집 에이전트 |

### 모니터링 구성

```
Spring Boot Actuator (/actuator/prometheus)
        │
        ▼
   Prometheus (메트릭 수집)
        │
        ▼
    Grafana (시각화 대시보드)

app 컨테이너 로그 (/app/logs/)
        │
        ▼
    Promtail (로그 수집)
        │
        ▼
      Loki (로그 저장)
        │
        ▼
    Grafana (로그 조회)
```

### 환경 구분

| 환경 | DB | DDL | 비고 |
|------|----|-----|------|
| local | H2 in-memory (PostgreSQL 모드) | create-drop | H2 Console 활성화 |
| prod | Supabase PostgreSQL | create | AWS EC2 배포 |

---

## 설계 결정

### WebSocket 대신 SSE 선택
서버 → 클라이언트 단방향 이벤트(착수 결과, 채팅)만 필요하므로, 연결 복잡도가 낮고 HTTP 위에서 동작하는 SSE를 선택했습니다. 클라이언트 → 서버 요청은 일반 REST API로 처리합니다.

### 게임 세션을 Redis에 저장
게임 중 착수마다 방 상태를 조회/갱신해야 하는데, 매번 RDB에 접근하면 불필요한 부하가 생깁니다. 게임 세션은 수명이 짧고(최대 3시간 TTL), 복잡한 관계 조인이 불필요하므로 Redis에 저장합니다. 기보(게임 결과)처럼 영구 보존이 필요한 데이터만 PostgreSQL에 저장합니다.

### 기보 저장 비동기 처리
착수 응답 속도에 영향을 주지 않기 위해 `@Async`로 기보 저장을 별도 스레드에서 처리합니다.

### 블루/그린 무중단 배포
롤링 배포 없이 두 컨테이너(blue/green)를 번갈아 전환하는 방식으로 배포 중 다운타임을 제거합니다. Health Check 실패 시 Caddy upstream을 변경하지 않아 자동으로 기존 버전이 유지됩니다.
