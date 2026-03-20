package com.fitnessclub.membershipportal.service;

import com.fitnessclub.membershipportal.dto.EnrollmentRequest;
import com.fitnessclub.membershipportal.dto.EnrollmentResultDto;
import com.fitnessclub.membershipportal.entity.*;
import com.fitnessclub.membershipportal.repository.BranchRepository;
import com.fitnessclub.membershipportal.repository.MemberRepository;
import com.fitnessclub.membershipportal.repository.MembershipEnrollmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Service
public class EnrollmentService {

    private static final Logger log = LoggerFactory.getLogger(EnrollmentService.class);

    private final MemberRepository memberRepository;
    private final BranchRepository branchRepository;
    private final MembershipEnrollmentRepository enrollmentRepository;
    private final PricingService pricingService;
    private final ContractPdfService contractPdfService;
    private final JdbcTemplate jdbcTemplate;

    public EnrollmentService(MemberRepository memberRepository,
                             BranchRepository branchRepository,
                             MembershipEnrollmentRepository enrollmentRepository,
                             PricingService pricingService,
                             ContractPdfService contractPdfService,
                             JdbcTemplate jdbcTemplate) {
        this.memberRepository = memberRepository;
        this.branchRepository = branchRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.pricingService = pricingService;
        this.contractPdfService = contractPdfService;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Create a new enrollment (cart) from request: create/find member, plan, branch, add-ons, compute total.
     */
    @Transactional
    public MembershipEnrollment createEnrollment(EnrollmentRequest req) {
        Member member = findOrCreateMember(req);
        Branch primaryBranch = null;
        if (req.getPlanType() == PlanType.BASIC) {
            if (req.getPrimaryBranchId() == null) {
                throw new IllegalArgumentException("Basic plan requires a primary branch.");
            }
            primaryBranch = branchRepository.findById(req.getPrimaryBranchId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid primary branch."));
        }

        MembershipEnrollment enrollment = new MembershipEnrollment();
        enrollment.setMember(member);
        enrollment.setPlanType(req.getPlanType());
        enrollment.setPrimaryBranch(primaryBranch);
        enrollment.setStartDate(req.getStartDate());
        enrollment.setContractDuration(req.getContractDuration());
        enrollment.setBillingType(req.getBillingType());
        enrollment.setStatus("DRAFT");

        BigDecimal planBase = pricingService.planBaseForPeriod(req.getPlanType(), req.getContractDuration());
        enrollment.setPlanBaseAmount(planBase);

        int contractMonths = pricingService.durationMonths(req.getContractDuration());
        int ptMax;
        if (req.getContractDuration() == ContractDuration.MONTHLY) {
            // Contract monthly => PT max = 30 days
            ptMax = 30;
        } else if (req.getContractDuration() == ContractDuration.SIX_MONTH) {
            // Fixed PT max for 6 months contracts
            ptMax = 180;
        } else if (req.getContractDuration() == ContractDuration.ANNUAL) {
            // Fixed PT max for 1-year contracts
            ptMax = 365;
        } else {
            // Fallback to 30 if contract duration is unexpected/null
            ptMax = 30;
        }

        for (EnrollmentRequest.AddOnItem item : req.getAddOns()) {
            if (item.getType() == null || item.getQuantity() <= 0) continue;
            BigDecimal unitPrice = pricingService.unitPriceForAddOn(item.getType());

            int qty = item.getQuantity();
            if (item.getType() == AddOnType.PERSONAL_TRAINING) {
                // PT sessions/days must be <= computed max days for the contract/billing type.
                qty = Math.min(qty, ptMax);
            }
            if (item.getType() == AddOnType.LOCKER_RENTAL) {
                // Locker quantity is "months rented"
                // Allow 0..contractMonths regardless of billing type.
                qty = Math.min(qty, contractMonths);
            }

            EnrollmentAddOn addOn = new EnrollmentAddOn(item.getType(), qty, unitPrice);
            enrollment.addAddOn(addOn);
        }

        BigDecimal total = pricingService.computeTotal(enrollment);
        enrollment.setTotalAmount(total);

        enrollment = enrollmentRepository.saveAndFlush(enrollment);
        return enrollment;
    }

    /**
     * Finalize enrollment: set status to FINALIZED, save, then generate PDF and return bytes for download.
     */
    @Transactional
    public byte[] finalizeAndGetPdf(Integer enrollmentId) throws Exception {
        MembershipEnrollment enrollment = enrollmentRepository.findByIdWithAddOns(enrollmentId);
        if (enrollment == null) {
            throw new IllegalArgumentException("Enrollment not found: " + enrollmentId);
        }
        if (!"FINALIZED".equals(enrollment.getStatus())) {
            enrollment.setStatus("FINALIZED");
            enrollmentRepository.save(enrollment);
        }

        byte[] pdfBytes = contractPdfService.generatePdfBytes(enrollment);

        // Persist a PDF path for the contract (so rubric "save to DB" can be satisfied)
        Path baseDir = Paths.get(System.getProperty("user.home"), "fitnessclub", "contracts");
        Files.createDirectories(baseDir);
        Path filePath = baseDir.resolve("membership-contract-" + enrollmentId + ".pdf");
        Files.write(filePath, pdfBytes);

        enrollment.setContractPdfPath(filePath.toString());
        enrollmentRepository.saveAndFlush(enrollment);

        return pdfBytes;
    }

    /**
     * Sign contract with signature image (PNG bytes), set status FINALIZED, generate PDF with embedded signature and return bytes.
     */
    @Transactional
    public byte[] signAndGetPdf(Integer enrollmentId, byte[] signaturePng) throws Exception {
        MembershipEnrollment enrollment = enrollmentRepository.findByIdWithAddOns(enrollmentId);
        if (enrollment == null) {
            throw new IllegalArgumentException("Enrollment not found: " + enrollmentId);
        }
        if (!"FINALIZED".equals(enrollment.getStatus())) {
            enrollment.setStatus("FINALIZED");
            enrollmentRepository.save(enrollment);
        }

        // Generate PDF with embedded signature, then persist file path to DB.
        byte[] pdfBytes = contractPdfService.generatePdfBytes(enrollment, signaturePng);

        Path baseDir = Paths.get(System.getProperty("user.home"), "fitnessclub", "contracts");
        Files.createDirectories(baseDir);
        Path filePath = baseDir.resolve("membership-contract-" + enrollmentId + ".pdf");
        Files.write(filePath, pdfBytes);

        enrollment.setContractPdfPath(filePath.toString());
        enrollmentRepository.saveAndFlush(enrollment);

        return pdfBytes;
    }

    private Member findOrCreateMember(EnrollmentRequest req) {
        if (req.getFirstName() == null || req.getLastName() == null || req.getDob() == null) {
            throw new IllegalArgumentException("Member first name, last name and DOB are required.");
        }
        Member member = new Member(req.getFirstName(), req.getLastName(), req.getDob(), req.getHealthGoals());
        return memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public Optional<MembershipEnrollment> getEnrollmentWithAddOns(Integer id) {
        MembershipEnrollment e = enrollmentRepository.findByIdWithAddOns(id);
        if (e == null) return Optional.empty();
        if (e.getMember() != null) e.getMember().getFirstName();
        if (e.getPrimaryBranch() != null) e.getPrimaryBranch().getName();
        if (e.getAddOns() != null) e.getAddOns().size();
        return Optional.of(e);
    }

    /**
     * Build DTO for result page (all entity access inside transaction).
     * Tries JPA first; if empty, falls back to raw SQL (JdbcTemplate) to avoid mapping issues.
     */
    @Transactional(readOnly = true)
    public Optional<EnrollmentResultDto> getEnrollmentResultDto(Integer id) {
        Optional<EnrollmentResultDto> fromJpa = enrollmentRepository.findById(id)
                .map(e -> {
                    if (e.getMember() != null) e.getMember().getFirstName();
                    if (e.getPrimaryBranch() != null) e.getPrimaryBranch().getName();
                    if (e.getAddOns() != null) e.getAddOns().size();

                    EnrollmentResultDto dto = new EnrollmentResultDto();
                    dto.setMemberName(e.getMember() != null ? (e.getMember().getFirstName() + " " + e.getMember().getLastName()) : "N/A");
                    dto.setPlanType(e.getPlanType() != null ? e.getPlanType().toString() : "N/A");
                    if (e.getPrimaryBranch() != null) {
                        String city = e.getPrimaryBranch().getCity();
                        dto.setBranchInfo(e.getPrimaryBranch().getName() + (city != null && !city.isEmpty() ? ", " + city : ""));
                    } else {
                        dto.setBranchInfo("Tất cả 5 chi nhánh (Premium)");
                    }
                    dto.setStartDate(e.getStartDate() != null ? e.getStartDate().toString() : "N/A");
                    dto.setContractDuration(e.getContractDuration() != null ? e.getContractDuration().toString() : "N/A");
                    dto.setBillingType(e.getBillingType() != null ? e.getBillingType().toString() : "N/A");
                    if (e.getAddOns() != null) {
                        BillingType billingType = e.getBillingType();
                        for (EnrollmentAddOn a : e.getAddOns()) {
                            Integer qty = a.getQuantity();
                            BigDecimal unit = a.getUnitPrice() != null ? a.getUnitPrice() : BigDecimal.ZERO;

                            int displayQty = qty != null ? qty : 0;
                            if (billingType == BillingType.MONTHLY) {
                                if (a.getAddOnType() == AddOnType.PERSONAL_TRAINING) {
                                    // MONTHLY: chỉ tính PT cho 30 ngày đầu tiên
                                    displayQty = Math.min(displayQty, 30);
                                } else if (a.getAddOnType() == AddOnType.LOCKER_RENTAL) {
                                    // MONTHLY: chỉ tính tủ cho tháng đầu (0/1)
                                    displayQty = displayQty > 0 ? 1 : 0;
                                }
                            }
                            BigDecimal lineTotal = unit.multiply(BigDecimal.valueOf(displayQty));

                            String line = (a.getAddOnType() != null ? a.getAddOnType().toString() : "") + " x " + displayQty + " = " + lineTotal.toString();
                            dto.getAddOnLines().add(line);
                        }
                    }
                    dto.setTotalAmount(e.getTotalAmount() != null ? e.getTotalAmount().toString() : "0");
                    dto.setFinalizeUrl("/api/enrollments/" + id + "/finalize");
                    return dto;
                });
        if (fromJpa.isPresent()) return fromJpa;
        log.debug("JPA findById({}) empty, trying SQL fallback", id);
        Integer nativeId = enrollmentRepository.findIdNative(id);
        if (nativeId != null) {
            log.info("Enrollment id={} found by native query, loading via JdbcTemplate", id);
        }
        return loadEnrollmentResultBySql(id);
    }

    /** Fallback: load enrollment + member by raw SQL when JPA returns empty. */
    private Optional<EnrollmentResultDto> loadEnrollmentResultBySql(Integer id) {
        try {
            String mainSql = "SELECT e.id, e.member_id, e.plan_type, e.primary_branch_id, e.start_date, e.contract_duration, e.billing_type, e.total_amount, " +
                    "m.first_name, m.last_name, b.name AS branch_name, b.city AS branch_city " +
                    "FROM membership_enrollments e " +
                    "JOIN members m ON e.member_id = m.id " +
                    "LEFT JOIN branches b ON e.primary_branch_id = b.id " +
                    "WHERE e.id = ?";
            List<EnrollmentResultDto> list = jdbcTemplate.query(mainSql,
                    (rs, rowNum) -> {
                        EnrollmentResultDto dto = new EnrollmentResultDto();
                        String fn = rs.getString("first_name");
                        String ln = rs.getString("last_name");
                        dto.setMemberName((fn != null ? fn : "") + " " + (ln != null ? ln : ""));
                        dto.setPlanType(rs.getString("plan_type"));
                        if (rs.getObject("primary_branch_id") != null && rs.getString("branch_name") != null) {
                            String city = rs.getString("branch_city");
                            dto.setBranchInfo(rs.getString("branch_name") + (city != null && !city.isEmpty() ? ", " + city : ""));
                        } else {
                            dto.setBranchInfo("Tất cả 5 chi nhánh (Premium)");
                        }
                        java.sql.Date startDate = rs.getDate("start_date");
                        dto.setStartDate(startDate != null ? startDate.toLocalDate().toString() : "N/A");
                        String contractDuration = rs.getString("contract_duration");
                        dto.setContractDuration(contractDuration);
                        String billingTypeStr = rs.getString("billing_type");
                        dto.setBillingType(billingTypeStr);
                        dto.setTotalAmount(rs.getBigDecimal("total_amount") != null ? rs.getBigDecimal("total_amount").toString() : "0");
                        dto.setFinalizeUrl("/api/enrollments/" + id + "/finalize");
                        List<String> addOnLines = jdbcTemplate.query(
                                "SELECT addon_type, quantity, unit_price FROM enrollment_addons WHERE enrollment_id = ?",
                                (r, n) -> {
                                    BigDecimal unit = r.getBigDecimal("unit_price");
                                    int qty = r.getInt("quantity");
                                    String addonType = r.getString("addon_type");
                                    int displayQty = qty;
                                    if ("MONTHLY".equals(billingTypeStr)) {
                                        if ("PERSONAL_TRAINING".equals(addonType)) {
                                            displayQty = Math.min(qty, 30);
                                        } else if ("LOCKER_RENTAL".equals(addonType)) {
                                            displayQty = qty > 0 ? 1 : 0;
                                        }
                                    }
                                    BigDecimal lineTotal = unit != null ? unit.multiply(BigDecimal.valueOf(displayQty)) : BigDecimal.ZERO;
                                    return addonType + " x " + displayQty + " = " + lineTotal.toString();
                                },
                                id);
                        dto.setAddOnLines(addOnLines);
                        return dto;
                    },
                    id);
            if (list.isEmpty()) {
                log.warn("SQL fallback: no row in membership_enrollments for id={}", id);
                return Optional.empty();
            }
            return Optional.of(list.get(0));
        } catch (Exception e) {
            log.error("SQL fallback failed for enrollment id=" + id, e);
            return Optional.empty();
        }
    }

    public java.util.List<Integer> findAllEnrollmentIds() {
        return enrollmentRepository.findAll().stream()
                .map(MembershipEnrollment::getId)
                .toList();
    }
}