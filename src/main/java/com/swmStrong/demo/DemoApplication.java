package com.swmStrong.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import com.swmStrong.demo.infra.LLM.GeminiConfig;
import com.swmStrong.demo.infra.LLM.OpenAIConfig;

import java.util.TimeZone;

@EnableScheduling
@SpringBootApplication(exclude = {io.awspring.cloud.autoconfigure.s3.S3AutoConfiguration.class})
@EnableConfigurationProperties({GeminiConfig.class, OpenAIConfig.class})
public class DemoApplication {

	public static void main(String[] args) {
		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
		SpringApplication.run(DemoApplication.class, args);
	}

}
