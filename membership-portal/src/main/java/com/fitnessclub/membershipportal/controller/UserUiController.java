package com.fitnessclub.membershipportal.controller;

import com.fitnessclub.membershipportal.entity.Member;
import com.fitnessclub.membershipportal.entity.UserAccount;
import com.fitnessclub.membershipportal.repository.MembershipEnrollmentRepository;
import com.fitnessclub.membershipportal.repository.UserAccountRepository;
import com.fitnessclub.membershipportal.service.EnrollmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;

@Controller
@RequestMapping("/user")
public class UserUiController {
    private static final DecimalFormat MONEY_FMT = new DecimalFormat("#,##0.##");

    private final MembershipEnrollmentRepository enrollmentRepository;
    private final EnrollmentService enrollmentService;
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserUiController(MembershipEnrollmentRepository enrollmentRepository,
                            EnrollmentService enrollmentService,
                            UserAccountRepository userAccountRepository,
                            PasswordEncoder passwordEncoder) {
        this.enrollmentRepository = enrollmentRepository;
        this.enrollmentService = enrollmentService;
        this.userAccountRepository = userAccountRepository;
        this.passwordEncoder = passwordEncoder;
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
        BigDecimal total = e.getTotalAmount() != null ? e.getTotalAmount() : BigDecimal.ZERO;
        if (e.getBillingType() == com.fitnessclub.membershipportal.entity.BillingType.MONTHLY) {
            int months = switch (e.getContractDuration()) {
                case MONTHLY -> 1;
                case SIX_MONTH -> 6;
                case ANNUAL -> 12;
            };
            model.addAttribute("displayMonthlyAmount", formatMoney(total));
            model.addAttribute("displayTotalAmount", formatMoney(total.multiply(BigDecimal.valueOf(months))));
        } else {
            model.addAttribute("displayMonthlyAmount", null);
            model.addAttribute("displayTotalAmount", formatMoney(total));
        }
        model.addAttribute("enrollment", e);
        return "user/enrollment-detail";
    }

    private static String formatMoney(BigDecimal amount) {
        BigDecimal safe = amount != null ? amount : BigDecimal.ZERO;
        return MONEY_FMT.format(safe) + " đ";
    }

    @GetMapping({"/settings", "/settings/"})
    public String settings(Authentication authentication,
                           @RequestParam(required = false) String updated,
                           @RequestParam(required = false) String error,
                           Model model) {
        String username = authentication != null ? authentication.getName() : "";
        UserAccount account = userAccountRepository.findByUsername(username).orElse(null);
        Member member = account != null ? account.getMember() : null;

        model.addAttribute("username", username);
        model.addAttribute("account", account);
        model.addAttribute("member", member);
        model.addAttribute("updated", updated);
        model.addAttribute("error", error);
        return "user/settings";
    }

    @PostMapping("/settings/profile")
    public String updateProfile(Authentication authentication,
                                @RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String dob,
                                @RequestParam(required = false) String healthGoals,
                                RedirectAttributes redirectAttributes) {
        String username = authentication != null ? authentication.getName() : "";
        UserAccount account = userAccountRepository.findByUsername(username).orElse(null);
        if (account == null || account.getMember() == null) {
            redirectAttributes.addAttribute("error", "Tài khoản chưa có hồ sơ hội viên. Vui lòng đăng ký lại.");
            return "redirect:/user/settings";
        }
        String fn = firstName != null ? firstName.trim() : "";
        String ln = lastName != null ? lastName.trim() : "";
        if (fn.isBlank() || ln.isBlank()) {
            redirectAttributes.addAttribute("error", "Vui lòng nhập đầy đủ Họ và Tên.");
            return "redirect:/user/settings";
        }
        LocalDate parsedDob;
        try {
            parsedDob = LocalDate.parse(dob);
        } catch (Exception e) {
            redirectAttributes.addAttribute("error", "Ngày sinh không hợp lệ.");
            return "redirect:/user/settings";
        }

        Member m = account.getMember();
        m.setFirstName(fn);
        m.setLastName(ln);
        m.setDob(parsedDob);
        m.setHealthGoals(healthGoals != null ? healthGoals : "");
        userAccountRepository.save(account);

        redirectAttributes.addAttribute("updated", "profile");
        return "redirect:/user/settings";
    }

    @PostMapping("/settings/email")
    public String updateEmail(Authentication authentication,
                              @RequestParam String newEmail,
                              HttpServletRequest request,
                              RedirectAttributes redirectAttributes) throws Exception {
        String username = authentication != null ? authentication.getName() : "";
        UserAccount account = userAccountRepository.findByUsername(username).orElse(null);
        if (account == null || account.getMember() == null) {
            redirectAttributes.addAttribute("error", "Tài khoản chưa có hồ sơ hội viên. Vui lòng đăng ký lại.");
            return "redirect:/user/settings";
        }
        String email = newEmail != null ? newEmail.trim() : "";
        if (email.isBlank() || !email.contains("@")) {
            redirectAttributes.addAttribute("error", "Email mới không hợp lệ.");
            return "redirect:/user/settings";
        }
        if (!email.equalsIgnoreCase(username) && userAccountRepository.existsByUsername(email)) {
            redirectAttributes.addAttribute("error", "Email này đã được sử dụng.");
            return "redirect:/user/settings";
        }

        account.setUsername(email);
        account.getMember().setEmail(email);
        userAccountRepository.save(account);

        // Force re-login because username changed
        request.logout();
        return "redirect:/login?logout";
    }

    @PostMapping("/settings/password")
    public String updatePassword(Authentication authentication,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmNewPassword,
                                 HttpServletRequest request,
                                 RedirectAttributes redirectAttributes) throws Exception {
        String username = authentication != null ? authentication.getName() : "";
        UserAccount account = userAccountRepository.findByUsername(username).orElse(null);
        if (account == null) {
            redirectAttributes.addAttribute("error", "Không tìm thấy tài khoản.");
            return "redirect:/user/settings";
        }
        if (currentPassword == null || !passwordEncoder.matches(currentPassword, account.getPassword())) {
            redirectAttributes.addAttribute("error", "Mật khẩu hiện tại không đúng.");
            return "redirect:/user/settings";
        }
        if (newPassword == null || newPassword.length() < 6) {
            redirectAttributes.addAttribute("error", "Mật khẩu mới tối thiểu 6 ký tự.");
            return "redirect:/user/settings";
        }
        if (!newPassword.equals(confirmNewPassword)) {
            redirectAttributes.addAttribute("error", "Mật khẩu mới xác nhận không khớp.");
            return "redirect:/user/settings";
        }

        account.setPassword(passwordEncoder.encode(newPassword));
        userAccountRepository.save(account);

        request.logout();
        return "redirect:/login?logout";
    }
}

