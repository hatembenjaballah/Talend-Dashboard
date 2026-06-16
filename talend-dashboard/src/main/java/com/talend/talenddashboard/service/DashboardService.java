package com.talend.talenddashboard.service;

import com.talend.talenddashboard.entity.*;
import com.talend.talenddashboard.repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {
    private final JobExecutionRepository jobRepo;
    private final ErrorLogRepository errorRepo;
    private final FlowMeterRepository meterRepo;

    public DashboardService(JobExecutionRepository jobRepo,
                            ErrorLogRepository errorRepo,
                            FlowMeterRepository meterRepo) {
        this.jobRepo = jobRepo;
        this.errorRepo = errorRepo;
        this.meterRepo = meterRepo;
    }

    // Filtre générique utilisé par toutes les méthodes
    private List<JobExecution> filterJobs(String machine, String job, LocalDateTime start, LocalDateTime end) {
        return jobRepo.findAll().stream()
                .filter(j -> machine == null || machine.isEmpty() || j.getMachineName().equals(machine))
                .filter(j -> job == null || job.isEmpty() || j.getJobName().equalsIgnoreCase(job))
                .filter(j -> start == null || (j.getStartTime() != null && !j.getStartTime().isBefore(start)))
                .filter(j -> end == null || (j.getStartTime() != null && !j.getStartTime().isAfter(end)))
                .collect(Collectors.toList());
    }

    public Map<String, Object> getKpis(String machine, String job, LocalDateTime start, LocalDateTime end) {
        List<JobExecution> jobs = filterJobs(machine, job, start, end);
        long total = jobs.size();
        long success = jobs.stream().filter(j -> j.getStatus() == JobExecution.ExecutionStatus.SUCCESS).count();
        double rate = total > 0 ? (success * 100.0) / total : 0;
        double avgDuration = jobs.stream().filter(j -> j.getDurationMs() != null)
                .mapToLong(JobExecution::getDurationMs).average().orElse(0);

        // Erreurs : on filtre par machine, job et plage de dates (via les jobs correspondants)
        List<String> jobExecutionIds = jobs.stream().map(JobExecution::getExecutionId).collect(Collectors.toList());
        long totalErrors = errorRepo.findAll().stream()
                .filter(e -> jobExecutionIds.contains(e.getExecutionId()))
                .count();

        // Meters : idem
        long totalRows = meterRepo.findAll().stream()
                .filter(m -> jobExecutionIds.contains(m.getExecutionId()))
                .mapToLong(FlowMeter::getValue).sum();

        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("totalExecutions", total);
        kpis.put("successRate", Math.round(rate * 10) / 10.0);
        kpis.put("avgDurationMs", Math.round(avgDuration));
        kpis.put("totalErrors", totalErrors);
        kpis.put("totalRows", totalRows);
        return kpis;
    }

    public List<Map<String, Object>> durationTimeSeries(String machine, String job, LocalDateTime start, LocalDateTime end) {
        return filterJobs(machine, job, start, end).stream()
                .filter(j -> j.getStartTime() != null && j.getDurationMs() != null)
                .map(j -> {
                    Map<String, Object> point = new LinkedHashMap<>();
                    point.put("start", j.getStartTime().toString());
                    point.put("duration", j.getDurationMs());
                    point.put("status", j.getStatus().name());
                    point.put("job", j.getJobName());
                    return point;
                }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> top5LongestJobs(String machine, String job, LocalDateTime start, LocalDateTime end) {
        return filterJobs(machine, job, start, end).stream()
                .filter(j -> j.getDurationMs() != null)
                .sorted((a, b) -> Long.compare(b.getDurationMs(), a.getDurationMs()))
                .limit(5)
                .map(j -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("jobName", j.getJobName());
                    m.put("durationMs", j.getDurationMs());
                    return m;
                }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> successFailureByJob(String machine, String job, LocalDateTime start, LocalDateTime end) {
        Map<String, long[]> map = new HashMap<>();
        filterJobs(machine, job, start, end).forEach(j -> {
            long[] counts = map.computeIfAbsent(j.getJobName(), k -> new long[2]);
            if (j.getStatus() == JobExecution.ExecutionStatus.SUCCESS) counts[0]++;
            else counts[1]++;
        });
        return map.entrySet().stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("jobName", e.getKey());
            m.put("success", e.getValue()[0]);
            m.put("failure", e.getValue()[1]);
            return m;
        }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> volumeByJob(String machine, String job, LocalDateTime start, LocalDateTime end) {
        // On filtre les meters en se basant sur les executionIds des jobs filtrés
        List<String> jobExecutionIds = filterJobs(machine, job, start, end).stream()
                .map(JobExecution::getExecutionId).collect(Collectors.toList());
        List<FlowMeter> meters = meterRepo.findAll().stream()
                .filter(m -> jobExecutionIds.contains(m.getExecutionId()))
                .collect(Collectors.toList());
        Map<String, Long> sumByJob = new HashMap<>();
        meters.forEach(m -> sumByJob.merge(m.getJobName(), m.getValue(), Long::sum));
        return sumByJob.entrySet().stream().map(e -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("jobName", e.getKey());
            m.put("totalRows", e.getValue());
            return m;
        }).collect(Collectors.toList());
    }
}