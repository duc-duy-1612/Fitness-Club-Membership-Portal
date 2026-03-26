package com.fitnessclub.membershipportal.controller;

import com.fitnessclub.membershipportal.repository.MemberRepository;
import com.fitnessclub.membershipportal.repository.MembershipEnrollmentRepository;
import com.fitnessclub.membershipportal.repository.UserAccountRepository;
import com.fitnessclub.membershipportal.service.EnrollmentService;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminUiController {

    private final MemberRepository memberRepository;
    private final MembershipEnrollmentRepository enrollmentRepository;
    private final EnrollmentService enrollmentService;
    private final UserAccountRepository userAccountRepository;

    public AdminUiController(MemberRepository memberRepository,
                             MembershipEnrollmentRepository enrollmentRepository,
                             EnrollmentService enrollmentService,
                             UserAccountRepository userAccountRepository) {
        this.memberRepository = memberRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.enrollmentService = enrollmentService;
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping
    @Transactional
    public String dashboard(Model model) {
        var members = memberRepository.findAll();
        var enrollments = enrollmentRepository.findAll();
        // Initialize lazy fields for view
        for (var e : enrollments) {
            if (e.getMember() != null) e.getMember().getFirstName();
            if (e.getPrimaryBranch() != null) e.getPrimaryBranch().getName();
            if (e.getAddOns() != null) e.getAddOns().size();
        }
        model.addAttribute("memberCount", members.size());
        model.addAttribute("enrollmentCount", enrollments.size());
        model.addAttribute("members", members);
        model.addAttribute("enrollments", enrollments);
        return "admin/dashboard";
    }

    @PostMapping("/members/{id}/delete")
    @Transactional
    public String deleteMember(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        var mOpt = memberRepository.findById(id);
        if (mOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("msg", "Không tìm thấy member id=" + id);
            return "redirect:/admin";
        }

        // Unlink user account if this member is linked to a login account.
        userAccountRepository.findByMember_Id(id).ifPresent(ua -> {
            ua.setMember(null);
            userAccountRepository.save(ua);
        });

        // Remove enrollments first to avoid FK constraints across DB configurations.
        var enrollments = enrollmentRepository.findByMember_IdOrderByCreatedAtDesc(id);
        if (!enrollments.isEmpty()) {
            enrollmentRepository.deleteAll(enrollments);
        }

        memberRepository.delete(mOpt.get());
        redirectAttributes.addFlashAttribute("msg", "Đã xóa member id=" + id + " và dữ liệu liên quan.");
        return "redirect:/admin";
    }

    // Fallback for browsers/UI flows that navigate to delete URL by GET.
    @GetMapping("/members/{id}/delete")
    @Transactional
    public String deleteMemberByGet(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        return deleteMember(id, redirectAttributes);
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userAccountRepository.findAll());
        return "admin/users";
    }

    @PostMapping("/users/{id}/role")
    public String updateUserRole(@PathVariable Integer id,
                                 @RequestParam String role,
                                 RedirectAttributes redirectAttributes) {
        var opt = userAccountRepository.findById(id);
        if (opt.isEmpty()) {
            redirectAttributes.addFlashAttribute("msg", "Không tìm thấy user id=" + id);
            return "redirect:/admin/users";
        }
        String normalized = role != null ? role.trim().toUpperCase() : "";
        if (!("ADMIN".equals(normalized) || "USER".equals(normalized))) {
            redirectAttributes.addFlashAttribute("msg", "Role không hợp lệ.");
            return "redirect:/admin/users";
        }
        var ua = opt.get();
        ua.setRole(normalized);
        userAccountRepository.save(ua);
        redirectAttributes.addFlashAttribute("msg", "Đã cập nhật role cho " + ua.getUsername() + " → " + normalized);
        return "redirect:/admin/users";
    }

    @GetMapping("/enrollments/{id}")
    public String enrollmentDetail(@PathVariable Integer id, Model model) {
        var opt = enrollmentService.getEnrollmentWithAddOns(id);
        if (opt.isEmpty()) return "redirect:/admin";
        var e = opt.get();
        if (e.getMember() != null) e.getMember().getFirstName();
        if (e.getPrimaryBranch() != null) e.getPrimaryBranch().getName();
        model.addAttribute("enrollment", e);
        return "admin/enrollment-detail";
    }

    @GetMapping("/members/{id}")
    @Transactional
    public String memberDetail(@PathVariable Integer id, Model model) {
        var mOpt = memberRepository.findById(id);
        if (mOpt.isEmpty()) return "redirect:/admin";
        var member = mOpt.get();
        var enrollments = enrollmentRepository.findByMember_IdOrderByCreatedAtDesc(id);
        for (var e : enrollments) {
            if (e.getAddOns() != null) e.getAddOns().size();
            if (e.getPrimaryBranch() != null) e.getPrimaryBranch().getName();
        }
        model.addAttribute("member", member);
        model.addAttribute("enrollments", enrollments);
        return "admin/member-detail";
    }
}

