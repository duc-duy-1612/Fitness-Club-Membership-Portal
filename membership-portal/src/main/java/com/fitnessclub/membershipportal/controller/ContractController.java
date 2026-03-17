package com.fitnessclub.membershipportal.controller;

import com.fitnessclub.membershipportal.service.EnrollmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Trang hợp đồng: bản xem trước + khung ký tên (signature_pad).
 * GET /contract/{id} -> contract.html
 */
@Controller
@RequestMapping("/contract")
public class ContractController {

    private final EnrollmentService enrollmentService;

    public ContractController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/{id}")
    public String contract(@PathVariable Integer id, Model model) {
        enrollmentService.getEnrollmentResultDto(id).ifPresent(dto -> model.addAttribute("enrollmentResult", dto));
        model.addAttribute("enrollmentId", id);
        return "contract";
    }
}
