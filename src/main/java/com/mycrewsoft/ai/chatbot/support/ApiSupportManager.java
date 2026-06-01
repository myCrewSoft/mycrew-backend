package com.mycrewsoft.ai.chatbot.support;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * ApiSupportManager
 *
 * 실시간 채팅/스트리밍에서 "Stop 버튼" 클릭을 관리하는 컴포넌트.
 *
 * 역할:
 *
 * 1. 클라이언트가 Stop 요청(requestId 기반)을 보냈는지 상태를 저장
 *
 * 2. Stop 요청 여부 확인
 *
 * 3. 요청이 처리되거나 세션이 종료되면 Stop 플래그 제거
 *
 * 구현 특징:
 *
 * - 멀티스레드 환경에서도 안전하도록 ConcurrentHashMap 기반 Set 사용 - 로그 기록을 통해 디버깅 용이
 */
@Slf4j
@Component
public class ApiSupportManager {

	/**
	 * Stop 요청을 저장하는 Set
	 *
	 * ConcurrentHashMap.newKeySet() 사용:
	 *
	 * - 멀티스레드 환경에서 안전하게 Stop 요청을 추가/삭제 가능
	 *
	 * - Set이므로 중복된 requestId 추가 방지
	 */
	private final Set<String> stopRequests = ConcurrentHashMap.newKeySet();

	/**
	 * Stop 플래그 설정
	 *
	 * 클라이언트가 "중단" 요청을 보내면 해당 requestId를 Set에 추가
	 *
	 * @param requestId 스트리밍 요청 식별 ID
	 */
	public void requestStop(String requestId) {
		log.debug("requestStop : {}", requestId); // 디버깅용 로그
		stopRequests.add(requestId); // Stop 요청 추가
	}

	/**
	 * 중단 요청 여부 확인
	 *
	 * 서버에서 스트리밍 중에 이 메서드를 호출하여 해당 requestId가 Stop 요청이 있는지 확인
	 *
	 * - true이면 Flux.takeUntil() 등에서 스트리밍을 중단
	 *
	 * - false이면 계속 스트리밍 진행
	 *
	 * @param requestId 스트리밍 요청 식별 ID
	 * @return 중단 요청 여부 (true: 중단 필요, false: 계속 진행)
	 */
	public boolean shouldStop(String requestId) {
		log.debug("shouldStop : {}", requestId); // 디버깅용 로그
		return stopRequests.contains(requestId); // Set에 존재 여부 확인
	}

	/**
	 * 중단 요청 제거
	 *
	 * 스트리밍 종료 후 또는 Stop 요청 처리 후 플래그 제거
	 *
	 * - 메모리 누수 방지 - 같은 requestId 재사용 가능
	 *
	 * @param requestId 스트리밍 요청 식별 ID
	 */
	public void clear(String requestId) {
		log.debug("clear : {}", requestId); // 디버깅용 로그
		stopRequests.remove(requestId); // Set에서 제거
	}
}
