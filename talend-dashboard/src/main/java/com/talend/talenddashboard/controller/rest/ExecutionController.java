package com.talend.talenddashboard.controller.rest;

import com.talend.talenddashboard.entity.*;
import com.talend.talenddashboard.repository.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
public class ExecutionController {

    private final JobExecutionRepository jobRepo;
    private final ErrorLogRepository errorRepo;
    private final FlowMeterRepository meterRepo;

    public ExecutionController(JobExecutionRepository jobRepo,
                               ErrorLogRepository errorRepo,
                               FlowMeterRepository meterRepo) {
        this.jobRepo = jobRepo;
        this.errorRepo = errorRepo;
        this.meterRepo = meterRepo;
    }


    @GetMapping("/api/executions")
    public Map<String, Object> getExecutions(
            @RequestParam(required = false) String machine,
            @RequestParam(required = false) String job,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "startTime") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder) {

       
        List<JobExecution> filtered = jobRepo.findAll().stream()
                .filter(j -> machine == null || machine.isEmpty() || j.getMachineName().equals(machine))
                .filter(j -> job == null || job.isEmpty() || j.getJobName().equalsIgnoreCase(job))
                .filter(j -> start == null || (j.getStartTime() != null && !j.getStartTime().isBefore(start)))
                .filter(j -> end == null || (j.getStartTime() != null && !j.getStartTime().isAfter(end)))
                .collect(Collectors.toList());

       
        Comparator<JobExecution> comparator;
        switch (sortField) {
            case "jobName":
                comparator = Comparator.comparing(JobExecution::getJobName);
                break;
            case "durationMs":
                comparator = Comparator.comparing(j -> j.getDurationMs() != null ? j.getDurationMs() : 0L);
                break;
            case "status":
                comparator = Comparator.comparing(j -> j.getStatus().name());
                break;
            case "startTime":
            default:
                comparator = Comparator.comparing(JobExecution::getStartTime);
        }
        if ("desc".equalsIgnoreCase(sortOrder)) {
            comparator = comparator.reversed();
        }
        filtered.sort(comparator);

       
        int totalElements = filtered.size();
        int totalPages = (int) Math.ceil((double) totalElements / size);
        int fromIndex = page * size;
        if (fromIndex >= totalElements && totalElements > 0) {
            fromIndex = 0;   
        }
        int toIndex = Math.min(fromIndex + size, totalElements);
        List<JobExecution> paged = totalElements > 0 ? filtered.subList(fromIndex, toIndex) : Collections.emptyList();

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", paged);
        result.put("totalElements", totalElements);
        result.put("totalPages", totalPages);
        result.put("currentPage", page);
        result.put("pageSize", size);
        return result;
    }


    @GetMapping("/api/executions/by-exec-id/{executionId}/errors")
    public List<ErrorLog> errorsByExecId(@PathVariable String executionId) {
        return errorRepo.findAll().stream()
                .filter(e -> e.getExecutionId().equals(executionId))
                .collect(Collectors.toList());
    }


    @GetMapping("/api/executions/by-exec-id/{executionId}/meters")
    public List<FlowMeter> metersByExecId(@PathVariable String executionId) {
        return meterRepo.findAll().stream()
                .filter(m -> m.getExecutionId().equals(executionId))
                .collect(Collectors.toList());
    }
}