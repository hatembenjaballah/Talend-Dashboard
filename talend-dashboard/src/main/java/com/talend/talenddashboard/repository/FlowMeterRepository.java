package com.talend.talenddashboard.repository;

import com.talend.talenddashboard.entity.FlowMeter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlowMeterRepository extends JpaRepository<FlowMeter, Long> {
    void deleteAllByMachineName(String machineName);    
}