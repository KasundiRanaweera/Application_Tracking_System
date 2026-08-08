# TalentBridge ATS — Backend API

A **single-company Applicant Tracking System** built with Java 21 + Spring Boot 4.

Candidates browse open jobs and apply through a clean REST API. Recruiters post jobs,
review applicants, rate them, add private internal notes, and drive each candidate
through a defined hiring pipeline — with a strict boundary between what each role can see.

> **Phase 1 — Backend API only.** The React frontend is a planned Phase 2.

---

## The Two Roles

| Role | How created | What they can do |
|---|---|---|
| **USER** (Candidate) | Self-registration via `POST /api/auth/register` | Browse open jobs, apply once per job, view and withdraw their own applications |
| **RECRUITER** | Seeded on startup — cannot self-register | Post and manage jobs, view all applicants, rate them, add internal notes, advance or reject through the hiring pipeline |

> Nobody can self-register as a recruiter. Recruiter accounts are created by seeding only.

### Seeded Recruiter Accounts (available after first boot)

| Name | Email | Password |
|---|---|---|
| Default Recruiter | `recruiter@talentbridge.com` | `Recruiter@123` |
| Second Recruiter | `recruiter2@talentbridge.com` | `Recruiter2@123` |

---

## Three Decisions That Make This Project Defensible

### 1. Candidate and recruiter views are completely separate

The same `Application` record returns **different JSON** depending on who asks.

- **Candidates** receive `ApplicationSummaryResponseDto` — status, job title, applied date. No `rating` field. No `notes` field. Not filtered at runtime — those fields do not exist in the DTO at all.
- **Recruiters** receive `ApplicationDetailResponseDto` — everything above plus rating, internal notes, candidate name, candidate email, resume URL, and cover note.

Internal data cannot leak to candidates because it is absent from the candidate response shape entirely. This is a design decision, not a runtime filter.

### 2. The hiring pipeline enforces rules, not free text

All application status transitions are validated by `PipelineValidator` before any database write occurs.

**Legal forward path:**
```
APPLIED → UNDER_REVIEW → SHORTLISTED → INTERVIEW → OFFER → HIRED
```

**Exit states (available from most active stages):**
```
→ REJECTED  (recruiter only)
→ WITHDRAWN (owning candidate only)
```

**Terminal states:** `HIRED`, `REJECTED`, `WITHDRAWN` — no further transitions allowed.

`PipelineValidator` also enforces **who** can make each move:
- Only a `RECRUITER` can advance or reject an application
- Only the `USER` (candidate) who owns an application can withdraw it
- A recruiter attempting to withdraw is rejected with `403 Forbidden`

28 unit tests in `PipelineValidatorTest` cover every legal transition, every illegal jump, every terminal state, and every role violation.

### 3. Owner checks on every candidate-facing fetch

A valid JWT proves **who someone is** — not which records they are allowed to access.

Every service method that fetches an application on a candidate's behalf calls `findByIdAndCandidateId()`, which verifies the application belongs to the calling candidate before returning it. A candidate cannot read another candidate's application by guessing its ID — they receive `404 Not Found` as if the record does not exist.

This pattern is applied consistently across:
- `GET /api/applications/me/{id}` — view single own application
- `DELETE /api/applications/{id}` — withdraw own application

---

## How to Run

### Prerequisites
- Java 21
- MySQL 8 running locally
- Maven (or use the included `./mvnw` wrapper)

### Steps

**1. Create the database**
```sql
CREATE DATABASE IF NOT EXISTS talentbridge_ats CHARACTER SET utf8mb4;
```

