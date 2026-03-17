package com.fitnessclub.membershipportal.controller;

import com.fitnessclub.membershipportal.dto.EnrollmentRequest;
import com.fitnessclub.membershipportal.entity.*;
import com.fitnessclub.membershipportal.repository.BranchRepository;
import com.fitnessclub.membershipportal.service.EnrollmentService;
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

    public EnrollmentPageController(BranchRepository branchRepository, EnrollmentService enrollmentService) {
        this.branchRepository = branchRepository;
        this.enrollmentService = enrollmentService;
    }

    @GetMapping
    public String form(Model model) {
        model.addAttribute("branches", branchRepository.findAllByOrderByCityAscNameAsc());
        model.addAttribute("planTypes", PlanType.values());
        model.addAttribute("durations", ContractDuration.values());
        model.addAttribute("billingTypes", BillingType.values());
        model.addAttribute("addOnTypes", AddOnType.values());
        return "enroll";
    }

    @PostMapping
    public String submit(@RequestParam String firstName,
                         @RequestParam String lastName,
                         @RequestParam String dob,
                         @RequestParam(required = false) String healthGoals,
                         @RequestParam String planType,
                         @RequestParam(required = false) Integer primaryBranchId,
                         @RequestParam String startDate,
                         @RequestParam String contractDuration,
                         @RequestParam String billingType,
                         @RequestParam(defaultValue = "0") int personalTrainingQty,
                         @RequestParam(defaultValue = "0") int lockerRentalQty,
                         RedirectAttributes redirectAttributes) {
        EnrollmentRequest req = new EnrollmentRequest();
        req.setFirstName(firstName);
        req.setLastName(lastName);
        req.setDob(java.time.LocalDate.parse(dob));
        req.setHealthGoals(healthGoals != null ? healthGoals : "");
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
        return "redirect:/contract-review/{id}";
    }

    @GetMapping("/result/{id}")
    public String result(@PathVariable Integer id, Model model) {
        var dtoOpt = enrollmentService.getEnrollmentResultDto(id);
        if (dtoOpt.isPresent()) {
            model.addAttribute("enrollmentResult", dtoOpt.get());
        }
        // when empty: still render result page so user sees "Không tìm thấy..." at same URL
        return "enroll-result";
    }
}
