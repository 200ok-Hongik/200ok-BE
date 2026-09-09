# RabbitMQ 비동기 스캔

다른 프로젝트의 음식 트렌드 분석 코드와 중복 클래스는 `reference-sources`에 보존했습니다. 실행 코드는 재활용 이미지 분석에 맞게 교체했습니다.

- 로컬 브로커: `docker compose up -d rabbitmq`
- 활성화: `RABBITMQ_ENABLED=true`. 기본값 false이므로 비활성화 상태에서는 분석 접수에 503을 반환합니다. 동기 분석 API는 없습니다.
- `POST /api/ai/analysis`: 인증된 사용자의 multipart `image`를 받아 202와 jobId 반환.
- `GET /api/ai/analysis/{jobId}`: 본인 작업의 상태와 V1 결과 조회.
- durable exchange `recycle.ai`, queue `recycle.ai.analyze`, routing key `analyze`.
- 이미지와 사용자 ID는 DB에 저장하고 큐에는 작업 ID만 전송합니다.
- 워커는 기존 HTTP AI 클라이언트를 호출합니다. AI 서버 자체의 RabbitMQ 수신 기능을 가정하지 않습니다.
- API는 DB 저장 후 202를 반환합니다. 1초 주기 relay가 새 QUEUED 작업을 큐에 전송하고 publisher confirm/return으로 전달을 확인합니다. 발행 전 DB에 예약 시간을 기록해 매초 중복 발행을 막습니다. 발행 실패 또는 메시지 유실 시 5분 후 재전송합니다. 오래 대기 중인 작업도 5분 간격으로 재전송될 수 있으며 중복 수신 시 분석을 생략합니다.
- 처리 완료/실패 후 이미지 바이트를 제거합니다. 실패 작업은 FAILED로 조회할 수 있습니다.
- 짧은 DB 트랜잭션에서 PROCESSING을 커밋한 뒤 AI를 호출하고, 별도 트랜잭션에서 완료/실패를 저장합니다. 처리 중 행 잠금이나 작업 트랜잭션을 유지하지 않습니다. DB 행 잠금과 상태로 일반 중복 전달을 무시합니다. 프로세스가 스캔 저장 직후 작업 상태 커밋 전에 종료되면 재처리로 스캔이 중복 생성될 수 있습니다. 정확히 한 번 실행을 보장하지 않습니다.
- GET에서 처리 중 PROCESSING을 조회할 수 있습니다. 5분 이상 멈춘 PROCESSING은 relay가 회수합니다. 이전 시도의 늦은 결과가 새 작업 상태를 덮어쓰지 않도록 시작 시각을 확인합니다. 5분은 AI 호출 제한 120초보다 길게 설정되어 있으며, 전체 처리가 5분을 넘으면 재시도가 겹칠 수 있습니다.
- 로그에 jobId, queueWaitMs, processingMs, AI 호출·이미지 업로드·결과 저장 elapsedMs를 남깁니다. queueWaitMs는 최초 접수부터 현재 시도 시작까지의 시간입니다.
- 2026-09-09 변경은 기존 status/updatedAt 열을 사용하므로 추가 DB 마이그레이션이 필요하지 않습니다.
- 새 테이블 수동 SQL: `migrations/20260908_ai_jobs.sql`.

## 검증 실행

일반: `./gradlew test bootJar`

실제 브로커: `RUN_RABBIT_INTEGRATION=true ./gradlew test --tests '*RabbitMqIntegrationTest'`

실제 AI: `RUN_AI_LIVE=true AI_SERVER_URL=<server> AI_TEST_IMAGE=<png path> ./gradlew test --tests '*AiLiveIntegrationTest'`

실제 브로커 테스트는 AI 분석을 mock으로 대체하며, 실제 AI 테스트는 별도로 HTTP 요청과 V1 파싱을 검증합니다. 외부 호출 테스트는 기본적으로 비활성화되어 있습니다.

## 2026-09-08 확인

- Render /health 200, 약 2.8초.
- /analyze: 빈 PNG로 200, 직접 호출 약 34.2초, Java 클라이언트 약 30.5초.
- 결과 objects=[], additionalObjects=[]; 실제 품목 사진 인식은 별도 검증 필요.
- Render 명세에는 아직 /analyze-ai2가 있고 /finalize가 없습니다. V1 빈 배열은 호환되지만 V2 배포 완료로 볼 수 없습니다.
- dev.ssok.store의 분석 응답 명세에는 objects/additionalObjects 반영됨.
- 배포 API의 비로그인 요청은 당시 HTTPS 로그인으로 302. 로컬 수정은 API 401 반환으로 변경.
- 실제 이미지 업로더는 기존 FakeImageUploader입니다. 원본 이미지 영구 저장은 아직 구현되어 있지 않습니다.
