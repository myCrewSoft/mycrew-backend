# 🖥️ MyCrew Backend

> **MyCrewSoft** — 통합 그룹웨어 플랫폼 백엔드 레포지토리  
> Spring Boot 3.3 · Java 21 · MyBatis · Oracle DB · Redis

---

## 📌 프로젝트 개요

사내에 분산된 커뮤니케이션, 일정, 문서 공유, 예약, 회의, 메일 기능을 하나의 플랫폼으로 통합한 엔터프라이즈 그룹웨어입니다.  
직원·팀장·관리자 역할 기반(RBAC) 권한 체계 위에서 실시간 메신저, 화상회의, 전자결재, AI 회의록 자동 생성 등 업무 자동화 기능을 제공합니다.

---

## 👥 팀 구성

| 역할 | 이름 | 담당 도메인 |
|------|------|------------|
| PL | 임원호 | 전자결재, 프로젝트 관리, 게시판, 조직도 |
| DA | 노윤하 | DB 설계, ERD, 공통 테이블 |
| UA | 박비주 | UI/UX, 화면 설계 |
| AA | 한재훈 | 공통 아키텍처, 화상회의/회의, SSE 알림, 메신저, 대시보드, 통합검색, 업무, 일정, 회의실 예약, 공휴일 API, LLM 챗봇, 관리자 페이지 |

---

## 🛠️ 기술 스택

| 분류 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | Spring Boot 3.5.14 |
| Build | Maven |
| ORM | MyBatis |
| DB | Oracle DB (HikariCP) |
| Cache | Redis (Refresh Token) |
| Auth | Spring Security + JWT (Access/Refresh 이중 구조) |
| Docs | Springdoc OpenAPI (Swagger UI) |
| Mapping | MapStruct |
| AI | Spring AI + OpenAI Whisper + LM Studio (LLM) |
| Real-time | WebSocket/STOMP, SSE |
| Video | LiveKit Cloud |

---

## 📁 패키지 구조

```
src/main/java/com/mycrewsoft/app/
│
├── config/                        # Security, Redis, Swagger, WebMvc 설정
├── security/                      # JWT, Filter, Handler
├── validate/                      # 커스텀 검증 어노테이션
├── common/
│   ├── response/ApiResponse.java  # 공통 응답 래퍼
│   ├── exception/                 # ErrorCode, CustomException, GlobalExceptionHandler
│   ├── util/                      # StringUtil, DateUtil, FileUtil
│   └── interceptor/               # LoggingInterceptor
│
└── domain/
    ├── mtng/                      # 회의 (온라인/오프라인/하이브리드)
    ├── video/                     # 화상회의 (LiveKit, Whisper STT)
    ├── messenger/                 # 실시간 메신저 (WebSocket/STOMP)
    ├── notification/              # SSE 실시간 알림
    ├── dashboard/                 # 대시보드 위젯
    ├── search/                    # 통합 검색
    ├── schedule/                  # 일정 관리
    ├── task/                      # 업무 관리
    ├── confRm/                    # 회의실 예약
    ├── holiday/                   # 공휴일 API 연동
    ├── chatbot/                   # LLM 챗봇
    ├── employee/                  # 공통 사원조회
    ├── approval/                  # 전자결재 (PL 담당)
    ├── project/                   # 프로젝트 관리 (PL 담당)
    ├── board/                     # 게시판 (PL 담당)
    ├── attend/                    # 근태관리
    ├── mail/                      # 메일 연동 (Google OAuth)
    ├── drive/                     # 문서 관리
    └── admin/                     # 관리자 페이지
```

---

## ⚙️ 환경 설정

### 필수 환경 변수 (`.env` 파일, 루트에 생성)

```env
# Database
DB_URL=jdbc:oracle:thin:@localhost:1521:XE
DB_USERNAME=your_oracle_username
DB_PASSWORD=your_oracle_password

# JWT
JWT_SECRET=your_jwt_secret_key

# File Storage
LOCAL_STORAGE=your_storage_path

# Google OAuth (메일 연동)
GOOGLE_OAUTH_CLIENT_ID=your_client_id
GOOGLE_OAUTH_CLIENT_SECRET=your_client_secret
```

### 실행

```bash
# 의존성 설치 및 빌드
mvn clean install

# 로컬 실행 (dev 프로파일)
mvn spring-boot:run
```

- Swagger UI: `http://localhost:80/swagger-ui/index.html`
- Redis: 로컬 6379 포트 실행 필요

---

## 🏗️ 핵심 설계

### 1. 공통 응답 구조

모든 API는 `ApiResponse<T>` 래퍼로 통일됩니다.

