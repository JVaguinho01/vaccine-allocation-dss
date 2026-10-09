# VaccDSS — Vaccination Decision Support System
## Technical and Functional Documentation

---

## Table of Contents

1. [System Overview](#1-system-overview)
2. [Technical Architecture](#2-technical-architecture)
3. [Authentication & Security](#3-authentication--security)
4. [System Modules](#4-system-modules)
   - 4.1 [Login](#41-login)
   - 4.2 [Dashboard — Allocation Request](#42-dashboard--allocation-request)
   - 4.3 [Regions](#43-regions)
   - 4.4 [Institutions](#44-institutions)
   - 4.5 [Patients](#45-patients)
5. [Allocation Process — Step by Step](#5-allocation-process--step-by-step)
   - 5.1 [User Input](#51-user-input)
   - 5.2 [Campaign Pace Parameters](#52-campaign-pace-parameters)
   - 5.3 [Patient Distribution — Cascade](#53-patient-distribution--cascade)
   - 5.4 [Effective Capacity & Waste Risk](#54-effective-capacity--waste-risk)
   - 5.5 [AHP Model — Weight Calculation](#55-ahp-model--weight-calculation)
   - 5.6 [Normalisation & Final Score](#56-normalisation--final-score)
   - 5.7 [Vaccine Distribution](#57-vaccine-distribution)
   - 5.8 [Per-Institution Results](#58-per-institution-results)
   - 5.9 [Final Response](#59-final-response)
6. [AHP Criteria — Detailed Definition](#6-ahp-criteria--detailed-definition)
7. [Glossary](#7-glossary)

---

## 1. System Overview

**VaccDSS** (Vaccination Decision Support System) is a web application designed to support decision-making in vaccine distribution during vaccination campaigns. The system applies the **AHP (Analytic Hierarchy Process)** model to weigh multiple criteria and distribute vaccine doses across health institutions in a given region in an objective and transparent way.

### Core Objective

Answer the question: *"Given a number of available doses, a region, and a campaign duration — how many doses should each institution receive?"*

The answer is calculated considering factors such as each institution's demand, the risk level of its patients, logistic cost, storage capacity, and the risk of vaccine waste.

### Access Profiles

| Role | Access |
|------|--------|
| **admin** | Read and write access to all modules |
| **user** | Read and write access to all modules |

Authentication is handled via email and password managed directly in Supabase (PostgreSQL).

---

## 2. Technical Architecture

| Layer | Technology |
|-------|-----------|
| **Frontend** | Angular 21 (standalone components, signals, reactive forms, CDK drag-and-drop) |
| **Backend** | Spring Boot 3 (Java 21, plain JDBC — no ORM) |
| **Database** | PostgreSQL on Supabase with PostGIS extension for geographic data |
| **Authentication** | JWT (JSON Web Token) with 30-minute expiry |
| **Communication** | REST API over HTTP, port 8080 (backend) and 4200 (frontend) |

### Main Tables

| Table | Description |
|-------|-------------|
| `regions` | Geographic regions (with PostGIS geometry boundary) |
| `institutions` | Health institutions (with PostGIS geography location) |
| `patients` | Registered patients (with location, risk_level, risk_exposure) |
| `vaccines` | Available vaccine types |
| `user_profiles` | Links Supabase Auth users to roles |
| `roles` | System roles (admin, user) |

---

## 3. Authentication & Security

### Login Flow and Token

1. The user enters their email and password on the Login screen.
2. The frontend sends a `POST /auth/login` request to the backend with the credentials.
3. The backend validates the credentials against the `user_profiles` table and generates a JWT with a **30-minute expiry**.
4. The token is stored in the browser's `localStorage`.
5. Every subsequent HTTP request includes the token in the `Authorization: Bearer <token>` header.

### Automatic Expiry — 3 Layers

The system checks token expiry at three distinct moments:

- **App startup**: if the stored token is expired, it is removed and the user is redirected to the login screen.
- **Every navigation (route guard)**: before accessing any protected page, the guard verifies that the token is still valid.
- **HTTP 401 responses**: if the backend rejects a request due to an invalid token, the interceptor triggers automatic logout.

### SQL Injection Protection

All database access uses **parameterised queries** (`?` placeholders in JDBC) — never string concatenation with user input. Dynamic sort fields are validated via `switch` statements with a **whitelist** of allowed column names.

---

## 4. System Modules

### 4.1 Login

The login screen is split into two panels:

- **Left panel**: visual branding with the VaccDSS name and tagline "Vaccination Decision Support System".
- **Right panel**: form with email, password (with visibility toggle) and a "Sign in" button.

After successful authentication the user is redirected to the **Dashboard**.

---

### 4.2 Dashboard — Allocation Request

The Dashboard is the central module of the system. This is where the user configures and runs the vaccine allocation process.

The screen is organised into two main areas:

**Configuration area (left side):**
- Region selection
- Total number of vaccines available
- Campaign duration in days
- Vaccination pace
- AHP criteria ordering (drag-and-drop)
- "Run Allocation" button

**Results area (right side):**
- AHP Weights with visual progress bars
- Consistency Check (CI, CR and consistency status)
- Per-institution results table (score, allocated vaccines, completion day, occupancy, restocks)

---

### 4.3 Regions

Lists all registered regions. Allows creating and removing regions. Each region has a name and a geographic boundary (PostGIS polygon).

---

### 4.4 Institutions

Lists all health institutions with name/region filters, column sorting and pagination. Allows:

- **Create**: name, region, storage capacity, daily vaccination rate, address, zip code
- **Edit**: name, storage capacity and daily rate only (address is preserved)
- **Remove**: soft delete — the institution gets `removed = true` and no longer appears in queries or allocations

---

### 4.5 Patients

Lists all patients with filters and pagination. Allows:

- **Create**: first name, last name, date of birth, gender, region, address, zip code, risk level, risk exposure
- **Edit**: only **Risk Level** and **Risk Exposure** (demographic and location data are not changed on the edit screen)
- **Activate/deactivate**: only patients with `is_active = true` participate in allocation calculations

---

## 5. Allocation Process — Step by Step

This section describes the complete internal workings of an allocation request, from user input to the final result.

---

### 5.1 User Input

The user fills in the following fields before running the allocation:

| Field | Type | Description |
|-------|------|-------------|
| **Region** | Dropdown | The geographic region to allocate vaccines to |
| **Total Vaccines Available** | Integer (min. 1) | Total doses available for distribution |
| **Campaign Days** | Integer (min. 1, default 30) | Duration of the campaign in days |
| **Vaccination Pace** | relaxed / standard / intensive | How intensively institutions use their capacity |
| **Criteria Order** | Drag-and-drop ranking | Relative priority of the 6 AHP criteria — top item has the highest weight |

The user **drags** the criteria to reorder them. The 6 available criteria are:

1. Demand
2. Risk Level
3. Risk Exposure
4. Logistic Cost
5. Capacity
6. Waste Risk

---

### 5.2 Campaign Pace Parameters

The **vaccination pace** defines two multipliers that control how intensively each institution uses its resources during the campaign:

| Pace | Storage Utilisation (storagePct) | Daily Rate Utilisation (ratePct) |
|------|----------------------------------|----------------------------------|
| **Relaxed** | 30% | 50% |
| **Standard** | 55% | 75% |
| **Intensive** | 85% | 100% |

- **storagePct**: fraction of physical storage capacity effectively used. For example, with *Intensive* pace (85%), an institution with 10,000-dose capacity uses 8,500 effectively.
- **ratePct**: fraction of the daily vaccination rate applied. With *Intensive* pace (100%), an institution rated at 200/day uses its full capacity. With *Relaxed* (50%), it uses only 100/day.

---

### 5.3 Patient Distribution — Cascade

This is the most complex step. Its goal is to determine **how many patients** will be served by each institution, respecting each institution's capacity and geographic proximity.

#### 5.3.1 Rate Cap per Institution

For each institution in the region, the maximum number of patients it can absorb during the campaign is calculated:

```
rateCap = floor(dailyVaccinationRate × ratePct × campaignDays)
```

Example: Institution with 200 vaccinations/day, Intensive pace (100%), 30-day campaign:
→ rateCap = floor(200 × 1.00 × 30) = **6,000 patients**

#### 5.3.2 Patient Preferences

The database uses PostGIS (`ST_Distance`) to compute, for every active patient in the region, the ranked list of all institutions ordered by **geodesic distance**. The nearest institution gets `pref_rank = 1`, the second nearest `pref_rank = 2`, and so on.

#### 5.3.3 Cascade Algorithm (Multi-Round Greedy)

The system runs an iterative patient-assignment algorithm:

**Round 1:**
- Every unassigned patient proposes to their preferred institution (the nearest one).
- Each institution accepts proposals in ascending distance order, up to its `rateCap`.
- Rejected patients (because the institution is full) advance to their next preference.

**Subsequent rounds:**
- Rejected patients repeat the process with their next preferred institution.
- The cycle continues until all patients are assigned or exhaust all options.

**Cascade result:**
For each institution:
- **demand**: number of assigned patients
- **riskLevel**: average risk_level of assigned patients
- **riskExposure**: average risk_exposure of assigned patients

This algorithm ensures that patients "overflow" to more distant institutions only when nearer ones are full, accurately reflecting each institution's real demand.

---

### 5.4 Effective Capacity & Waste Risk

After the cascade, the effective vaccination capacity for the full campaign period is calculated for each institution:

```
storageAlloc = storageCapacity × storagePct
rateAlloc    = dailyVaccinationRate × ratePct × campaignDays
effectiveCap = min(storageAlloc, rateAlloc)
```

**Waste Risk** measures the risk of stored vaccines not being administered:

```
wasteRisk = max(0, 1 − effectiveCap / storageAlloc)
```

- **wasteRisk = 0**: the institution can use all available stored doses → no waste risk.
- **wasteRisk = 1**: vaccination rate is so low that virtually no stored vaccines will be administered.

---

### 5.5 AHP Model — Weight Calculation

**AHP (Analytic Hierarchy Process)** converts the user's drag-and-drop ordering into numerical weights that reflect the relative importance of each criterion.

#### Step 1 — Pairwise Comparison Matrix (6×6)

The user's ordering is converted into a matrix of pairwise comparisons. Each cell `matrix[i][j]` represents "how many times criterion i is more important than j":

```
If criterion i ranks ahead of criterion j:
  difference = position_j − position_i
  matrix[i][j] = min(|difference| + 1, 9)   → i is more important
  matrix[j][i] = 1 / matrix[i][j]            → j is less important

If i == j:
  matrix[i][j] = 1   → criterion compared with itself
```

**Example:** Ordering `Demand > Risk Level > Risk Exposure > Capacity > Logistic Cost > Waste Risk`

- Demand vs. Waste Risk: 5 positions apart → matrix[0][5] = min(6, 9) = 6, matrix[5][0] = 1/6
- Risk Level vs. Logistic Cost: 3 positions apart → matrix[1][3] = 4, matrix[3][1] = 1/4

#### Step 2 — Geometric Mean Method

For each criterion i (matrix row):

```
product_i = matrix[i][0] × matrix[i][1] × ... × matrix[i][5]
geometricMean_i = (product_i) ^ (1/6)
```

Weights are normalised so they sum to 1:

```
weight[i] = geometricMean_i / Σ(geometricMean_j)
```

#### Step 3 — Consistency Check

AHP verifies whether the user was consistent in their preferences (if A > B > C, then A > C must hold).

**λmax** (maximum eigenvalue):
```
weightedSum[i] = Σ(matrix[i][j] × weight[j])
λmax = (1/n) × Σ(weightedSum[i] / weight[i])
```

**Consistency Index (CI)**:
```
CI = (λmax − n) / (n − 1)
```

**Consistency Ratio (CR)**:
```
CR = CI / RI
```
Where `RI` is the tabulated Random Index (for n=6 criteria, RI = 1.24).

**Interpretation:**
- `CR < 0.10` → **Consistent** ✓ — ordering is logical and weights are reliable.
- `CR ≥ 0.10` → **Inconsistent** ✗ — preferences are contradictory.

---

### 5.6 Normalisation & Final Score

#### Adjusting Weights for Empty Criteria

If no patient was assigned to any institution (all demands are 0), the weights for **Demand**, **Risk Level** and **Risk Exposure** are zeroed out and the remaining weights are **renormalised** to continue summing to 1.

#### Per-Criterion Normalisation (range [0, 1])

| Criterion | Normalisation | Logic |
|-----------|---------------|-------|
| **Demand** | demand / maxDemand | Higher demand → higher priority |
| **Risk Level** | riskLevel / maxRiskLevel | Higher average risk → higher priority |
| **Risk Exposure** | riskExposure / maxRiskExposure | Higher average exposure → higher priority |
| **Logistic Cost** | minCost / cost (or 1.0 if cost = 0) | Lower cost → better score (inverted criterion) |
| **Capacity** | capacity / maxCapacity | Higher effective capacity → better score |
| **Waste Risk** | 1 − wasteRisk | Lower waste → better score (inverted criterion) |

#### Final Score Calculation

```
finalScore = (wDemand       × demandNorm)
           + (wRiskLevel    × riskLevelNorm)
           + (wRiskExposure × riskExposureNorm)
           + (wCost         × costNorm)
           + (wCapacity     × capacityNorm)
           + (wWaste        × wasteNorm)
```

The `finalScore` is in the range [0, 1] and represents the **relative priority** of each institution to receive vaccines.

---

### 5.7 Vaccine Distribution

With `finalScore` calculated for each institution, vaccines are distributed proportionally:

#### 1 — Proportional Distribution

```
proposed[i] = round(totalVaccines × (finalScore[i] / Σ finalScore))
```

#### 2 — Dual Cap

Each institution's allocation is capped at the minimum of the rate-based capacity and real patient demand:

```
rateCappedCapacity = min(
    floor(dailyRate × ratePct × campaignDays),   ← rate-based cap
    ceil(demand)                                   ← demand-based cap
)

allocated[i] = min(proposed[i], rateCappedCapacity[i])
```

This ensures **no institution receives more vaccines than it can administer**, nor more than the number of assigned patients.

#### 3 — Remainder Distribution

If doses remain after the initial distribution (because some institutions hit their cap), the remainder is distributed **one dose at a time** to the highest-scoring institutions that still have available capacity.

---

### 5.8 Per-Institution Results

For each institution, the following final indicators are computed:

#### Completion Day

The day on which the last allocated dose is administered:

```
completionDay = ceil(allocatedVaccines / (dailyRate × ratePct))
```

Example: 1,200 allocated doses, daily rate 580 × 100% → ceil(1200 / 580) = **day 3**

#### Effective Capacity

How many vaccines **could** have been administered in the same active period:

```
effectiveCapacity = round(dailyRate × ratePct × completionDay)
```

Used to calculate the institution's **occupancy percentage**.

#### Occupancy

```
occupancy% = allocatedVaccines / effectiveCapacity × 100
```

Displayed with colour coding:
- 🟢 Green: ≤ 50% — institution has spare capacity
- 🟡 Yellow: 51–80% — moderate utilisation
- 🔴 Red: > 80% — institution near its daily limit

#### Restock Days

Allocated doses may exceed an institution's storage capacity. The system calculates which days a stock replenishment must arrive:

```
effectiveStorage = storageCapacity × storagePct
effectiveDaily   = dailyRate × ratePct
daysPerCycle     = floor(effectiveStorage / effectiveDaily)
```

`daysPerCycle` is how many days the initial stock lasts. If less than `completionDay`, restocks are needed:

```
restockDays = [daysPerCycle, 2×daysPerCycle, 3×daysPerCycle, ...]
              (only days less than completionDay)
```

Each restock delivers:
```
restockDoses = round(storageCapacity × storagePct)
```

**Practical example:**
- Storage: 2,000 doses × 85% (Intensive) = 1,700 effective doses
- Daily rate: 100 vaccines/day
- daysPerCycle = floor(1700 / 100) = **17 days**
- completionDay = 30 days
- restockDays = [**17**] → one restock on day 17

The user can view restock days by hovering over the "Restocks" column in the results table.

---

### 5.9 Final Response

The system returns and displays the following in the Dashboard:

| Field | Description |
|-------|-------------|
| **Weights** | Final weight of each AHP criterion (after renormalisation if applicable), as a percentage |
| **Consistency** | CI, CR and consistency indicator (✓ / ✗) |
| **Allocations** | Table sorted by `finalScore` (descending) with all per-institution indicators |
| **Total Allocated** | Sum of all distributed vaccines |
| **Total Unallocated** | Vaccines not distributed (totalVaccines − totalAllocated) |
| **Total Patients** | Number of active patients in the region |
| **Unvaccinated Patients** | Patients not receiving a vaccine in this campaign (totalPatients − totalAllocated) |

---

## 6. AHP Criteria — Detailed Definition

| # | Criterion | What it measures | Data source |
|---|-----------|-----------------|-------------|
| 1 | **Demand** | Number of active patients assigned to the institution by the cascade algorithm | Patient count after cascade |
| 2 | **Risk Level** | Average `risk_level` of assigned patients (scale 1–5) | `patients` table |
| 3 | **Risk Exposure** | Average `risk_exposure` of assigned patients (scale 1–5) | `patients` table |
| 4 | **Logistic Cost** | Distance in km from the institution to the network's largest warehouse × €0.50/km | Computed by PostGIS (ST_Distance) |
| 5 | **Capacity** | Effective Capacity: `min(storageCapacity × storagePct, dailyRate × ratePct × campaignDays)` | `institutions` table + pace |
| 6 | **Waste Risk** | Probability of vaccine waste: `1 − effectiveCap / storageAlloc` | Computed internally |

### Notes on Logistic Cost

The logistic cost is automatically computed as the **geodesic distance** (straight-line, using PostGIS) between the institution and the network's largest warehouse (the institution with the highest `storage_capacity`), multiplied by €0.50/km. An institution that **is** the warehouse has cost 0 and receives the maximum score for this criterion.

### Notes on Risk Level and Risk Exposure

- `risk_level`: represents the clinical severity of the patient (e.g. 5 = high clinical risk, urgent need for vaccination).
- `risk_exposure`: represents the patient's degree of exposure to the infectious agent (e.g. 5 = worker in direct contact with infected individuals).

Both are configured by the user when creating or editing a patient.

---

## 7. Glossary

| Term | Definition |
|------|------------|
| **AHP** | Analytic Hierarchy Process — multi-criteria decision method that converts subjective priorities into numerical weights |
| **Allocation** | The process of distributing vaccine doses across institutions in a region |
| **Campaign Days** | Total duration of the vaccination campaign in days (default: 30) |
| **Cascade** | Iterative patient-assignment algorithm based on geographic proximity, with overflow to the next nearest institution when the preferred one is full |
| **Completion Day** | The day on which the last allocated dose is administered at an institution |
| **CR (Consistency Ratio)** | Ratio measuring the coherence of AHP preferences; must be below 0.10 to be considered consistent |
| **Demand** | Number of patients assigned to an institution after the cascade algorithm |
| **Effective Capacity** | Number of doses the institution could have administered during its active campaign period |
| **Institution** | A health facility (hospital, health centre, USF) participating in the vaccination campaign |
| **JWT** | JSON Web Token — stateless authentication mechanism with a 30-minute expiry |
| **Logistic Cost** | Estimated transport cost to the institution (distance to warehouse × €0.50/km) |
| **Occupancy** | Percentage utilisation of an institution's effective capacity (allocatedVaccines / effectiveCapacity) |
| **Patient** | A person registered in the system as a vaccination candidate |
| **PostGIS** | PostgreSQL extension for geographic data — used to calculate geodesic distances and proximity rankings |
| **Rate Cap** | Maximum number of patients an institution can serve throughout the campaign given its vaccination rate |
| **ratePct** | Fraction of the daily vaccination rate applied, determined by the vaccination pace (50% / 75% / 100%) |
| **Region** | Administrative geographic unit grouping institutions and patients for allocation purposes |
| **Restock** | Replenishment of vaccine stock at an institution during the campaign when the initial storage runs out |
| **Risk Exposure** | A patient's degree of exposure to the infectious agent (scale 1–5) |
| **Risk Level** | A patient's clinical severity and vaccination urgency (scale 1–5) |
| **Score / Final Score** | AHP score for an institution in [0, 1] — determines the proportion of doses it receives |
| **storagePct** | Fraction of storage capacity used, determined by the vaccination pace (30% / 55% / 85%) |
| **Vaccination Pace** | Campaign intensity: Relaxed, Standard or Intensive — defines storagePct and ratePct multipliers |
| **Waste Risk** | Risk that stored vaccines will not be administered during the campaign (0 = no risk, 1 = maximum risk) |
| **Weight** | Relative importance of an AHP criterion in the final score calculation — all weights always sum to 1 |
