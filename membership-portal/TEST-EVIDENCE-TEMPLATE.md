# Test Evidence Template

Fill this file while collecting screenshots/logs for the report.

## A. Browser Flow Evidence

### A1. Enrollment form
- URL: `http://localhost:8080/enroll`
- Input used:
  - firstName:
  - lastName:
  - dob:
  - healthGoals:
  - planType:
  - primaryBranchId:
  - startDate:
  - contractDuration:
  - billingType:
  - PT qty:
  - Locker qty:
- Expected:
  - Form submits successfully.
  - Redirect to `/enroll/result/{id}`.
- Screenshot file:

### A2. Result page
- URL: `http://localhost:8080/enroll/result/{id}`
- Expected:
  - Member info, plan info, add-ons, total displayed.
  - Button navigates to `/contract-review/{id}`.
- Screenshot file:

### A3. Contract review page
- URL: `http://localhost:8080/contract-review/{id}`
- Expected:
  - Correct summary and total amount.
  - Button navigates to `/contract/{id}`.
- Screenshot file:

### A4. Signature + PDF
- URL: `http://localhost:8080/contract/{id}`
- Expected:
  - Signature pad usable.
  - Click "Ký & Tải PDF" downloads PDF.
- Screenshot file:

## B. API Evidence (Postman/Browser)

### B1. Protected endpoint unauthorized (401/403)
- Endpoint: `GET /api/branches` (or `/api/members`, `/api/enrollments/ids`)
- Without auth:
- Expected status: 401 or 403
- Screenshot file:

### B2. Protected endpoint authorized (200)
- Endpoint:
- Auth used: HTTP Basic (`admin` / `admin123`)
- Expected status: 200
- Screenshot file:

### B3. Enrollment detail endpoint
- Endpoint: `GET /api/enrollments/{id}`
- Expected:
  - Enrollment with add-ons is returned.
- Screenshot file:

## C. Database Evidence

### C1. BCrypt password proof
- SQL:
```sql
SELECT username, password, role FROM users WHERE username='admin';
```
- Expected:
  - `password` starts with `$2` (BCrypt hash), not plain text.
- Screenshot file:

### C2. Enrollment data proof
- SQL:
```sql
SELECT id, member_id, plan_type, contract_duration, billing_type, total_amount, status
FROM membership_enrollments
ORDER BY id DESC
LIMIT 10;
```
- Screenshot file:

### C3. Add-on data proof
- SQL:
```sql
SELECT enrollment_id, addon_type, quantity, unit_price
FROM enrollment_addons
ORDER BY enrollment_id, id;
```
- Screenshot file:

## D. Notes for Presentation
- Boundary case tested:
- Error case tested:
- Key architecture points explained:
