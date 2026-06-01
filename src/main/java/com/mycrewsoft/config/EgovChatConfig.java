package com.mycrewsoft.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * EgovChatConfig
 *
 * - Spring Context에서 ChatClient를 Bean으로 등록
 *
 * - OpenAI 기반 채팅 모델을 사용하기 위한 설정
 *
 * - Spring의 의존성 주입(DI)을 통해 어디서든 ChatClient를 사용할 수 있도록 함
 */
@Configuration
public class EgovChatConfig {

	/**
	 * ChatClient Bean 등록
	 *
	 * @param openAiChatModel OpenAI 기반 채팅 모델 Spring이 자동으로 주입해줌
	 * @return ChatClient 인스턴스
	 *
	 *         사용 예: - ChatController에서 의존성 주입 받아 사용 -
	 *         chatClient.prompt().user("안녕")... 형태로 AI 채팅 수행
	 */
	@Bean
	protected ChatClient chatClient(

			OpenAiChatModel openAiChatModel

	) {
		// ChatClient 생성
		// Builder 패턴 사용
		// openAiChatModel: OpenAI GPT 계열 모델 사용
		return ChatClient.builder(openAiChatModel).build();
	}
}
