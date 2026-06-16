package com.talend.talenddashboard.repository;

import com.talend.talenddashboard.entity.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {
    void deleteAllByMachineName(String machineName);   // <-- à ajouter
}