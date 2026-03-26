package com.fitnessclub.membershipportal.controller;

import com.fitnessclub.membershipportal.repository.MembershipEnrollmentRepository;
import com.fitnessclub.membershipportal.service.EnrollmentService;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
public class UserUiController {

    private final MembershipEnrollmentRepository enrollmentRepository;
    private final EnrollmentService enrollmentService;

    public UserUiController(MembershipEnrollmentRepository enrollmentRepository, EnrollmentService enrollmentService) {
        this.enrollmentRepository = enrollmentRepository;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    @Transactional
    public String dashboard(Authentication authentication,
                            @RequestParam(required = false) String denied,
                            Model model) {
        String username = authentication != null ? authentication.getName() : "";
        var enrollments = enrollmentRepository.findByMember_EmailOrderByCreatedAtDesc(username);

        // Hide duplicate DRAFT records when there is an existing FINALIZED contract
        // with the same configuration (so user only sees 1 "bản").
        var finalizedKeys = new java.util.HashSet<String>();
        for (var e : enrollments) {
            if (e != null && "FINALIZED".equals(e.getStatus())) {
                finalizedKeys.add(makeContractKey(e));
            }
        }

        var filtered = new java.util.ArrayList<com.fitnessclub.membershipportal.entity.MembershipEnrollment>();
        for (var e : enrollments) {
            if (e == null) continue;
            if ("DRAFT".equals(e.getStatus()) && finalizedKeys.contains(makeContractKey(e))) {
                continue; // skip draft that was already signed
            }
            filtered.add(e);
        }

        for (var e : filtered) {
            if (e.getMember() != null) e.getMember().getFirstName();
            if (e.getPrimaryBranch() != null) e.getPrimaryBranch().getName();
            if (e.getAddOns() != null) e.getAddOns().size();
        }
        model.addAttribute("username", username);
        model.addAttribute("enrollments", filtered);
        model.addAttribute("denied", denied != null);
        return "user/dashboard";
    }

    private String makeContractKey(com.fitnessclub.membershipportal.entity.MembershipEnrollment e) {
        if (e == null) return "";
        java.math.BigDecimal total = e.getTotalAmount();
        // Keep the key aligned with deletion logic: for signed contracts we only
        // match by plan/billing/duration + total, ignoring startDate/branch.
        return String.valueOf(e.getPlanType()) + "|" +
                String.valueOf(e.getBillingType()) + "|" +
                String.valueOf(e.getContractDuration()) + "|" +
                (total != null ? total.stripTrailingZeros().toPlainString() : "null");
    }

    @GetMapping("/enrollments/{id}")
    public String myEnrollment(@PathVariable Integer id, Authentication authentication, Model model) {
        String username = authentication != null ? authentication.getName() : "";
        var opt = enrollmentService.getEnrollmentWithAddOns(id);
        if (opt.isEmpty()) return "redirect:/user";
        var e = opt.get();
        String email = (e.getMember() != null) ? e.getMember().getEmail() : null;
        if (email == null || !email.equalsIgnoreCase(username)) {
            return "redirect:/user?denied=1";
        }
        if (e.getMember() != null) e.getMember().getFirstName();
        if (e.getPrimaryBranch() != null) e.getPrimaryBranch().getName();
        model.addAttribute("enrollment", e);
        return "user/enrollment-detail";
    }
}

