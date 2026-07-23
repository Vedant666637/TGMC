# Product Requirements Document (PRD)
## TGM-C — Parental Control Platform

**Version:** 1.0
**Status:** Draft for Review
**Owners:** Product / Engineering
**Last Updated:** June 29, 2026

---

## 1. Executive Summary

TGM-C is a parental control platform consisting of:
- An **Android mobile app** with two modes: **Parent mode** and **Child mode** 
- A **Web Admin Dashboard** used exclusively by internal company administrators, covering: Admin Login, a Dashboard overview (total downloads, number of devices), Parent/Child login counts, Users Information (email), Storage, and system Health — **not** exposed to end-user parents.

The product lets a parent supervise a child's Android device: app usage, screen time, location, web content, and (with explicit consent flows) remote camera, screen mirroring, and live audio monitoring.

This PRD covers scope, personas, functional/non-functional requirements, compliance posture, monetization, and a phased roadmap.

---

## 2. Goals & Non-Goals

### 2.1 Goals
- Give parents real-time visibility and control over a minor child's Android device.
- Provide a safe, legally defensible consent model (child is aware monitoring exists).
- Provide internal Admins a single dashboard to manage accounts, billing, abuse reports, and device fleet health — with **no access to live camera/mic/screen feeds** by default (see 6.3).
- Ship a production-grade, scalable, secure system from day one (not a prototype).

### 2.2 Non-Goals (v1)
- **iOS support, in any form.** TGM-C is **Android-only**, for both Parent and Child apps. This is a confirmed product decision, not a "v1 limitation" — see Section 6.4.
- Covert/stealth monitoring of a partner, spouse, or adult without their consent. **TGM-C is positioned and engineered only for parent–minor child supervision.** This affects store policy compliance, legal exposure, and is treated as a hard product boundary, not a configurable option.
- Employee monitoring (separate product line if pursued later).

---

## 3. User Personas

| Persona | Description | Primary Surface |
|---|---|---|
| **Parent** | Account owner/guardian. Pairs child devices, configures rules, views activity. | Android app (Parent mode) |
| **Child** | Device holder, typically a minor. Sees a visible companion app (not silently hidden — see 6.2). Can trigger SOS, sees their own time limits, and uses parent-gated social/consumer features (status, posts, reels, videos, shopping store) — see Section 5.2. | Android app (Child mode) |
| **Admin** | Internal employee. Logs in with email/password to the Dashboard, which shows total downloads, number of devices, Parent/Child login counts, Users Information (email), Storage, and system Health. | Web Dashboard only |
| **Family Member (secondary parent/guardian)** | Invited by primary parent, shares visibility. | Android app (Parent mode, limited role) |

---

## 4. Scope Summary

| Surface | Users | Platform |
|---|---|---|
| Parent App | Parents, secondary guardians | Android (native) |
| Child App | Children | Android (native) |
| Admin Dashboard | Internal staff only | Web (responsive, desktop-first) |

No public-facing web dashboard for parents in v1 (Android-only for end users, as specified). This can be a fast-follow (v2) if validated demand exists.

**Confirmed: Android-only, no iOS support for Parent App or Child App, on any roadmap phase.** See Section 6.4 for the rationale.

---

## 5. Functional Requirements

### 5.1 Parent App — Core Features (confirmed scope)