```json
{
  "success": true,
  "message": "요청이 성공했습니다.",
  "data": { ... }
}
```

실패 시 `GlobalExceptionHandler`가 자동 처리하며, 서비스 계층에서는 `throw new CustomException(ErrorCode.XXX)` 만 작성하면 됩니다.

### 2. JWT 인증 흐름

```
로그인 → AccessToken(30분) + RefreshToken(7일) 발급
→ 모든 요청 Authorization: Bearer {accessToken}
→ JwtAuthenticationFilter 검증
→ 만료 시 /api/v1/auth/refresh 로 재발급
→ Redis에 세션 저장·관리
```

### 3. 화상회의 — AI 회의록 자동 생성

팩토리 메서드 + 템플릿 메서드 패턴으로 온라인·오프라인·하이브리드 회의를 단일 도메인(`TB_MTNG`)에서 처리합니다.

```
화상회의 종료
→ LiveKit 녹취 오디오
→ Whisper STT → 대화 로그 저장
→ LLM → 회의록 초안 자동 생성
→ 참석자 자동 결재자 설정 → 전자결재 기안 생성
```

외부 API 호출은 `@TransactionalEventListener(AFTER_COMMIT)` + `@Async`로 DB 커밋 후 비동기 처리합니다.

### 4. SSE 실시간 알림

`ApplicationEventPublisher`로 이벤트를 발행하면 `SseEmitterService`가 전송을 전담합니다. 도메인 서비스는 '언제 알림을 보낼지'만 결정하면 됩니다.

### 5. RBAC 권한 체계

```
TB_EMPLOYEE → TB_ROLE_ASSIGNMENT → TB_ROLE → TB_PERMISSION_ROLE_MAPPING → TB_PERMISSION
```

`@PreAuthorize` + `AuthorizationService`로 URL 레벨·메서드 레벨 권한을 이중 적용합니다.

---

## 📡 주요 API

| 도메인 | Base URL | 비고 |
|--------|----------|------|
| 인증 | `/api/v1/auth` | 로그인, 토큰 재발급 |
| 회의 | `/api/v1/meetings` | 온라인/오프라인/하이브리드 |
| 메신저 | `/api/v1/chats` + WebSocket | STOMP 실시간 |
| 알림 | `/api/v1/notifications` + SSE | 실시간 이벤트 |
| 대시보드 | `/api/v1/dashboard/widgets/**` | 위젯별 독립 API |
| 통합검색 | `/api/v1/search` | 6개 도메인 단일 진입점 |
| 일정 | `/api/v1/schedules` | 개인/팀/프로젝트 |
| 업무 | `/api/v1/projects/{id}/tasks` | 댓글·마감 알림 연동 |
| 회의실 예약 | `/api/v1/reservations` | 중복 검증 포함 |
| 전자결재 | `/api/v1/approval` | PL 담당 |
| 관리자 | `/api/v1/admin/**` | `ROLE_ADMIN` 전용 |

전체 명세: Swagger UI 또는 `AA_협업_가이드.md` 참고

---

## 🌿 Git 브랜치 전략

```
main      ← 배포 브랜치 (직접 커밋 금지)
  └── develop   ← 통합 브랜치
        ├── feature/{기능명}
        └── hotfix/{버그명}
```

### 커밋 메시지 형식

```
feat: 회의실 예약 중복 검증 로직 추가
fix: JWT 만료 오류 수정
refactor: MtngService 메서드 분리
docs: API 명세서 업데이트
```

### PR 규칙

- PR 방향: `feature` → `develop` (main 직접 PR 금지)
- 제목 형식: `[feat] 화상회의 AI 회의록 자동 생성 구현`
- 팀원 2명 이상 Approve 후 AA/PL이 머지

---

## 📋 개발 컨벤션

- 예외 처리: 서비스 계층에서 `throw new CustomException(ErrorCode.XXX)` 사용, Controller에서 try-catch 금지
- DTO 변환: MapStruct `~DtoMapper` 사용 (MyBatis Mapper와 이름 구분)
- 보안: `SecurityUtil.getCurrentEmpId()` 는 서비스 계층에서만 호출
- 팀원 코드 수정 금지: 새 메서드·이벤트 리스너 추가 방식으로 기능 확장
- MyBatis XML: `<`, `>`, `<=`, `>=` 는 `&lt;`, `&gt;`, `&lt;=`, `&gt;=` 로 작성

자세한 컨벤션은 `AA_협업_가이드.md` 참고

---

## 📅 프로젝트 기간

**2025.12.01 ~ 2026.07.06** (대덕인재개발원 전자정부 표준 프레임워크 & React 기반 풀스택 개발자 양성과정 14기)
