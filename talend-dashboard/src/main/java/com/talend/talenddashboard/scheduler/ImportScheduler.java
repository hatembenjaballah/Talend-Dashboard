package com.talend.talenddashboard.scheduler;

import com.talend.talenddashboard.config.SchedulerConfig;
import com.talend.talenddashboard.service.FileImportService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ImportScheduler {
    private final FileImportService importService;
    private final SchedulerConfig config;

    public ImportScheduler(FileImportService importService, SchedulerConfig config) {
        this.importService = importService;
        this.config = config;
    }

    @Scheduled(cron = "${app.import.cron}")
    public void scheduledImport() {
        importService.importAll();
    }
}