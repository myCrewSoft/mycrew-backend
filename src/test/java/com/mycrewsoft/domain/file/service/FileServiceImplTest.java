package com.mycrewsoft.domain.file.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import com.mycrewsoft.domain.file.dto.FileUploadRequestDto;
import com.mycrewsoft.security.users.AuthorizationUserDetails;

@SpringBootTest
@Transactional
class FileServiceImplTest {
	@Autowired
	private FileService service;
	
	@Autowired
	private Environment env;
	
	@Test
	void debug() {
	    System.out.println("spring.datasource.username = " 
	        + env.getProperty("spring.datasource.username"));
	}
	
	@Test
	void test() {
		System.out.println("DB USER = " + System.getProperty("DB_USERNAME"));
		System.out.println("DB PASS = " + System.getProperty("DB_PASSWORD"));
	}
	
	@BeforeEach
	void setUp() {
	    // 가짜 로그인 사용자 세팅
	    AuthorizationUserDetails userDetails = new AuthorizationUserDetails(
	            1L,           // empId
	            "testUser",   // username
	            null,
	            true,
	            1,
	            List.of(new SimpleGrantedAuthority("ROLE_USER")), null
	    );

	    SecurityContextHolder.getContext().setAuthentication(
	            new UsernamePasswordAuthenticationToken(
	                    userDetails,
	                    null,
	                    userDetails.getAuthorities()
	            )
	    );
	}

	@AfterEach
	void tearDown() {
	    SecurityContextHolder.clearContext();  // 테스트 후 인증 정보 초기화
	}
	
	@Test
	void testUpload() throws IOException {
		// given: 테스트 파일 생성
        String filePath = "C:/Users/PC-27/Desktop/bird.jpg";

        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "chk.png",
                "image/png",
                new FileInputStream(filePath)
        );

        FileUploadRequestDto reqDto = new FileUploadRequestDto();
        reqDto.setFile(mockFile);

        // when
        assertDoesNotThrow(() -> service.upload(reqDto, "01"));
	}

}
