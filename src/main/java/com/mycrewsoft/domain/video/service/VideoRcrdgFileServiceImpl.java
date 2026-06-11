package com.mycrewsoft.domain.video.service;

import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.util.FileUtil;
import com.mycrewsoft.domain.file.constant.FileConstants;
import com.mycrewsoft.domain.file.vo.FileClsfVo;
import com.mycrewsoft.domain.file.vo.FileDtlVo;
import com.mycrewsoft.domain.video.mapper.VideoRcrdgFileMapper;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoRcrdgFileServiceImpl implements VideoRcrdgFileService {

    private final VideoRcrdgFileMapper videoRcrdgFileMapper;

    @Value("${file.upload-path}")
    private String uploadPath;

    // 허용 오디오 확장자
    private static final Set<String> ALLOWED_AUDIO_EXTENSIONS = Set.of("webm", "mp3", "wav", "ogg");

    // 최대 파일 크기 500MB
    private static final long MAX_AUDIO_SIZE = 50 * 1024 * 1024L;

    @Override
    @Transactional
    public Long upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        String originalFileName = file.getOriginalFilename();
        String extension = FileUtil.getExtension(originalFileName);

        // 오디오 파일 확장자 검증
        if (!ALLOWED_AUDIO_EXTENSIONS.contains(extension)) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }

        // 파일 크기 검증
        if (file.getSize() > MAX_AUDIO_SIZE) {
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        // UUID 기반 저장 파일명 생성 후 로컬 디스크에 저장
        String saveFileNm = FileUtil.generateStoredFileName(originalFileName);
        try {
            Path savePath = Paths.get(uploadPath, saveFileNm);
            file.transferTo(savePath);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        // 분류 테이블 INSERT (04: 녹취록 비즈코드)
        FileClsfVo clsfVO = new FileClsfVo();
        clsfVO.setAtchFileBizCd(FileConstants.VOICE);
        videoRcrdgFileMapper.insertClsf(clsfVO);

        // 상세 테이블 INSERT
        FileDtlVo dtlVO = new FileDtlVo();
        dtlVO.setAtchFileId(clsfVO.getAtchFileId());
        dtlVO.setOrgnlFileNm(originalFileName);
        dtlVO.setAtchFileTyCd("03"); // 03: AUDIO
        dtlVO.setSavePathNm(uploadPath);
        dtlVO.setSaveFileNm(saveFileNm);
        dtlVO.setFileExtsn(extension);
        dtlVO.setFileSz(file.getSize());
        dtlVO.setFrstRgstrId(SecurityUtil.getCurrentEmpId());
        videoRcrdgFileMapper.insertDtl(dtlVO);

        // TB_VIDEO_RCRDG에서 참조할 ATCH_FILE_DTL_ID 반환
        return dtlVO.getAtchFileDtlId();
    }

    @Override
    public Resource getResource(Long atchFileId) {
        // 파일 정보 조회 및 삭제 여부 확인
        FileDtlVo dtlVO = videoRcrdgFileMapper.selectDtlByAtchFileId(atchFileId);
        if (dtlVO == null || "Y".equals(dtlVO.getDelYn())) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        // 실제 파일 존재 여부 확인
        Path filePath = Paths.get(dtlVO.getSavePathNm(), dtlVO.getSaveFileNm());
        if (!Files.exists(filePath)) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        // 생성 실패시 예외
        try {
            return new UrlResource(filePath.toUri());
        } catch (Exception e) {
            log.error("[녹취록] 파일 리소스 생성 실패 - atchFileId: {}", atchFileId, e);
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }
    }

    @Override
    public String getOriginalFileName(Long atchFileId) {
        // 파일 정보 조회 및 삭제 여부 확인
        FileDtlVo dtlVO = videoRcrdgFileMapper.selectDtlByAtchFileId(atchFileId);
        if (dtlVO == null || "Y".equals(dtlVO.getDelYn())) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }
        return dtlVO.getOrgnlFileNm();
    }
}