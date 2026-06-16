package com.talend.talenddashboard.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.talend.talenddashboard.entity.Machine;
import com.talend.talenddashboard.repository.MachineRepository;
import com.talend.talenddashboard.service.FileImportService;

@Controller
public class DashboardController {

    private final MachineRepository machineRepo;
    private final FileImportService importService;

    public DashboardController(MachineRepository machineRepo, FileImportService importService) {
        this.machineRepo = machineRepo;
        this.importService = importService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        List<Machine> machines = machineRepo.findAll();
        model.addAttribute("machines", machines);
        return "dashboard";
    }

    @GetMapping("/import")
    public String manualImport() {
        importService.importAll();
        return "redirect:/";
    }
}