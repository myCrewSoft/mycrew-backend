# MyCrew 부하 테스트 실행 가이드

이 구성은 다음을 한 번에 다룹니다.

- Spring Boot Actuator + Prometheus 지표 수집
- Grafana 대시보드
- k6 API 시나리오
- JMeter API 시나리오
- nGrinder Controller/Agent 및 Groovy 시나리오

## 1. 백엔드 실행

백엔드를 먼저 실행합니다. 기본 타깃은 `http://localhost:80`입니다.

```powershell
mvn spring-boot:run
```

Actuator 확인:

```text
http://localhost/actuator/health
http://localhost/actuator/prometheus
```

## 2. Prometheus, Grafana, nGrinder 실행

```powershell
cd docs/performance/load-test
docker compose up -d
```

nGrinder는 `3.5.8` 이미지를 사용합니다. `3.5.9` Agent는 Java 11로 실행되며 nGrinder Local DNS provider가 Java 8 내부 API인 `sun.net.spi.nameservice.NameService`를 찾다가 worker가 종료될 수 있습니다.

Agent 컨테이너의 Controller 주소는 `ngrinder-controller:80`으로 둡니다. 이 값은 Agent 패키지를 내려받는 HTTP 포트이며, worker 통신 포트 `16001`이 아닙니다.

접속 주소:

| 도구 | URL | 기본 계정 |
| --- | --- | --- |
| Prometheus | http://localhost:9090 | 없음 |
| Grafana | http://localhost:3000 | admin / admin |
| nGrinder | http://localhost:8300 | admin / admin |

Grafana에는 `MyCrew Spring Boot Load Test` 대시보드가 자동 등록됩니다.

## 3. k6 실행

로컬에 k6가 설치되어 있으면:

```powershell
cd docs/performance/load-test
$env:BASE_URL="http://localhost"
$env:EMP_ID="1234"
$env:PASSWORD="1234"
$env:K6_VUS="50"
$env:K6_DURATION="5m"
k6 run .\k6\mycrew-api-scenario.js
```

Docker로 실행하려면:

```powershell
cd docs/performance/load-test
docker run --rm `
  -e BASE_URL=http://192.168.35.113 `
  -e EMP_ID=1234 `
  -e PASSWORD=1234 `
  -e K6_VUS=50 `
  -e K6_DURATION=5m `
  -v "${PWD}\k6:/scripts" `
  grafana/k6 run /scripts/mycrew-api-scenario.js
```

## 4. JMeter 실행

GUI로 열어서 확인하거나, CLI로 실행합니다.

```powershell
cd docs/performance/load-test
New-Item -ItemType Directory -Force .\results | Out-Null
jmeter -n `
  -t .\jmeter\mycrew-api-load.jmx `
  -l .\results\mycrew-api-load.jtl `
  -e `
  -o .\results\mycrew-api-load-report `
  -Jhost=localhost `
  -Jport=80 `
  -JempId=1234 `
  -Jpassword=1234 `
  -Jusers=50 `
  -Jramp=30 `
  -Jloops=10
```

## 5. nGrinder 실행

1. `http://localhost:8300` 접속
2. `admin / admin` 로그인
3. Script 메뉴에서 `ngrinder/MyCrewApiScenario.groovy` 업로드
4. 테스트 생성 후 Agent, Vuser, Duration 설정
5. Script properties에 아래 값을 지정

```properties
mycrew.baseUrl=http://192.168.35.113
mycrew.empId=1234
mycrew.password=1234
mycrew.thinkTimeMs=1000
```

첫 실행은 아래처럼 작게 시작합니다.

| 항목 | 권장 초기값 |
| --- | --- |
| Agent | 1 |
| Processes | 1 |
| Threads | 1~3 |
| Duration | 30~60초 |
| Run Count | 0 |

정상 응답이 확인되면 Threads를 5, 10, 20 순서로 올립니다. `Connection refused`가 다시 나오면 테스트를 중단하고 `mycrew.baseUrl`, Spring 서버 실행 상태, Prometheus의 `up` 여부를 먼저 확인합니다. 로컬 Eclipse 실행 서버는 처음부터 큰 Vuser로 올리지 말고 `mycrew.thinkTimeMs=1000` 이상을 유지합니다.

Docker Desktop 환경이 아니어서 `host.docker.internal`이 동작하지 않으면 `mycrew.baseUrl`을 백엔드 서버의 실제 IP로 바꿉니다.

### Local DNS provider 오류가 나는 경우

아래 오류가 보이면 Agent 이미지가 Java 11 이상으로 실행 중인 상태입니다.

```text
net.grinder.engine.common.EngineException: Setting of Local DNS provider failed
Caused by: java.lang.ClassNotFoundException: sun.net.spi.nameservice.NameService
```

현재 compose 파일은 Java 8 기반 `ngrinder/controller:3.5.8`, `ngrinder/agent:3.5.8`로 고정되어 있습니다. 기존에 `3.5.9` 컨테이너를 띄웠다면 재생성합니다.

```powershell
docker compose down
docker compose up -d
```

## 6. 권장 측정 순서

| 단계 | 부하 | 목적 |
| --- | --- | --- |
| Smoke | 1~5 VU, 1분 | 인증/주요 API 정상 여부 확인 |
| Load | 50 VU, 5~10분 | 예상 사용량 응답시간 확인 |
| Stress | 100 -> 300 VU 단계 증가 | 한계 지점 확인 |
| Soak | 50~100 VU, 30분 이상 | 메모리/커넥션 누수 확인 |

주요 기준:

| 지표 | 1차 목표 |
| --- | --- |
| 에러율 | 1% 미만 |
| p95 응답시간 | 1초 미만 |
| 평균 응답시간 | 300~500ms 이하 |
| DB 커넥션 | pending connection 0 유지 |
| JVM | 장시간 테스트 중 heap/GC 급증 없음 |
