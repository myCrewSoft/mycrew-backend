# 메일 일괄 처리 성능 개선 결과

## 목적

메일 읽음 처리뿐 아니라 중요 메일 처리, 휴지통 이동, 휴지통 비우기에서도 기존 단건 루프 방식은 대상 메일마다 조회, 라벨 매핑 추가/삭제, 상태 업데이트, Gmail API 호출이 반복되어 대상 건수가 늘수록 응답 시간이 선형으로 증가했다.

이번 개선은 다음 네 가지 작업을 일괄 처리 구조로 변경했다.

- 읽음 처리: `UNREAD` 라벨을 Gmail `batchModify`와 로컬 DB set-based DELETE로 제거
- 중요 메일 처리: `IMPORTANT` 라벨을 Gmail `batchModify`와 로컬 DB set-based INSERT/DELETE로 반영
- 휴지통 이동: `TRASH` 라벨을 Gmail `batchModify`와 로컬 DB set-based INSERT로 반영
- 휴지통 비우기: Gmail `batchDelete`로 영구 삭제 요청을 묶고, 로컬 DB는 `MAIL_MSG_ID IN (...)` 배치 UPDATE로 삭제 처리

Oracle `IN` 조건과 Gmail `batchModify`의 요청당 1000건 제한을 고려해 1000건 단위 chunk로 처리한다.

## 적용 인덱스

```sql
CREATE INDEX IX_MAIL_LABEL_EMP_TYPE_USE
ON TB_MAIL_LABEL (EMP_ID, TRIM(LABEL_TYPE_CD), USE_YN);

CREATE INDEX IX_MAIL_LABEL_MAP_EMP_LABEL_MSG
ON TB_MAIL_LABEL_MAP (EMP_ID, MAIL_LABEL_ID, MAIL_MSG_ID);
```

`IX_MAIL_LABEL_EMP_TYPE_USE`는 사용자별 시스템 라벨 ID를 빠르게 찾기 위한 인덱스다. `TRIM(LABEL_TYPE_CD) = 'UNREAD' | 'IMPORTANT' | 'TRASH'` 조건을 매번 전체 스캔하지 않고, `EMP_ID`, 라벨 타입, 사용 여부 기준으로 바로 좁힌다.

`IX_MAIL_LABEL_MAP_EMP_LABEL_MSG`는 특정 사용자와 라벨에 속한 메일 매핑을 빠르게 삭제하거나 존재 여부를 확인하기 위한 인덱스다. 일괄 DELETE/INSERT의 `NOT EXISTS` 조건에서 `EMP_ID + MAIL_LABEL_ID + MAIL_MSG_ID` 접근을 지원해 라벨 매핑 테이블의 탐색 비용을 줄인다.

## 변경 파일

- `src/main/java/com/mycrewsoft/domain/mail/service/MailServiceImpl.java`
- `src/main/java/com/mycrewsoft/domain/mail/service/GoogleGmailClient.java`
- `src/main/java/com/mycrewsoft/domain/mail/service/WebClientGoogleGmailClient.java`
- `src/main/java/com/mycrewsoft/domain/mail/mapper/MailMapper.java`
- `src/main/resources/mapper/mail/MailMapper.xml`
- `src/test/java/com/mycrewsoft/domain/mail/service/MailServiceImplTest.java`
- `src/test/java/com/mycrewsoft/domain/mail/service/WebClientGoogleGmailClientTest.java`
- `src/test/java/com/mycrewsoft/perfomance/MailReadPerformanceTest.java`

사용자가 요청한 패키지명에 맞춰 성능 테스트 패키지는 `perfomance`로 작성했다.

## 측정 방법

성능 테스트는 실제 Oracle DB에 테스트용 메일, 라벨, 라벨 매핑을 생성한 뒤 같은 건수로 기존 단건 루프 방식과 개선된 일괄 처리 방식을 비교한다. 테스트 데이터는 하나의 트랜잭션 안에서 생성하고 마지막에 rollback하므로 DB에 남지 않는다.

