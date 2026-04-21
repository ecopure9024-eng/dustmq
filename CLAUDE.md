# dustmq 프로젝트

## 규칙
- 항상 한국어로 대답해줘
- 작업 완료 후 항상 git add . → git commit → git push 해줘

## 프로젝트 설명

광고 리워드 앱 — 사용자가 AdMob 리워드 광고를 시청하면 포인트를 적립하고, 해당 포인트로 기프티콘/현금으로 교환할 수 있는 안드로이드 앱.

### 기술 스택
- **앱**: Android (Kotlin + Jetpack Compose)
- **백엔드**: Firebase (Auth, Firestore, Cloud Functions)
- **광고**: Google AdMob (Rewarded Video + SSV)
- **인증**: 카카오 소셜 로그인 + 휴대폰 본인인증
- **알림**: Firebase Cloud Messaging (FCM)
- **분석**: Firebase Analytics
- **어뷰징 방지**: Google Play Integrity API

### 폴더 구조
```
dustmq/
├── app/                          # Android 앱
│   └── src/main/java/com/rewardapp/
│       ├── MainActivity.kt
│       ├── ui/                   # Compose 화면
│       ├── viewmodel/            # ViewModel
│       ├── repository/           # 데이터 계층
│       └── util/                 # AdManager, IntegrityChecker
└── functions/                    # Firebase Cloud Functions (TS)
    └── src/
        ├── admobSsv.ts           # AdMob SSV 검증 및 포인트 지급 (핵심)
        ├── attendance.ts         # 출석 체크
        ├── exchange.ts           # 기프티콘 교환
        └── integrityVerify.ts    # Play Integrity 토큰 검증
```

### 핵심 규칙 (엄수)
1. **포인트 지급은 Cloud Functions에서만** — 앱에서 Firestore에 포인트를 직접 쓰면 절대 안 됨
2. **모든 포인트 변동은 `pointTransactions` 컬렉션에 로그 필수**
3. **개발 중에는 AdMob 테스트 광고 ID 사용** (실광고 ID 사용 시 정책 위반)
4. **광고 일일 시청 한도**: 유저당 20회
5. **Firestore 보안 규칙**: `users` 컬렉션은 본인만 읽기 가능, 쓰기는 Functions만

### AdMob SSV 흐름
```
앱: 광고 로드 → 사용자 시청 완료
  ↓
AdMob → Cloud Functions (/admobSsv) 콜백 전송
  ↓
Functions: Google 공개키로 ECDSA 서명 검증
  ↓
Functions: transaction_id 중복 체크 → Firestore 트랜잭션으로 포인트 증가
  ↓
앱: Firestore 실시간 리스닝으로 포인트 업데이트 반영
```

### Firestore 컬렉션
- `users/{userId}` — phone, points, streak, lastAttendance, createdAt, status
- `pointTransactions/{txId}` — userId, type, amount, refId, createdAt
- `adViews/{viewId}` — userId, adUnitId, rewardAmount, ssvVerified, deviceId, ip, createdAt
- `withdrawals/{wdId}` — userId, type, itemName, pointsUsed, status, createdAt

### MVP 개발 순서
1. Firebase 프로젝트 세팅 (Auth/Firestore/Functions)
2. 카카오 소셜 로그인 + Firebase 연동
3. **AdMob SSV Cloud Function** (핵심)
4. 홈 화면 (포인트 카드 + 미션 리스트)
5. 광고 시청 플로우 (로드 → 시청 → SSV → 반영)
6. 교환하기 (기프티콘 목록 + 교환 신청)
7. Play Integrity 어뷰징 방지
8. 출석 체크
9. FCM 푸시 알림

### 테스트 광고 ID (개발 중 고정 사용)
- App ID: `ca-app-pub-3940256099942544~3347511713`
- Rewarded Ad Unit: `ca-app-pub-3940256099942544/5224354917`

### 참고 자료
- AdMob SSV: https://developers.google.com/admob/android/ssv
- Play Integrity: https://developer.android.com/google/play/integrity
- Firebase Functions: https://firebase.google.com/docs/functions
- 기프티쇼 비즈 API: https://biz.giftshow.co.kr
