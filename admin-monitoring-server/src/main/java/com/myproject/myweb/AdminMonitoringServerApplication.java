package com.myproject.myweb;

import de.codecentric.boot.admin.server.config.EnableAdminServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@EnableAdminServer
@SpringBootApplication
public class AdminMonitoringServerApplication{
    // (exclude = AdminServerHazelcastAutoConfiguration.class)

    public static void main(String[] args) {
        SpringApplication.run(AdminMonitoringServerApplication.class, args);
    }
}