**2. Check credentials in `src/main/resources/application.properties`**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/talentbridge_ats?createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=your_password
```

**3. Run the application**
```bash
./mvnw spring-boot:run
```

The app starts on port **8080**. On first boot, Hibernate creates the tables and the `DataSeeder` inserts the two recruiter accounts automatically.

**4. Open Swagger UI**

[http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)

All endpoints are documented and testable directly from the browser.

### Authenticating in Swagger UI
1. Expand `POST /api/auth/login` → click **Try it out**
2. Enter recruiter or candidate credentials → click **Execute**
3. Copy the `token` value from the response
4. Click **Authorize** (top right of the page)
5. Enter `Bearer <your-token>` → click **Authorize**
6. All protected endpoints now work from the browser

---

## Complete API Reference

### Auth — `/api/auth`

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register a new candidate account |
| POST | `/api/auth/login` | Public | Log in and receive a JWT token |

### Jobs — `/api/jobs`

| Method | Path | Auth | Description |
|---|---|---|---|
| GET | `/api/jobs` | Any logged-in user | List all OPEN jobs. Supports filtering by `workMode`, `employmentType`, `location`; keyword `search` on title; sorting and pagination |
| GET | `/api/jobs/{id}` | Any logged-in user | View full detail of a single OPEN job. Returns 404 if not OPEN |
| POST | `/api/jobs` | RECRUITER | Create a new job (starts as DRAFT) |
| PUT | `/api/jobs/{id}` | RECRUITER (owner only) | Update a job — only allowed when status is DRAFT |
| DELETE | `/api/jobs/{id}` | RECRUITER (owner only) | Delete a job — only allowed when status is DRAFT |
| PATCH | `/api/jobs/{id}/status` | RECRUITER (owner only) | Change job status: `DRAFT → OPEN → CLOSED` |
| GET | `/api/jobs/manage/all` | RECRUITER | List all own jobs regardless of status, with optional keyword search and pagination |

### Applications — `/api/applications`

| Method | Path | Auth | Description |
|---|---|---|---|
| POST | `/api/applications` | USER | Apply to an open job. Includes optional `resumeUrl` and `coverNote`. Rejected with 409 if already applied to that job |
| GET | `/api/applications/me` | USER | List own applications with current status. Paginated. No internal recruiter data |
| GET | `/api/applications/me/{id}` | USER | View a single own application. Returns 404 if not owned by caller |
| DELETE | `/api/applications/{id}` | USER | Withdraw own application. Validated through `PipelineValidator` |
| GET | `/api/applications/job/{jobId}` | RECRUITER | List all applications for a job. Supports filtering by `status`, sorting by `rating` or `appliedAt`, and pagination |
| GET | `/api/applications/{id}` | RECRUITER | View full application detail including candidate info, rating, and all internal notes |
| PATCH | `/api/applications/{id}/rating` | RECRUITER | Assign or update a rating (1–5) on an application |
| POST | `/api/applications/{id}/notes` | RECRUITER | Add a timestamped internal note. Append-only — notes are never shown to candidates |
| PATCH | `/api/applications/{id}/status` | RECRUITER | Advance or reject an application. Validated by `PipelineValidator` — only legal transitions allowed |

---

## Hiring Pipeline Reference

```
                    ┌─────────┐
                    │ APPLIED │
                    └────┬────┘
                         │ Recruiter
                    ┌────▼──────────┐
                    │ UNDER_REVIEW  │
                    └────┬──────────┘
                         │ Recruiter
                    ┌────▼──────────┐
                    │  SHORTLISTED  │
                    └────┬──────────┘
                         │ Recruiter
                    ┌────▼──────────┐
                    │   INTERVIEW   │
                    └────┬──────────┘
                         │ Recruiter
                    ┌────▼──────────┐
                    │    OFFER      │
                    └────┬──────────┘
                         │ Recruiter
                    ┌────▼──────────┐
                    │    HIRED      │ ← Terminal
                    └───────────────┘

From any active stage:
  → REJECTED  (recruiter only)  ← Terminal
  → WITHDRAWN (candidate only)  ← Terminal
