package com.fitnessclub.membershipportal.controller;

import com.fitnessclub.membershipportal.dto.EnrollmentRequest;
import com.fitnessclub.membershipportal.entity.*;
import com.fitnessclub.membershipportal.repository.BranchRepository;
import com.fitnessclub.membershipportal.repository.UserAccountRepository;
import com.fitnessclub.membershipportal.service.EnrollmentService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/enroll")
public class EnrollmentPageController {

    private final BranchRepository branchRepository;
    private final EnrollmentService enrollmentService;
    private final UserAccountRepository userAccountRepository;

    public EnrollmentPageController(BranchRepository branchRepository, EnrollmentService enrollmentService, UserAccountRepository userAccountRepository) {
        this.branchRepository = branchRepository;
        this.enrollmentService = enrollmentService;
        this.userAccountRepository = userAccountRepository;
    }

    @GetMapping
    public String form(Authentication authentication, Model model) {
        model.addAttribute("branches", branchRepository.findAllByOrderByCityAscNameAsc());
        model.addAttribute("planTypes", PlanType.values());
        model.addAttribute("durations", ContractDuration.values());
        model.addAttribute("billingTypes", BillingType.values());
        model.addAttribute("addOnTypes", AddOnType.values());
        String username = authentication != null ? authentication.getName() : "";
        model.addAttribute("prefillEmail", username);

        boolean lockPersonal = false;
        if (authentication != null && username != null && !username.isBlank()) {
            var account = userAccountRepository.findByUsername(username).orElse(null);
            if (account != null && account.getMember() != null) {
                var m = account.getMember();
                model.addAttribute("prefillFirstName", m.getFirstName());
                model.addAttribute("prefillLastName", m.getLastName());
                model.addAttribute("prefillDob", m.getDob() != null ? m.getDob().toString() : "");
                model.addAttribute("prefillHealthGoals", m.getHealthGoals());
                model.addAttribute("prefillEmail", m.getEmail() != null ? m.getEmail() : username);
                lockPersonal = true;
            }
        }

        model.addAttribute("lockPersonal", lockPersonal);
        return "enroll";
    }

    @PostMapping
    public String submit(@RequestParam String firstName,
                         @RequestParam String lastName,
                         @RequestParam String dob,
                         @RequestParam(required = false) String healthGoals,
                         @RequestParam(required = false) String email,
                         @RequestParam String planType,
                         @RequestParam(required = false) Integer primaryBranchId,
                         @RequestParam String startDate,
                         @RequestParam String contractDuration,
                         @RequestParam String billingType,
                         @RequestParam(defaultValue = "0") int personalTrainingQty,
                         @RequestParam(defaultValue = "0") int lockerRentalQty,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        EnrollmentRequest req = new EnrollmentRequest();

        if (authentication != null && authentication.getName() != null && !authentication.getName().isBlank()) {
            var account = userAccountRepository.findByUsername(authentication.getName()).orElse(null);
            if (account != null && account.getMember() != null) {
                // Locked personal info: ignore any incoming request params, always load from DB.
                var m = account.getMember();
                req.setFirstName(m.getFirstName());
                req.setLastName(m.getLastName());
                req.setDob(m.getDob());
                String goalsInput = healthGoals != null ? healthGoals.trim() : "";
                req.setHealthGoals(!goalsInput.isBlank() ? goalsInput : (m.getHealthGoals() != null ? m.getHealthGoals() : ""));
                req.setEmail(m.getEmail() != null && !m.getEmail().isBlank() ? m.getEmail().trim() : authentication.getName());
            } else {
                req.setFirstName(firstName);
                req.setLastName(lastName);
                req.setDob(java.time.LocalDate.parse(dob));
                req.setHealthGoals(healthGoals != null ? healthGoals : "");
                String normalizedEmail = email != null ? email.trim() : "";
                if (normalizedEmail.isBlank()) normalizedEmail = authentication.getName();
                req.setEmail(normalizedEmail);
            }
        } else {
            req.setFirstName(firstName);
            req.setLastName(lastName);
            req.setDob(java.time.LocalDate.parse(dob));
            req.setHealthGoals(healthGoals != null ? healthGoals : "");
            req.setEmail(email != null ? email.trim() : "");
        }

        req.setPlanType(PlanType.valueOf(planType));
        req.setPrimaryBranchId(primaryBranchId);
        req.setStartDate(java.time.LocalDate.parse(startDate));
        req.setContractDuration(ContractDuration.valueOf(contractDuration));
        req.setBillingType(BillingType.valueOf(billingType));

        List<EnrollmentRequest.AddOnItem> addOns = new ArrayList<>();
        if (personalTrainingQty > 0) {
            EnrollmentRequest.AddOnItem item = new EnrollmentRequest.AddOnItem();
            item.setType(AddOnType.PERSONAL_TRAINING);
            item.setQuantity(personalTrainingQty);
            addOns.add(item);
        }
        if (lockerRentalQty > 0) {
            EnrollmentRequest.AddOnItem item = new EnrollmentRequest.AddOnItem();
            item.setType(AddOnType.LOCKER_RENTAL);
            item.setQuantity(lockerRentalQty);
            addOns.add(item);
        }
        req.setAddOns(addOns);

        MembershipEnrollment enrollment = enrollmentService.createEnrollment(req);
        redirectAttributes.addAttribute("id", enrollment.getId());
        return "redirect:/enroll/result/{id}";
    }

    @GetMapping("/result/{id}")
    public String result(@PathVariable Integer id, Model model) {
        var dtoOpt = enrollmentService.getEnrollmentResultDto(id);
        if (dtoOpt.isPresent()) {
            model.addAttribute("enrollmentResult", dtoOpt.get());
        }
        model.addAttribute("enrollmentId", id);
        // when empty: still render result page so user sees "Không tìm thấy..." at same URL
        return "enroll-result";
    }
}
