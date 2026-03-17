package com.fitnessclub.membershipportal.config;

import com.fitnessclub.membershipportal.entity.Branch;
import com.fitnessclub.membershipportal.repository.BranchRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds 5 city branches if none exist (for Premium: access to all 5).
 */
@Component
public class BranchDataInitializer implements ApplicationRunner {

    private final BranchRepository branchRepository;

    public BranchDataInitializer(BranchRepository branchRepository) {
        this.branchRepository = branchRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (branchRepository.count() > 0) return;
        List<Branch> branches = List.of(
                new Branch("Central Gym", "Ho Chi Minh City"),
                new Branch("North Fitness", "Hanoi"),
                new Branch("Coast Wellness", "Da Nang"),
                new Branch("South Branch", "Can Tho"),
                new Branch("Highland Club", "Da Lat")
        );
        branchRepository.saveAll(branches);
    }
}
