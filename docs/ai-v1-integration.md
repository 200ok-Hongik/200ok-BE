# AI 1차 판단 V1 수신

`AiModelResponse`는 AI `POST /analyze`의 `objects[]`, `additionalObjects[]`를 받습니다.
객체는 `objectId`, `bbox`(xMin/yMin/xMax/yMax), `finalResult`(itemCode/states/source)를 가집니다.
분석 API 응답은 `scanResultId`, `objects`, `additionalObjects`입니다. 기존 단일 categoryCode/checklistResults 계약을 대체하므로 프론트 수정이 필요합니다.

## 저장

- `scan_results.ai_raw_response`: AI 원문 전체. 탐지 0개, 추가 후보와 알 수 없는 분석 메타데이터도 보존합니다.
- `ai_scan_results`: 확정 탐지 객체당 한 행. `object_id`로 사진 내 객체를 식별합니다.
- 각 행의 `raw_response`: 해당 객체의 bbox, finalResult를 JSON으로 저장합니다.
- 기존 confidence/model_version은 V1 예시에 없으므로 null입니다. source로 신뢰도를 추정하지 않습니다.
- 기존 체크리스트 조회를 지원하기 위해 일치하는 상태 키만 AI_SCAN_DETAILS에 투영합니다. 상태 전체는 JSON에 남습니다.
- 전체 객체의 DB 쓰기는 하나의 트랜잭션입니다. 외부 AI 호출과 이미지 업로드는 트랜잭션 밖에서 수행합니다.

`docs/migrations/20260908_ai_v1.sql`은 배포 전 적용할 수동 마이그레이션입니다. 이 작업에서 실제 DB에는 적용하지 않았습니다.

## 아직 확정하지 않은 계약

- 고정 상태 6개의 전체 이름과 타입이 제공되지 않아 states를 Map<String, JsonNode>로 보존합니다. boolean/null을 문자열로 변환해 응답하지 않습니다.
- additionalObjects의 스키마/확정 규칙이 없으므로 JSON 배열 그대로 보존·반환하며 확정 탐지 객체로 승격하지 않습니다.
- bbox는 유한한 비음수 좌표와 양의 크기를 검증합니다. 원본 좌표계 및 최대 크기는 실제 AI 명세로 확인해야 합니다.
- V2 /finalize, 객체별 사용자 수정/삭제/추가, 객체별 가이드 API는 이번 1차 수신 변경에 포함되지 않습니다.
- 기존 단일품목 상세 조회/확정 API는 V1 객체가 1개이고 추가 후보가 없는 경우만 허용합니다. 다중 객체를 임의로 하나 선택하는 것을 방지합니다.
