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
- `ST_DWithin` 기반 반경 검색
- `ST_Intersects` 기반 지도 영역 검색
