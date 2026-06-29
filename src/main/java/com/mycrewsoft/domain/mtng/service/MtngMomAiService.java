package com.mycrewsoft.domain.mtng.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.mycrewsoft.domain.mtng.mapper.MtngMapper;
import com.mycrewsoft.domain.mtng.mapper.MtngMomMapper;
import com.mycrewsoft.domain.mtng.vo.MtngDetailVO;
import com.mycrewsoft.domain.mtng.vo.MtngPtcptDetailVO;
import com.mycrewsoft.domain.mtng.vo.mom.MtngMomHistVO;
import com.mycrewsoft.domain.mtng.vo.mom.MtngMomVO;
import com.mycrewsoft.domain.video.mapper.VideoConfMapper;
import com.mycrewsoft.domain.video.vo.VideoChatLogVO;
import com.mycrewsoft.ai.chatbot.service.PromptService;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class MtngMomAiService {

        private final ChatClient chatClient;
        private final MtngMapper mtngMapper;
        private final MtngMomMapper mtngMomMapper;
        private final VideoConfMapper videoConfMapper;

        @Autowired
        @Qualifier("promptMeetingService")
        private PromptService promptMeetingService;

        // AI 초안 생성 후 draftCn 반환 (최초 생성, 재생성 공통 사용)
        @Async
        public String generateDraft(Long vconfId, Long mtngId) {
                MtngDetailVO detailVO = mtngMapper.selectMtngDetailByVconfId(vconfId);
                List<MtngPtcptDetailVO> ptcptList = mtngMapper.selectMtngPtcptDetailList(mtngId);

                String reference = buildReference(vconfId, detailVO, ptcptList);
                String question = "위 대화 로그를 기반으로 회의록 HTML을 작성하십시오.";

                return chatClient.prompt()
                                .user(promptMeetingService.build(question, reference))
                                .call()
                                .content();
        }

        // 회의록 AI 초안 재생성 (실패 시 수동 재시도, 수정 이력 저장 포함)
        public void regenerateAiDraft(Long mtngId, Long empId) {

                MtngDetailVO mtngVO = mtngMapper.selectMtngDetail(mtngId);
                if (mtngVO == null)
                        throw new CustomException(ErrorCode.MTNG_NOT_FOUND);
                if (!mtngVO.getCrtrId().equals(empId))
                        throw new CustomException(ErrorCode.ACCESS_DENIED);

                String draftCn = generateDraft(mtngVO.getVconfId(), mtngId);

                MtngMomVO momVO = mtngMomMapper.selectMomByMtngId(mtngId);
                if (momVO == null) {
                        // 회의록 없으면 새로 INSERT
                        MtngMomVO newMomVO = MtngMomVO.builder()
                                        .mtngId(mtngId)
                                        .momCn(draftCn)
                                        .momSttusCd("01")
                                        .build();
                        mtngMomMapper.createMtngMom(newMomVO);
                } else {
                        // 기존 회의록 있으면 이력 저장 후 UPDATE
                        MtngMomHistVO histVO = MtngMomHistVO.builder()
                                        .momId(momVO.getMomId())
                                        .momCn(momVO.getMomCn())
                                        .edtrId(empId)
                                        .build();
                        mtngMomMapper.createMtngMomHist(histVO);

                        momVO.setMomCn(draftCn);
                        mtngMomMapper.updateMtngMom(momVO);
                }
        }

        // 대화 로그 + 회의 정보를 AI 프롬프트용 텍스트로 조합
        private String buildReference(Long vconfId, MtngDetailVO detailVO, List<MtngPtcptDetailVO> ptcptList) {
                List<VideoChatLogVO> chatLogs = videoConfMapper.selectChatLogsByVconfId(vconfId);

                String ptcptNames = ptcptList.stream()
                                .map(MtngPtcptDetailVO::getEmpNm)
                                .collect(Collectors.joining(", "));

                // 1단계: 대화 로그를 30줄씩 청크로 분할
                List<String> logLines = chatLogs.stream()
                                .map(log -> "[" + log.getMbrId() + "] " + log.getSpkngCn())
                                .collect(Collectors.toList());

                int chunkSize = 30;
                StringBuilder summaries = new StringBuilder();

                // 2단계: 청크별 LLM 요약 호출
                for (int i = 0; i < logLines.size(); i += chunkSize) {
                        List<String> chunk = logLines.subList(i, Math.min(i + chunkSize, logLines.size()));
                        String chunkText = String.join("\n", chunk);

                        String summary = chatClient.prompt()
                                        .user(promptMeetingService.buildChunkSummary(chunkText))
                                        .call()
                                        .content();

                        summaries.append("[요약 ").append(i / chunkSize + 1).append("]\n");
                        summaries.append(summary).append("\n\n");
                }

                // 3단계: 요약 결과로 reference 구성
                return """
                                회의명: %s
                                일시: %s ~ %s
                                장소: %s
                                주재자: %s
                                참석자: %s

                                [대화 로그 요약]
                                %s
                                """.formatted(
                                detailVO.getMtngNm(),
                                detailVO.getBeginDt(),
                                detailVO.getEndDt(),
                                detailVO.getConfRmNm() != null ? detailVO.getConfRmNm() : "온라인",
                                detailVO.getCrtrNm(),
                                ptcptNames,
                                summaries.toString());
        }

}