package com.talend.talenddashboard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "machines")
public class MachineProperties {
    private List<MachineConfig> list = new ArrayList<>();

    public List<MachineConfig> getList() { return list; }
    public void setList(List<MachineConfig> list) { this.list = list; }

    public static class MachineConfig {
        private String name;
        private StatsPath stats = new StatsPath();
        private LogsPath logs = new LogsPath();
        private MetersPath meters = new MetersPath();

        // Getters & setters
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public StatsPath getStats() { return stats; }
        public void setStats(StatsPath stats) { this.stats = stats; }
        public LogsPath getLogs() { return logs; }
        public void setLogs(LogsPath logs) { this.logs = logs; }
        public MetersPath getMeters() { return meters; }
        public void setMeters(MetersPath meters) { this.meters = meters; }

        public static class StatsPath {
            private String path;
            public String getPath() { return path; }
            public void setPath(String path) { this.path = path; }
        }

        public static class LogsPath {
            private String path;
            public String getPath() { return path; }
            public void setPath(String path) { this.path = path; }
        }

        public static class MetersPath {
            private String path;
            public String getPath() { return path; }
            public void setPath(String path) { this.path = path; }
        }
    }
}