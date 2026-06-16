package com.talend.talenddashboard.controller.rest;

import com.talend.talenddashboard.entity.JobExecution;
import com.talend.talenddashboard.repository.JobExecutionRepository;
import com.talend.talenddashboard.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardRestController {

    private final DashboardService dashboardService;
    private final JobExecutionRepository jobRepo;

    public DashboardRestController(DashboardService dashboardService,
                                   JobExecutionRepository jobRepo) {
        this.dashboardService = dashboardService;
        this.jobRepo = jobRepo;
    }

    @GetMapping("/kpis")
    public Map<String, Object> kpis(
            @RequestParam(required = false) String machine,
            @RequestParam(required = false) String job,          // <-- AJOUTÉ
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return dashboardService.getKpis(machine, job, start, end);
    }

    @GetMapping("/duration-timeseries")
    public List<Map<String, Object>> durationTimeseries(
            @RequestParam(required = false) String machine,
            @RequestParam(required = false) String job,          // <-- AJOUTÉ
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return dashboardService.durationTimeSeries(machine, job, start, end);
    }

    @GetMapping("/top5-longest")
    public List<Map<String, Object>> top5(
            @RequestParam(required = false) String machine,
            @RequestParam(required = false) String job,          // <-- AJOUTÉ
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return dashboardService.top5LongestJobs(machine, job, start, end);
    }

    @GetMapping("/success-failure-job")
    public List<Map<String, Object>> successFailureByJob(
            @RequestParam(required = false) String machine,
            @RequestParam(required = false) String job,          // <-- AJOUTÉ
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return dashboardService.successFailureByJob(machine, job, start, end);
    }

    @GetMapping("/volume-by-job")
    public List<Map<String, Object>> volumeByJob(
            @RequestParam(required = false) String machine,
            @RequestParam(required = false) String job,          // <-- AJOUTÉ
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return dashboardService.volumeByJob(machine, job, start, end);
    }

    // Liste déroulante des jobs : ne doit PAS être filtrée par job
    @GetMapping("/jobs")
    public List<String> distinctJobs(
            @RequestParam(required = false) String machine,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {
        return jobRepo.findAll().stream()
                .filter(j -> machine == null || machine.isEmpty() || j.getMachineName().equals(machine))
                .filter(j -> start == null || (j.getStartTime() != null && !j.getStartTime().isBefore(start)))
                .filter(j -> end == null || (j.getStartTime() != null && !j.getStartTime().isAfter(end)))
                .map(JobExecution::getJobName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}