package com.talend.talenddashboard.repository;

import com.talend.talenddashboard.entity.JobExecution;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface JobExecutionRepository extends JpaRepository<JobExecution, Long> {
    Optional<JobExecution> findByMachineNameAndExecutionId(String machineName, String executionId);
    void deleteAllByMachineName(String machineName);
}