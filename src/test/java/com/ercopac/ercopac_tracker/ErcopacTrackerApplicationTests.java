package com.ercopac.ercopac_tracker;

import com.ercopac.ercopac_tracker.auth.AuthDtos.LoginResponse;
import com.ercopac.ercopac_tracker.seed.DefaultPlatformOwnerBootstrap;
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
	@Autowired private DefaultPlatformOwnerBootstrap defaultPlatformOwnerBootstrap;

	@Test
	void contextLoads() {
	}

	@Test
	void defaultPlatformOwnerIsPreservedRecreatedAndCanLogIn() {
		AppUser owner = userRepository.findByEmailIgnoreCase("owner@projectum.com").orElseThrow();
		Long originalId = owner.getId();
		String originalPasswordHash = owner.getPasswordHash();
		assertThat(owner.getRole()).isEqualTo(Role.PLATFORM_OWNER);
		assertThat(owner.isActive()).isTrue();
		assertThat(owner.getOrganisation()).isNull();
		assertThat(owner.getDepartment()).isNull();
		assertThat(owner.getResourceType()).isNull();
		assertThat(passwordEncoder.matches("Projectum123!", originalPasswordHash)).isTrue();

		defaultPlatformOwnerBootstrap.run();
		AppUser preservedOwner = userRepository.findByEmailIgnoreCase("owner@projectum.com").orElseThrow();
		assertThat(preservedOwner.getId()).isEqualTo(originalId);
		assertThat(preservedOwner.getPasswordHash()).isEqualTo(originalPasswordHash);

		userRepository.delete(preservedOwner);
		userRepository.flush();
		defaultPlatformOwnerBootstrap.run();
		AppUser recreatedOwner = userRepository.findByEmailIgnoreCase("owner@projectum.com").orElseThrow();

		ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
				"/api/auth/login",
				Map.of("username", recreatedOwner.getEmail(), "password", "Projectum123!"),
				LoginResponse.class
		);

		assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
		assertThat(response.getBody()).isNotNull();
		assertThat(response.getBody().token()).isNotBlank();
		assertThat(response.getBody().role()).isEqualTo("PLATFORM_OWNER");
	}

}
