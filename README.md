# Vendor Onboarding AI Workflow Engine

A procurement-focused vendor onboarding system that combines deterministic workflow agents with optional AI assistance for unstructured document text, vendor communication, and audit summaries. Each submission produces an **Approved**, **Pending**, or **Rejected** decision with a full audit trail.

## Problem

Manual vendor onboarding in procurement is slow, inconsistent, and risky:

- Reviewers spend hours checking forms, PDFs, and bank details by hand
- Fraud and duplicate vendors slip through when checks are ad hoc
- Decisions lack structured reasoning and audit history

## Solution

This project implements a **controlled agentic workflow** that:

1. Accepts a vendor submission (form fields + PDF documents)
2. Runs nine sequential agents (validation, document extraction, risk checks, decision)
3. Persists every step, issue, decision, communication draft, and audit summary
4. Surfaces live progress in a React dashboard with polling

Final decisions are **rule-based and deterministic**. AI is used only where unstructured text helps (document understanding, email draft, audit narrative), with safe template fallbacks when no API key is configured.

## Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Java 17+, Spring Boot 3, Spring Data JPA |
| Frontend | React 18, TypeScript, Vite |
| Database | PostgreSQL 16 |
| PDF text | Apache PDFBox |
| AI (optional) | OpenAI-compatible HTTP API (`LLM_API_KEY`) |

## Agent Workflow

Each run executes these agents in order:

1. **Intake Agent** — normalizes submission context
2. **Document Understanding Agent** — PDF text extraction + structured field extraction (LLM or fallback)
3. **Completeness Agent** — required fields and documents
4. **Format Validation Agent** — email, tax ID, and format rules
5. **Consistency Check Agent** — cross-field and document consistency
6. **Duplicate & Risk Agent** — duplicate tax ID and blocked vendor checks against seeded data
7. **Decision Agent** — Approved / Pending / Rejected via rule engine
8. **Communication Agent** — vendor email draft (LLM or template fallback)
9. **Audit Summary Agent** — procurement audit narrative (LLM or template fallback)

## AI vs Rules

| Concern | Approach |
|---------|----------|
| Final decision | Deterministic `DecisionEngine` from issue severities |
| PDF text | PDFBox (no AI) |
| Structured document fields | LLM when configured; otherwise fallback stub |
| Vendor email & audit summary | LLM when configured; otherwise template fallback |
| Agent failures | WARNING + fallback — workflow continues unless a technical error occurs |

## Live Workflow

```text
New Submission (React)
  → POST /api/vendor-submissions/review (multipart)
  → WorkflowOrchestrator (async steps, ~800ms delay between steps)
  → PostgreSQL persistence
  → Run Details page polls GET /api/workflow-runs/{id} every 1s
  → Timeline + issues + decision + communication + audit summary
```

## Database Overview

Key tables:

- `vendor_submissions` — submitted vendor payload
- `uploaded_documents` — PDF metadata and extracted text
- `document_extractions` — structured fields per document
- `workflow_runs` — run status, current step, final decision
- `workflow_step_logs` — per-agent timeline entries
- `issues` — validation and risk findings
- `decisions` — status, risk score, triggered rules, required actions
- `communications` — vendor email draft
- `audit_summaries` — procurement audit narrative
- `existing_vendors` — seeded reference data for duplicate/blocked checks

## Demo Scenarios

Use **New Submission → Demo Scenarios** to prefill the form. Submit to verify the expected outcome.

| Scenario | Expected decision | Key trigger |
|----------|-------------------|-------------|
| Approved Vendor | **APPROVED** | Complete fields + 4 sample PDFs auto-attached |
| Missing Document | **PENDING** | No PDFs (+ missing business category) |
| Bank Name Mismatch | **PENDING** | `BANK_NAME_MISMATCH` (MEDIUM) |
| Duplicate Vendor | **REJECTED** | `DUPLICATE_TAX_ID` (CRITICAL) |
| Blocked Vendor | **REJECTED** | `BLOCKED_VENDOR_MATCH` (CRITICAL) |
| Wrong Document Uploaded | **PENDING** | `WRONG_DOCUMENT_TYPE` (MEDIUM) |

### Edge Cases

- **Wrong Document Uploaded → PENDING** — Uploaded document type does not match the expected slot. Action: ask the vendor to upload the correct document.

**Dashboard → Demo Tools**

- **Reset all runs** — clears workflow history; preserves `existing_vendors`
- **Reseed existing vendors** — restores the four default seed records

Sample PDFs live in `frontend/public/samples/` and are auto-loaded for the Approved Vendor scenario. Generate them with `mvn test -Dtest=GenerateSamplePdfsTest` from the `backend` directory if the folder is empty. The Wrong Document Uploaded scenario places a bank proof PDF in the tax registration slot (requires LLM configured for type detection).

## Setup

### Prerequisites

- Java 17 or 21
- Maven 3.9+
- Node.js 20+
- Docker (for PostgreSQL)

### 1. Start PostgreSQL

```bash
docker compose up -d
```

### 2. Configure environment (optional)

Copy `.env.example` to `.env` and adjust as needed. `LLM_API_KEY` is optional — the app runs fully with fallbacks when it is empty.

### 3. Start the backend

```bash
cd backend
mvn spring-boot:run
```

API base: `http://localhost:8080`

### 4. Start the frontend

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`

### Verify

```bash
cd backend && mvn test
cd frontend && npm run build
```

## Limitations

- Synthetic demo data only; no production integrations
- PDF text extraction only (no OCR for scanned documents)
- No real email delivery, payment, or third-party verification APIs
- No authentication or role-based access control
- AI outputs use template fallbacks without `LLM_API_KEY`

## Next Steps

- Approval queue UI for procurement reviewers
- Country-specific tax validation rules
- OCR for scanned PDFs
- External verification APIs (bank, tax registry)
- RBAC and audit export

## Case Study Notes

This project demonstrates:

- **Controlled workflow** — fixed agent sequence with persisted step logs
- **Rule-based decisioning** — Approved/Pending/Rejected from issue severity, not LLM opinion
- **AI where it helps** — document text, communication, and audit narrative only
- **Graceful degradation** — every AI step has a deterministic fallback
- **Full audit trail** — submissions, documents, issues, decisions, and summaries are queryable end-to-end

A reviewer can run all five demo scenarios from the UI alone using this README and the in-app Demo Scenarios panel.
