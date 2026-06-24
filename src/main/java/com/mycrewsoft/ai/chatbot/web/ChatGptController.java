package com.mycrewsoft.ai.chatbot.web;

import java.time.Duration;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycrewsoft.ai.chatbot.service.PromptService;
import com.mycrewsoft.ai.chatbot.support.ApiSupportManager;
import com.mycrewsoft.domain.project.service.ProjectService;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;

/**
 * ChatGptController
 *
 * - Spring AI ChatClient를 사용하여 AI와 채팅 스트리밍 제공
 *
 * - StopManager를 통해 클라이언트에서 중단 요청
 *
 *
 * 처리 - SSE (Server-Sent Events)를 활용한 실시간 스트리밍
 */
@Slf4j
@RestController
public class ChatGptController {
	@Autowired
	private ProjectService projectService;

	@Autowired
	private ChatClient chatClient; // Spring AI ChatClient (AI 모델 호출용)

	@Autowired
	private ApiSupportManager apiSupportManager; // Stop 요청 관리용 컴포넌트
	
	@Autowired
	@Qualifier("promptApprovalService")
	private PromptService promptApproval;
	@Autowired
	@Qualifier("promptMeetingService")
	private PromptService promptMeeting;
	@Autowired
	@Qualifier("promptReportService")
	private PromptService promptReport;
	@Autowired
	@Qualifier("promptPostRiskService")
	private PromptService promptPostRisk;
	@Autowired
	@Qualifier("promptChatbotService")
	private PromptService promptChatbot;
	/**
	 * 헬스 체크용 엔드포인트
	 *
	 * - 클라이언트 또는 모니터링 서버에서 호출 가능
	 *
	 * - 단순히 서버가 살아 있는지 확인용 예: GET /api/ping
	 */
	@GetMapping("/ping")
	public String ping() {
		return "pong";
	}

	/**
	 * SSE 기반 채팅 스트리밍 엔드포인트
	 *
	 * - 클라이언트가 메시지를 보내면 AI 응답을 실시간으로 스트리밍
	 *
	 * - StopManager와 연동하여 Stop 버튼 클릭 시 스트리밍 중단 가능
	 *
	 * 호출 예: /api/chat/stream?message=안녕&requestId=xxxx 반환: text/event-stream
	 *
	 * (EventSource 수신용)
	 *
	 * @param message   사용자가 입력한 메시지
	 * @param requestId 특정 요청 식별용 ID (Stop 기능에 사용)
	 * @return Flux<String> AI 응답 스트리밍
	 */
	@GetMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<String> stream(
		@RequestParam String message,
		@RequestParam String requestId,
		@RequestParam String aiType,
		@RequestParam(required = false) Long boardId,
		@RequestParam(required = false) Long projId
	) {
		log.debug("requestId : {}", requestId);
		
		// Stop 플래그 초기화
		// 이전에 Stop 요청이 남아있으면 제거하여 새로운 요청을 받을 준비
		apiSupportManager.clear(requestId);

		String aiMessage = " 질문이 없어요. ";
		
		if ("APPR".equalsIgnoreCase(aiType)) {
			aiMessage = promptApproval.build(message);
		} else if ("MEET".equalsIgnoreCase(aiType)) {
			aiMessage = promptMeeting.build(message);
		} else if ("REPT".equalsIgnoreCase(aiType)) {
			Long parsedProjId = projId;
		    String cleanMessage = message;
		    if (parsedProjId == null && message.startsWith("projId:")) {
		        String[] parts = message.split(" ", 2);
		        try {
		            parsedProjId = Long.parseLong(parts[0].replace("projId:", "").trim());
		            cleanMessage = parts.length > 1 ? parts[1] : "";
		        } catch (NumberFormatException e) {
		            log.warn("projId 파싱 실패: {}", parts[0]);
		        }
		    }
		    if (parsedProjId != null) {
		        String reference = projectService.buildAiReference(parsedProjId);
		        aiMessage = promptReport.build(cleanMessage, reference);
		    } else {
		        aiMessage = promptReport.build(message);
		    }
		} else if ("PORK".equalsIgnoreCase(aiType)) {
			if (boardId == null) {
		        throw new IllegalArgumentException("게시글 ID가 필요합니다.");
		    }
			aiMessage = promptPostRisk.build(message, boardId.toString());
		} else {
			aiMessage = promptChatbot.build(message);
		}
		
		// Spring AI ChatClient를 사용하여 사용자 메시지 전송
		// Flux<String>으로 AI의 응답을 스트리밍
		Flux<String> origin = chatClient.prompt()
				.user(
					aiMessage
				).stream() // 스트리밍 모드 활성화
				.content() // 응답 내용 추출
				.delayElements(Duration.ofMillis(20)); // 부드러운 전송 위해 딜레이 추가

		return origin
				// Stop 버튼 클릭 시 스트리밍 중단
				.takeUntil(chunk -> apiSupportManager.shouldStop(requestId))

				// 브라우저 종료나 네트워크 단절 시 IOException 발생 가능
				.onErrorResume(ex -> {
					// IOException이나 기타 예외 발생 시 로그만 남기고 정상 종료
					log.warn("Stream aborted: {}", ex.getMessage());
					return Flux.empty(); // 스트림 종료
				})

				// 스트림 종료 시 항상 Stop 플래그 초기화
				.doFinally(signalType -> apiSupportManager.clear(requestId));
	}

	/**
	 * 클라이언트 Stop 요청 엔드포인트
	 *
	 * - 사용자가 Stop 버튼 클릭 시 호출
	 *
	 * - StopManager에 Stop 요청 등록
	 *
	 * 호출 예: POST /api/chat/stop?requestId=xxxx
	 *
	 * @param requestId 중단할 요청 식별용 ID
	 * @return "stopping" 문자열 반환
	 */
	@PostMapping("/chat/stop")
	public String stop(@RequestParam String requestId) {
		// 해당 requestId에 대해 Stop 요청 등록
		apiSupportManager.requestStop(requestId);
		return "stopping";
	}
}
