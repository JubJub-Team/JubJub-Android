<div align="center">
  <img src="https://github.com/user-attachments/assets/9db659f1-fb90-43a3-ac4c-0690f596bc9d" width="500" alt="jubjub_logo">
</div>

# 🧺 JubJub (줍줍)

<div align="center">

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white) ![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white) ![Firebase](https://img.shields.io/badge/Firebase-FFCA28?style=flat&logo=firebase&logoColor=white) ![NaverMap](https://img.shields.io/badge/NaverMap-03C75A?style=flat&logo=naver&logoColor=white) ![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=flat&logo=springboot&logoColor=white) ![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=flat&logo=postgresql&logoColor=white) ![PostGIS](https://img.shields.io/badge/PostGIS-2F7D32?style=flat&logo=postgresql&logoColor=white) ![TeachableMachine](https://img.shields.io/badge/Teachable_Machine-4285F4?style=flat&logo=google&logoColor=white) ![ML Kit](https://img.shields.io/badge/Google_ML_Kit-4285F4?style=flat&logo=google&logoColor=white)

> **"대학 생활의 따뜻한 연결고리"**
>
> **대학생을 위한 캠퍼스 분실물 찾기 & 물품 나눔 플랫폼**

</div>

---

## 📖 프로젝트 개요

JubJub(줍줍)은 "줍다"라는 단어에서 영감을 받아 탄생한 서비스입니다. 넓은 캠퍼스에서 물건을 잃어버려 당황했던 경험, 혹은 더 이상 쓰지 않는 물건을 버리기엔 아까웠던 경험을 해결하기 위해 기획되었습니다.

단순한 게시판을 넘어, **위치 기반 서비스**를 통해 분실물의 습득 위치를 직관적으로 확인하고, 학우들과 신뢰할 수 있는 환경에서 물품을 나눌 수 있는 **따뜻한 캠퍼스 커뮤니티**를 지향합니다.

또한 짧은 개발 기간 동안 Firebase 기반으로 서비스를 먼저 완성한 뒤, 이후 확장성과 공간 검색 성능을 고려해 **Spring Boot + PostgreSQL/PostGIS 백엔드 모듈**을 별도로 분리해 두었습니다.

---

## ✨ 핵심 기능

### 🔎 분실물 찾기
* **위치 기반 등록 및 탐색:** 네이버 지도 API를 활용해 분실 및 습득 장소를 직접 선택하고, 게시글에 좌표를 함께 저장할 수 있습니다.
* **상세 지도 보기:** 상세 화면에서 등록된 위치를 지도 위에서 바로 확인할 수 있습니다.
* **상태 관리:** `찾는 중` → `찾음 완료` 상태 변경을 통해 해결된 건을 직관적으로 구분합니다.

### 🎁 물품 나눔
* **자원 순환:** 더 이상 사용하지 않는 전공 서적, 충전기, 생활용품 등을 학우들과 무료로 나눕니다.
* **나눔 방식 설정:** `직거래`, `택배` 등 원하는 거래 방식을 선택할 수 있습니다.
* **위치 기반 거래 희망 장소 등록:** 나눔 게시글에도 거래 희망 위치를 좌표와 함께 남길 수 있습니다.
* **상태 관리:** `나눔 중` → `예약 중` → `나눔 완료` 프로세스를 통해 중복 연락을 방지합니다.

### 📱 사용자 편의 기능
* **통합 검색 & 필터:** 키워드 검색 및 카테고리/상태별 필터링을 통해 원하는 정보를 빠르게 찾습니다.
* **위치 등록 글 필터:** 위치가 등록된 게시글만 모아서 확인할 수 있습니다.
* **캠퍼스 기반 서비스:** 재학 중인 학교뿐만 아니라 방문한 다른 학교의 분실물도 자유롭게 검색하고 교류할 수 있는 유연한 캠퍼스 환경을 제공합니다.
* **이미지 AI 태깅:** 업로드한 이미지를 분석하여 자동으로 태그를 생성해 줍니다.

### 🗺️ PostGIS 기반 확장 기능
* **공간 좌표 저장:** `geometry(Point, 4326)` 기반으로 게시글 위치를 저장할 수 있는 백엔드 구조를 분리했습니다.
* **반경 검색 및 지도 영역 검색:** `ST_DWithin`, `ST_Intersects`를 활용한 위치 기반 조회 API를 구현했습니다.
* **분실글/습득글 매칭:** 기준 게시글 주변의 반대 유형 게시글을 거리순으로 찾는 매칭 API를 추가했습니다.

---

## 🛠️ 기술 스택

### **Android App**
* **Language:** Kotlin
* **Architecture:** MVVM pattern
* **DI:** Hilt
* **Asynchronous:** Coroutines & Flow
* **UI:** XML, ViewBinding
* **Network:** Firebase SDK, Naver Maps API

### **Backend & Database**
* **Current Service Platform:** Firebase
* **Database:** Cloud Firestore
* **Storage:** Cloud Storage for Firebase
* **Authentication:** Firebase Auth
* **Separated Backend Module:** Java Spring Boot
* **Spatial Database:** PostgreSQL + PostGIS
* **Migration:** Flyway
* **Local Infra:** Docker Compose

---

## 📂 아키텍처 및 보안

* **데이터 관리:**
  * ViewModel을 활용한 데이터 캐싱 전략으로 불필요한 네트워크 요청 최소화
  * LiveData/StateFlow를 이용한 반응형 UI 구현
  * Android 앱과 분리 가능한 `backend` 모듈 구조로 서버 기능 확장 기반 마련
  * PostGIS 공간 인덱스와 반경 검색 쿼리 구조를 분리해 위치 기반 확장성 확보
* **보안:**
  * `local.properties`를 활용한 Android API Key 은닉 및 관리
  * `backend/.env` 기반 로컬 DB 접속 정보 분리
  * 민감한 환경 파일이 GitHub에 올라가지 않도록 `.gitignore` 관리
  * Firebase Security Rules를 통한 데이터베이스 읽기/쓰기 권한 제어
