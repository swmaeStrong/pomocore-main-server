# Pomocore Backend API Server

## 📋 Overview
Pomocore 앱의 핵심 백엔드 서비스로, 사용자 인증, 포모도로 세션 관리, 실시간 리더보드, 그룹 기능 등을 제공하는 고성능 REST API 서버입니다.

**핵심 최적화**: Spring Boot 3.4와 Redis를 활용한 캐싱 전략으로 응답 속도 개선 및 실시간 데이터 처리

## 🏗️ Architecture Pattern
**레이어드 아키텍처 (Layered Architecture)** 를 기반으로 DDD 패턴을 적용:
- `domain/`: 비즈니스 로직과 도메인 엔티티
- `infra/`: 외부 시스템 통합 (Redis, MongoDB, LLM, S3)
- `config/`: 애플리케이션 설정 및 보안
- `web/`: 외부 웹훅 처리

## 🔄 Data Flow

```
[Client Request] → [Controller] → [Service/Facade] → [Repository] → [Database]
        ↓                ↓                ↓                              ↓
    Security         Business        Domain Logic              MongoDB/Redis
    Filter            Logic          & Validation               PostgreSQL
```

### 상세 처리 과정:

1. **Request Authentication**: JWT 토큰 기반 인증
2. **Business Logic Processing**: Service 레이어에서 비즈니스 로직 처리
3. **Data Persistence**: 
   - PostgreSQL: 사용자, 구독 정보
   - MongoDB: 포모도로 세션, 카테고리 패턴
   - Redis: 캐싱, 리더보드, 세션 관리
4. **Real-time Processing**: Redis Stream을 통한 실시간 데이터 파이프라인
5. **Response Formatting**: DTO 변환 및 응답

## 📂 Key Components

### 1. Domain Layer (`domain/`)
핵심 비즈니스 도메인 모델과 로직을 포함합니다.

#### Core Domains
- **User Domain**: 사용자 관리 및 인증
  - OAuth2 소셜 로그인 (Google, Apple)
  - JWT 토큰 기반 인증/인가
  - 사용자 프로필 관리

- **Pomodoro Domain**: 포모도로 세션 관리
  - 세션 생성/조회/분석
  - 실시간 패턴 분류 (Redis Stream 연동)
  - 일별/주간/월간 통계

- **Leaderboard Domain**: 실시간 리더보드
  - Redis ZSet 기반 순위 시스템
  - 일별/주간/월간/전체 리더보드
  - 카테고리별 순위

- **Group Domain**: 그룹 기능
  - 그룹 생성/가입/탈퇴
  - 그룹 내 리더보드
  - 그룹 목표 설정

- **Goal Domain**: 목표 관리
  - 개인/그룹 목표 설정
  - 목표 달성 추적
  - 진행률 계산

- **Streak Domain**: 연속 기록 관리
  - 일일 스트릭 추적
  - 최대 연속 기록 관리
  - 스트릭 리워드 시스템

- **CategoryPattern Domain**: 카테고리 패턴 관리
  - 앱/URL 패턴 CRUD
  - 패턴 우선순위 관리
  - AI 분류 결과 저장

- **SessionScore Domain**: 세션 점수 계산
  - 포모도로 세션 점수화
  - 카테고리별 가중치 적용
  - 점수 집계 및 통계

### 2. Infrastructure Layer (`infra/`)

#### Redis Integration
- **RedisRepository**: 범용 Redis 작업
  - 키-값 저장/조회/삭제
  - TTL 관리
  - 배치 작업 지원

- **RedisStreamProducer**: 스트림 메시지 발행
  - 포모도로 패턴 분류 요청
  - 리더보드 업데이트 요청
  - 비동기 메시지 처리

- **AbstractRedisStreamConsumer**: 스트림 컨슈머 기반 클래스
  - 메시지 소비 추상화
  - 에러 핸들링
  - 재시도 로직

#### LLM Integration
- **ChatGPTManager**: OpenAI API 통합
  - 요약 생성
  - 프롬프트 템플릿 관리

