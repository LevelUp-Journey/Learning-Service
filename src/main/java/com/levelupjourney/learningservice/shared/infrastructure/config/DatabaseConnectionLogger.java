package com.levelupjourney.learningservice.shared.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * Logger component to monitor database connection status
 */
@Component
@Slf4j
public class DatabaseConnectionLogger {

    @Autowired
    private DataSource dataSource;

    @EventListener(ApplicationReadyEvent.class)
    public void logDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            String databaseProductName = connection.getMetaData().getDatabaseProductName();
            String databaseProductVersion = connection.getMetaData().getDatabaseProductVersion();
            String url = connection.getMetaData().getURL();

            log.info("=".repeat(80));
            log.info("✅ DATABASE CONNECTION SUCCESSFUL");
            log.info("=".repeat(80));
            log.info("📊 Database: {} {}", databaseProductName, databaseProductVersion);
            log.info("🔗 URL: {}", url);
            log.info("🎯 Schema: {}", connection.getSchema());
            log.info("🔓 Auto-commit: {}", connection.getAutoCommit());
            log.info("=".repeat(80));

        } catch (Exception e) {
            log.error("=".repeat(80));
            log.error("❌ DATABASE CONNECTION FAILED");
            log.error("=".repeat(80));
            log.error("⚠️ Error: {}", e.getMessage(), e);
            log.error("=".repeat(80));
        }
    }
}

