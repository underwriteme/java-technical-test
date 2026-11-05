# Insurance Technical Exercise

## Overview

You'll be working with a small Java codebase that models a simplified insurance application flow.

The system allows an adviser to:
- Share an application with customers
- Collect required customer details
- Trigger an underwriting decision
- Send appropriate emails based on the outcome

Some parts of the implementation are intentionally incomplete or incorrect.

---

## Getting started

```bash
./gradlew test
```

---

## Business rules

### Application sharing

An application can only be shared if all of the following are true:

- The application was created within the last 30 days
- Each customer has at least one insurance product selected
- Each customer has a valid email address
- All customers are under 80 years old at the policy start date *(79 is allowed; exactly 80 is not)*

When an application is shared:
- Each customer receives an invitation email *(simulated)*
- Each customer must provide: **height**, **weight**, and **profession**

Once all required information is available for all customers:
- The system requests an `underwriting` decision *(simulated)*
- Note `underwriting` is the process where a decision is made, Quote offered or Application Rejected

Based on the outcome:
- If a quote is available → a **"Buy Now"** email is sent
- If no quote is available → an **"Unable to offer cover"** email is sent

---

## The exercise

### Part 1 — Warmup

The age and email validation tests in `ApplicationServiceTest.ShareApplicationTest` are currently failing.
Your goal is to:

- Review the failing age‑ and email‑related tests
- Identify issues in the validation logic
- Fix the implementation so all Task 1 tests pass

You should not need to add or modify tests for this task.

---

### Part 2 — Bugfix

Modify the system to implement the desired behavior.

Current behavior

Underwriting is triggered as soon as **any** customer submits their details. This causes decisions to be requested too early. The previous developer did not create unit tests to cover the original behavior.

Expect behavior

Underwriting should only be triggered once **all** customers have provided height, weight, and profession. If any customer is missing any of these, underwriting must not be triggered.

---

### Part 3 — Extension

Pick **one** of the features below and implement it, including tests.

We're looking for clean integration with the existing codebase — consistent style,
appropriate use of the domain model, and clear test coverage of both the expected
behavior and relevant edge cases.

**Choose one:**

A. **Idempotent decision** — Once the underwriting decision has been triggered,
   repeated or duplicate customer detail submissions must not re-run the decision
   or resend outcome emails.

B. **Post-decision immutability** — After the underwriting decision has been triggered,
   customer details can no longer be modified. Attempts to update details on a decided
   application should fail in a clear and appropriate way.

C. **Product-specific readiness** — The information required before underwriting can
   be triggered depends on the products selected. For example, one product might
   require height and weight only, while another also requires profession. The decision
   must not trigger until each customer has provided everything their selected products
   require.

D. **Reopening a decided application** — After an underwriting decision has been made,
   allow the application to be reopened so that customer details can be corrected.
   Reopening should reset the decision state, and underwriting must be re-triggered
   once all customers are ready again.

---

### Part 4 — Communication Channels

Customers can now specify a preferred communication channel: **EMAIL**, **WHATSAPP**, or **SMS**.

**Requirements:**

1. All messages (invitation, buy-now, unable-to-offer) must be sent via the customer's preferred channel
2. If no preference is specified, default to EMAIL (existing behavior preserved)
3. Different customers on the same application may have different channel preferences
4. The design must be extensible — adding a new channel (e.g., PUSH_NOTIFICATION) in the future should not require modifying existing channel implementations

---

## Project structure

```
src/
├── main/java/com/example/insurance/
│   ├── domain/
│   │   ├── Application.java          — application aggregate
│   │   ├── Customer.java             — customer with details
│   │   ├── Product.java              — insurance product enum
│   │   └── ApplicationStatus.java    — DRAFT / SHARED
│   ├── port/
│   │   ├── EmailService.java         — email sending interface
│   │   ├── UnderwritingService.java  — underwriting interface
│   │   └── Decision.java             — underwriting decision enum
│   └── service/
│       ├── ApplicationService.java   — main service (start here)
│       ├── ShareResult.java          — sealed result of sharing
│       └── ValidationError.java      — validation error record
└── test/java/com/example/insurance/
    ├── fixture/
    │   ├── ApplicationFixture.java   — test data builders
    │   └── CustomerFixture.java      — test data builders
    └── service/
        ├── ApplicationServiceTest.java   — all exercise tests
        ├── EmailServiceMock.java         — hand-rolled mock
        └── UnderwritingServiceMock.java  — hand-rolled mock
```

## Purpose

This repository contains a self-contained coding exercise used exclusively as part of the UnderwriteMe engineering recruitment process.

It is intended to:

- Provide a consistent and fair way to evaluate candidates
- Assess technical skills, problem-solving ability and communication
- Offer a simplified, illustrative scenario for discussion

This repository is not part of any production system.

---

## Important Disclaimer

- The code and materials in this repository are provided solely for interview and assessment purposes
- They are not production-ready, not supported and not maintained
- No warranty, express or implied, is provided regarding correctness or fitness for any purpose
- This repository must not be used, reused, or adapted for commercial or production purposes

---

## Intellectual Property & Usage

- All content in this repository is owned by UnderwriteMe, unless otherwise stated
- Use of this repository is strictly limited to candidates participating in the recruitment process
- Any reuse, redistribution, or derivative work outside of this context is prohibited without explicit authorisation

---

## Repository Usage Guidelines

- Use this repository only within the context of your interview process
- Do not publicly share or distribute the exercise content
- Do not publish completed solutions unless explicitly permitted
- Treat this material as confidential to the recruitment process

---

## Contact

For any questions regarding this exercise, please contact your UnderwriteMe recruiter or interview coordinator.