#### External Services
- **MailSender**: 이메일 발송
  - 템플릿 기반 이메일
  - 비동기 발송
  - 발송 상태 추적

- **S3 Integration**: 파일 저장소
  - 프로필 이미지 업로드
  - 리포트 파일 저장
  - CDN 연동

### 3. Configuration Layer (`config/`)

#### Security Configuration
- **JWT Authentication**: 토큰 기반 인증
  - Access/Refresh 토큰 관리
  - 토큰 갱신 로직
  - 권한 기반 접근 제어

- **OAuth2 Configuration**: 소셜 로그인
  - Google OAuth2
  - Apple Sign-In
  - 사용자 정보 매핑

- **CORS Configuration**: Cross-Origin 설정
  - 허용 도메인 관리
  - 메서드/헤더 설정

#### Application Configuration
- **AsyncConfig**: 비동기 처리 설정
  - 스레드 풀 관리
  - 비동기 작업 큐
  - 타임아웃 설정

- **CacheConfig**: Caffeine 캐시 설정
  - 캐시 정책 정의
  - TTL 설정
  - 캐시 통계

- **RedisConfig**: Redis 연결 설정
  - 커넥션 풀 관리
  - Serializer 설정
  - 클러스터 지원

## 🚀 Performance Optimizations

### 1. 캐싱 전략
- **Caffeine Cache**: 로컬 캐싱으로 반복 조회 최적화
- **Redis Cache**: 분산 캐싱으로 서버 간 데이터 공유
- **Query Result Caching**: 복잡한 쿼리 결과 캐싱

### 2. 비동기 처리
- **@Async 어노테이션**: CPU 집약적 작업 비동기화
- **CompletableFuture**: 비동기 작업 체이닝
- **Redis Stream**: 이벤트 기반 비동기 처리

### 3. 데이터베이스 최적화
- **인덱싱 전략**: 자주 사용되는 쿼리 최적화
- **배치 처리**: 대량 데이터 일괄 처리
- **연결 풀링**: HikariCP를 통한 커넥션 관리

### 4. API 최적화
- **Pagination**: 대량 데이터 페이징 처리
- **Projection**: 필요한 필드만 조회
- **DTO 변환**: 엔티티 직접 노출 방지

## 🔧 Configuration

### Application Profiles
```yaml
# application-local.yml: 로컬 개발 환경
# application-dev.yml: 개발 서버 환경
# application-prod.yml: 프로덕션 환경
```

### Key Configuration Values
- **JWT Token Expiry**: Access Token 30분, Refresh Token 14일
- **Redis TTL**: 리더보드 24시간, 캐시 1시간
- **Thread Pool**: Core 10, Max 50, Queue 100
- **Database Pool**: Min 10, Max 30 connections

## 🏃‍♂️ Running the Server

### Dependencies
- **Java 17+**
- **PostgreSQL 14+**
- **MongoDB 6.0+**
- **Redis 7.0+**

### Environment Variables
```bash
# Database
SPRING_DATASOURCE_URL=${postgresql_url}
SPRING_DATA_MONGODB_URI=${mongodb_uri}
SPRING_DATA_REDIS_HOST=${redis_host}
SPRING_DATA_REDIS_PASSWORD=${redis_password}

# OAuth2
GOOGLE_CLIENT_ID=${google_client_id}
GOOGLE_CLIENT_SECRET=${google_client_secret}
APPLE_CLIENT_ID=${apple_client_id}
APPLE_KEY_ID=${apple_key_id}

# OpenAI
OPENAI_API_KEY=${openai_api_key}

# AWS
AWS_ACCESS_KEY=${aws_access_key}
AWS_SECRET_KEY=${aws_secret_key}
AWS_S3_BUCKET=${s3_bucket_name}
```

### Build & Run
```bash
# Build
./gradlew clean build

# Run with profile
./gradlew bootRun --args='--spring.profiles.active=local'

# Docker build
docker build -t pomocore-backend .

# Docker run
docker run -d \
  --name pomocore-api \
  -p 8080:8080 \
  --env-file .env \
  pomocore-backend
```

