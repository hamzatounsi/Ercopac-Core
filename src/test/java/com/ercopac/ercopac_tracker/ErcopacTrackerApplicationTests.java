package com.ercopac.ercopac_tracker;

import com.ercopac.ercopac_tracker.auth.AuthDtos.LoginResponse;
import com.ercopac.ercopac_tracker.user.AppUser;
import com.ercopac.ercopac_tracker.user.Role;
import com.ercopac.ercopac_tracker.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "spring.datasource.url=jdbc:h2:mem:ercopac;DB_CLOSE_DELAY=-1;NON_KEYWORDS=VALUE",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.hikari.schema=PUBLIC",
        "spring.jpa.properties.hibernate.default_schema=PUBLIC",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "app.seed.enabled=false"
})
class ErcopacTrackerApplicationTests {

	@Autowired private TestRestTemplate restTemplate;
	@Autowired private UserRepository userRepository;
	@Autowired private PasswordEncoder passwordEncoder;

	@Test
	void contextLoads() {
	}

	@Test
	void platformOwnerCanLogInAndReceiveJwt() {
		AppUser user = new AppUser(
				"Integration Owner",
				"integration.owner@example.com",
				passwordEncoder.encode("IntegrationPassword123!"),
				Role.PLATFORM_OWNER
		);
		userRepository.saveAndFlush(user);

		ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
				"/api/auth/login",
				Map.of("username", user.getEmail(), "password", "IntegrationPassword123!"),
				LoginResponse.class
		);

		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().token()).isNotBlank();
	}

}
