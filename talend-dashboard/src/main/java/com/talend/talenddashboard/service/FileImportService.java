package com.talend.talenddashboard.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.talend.talenddashboard.entity.ErrorLog;
import com.talend.talenddashboard.entity.FlowMeter;
import com.talend.talenddashboard.entity.JobExecution;
import com.talend.talenddashboard.entity.Machine;
import com.talend.talenddashboard.repository.ErrorLogRepository;
import com.talend.talenddashboard.repository.FlowMeterRepository;
import com.talend.talenddashboard.repository.JobExecutionRepository;
import com.talend.talenddashboard.repository.MachineRepository;

@Service
public class FileImportService {

    private static final Logger log = LoggerFactory.getLogger(FileImportService.class);
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MachineRepository machineRepo;
    private final JobExecutionRepository jobExecRepo;
    private final ErrorLogRepository errorLogRepo;
    private final FlowMeterRepository flowMeterRepo;

    public FileImportService(MachineRepository machineRepo,
                             JobExecutionRepository jobExecRepo,
                             ErrorLogRepository errorLogRepo,
                             FlowMeterRepository flowMeterRepo) {
        this.machineRepo = machineRepo;
        this.jobExecRepo = jobExecRepo;
        this.errorLogRepo = errorLogRepo;
        this.flowMeterRepo = flowMeterRepo;
    }

    @Transactional
    public void importAll() {
        List<Machine> machines = machineRepo.findAll();
        for (Machine m : machines) {
            importStats(m);
            importLogs(m);
            importMeters(m);
        }
    }

    private void importStats(Machine machine) {
        if (machine.getJdbcUrl() != null && !machine.getJdbcUrl().isEmpty()) {
            if (tableExists(machine, "statcatcher")) {
                importStatsFromDb(machine);
            } else {
                log.warn("Table 'statcatcher' introuvable dans la base {}, import stats ignoré.", machine.getJdbcUrl());
            }
        } else {
            importStatsFromFile(machine);
        }
    }

