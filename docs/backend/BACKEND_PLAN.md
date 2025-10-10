# EasyBody Backend Plan (v1.0)

_Last updated: October 2025_

This document consolidates the engineering roadmap for the Spring Boot backend together with the legacy fix log from the early Node.js service. Use it as the single reference for current work, historical decisions, and quick links to supporting materials.

---

## 1. Active Roadmap

### Backend Focus
- ✅ Stabilise malformed components (`OfferService`, offer DTOs, `MediaController`) so the project compiles cleanly.
- 🔐 Harden authentication & authorization: validate Cognito JWTs, tighten Spring Security rules, and extend the security test suite.
- 📦 Finalise the moderation pipeline: publish moderation jobs to SQS, add the consumer that updates offer status, and cover everything with unit/integration tests.

### Database Focus
- 🗃️ Adopt Flyway migrations, enable PostGIS extensions, and create spatial + standard indexes to support nearby search.
- 📋 Seed baseline data (roles, gyms, PTs, sample offers) and switch Hibernate to `validate` once migrations own schema creation.
- 🚨 Add rating triggers plus health checks so aggregates stay correct even when batch-importing historical data.

### Reference Docs
- Schema migrations & seeding: `docs/backend/DATABASE_LOCAL_SETUP.md`
- API coverage & behaviour: `docs/api/API_DOCUMENTATION.md`
- AWS deployment checklist: `docs/aws/AWS_DEPLOY_GUIDE.md`

---

## 2. Legacy Fix Log (Node.js Era, Archived)

> **Status:** ✅ Completed (kept for historical context; current stack is Spring Boot).

During the original Node.js rollout (Sequelize ORM) the team resolved numerous alias and eager-loading bugs that broke `/auth/me`, PT listings, and nearby search. Key takeaways retained here in case we revisit the legacy service:

- **Middleware:** `verifyToken` now includes the PT profile (`as: 'ptProfile'`) so session refresh no longer drops vital data.
- **Auth Controller:** `/auth/me` enriches responses with PT metadata when available, matching the frontend’s expectations.
- **PT Controller/Search:** Every query uses explicit aliases (`as: 'user'`, `as: 'location'`, etc.) preventing Sequelize’s “You must use the 'as' keyword” errors.
- **Search Module:** Consolidated nearby search to return gym/PT/offer cards with prepared display names and coordinates.
- **Frontend Contracts:** Documented response changes so FE could adapt quickly (PT profiles now under `user`, search results include `name`, etc.).

Although the Spring Boot migration replaces this code path, carrying the lessons forward helps avoid similar issues in Hibernate/JPA mappings and ensures parity with the current REST contract.

---

## 3. Engineering Log (10 Oct 2025)

- Rebuilt broken Spring components (`OfferService`, `OfferResponse`, `OfferSearchResponse`, `MediaController`) so the project compiles on Java 21.
- Enabled Flyway migrations end-to-end, added idempotent seed script (`V3__seed_minimal_data.sql`), and wired the Gradle plugin for local db bootstrap.
- Added global OpenAPI bearer security (`OpenApiConfig`) and marked protected endpoints so Swagger now attaches JWT headers automatically.
- Tightened `SecurityConfig`: swagger/public routes remain open, while gym creation/assignment now requires `GYM_STAFF` authority and admin routes stay locked to `ADMIN`.
- Generated dev JWT helpers for Postman/Swagger testing, verified `/api/v1/auth/register`, gym + PT flows, and ratings with the new seed data.

---

## 3. Next Actions

1. Monitor new Flyway bootstrap (`V0__create_core_tables.sql` + V1–V3) and remove any remaining references to the old Docker init script in downstream docs/scripts.
2. Expand automated tests around security & moderation once Flyway stabilises schema baseline.
3. Keep this plan living—update roadmap and archive sections after each sprint so onboarding engineers understand both current scope and historical context.
