package com.talend.talenddashboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"machineName", "executionId", "component", "exceptionMessage", "timestamp"}))
public class ErrorLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String machineName;
    private String jobName;
    private LocalDateTime timestamp;
    private String component;
    @Column(length = 2000)
    private String exceptionMessage;
    private String executionId;

    // Getters & setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMachineName() { return machineName; }
    public void setMachineName(String machineName) { this.machineName = machineName; }
    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getComponent() { return component; }
    public void setComponent(String component) { this.component = component; }
    public String getExceptionMessage() { return exceptionMessage; }
    public void setExceptionMessage(String exceptionMessage) { this.exceptionMessage = exceptionMessage; }
    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }
}