- 기존 방식: 메일마다 `SELECT` 후 단건 DELETE/INSERT/UPDATE 반복
- 개선 방식: `MAIL_MSG_ID IN (...)` 기반 조회와 set-based DELETE/INSERT/UPDATE를 1000건 chunk 단위로 실행
- Gmail 네트워크 시간은 제외하고 로컬 DB 처리 병목만 비교
- Gmail API 호출 수는 2000건 기준 단건 2000회에서 1000건 chunk 2회로 감소

실행 명령:

```bash
mvn -q "-Dtest=MailReadPerformanceTest" "-Dmail.performance.enabled=true" "-Dmail.performance.target-count=2000" test
```

## 측정 결과

측정일: 2026-06-19

대상: 2,000건

| 시나리오 | 방식 | 처리 시간 | SQL 실행 횟수 | 처리 건수 | 개선 배율 |
| --- | --- | ---: | ---: | ---: | ---: |
| 읽음 처리 | 기존 단건 조회/삭제 | 1072.580 ms | 4,000회 | 2,000건 | - |
| 읽음 처리 | 개선 일괄 조회/삭제 | 109.128 ms | 4회 | 2,000건 | 9.83배 |
| 중요 메일 처리 | 기존 단건 조회/추가 | 1282.387 ms | 8,000회 | 2,000건 | - |
| 중요 메일 처리 | 개선 일괄 조회/추가 | 84.127 ms | 4회 | 2,000건 | 15.24배 |
| 휴지통 이동 | 기존 단건 조회/추가 | 1025.387 ms | 8,000회 | 2,000건 | - |
| 휴지통 이동 | 개선 일괄 조회/추가 | 60.648 ms | 4회 | 2,000건 | 16.91배 |
| 휴지통 비우기 | 기존 단건 조회/업데이트 | 626.638 ms | 4,000회 | 2,000건 | - |
| 휴지통 비우기 | 개선 일괄 조회/업데이트 | 101.910 ms | 4회 | 2,000건 | 6.15배 |

SQL 실행 횟수 감소율:

- 읽음 처리: 4,000회 -> 4회, 99.90% 감소
- 중요 메일 처리: 8,000회 -> 4회, 99.95% 감소
- 휴지통 이동: 8,000회 -> 4회, 99.95% 감소
- 휴지통 비우기: 4,000회 -> 4회, 99.90% 감소

## 이력서용 요약

메일 대량 처리에서 메일별 단건 조회/라벨 매핑 DML/Gmail API 호출 루프를 제거하고, Oracle 복합 인덱스 기반 set-based SQL과 Gmail batch API를 적용했다. 2,000건 기준 로컬 DB 처리에서 읽음 처리는 1072.580ms에서 109.128ms로 9.83배, 중요 메일 처리는 1282.387ms에서 84.127ms로 15.24배, 휴지통 이동은 1025.387ms에서 60.648ms로 16.91배, 휴지통 비우기는 626.638ms에서 101.910ms로 6.15배 개선했다. SQL 실행 횟수는 시나리오별 4,000~8,000회에서 4회로 줄여 99.90~99.95% 감소시켰다.

## 참고

- Gmail `users.messages.batchModify`는 여러 메시지의 라벨을 한 번에 추가/제거하며, 요청 본문에 `ids`, `addLabelIds`, `removeLabelIds`를 전달한다. 공식 문서 기준 `ids`는 요청당 최대 1000개다.
- Gmail 시스템 라벨 중 `TRASH`, `UNREAD`, `IMPORTANT`는 수동 적용 가능한 라벨이다.
- Gmail `users.messages.batchDelete`는 여러 메시지 ID를 요청 본문 `ids`로 받아 영구 삭제한다.

참고 문서:

- https://developers.google.com/workspace/gmail/api/reference/rest/v1/users.messages/batchModify
- https://developers.google.com/workspace/gmail/api/reference/rest/v1/users.messages/batchDelete
- https://developers.google.com/workspace/gmail/api/guides/labels
