package com.fitnessclub.membershipportal.controller;

import com.fitnessclub.membershipportal.entity.Member;
import com.fitnessclub.membershipportal.entity.UserAccount;
import com.fitnessclub.membershipportal.repository.MemberRepository;
import com.fitnessclub.membershipportal.repository.UserAccountRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class AuthController {

    private final UserAccountRepository userAccountRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserAccountRepository userAccountRepository, MemberRepository memberRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        model.addAttribute("hasError", error != null);
        model.addAttribute("loggedOut", logout != null);
        return "login";
    }

    @GetMapping("/register")
    public String registerPage(@RequestParam(required = false) String error,
                               @RequestParam(required = false) String success,
                               Model model) {
        model.addAttribute("error", error);
        model.addAttribute("success", success != null);
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           @RequestParam String confirmPassword,
                           @RequestParam String firstName,
                           @RequestParam String lastName,
                           @RequestParam String dob,
                           RedirectAttributes redirectAttributes) {
        String u = username != null ? username.trim() : "";
        if (u.isEmpty()) {
            redirectAttributes.addAttribute("error", "Username không được để trống.");
            return "redirect:/register";
        }
        if (!u.contains("@")) {
            redirectAttributes.addAttribute("error", "Username nên là email hợp lệ (ví dụ: you@gmail.com).");
            return "redirect:/register";
        }
        if (password == null || password.length() < 6) {
            redirectAttributes.addAttribute("error", "Password tối thiểu 6 ký tự.");
            return "redirect:/register";
        }
        if (!password.equals(confirmPassword)) {
            redirectAttributes.addAttribute("error", "Password xác nhận không khớp.");
            return "redirect:/register";
        }
        String fn = firstName != null ? firstName.trim() : "";
        String ln = lastName != null ? lastName.trim() : "";
        if (fn.isEmpty() || ln.isEmpty()) {
            redirectAttributes.addAttribute("error", "Vui lòng nhập đầy đủ Họ và Tên.");
            return "redirect:/register";
        }
        LocalDate parsedDob;
        try {
            parsedDob = LocalDate.parse(dob);
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Ngày sinh không hợp lệ.");
            return "redirect:/register";
        }
        if (userAccountRepository.findByUsername(u).isPresent()) {
            redirectAttributes.addAttribute("error", "Username đã tồn tại.");
            return "redirect:/register";
        }

        Member member = new Member(fn, ln, parsedDob, "");
        member.setEmail(u);
        member = memberRepository.save(member);

        String encoded = passwordEncoder.encode(password);
        UserAccount account = new UserAccount(u, encoded, "USER");
        account.setMember(member);
        userAccountRepository.save(account);
        redirectAttributes.addAttribute("success", "1");
        return "redirect:/register";
    }

    @GetMapping("/post-login")
    public String postLogin(Authentication authentication) {
        if (authentication == null) return "redirect:/";
        for (GrantedAuthority a : authentication.getAuthorities()) {
            String auth = a.getAuthority();
            if ("ROLE_ADMIN".equals(auth)) return "redirect:/admin";
            if ("ROLE_USER".equals(auth)) return "redirect:/user";
        }
        return "redirect:/";
    }
}

