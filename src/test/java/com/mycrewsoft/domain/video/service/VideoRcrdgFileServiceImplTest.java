package com.mycrewsoft.domain.video.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

import com.mycrewsoft.common.exception.CustomException;
import com.mycrewsoft.common.exception.ErrorCode;
import com.mycrewsoft.domain.file.mapper.FileMapper;
import com.mycrewsoft.domain.file.service.FileService;
import com.mycrewsoft.domain.file.vo.FileDtlVo;
import com.mycrewsoft.domain.mtng.mapper.MtngMapper;
import com.mycrewsoft.domain.mtng.vo.MtngDetailVO;
import com.mycrewsoft.domain.video.mapper.VideoConfMapper;
import com.mycrewsoft.security.util.SecurityUtil;

@ExtendWith(MockitoExtension.class)
class VideoRcrdgFileServiceImplTest {

    @Mock
    private FileMapper fileMapper;

    @Mock
    private FileService fileService;

    @Mock
    private MtngMapper mtngMapper;

    @Mock
    private VideoConfMapper videoConfMapper;

    @InjectMocks
    private VideoRcrdgFileServiceImpl service;

    @Test
    void upload_회의생성자가아니면_파일저장전에차단한다() {
        Long vconfId = 10L;
        Long currentEmpId = 100L;
        MtngDetailVO detail = mock(MtngDetailVO.class);

        when(detail.getCrtrId()).thenReturn(200L);
        when(mtngMapper.selectMtngDetailByVconfId(vconfId)).thenReturn(detail);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(currentEmpId);

            assertThatThrownBy(() -> service.upload(vconfId, null))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.VIDEO_ACCESS_DENIED);
        }

        verify(fileMapper, never()).insertClsf(org.mockito.ArgumentMatchers.any());
        verify(videoConfMapper, never()).insertRcrdg(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void getFile_회의와연결되지않은파일이면_접근을차단한다() {
        Long vconfId = 10L;
        Long fileId = 20L;
        Long currentEmpId = 100L;

        when(videoConfMapper.countAccessibleRcrdg(vconfId, fileId, currentEmpId)).thenReturn(0);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(currentEmpId);

            assertThatThrownBy(() -> service.getFile(vconfId, fileId))
                    .isInstanceOf(CustomException.class)
                    .extracting(e -> ((CustomException) e).getErrorCode())
                    .isEqualTo(ErrorCode.VIDEO_ACCESS_DENIED);
        }

        verify(fileService, never()).download(fileId);
    }

    @Test
    void getFile_mp3이면_원본파일명과Mpeg타입을반환한다() {
        Long vconfId = 10L;
        Long fileId = 20L;
        Long currentEmpId = 100L;
        Resource resource = mock(Resource.class);
        FileDtlVo file = new FileDtlVo();
        file.setOrgnlFileNm("회의 녹음.mp3");
        file.setAtchFileTyCd("03");
        file.setFileExtsn("mp3");
        file.setDelYn("N");

        when(videoConfMapper.countAccessibleRcrdg(vconfId, fileId, currentEmpId)).thenReturn(1);
        when(fileMapper.selectDtlById(fileId)).thenReturn(file);
        when(fileService.download(fileId)).thenReturn(resource);

        try (MockedStatic<SecurityUtil> securityUtil = mockStatic(SecurityUtil.class)) {
            securityUtil.when(SecurityUtil::getCurrentEmpId).thenReturn(currentEmpId);

            VideoRcrdgFileResource result = service.getFile(vconfId, fileId);

            assertThat(result.resource()).isSameAs(resource);
            assertThat(result.originalFileName()).isEqualTo("회의 녹음.mp3");
            assertThat(result.contentType()).isEqualTo(MediaType.parseMediaType("audio/mpeg"));
        }
    }
}