```

Invalid transitions are rejected with `400 Bad Request`. Attempts by the wrong role are rejected with `403 Forbidden`.

---

## Project Structure

```
src/
├── main/java/com/example/talentbridgeats/
│   ├── TalentbridgeAtsApplication.java
│   │
│   ├── config/
│   │   ├── DataSeeder.java            # Seeds recruiter accounts on startup
│   │   ├── OpenApiConfig.java         # Swagger UI with JWT bearer auth
│   │   └── SecurityConfig.java        # JWT filter chain, role-based access rules
│   │
│   ├── controller/
│   │   ├── AuthController.java        # /api/auth/register, /api/auth/login
│   │   ├── JobController.java         # /api/jobs/** (candidate + recruiter)
│   │   └── ApplicationController.java # /api/applications/** (candidate + recruiter)
│   │
│   ├── dto/
│   │   ├── AuthResponseDto.java
│   │   ├── RegisterRequestDto.java
│   │   ├── LoginRequestDto.java
│   │   ├── JobCreateRequestDto.java
│   │   ├── JobUpdateRequestDto.java
│   │   ├── JobStatusChangeRequestDto.java
│   │   ├── JobResponseDto.java
│   │   ├── ApplyRequestDto.java
│   │   ├── ApplicationSummaryResponseDto.java   # Candidate view (no rating/notes)
│   │   ├── ApplicationDetailResponseDto.java    # Recruiter view (full)
│   │   ├── RatingRequestDto.java
│   │   ├── NoteRequestDto.java
│   │   ├── NoteResponseDto.java
│   │   └── StatusChangeRequestDto.java
│   │
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── AccessDeniedException.java
│   │   ├── DuplicateApplicationException.java
│   │   ├── DuplicateEmailException.java
│   │   └── ResourceNotFoundException.java
│   │
│   ├── model/
│   │   ├── User.java
│   │   ├── Role.java                  # USER, RECRUITER
│   │   ├── Job.java
│   │   ├── JobStatus.java             # DRAFT, OPEN, CLOSED
│   │   ├── WorkMode.java              # ONSITE, REMOTE, HYBRID
│   │   ├── EmploymentType.java        # FULL_TIME, PART_TIME, CONTRACT, INTERNSHIP
│   │   ├── Application.java
│   │   ├── ApplicationStatus.java     # APPLIED → ... → HIRED / REJECTED / WITHDRAWN
│   │   └── ApplicationNote.java
│   │
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── JobRepository.java         # JpaSpecificationExecutor for filtering
│   │   ├── ApplicationRepository.java # JpaSpecificationExecutor + owner check queries
│   │   └── ApplicationNoteRepository.java
│   │
│   ├── security/
│   │   ├── JwtService.java            # Generate and validate JWT tokens
│   │   ├── JwtAuthFilter.java         # Intercepts every request, sets SecurityContext
│   │   ├── UserDetailsServiceImpl.java
│   │   └── CustomAccessDeniedHandler.java
│   │
│   ├── service/
│   │   ├── AuthService.java
│   │   ├── JobService.java
│   │   ├── ApplicationService.java
│   │   └── PipelineValidator.java     # All pipeline transition rules live here
│   │
│   └── util/
│       └── SecurityUtils.java         # getCurrentUserId() from auth context
│
└── test/java/com/example/talentbridgeats/
    ├── TalentbridgeAtsApplicationTests.java
    └── service/
        └── PipelineValidatorTest.java  # 28 tests: legal, illegal, terminal, role rules
```

---

## Database Schema

Four tables. `tbl_users` uses a `role` column as a discriminator — candidates and recruiters share the same table and authentication mechanism, differing only in their permissions.

```
tbl_users
  id, name, email (unique), password (bcrypt), role (USER|RECRUITER), created_at

tbl_jobs
  id, title, description, location, work_mode, employment_type,
  salary_min, salary_max, required_skills, status (DRAFT|OPEN|CLOSED),
  closing_date, posted_by (FK→tbl_users), created_at, updated_at

tbl_applications
  id, job_id (FK→tbl_jobs), candidate_id (FK→tbl_users),
  resume_url, cover_note,
  status (APPLIED|UNDER_REVIEW|SHORTLISTED|INTERVIEW|OFFER|HIRED|REJECTED|WITHDRAWN),
  rating (1–5, nullable), applied_at, updated_at
  UNIQUE (job_id, candidate_id)   ← prevents duplicate applications at DB level

tbl_application_notes
  id, application_id (FK→tbl_applications), recruiter_id (FK→tbl_users),
  content, created_at
```

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.0 |
| Security | Spring Security — stateless JWT |
| Persistence | Spring Data JPA + Hibernate |
| Database | MySQL 8 |
| Validation | Jakarta Bean Validation |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Boilerplate | Lombok |
| Build | Maven |

---

## Running Tests

```bash
./mvnw test
```

`PipelineValidatorTest` runs without a Spring context (pure unit test — fast). It covers:
- All 5 legal forward transitions (recruiter)
- Rejection from all 5 active stages (recruiter)
- Withdrawal from all 5 active stages (candidate)
- Illegal skips (e.g. `APPLIED → HIRED`)
- Backward moves (e.g. `INTERVIEW → APPLIED`)
- All 3 terminal states (`HIRED`, `REJECTED`, `WITHDRAWN`)
- Role violations (recruiter trying to withdraw, candidate trying to advance)