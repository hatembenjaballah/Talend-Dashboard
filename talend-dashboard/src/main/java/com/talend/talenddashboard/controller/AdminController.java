package com.talend.talenddashboard.controller;

import com.talend.talenddashboard.entity.Machine;
import com.talend.talenddashboard.repository.*;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final MachineRepository machineRepo;
    private final JobExecutionRepository jobExecRepo;
    private final ErrorLogRepository errorLogRepo;
    private final FlowMeterRepository flowMeterRepo;

    public AdminController(MachineRepository machineRepo,
                           JobExecutionRepository jobExecRepo,
                           ErrorLogRepository errorLogRepo,
                           FlowMeterRepository flowMeterRepo) {
        this.machineRepo = machineRepo;
        this.jobExecRepo = jobExecRepo;
        this.errorLogRepo = errorLogRepo;
        this.flowMeterRepo = flowMeterRepo;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("machines", machineRepo.findAll());
        return "admin";
    }

    @PostMapping("/machine")
    public String save(@ModelAttribute Machine machine) {
        machineRepo.save(machine);
        return "redirect:/admin";
    }

    @GetMapping("/machine/{id}/delete")
    @Transactional   // <-- AJOUTEZ CETTE ANNOTATION
    public String delete(@PathVariable Long id) {
        Machine machine = machineRepo.findById(id).orElse(null);
        if (machine != null) {
            String machineName = machine.getName();

            // Suppression des données liées
            deleteAllDataForMachine(machineName);

            // Suppression de la machine
            machineRepo.deleteById(id);
        }
        return "redirect:/admin";
    }

    private void deleteAllDataForMachine(String machineName) {
        jobExecRepo.deleteAllByMachineName(machineName);
        errorLogRepo.deleteAllByMachineName(machineName);
        flowMeterRepo.deleteAllByMachineName(machineName);
    }
}