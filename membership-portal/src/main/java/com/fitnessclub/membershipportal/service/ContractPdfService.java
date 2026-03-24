package com.fitnessclub.membershipportal.service;

import com.fitnessclub.membershipportal.entity.*;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;

@Service
public class ContractPdfService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Generate PDF contract for a finalized enrollment and return bytes.
     */
    public byte[] generatePdfBytes(MembershipEnrollment enrollment) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        document.add(new Paragraph("FITNESS CLUB - MEMBERSHIP CONTRACT", titleFont));
        document.add(new Paragraph(" "));

        Member m = enrollment.getMember();
        document.add(new Paragraph("Member: " + m.getFirstName() + " " + m.getLastName(), normalFont));
        document.add(new Paragraph("Date of Birth: " + (m.getDob() != null ? m.getDob().format(DATE_FMT) : "N/A"), normalFont));
        document.add(new Paragraph("Health Goals: " + (m.getHealthGoals() != null ? m.getHealthGoals() : "N/A"), normalFont));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Plan: " + enrollment.getPlanType().name(), normalFont));
        if (enrollment.getPrimaryBranch() != null) {
            document.add(new Paragraph("Primary Branch: " + enrollment.getPrimaryBranch().getName() + ", " + enrollment.getPrimaryBranch().getCity(), normalFont));
        } else {
            document.add(new Paragraph("Access: All 5 city branches (Premium)", normalFont));
        }
        document.add(new Paragraph("Start Date: " + enrollment.getStartDate().format(DATE_FMT), normalFont));
        document.add(new Paragraph("Contract Duration: " + enrollment.getContractDuration().name(), normalFont));
        document.add(new Paragraph("Billing: " + enrollment.getBillingType().name(), normalFont));
        document.add(new Paragraph(" "));

        if (!enrollment.getAddOns().isEmpty()) {
            document.add(new Paragraph("Add-ons:", normalFont));
            for (EnrollmentAddOn addOn : enrollment.getAddOns()) {
                int qty = addOn.getQuantity() != null ? addOn.getQuantity() : 0;
                int displayQty = qty;
                BigDecimal unit = addOn.getUnitPrice() != null ? addOn.getUnitPrice() : BigDecimal.ZERO;
                BigDecimal lineTotal = unit.multiply(BigDecimal.valueOf(displayQty));
                document.add(new Paragraph("  - " + addOn.getAddOnType().name() + " x " + displayQty + " @ " + unit + " = " + lineTotal, normalFont));
            }
            document.add(new Paragraph(" "));
        }

        BigDecimal total = enrollment.getTotalAmount() != null ? enrollment.getTotalAmount() : BigDecimal.ZERO;
        document.add(new Paragraph("Total Amount: " + total + " (according to " + enrollment.getBillingType() + ")", normalFont));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("By signing below, the member agrees to the terms and conditions of the Fitness Club.", normalFont));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("_________________________     Date: _______________", normalFont));
        document.add(new Paragraph("Member Signature (digital)", normalFont));

        document.close();
        return out.toByteArray();
    }

    /**
     * Generate PDF with embedded signature image (PNG base64 or raw bytes).
     * Replaces the placeholder line with the signature image.
     */
    public byte[] generatePdfBytes(MembershipEnrollment enrollment, byte[] signaturePng) throws DocumentException, IOException {
        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

        document.add(new Paragraph("FITNESS CLUB - MEMBERSHIP CONTRACT", titleFont));
        document.add(new Paragraph(" "));

        Member m = enrollment.getMember();
        document.add(new Paragraph("Member: " + m.getFirstName() + " " + m.getLastName(), normalFont));
        document.add(new Paragraph("Date of Birth: " + (m.getDob() != null ? m.getDob().format(DATE_FMT) : "N/A"), normalFont));
        document.add(new Paragraph("Health Goals: " + (m.getHealthGoals() != null ? m.getHealthGoals() : "N/A"), normalFont));
        document.add(new Paragraph(" "));

        document.add(new Paragraph("Plan: " + enrollment.getPlanType().name(), normalFont));
        if (enrollment.getPrimaryBranch() != null) {
            document.add(new Paragraph("Primary Branch: " + enrollment.getPrimaryBranch().getName() + ", " + enrollment.getPrimaryBranch().getCity(), normalFont));
        } else {
            document.add(new Paragraph("Access: All 5 city branches (Premium)", normalFont));
        }
        document.add(new Paragraph("Start Date: " + enrollment.getStartDate().format(DATE_FMT), normalFont));
        document.add(new Paragraph("Contract Duration: " + enrollment.getContractDuration().name(), normalFont));
        document.add(new Paragraph("Billing: " + enrollment.getBillingType().name(), normalFont));
        document.add(new Paragraph(" "));

        if (!enrollment.getAddOns().isEmpty()) {
            document.add(new Paragraph("Add-ons:", normalFont));
            for (EnrollmentAddOn addOn : enrollment.getAddOns()) {
                int qty = addOn.getQuantity() != null ? addOn.getQuantity() : 0;
                int displayQty = qty;
                BigDecimal unit = addOn.getUnitPrice() != null ? addOn.getUnitPrice() : BigDecimal.ZERO;
                BigDecimal lineTotal = unit.multiply(BigDecimal.valueOf(displayQty));
                document.add(new Paragraph("  - " + addOn.getAddOnType().name() + " x " + displayQty + " @ " + unit + " = " + lineTotal, normalFont));
            }
            document.add(new Paragraph(" "));
        }

        BigDecimal total = enrollment.getTotalAmount() != null ? enrollment.getTotalAmount() : BigDecimal.ZERO;
        document.add(new Paragraph("Total Amount: " + total + " (according to " + enrollment.getBillingType() + ")", normalFont));
        document.add(new Paragraph(" "));
        document.add(new Paragraph("By signing below, the member agrees to the terms and conditions of the Fitness Club.", normalFont));
        document.add(new Paragraph(" "));

        if (signaturePng != null && signaturePng.length > 0) {
            try {
                Image img = Image.getInstance(signaturePng);
                img.scaleToFit(200f, 80f);
                document.add(img);
            } catch (Exception e) {
                document.add(new Paragraph("_________________________     (signature)", normalFont));
            }
        } else {
            document.add(new Paragraph("_________________________     Date: _______________", normalFont));
        }
        document.add(new Paragraph("Member Signature (digital)", normalFont));

        document.close();
        return out.toByteArray();
    }

    /**
     * Save PDF to disk and return path (optional; for later download).
     */
    public String savePdfToFile(MembershipEnrollment enrollment, Path baseDir) throws IOException, DocumentException {
        Files.createDirectories(baseDir);
        String fileName = "contract-" + enrollment.getId() + ".pdf";
        Path filePath = baseDir.resolve(fileName);
        byte[] pdf = generatePdfBytes(enrollment);
        Files.write(filePath, pdf);
        return filePath.toString();
    }

    private int contractDurationMonths(ContractDuration duration) {
        if (duration == null) return 1;
        return switch (duration) {
            case MONTHLY -> 1;
            case SIX_MONTH -> 6;
            case ANNUAL -> 12;
        };
    }
}
