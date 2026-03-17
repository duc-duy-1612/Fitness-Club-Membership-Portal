package com.fitnessclub.membershipportal.controller;

import com.fitnessclub.membershipportal.entity.Branch;
import com.fitnessclub.membershipportal.repository.BranchRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/branches")
public class BranchController {

    private final BranchRepository branchRepository;

    public BranchController(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    @GetMapping
    public List<Branch> getAllBranches() {
        return branchRepository.findAllByOrderByCityAscNameAsc();
    }
}
