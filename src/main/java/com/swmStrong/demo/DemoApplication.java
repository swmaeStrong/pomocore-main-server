package com.swmStrong.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.swmStrong.demo.infra.LLM.LLMConfig;

import java.util.TimeZone;

@EnableScheduling
@SpringBootApplication
@EnableConfigurationProperties(LLMConfig.class)
public class DemoApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		SpringApplication.run(DemoApplication.class, args);
	}

}
