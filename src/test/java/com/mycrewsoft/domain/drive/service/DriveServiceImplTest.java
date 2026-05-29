package com.mycrewsoft.domain.drive.service;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mycrewsoft.domain.drive.dto.DriveFolderCreateRequestDto;
import com.mycrewsoft.domain.drive.dto.DriveResponseDto;
import com.mycrewsoft.security.users.AuthorizationUserDetails;

@SpringBootTest
class DriveServiceImplTest {
	
	@Autowired
	private DriveService service;
	
	 @BeforeEach
	    void setUpSecurityContext() {
	        AuthorizationUserDetails principal = new AuthorizationUserDetails(
	                1L, "testuser", null, true, 1, List.of(), null
	        );
	        SecurityContextHolder.getContext().setAuthentication(
	                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities())
	        );
	    }

	    @AfterEach
	    void clearSecurityContext() {
	        SecurityContextHolder.clearContext();
	    }
	
	@Test
	void testCreateFolder() {
		DriveFolderCreateRequestDto reqDto = new DriveFolderCreateRequestDto();
		reqDto.setItemNm("테스트 폴더명");
		reqDto.setPrntDriveItemId(1L);
		
		DriveResponseDto dto = service.createFolder(reqDto);
		
		System.out.println(dto);
	}

}
