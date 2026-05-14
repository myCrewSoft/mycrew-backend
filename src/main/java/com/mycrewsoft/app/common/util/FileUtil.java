package com.mycrewsoft.app.common.util;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.mycrewsoft.app.common.exception.CustomException;
import com.mycrewsoft.app.common.exception.ErrorCode;

import java.util.Set;
import java.util.UUID;

/**
 * 파일 처리 관련 유틸리티 클래스.
 * 파일명 처리는 Commons IO FilenameUtils 를 사용한다.
 * MIME 타입 검증은 확장자 기반으로 수행한다.
 *
 * 모든 메서드는 static 이며, 인스턴스 생성을 금지한다.
 */
public final class FileUtil {

    private FileUtil() {}

    /** 업로드 허용 이미지 확장자 목록 */
    private static final Set<String> ALLOWED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");

    /** 업로드 허용 문서 확장자 목록 */
    private static final Set<String> ALLOWED_DOCUMENT_EXTENSIONS = Set.of("pdf", "docx", "xlsx");

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024L; // 10MB
    private static final long MAX_DOCUMENT_SIZE = 50 * 1024 * 1024L; // 50MB

    /**
     * UUID 기반의 고유한 저장용 파일명을 생성한다.
     * 원본 파일명 저장 시 한글 깨짐, 덮어쓰기, 경로 탐색 공격이 발생할 수 있다.
     * 예) "내 프로필.jpg" → "a1b2c3d4-xxxx.jpg"
     *
     * @param originalFileName 원본 파일명
     * @return UUID 기반의 저장용 파일명
     * @throws CustomException 확장자가 없는 파일이면 INVALID_FILE_TYPE
     */
    public static String generateStoredFileName(String originalFileName) {
        String extension = FilenameUtils.getExtension(originalFileName);
        if (StringUtils.isBlank(extension))
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
        
        return UUID.randomUUID() + "." + extension.toLowerCase();
    }

    /**
     * 파일명에서 확장자를 추출한다. Windows/Unix 경로 구분자를 모두 처리한다.
     * 예) "photo.jpg" → "jpg" / "report.2024.pdf" → "pdf"
     *
     * @param fileName 확장자를 추출할 파일명
     * @return 소문자 확장자 문자열. 확장자 없으면 빈 문자열
     */
    public static String getExtension(String fileName) {
        return FilenameUtils.getExtension(fileName).toLowerCase();
    }

    /**
     * 파일 크기를 사람이 읽기 좋은 단위로 변환한다.
     * 예) 1048576 → "1.0 MB"
     *
     * @param bytes 변환할 파일 크기 (바이트 단위)
     * @return 단위가 포함된 파일 크기 문자열
     */
    public static String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));

        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }

    /**
     * 이미지 파일 유효성 검증. jpg, jpeg, png, gif, webp 허용. (최대 10MB)
     * 검증 순서: 파일 존재 → 크기 → 확장자
     *
     * @param file 검증할 MultipartFile
     * @throws CustomException 파일이 없으면 FILE_UPLOAD_FAILED
     * @throws CustomException 크기 초과 시 FILE_SIZE_EXCEEDED
     * @throws CustomException 허용되지 않는 확장자면 INVALID_FILE_TYPE
     */
    public static void validateImageFile(MultipartFile file) {
        validateNotEmpty(file);
        validateSize(file, MAX_IMAGE_SIZE);
        validateExtension(file, ALLOWED_IMAGE_EXTENSIONS);
    }

    /**
     * 문서 파일 유효성 검증. pdf, docx, xlsx 허용. (최대 50MB)
     * 검증 순서: 파일 존재 → 크기 → 확장자
     *
     * @param file 검증할 MultipartFile
     * @throws CustomException 파일이 없으면 FILE_UPLOAD_FAILED
     * @throws CustomException 크기 초과 시 FILE_SIZE_EXCEEDED
     * @throws CustomException 허용되지 않는 확장자면 INVALID_FILE_TYPE
     */
    public static void validateDocumentFile(MultipartFile file) {
        validateNotEmpty(file);
        validateSize(file, MAX_DOCUMENT_SIZE);
        validateExtension(file, ALLOWED_DOCUMENT_EXTENSIONS);
    }

    /**
     * 파일이 null 이거나 비어있는지 확인한다.
     *
     * @throws CustomException 파일이 없으면 FILE_UPLOAD_FAILED
     */
    private static void validateNotEmpty(MultipartFile file) {
        if (file == null || file.isEmpty())
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
    }

    /**
     * 파일 크기가 허용 최대 크기를 초과하는지 확인한다.
     *
     * @param file    크기를 확인할 파일
     * @param maxSize 허용 최대 바이트 크기
     * @throws CustomException 크기 초과 시 FILE_SIZE_EXCEEDED
     */
    private static void validateSize(MultipartFile file, long maxSize) {
        if (file.getSize() > maxSize)
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
    }

    /**
     * 파일 확장자가 허용 목록에 포함되는지 확인한다.
     * MultipartFile 의 원본 파일명에서 확장자를 추출해 소문자로 비교한다.
     *
     * @param file              확인할 파일
     * @param allowedExtensions 허용 확장자 목록
     * @throws CustomException 허용되지 않는 확장자면 INVALID_FILE_TYPE
     */
    private static void validateExtension(MultipartFile file, Set<String> allowedExtensions) {
        String extension = getExtension(file.getOriginalFilename());
        if (!allowedExtensions.contains(extension))
            throw new CustomException(ErrorCode.INVALID_FILE_TYPE);
    }
}