    private void importStatsFromFile(Machine machine) {
        File file = new File(machine.getStatsPath());
        if (!file.exists()) {
            log.warn("Fichier stats introuvable: {}", machine.getStatsPath());
            return;
        }
        final String machineName = machine.getName();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(";", -1);
                if (parts.length < 12) continue;
                processStatsLine(machineName, parts);
            }
        } catch (Exception e) {
            log.error("Erreur import stats {}: {}", machineName, e.getMessage());
        }
    }

    private void importStatsFromDb(Machine machine) {
        final String machineName = machine.getName();
        try {
            JdbcTemplate jdbc = createJdbcTemplate(machine);
            List<Map<String, Object>> rows = jdbc.queryForList(machine.getStatsQuery());
            for (Map<String, Object> row : rows) {
                String[] parts = new String[14];
                Object momentObj = row.get("moment");
                parts[0] = momentObj != null ? momentObj.toString() : "";
                parts[1] = row.get("pid") != null ? row.get("pid").toString() : "";
                parts[2] = row.get("father_pid") != null ? row.get("father_pid").toString() : "";
                parts[3] = row.get("root_pid") != null ? row.get("root_pid").toString() : "";
                parts[4] = row.get("system_pid") != null ? row.get("system_pid").toString() : "";
                parts[5] = row.get("project") != null ? row.get("project").toString() : "";
                parts[6] = row.get("job") != null ? row.get("job").toString() : "";
                parts[7] = row.get("job_repository_id") != null ? row.get("job_repository_id").toString() : "";
                parts[8] = row.get("job_version") != null ? row.get("job_version").toString() : "";
                parts[9] = row.get("context") != null ? row.get("context").toString() : "";
                parts[10] = row.get("origin") != null ? row.get("origin").toString() : "";
                parts[11] = row.get("message_type") != null ? row.get("message_type").toString() : "";
                parts[12] = row.get("message") != null ? row.get("message").toString() : "";
                parts[13] = row.get("duration") != null ? row.get("duration").toString() : "";
                processStatsLine(machineName, parts);
            }
        } catch (Exception e) {
            log.error("Erreur import stats (DB) {}: {}", machineName, e.getMessage());
        }
    }

    private void processStatsLine(String machineName, String[] parts) {
        LocalDateTime timestamp = parseMoment(parts[0].trim());
        if (timestamp == null) {
            log.warn("Impossible de parser le moment: {}", parts[0]);
            return;
        }
        String pid = parts[1].trim();
        String jobName = parts[6].trim();
        String context = parts[9].trim();
        String messageType = parts[11].trim().toLowerCase();

        if ("begin".equals(messageType)) {
            if (jobExecRepo.findByMachineNameAndExecutionId(machineName, pid).isEmpty()) {
                JobExecution je = new JobExecution();
                je.setMachineName(machineName);
                je.setJobName(jobName);
                je.setPid(parts[4].trim());
                je.setContext(context);
                je.setExecutionId(pid);
                je.setStartTime(timestamp);
                je.setStatus(JobExecution.ExecutionStatus.RUNNING);
                jobExecRepo.save(je);
            }
        } else if ("end".equals(messageType)) {
            String statusStr = parts.length > 12 ? parts[12].trim().toLowerCase() : "";
            Long durationMs = null;
            if (parts.length > 13 && !parts[13].trim().isEmpty()) {
                try { durationMs = Long.parseLong(parts[13].trim()); } catch (NumberFormatException ignored) {}
            }

            JobExecution je = jobExecRepo.findByMachineNameAndExecutionId(machineName, pid)
                    .orElseGet(() -> {
                        JobExecution temp = new JobExecution();
                        temp.setMachineName(machineName);
                        temp.setJobName(jobName);
                        temp.setPid(parts[4].trim());
                        temp.setContext(context);
                        temp.setExecutionId(pid);
                        temp.setStartTime(timestamp);
                        temp.setStatus(JobExecution.ExecutionStatus.RUNNING);
                        return temp;
                    });

            je.setEndTime(timestamp);
            if ("success".equalsIgnoreCase(statusStr)) {
                je.setStatus(JobExecution.ExecutionStatus.SUCCESS);
            } else if ("failure".equalsIgnoreCase(statusStr)) {
                je.setStatus(JobExecution.ExecutionStatus.FAILURE);
            }

            if (durationMs != null) {
                je.setDurationMs(durationMs);
            } else if (je.getStartTime() != null) {
                je.setDurationMs(java.time.Duration.between(je.getStartTime(), timestamp).toMillis());
            }
            jobExecRepo.save(je);
        }
    }

    private void importLogs(Machine machine) {
        if (machine.getJdbcUrl() != null && !machine.getJdbcUrl().isEmpty()) {
            if (tableExists(machine, "logcatcher")) {
                importLogsFromDb(machine);
            } else {
                log.warn("Table 'logcatcher' introuvable dans la base {}, import logs ignoré.", machine.getJdbcUrl());
            }
        } else {
            importLogsFromFile(machine);
        }
    }

    private void importLogsFromFile(Machine machine) {
        File file = new File(machine.getLogsPath());
        if (!file.exists()) {
            log.warn("Fichier logs introuvable: {}", machine.getLogsPath());
            return;
        }
        final String machineName = machine.getName();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(";", -1);
                if (parts.length < 12) continue;
                processLogLine(machineName, parts);
            }
        } catch (Exception e) {
            log.error("Erreur import logs {}: {}", machineName, e.getMessage());
        }
    }

    private void importLogsFromDb(Machine machine) {
        final String machineName = machine.getName();
        try {
            JdbcTemplate jdbc = createJdbcTemplate(machine);
            List<Map<String, Object>> rows = jdbc.queryForList(machine.getLogsQuery());
            for (Map<String, Object> row : rows) {
                String[] parts = new String[12];
                Object momentObj = row.get("moment");
                parts[0] = momentObj != null ? momentObj.toString() : "";
                parts[1] = row.get("pid") != null ? row.get("pid").toString() : "";
                parts[2] = row.get("root_pid") != null ? row.get("root_pid").toString() : "";
                parts[3] = row.get("father_pid") != null ? row.get("father_pid").toString() : "";
                parts[4] = row.get("project") != null ? row.get("project").toString() : "";
                parts[5] = row.get("job") != null ? row.get("job").toString() : "";
                parts[6] = row.get("context") != null ? row.get("context").toString() : "";
                parts[7] = row.get("priority") != null ? row.get("priority").toString() : "";
                parts[8] = row.get("type") != null ? row.get("type").toString() : "";
                parts[9] = row.get("origin") != null ? row.get("origin").toString() : "";
                parts[10] = row.get("message") != null ? row.get("message").toString() : "";
                parts[11] = row.get("code") != null ? row.get("code").toString() : "";
                processLogLine(machineName, parts);
            }
        } catch (Exception e) {
            log.error("Erreur import logs (DB) {}: {}", machineName, e.getMessage());
        }
    }

    private void processLogLine(String machineName, String[] parts) {
        LocalDateTime ts = parseMoment(parts[0].trim());
        if (ts == null) {
            log.warn("Impossible de parser le moment d'un log: {}", parts[0]);
            return;
        }
        String pid = parts[1].trim();
        String jobName = parts[5].trim();
        String component = parts[9].trim();
        String message = parts[10].trim();
        String code = parts[11].trim();
        String fullMessage = message + " (code " + code + ")";

        boolean exists = errorLogRepo.findAll().stream()
                .anyMatch(e -> e.getMachineName().equals(machineName)
                        && e.getExecutionId().equals(pid)
                        && e.getComponent().equals(component)
                        && e.getExceptionMessage().equals(fullMessage));
        if (!exists) {
            ErrorLog el = new ErrorLog();
            el.setMachineName(machineName);
            el.setJobName(jobName);
            el.setTimestamp(ts);
            el.setComponent(component);
            el.setExceptionMessage(fullMessage);
            el.setExecutionId(pid);
            errorLogRepo.save(el);
        }
    }

    private void importMeters(Machine machine) {
        if (machine.getJdbcUrl() != null && !machine.getJdbcUrl().isEmpty()) {
            if (tableExists(machine, "flowmeter")) {
                importMetersFromDb(machine);
            } else {
                log.warn("Table 'flowmeter' introuvable dans la base {}, import meters ignoré.", machine.getJdbcUrl());
            }
        } else {
            importMetersFromFile(machine);
        }
    }

    private void importMetersFromFile(Machine machine) {
        File file = new File(machine.getMetersPath());
        if (!file.exists()) {
            log.warn("Fichier meters introuvable: {}", machine.getMetersPath());
            return;
        }
        final String machineName = machine.getName();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(";", -1);
                if (parts.length < 15) continue;
                processMeterLine(machineName, parts);
            }
        } catch (Exception e) {
            log.error("Erreur import meters {}: {}", machineName, e.getMessage());
        }
    }

    private void importMetersFromDb(Machine machine) {
        final String machineName = machine.getName();
        try {
            JdbcTemplate jdbc = createJdbcTemplate(machine);
            List<Map<String, Object>> rows = jdbc.queryForList(machine.getMetersQuery());
            for (Map<String, Object> row : rows) {
                String[] parts = new String[15];
                Object momentObj = row.get("moment");
                parts[0] = momentObj != null ? momentObj.toString() : "";
                parts[1]  = row.get("pid") != null ? row.get("pid").toString() : "";
                parts[2]  = row.get("father_pid") != null ? row.get("father_pid").toString() : "";
                parts[3]  = row.get("root_pid") != null ? row.get("root_pid").toString() : "";
                parts[4]  = row.get("system_pid") != null ? row.get("system_pid").toString() : "";
                parts[5]  = row.get("project") != null ? row.get("project").toString() : "";
                parts[6]  = row.get("job") != null ? row.get("job").toString() : "";
                parts[7]  = row.get("job_repository_id") != null ? row.get("job_repository_id").toString() : "";
                parts[8]  = row.get("job_version") != null ? row.get("job_version").toString() : "";
                parts[9]  = row.get("context") != null ? row.get("context").toString() : "";
                parts[10] = row.get("origin") != null ? row.get("origin").toString() : "";
                parts[11] = row.get("label") != null ? row.get("label").toString() : "";
                parts[12] = row.get("count") != null ? row.get("count").toString() : "";
                parts[13] = row.get("reference") != null ? row.get("reference").toString() : "";
                parts[14] = row.get("thresholds") != null ? row.get("thresholds").toString() : "";
                processMeterLine(machineName, parts);
            }
        } catch (Exception e) {
            log.error("Erreur import meters (DB) {}: {}", machineName, e.getMessage());
        }
    }

    private void processMeterLine(String machineName, String[] parts) {
        LocalDateTime ts = parseMoment(parts[0].trim());
        if (ts == null) {
            log.warn("Impossible de parser le moment d'un meter: {}", parts[0]);
            return;
        }
        String pid = parts[1].trim();
        String jobName = parts[6].trim();
        String meterName = parts[10].trim();
        String counterName = parts[11].trim();
        long value = Long.parseLong(parts[12].trim());
        long reference = 0;
        if (parts.length > 13 && !parts[13].trim().isEmpty()) {
            try {
                reference = Long.parseLong(parts[13].trim());
            } catch (NumberFormatException e) {
                log.warn("Reference non numérique: {}", parts[13]);
            }
        }
        String thresholds = parts.length > 14 ? parts[14].trim() : "";

        boolean exists = flowMeterRepo.findAll().stream()
                .anyMatch(m -> m.getMachineName().equals(machineName)
                        && m.getExecutionId().equals(pid)
                        && m.getMeterName().equals(meterName)
                        && m.getTimestamp().equals(ts));
        if (!exists) {
            FlowMeter fm = new FlowMeter();
            fm.setMachineName(machineName);
            fm.setJobName(jobName);
            fm.setTimestamp(ts);
            fm.setMeterName(meterName);
            fm.setCounterName(counterName);
            fm.setValue(value);
            fm.setReference(reference);
            fm.setThresholds(thresholds);
            fm.setExecutionId(pid);
            flowMeterRepo.save(fm);
        }
    }

    private JdbcTemplate createJdbcTemplate(Machine machine) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(machine.getJdbcUrl());
        dataSource.setDriverClassName(machine.getJdbcDriver());
        dataSource.setUsername(machine.getJdbcUser());
        dataSource.setPassword(machine.getJdbcPassword());
        return new JdbcTemplate(dataSource);
    }

    private boolean tableExists(Machine machine, String tableName) {
        try {
            JdbcTemplate jdbc = createJdbcTemplate(machine);
            jdbc.queryForList("SELECT 1 FROM " + tableName + " WHERE 1=0");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private LocalDateTime parseMoment(String momentStr) {
        if (momentStr == null || momentStr.isEmpty()) return null;
        try {
            long epochMilli = Long.parseLong(momentStr);
            return Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalDateTime();
        } catch (NumberFormatException ignored) {}
        try {
            return LocalDateTime.parse(momentStr, DTF);
        } catch (DateTimeParseException e) {
            log.error("Format de date non reconnu: {}", momentStr);
            return null;
        }
    }
}