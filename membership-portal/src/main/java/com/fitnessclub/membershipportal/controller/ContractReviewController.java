package com.fitnessclub.membershipportal.controller;

import com.fitnessclub.membershipportal.service.EnrollmentService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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
    public String review(@PathVariable Integer id, Authentication authentication, Model model) {
        if (!canAccessEnrollment(id, authentication)) throw new AccessDeniedException("Not allowed");
        enrollmentService.getEnrollmentResultDto(id).ifPresent(dto -> model.addAttribute("enrollmentResult", dto));
        model.addAttribute("enrollmentId", id);
        return "contract-review";
    }

    private boolean canAccessEnrollment(Integer id, Authentication authentication) {
        if (authentication == null) return false;
        boolean isAdmin = false;
        for (GrantedAuthority a : authentication.getAuthorities()) {
            if ("ROLE_ADMIN".equals(a.getAuthority())) {
                isAdmin = true;
                break;
            }
        }
        if (isAdmin) return true;

        String username = authentication.getName();
        var opt = enrollmentService.getEnrollmentWithAddOns(id);
        if (opt.isEmpty()) return false;
        var e = opt.get();
        String email = (e.getMember() != null) ? e.getMember().getEmail() : null;
        return email != null && email.equalsIgnoreCase(username);
    }
}
