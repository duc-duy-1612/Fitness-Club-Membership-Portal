package com.fitnessclub.membershipportal.repository;

import com.fitnessclub.membershipportal.entity.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Integer> {

    List<Branch> findAllByOrderByCityAscNameAsc();
}
