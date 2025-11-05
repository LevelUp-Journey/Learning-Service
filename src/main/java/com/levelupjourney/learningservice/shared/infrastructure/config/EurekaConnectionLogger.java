package com.levelupjourney.learningservice.shared.infrastructure.config;

import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.shared.Application;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Logger component to monitor Eureka Service Discovery connection status
 */
@Component
@Slf4j
public class EurekaConnectionLogger {

    @Autowired(required = false)
    private EurekaClient eurekaClient;

    @Value("${eureka.client.service-url.defaultZone:N/A}")
    private String eurekaServiceUrl;

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String serverPort;

    @EventListener(ApplicationReadyEvent.class)
    public void logEurekaConnection() {
        // Wait a bit for Eureka to register
        new Thread(() -> {
            try {
                Thread.sleep(3000); // Wait 3 seconds for registration

                if (eurekaClient == null) {
                    log.warn("=".repeat(80));
                    log.warn("⚠️ EUREKA CLIENT NOT CONFIGURED");
                    log.warn("=".repeat(80));
                    return;
                }

                InstanceInfo instanceInfo = eurekaClient.getApplicationInfoManager().getInfo();

                log.info("=".repeat(80));
                log.info("✅ SERVICE DISCOVERY (EUREKA) CONNECTION SUCCESSFUL");
                log.info("=".repeat(80));
                log.info("🔗 Eureka Server: {}", eurekaServiceUrl);
                log.info("🏷️  Application Name: {}", applicationName);
                log.info("🆔 Instance ID: {}", instanceInfo.getInstanceId());
                log.info("📍 Status: {}", instanceInfo.getStatus());
                log.info("🌐 IP Address: {}", instanceInfo.getIPAddr());
                log.info("🔌 Port: {}", serverPort);
                log.info("🏠 Home Page: {}", instanceInfo.getHomePageUrl());
                log.info("💚 Health Check: {}", instanceInfo.getHealthCheckUrl());
                log.info("📖 Status Page: {}", instanceInfo.getStatusPageUrl());

                // Log registered services
                List<Application> applications = eurekaClient.getApplications().getRegisteredApplications();
                log.info("📋 Registered Services: {} services found", applications.size());
                applications.forEach(app -> {
                    log.info("   ├─ {} ({} instances)", app.getName(), app.getInstances().size());
                });

                log.info("=".repeat(80));

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Thread interrupted while checking Eureka connection", e);
            } catch (Exception e) {
                log.error("=".repeat(80));
                log.error("❌ SERVICE DISCOVERY (EUREKA) CONNECTION FAILED");
                log.error("=".repeat(80));
                log.error("⚠️ Error: {}", e.getMessage());
                log.error("💡 Make sure Eureka Server is running at: {}", eurekaServiceUrl);
                log.error("=".repeat(80));
            }
        }).start();
    }
}

