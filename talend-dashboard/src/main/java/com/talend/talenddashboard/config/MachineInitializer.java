package com.talend.talenddashboard.config;

import com.talend.talenddashboard.entity.Machine;
import com.talend.talenddashboard.repository.MachineRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class MachineInitializer implements ApplicationRunner {
    private final MachineProperties machineProperties;
    private final MachineRepository machineRepo;

    public MachineInitializer(MachineProperties machineProperties, MachineRepository machineRepo) {
        this.machineProperties = machineProperties;
        this.machineRepo = machineRepo;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (MachineProperties.MachineConfig cfg : machineProperties.getList()) {
            if (machineRepo.findByName(cfg.getName()).isEmpty()) {
            	Machine m = new Machine();
            	m.setName(cfg.getName());
            	m.setStatsPath(cfg.getStats().getPath());
            	m.setLogsPath(cfg.getLogs().getPath());
            	m.setMetersPath(cfg.getMeters().getPath());
                machineRepo.save(m);
            }
        }
    }
}