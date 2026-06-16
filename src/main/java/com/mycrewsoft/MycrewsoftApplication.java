package com.mycrewsoft;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableAsync
@SpringBootApplication
@EnableScheduling
public class MycrewsoftApplication {

	public static void main(String[] args) {
		SpringApplication.run(MycrewsoftApplication.class, args);
	}

}
