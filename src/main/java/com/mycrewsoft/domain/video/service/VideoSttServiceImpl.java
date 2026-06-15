package com.mycrewsoft.domain.video.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.util.FileUtil;
import com.mycrewsoft.domain.mtng.mapper.MtngMapper;
import com.mycrewsoft.domain.mtng.vo.MtngDetailVO;
import com.mycrewsoft.domain.video.mapper.VideoConfMapper;
import com.mycrewsoft.domain.video.vo.VideoChatLogVO;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoSttServiceImpl implements VideoSttService {

    private final VideoConfMapper videoConfMapper;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final MtngMapper mtngMapper;

    @Value("${openai.api-key}")
    private String openAiApiKey;

    @Value("${file.upload-path}")
    private String uploadPath;

    // Whisper API 엔드포인트 (OpenAI 공식 음성 텍스트 변환 API)
    private static final String WHISPER_URL = "https://api.openai.com/v1/audio/transcriptions";

    @Override
    @Transactional
    public String transcribeAndSave(Long vconfId, MultipartFile audioChunk) {
        Long empId = SecurityUtil.getCurrentEmpId();

        // 회의 존재 여부 확인
        MtngDetailVO detailVO = mtngMapper.selectMtngDetailByVconfId(vconfId);
        if (detailVO == null) throw new CustomException(ErrorCode.VIDEO_CONF_NOT_FOUND);

        // 참여자만 STT 요청 가능
        boolean isPtcpt = mtngMapper.selectMtngPtcptDetailList(detailVO.getMtngId()).stream()
            .anyMatch(p -> p.getEmpId().equals(empId));
        if (!isPtcpt) throw new CustomException(ErrorCode.VIDEO_ACCESS_DENIED);

        // 프론트에서 받은 5초 청크 파일을 서버 임시 경로에 저장
        // Whisper API는 MultipartFile을 직접 받지 않고 실제 파일 경로가 필요하기 때문
        String tmpFileName = FileUtil.generateStoredFileName(audioChunk.getOriginalFilename());
        Path tmpPath = Paths.get(uploadPath, "tmp", tmpFileName);

        try {
            Files.createDirectories(tmpPath.getParent()); // tmp 디렉토리 없으면 자동 생성
            audioChunk.transferTo(tmpPath);               // 임시 파일로 저장
        } catch (IOException e) {
            log.error("[STT] 임시 파일 저장 실패 - vconfId: {}, empId: {}", vconfId, empId, e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        String transcribedText;
        try {
            // Authorization 헤더에 OpenAI API 키 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.setBearerAuth(openAiApiKey);

            // Whisper API 요청 바디 구성
            // file: 변환할 오디오 파일
            // model: whisper-1 (현재 유일한 Whisper 모델)
            // language: ko (한국어 지정 시 정확도 향상)
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new FileSystemResource(tmpPath));
            body.add("model", "whisper-1");
            body.add("language", "ko");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Whisper API 호출 → 응답은 {"text": "변환된 텍스트"} 형태의 JSON
            ResponseEntity<String> response = restTemplate.postForEntity(
                    WHISPER_URL, requestEntity, String.class);

            // JSON 응답에서 text 필드만 추출
            JsonNode root = objectMapper.readTree(response.getBody());
            transcribedText = root.path("text").asText();

        } catch (Exception e) {
            log.error("[STT] Whisper API 호출 실패 - vconfId: {}, empId: {}", vconfId, empId, e);
            throw new CustomException(ErrorCode.STT_TRANSCRIBE_FAILED);
        } finally {
            // 성공/실패 여부와 관계없이 임시 파일은 반드시 삭제
            try {
                Files.deleteIfExists(tmpPath);
            } catch (IOException e) {
                log.warn("[STT] 임시 파일 삭제 실패 - path: {}", tmpPath);
            }
        }

        // 변환된 텍스트를 대화 로그로 DB에 저장
        VideoChatLogVO chatLogVO = new VideoChatLogVO();
        chatLogVO.setVconfId(vconfId);
        chatLogVO.setMbrId(empId);
        chatLogVO.setSpkngCn(transcribedText);
        videoConfMapper.insertChatLog(chatLogVO);

        // 프론트에 텍스트 반환 → 화면에 자막처럼 표시
        return transcribedText;
    }
}