package com.fitnessclub.membershipportal.controller;

import com.fitnessclub.membershipportal.dto.EnrollmentRequest;
import com.fitnessclub.membershipportal.dto.SignContractRequest;
import com.fitnessclub.membershipportal.entity.MembershipEnrollment;
import com.fitnessclub.membershipportal.service.EnrollmentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final JdbcTemplate jdbcTemplate;

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    public EnrollmentController(EnrollmentService enrollmentService, JdbcTemplate jdbcTemplate) {
        this.enrollmentService = enrollmentService;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Create new enrollment (plan + add-ons + billing). Returns the saved enrollment with computed total.
     */
    @PostMapping
    public MembershipEnrollment createEnrollment(@RequestBody EnrollmentRequest request) {
        return enrollmentService.createEnrollment(request);
    }

    /**
     * Get enrollment by id (with add-ons).
     */
    @GetMapping("/{id}")
    public ResponseEntity<MembershipEnrollment> getEnrollment(@PathVariable Integer id) {
        Optional<MembershipEnrollment> opt = enrollmentService.getEnrollmentWithAddOns(id);
        return opt.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * List all enrollment IDs (for debugging: verify data in DB).
     */
    @GetMapping("/ids")
    public java.util.List<Integer> listEnrollmentIds() {
        return enrollmentService.findAllEnrollmentIds();
    }

    /**
     * Kiểm tra kết nối DB – so sánh với MySQL Workbench.
     * Mở: http://localhost:8081/api/enrollments/db-check
     */
    @GetMapping("/db-check")
    public Map<String, Object> dbCheck() {
        Map<String, Object> out = new HashMap<>();
        String safeUrl = datasourceUrl != null && datasourceUrl.contains("//")
            ? datasourceUrl.replaceFirst(":[^:@]+@", ":****@")
            : datasourceUrl;
        out.put("datasourceUrl", safeUrl);
        out.put("message", "So sánh enrollmentIds_from_app với SELECT id FROM membership_enrollments trong MySQL Workbench.");
        try {
            List<Integer> ids = jdbcTemplate.query(
                "SELECT id FROM membership_enrollments ORDER BY id DESC LIMIT 15",
                (rs, rowNum) -> rs.getInt("id"));
            out.put("enrollmentIds_from_app", ids);
            out.put("ok", true);
        } catch (Exception e) {
            out.put("ok", false);
            out.put("error", e.getMessage());
        }
        return out;
    }

    /**
     * Finalize enrollment and download PDF contract.
     */
    @RequestMapping(value = "/{id}/finalize", method = { RequestMethod.GET, RequestMethod.POST }, produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> finalizeAndDownloadPdf(@PathVariable Integer id) {
        try {
            byte[] pdf = enrollmentService.finalizeAndGetPdf(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "membership-contract-" + id + ".pdf");
            headers.setContentLength(pdf.length);
            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Ký & Tải PDF: nhận chữ ký base64, tạo PDF có nhúng chữ ký, cập nhật trạng thái FINALIZED, trả về file PDF.
     */
    @PostMapping(value = "/{id}/sign-pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> signAndDownloadPdf(@PathVariable Integer id, @RequestBody SignContractRequest request) {
        try {
            byte[] signaturePng = null;
            if (request != null && request.getSignatureDataUrl() != null && !request.getSignatureDataUrl().isEmpty()) {
                String dataUrl = request.getSignatureDataUrl();
                int comma = dataUrl.indexOf(',');
                if (comma >= 0) {
                    String base64 = dataUrl.substring(comma + 1);
                    signaturePng = Base64.getDecoder().decode(base64);
                }
            }
            byte[] pdf = enrollmentService.signAndGetPdf(id, signaturePng);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "hop-dong-hoi-vien-" + id + ".pdf");
            headers.setContentLength(pdf.length);
            return ResponseEntity.ok().headers(headers).body(pdf);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
