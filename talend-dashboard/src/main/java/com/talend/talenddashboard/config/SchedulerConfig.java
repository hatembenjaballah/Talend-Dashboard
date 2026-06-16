package com.talend.talenddashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.import")
public class SchedulerConfig {
    private String cron = "0 0 * * * *"; // valeur par défaut

    public String getCron() { return cron; }
    public void setCron(String cron) { this.cron = cron; }
}