| # | Feature | Description |
|---|---|---|
| F1 | **Parent Login** | Email + password account login (social login optional later). Required before any other screen is accessible. Includes "Forgot Password" / reset-via-email flow. Session persists via secure refresh token; logout available from settings. |
| F2 | **Pairing** | After login, parent generates an invite code/QR/link; child installs Child App and enters code to bind devices to the parent's account. *(Implicit prerequisite for all features below — child device must be paired first.)* |
| F3 | **App Block** | Block/allow specific apps individually or by category (Social, Games, Streaming). |
| F4 | **Time Schedule** | Downtime windows (School Hours, Bedtime, custom); per-app/category daily time limits; instant "pause all" lock. |
| F5 | **Alert** | Push notifications for: new app install, blocked-content keyword detected, geofence entry/exit, low battery, SOS trigger. |
| F6 | **App Install** | Log and notify on new app installs/uninstalls on the child's device; optionally auto-block unapproved installs. |
| F7 | **Remote Camera** | On-demand front/rear camera snapshot or short live view; flashlight toggle (rear only). Requires the visible on-device indicator described in Section 6.2. |
| F8 | **Screen Mirroring** | Real-time view of the child's screen. |
| F9 | **GPS** | Real-time location, location history, and geofence zones with enter/exit alerts. |
| F10 | **Live Listening (One-Way Audio)** | Live microphone stream; optional recording with retention limits and parent-side storage controls. |

> Features F11+ (Activity Reports, SOS, Family Sharing, Multi-child management, Web/Content Filtering) from the original draft are retained as **Phase 2/5 roadmap items** (Section 11) but are not in the confirmed v1 list above — flag if any should be pulled into v1.

### 5.2 Child App — Core Features (confirmed scope)