### Health Check
```bash
curl http://localhost:8080/actuator/health
```

## 📊 API Documentation

### Swagger UI
- Local: http://localhost:8080/swagger-ui/index.html

### Main API Endpoints

#### Authentication
- `POST /api/v1/auth/login`: 소셜 로그인
- `POST /api/v1/auth/refresh`: 토큰 갱신
- `POST /api/v1/auth/logout`: 로그아웃

#### Pomodoro
- `POST /api/v1/pomodoro/session`: 세션 생성
- `GET /api/v1/pomodoro/sessions`: 세션 목록 조회
- `GET /api/v1/pomodoro/statistics`: 통계 조회

#### Leaderboard
- `GET /api/v1/leaderboard/daily`: 일별 순위
- `GET /api/v1/leaderboard/weekly`: 주간 순위
- `GET /api/v1/leaderboard/monthly`: 월간 순위

#### Group
- `POST /api/v1/groups`: 그룹 생성
- `POST /api/v1/groups/{id}/join`: 그룹 가입
- `GET /api/v1/groups/{id}/leaderboard`: 그룹 리더보드

## 🎯 Key Design Decisions

### 1. 왜 레이어드 아키텍처인가?
- **명확한 책임 분리**: 각 레이어가 명확한 역할 수행
- **테스트 용이성**: 레이어별 독립적 테스트 가능
- **유지보수성**: 변경 영향도 최소화

### 2. 왜 MongoDB + PostgreSQL + Redis인가?
- **PostgreSQL**: 트랜잭션이 중요한 사용자/결제 데이터
- **MongoDB**: 유연한 스키마가 필요한 세션/패턴 데이터
- **Redis**: 실시간 처리와 캐싱

### 3. 왜 Redis Stream인가?
- **실시간 처리**: 낮은 지연시간으로 메시지 처리
- **안정성**: 메시지 영속성과 재처리 지원
- **확장성**: 컨슈머 그룹으로 수평 확장 가능

### 4. 왜 JWT 토큰인가?
- **Stateless**: 서버 세션 관리 불필요
- **확장성**: 서버 증설 시 세션 공유 불필요
- **성능**: 매 요청마다 DB 조회 불필요

## 🔄 Evolution History

1. 기본 CRUD API 구현
2. OAuth2 소셜 로그인 추가
3. Redis Stream 기반 실시간 처리
4. 그룹 기능 및 그룹 리더보드
5. AI 기반 패턴 분류 통합
6. 캐싱 전략 및 성능 최적화
7. 마이크로서비스 아키텍처 전환 (현재)
   - 패턴 분류 로직을 독립적인 Go 서버로 분리
   - 데이터 파이프라인 서비스 분리로 90% 성능 개선
   - Spring 서버는 API 게이트웨이 역할에 집중
   - 서비스 간 Redis Stream 통신으로 느슨한 결합 구현

## 🚨 Known Limitations & Future Work

### Current Limitations
- **모니터링**: APM 도구 미적용
- **테스트**: 통합 테스트 커버리지 부족

### Future Improvements
- [ ] 오토 스케일링
- [ ] 모니터링 시스템 구축
- [ ] 테스트 코드 작성

---

## 📚 Code Reading Guide

### 시작점
1. `DemoApplication.java` - 애플리케이션 엔트리포인트
2. `config/security/` - 인증/인가 설정
3. `domain/*/controller/` - API 엔드포인트 정의

### 핵심 파일들
- **인증 플로우**: `config/security/jwt/JwtAuthenticationFilter.java`
- **비즈니스 로직**: `domain/pomodoro/service/PomodoroServiceImpl.java`
- **실시간 처리**: `infra/redis/stream/RedisStreamProducer.java`
- **리더보드**: `domain/leaderboard/service/LeaderboardServiceImpl.java`
- **캐싱**: `config/CacheConfig.java`

이 문서를 통해 Pomocore 백엔드 서버의 전체적인 구조와 주요 기능을 이해할 수 있으며, 향후 개발 및 유지보수 시 참고자료로 활용할 수 있습니다.