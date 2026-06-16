package com.talend.talenddashboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"machineName", "executionId"}))
public class JobExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String machineName;

    @Column(nullable = false)
    private String jobName;

    private String pid;
    private String context;

    @Column(nullable = false)
    private String executionId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Long durationMs;

    @Enumerated(EnumType.STRING)
    private ExecutionStatus status = ExecutionStatus.RUNNING;

    public enum ExecutionStatus { SUCCESS, FAILURE, RUNNING }

    public JobExecution() {}
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMachineName() { return machineName; }
    public void setMachineName(String machineName) { this.machineName = machineName; }
    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }
    public String getPid() { return pid; }
    public void setPid(String pid) { this.pid = pid; }
    public String getContext() { return context; }
    public void setContext(String context) { this.context = context; }
    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    public Long getDurationMs() { return durationMs; }
    public void setDurationMs(Long durationMs) { this.durationMs = durationMs; }
    public ExecutionStatus getStatus() { return status; }
    public void setStatus(ExecutionStatus status) { this.status = status; }
}