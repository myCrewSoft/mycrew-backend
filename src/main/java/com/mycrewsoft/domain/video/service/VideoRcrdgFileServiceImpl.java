package com.mycrewsoft.domain.video.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.common.util.FileUtil;
import com.mycrewsoft.domain.file.constant.FileConstants;
import com.mycrewsoft.domain.file.mapper.FileMapper;
import com.mycrewsoft.domain.file.service.FileService;
import com.mycrewsoft.domain.file.vo.FileClsfVo;
import com.mycrewsoft.domain.file.vo.FileDtlVo;
import com.mycrewsoft.domain.mtng.mapper.MtngMapper;
import com.mycrewsoft.domain.mtng.vo.MtngDetailVO;
import com.mycrewsoft.domain.video.mapper.VideoConfMapper;
import com.mycrewsoft.domain.video.vo.VideoRcrdgVO;
import com.mycrewsoft.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VideoRcrdgFileServiceImpl implements VideoRcrdgFileService {

    private final FileMapper fileMapper;
    private final FileService fileService;
    private final MtngMapper mtngMapper;
    private final VideoConfMapper videoConfMapper;

    @Value("${file.upload-path}")
    private String uploadPath;

    private static final Set<String> ALLOWED_AUDIO_EXTENSIONS = Set.of("webm", "mp3", "wav", "ogg");
    private static final long MAX_AUDIO_SIZE = 500 * 1024 * 1024L;

    @Override
    @Transactional
    public Long upload(Long vconfId, MultipartFile file) {
        Long empId = SecurityUtil.getCurrentEmpId();
        validateUploadAccess(vconfId, empId);

        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        String originalFileName = file.getOriginalFilename();
        String extension = FileUtil.getExtension(originalFileName);

        if (!ALLOWED_AUDIO_EXTENSIONS.contains(extension)) {
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        }

        if (file.getSize() > MAX_AUDIO_SIZE) {
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        String saveFileNm = FileUtil.generateStoredFileName(originalFileName);
        Path uploadDirectory = Paths.get(uploadPath).toAbsolutePath().normalize();
        Path savePath = uploadDirectory.resolve(saveFileNm).normalize();

        if (!savePath.startsWith(uploadDirectory)) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        try {
            Files.createDirectories(uploadDirectory);
            file.transferTo(savePath);
            registerRollbackCleanup(savePath);
        } catch (Exception e) {
            deleteQuietly(savePath);
            log.error("[녹취록] 실제 파일 저장 실패 - vconfId: {}", vconfId, e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }

        FileClsfVo clsfVO = new FileClsfVo();
        clsfVO.setAtchFileBizCd(FileConstants.VOICE);
        fileMapper.insertClsf(clsfVO);

        FileDtlVo dtlVO = new FileDtlVo();
        dtlVO.setAtchFileId(clsfVO.getAtchFileId());
        dtlVO.setOrgnlFileNm(originalFileName);
        dtlVO.setAtchFileTyCd("03");
        dtlVO.setSavePathNm(uploadDirectory.toString());
        dtlVO.setSaveFileNm(saveFileNm);
        dtlVO.setFileExtsn(extension);
        dtlVO.setFileSz(file.getSize());
        dtlVO.setFrstRgstrId(empId);
        fileMapper.insertDtl(dtlVO);

        VideoRcrdgVO rcrdgVO = VideoRcrdgVO.builder()
                .vconfId(vconfId)
                .rcrdgAtchFileId(dtlVO.getAtchFileDtlId())
                .build();
        videoConfMapper.insertRcrdg(rcrdgVO);

        return dtlVO.getAtchFileDtlId();
    }

    @Override
    public VideoRcrdgFileResource getFile(Long vconfId, Long atchFileDtlId) {
        Long empId = SecurityUtil.getCurrentEmpId();
        if (videoConfMapper.countAccessibleRcrdg(vconfId, atchFileDtlId, empId) == 0) {
            throw new CustomException(ErrorCode.VIDEO_ACCESS_DENIED);
        }

        FileDtlVo dtlVO = fileMapper.selectDtlById(atchFileDtlId);
        if (dtlVO == null || "Y".equals(dtlVO.getDelYn()) || !"03".equals(dtlVO.getAtchFileTyCd())) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND);
        }

        Resource resource = fileService.download(atchFileDtlId);
        return new VideoRcrdgFileResource(
                resource,
                dtlVO.getOrgnlFileNm(),
                resolveAudioContentType(dtlVO.getFileExtsn()));
    }

    private void validateUploadAccess(Long vconfId, Long empId) {
        MtngDetailVO detailVO = mtngMapper.selectMtngDetailByVconfId(vconfId);
        if (detailVO == null) {
            throw new CustomException(ErrorCode.VIDEO_CONF_NOT_FOUND);
        }
        if (!detailVO.getCrtrId().equals(empId)) {
            throw new CustomException(ErrorCode.VIDEO_ACCESS_DENIED);
        }
    }

    private MediaType resolveAudioContentType(String extension) {
        return switch (extension) {
            case "webm" -> MediaType.parseMediaType("audio/webm");
            case "mp3" -> MediaType.parseMediaType("audio/mpeg");
            case "wav" -> MediaType.parseMediaType("audio/wav");
            case "ogg" -> MediaType.parseMediaType("audio/ogg");
            default -> throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        };
    }

    private void registerRollbackCleanup(Path savePath) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != TransactionSynchronization.STATUS_COMMITTED) {
                    deleteQuietly(savePath);
                }
            }
        });
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (Exception cleanupException) {
            log.warn("[녹취록] 롤백 파일 삭제 실패 - path: {}", path, cleanupException);
        }
    }
}
