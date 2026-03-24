# Submission Checklist - Fitness Club Membership Portal

Use this checklist before final submission.

## 1) Packaging
- [ ] Slide deck exported to PDF.
- [ ] Source code zipped from latest `main` branch.
- [ ] SQL script exported as text file (`fitness_club.sql`).
- [ ] Documentation/report file prepared (Excel or equivalent).

## 2) Functional Flow (Thymeleaf + Spring Boot)
- [ ] Open `/` (landing page works).
- [ ] Open `/enroll` and submit valid form.
- [ ] Redirect flow works: `/enroll/result/{id}` -> `/contract-review/{id}` -> `/contract/{id}`.
- [ ] Digital signature works and PDF downloads.
- [ ] `contract_pdf_path` is saved after sign/finalize.

## 3) Requirement A - Plan Enrollment
- [ ] Required fields: first name, last name, DOB, health goals.
- [ ] Plan types: Basic and Premium.
- [ ] Basic requires one primary branch.
- [ ] Premium displays access to all 5 branches.
- [ ] Start date and contract duration (Monthly / 6-month / Annual) work.

## 4) Requirement B - Enrollment Cart
- [ ] Add-ons: PT and Locker.
- [ ] Billing type toggle: Monthly and One-time upfront.
- [ ] Realtime total updates on enroll page.
- [ ] Monthly billing total follows first-month charging rules.

## 5) Database (DDL + DML)
- [ ] DDL creates: `members`, `branches`, `users`, `membership_enrollments`, `enrollment_addons`.
- [ ] DML inserts mock data (>= 5 records, currently 10+).
- [ ] GET endpoints return real seeded data.

## 6) Security Evidence
- [ ] Unauthorized access screenshot (401/403) for protected endpoint.
- [ ] Successful login screenshot/result (200 or redirect).
- [ ] DB screenshot proving `users.password` is BCrypt hash (`$2...`).

## 7) Demo Readiness
- [ ] Explain architecture: Controller -> Service -> Repository -> MySQL.
- [ ] Explain pricing rules (plan/PT/locker and monthly behavior).
- [ ] Prepare 3 test cases (normal, boundary, invalid).
- [ ] Prepare fallback explanation for enrollment lookup (JPA + SQL fallback).
