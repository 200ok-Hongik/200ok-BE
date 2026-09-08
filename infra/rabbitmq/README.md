# 기존 EB 서버에 RabbitMQ 함께 실행

새 Amazon MQ, EC2, 디스크를 생성하지 않습니다. 기존 t3.micro의 CPU/메모리/디스크를 공유합니다. 별도 브로커 리소스 요금은 없지만 기존 AWS 서비스 요금과 해당되는 통신/스토리지 사용량은 유지됩니다.

## 배포 동작

`.platform/hooks/predeploy/20-local-rabbitmq.sh`가 기존 서버에서 수행됩니다.

1. 전체 메모리 900,000KiB 이상, /var 디스크 여유 3GiB 이상, 최초 설치 시 가용 메모리 384MiB 이상인지 확인. 부족하면 오류로 배포를 중단하며 인스턴스를 확대하거나 볼륨을 추가하지 않습니다.
2. Amazon Linux의 Docker 패키지 설치/실행.
3. 로컬 브로커 전용 비밀번호를 생성해 root 전용 파일에 보관. 외부로 출력하지 않습니다.
4. 공식 `rabbitmq:4.2-alpine` 컨테이너를 기존 EC2에 실행. RAM 상한 256MiB, swap 추가 사용 금지, CPU 0.5, 로그 크기 제한.
5. 포트는 `127.0.0.1:5672`만 바인딩. 보안 그룹을 변경하지 않고 관리 UI도 공개하지 않습니다.
6. 기동 확인 후 `/etc/ssok/rabbitmq.properties`에 백엔드 연결 정보 저장.
7. Procfile이 해당 파일을 읽습니다. Java heap 상한 320MiB, direct memory 상한 64MiB로 제한합니다. 전체 프로세스 메모리는 heap 외 영역도 사용합니다.

## AWS 환경변수

새 RabbitMQ 환경변수를 직접 입력할 필요가 없습니다. 브로커가 준비된 뒤 hook이 다음 값을 서버 로컬 파일로 제공합니다.

- app.rabbitmq.enabled=true
- spring.rabbitmq.host=127.0.0.1
- spring.rabbitmq.port=5672
- spring.rabbitmq.username=ssok_backend
- spring.rabbitmq.password=(자동 생성)
- spring.rabbitmq.virtual-host=/
- spring.rabbitmq.ssl.enabled=false (서버 내부 loopback만 사용)

기존 `AI_SERVER_URL=https://two00ok-ai.onrender.com`, DB, OAuth 설정은 유지합니다. 이전에 직접 `SPRING_RABBITMQ_*`, `SPRING_APPLICATION_JSON` 또는 커맨드라인 RabbitMQ 설정을 넣었다면 로컬 파일보다 우선할 수 있으므로 충돌 여부를 확인합니다.

## 운영 제한

- 이 구성은 비용을 우선한 개발용 단일 서버 구성입니다. RabbitMQ와 앱이 같은 서버에 있어 한 서버 장애로 함께 중단됩니다.
- Docker volume은 같은 EC2에서 재배포/재시작 시 유지됩니다. EB 인스턴스 교체 시 브로커 데이터는 사라질 수 있으며 DB에 남은 QUEUED 작업은 relay가 새 큐에 재발행합니다.
- 무중단·정확히 한 번 처리 보장은 없습니다. 워커의 결과 저장 직후 종료 시 중복 스캔 생성 가능성은 기존 문서를 참조하세요.
- RabbitMQ 디스크 알람은 전체 디스크 사용 상한이 아닙니다. 큐 적체/DB 작업 수/EC2 메모리를 관찰해야 합니다.
- 이 hook은 같은 이름의 컨테이너가 있으면 재사용하고 삭제하지 않습니다. 기존 컨테이너의 이미지/제한 변경은 별도 작업입니다.

## 확인

배포 완료 후 Swagger 새로고침 → POST `/api/ai/analysis` 202 + jobId → GET `/api/ai/analysis/{jobId}` COMPLETED와 결과를 확인합니다. 자원 검사에서 실패하면 로그의 실측값을 보고 다음 결정을 합니다. 추가 유료 리소스는 자동 생성하지 않습니다.

## 중단/롤백

이전 애플리케이션 버전으로 롤백해도 컨테이너/volume은 삭제하지 않습니다. 별도 중단이 필요하면 서버에서 `docker stop ssok-rabbitmq`를 실행하고 /etc/ssok/rabbitmq.properties의 활성화 값을 false로 바꾼 뒤 앱을 재시작합니다. 데이터 삭제는 별도 확인 후 진행합니다.
