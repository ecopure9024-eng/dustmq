# dustmq — 광고 리워드 앱

AdMob 리워드 광고 시청 → 포인트 적립 → 기프티콘/현금 교환이 가능한 안드로이드 앱.

## 기술 스택
- Android (Kotlin + Jetpack Compose + Hilt)
- Firebase (Auth, Firestore, Cloud Functions, FCM, Analytics)
- Google AdMob (Rewarded Video + SSV)
- 카카오 소셜 로그인
- Google Play Integrity API

## 폴더 구조
```
dustmq/
├── app/                          # Android 앱 (Kotlin + Compose)
│   └── src/main/java/com/rewardapp/
│       ├── MainActivity.kt
│       ├── RewardApp.kt
│       ├── di/AppModule.kt
│       ├── data/model/           # 데이터 모델
│       ├── repository/           # Firestore/Functions 접근
│       ├── viewmodel/            # Hilt ViewModel
│       ├── ui/                   # Compose 화면
│       │   ├── auth/LoginScreen.kt
│       │   ├── home/HomeScreen.kt
│       │   ├── ads/AdListScreen.kt
│       │   ├── ads/AdWatchBottomSheet.kt
│       │   ├── exchange/ExchangeScreen.kt
│       │   └── profile/ProfileScreen.kt
│       └── util/                 # AdManager, IntegrityChecker, FCM
└── functions/                    # Firebase Cloud Functions (TypeScript)
    └── src/
        ├── index.ts
        ├── admobSsv.ts           # AdMob SSV 검증 + 포인트 지급 (핵심!)
        ├── attendance.ts         # 출석 체크
        ├── exchange.ts           # 기프티콘 교환 요청
        ├── integrityVerify.ts    # Play Integrity 토큰 검증
        └── kakaoAuth.ts          # 카카오 → Firebase 커스텀 토큰 발급
```

## 개발 시작 전 필요한 세팅

### 1. Firebase 프로젝트 연결
```bash
# Firebase CLI 설치
npm install -g firebase-tools
firebase login

# 프로젝트 연결
firebase use --add  # .firebaserc의 YOUR_FIREBASE_PROJECT_ID 교체
```

### 2. google-services.json 다운로드
Firebase 콘솔 → 프로젝트 설정 → 내 앱(Android) → `google-services.json` 다운로드 → `app/` 폴더에 배치.
(이 파일은 `.gitignore`에 포함되어 있음)

### 3. 카카오 네이티브 앱 키 설정
1. https://developers.kakao.com 에서 앱 등록 후 네이티브 앱 키 확인
2. `gradle.properties`의 `KAKAO_NATIVE_APP_KEY=...` 값 교체
3. 카카오 콘솔에 앱 패키지명(`com.rewardapp`)과 키 해시 등록

### 4. AdMob 콘솔에 SSV 콜백 URL 등록
Cloud Function `admobSsv`를 배포한 뒤 발급된 URL을 AdMob 광고 단위 설정에서
**서버 측 확인 (SSV)** URL로 등록한다.

```
https://asia-northeast3-{프로젝트ID}.cloudfunctions.net/admobSsv
```

### 5. Cloud Functions 배포
```bash
cd functions
npm install
npm run build
firebase deploy --only functions
```

### 6. Firestore 규칙/인덱스 배포
```bash
firebase deploy --only firestore:rules,firestore:indexes
```

## 주의사항 (엄수)
- 포인트 지급은 반드시 **Cloud Functions**에서만 (앱에서 직접 Firestore 쓰기 금지)
- 모든 포인트 변동은 `pointTransactions`에 로그 남김
- 개발 중에는 **AdMob 테스트 광고 ID**만 사용
  - App ID: `ca-app-pub-3940256099942544~3347511713`
  - Rewarded: `ca-app-pub-3940256099942544/5224354917`
- 광고 일일 시청 한도: 유저당 20회 (`admobSsv.ts`에서 검증)

## MVP 체크리스트
- [x] Firebase / Functions 스캐폴딩
- [x] 카카오 로그인 플로우 (클라이언트 + 커스텀 토큰 교환 함수)
- [x] AdMob SSV Cloud Function
- [x] 5개 화면 Compose UI
- [x] 어뷰징 방지 (Play Integrity)
- [x] 출석 체크
- [ ] FCM 토큰 Firestore 저장 & 서버 발송 로직
- [ ] 기프티쇼 비즈 API 연동 (계약 후)
- [ ] 실제 광고 단위 ID 및 운영 배포
```
