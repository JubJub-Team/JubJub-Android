# JubJub Backend

`backend` 모듈은 Android 앱과 분리된 Spring Boot + PostgreSQL/PostGIS 백엔드 시작점입니다.

## 분리 원칙

- Android SDK, Compose, Hilt 같은 안드로이드 의존성 미사용
- 루트 프로젝트에서는 `:backend` 모듈로 포함하고, 이후 `backend` 폴더만 별도 저장소나 IntelliJ 프로젝트로 이관 가능
- DB 주소와 계정 정보는 환경 변수와 `.env` 파일로 외부 분리

## 로컬 실행

1. `backend/.env.example`을 참고해서 `backend/.env` 생성
2. `backend` 폴더에서 `docker compose up -d` 실행
3. 루트에서 `./gradlew :backend:bootRun` 실행

## PostGIS 적용 범위

- `geometry(Point, 4326)` 기반 게시글 위치 저장
- `GIST` 공간 인덱스 기반 좌표 검색 최적화
- `GIST ((location::geography))` 기반 미터 반경 검색 인덱스 적용
- `ST_DWithin` 기반 반경 검색
- `ST_Intersects` 기반 지도 영역 검색
- `ST_DWithin` 기반 분실글/습득글 주변 매칭 검색

## PostGIS 검증

- 실제 PostGIS 컨테이너 기반 통합 테스트: `PostSpatialJdbcRepositoryIntegrationTest`
- 검증 범위
  - 반경 검색 결과 검증
  - 분실글/습득글 매칭 결과 검증
  - `EXPLAIN ANALYZE` 기반 geography GiST 인덱스 사용 확인
- 실행 조건
  - Docker Desktop 또는 Docker Engine 구동 상태
  - Docker daemon 접근 가능한 로컬 터미널 환경

### EXPLAIN ANALYZE 예시

```sql
EXPLAIN ANALYZE
SELECT id
FROM posts
WHERE school = 'Konkuk'
  AND post_type = 'FOUND'
  AND ST_DWithin(
      location::geography,
      ST_SetSRID(ST_MakePoint(127.0764, 37.5405), 4326)::geography,
      500
  )
ORDER BY ST_Distance(
    location::geography,
    ST_SetSRID(ST_MakePoint(127.0764, 37.5405), 4326)::geography
) ASC
LIMIT 10;
```

확인 포인트

- 실행 계획에 `idx_posts_location_geography_gist`가 포함되는지 확인
- `Index Scan`, `Bitmap Index Scan`, `Bitmap Heap Scan` 중 하나가 보이는지 확인
- 소량 데이터에서는 planner가 sequential scan을 고를 수 있으므로 `ANALYZE` 후 확인
