package com.mycrewsoft.domain.file.mapper;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.mycrewsoft.domain.file.vo.FileClsfVo;
import com.mycrewsoft.domain.file.vo.FileDtlVo;

@SpringBootTest
class FileMapperTest {
	@Autowired
	private FileMapper fileMapper;
	
	@Test
	void testInsertClsf() {
		FileClsfVo vo1 = new FileClsfVo();
		
		int result = fileMapper.insertClsf(vo1);
		assertEquals(1, result);
		assertNotNull(vo1.getAtchFileId());
	}

}
