package com.fitnessclub.membershipportal.controller;

import com.fitnessclub.membershipportal.entity.UserAccount;
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

@Controller
public class AuthController {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(UserAccountRepository userAccountRepository, PasswordEncoder passwordEncoder) {
        this.userAccountRepository = userAccountRepository;
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
                           RedirectAttributes redirectAttributes) {
        String u = username != null ? username.trim() : "";
        if (u.isEmpty()) {
            redirectAttributes.addAttribute("error", "Username không được để trống.");
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
        if (userAccountRepository.findByUsername(u).isPresent()) {
            redirectAttributes.addAttribute("error", "Username đã tồn tại.");
            return "redirect:/register";
        }

        String encoded = passwordEncoder.encode(password);
        userAccountRepository.save(new UserAccount(u, encoded, "USER"));
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