| # | Feature | Description |
|---|---|---|
| C1 | **Visible persistent notification/icon** | Shown whenever monitoring features (camera/mic/screen) are active (see 6.2 — non-negotiable for store compliance and ethics). |
| C2 | **Trigger SOS** | One-tap emergency alert to all linked parents, with siren + instant location. |
| C3 | **Time Limits (visibility)** | Child sees remaining screen time / blocked status, reducing confusion and conflict. |
| C4 | **Status** | WhatsApp-style "Status" feed — short-lived **educational** photo/text/video updates **uploaded by Admin** and visible to both Parent and Child within the app (Section 5.2.1). |
| C5 | **Posts** | Persistent (non-expiring) **educational** posts/feed entries, **uploaded by Admin** and visible to both Parent and Child — not a public/open feed. |
| C6 | **Reels** | Short-form vertical **educational** video content, **uploaded by Admin** and viewed by both Parent and Child within the app (Section 5.2.1). |
| C7 | **Videos** | Longer-form **educational** video content, **uploaded by Admin** and viewable by both Parent and Child within the app. |
| C9 | **Shopping Store** | In-app store the child can browse/purchase **educational items/products** from; product catalog is curated by Admin (Section 5.2.2). Purchase **payment** still requires parent authorization (Section 5.2.2 flags why this part stays with the parent). |
| C10 | **Consent/onboarding screen** | At pairing, child (or parent on child's behalf for younger kids) acknowledges what is monitored and what social features are active. |

#### 5.2.1 Admin-Curated Educational Content Model (C4–C7)
- Status, Posts, Reels, and Videos (C4–C7) are **not open user-generated content** — they are **educational content** uploaded directly by **Admin (internal platform staff)** for the family, and are visible to both Parent and Child. Neither the child nor the parent uploads this content; they only consume it.
- Because this content is Admin-sourced and restricted to educational subject matter (rather than child-submitted or general entertainment content), there is no child-to-child content moderation queue for Status/Posts/Reels/Videos — Admin reviews/clears content for age-appropriateness and accuracy before publishing it, in the same step as uploading it.
- New Admin Dashboard feature required to support content publishing — added as **A8: Content Publishing** in Section 5.3.

#### 5.2.2 Shopping Store Notes (C9)
- **Catalog management (by Admin):** Admin curates which **educational items/products** appear in the store — this part of "Admin, not Parent" makes sense operationally (Admin manages the storefront, like any e-commerce backend).
- **Payment authorization (stays with Parent):** the actual charge has to be authorized by whoever owns the payment method — that's the parent's card, not the platform's. Recommend: child requests a purchase → **parent** receives a push notification and approves/declines before the charge completes → Admin never has the authority to spend a parent's money on the child's behalf. Flagging this explicitly since "not from parent, by admin" could be read as applying to the whole feature — happy to adjust if you did intend Admin-side purchase approval, but it has fraud/liability implications worth a deliberate decision rather than a default.
- Needs its own sub-PRD before build either way: spend limits/allowance model, refund/dispute handling, and PCI compliance scope (separate workstream from monitoring features).

> **Note — confirmed scope:** C4–C7 and C9 turn the Child App from a *pure parental-control agent* into an **Admin-curated educational content hub + educational-items storefront for families**. This is narrower than a general social/shopping feature set, but still materially adds to the product:
> - **Compliance surface:** even Admin-sourced, educational-only image/video content distributed to minors should still pass through a basic content-safety/age-appropriateness review before publishing, and most app stores require additional safety disclosures for any app showing minors media.
> - **Engineering surface:** media storage/CDN, a content publishing pipeline for Admin (upload, schedule, take-down) restricted to educational material, and a payments/PCI workstream for the educational-items store (C9) — each comparable in size to the original monitoring feature set.
> - Recommendation carried over from the Storage discussion: keep this as a clearly labeled **Phase 6 — Educational Content & Shopping Module** in the roadmap (Section 11) with its own mini-PRD, rather than shipping it inside v1 alongside the monitoring core.

### 5.3 Web Admin Dashboard (Internal Only — confirmed scope)

| # | Feature | Description |
|---|---|---|
| A1 | **Admin Login** | Email + password login for internal staff. MFA strongly recommended (Section 7) given the sensitivity of the data reachable from this dashboard. |
| A2 | **Dashboard (Overview)** | Landing page after login. Shows aggregate KPIs: **total app downloads** (Parent App + Child App, broken out by platform/store) and **number of active devices** (paired child devices currently online vs. total ever paired). |
| A3 | **Parent Logins / Child Logins** | Counts of **total registered parent accounts** and **total paired child profiles**, with trend over time (daily/weekly/monthly growth). Note: "Child Logins" here means *paired child devices*, not a separate child credential — see login model below. |
| A4 | **Users Information** | Searchable list of parent accounts showing **email**, signup date, subscription status, and number of linked child devices. (Drill-in should not expose live camera/mic/screen content — see design principle below.) |
| A5 | **Storage** | Storage usage metrics (recordings, snapshots, media). **Planned for future feature expansion**: once additional in-app modules ship — e.g. shopping, status updates, reels, video, and posts — this section will also report storage consumed per feature/module so Admins can monitor infrastructure cost and growth. *(Flagged below — this is a notable scope expansion beyond parental-control monitoring; see callout.)* |
| A6 | **Health** | System health view: API uptime, background job status, push notification delivery success rate, error rates — i.e., is the platform "up and working" or degraded. |
| A7 | **Logout** | Ends the Admin session and invalidates the session token. |
| A8 | **Content Publishing** | *(New — required by Section 5.2.1.)* Screen for Admin to upload and manage educational Status, Posts, Reels, and Videos content (publish, schedule, take down), with an audit trail of who published what. |

> **Design principle (carried over from v1.0):** The Admin Dashboard manages the *business and platform health*, not individual children's live feeds. Nothing in A1–A7 grants an employee access to a specific child's live camera, microphone, or screen content. Admin visibility into "Users Information" is limited to account-level metadata (email, plan, device count) — not surveillance content. **A8 is the one deliberate exception** — by design, Admin uploads and manages the Status/Posts/Reels/Videos content shown to families, because that content is Admin-sourced rather than child-submitted (Section 5.2.1).

> **Note on A5 (Storage) — confirmed scope:** The future features mentioned in A5 (shopping, status, reels, videos, and posts) are confirmed to be **Admin-curated, educational content/items only** — not open social or entertainment content. This meaningfully narrows the compliance and moderation surface versus a general-purpose social feature: content is staff-sourced and subject-matter-restricted (educational), rather than open-ended user-generated or entertainment content. Still recommend tracking this as a clearly scoped **Phase 6 — Educational Content & Shopping Module** initiative with its own mini-PRD once core monitoring is live, so the content-sourcing workflow (curation, upload, review for age-appropriateness) and the shopping catalog don't get folded silently into "Storage."

### 5.4 Authentication Model Summary (all three surfaces)

| Surface | Needs login? | Model | Notes |
|---|---|---|---|
| **Parent App** | ✅ Yes | Email + password (F1) | Owns the account, billing, and all child pairings. Standard signup/login/forgot-password flow. |
| **Child App** | ✅ Yes, but not a separate credential | **Pairing, not login** | The child does not create a username/password. The Parent generates a one-time invite code/QR (F2); the Child App is activated by entering that code, which links it permanently to the parent's account. From then on it just runs in the background — no login screen the child interacts with day-to-day. |
| **Admin Dashboard** | ✅ Yes | Email + password (A1), MFA recommended | Internal staff only. Stricter than parent login given access to account/billing data across all users. |

This is why "Parent Logins / Child Logins" in A3 is phrased the way it is: it's really *parent accounts* (real logins) vs. *paired child devices* (no independent login, just a pairing count).

---

## 6. Privacy, Legal & Compliance (Critical — Read Before Build)

This category of app draws significant regulatory and app-store scrutiny. These are **requirements**, not nice-to-haves, for a "full production app meant for real users."

### 6.1 Regulatory Landscape
- **COPPA (US)** — applies to collection of personal data from children under 13; requires verifiable parental consent flows and strict data minimization for that age group.
- **GDPR-K / UK GDPR (EU/UK)** — additional protections for children's data; lawful basis and parental consent considerations.
- **State/country-specific monitoring laws** — several jurisdictions have laws specifically addressing electronic monitoring of minors and require disclosure; some require monitoring software to be **visibly disclosed on the device** (this directly drives requirement 6.2).
- Recommend formal legal review per target launch market before GA, not just at the PRD stage.

### 6.2 On-Device Transparency Requirement (Non-Negotiable)
- The Child App **must not be hidden or disguised**. No "stealth mode" hiding the app icon.
- A **persistent, visible notification/icon** must be shown whenever camera, microphone, or screen access is active.
- This is both an ethical requirement and increasingly an **app store policy requirement** (Google Play's policy on monitoring apps with access to sensitive permissions like camera/microphone/screen capture restricts covert operation and requires in-app disclosure to the device user).
- Product decision: build trust-based transparency as a feature, not a workaround — children should know monitoring exists, even if they don't control it.

### 6.3 Google Play Policy Risk
- Apps with "stalkerware-style" capabilities (covert location/camera/mic/SMS access) face the strictest tier of Play Store review (the "Mobile Unwanted Software"/"Commercial Spyware" policies).
- To remain compliant, TGM-C must, at minimum:
  - Disclose itself as a parental-control/monitoring app in its Play Store listing and at runtime.
  - Never hide its icon or run fully covertly.
  - Restrict marketing language that suggests use on non-consenting adults (spouses, employees, etc.).
  - Implement an in-app, persistent monitoring indicator (6.2).
- **Action item:** Engage Google Play's policy team / submit for the "approved use case" declaration required for apps requesting sensitive permissions (`AccessibilityService`, background location, etc.) before submission — this process can take weeks and should be on the critical path of the roadmap, not an afterthought.

### 6.4 Android-Only — No iOS Support (Confirmed)
- TGM-C does not support iPhone/iOS in any capacity — not for the Parent App, not for the Child App, and not on any future roadmap phase.
- Context for why this is a clean product boundary rather than just a technical gap: Apple does not provide public APIs for remote camera activation, screen mirroring, or background microphone capture by a separate "controller" app, so a true iOS equivalent of TGM-C's core features (Remote Camera, Screen Mirroring, Live Listening, reliable App Block via Accessibility Service) is not achievable on iOS the way it is on Android. A partial iOS version would only be able to offer a fundamentally different, much thinner feature set via Apple's own Screen Time/Family Sharing framework — which isn't what TGM-C is.
- **Practical implication for go-to-market:** the Parent App should also be Android-only (not "Android child + iOS parent"), so parents don't download the app expecting iPhone support for either side. App Store listings, marketing pages, and onboarding copy should clearly state "Android only" to avoid confused installs and refund/support load from iPhone users.

### 6.5 Data Minimization & Retention
- Default retention windows (e.g., location: 90 days, recordings: 30 days) with parent-configurable deletion.
- Encrypt sensitive data at rest (camera/audio/screen recordings, location history) and in transit (TLS 1.2+, WebRTC DTLS-SRTP).
- Children's data should never be sold or used for ad targeting — should be an explicit, public commitment (also a regulatory expectation under COPPA/GDPR-K).

### 6.6 Abuse Prevention
- Trust & Safety queue (A3) to catch misuse (e.g., reports the app is being used on a non-consenting adult/partner rather than a minor child) — include an in-app/account-level reporting path and a defined investigation SLA.
- Consider age-verification signal at account setup (parent self-attests child's age; flag accounts where "child" device behavior patterns are inconsistent with a minor, for review).

---

## 7. Security Requirements

- TLS everywhere; certificate pinning in mobile apps for API calls.
- Encrypted local storage on Child App for cached rules/credentials (Android Keystore-backed).
- MFA available for Parent accounts; mandatory MFA for Admin accounts.
- All Admin actions audit-logged and immutable (A6).
- Principle of least privilege for Admin RBAC roles (A5).
- Regular third-party penetration testing before GA and on a recurring (e.g., annual) cadence given the sensitivity of data handled.
- Bug bounty program post-launch recommended given attack surface (remote camera/mic/screen access is a high-value target if compromised).

---

## 8. Non-Functional Requirements

| Category | Requirement |
|---|---|
| Availability | 99.9% uptime SLA for core API/backend |
| Latency | Location ping → parent alert: <10s; Live camera/screen session establish: <3s |
| Scalability | Support 1M+ paired device-pairs at GA+12mo horizon; horizontally scalable services |
| Battery impact | Child App background battery usage <5%/day under normal polling intervals |
| Offline resilience | Queue rule updates/alerts for delivery when child device reconnects |
| Localization | English at launch; architecture should support i18n from day one |
| Accessibility | Admin dashboard and Parent app meet WCAG 2.1 AA |

---

## 9. Monetization

| Plan | Price (suggested, validate via market research) | Devices | Features |
|---|---|---|---|
| Free trial | $0 | 1 device | Full feature trial |
| Monthly | $1 | up to 5 devices | Full feature set |
| Annual | $10/yr | up to 10 devices | Full feature set, best value |
| Family+ (future) | TBD | 10+ devices | Multi-guardian, priority support |

---

## 10. Success Metrics (KPIs)

- Activation rate: % of installs that complete pairing within 24h.
- D7/D30 retention of paired families.
- Feature adoption rate per core feature (App Block, GPS, Screen Mirroring, etc.).
- Support ticket volume per 1,000 active families (target: declining trend).
- Abuse report rate per 10,000 accounts (target: very low, monitored closely).
- Play Store rating and policy-strike count (target: zero strikes).
- Churn rate (monthly/annual).

---


## 11. Risks & Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Play Store rejects/suspends app under spyware policy | Launch blocker | Early policy team engagement (6.3); transparency-by-design (6.2); avoid stealth features entirely |
| OEM battery optimization kills background service | Feature reliability | Device Owner/Android Enterprise mode for managed devices; user education flow for battery-optimization whitelisting |
| Misuse on non-consenting adults | Legal/PR liability | T&S abuse queue, ToS restrictions, marketing copy review, account-level reporting |
| Data breach of camera/mic/location data | Severe reputational/legal damage | Encryption everywhere, pen testing, least-privilege admin access, no Admin access to live feeds |
| Regulatory change (COPPA/GDPR updates) | Compliance gap | Recurring legal review cadence, modular consent architecture |

---

## 12. Technology Stack & Infrastructure

- **Backend & Database:** Firebase (Authentication, Real-time Database / Firestore, and Cloud Storage for media assets).
- **Source Code & Version Control:** GitHub (for repository storage, version control, and CI/CD pipelines).

---

*End of PRD v1.0*
