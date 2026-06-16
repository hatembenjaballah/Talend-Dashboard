package com.talend.talenddashboard.repository;

import com.talend.talenddashboard.entity.Machine;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface MachineRepository extends JpaRepository<Machine, Long> {
    Optional<Machine> findByName(String name);
}