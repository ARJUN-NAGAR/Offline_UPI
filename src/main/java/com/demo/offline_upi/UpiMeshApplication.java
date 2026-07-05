package com.demo.offline_upi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot Application runner for air-upi.
 * Enables Scheduled eviction tasks and AspectJ proxies for Auditing.
 */
@SpringBootApplication
@EnableScheduling
@EnableAspectJAutoProxy
public class UpiMeshApplication {

    public static void main(String[] args) {
        SpringApplication.run(UpiMeshApplication.class, args);
    }
}
