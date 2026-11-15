package com.fypgrading.configserver;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.RestClient;

/** Serves the shared file to every service, and exposes no actuator endpoint beyond health, info, prometheus. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"spring.profiles.active=native", "eureka.client.enabled=false"})
class ConfigServerApplicationTests {

    @TempDir
    static Path repo;

    @LocalServerPort
    int port;

    @DynamicPropertySource
    static void configDir(DynamicPropertyRegistry registry) throws Exception {
        Files.writeString(repo.resolve("application.yml"), "management.endpoints.web.exposure.include: health\n");
        registry.add("CONFIG_DIR", repo::toString);
    }

    @Test
    void servesTheSharedConfigurationToAService() {
        String body = RestClient.create("http://localhost:" + port).get().uri("/config/admin-service/default")
                .retrieve().body(String.class);
        assertThat(body).contains("management.endpoints.web.exposure.include");
    }

    @Test
    void actuatorIsLimitedToHealthInfoAndPrometheus() {
        RestClient http = RestClient.create("http://localhost:" + port);
        assertThat(http.get().uri("/actuator/health").retrieve().toBodilessEntity().getStatusCode().value()).isEqualTo(200);
        int env = http.get().uri("/actuator/env").exchange((req, res) -> res.getStatusCode().value());
        assertThat(env).isEqualTo(404);
    }
}
