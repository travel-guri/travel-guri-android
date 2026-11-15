# 떠나구리

차량의 실제 주행 경로와 여행 중 찍은 사진을 연결해 여행 리캡을 자동으로 만들어 주는 Pleos Connect 앱입니다.

현재 저장소에는 기획 의도를 보여 주는 목업 앱이 들어 있습니다. 화면에 나오는 여행·사진·게시글은 고정된 예시 데이터이고, 주행/주차 화면 전환만 Pleos Vehicle SDK의 실제 기어 신호로 동작합니다.

| 경로 | 내용 |
| --- | --- |
| `trip-recap-mockup/` | 떠나구리 목업 앱 (Kotlin, Jetpack Compose) |
| `tteonaguri-scenario-demo.mp4` | 에뮬레이터에서 녹화한 사용자 시나리오 영상 (84초) |
| `pleos-sample-vehicle-app/` | 참고용 Pleos Vehicle SDK 샘플 앱 |
| `claude-sessions/` | 개발에 사용한 Claude Code 세션 기록 |

## 실행 방법

1. Pleos Connect 에뮬레이터를 실행합니다.
2. 앱을 빌드해 설치합니다.

   ```bash
   cd trip-recap-mockup
   ./gradlew :app:installDebug
   ```

3. 에뮬레이터 앱 목록에서 앱을 열거나 아래 명령으로 실행합니다.

   ```bash
   adb shell am start -n com.triprecap.mockup/.MainActivity
   ```

4. 기어를 바꿔 주행 화면과 주차 화면을 전환합니다. Pleos Virtual Console에서 직접 바꾸거나 아래 스크립트를 씁니다.

   ```bash
   ./tools/vc.sh gear Drive   # 주행 화면 (기록 상태만 표시)
   ./tools/vc.sh gear Park    # 주차 화면 (지도·캘린더·리캡·캐릭터·게시판)
   ```

Android Studio에서 실행하려면 `trip-recap-mockup` 폴더를 프로젝트로 열고 `app` 구성을 실행하면 됩니다.

### 시연 순서

1. 주차 상태에서 지도 탭의 "새 여행 시작" → "기록 시작"
2. 기어를 D로 변경: 주행 화면으로 바뀌고 주행 거리가 올라감
3. 기어를 P로 변경: "여행을 마칠까요?" 팝업 → "여행 종료" → 리캡 생성
4. "리캡 보기"에서 리캡 확인, 사진 추가(QR), 게시판 공유
5. 캘린더 탭에서 지난 여행을 골라 지도로 다시 보기

## 테스트 환경 및 필요한 설정

| 항목 | 값 |
| --- | --- |
| JDK | 17 이상 (JDK 21에서 빌드 확인) |
| Gradle / AGP | 8.10.2 / 8.8.2 (Gradle Wrapper 포함) |
| Kotlin / Compose BOM | 2.0.0 / 2024.04.01 |
| compileSdk / targetSdk / minSdk | 35 / 35 / 28 |
| Pleos Vehicle SDK | `ai.pleos.playground:Vehicle:2.3.2` |

필요한 설정은 다음과 같습니다.

- **Android SDK 경로**: `trip-recap-mockup/local.properties`에 `sdk.dir=<Android SDK 경로>`를 넣습니다. Android Studio로 열면 자동으로 만들어집니다. 이 파일은 저장소에 올리지 않습니다.
- **Vehicle SDK 저장소**: `https://nexus-playground.pleos.ai/repository/maven-releases/`에서 내려받습니다. `settings.gradle.kts`에 이미 등록되어 있어 따로 설정할 것은 없고, 빌드할 때 인터넷 연결만 필요합니다.
- **권한**: 기어 상태를 읽기 위해 `pleos.car.permission.CAR_DRIVE`를 사용합니다. 매니페스트에 선언되어 있습니다.
- **`tools/vc.sh`**: macOS/Linux용이며 `adb`, `curl`, `python3`가 필요합니다. `adb forward`로 에뮬레이터의 Virtual Console(8088 포트)에 연결합니다.

기어 신호를 아직 받지 못했거나 값을 알 수 없으면 안전을 위해 주행 화면을 유지합니다. Vehicle SDK를 쓸 수 없는 일반 에뮬레이터에서는 계기판 아래에 기어 P/D 데모 전환 버튼이 나타납니다.

## 개발에 사용한 Pleos Connect 에뮬레이터 버전과 화면 규격

| 항목 | 값 |
| --- | --- |
| 에뮬레이터 | Pleos Connect 2.1.0 (시스템 이미지 `android-34/pleos_car_emulator`, arm64-v8a) |
| Android API | 34 |
| 화면 해상도 | 2560 × 1440 px, 가로 |
| 화면 밀도 | 160 dpi (mdpi) |
| 앱 표시 영역 | 약 1678 × 1208 px |

화면은 Figma 프레임 크기인 1200 × 560 기준으로 그리고, 앱 표시 영역에 맞춰 같은 비율로 확대합니다. 비율이 달라 남는 위아래 영역은 계기판 배경색으로 채웁니다.

## APK 파일 위치

GitHub Release에 디버그 APK를 올려 두었습니다.

- Release: https://github.com/travel-guri/travel-guri-android/releases/tag/v0.1.0-mockup
- 파일: `tteonaguri-v0.1.0-mockup-debug.apk`
- 설치: `adb install -r tteonaguri-v0.1.0-mockup-debug.apk`

직접 빌드하면 `trip-recap-mockup/app/build/outputs/apk/debug/app-debug.apk`에 만들어집니다.

```bash
cd trip-recap-mockup
./gradlew :app:assembleDebug
```
