package com.levelupjourney.learningservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.beans.factory.annotation.Autowired;
import lombok.extern.slf4j.Slf4j;

@SpringBootApplication(scanBasePackages = "com.levelupjourney.learningservice")
@EnableJpaRepositories(basePackages = "com.levelupjourney.learningservice")
@Slf4j
public class LearningServiceApplication {

    @Autowired
    private Environment environment;

    public static void main(String[] args) {
        SpringApplication.run(LearningServiceApplication.class, args);
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        String port = environment.getProperty("server.port", "8080");
        String contextPath = environment.getProperty("server.servlet.context-path", "");
        
        log.info("=".repeat(80));
        log.info("🚀 Learning Service started successfully!");
        log.info("=".repeat(80));
        log.info("📖 Swagger UI: http://localhost:{}{}/swagger-ui/index.html", port, contextPath);
        log.info("📡 API Docs: http://localhost:{}{}/v3/api-docs", port, contextPath);
        log.info("🔧 H2 Console: http://localhost:{}{}/h2-console", port, contextPath);
        log.info("=".repeat(80));
    }

}
