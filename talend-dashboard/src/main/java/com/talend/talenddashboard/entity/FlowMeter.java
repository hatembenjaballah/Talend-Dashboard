package com.talend.talenddashboard.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"machineName", "executionId", "meterName", "counterName", "value", "timestamp"}))
public class FlowMeter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String machineName;
    private String jobName;
    private LocalDateTime timestamp;
    private String meterName;
    private String counterName;
    private Long value;         // count
    private Long reference;     // ← nouveau champ
    @Column(length = 500)
    private String thresholds; // JSON / texte des seuils
    private String executionId;

    public FlowMeter() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMachineName() { return machineName; }
    public void setMachineName(String machineName) { this.machineName = machineName; }
    public String getJobName() { return jobName; }
    public void setJobName(String jobName) { this.jobName = jobName; }
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    public String getMeterName() { return meterName; }
    public void setMeterName(String meterName) { this.meterName = meterName; }
    public String getCounterName() { return counterName; }
    public void setCounterName(String counterName) { this.counterName = counterName; }
    public Long getValue() { return value; }
    public void setValue(Long value) { this.value = value; }
    public Long getReference() { return reference; }
    public void setReference(Long reference) { this.reference = reference; }
    public String getThresholds() { return thresholds; }
    public void setThresholds(String thresholds) { this.thresholds = thresholds; }
    public String getExecutionId() { return executionId; }
    public void setExecutionId(String executionId) { this.executionId = executionId; }
}