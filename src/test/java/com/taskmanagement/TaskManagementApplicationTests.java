package com.taskmanagement;

import io.github.cdimascio.dotenv.Dotenv;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest
class TaskManagementApplicationTests {
	@DynamicPropertySource
	static void setProperties(DynamicPropertyRegistry registry) {
		Dotenv dotenv = Dotenv.load();
		registry.add("spring.datasource.username", () -> dotenv.get("POSTGRES_USER"));
		registry.add("spring.datasource.password", () -> dotenv.get("POSTGRES_PASSWORD"));
	}

	//context Load
	@Test
	void contextLoads() {
	}

}
