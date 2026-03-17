package com.fitnessclub.membershipportal.controller;

import com.fitnessclub.membershipportal.service.EnrollmentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Bản xem trước hợp đồng – sau khi gửi form /enroll, chuyển hướng đến đây.
 */
@Controller
@RequestMapping("/contract-review")
public class ContractReviewController {

    private final EnrollmentService enrollmentService;

    public ContractReviewController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @GetMapping("/{id}")
    public String review(@PathVariable Integer id, Model model) {
        enrollmentService.getEnrollmentResultDto(id).ifPresent(dto -> model.addAttribute("enrollmentResult", dto));
        model.addAttribute("enrollmentId", id);
        return "contract-review";
    }
}
